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
package com.ooluk.ddm.dataimport.workers.cobol;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.ImportException;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedAttributeCode;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;
import com.ooluk.ddm.dataimport.workers.TypeMetaData;

/**
 * @author Siddhesh Prabhu
 *
 */
public class COBOLDataObjectReaderTest {
	
	private final int COPYBOOKS_IN_PDS = 3;
	private final int FIELDS_IN_COPYBOOK = 61;

	private RuleStore ruleStore;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {	
		ruleStore = new RuleStore();
		ruleStore.addRule("data-type", "UINT", "%type%(%size%)");
		ruleStore.addRule("data-type", "SINT", "%type%(%size%)");
		ruleStore.addRule("data-type", "ALPHA", "%type%(%size%)");
		ruleStore.addRule("data-type", "ALPHANUM", "%type%(%size%)");
		ruleStore.addRule("data-type", "UNUM", "%type%([!%size%-%scale%!],[!%scale%!])");
		ruleStore.addRule("data-type", "SNUM", "%type%([!%size%-%scale%!],[!%scale%!])");
		
		ruleStore.addRule("common-type", "UINT", "INT(%size%)");
		ruleStore.addRule("common-type", "SINT", "INT(%size%)");
		ruleStore.addRule("common-type", "ALPHA", "CHAR(%size%)");
		ruleStore.addRule("common-type", "ALPHANUM", "CHAR(%size%)");
		ruleStore.addRule("common-type", "UNUM", "DECIMAL([!%size%-%scale%!],[!%scale%!])");
		ruleStore.addRule("common-type", "SNUM", "DECIMAL([!%size%-%scale%!],[!%scale%!])");
		ruleStore.addRule("common-type", "FLOAT4", "%type%");
		ruleStore.addRule("common-type", "FLOAT8", "%type%");
		
		ruleStore.addRule("namespace", "nspace", "%prefix%");
	}
	
	/**
	 * Sets up the common parameters across test cases for the reader
	 * 
	 * @return a param-value map for the common parameters
	 */
	private HashMap<String, Object> getCommonParams() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("namespacePrefix", "TEMP_SPACE");
		params.put("ruleGroup", "COBOL");
		params.put("ruleStore", ruleStore);
		return params;		
	}
	
	private COBOLDataObjectReader getReader() {
		COBOLDataObjectReader reader = new COBOLDataObjectReader();
		reader.setLogWriter(new OutputStreamWriter(System.out));
		return reader;		
	}
	
	private String getFile(String file) {
		URL url = this.getClass().getResource(file);
		return url.getFile();
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1] 
	 * ----------------------------------
	 * Test of init() method of class COBOLDataObjectReader.
	 * 
	 * </pre>
	 */
    private void verifyCommonParameters(COBOLDataObjectReader reader) {
		// Verify the namespace prefix
		assertEquals("TEMP_SPACE", reader.getNamespacePrefix());
		// Verify rule group
		assertEquals("COBOL", reader.getRuleGroup());
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
	 * Test init() for success for COPYBOOK scope read
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_Copybook() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(COBOLMetaDataScope.COPYBOOK, reader.getScope());
		// Verify the copybook file
		assertEquals(getFile("/copybook"), reader.getCopybookFile());
	}
	
    /**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.2] 
	 * ----------------------------------
	 * Test init() for success for PDS scope read
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */  
	@Test
	public void init_For_PDS() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "pds");
		params.put("copybookDirectory", getFile("/pds"));
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
		// Verify the scope
		assertEquals(COBOLMetaDataScope.PDS, reader.getScope());
		// Verify PDS directory 
		assertEquals(getFile("/pds"), reader.getCopybookDirectory());
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.3] 
	 * ----------------------------------
	 * Test init() for missing scope parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [scope] missing"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Missing_Scope() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [scope] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.4] 
	 * ----------------------------------
	 * Test init() for invalid scope parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [scope] missing"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Invalid_Scope() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copy");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: invalid value for parameter [scope]"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.5] 
	 * ----------------------------------
	 * Test init() for missing copybookFile parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [copybookFile] missing"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Missing_Copybook_File() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [copybookFile] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.6] 
	 * ----------------------------------
	 * Test init() for non-existent copybookFile 
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: copybook nocopybook does not exist"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_NonExistent_Copybook_File() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", "nocopybook");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: copybook nocopybook does not exist"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.7] 
	 * ----------------------------------
	 * Test init() for directory copybookFile parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: copybook "directory" does not denote a file"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Directory_Copybook_File() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		String dir = getFile("/com/ooluk");
		params.put("copybookFile", dir);
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: copybook " + dir + " does not denote a file"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.8] 
	 * ----------------------------------
	 * Test init() for missing copybookDirectory parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [copybookDirectory] missing"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Missing_Copybook_Directory() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "pds");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [copybookDirectory] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.9] 
	 * ----------------------------------
	 * Test init() for non-existent copybookDirectory parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: copybook nocopybook does not exist"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_NonExistent_Copybook_Directory() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "pds");
		params.put("copybookDirectory", "nodirectory");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: directory nodirectory does not exist"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.10] 
	 * ----------------------------------
	 * Test init() for file copybookDirectory parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: PDS "file" does not denote a directory"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_File_Copybook_Directory() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "pds");
		String file = getFile("/copybook");
		params.put("copybookDirectory", file);
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: directory " + file + " does not denote a directory"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.11] 
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
	public void init_For_Missing_Namespace_Prefix() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		params.remove("namespacePrefix");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.12] 
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
	public void init_For_Empty_Namespace() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		params.put("namespacePrefix", "");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [namespacePrefix] empty"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.13] 
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
	public void init_For_Missing_RuleStore() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		params.remove("ruleStore");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [ruleStore] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1.14] 
	 * ----------------------------------
	 * Test init() for invalid Case Mode
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: invalid value for parameter [case]"]
	 * 
	 * </pre>
	 */
	@Test
	public void init_For_Invalid_CaseMode() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = this.getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		params.put("case", "INVALID");
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: invalid value for parameter [case]"));
		reader.init(params);
	}
	
	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2] 
	 * ----------------------------------
	 * Test of read() method of class COBOLDataObjectReader.
	 * 
	 * </pre>
	 */

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.1] 
	 * ----------------------------------
	 * Test read() for success for COPYBOOK scope
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should read the attribute data in the copybook.
	 * 
	 * </pre>
	 */
	@Test
	public void read_For_Copybook() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = this.getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		reader.init(params);
		ScannedDataObject dObj = reader.read();		
		assertEquals("copybook", dObj.getName());
		String[][] expAttributes = COBOLTestConstants.ATTR_DATA;
		for (int i = 0; i < dObj.getAttributes().size(); i++) {
			ScannedAttribute attr = dObj.getAttributes().get(i);
			assertEquals(expAttributes[i][0], attr.getName());
			assertEquals(expAttributes[i][1], attr.getDataType());
			assertEquals(expAttributes[i][2], attr.getCommonType());
			assertEquals(expAttributes[i][3], attr.getDefaultValue());
			// Last attribute has codes
			if (i == dObj.getAttributes().size()-1) {
				String[][] expCodes = COBOLTestConstants.CODE_DATA;
				for (int j = 0 ; j < expCodes.length; j++) {
					ScannedAttributeCode code = attr.getCodes().get(j);
					assertEquals(expCodes[j][0], code.getValue());
					assertEquals(expCodes[j][1], code.getDescription());
				}
			}
		}
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.2] 
	 * ----------------------------------
	 * Test read() for success for PDS scope
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should read the attribute data for all copybooks in the PDS.
	 * 
	 * </pre>
	 */
	@Test
	public void read_For_PDS() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = this.getCommonParams();
		params.put("scope", "pds");
		params.put("copybookDirectory", getFile("/pds"));
		reader.init(params);
		ScannedDataObject dObj = null;
		for (int i = 1; i <= COPYBOOKS_IN_PDS; i++) {
			dObj = reader.read();	
			assertEquals("copybook"+i, dObj.getName());		
			// we just check number of attributes - the verification of each has been done in COPYBOOK scope.
			assertEquals(FIELDS_IN_COPYBOOK, dObj.getAttributes().size());
		}
		assertNull(reader.read());	
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [3] 
	 * ----------------------------------
	 * Test getTypeMetadata() for success
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should read the attribute data for all copybooks in the PDS.
	 * 
	 * </pre>
	 */
	
	private Map<String, Map<String, String>> getCOBOLTypeMetaData() {
		Map<String, Map<String, String>> mapping = new HashMap<>();
		for (String[] field : COBOLTestConstants.TYPE_METADATA) {
			String attribute = field[0];
			String declaration = field[1];
			String type = field[2];
			String size = field[3];
			String scale = field[4];
			String usage = field[5];
			Map<String, String> metadata = new HashMap<>();
			metadata.put("attribute", attribute);
			metadata.put("declaration", declaration);
			metadata.put("type", type);
			metadata.put("size", size);
			metadata.put("scale", scale);
			metadata.put("usage", usage);
			mapping.put(attribute, metadata);
		}
		return mapping;
	}
	
	private void verifyTypeMetaData(Map<String, Map<String, String>> expected, TypeMetaData actual) {
			
		for(Map<String, String> mdAct : actual.getTypeMetaData()) {
			String attribute = mdAct.get("attribute");
			Map<String, String> mdExp = expected.get(attribute);
			assertEquals(mdExp.get("attribute"), attribute);
			assertEquals(mdExp.get("declaration"), mdAct.get("declaration"));
			assertEquals(mdExp.get("type"), mdAct.get("type"));
			assertEquals(mdExp.get("size"), mdAct.get("size"));
			assertEquals(mdExp.get("scale"), mdAct.get("scale"));
			assertEquals(mdExp.get("usage"), mdAct.get("usage"));
		}
	}
	
	@Test
	public void getTypeMetadata_For_Success() {
		COBOLDataObjectReader reader = getReader();
		HashMap<String, Object> params = this.getCommonParams();
		params.put("scope", "copybook");
		params.put("copybookFile", getFile("/copybook"));
		reader.init(params);
		reader.enableForTypeMode();
		reader.read();
		TypeMetaData md = reader.getTypeMetaData();
		this.verifyTypeMetaData(getCOBOLTypeMetaData(), md);
		/*for (Map<String, String> item : md.getTypeMetaData()) {
			System.out.println();
		}*/
	}
}