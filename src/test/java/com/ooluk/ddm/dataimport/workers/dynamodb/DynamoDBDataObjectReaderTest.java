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
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;

/**
 * This class REQUIRES a Amazon DynamoDB connection.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public class DynamoDBDataObjectReaderTest {
	
	private RuleStore ruleStore;
	
	private static final String REGION = "dynamodb.us-west-2.amazonaws.com";

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
		exception.expectMessage(equalTo("Initialization error: parameter [scope] missing"));
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
	@Test
	public void read_For_Region_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "region");
		reader.init(params);
		ScannedDataObject dObj = null;
		while ((dObj = reader.read()) != null) {
			System.out.println(dObj.getName());
			System.out.println(dObj.getNamespace());
			for (ScannedAttribute attr : dObj.getAttributes()) {
				System.out.println(attr.getName());
				System.out.println(attr.getDataType());
				System.out.println(attr.getCommonType());
			}
		}
	}
	@Test
	public void read_For_Tables_Scope() {
		DynamoDBDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "tables");
		params.put("tables", "Feedback");
		reader.init(params);
		ScannedDataObject dObj = null;
		while ((dObj = reader.read()) != null) {
			System.out.println(dObj.getName());
			System.out.println(dObj.getNamespace());
			for (ScannedAttribute attr : dObj.getAttributes()) {
				System.out.println(attr.getName());
				System.out.println(attr.getDataType());
				System.out.println(attr.getCommonType());
			}
		}
	}
}
