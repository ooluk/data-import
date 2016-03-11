/*
 *  Copyright 2015 Ooluk Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ooluk.ddm.dataimport.workers.dynamodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.MessageKey;
import com.ooluk.ddm.dataimport.Messages;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;
import com.ooluk.ddm.dataimport.rule.RulesEngine;
import com.ooluk.ddm.dataimport.workers.AbstractRuleBasedDataObjectReader;

/**
 * <p>
 * DynamoDBDataObjectReader is an implementation of the DataObjectReader for Amazon DynamoDB databases.
 * DynamoDBDataObjectReader extends the RuleBasedDataObjectReader thus leveraging the rules facility.
 * 
 * <p>
 * This reader can be configured for three scopes of scanning for DataObjects:
 * <ul>
 * <li>REGION: All tables in the region
 * <li>TABLES: Specific Table(s)
 * </ul>
 * </p>
 * 
 * <p>
 * This implementation uses three types of rules
 * <ul>
 * <li>Namespace rules
 * <li>Data Type rules
 * <li>Common Type rules
 * </p>
 * 
 * <p>
 * This class uses new ProfileCredentialsProvider() thus picking the credentials from the standard location. A future
 * version may the credentials / credentials file location to be supplied as parameters.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class DynamoDBDataObjectReader extends AbstractRuleBasedDataObjectReader {

	private final Logger log = LogManager.getLogger();

	/*
	 * Parameters passed on call to init(...) that need to be saved.
	 */
	// The case mode is optional and defaults to MIXED case
	private CaseMode caseMode = CaseMode.MIXED;

	// The AWS region for the DynamoDB
	private String awsRegion;

	// The list of tables is mandatory for scope = TABLES
	private List<String> tables;

	// Scope for the import
	private DynamoDBMetaDataScope scope;

	// A flag used to signal the completion of initial configuration
	private boolean configured = false;
	
	// DynamoDB client
	private DynamoDB dynamoDB;
	
	// Iterator over tables
	private Iterator<Table> regionItr;
	// Iterator over table names
	private Iterator<String> tablesItr;

	@Override
	public void init(Map<String, Object> params) {

		extractScope(params);
    	extractNamespacePrefix(params);
    	extractRulesParameters(params);
    	extractCaseMode(params);
		extractAWSRegion(params);
		configured = true;
		init();
	}

	/**
	 * This method should not be called externally for DynamoDBDataObjectReader. Call init(params) only.
	 */
	@Override
	public void init() {

		if (!configured) {
			String msg = Messages.getMessage(MessageKey.READ_NOT_CONFIG, "DynamoDBDataObjectReader");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		// Create a connection to DynamoDB 
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
		client.setEndpoint(awsRegion);
		dynamoDB = new DynamoDB(client);
		openResultSet();
	}

	/**
	 * This method extracts the scope from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractScope(Map<String, Object> params) {

		String readScope = (String) params.get("scope");
		if (readScope == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "scope");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		try {
			scope = DynamoDBMetaDataScope.valueOf(readScope.toUpperCase());
		} catch (IllegalArgumentException ex) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "scope");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}

		/*
		 * Read scope specific parameters For SCHEMA scope: list of schemas as a comma separated list For TABLE scope:
		 * schema name and table name
		 */
		switch (scope) {
			case REGION:
				break;
			case TABLES:
				configureForTablesScope(params);
				break;
		}
	}

	/**
	 * Configures the reader for TABLES scope.
	 * 
	 * @param params
	 *            initialization parameters as a parameter name-value map
	 */
	private void configureForTablesScope(Map<String, Object> params) {
		// Tables are provided as a comma separated list
		String csList = (String) params.get("tables");
		if (csList == null || csList.trim().isEmpty()) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "tables");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		tables = Arrays.asList(csList.split("(\\s)*,(\\s)*"));
	}

	/**
	 * Extracts the namespace prefix from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractNamespacePrefix(Map<String, Object> params) {

		namespacePrefix = ((String) params.get("namespacePrefix"));
		if (namespacePrefix == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "namespacePrefix");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}

		namespacePrefix = namespacePrefix.trim();
		if (namespacePrefix.isEmpty()) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_EMPTY, "namespacePrefix");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
	}

	/**
	 * Extracts the import rules from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractRulesParameters(Map<String, Object> params) {

		// Rule group is not mandatory
		String ruleGroup = ((String) params.get("ruleGroup"));
		if (ruleGroup != null) {
			super.setRuleGroup(ruleGroup);
		}

		ruleStore = ((RuleStore) params.get("ruleStore"));
		if (ruleStore == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "ruleStore");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
	}

	/**
	 * Extracts the case mode from the initialization parameters. The case mode is optional.
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractCaseMode(Map<String, Object> params) {

		// Case Conversion - not mandatory
		String mode = (String) params.get("case");
		if (mode != null) {
			try {
				caseMode = CaseMode.valueOf(mode.toUpperCase());
			} catch (IllegalArgumentException ex) {
				String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "case");
				super.appendStatusLine(msg);
				throwImportException(msg);
			}
		}
	}

	/**
	 * Extracts AWS region from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractAWSRegion(Map<String, Object> params) {

		awsRegion = (String) params.get("region");
		if (awsRegion == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "region");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
	}
	
	/**
	 * Opens the result set for the read.
	 */
	private void openResultSet() {
		switch (scope) {
			case REGION:
				TableCollection<ListTablesResult> tableList = dynamoDB.listTables();
				regionItr = tableList.iterator(); 
				break;
			case TABLES: 
				tablesItr = tables.iterator();
				break;				
		}
	}

	@Override
	public ScannedDataObject read() {

		switch (scope) {
			case REGION:
				return readNextTableFromRegion();
			case TABLES:
				return readNextTableFromTableList();
			default:
				throw new IllegalStateException("Illegal Scope Encountered");
		}
	}
	
	private ScannedDataObject readNextTableFromRegion() {

        ScannedDataObject dObj = null;

		if (regionItr.hasNext()) {
			Table table = regionItr.next();
			dObj = createDataObject(table);
		}
		
        return dObj;
		
	}
	
	private ScannedDataObject readNextTableFromTableList() {

        ScannedDataObject dObj = null;   
        
        if (tablesItr.hasNext()) {
        	String tableName = tablesItr.next();
        	Table table = dynamoDB.getTable(tableName);
        	try {
        		dObj = createDataObject(table);
        	} catch (ResourceNotFoundException e) {
    			// TODO Internationalize 
    			String msg = "Table " + table.getTableName() + " not found";
    			super.appendStatusLine(msg);
    			throwImportException(msg);			
    		}
        }
        return dObj;
		
	}

	@Override
	public void close() {
	}
	

	/**
	 * Creates a data object from a DynamoDB table.
	 * 
	 * @param table
	 *            DynamoDB table
	 * 
	 * @return ScannedDataObject representing a DynamoDB table.
	 */
    private ScannedDataObject createDataObject(Table table) {
		TableDescription tableDesc = table.describe();
		
    	// Namespace
        String nspaceRule = ruleStore.getRule("namespace", "name");
    	String oNamespace = nspaceRule == null ? namespacePrefix : 
    		nspaceRule.replace("%prefix%", namespacePrefix);
    				
        // Table Name
    	String oName = tableDesc.getTableName();
    	
    	// Partitioning Key
    	List<String> _keys = new ArrayList<>();
    	List<KeySchemaElement> keys = tableDesc.getKeySchema();
    	for (KeySchemaElement key : keys) {
    		_keys.add(key.getAttributeName());
    	}
    	
    	// Attributes
    	List<ScannedAttribute> attributes = new ArrayList<>();
		List<AttributeDefinition> list = tableDesc.getAttributeDefinitions();
		for (AttributeDefinition defn : list) {
			attributes.add(createAttribute(defn, _keys));
		}
		        
        // Create ScannedDataObject
        ScannedDataObject dObj = new ScannedDataObject(caseMode);
        dObj.setNamespace(oNamespace);
        dObj.setName(oName);
        dObj.setAttributes(attributes);
		return dObj;
    }
    
	/**
	 * Create a ScannedAttribute from a DynamoDB AttributeDefinition
	 * 
	 * @param attrDefinition
	 *            attribute definition
	 * @param key
	 * 			  partitioning HASH key for the table
	 *            
	 * @return ScannedAttribute representing a DynamoDB attribute.
	 */
    private ScannedAttribute createAttribute(AttributeDefinition attrDefinition, List<String> keys) {
    	
    	// Attribute Name
    	String aName = attrDefinition.getAttributeName();
    	boolean isKey = keys.contains(aName);

        String _type = attrDefinition.getAttributeType();

        // Data type - default: map to _type
        String aDataType = ruleStore.getRule("data-type", _type);
        if (aDataType != null) {
            aDataType = aDataType.replace("%type%", _type);
            aDataType = RulesEngine.processRule(aDataType);
        } else {
        	//super.appendStatusLine("WARNING: No data type rules were found... performing default mapping ...");
            aDataType = _type;
        }

        // Common type - default: map to "blanks"
        String aCommonType = ruleStore.getRule("common-type", _type);
        if (aCommonType != null) {
            aCommonType = aCommonType.replace("%type%", _type);
            aCommonType = RulesEngine.processRule(aCommonType);
        } else {
        	//super.appendStatusLine("WARNING: No common type rules were found... performing default mapping ...");
            aCommonType = "";
        }
    	
    	ScannedAttribute attr = new ScannedAttribute();
    	attr.setName(aName);
    	attr.setDataType(aDataType);
    	attr.setCommonType(aCommonType);
    	attr.setKey(isKey);
    	return attr;
    }
    
	/*
	 * Define GETTER methods for instance properties. 
	 */
	
	/**
	 * Returns the AWS region.
	 * 
	 * @return String representing the AWS region
	 */
	public String getAWSRegion() {
		return awsRegion;
	}
	
	/**
	 * Returns the case mode configured for this data object reader.
	 * 
	 * @return reader's case mode.
	 */
	public CaseMode getCaseMode() {
		return caseMode;
	}
    
    /**
     * Returns the scope set for this data object reader.
     * 
	 * @return reader's scope.
	 */
	public DynamoDBMetaDataScope getScope() {
		return scope;
	}

	/**
	 * Returns the list of tables configured for metadata extraction. This method should be called only if the scope
	 * is set to TABLES. The returned result is otherwise meaningless.
	 * 
	 * @return the list schemas to extract metadata for.
	 */
	public List<String> getTables() {
		return tables;
	}
}
