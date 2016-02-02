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
package com.ooluk.ddm.dataimport.workers.xml;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.ImportException;
import com.ooluk.ddm.dataimport.ScannedDataObjectComparator;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedAttributeCode;
import com.ooluk.ddm.dataimport.data.ScannedAttributeSource;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.data.ScannedDataObjectSource;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class XMLDataObjectReaderTest {

	private Map<Integer, ScannedDataObject> data;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
	}

	/**
	 * Sets up the common parameters across test cases for the reader
	 * 
	 * @return a param-value map for the common parameters
	 */
	private HashMap<String, Object> getCommonParams() {
		HashMap<String, Object> params = new HashMap<>();
		URL url = this.getClass().getResource("/dif.xml");
		params.put("file", url.getFile());
		return params;
	}

	private XMLDataObjectReader getReader() {
		XMLDataObjectReader reader = new XMLDataObjectReader();
		reader.setLogWriter(new OutputStreamWriter(System.out));
		return reader;
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1] 
	 * ----------------------------------
	 * Test of init(params) method of class XMLDataObjectReader.
	 * 
	 * </pre>
	 */
	private void verifyCommonParameters(XMLDataObjectReader reader) {
		URL url = this.getClass().getResource("/dif.xml");
		// Verify the xml file location
		assertEquals(url.getFile(), reader.getXMLFile());
		// Verify case mode
		assertEquals(CaseMode.MIXED, reader.getCaseMode());

	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.1] 
	 * ----------------------------------
	 * Test init() for success
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should be properly initialized
	 * 
	 * </pre>
	 */
	@Test
	public void testInitForSuccess() {
		XMLDataObjectReader reader = getReader();
		HashMap<String, Object> params = getCommonParams();
		reader.init(params);
		// Verify common parameters
		verifyCommonParameters(reader);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.2] 
	 * ----------------------------------
	 * Test init() for missing XML file parameter
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Initialization error: parameter [file] missing"]
	 * 
	 * </pre>
	 */
	@Test
	public void testInitForMissingXMLFile() {
		XMLDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		exception.expect(ImportException.class);
		exception.expectMessage(equalTo("Initialization error: parameter [file] missing"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.3] 
	 * ----------------------------------
	 * Test init() for non existent XML file
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Error opening XML file:"]
	 * 
	 * </pre>
	 */
	@Test
	public void testInitForNonExistentXMLFile() {
		XMLDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		params.put("file", "invalid/nonexistent.xml");
		exception.expect(ImportException.class);
		exception.expectMessage(startsWith("Error opening XML file:"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.4] 
	 * ----------------------------------
	 * Test init() for directory
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * ImportException ["Error opening XML file:"]
	 * 
	 * </pre>
	 */
	@Test
	public void testInitForDirectoryAsXMLFile() {
		XMLDataObjectReader reader = getReader();
		HashMap<String, Object> params = new HashMap<>();
		URL url = this.getClass().getResource("/com/ooluk");
		params.put("file", url.getFile());
		exception.expect(ImportException.class);
		exception.expectMessage(startsWith("Error opening XML file:"));
		reader.init(params);
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [1.5] 
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
	public void testInitForInvalidCaseMode() {
		XMLDataObjectReader reader = getReader();
		HashMap<String, Object> params = this.getCommonParams();
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
	 * Test of read() method of class XMLDataObjectReader.
	 * 
	 * </pre>
	 */

	/**
	 * Creates an in-memory mapping similar to the data in iif.xml
	 */
	private void buildDataMap() {
		data = new HashMap<Integer, ScannedDataObject>();
		for (int i = 1; i <= 4; i++) {
			ScannedDataObject dObj = new ScannedDataObject();
			dObj.setNamespace("TEMP_SPACE" + (i <= 2 ? "1" : "2"));
			dObj.setName("OBJECT_" + i);
			dObj.setLogicalName("Object " + i);
			List<String> tags = Arrays.asList(new String[] { "Tag 1" + i, "Tag 2" + i });
			dObj.setTags(tags);
			dObj.setSource("External Source " + i);
			dObj.setSummary("Object " + i + " Short Description");
			dObj.setDescription("Object " + i + " Long Description");
			dObj.setAttributes(getAttributes(i));
			dObj.setExtendedProperties(new HashMap<String, String>());
			if (i == 1) {
				dObj.getExtendedProperties().put("field1", "Field 1");
				ScannedDataObjectSource source = new ScannedDataObjectSource();
				source.setNamespace("TEMP_SPACE1"); source.setName("OBJECT_2"); dObj.getLocalSources().add(source);
				source = new ScannedDataObjectSource();
				source.setNamespace("TEMP_SPACE2"); source.setName("OBJECT_3"); dObj.getLocalSources().add(source);				
			}
			data.put(i, dObj);
		}
	}

	/**
	 * Creates in-memory mapping of attribute data similar to that in iif.xml for the specified data object.
	 * 
	 * @param i data object item number
	 * @return list of attributes
	 */
	private List<ScannedAttribute> getAttributes(int i) {
		if (i == 2) {
			return Collections.<ScannedAttribute> emptyList();
		}
		List<ScannedAttribute> attrs = new ArrayList<ScannedAttribute>();
		for (int j = 1; j <= 2; j++) {
			// only the 4th data object has 2 attributes
			if (j == 2 && i != 4) {
				break;
			}
			ScannedAttribute attr = new ScannedAttribute();
			attr.setName("ATTRIBUTE_" + i + j);
			attr.setLogicalName("Attribute " + i + j);
			attr.setSeqNo(j);
			List<String> tags = Arrays.asList(new String[] { "Tag 1" + i + j, "Tag 2" + i + j });
			attr.setTags(tags);
			attr.setDataType("VARCHAR(30)");
			attr.setCommonType("AN(30)");
			// First column is the key
			attr.setKey(j == 1 ? true : false);
			attr.setRequired(i == 1 ? false : true);
			attr.setDefaultValue(i == 3 ? "''" : "");
			attr.setSource("External Source " + i + j);
			attr.setParentAttribute(i == 3 ? "TEMP_SPACE1.OBJECT_1.ATTRIBUTE_11" : "");
			attr.setSummary(j == 2 ? "Attribute 42 <html/>Short Description" : "Attribute " + i + j
			        + " Short Description");
			attr.setDescription("Attribute " + i + j + " Long Description");
			attr.setExtendedProperties(new HashMap<String, String>());
			if (i == 3) {
				attr.getExtendedProperties().put("afield1", "A Field 31");
				ScannedAttributeSource source = new ScannedAttributeSource();
				source.setNamespace("TEMP_SPACE1"); 
				source.setObjectName("OBJECT_1");
				source.setAttributeName("ATTRIBUTE_11");
				attr.getLocalSources().add(source);		
				attr.getCodes().add(new ScannedAttributeCode("A", "ALL"));		
			}
			attrs.add(attr);
		}
		return attrs;
	}

	/**
	 * <pre>
	 * ----------------------------------
	 * Case: [2.1] 
	 * ----------------------------------
	 * Test read() for success
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * The reader should read the data object and attribute data in the XML file.
	 * 
	 * </pre>
	 */
	@Test
	public void testReadForSuccess() {
		XMLDataObjectReader reader = new XMLDataObjectReader();
		reader.setLogWriter(new PrintWriter(System.out));
		HashMap<String, Object> params = getCommonParams();
		reader.init(params);
		ScannedDataObject dObj = null;
		buildDataMap();
		for (int i = 1; i <= 4; i++) {
			dObj = reader.read();
			ScannedDataObject expObj = data.get(i);
			ScannedDataObjectComparator.compare(expObj, dObj);
		}
	}
}