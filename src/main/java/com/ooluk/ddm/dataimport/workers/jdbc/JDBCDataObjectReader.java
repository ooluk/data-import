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
package com.ooluk.ddm.dataimport.workers.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.MessageKey;
import com.ooluk.ddm.dataimport.Messages;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;
import com.ooluk.ddm.dataimport.rule.RulesEngine;
import com.ooluk.ddm.dataimport.workers.AbstractRuleBasedDataObjectReader;
import com.ooluk.ddm.dataimport.workers.TypeMetaData;

/**
 * <p>
 * JDBCDataObjectReader is a JDBC based implementation of the DataObjectReader for SQL databases. JDBCDataObjectReader
 * extends the RuleBasedDataObjectReader thus leveraging the rules facility.
 * 
 * <p>
 * This reader can be configured for three scopes of scanning for DataObjects:
 * <ul>
 * <li>DATABASE: Entire Database
 * <li>SCHEMA: Specific Schema(s)
 * <li>TABLE: Specific Table
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
 * The JDBCDataObjectReader can also be used to obtain JDBC type information. This is only valid for TABLE scope. Enable
 * the reader for type information using {@link #enableForTypeMode}. To get type information use the usual {@link #read}
 * method and then call {@link #getTypeMetaData}. {@link #enableForTypeMode} should be called before {@link #init(Map)}.
 * 
 * <p>
 * While initializing this reader do not call {@link #init()} instead call {@link #init(Map)}.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public class JDBCDataObjectReader extends AbstractRuleBasedDataObjectReader {
	
	private final Logger log = LogManager.getLogger();
	        
    /*
     * Parameters passed on call to init(...) that need to be saved.
     */
    // The case mode is optional and defaults to MIXED case 
    private CaseMode caseMode = CaseMode.MIXED;
    
    // The MetaDataScope is mandatory
    private MetaDataScope scope;     
    
    // The list of schemas is mandatory for scope = SCHEMA
    private List<String> schemas;
    
    // The schema and table parameters are mandatory for scope = TABLE
    private String schema;
    private String table;
    
	// The objectTypes parameters specified as import#XXX=Yes|No are optional but their absence will result in no
	// metadata being extracted.
    private String[] objectTypes;
        
    /*
     * Member variables
     */    
    // Database connection.
    private Connection conn;
    
    // Reference to the database metadata
    private DatabaseMetaData dbmd;
    
    // The main result set is saved in an instance variable.
    private ResultSet rs;
    
	// For SCHEMA scope we need to process each schema individually. This index is used to track the current schema.
    private int schemaIdx = 0;
    
    // A flag used to signal the completion of initial configuration
    private boolean configured = false;
    
    // Flags to indicate the database for database specific processing
    private boolean isOracle = false;
    private boolean isSQLServer = false;
    
    TypeMetaData typeMetaData;
    
    /**
     * Constructs a JDBCDataObjectReader.
     */
    public JDBCDataObjectReader() {    	
    }
    
    @Override
    public void enableForTypeMode() {
    	super.enableForTypeMode();
    	typeMetaData = new TypeMetaData();
    }
    
    @Override
    public void init(Map<String, Object> params) {     
    	
    	createDatabaseConnection(params);
    	extractScope(params);
    	extractNamespacePrefix(params);
    	extractRulesParameters(params);
    	extractCaseMode(params);
    	extractObjectTypes(params);    	
    	configured = true;
		init();
    }

    /**
     * This method should not be called externally for JDBCDataObjectReader. Call init(params) only.
     */
    @Override
    public void init() {        
    	    
        if (!configured) {
        	String msg = Messages.getMessage(MessageKey.READ_NOT_CONFIG, "JDBCDataObjectReader");
        	super.appendStatusLine(msg);
        	throwImportException(msg);
        }
        
        try {
            dbmd = conn.getMetaData();
            flagDatabases();
            openResultSet();
        } catch(SQLException ex) {
        	String msg = ex.getMessage();
            log.error(msg, ex);
        	throwImportException(msg, ex);
        }
    }
    
    /**
	 * This method creates a connection using the connection properties specified.
	 * 
	 * @param params
	 *            initialization parameters
	 */
    private void createDatabaseConnection(Map<String, Object> params) {

    	Properties props = (Properties) params.get("databaseConnection");
    	if (props == null) {
    		String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "databaseConnection");
        	super.appendStatusLine(msg);
        	throwImportException(msg);
    	}

		// TODO Internationalize messages
    	
    	// Extract and load the JDBC driver
    	String driver = props.getProperty("driver");
    	if (driver == null) {
    		String msg ="JDBC driver class not specified";
        	super.appendStatusLine(msg);
        	throwImportException(msg);   		
    	}
    	
    	try {
	        @SuppressWarnings({ "unchecked", "unused" })
            Class<? extends Driver> cls = (Class<? extends Driver>) Class.forName(driver);
        } catch (ClassNotFoundException e) {
    		String msg ="Unable to load JDBC driver class " + driver;
        	super.appendStatusLine(msg);
    		throwImportException(e);	        
        } catch (ClassCastException e) {
    		String msg ="Specified driver is not a valid JDBC driver";
        	super.appendStatusLine(msg);
        	throwImportException(msg);      	
        }    	
    	// Remove the driver property. It is no longer required.
    	props.remove(driver);
    	
    	// Extract the JDBC URL
    	String url = props.getProperty("url");
    	if (url == null) {
    		String msg ="JDBC URL missing";
        	super.appendStatusLine(msg);
        	throwImportException(msg);   		
    	}
    	// Remove the URL property. It is no longer required.
    	props.remove(url);
    	
    	try {
	        conn = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
    		String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "databaseConnection");
        	super.appendStatusLine(msg);
        	super.appendStatusLine(e.getMessage());
        	log.error(e.getMessage(), e);
        	throwImportException(msg);
        }    	
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
    		scope = MetaDataScope.valueOf(readScope.toUpperCase());
    	} catch(IllegalArgumentException ex) {
        	String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "scope");
        	super.appendStatusLine(msg);
        	throwImportException(msg);
    	}
    	
       	// Type Mode is only valid for TABLE scope
    	if (super.isEnabledForTypeMode() && scope != MetaDataScope.TABLE) {
    		throwImportException(Messages.getMessage(MessageKey.JDBC_TYPE_MODE_INVALID));  			
    	}
    	
    	/*
    	 * Read scope specific parameters
    	 * For SCHEMA scope: list of schemas as a comma separated list
    	 * For TABLE scope: schema name and table name
    	 */
    	switch(scope) {
	    	case DATABASE:
	    		break;
	    	case SCHEMA: 
	    		configureForSchemaScope(params);
	    		break;
	    	case TABLE:
	    		configureForTableScope(params);
	    		break;
    	}
    }
    
	/**
	 * Configures the reader for SCHEMA scope.
	 * 
	 * @param params
	 *            initialization parameters as a parameter name-value map
	 */
    private void configureForSchemaScope(Map<String, Object> params) {
		// Schemas are provided as a comma separated list
		String csList = (String) params.get("schemas");
		if (csList == null || csList.trim().isEmpty()) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "schemas");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		// This creates an immutable list but we are okay with that.
		schemas = Arrays.asList(csList.split("(\\s)*,(\\s)*"));
	}

	/**
	 * Configures the reader for TABLE scope.
	 * 
	 * @param params
	 *            initialization parameters as a parameter name-value map
	 */
	private void configureForTableScope(Map<String, Object> params) {
		// Schema the table belongs to
		schema = (String) params.get("schema");
		if (schema == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "schema");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		// Table name
		table = (String) params.get("table");
		if (table == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "table");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
	}
     
    /**
	 * Extracts the namespace prefix from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
    private void extractNamespacePrefix(Map<String, Object> params) {
    	
        namespacePrefix = ((String)params.get("namespacePrefix"));
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
        String ruleGroup = ((String)params.get("ruleGroup"));
        if (ruleGroup != null) {
        	super.setRuleGroup(ruleGroup);        	
        }        
        
        ruleStore = ((RuleStore)params.get("ruleStore"));
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
	    	} catch(IllegalArgumentException ex) {
	        	String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "case");
	        	super.appendStatusLine(msg);
	        	throwImportException(msg);
	    	}
    	} 
    }
    
    /**
	 * Extracts configured object types from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
    private void extractObjectTypes(Map<String, Object> params) {

    	List<String> types = new ArrayList<>();
    	for (String param : params.keySet()) {
    		if (param.startsWith("import#")) {
    			String value = params.get(param).toString();
    			if (value.equalsIgnoreCase("Yes")) {
    				types.add(param);
    			}
    		}
    	}
    	fetchObjectTypes(types);
    }

	/**
	 * Creates array of database object types. The types are specified as a list of "import#XXX". This method extracts
	 * the XXXs into an array.
	 * 
	 * @param types
	 *            list of import# parameters
	 */
	private void fetchObjectTypes(List<String> types) {

		objectTypes = new String[types.size()];
		int i = 0;
		for (String type : types) {
			// Omit the "import#" - 7 characters so begin with index 7.
			objectTypes[i++] = type.substring(7);
		}
	}
	
	/**
	 * Flags the database type for database specific processing.
	 * 
	 * @throws SQLException
	 */
	private void flagDatabases() throws SQLException {

        String databaseProduct = dbmd.getDatabaseProductName();
        
		/*
		 * ORACLE
		 * 		default values are specially handled for Oracle.
		 * Microsoft SQL Server
		 * 		description is specially handled for SQLServer.
		 * 
		 * TODO: impact of using Microsoft on ACCESS. Don't use vendor name if possible. 
		 */
    	if (databaseProduct.toLowerCase().contains("oracle")) {
    		isOracle = true;
		} else if (databaseProduct.toLowerCase().contains("microsoft")) {
    		isSQLServer = true;
		}
	}
	
	/**
	 * Opens the database metadata result set.
	 * 
	 * @throws SQLException
	 */
	private void openResultSet() throws SQLException {
		switch (scope) {
			case DATABASE:
				rs = dbmd.getTables(null, null, null, objectTypes);
				break;
			case SCHEMA:
				rs = dbmd.getTables(null, schemas.get(schemaIdx++), null, objectTypes);
				break;
			case TABLE:
				rs = dbmd.getTables(null, schema, table, objectTypes);
				break;
		}
	}
	
	/**
	 * Determines if there are any more unprocessed tables in the metadata result set.
	 * 
	 * @return true if there are more unprocessed tables false otherwise.
	 * 
	 * @throws SQLException
	 */
	private boolean hasMore() throws SQLException {
		return rs.next();
	}
  
    @Override
    public ScannedDataObject read() {
        
        ScannedDataObject dObj = null;
        try {
            if (hasMore()) {                
                dObj = createDataObject();
            } else {
            	// For SCHEMA scope if current schema has no more data process next schema
                if (scope == MetaDataScope.SCHEMA && schemaIdx < schemas.size()) {
                    openResultSet();
                    dObj = hasMore() ? createDataObject() : null;
                } else {
                	dObj = null;
                }
            }
        } catch (SQLException ex) {
        	String msg = Messages.getMessage(MessageKey.JDBC_READ_EXCP, ex.getMessage()); 
        	super.appendStatusLine(msg);
        	throwImportException("Exception reading data object", ex);
        }   
    	return dObj;
    }
    
    @Override
    public void close() {        

        try {
        	if (rs != null) {
        		rs.close();
        	}
        	if (conn != null) {
        		conn.close();
        	}
        } catch (SQLException ex) {
        	log.error(ex.getMessage(), ex);
	        // Don't throw exception if error occurs on close(). Do log it though.
        }
    }

	/**
	 * Creates a ScannedDataObject from the current table.
	 * 
	 * Refer to: http://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html
	 * 
	 * @return a ScannedDataObject instance for the the current table.
	 * 
	 * @throws SQLException
	 */
    private ScannedDataObject createDataObject() throws SQLException {
    	
    	// Namespace
        String schema = rs.getString("TABLE_SCHEM");
        String nspaceRule = ruleStore.getRule("namespace", "name");
        String oNamespace = getNamespace(namespacePrefix, schema, nspaceRule);
        
        // Table name
        String oName = rs.getString("TABLE_NAME");
        String msg = Messages.getMessage(MessageKey.READ_IMPORTING, oName);
        super.appendStatusLine("-------------------------------------------");
        super.appendStatusLine(msg);
        super.appendStatusLine("-------------------------------------------");
        log.trace(msg);
        
        // Primary Keys
        ResultSet keys = dbmd.getPrimaryKeys(null, schema, oName);
        List<String> pkKeys = new ArrayList<>();
        while (keys.next()) {
        	pkKeys.add(keys.getString("COLUMN_NAME"));
        }
        keys.close();
        
        // Foreign Keys
        /*
		 * We store foreign keys (FK) and their associated primary keys (PK) in a map with the FK as the key. The PK is
		 * saved as {namespace}.{table}.{column}. The PK namespace is constructed using the current namespace rule. It's
		 * possible that the PK data was imported using a different namespace rule. In such a case the FK relationships
		 * will not be populated or may be incorrectly mapped.
		 */
        ResultSet foreignKeys = dbmd.getImportedKeys(null, schema, oName);
        HashMap<String, String> fkMap = new HashMap<>();
        while (foreignKeys.next()) {
        	String pkSchema = foreignKeys.getString("PKTABLE_SCHEM");
        	String pkNamespace = getNamespace(namespacePrefix, pkSchema, nspaceRule);
            fkMap.put(foreignKeys.getString("FKCOLUMN_NAME"),
            		pkNamespace + "."
                    + foreignKeys.getString("PKTABLE_NAME") + "."
                    + foreignKeys.getString("PKCOLUMN_NAME"));
        }
        foreignKeys.close();
        
        // Comments / Remarks / Description
        String fullTableName = schema + "." + oName;
        String oComment = rs.getString("REMARKS");
        if (isSQLServer) {
        	oComment = getSQLServerTableDescription(fullTableName);
        }
        if (oComment == null)
        	oComment = "";
        
        // Columns
        ResultSet columns = dbmd.getColumns(null, schema, oName, null);
        List<ScannedAttribute> attributes = createAttributes(columns, pkKeys, fkMap, fullTableName);
        columns.close();
        
        // Create ScannedDataObject
        ScannedDataObject dObj = new ScannedDataObject(caseMode);
        dObj.setNamespace(oNamespace);
        dObj.setName(oName);
        dObj.setSummary(oComment);
        dObj.setDescription(oComment);
        dObj.setAttributes(attributes);
        return dObj;
	}

	/**
	 * Returns a namespace name based on the passed parameters.
	 * 
	 * @param namespacePrefix
	 *            namespace prefix
	 * @param schema
	 *            schema of the data object being read
	 * @param nspaceRule
	 *            namespace rule
	 *            
	 * @return namespace name.
	 */
    private String getNamespace(String namespacePrefix, String schema, String nspaceRule) {
    	String namespace = "";
        if (nspaceRule != null) {
        	namespace = nspaceRule.replace("%prefix%", namespacePrefix);
        	namespace = nspaceRule.replace("%schema%", schema == null ? "<no_schema>" : schema);
        } else {
        	namespace = namespacePrefix + (schema == null ? "" : "." + schema);
        } 
        return namespace;
	}

	/**
	 * Creates the attributes for the current ScannedDataObject.
	 * 
	 * @param columns
	 *            the columns of the table
	 * @param pkKeys
	 *            primary key columns
	 * @param fkMap
	 *            the foreign key map
	 * @param fullTableName
	 *            full table name
	 * 
	 * @throws SQLException
	 */
	private List<ScannedAttribute> createAttributes(
			ResultSet columns, 
			List<String> pkKeys, 
			Map<String, String> fkMap, 
			String fullTableName) throws SQLException {
        
        List<ScannedAttribute> attributes = new ArrayList<>();
        
        while (columns.next()) {
        	
            // Default value
			/*
			 * We read default value first because Oracle returns LONG type for COLUMN_DEF and if we do not read LONG
			 * types first we receive "java.sql.SQLException: Stream has already been closed".
			 */
            String defaultValue = "";
            if (isOracle) {            	
            	// System.out.println("DEBUG: " + columns.getMetaData().getColumnTypeName(13));
            	byte[] bytes = columns.getBytes("COLUMN_DEF");
            	if (bytes != null) {
			        defaultValue = new String(bytes);
            	}
            } else {
                defaultValue = columns.getString("COLUMN_DEF");
                if (defaultValue == null)
                	defaultValue = "";
            }
                        
            // Attribute name
            String aName = columns.getString("COLUMN_NAME");
            
            // Attribute sequence
            String aSeqNo = columns.getString("ORDINAL_POSITION");
            
            String _type = columns.getString("TYPE_NAME").toUpperCase();
            String _size = columns.getString("COLUMN_SIZE");
            String _decimal = columns.getString("DECIMAL_DIGITS");
            
            if (_type == null) { _type = ""; }
            if (_size == null) { _size = "0"; }
            if (_decimal == null) { _decimal = "0"; }
            
            if (super.isEnabledForTypeMode()) {
            	addTypeData(aName, _type, _size, _decimal);
            	// We do not require the remaining logic in the while loop for metadata type information
            	continue;
            }

            // Data type - default: map to _type
            String aDataType = ruleStore.getRule("data-type", _type);
            if (aDataType != null) {
                aDataType = aDataType.replace("%type%", _type);
                aDataType = aDataType.replace("%size%", _size);
                aDataType = aDataType.replace("%scale%", _decimal);
                aDataType = RulesEngine.processRule(aDataType);
            } else {
            	//super.appendStatusLine("WARNING: No data type rules were found... performing default mapping ...");
                aDataType = _type;
            }

            // Common type - default: map to "blanks"
            String aCommonType = ruleStore.getRule("common-type", _type);
            if (aCommonType != null) {
                aCommonType = aCommonType.replace("%type%", _type);
                aCommonType = aCommonType.replace("%size%", _size);
                aCommonType = aCommonType.replace("%scale%", _decimal);
                aCommonType = RulesEngine.processRule(aCommonType);
            } else {
            	//super.appendStatusLine("WARNING: No common type rules were found... performing default mapping ...");
                aCommonType = "";
            }
            
            // Primary Key
            /*
			 * There is no indicator on column metadata to indicate if it is a primary key. We therefore track the list
			 * of primary keys extracted from the table metadata and check if this column is in that list.
			 */
            boolean aKey = pkKeys.contains(aName);
            
            // Parent attribute
            /*
			 * Foreign key metadata is maintained at the table level. We therefore extract it there and pass it here to
			 * perform lookup on the current column.
			 */
            String pkCol = "";
            if (fkMap.containsKey(aName)) {
            	pkCol = fkMap.get(aName);
            }
            
            // Is an attribute value mandatory / required?
            // If "IS_NULLABLE" = NO, REQUIRED = true else REQUIRED = false
            boolean aRequired = columns.getString("IS_NULLABLE").startsWith("N");
            
            // Comments
            String comment = columns.getString("REMARKS");
            if (isSQLServer) {
            	comment = getSQLServerColumnDescription(fullTableName, aName);
            }
            if (comment == null)
            	comment = "";
            
            ScannedAttribute attr = new ScannedAttribute(caseMode);
            attr.setName(aName);
            attr.setSeqNo(Integer.parseInt(aSeqNo));
            attr.setDataType(aDataType);
            attr.setCommonType(aCommonType);
            attr.setKey(aKey);
            attr.setParentAttribute(pkCol);
            attr.setRequired(aRequired);
            attr.setDefaultValue(defaultValue);
            attr.setSummary(comment);
            attr.setDescription(comment);            
            attributes.add(attr);
        }
        
        return attributes;
	}

	/**
	 * Adds JDBC type information.
	 * 
	 * @param col
	 *            column name
	 * @param type
	 *            JDBC metadata "TYPE_NAME"
	 * @param size
	 *            JDBC metadata "COLUMN_SIZE"
	 * @param scale
	 *            JDBC metadata "DECIMAL_DIGITS"
	 */
    private void addTypeData(String col, String type, String size, String scale) {
    	Map<String, String> typeMap = new HashMap<>();
    	typeMap.put("attribute", col);
    	typeMap.put("type", type);
    	typeMap.put("size", size);
    	typeMap.put("scale", scale);
    	typeMetaData.add(typeMap);
	}

	/**
	 * Fetches the description from extended properties for SQL Server tables. DatabaseMetaData doesn't return remarks
	 * for SQL Server.
	 * 
	 * @param table
	 *            full table name
	 * 
	 * @return the description for the table.
	 * 
	 * @throws SQLException
	 */
    private String getSQLServerTableDescription(String table) throws SQLException {    	
    	String remarks = "";
    	String query = 
    			  "SELECT  	cast(td.value as varchar) AS [table_desc]"
    			+ "  FROM  	sysobjects t "
    			+ "        	INNER JOIN "
    			+ "        	sysusers u "
    			+ "    ON  	u.uid = t.uid "
    			+ "        	LEFT OUTER JOIN "
    			+ "			sys.extended_properties td "
    			+ "	   ON 	td.major_id = object_id(u.name + '.' + t.name) "
    			+ "	  AND 	td.minor_id = 0 "
    			+ "   AND   td.name = 'MS_Description' "
    			+ "	WHERE 	u.name + '.' + t.name = ?";
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		stmt.setString(1, table);
        	try (ResultSet rs = stmt.executeQuery()) {
	        	if (rs.next()) {
	        		remarks = rs.getString("table_desc");
	        	}
        	}
    	} 	
    	return remarks;    	
	}

	/**
	 * Fetches the description from extended properties for SQL Server columns. DatabaseMetaData doesn't return remarks
	 * for SQL Server.
	 * 
	 * @param table
	 *            full table name
	 * 
	 * @param column
	 *            column name
	 * 
	 * @return the description for the table.
	 * 
	 * @throws SQLException
	 */
    private String getSQLServerColumnDescription(String table, String column) throws SQLException {    	
    	String remarks = "";
    	String query = 
    			  "SELECT  	cast(cd.value as varchar) AS [column_desc]"
    			+ "  FROM  	sysobjects t "
    			+ "        	INNER JOIN "
    			+ "        	sysusers u "
    			+ "    ON  	u.uid = t.uid "
    			+ "         INNER JOIN "
    			+ "         syscolumns c "
    			+ "    ON	c.id = t.id " 
    			+ "        	LEFT OUTER JOIN "
    			+ "			sys.extended_properties cd "
    			+ "	   ON 	cd.major_id = object_id(u.name + '.' + t.name) "
    			+ "	  AND 	cd.minor_id = c.colid "
    			+ "   AND   cd.name = 'MS_Description' "
    			+ "	WHERE 	u.name + '.' + t.name = ?"
    			+ "	  AND 	c.name = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, table);
			stmt.setString(2, column);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					remarks = rs.getString("column_desc");
				}
			}
		}	    	
    	return remarks;    	
    }
	    
	@Override
	public TypeMetaData getTypeMetaData() {
		typeMetaData.addProperty("attribute");
		typeMetaData.addProperty("type");
		typeMetaData.addProperty("size");
		typeMetaData.addProperty("scale");
		return typeMetaData;
	}
    
	/*
	 * Define GETTER methods for instance properties. 
	 */
	
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
	public MetaDataScope getScope() {
		return scope;
	}

	/**
	 * Returns the list of schemas configured for metadata extraction. This method should be called only if the scope
	 * is set to SCHEMA. The returned result is otherwise meaningless.
	 * 
	 * @return the list schemas to extract metadata for.
	 */
	public List<String> getSchemas() {
		return schemas;
	}

	/**
	 * Returns the schema of the table configured for metadata extraction. This method should be called only if the scope
	 * is set to TABLE. The returned result is otherwise meaningless.
	 * 
	 * @return the schema of the table configured for metadata extraction.
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * Returns the table configured for metadata extraction.  This method should be called only if the scope
	 * is set to TABLE. The returned result is otherwise meaningless.
	 * 
	 * @return the table configured for metadata extraction.
	 */
	public String getTable() {
		return table;
	}
}
