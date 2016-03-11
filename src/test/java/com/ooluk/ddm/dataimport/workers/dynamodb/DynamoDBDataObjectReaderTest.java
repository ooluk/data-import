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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.ImportException;
import com.ooluk.ddm.dataimport.ScannedDataObjectComparator;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;

/**
 * This class REQUIRES a Amazon DynamoDB connection.
 * 
 * <pre>
 * This test requires the following DynamoDB tables to be setup
 * 
 * DataObject: Table_1
 * Hash Key: key_1 : N
 * Sort Key: key_2 : B
 * index_1 - [ {key_1, Number, KeyType: HASH}, {key_3, String, KeyType: RANGE}]
 * 
 * DataObject: Table_2
 * Hash Key: key_1 : N
 * index-1 - [{key_2, String, KeyType: HASH}, {key_4, String, KeyType: RANGE}]
 * </pre>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class DynamoDBDataObjectReaderTest {
	
	private RuleStore ruleStore;
	
	private static final String REGION = "dynamodb.us-west-2.amazonaws.com";
	
	private HashMap<String, ScannedDataObject> data;

	@Rule
    public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		ruleStore = new RuleStore();
		ruleStore.addRule("data-type", "S", "String");
		ruleStore.addRule("data-type", "N", "Number");
		ruleStore.addRule("data-type", "B", "Binary");
		ruleStore.addRule("common-type", "S", "String");
		ruleStore.addRule("common-type", "N", "Number");
		ruleStore.addRule("common-type", "B", "Binary");
	}

	@After
	public void tearDown() {
	}
	
	/**
	 * Sets up the common parameters across test cases for the reader
	 * 
	 * @return a param-value map for the common parameters
	 */
	private HashMap<String, Object> getCommonParams() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("namespacePrefix", "TEMP_SPACE");
		params.put("ruleGroup", "dynamo");
		params.put("ruleStore", ruleStore);
		params.put("region", REGION);
		return params;		
	}
	
	private DynamoDBDataObjectReader getReader() {
		DynamoDBDataObjectReader reader = new DynamoDBDataObjectReader();
		reader.setLogWriter(new OutputStreamWriter(System.out));
		return reader;
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1] 
	 * ----------------------------------
	 * Test of init() method of class DynamoDBDataObjectReader.
	 * 
	 * </pre>
	 */
    private void verifyCommonParameters(DynamoDBDataObjectReader reader) {
		// Verify the namespace prefix
		assertEquals("TEMP_SPACE", reader.getNamespacePrefix());
		// Verify the region
		assertEquals(REGION, reader.getAWSRegion());
		// Verify rule group
		assertEquals("dynamo", reader.getRuleGroup());
		// Verify rule store
		assertSame(ruleStore, reader.getRuleStore()); 
		// Verify case mode
		assertEquals(CaseMode.MIXED, reader.getCaseMode());   	
    }
	
    /**
	 * Test init() method
	 */  
	@Test
	public void init_For_Region_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "region");
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(DynamoDBMetaDataScope.REGION, reader.getScope());
	}
	
	@Test
	public void init_For_Tables_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "tables");
		params.put("tables", "t1, t2");
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(DynamoDBMetaDataScope.TABLES, reader.getScope());
	} 
	
	@Test
	public void init_For_Tables_Scope_With_Missing_Tables() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "tables");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [tables] missing"));
		reader.init(params);		
	} 
	
	@Test
	public void init_For_Missing_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		//params.put("scope", "region");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [scope] missing"));
		reader.init(params);		
	}
	
	@Test
	public void init_For_Invalid_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: invalid value for parameter [scope]"));
		reader.init(params);
	}
	
	@Test
	public void init_For_Missing_Namespace() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		// Remove namespacePrefix that was added as a common param 
		params.remove("namespacePrefix");
		params.put("scope", "region");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] missing"));
		reader.init(params);		
	}
	
	@Test
	public void init_For_Blank_Namespace() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "region");
		params.put("namespacePrefix", "  ");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] empty"));
		reader.init(params);	
	}
	
	@Test
	public void init_For_Missing_RuleStore() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		// Remove ruleStore that was added as a common param 
		params.remove("ruleStore");
		params.put("scope", "region");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [ruleStore] missing"));
		reader.init(params);
	}
	
    /**
	 * Test read() method
	 */
	
	/**
	 * Creates an in-memory mapping similar to the data in iif.xml
	 */
	private void buildDataMap() {
		data = new HashMap<String, ScannedDataObject>();
		// Table_1
		ScannedDataObject dObj1 = new ScannedDataObject();
		dObj1.setNamespace("TEMP_SPACE");
		dObj1.setName("Table_1");
		dObj1.getAttributes().add(getAttribute("key_1", "Number", true));
		dObj1.getAttributes().add(getAttribute("key_2", "Binary", true));
		dObj1.getAttributes().add(getAttribute("key_3", "String", false));
		data.put("Table_1", dObj1);
		ScannedDataObject dObj2 = new ScannedDataObject();
		dObj2.setNamespace("TEMP_SPACE");
		dObj2.setName("Table_2");
		dObj2.getAttributes().add(getAttribute("key_1", "Number", true));
		dObj2.getAttributes().add(getAttribute("key_2", "String", false));
		dObj2.getAttributes().add(getAttribute("key_4", "String", false));	
		data.put("Table_2", dObj2);	
	}
	
	private ScannedAttribute getAttribute(String name, String type, boolean key) {
		ScannedAttribute attr = new ScannedAttribute();
		attr.setName(name); 
		attr.setDataType(type); 
		attr.setCommonType(type); 
		attr.setKey(key);	
		return attr;
	}
	
	@Test
	public void read_For_Region_Scope() {
		buildDataMap();
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "region");
		reader.init(params);
		ScannedDataObject dObj = null;
		while ((dObj = reader.read()) != null) {
			ScannedDataObject exp = data.get(dObj.getName());
			ScannedDataObjectComparator.compare(exp, dObj);
		}
	}
	
	@Test
	public void read_For_Tables_Scope_For_Single_Table() {
		buildDataMap();
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "tables");
		params.put("tables", "Table_1");
		reader.init(params);
		ScannedDataObject dObj = null;
		while ((dObj = reader.read()) != null) {
			ScannedDataObject exp = data.get(dObj.getName());
			ScannedDataObjectComparator.compare(exp, dObj);
		}
	}
	
	@Test
	public void read_For_Tables_Scope_For_Multiple_Tables() {
		buildDataMap();
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "tables");
		params.put("tables", "Table_1, Table_2");
		reader.init(params);
		ScannedDataObject dObj = null;
		while ((dObj = reader.read()) != null) {
			ScannedDataObject exp = data.get(dObj.getName());
			ScannedDataObjectComparator.compare(exp, dObj);
		}
	}
}
