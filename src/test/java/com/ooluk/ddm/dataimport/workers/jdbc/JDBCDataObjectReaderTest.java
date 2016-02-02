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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import com.ooluk.ddm.dataimport.workers.TypeMetaData;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public class JDBCDataObjectReaderTest {
	
	private static final int DDM_SCHEMA_OBJ_COUNT = 32;
	private static final int DDM_SCHEMA_ATTR_COUNT = 157;
	private static final int TEST_SCHEMA_OBJ_COUNT = 1;
	private static final int TEST_SCHEMA_ATTR_COUNT = 19;
	private static final int DB_OBJ_COUNT = DDM_SCHEMA_OBJ_COUNT + TEST_SCHEMA_OBJ_COUNT;
	private static final int DB_ATTR_COUNT = DDM_SCHEMA_ATTR_COUNT + TEST_SCHEMA_ATTR_COUNT;
	

	private RuleStore ruleStore;
	private Properties ds;
	
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
		ds = new Properties();
		ds.setProperty("driver", "org.postgresql.Driver");
		ds.setProperty("url", "jdbc:postgresql://localhost/ddm_intg_db");
		ds.setProperty("user", "ddmapp");
		ds.setProperty("password", "password");
		ruleStore = new RuleStore();
		ruleStore.addRule("data-type", "NUMERIC", "%type%(%size%,%scale%)");
		ruleStore.addRule("data-type", "VARCHAR", "%type%(%size%)");
		ruleStore.addRule("data-type", "BPCHAR", "CHAR(%size%)");
		ruleStore.addRule("common-type", "NUMERIC", "%type%([!%size%-%scale%!],[!%scale%+0!])");
		ruleStore.addRule("common-type", "VARCHAR", "CHAR(%size%)");
		ruleStore.addRule("common-type", "BPCHAR", "CHAR(%size%)");
		ruleStore.addRule("namespace", "nspace", "%prefix%.%schema%");
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
		params.put("databaseConnection", ds);
		params.put("namespacePrefix", "TEMP_SPACE");
		params.put("ruleGroup", "sql");
		params.put("ruleStore", ruleStore);
		params.put("import#TABLE", "Yes");
		return params;		
	}
	
	private JDBCDataObjectReader getReader() {
		JDBCDataObjectReader reader = new JDBCDataObjectReader();
		reader.setLogWriter(new OutputStreamWriter(System.out));
		return reader;
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1] 
	 * ----------------------------------
	 * Test of init() method of class JDBCDataObjectReader.
	 * 
	 * </pre>
	 */
    private void verifyCommonParameters(JDBCDataObjectReader reader) {
		// Verify the namespace prefix
		assertEquals("TEMP_SPACE", reader.getNamespacePrefix());
		// Verify rule group
		assertEquals("sql", reader.getRuleGroup());
		// Verify rule store
		assertSame(ruleStore, reader.getRuleStore()); 
		// Verify case mode
		assertEquals(CaseMode.MIXED, reader.getCaseMode());   	
    }
	
    /**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.1] 
	 * ----------------------------------
	 * Test init() for success for full database read
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Database_Scope() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "database");
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(MetaDataScope.DATABASE, reader.getScope());
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.2] 
	 * ----------------------------------
	 * Test init() for missing database
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [databaseConnection] missing"]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Database_For_Missing_DatabaseConnectionProperties() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [databaseConnection] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.3] 
	 * ----------------------------------
	 * Test init() for missing scope
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [scope] missing"]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Database_For_Missing_Scope() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("databaseConnection", ds);
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [scope] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.4] 
	 * ----------------------------------
	 * Test init() for invalid scope
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: invalid value for parameter [scope]"]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Database_For_Invalid_Scope() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("databaseConnection", ds);
		params.put("scope", "db");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: invalid value for parameter [scope]"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.5] 
	 * ----------------------------------
	 * Test init() for missing namespace prefix
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [namespacePrefix] missing"]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Database_For_Missing_Namespace() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("databaseConnection", ds);
		params.put("scope", "database");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.6] 
	 * ----------------------------------
	 * Test init() for empty namespace prefix
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [namespacePrefix] empty"]
	 * 
	 * </pre>
	 */ 
	@Test
	public void init_For_Database_For_Empty_Namespace() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("databaseConnection", ds);
		params.put("scope", "database");
		params.put("namespacePrefix", "  ");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] empty"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.7] 
	 * ----------------------------------
	 * Test init() for missing rule store
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [ruleStore] missing""]
	 * 
	 * </pre>
	 */ 
	@Test
	public void init_For_Database_For_Missing_RuleStore() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("databaseConnection", ds);
		params.put("scope", "database");
		params.put("namespacePrefix", "TEMP_SPACE");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [ruleStore] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.2.1] 
	 * ----------------------------------
	 * Test init() for success for schema read
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Schema() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "schema");
		params.put("schemas", "schema1, schema2");
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(MetaDataScope.SCHEMA, reader.getScope());
		// Verify the schemas
		List<String> schemas = new ArrayList<>();
		schemas.add("schema1"); schemas.add("schema2");
		assertEquals(schemas, reader.getSchemas());
	}
	
	/*
	 * We do not test for missing common parameters here as they have been tested for DATABASE scope
	 */
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.2.2] 
	 * ----------------------------------
	 * Test init() for missing schemas
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [schemas] missing""]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Schema_For_Missing_Schemas() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "schema");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [schemas] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.2.3] 
	 * ----------------------------------
	 * Test init() for empty schemas
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [schemas] missing""]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Schema_For_Empty_Schemas() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "schema");
		params.put("schemas", " ");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [schemas] missing"));
		reader.init(params);
	}
	
	/** <pre>
	 * ----------------------------------
	 * Case: [1.3.1] 
	 * ----------------------------------
	 * Test init() for table read
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Table() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		params.put("schema", "schema1");
		params.put("table", "table1");
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(MetaDataScope.TABLE, reader.getScope());
		// Verify the schema
		assertEquals("schema1", reader.getSchema());
		// Verify the table
		assertEquals("table1", reader.getTable());
	}
	
	/*
	 * We do not test for missing common parameters here as they have been tested for DATABASE scope
	 */	
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.3.2] 
	 * ----------------------------------
	 * Test init() for missing schema
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [schema] missing""]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Table_For_Missing_Schema() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [schema] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.3.3] 
	 * ----------------------------------
	 * Test init() for missing table
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [table] missing""]
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Table_For_Missing_Table() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		params.put("schema", "");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [table] missing"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2] 
	 * ----------------------------------
	 * Test of read method of class JDBCDataObjectReader.
	 * 
	 * </pre>
	 */
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.1] 
	 * ----------------------------------
	 * Read for database scope
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * DataObjects = DB_OBJ_COUNT
	 * Attributes = DB_ATTR_COUNT
	 * 
	 * Note: We just test the total data object count and attribute count here.
	 * We test the details in the "TABLE" scope. 
	 * 
	 * </pre>
	 */ 
	@Test
	public void read_For_Database() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "database");
		reader.init(params);
		int dataObjectCount = 0;
		int attributeCount = 0;		
		ScannedDataObject dObj = reader.read();
		while(dObj != null) {
			dataObjectCount++;
			attributeCount+= dObj.getAttributes().size();
			dObj = reader.read();
		}
		// Verify the counts
		assertEquals(DB_OBJ_COUNT, dataObjectCount);
		assertEquals(DB_ATTR_COUNT, attributeCount);	
		reader.close();
	}
		
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.2] 
	 * ----------------------------------
	 * Read for schema scope for single schema 
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * Schema = "ddm"
	 * DataObjects = DDM_SCHEMA_OBJ_COUNT
	 * Attributes = DDM_SCHEMA_ATTR_COUNT
	 * 
	 * Note: We just test the total data object count and attribute count here.
	 * We test the details in the "TABLE" scope. 
	 * 
	 * </pre>
	 */ 
	@Test
	public void read_For_Single_Schema() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "schema");
		params.put("schemas", "ddm");
		reader.init(params);
		int dataObjectCount = 0;
		int attributeCount = 0;		
		ScannedDataObject dObj = reader.read();
		while(dObj != null) {
			dataObjectCount++;
			attributeCount+= dObj.getAttributes().size();
			dObj = reader.read();
		}
		// Verify the counts
		assertEquals(DDM_SCHEMA_OBJ_COUNT, dataObjectCount);
		assertEquals(DDM_SCHEMA_ATTR_COUNT, attributeCount);	
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.3] 
	 * ----------------------------------
	 * Read for schema scope for multiple schemas 
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * Schema = "ddm, test"
	 * DataObjects = DB_OBJ_COUNT
	 * Attributes = DB_ATTR_COUNT
	 * 
	 * Note: We just test the total data object count and attribute count here.
	 * We test the details in the "TABLE" scope. 
	 * 
	 * </pre>
	 */ 
	@Test
	public void read_For_Multiple_Schema() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "schema");
		params.put("schemas", "ddm, test");
		reader.init(params);
		int dataObjectCount = 0;
		int attributeCount = 0;		
		ScannedDataObject dObj = reader.read();
		while(dObj != null) {
			dataObjectCount++;
			attributeCount+= dObj.getAttributes().size();
			dObj = reader.read();
		}
		// Verify the counts
		assertEquals(DB_OBJ_COUNT, dataObjectCount);
		assertEquals(DB_ATTR_COUNT, attributeCount);	
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.4] 
	 * ----------------------------------
	 * Read for table scope  
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * Schema = "test", table = "data_type_test1" 
	 * 
	 * </pre>
	 */ 
	private void verifyAttributes(List<ScannedAttribute> attrs, String[][] exp) {
		int row = 0;
		for(ScannedAttribute attr : attrs) {
			String[] expvals = exp[row++];
			assertEquals(expvals[0], attr.getName());
			assertEquals(expvals[1], attr.getSeqNo().toString());
			assertEquals(expvals[2], attr.getDataType());
			assertEquals(expvals[3], attr.getCommonType());
			assertEquals(expvals[4], String.valueOf(attr.isKey()));
			assertEquals(expvals[5], attr.getParentAttribute());
			assertEquals(expvals[6], String.valueOf(attr.isRequired()));
			assertEquals(expvals[7], attr.getDefaultValue());
			assertEquals(expvals[8], attr.getSummary());
			assertEquals(expvals[9], attr.getDescription());
		}
	}
	
	@SuppressWarnings("unused")
    private void printAttributes(List<ScannedAttribute> attrs) {
		for(ScannedAttribute attr : attrs) {
			System.out.println("{\"" + attr.getName() + "\"," +
							"\"" + attr.getSeqNo().toString() + "\"," +
							"\"" + attr.getDataType() + "\"," +
							"\"" + attr.getCommonType() + "\"," +
							"\"" + String.valueOf(attr.isKey()) + "\"," +
							"\"" + attr.getParentAttribute() + "\"," +
							"\"" + String.valueOf(attr.isRequired()) + "\"," +
							"\"" + attr.getDefaultValue() + "\"," +
							"\"" + attr.getSummary() + "\"," +
							"\"" + attr.getDescription() + "\"" +
							"},");
		}
	}
	
	@Test
	public void read_For_Table() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		params.put("schema", "test");
		params.put("table", "data_type_test");
		reader.init(params);
		ScannedDataObject dObj = reader.read();
		//while(dObj != null) {
			// verify namespace
			assertEquals("TEMP_SPACE.test", dObj.getNamespace());
			// verify name
			assertEquals("data_type_test", dObj.getName());
			// verify short description
			assertEquals("This is a table for testing the import API", dObj.getSummary());
			// verify long description
			assertEquals("This is a table for testing the import API", dObj.getDescription());
			String[][] attributeProps = {
					{"col_1","1","BIGSERIAL","","true","","true","nextval('test.data_type_test_col_1_seq'::regclass)","Column 1","Column 1"},
					{"col_2","2","INT8","","false","","false","","Column 2","Column 2"},
					{"col_3","3","BOOL","","false","","false","","Column 3","Column 3"},
					{"col_4","4","CHAR(10)","CHAR(10)","false","","true","'ABC'::bpchar","Column 4","Column 4"},
					{"col_5","5","VARCHAR(10)","CHAR(10)","false","","false","","Column 5","Column 5"},
					{"col_6","6","DATE","","false","","false","","Column 6","Column 6"},
					{"col_7","7","FLOAT8","","false","","false","","Column 7","Column 7"},
					{"col_8","8","INT4","","false","","false","","Column 8","Column 8"},
					{"col_9","9","MONEY","","false","","false","","Column 9","Column 9"},
					{"col_10","10","NUMERIC(10,6)","NUMERIC(4,6)","false","","false","","Column 10","Column 10"},
					{"col_11","11","INT2","","false","","false","","Column 11","Column 11"},
					{"col_12","12","INT2","","false","","true","nextval('test.data_type_test_col_12_seq'::regclass)","Column 12","Column 12"},
					{"col_13","13","SERIAL","","false","","true","nextval('test.data_type_test_col_13_seq'::regclass)","Column 13","Column 13"},
					{"col_14","14","TEXT","","false","","false","","Column 14","Column 14"},
					{"col_15","15","TIME","","false","","false","","Column 15","Column 15"},
					{"col_16","16","TIMETZ","","false","","false","","Column 16","Column 16"},
					{"col_17","17","TIMESTAMP","","false","","false","","Column 17","Column 17"},
					{"col_18","18","TIMESTAMPTZ","","false","","false","","Column 18","Column 18"},
					{"col_19","19","NUMERIC(10,6)","NUMERIC(4,6)","false","","false","","Column 19","Column 19"}					
			};
			//printAttributes(dObj.getAttributes());
			verifyAttributes(dObj.getAttributes(), attributeProps); 
			//dObj = reader.read();
		//}	
	}

	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [3] 
	 * ----------------------------------
	 * Test getTypeMetaData method
	 * 
	 * </pre>
	 */
	
	private Map<String, Map<String, String>> getPostgreSQLTypeMetaData() {
		String[][] md = {
			{"col_1","BIGSERIAL","19","0"},
			{"col_2","INT8","19","0"},
			{"col_3","BOOL","1","0"},
			{"col_4","BPCHAR","10","0"},
			{"col_5","VARCHAR","10","0"},
			{"col_6","DATE","13","0"},
			{"col_7","FLOAT8","17","17"},
			{"col_8","INT4","10","0"},
			{"col_9","MONEY","2147483647","0"},
			{"col_10","NUMERIC","10","6"},
			{"col_11","INT2","5","0"},
			{"col_12","INT2","5","0"},
			{"col_13","SERIAL","10","0"},
			{"col_14","TEXT","2147483647","0"},
			{"col_15","TIME","11","2"},
			{"col_16","TIMETZ","17","2"},
			{"col_17","TIMESTAMP","29","6"},
			{"col_18","TIMESTAMPTZ","35","6"},
			{"col_19","NUMERIC","10","6"}
		};
		Map<String, Map<String, String>> mapping = new HashMap<>();
		for (String[] col : md) {
			String column = col[0];
			String type = col[1];
			String size = col[2];
			String scale = col[3];
			Map<String, String> metadata = new HashMap<>();
			metadata.put("attribute", column);
			metadata.put("type", type);
			metadata.put("size", size);
			metadata.put("scale", scale);
			mapping.put(column, metadata);
		}
		return mapping;
	}
		
	private void verifyTypeMetaData(Map<String, Map<String, String>> expected, TypeMetaData actual) {
			
		for(Map<String, String> mdAct : actual.getTypeMetaData()) {
			String attribute = mdAct.get("attribute");
			Map<String, String> mdExp = expected.get(attribute);
			assertEquals(mdExp.get("attribute"), attribute);
			assertEquals(mdExp.get("type"), mdAct.get("type"));
			assertEquals(mdExp.get("size"), mdAct.get("size"));
			assertEquals(mdExp.get("scale"), mdAct.get("scale"));
		}
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [3.1] 
	 * ----------------------------------
	 * Test getTypeMetaData method
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * Schema = "test", table = "data_type_test" 
	 * 
	 * </pre>
	 */ 	
	@Test
	public void getTypeMetaData() {
		JDBCDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "table");
		params.put("schema", "test");
		params.put("table", "data_type_test");
		reader.init(params);
		reader.enableForTypeMode();
		reader.read();
		TypeMetaData md = reader.getTypeMetaData();
		//System.out.println(md);
		verifyTypeMetaData(this.getPostgreSQLTypeMetaData(), md);
	}
}
