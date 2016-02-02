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
package com.ooluk.ddm.dataimport.dif.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedAttributeCode;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;

/**
 * @author Siddhesh
 * @since 1.0
 * 
 */
public class DummyMetadataAdapter implements MetadataAdapter {
	
	private static final int NAMESPACES = 1;
	private static final int DATA_OBJECTS_PER_NSPACE = 1;
	private static final int ATTRIBUTES_PER_NSPACE = 25;
	private static final int ATTRIBUTE_CODES = 25;
	private static final int DISTINCT_TAGS = 5;	
	private final String file;

	public DummyMetadataAdapter(String file) {
		this.file = file; 
	}
	
	@Override
    public void extractAndWriteMetadata() {
		
		DIFWriter writer = new DIFWriter();
		writer.init(file);
		
		for (int i = 1; i <= NAMESPACES; i++) {
			String namespace = "NSPACE_" + i;
			writer.beginNamespace(namespace);
			for (int j = 1; j <= DATA_OBJECTS_PER_NSPACE; j++) {
				ScannedDataObject obj = generateDataObject(namespace, j);
				for (int k = 1; k <= ATTRIBUTES_PER_NSPACE; k++) {
					obj.getAttributes().add(this.generateAttibute(namespace, j, k));
				}
				writer.writeDataObject(obj);
			}	
		}
		
		writer.close();
	}
	
	private ScannedDataObject generateDataObject(String namespace, int i) {
		ScannedDataObject obj = new ScannedDataObject();
		String name = "OBJ_"+i; 
		obj.setName(name);
		obj.setLogicalName("Name "+i);
		obj.setTags(Arrays.asList(new String[] {"Tag_"+ (i % DISTINCT_TAGS), "Tag_" + DISTINCT_TAGS}));
		String source = namespace + ".OBJ_" + (i-1);
		obj.setSource(source);
		Map<String, String> extnProps = new HashMap<>();
		extnProps.put("field1", "value1");
		extnProps.put("field2", "value2");
		obj.setExtendedProperties(extnProps);
		obj.setSummary("<Summary /> " + i);
		obj.setDescription("Description " + i);
		return obj;
	}
	
	private ScannedAttribute generateAttibute(String namespace, int obj_no, int i) {
		ScannedAttribute attr = new ScannedAttribute();
		String name = "ATTR_"+i; 
		attr.setName(name);
		attr.setLogicalName("Name "+i);
		attr.setTags(Arrays.asList(new String[] {"Tag_"+ (i % 5)}));
		attr.setSeqNo(i);
		attr.setDataType("Data_Type_" + i);
		attr.setCommonType("Common_Type_" + i);
		String parent = (i % 5) == 0 ? namespace + ".OBJ_" + (obj_no-1) + ".ATTR_" + (i-1) : "";
		attr.setParentAttribute(parent);
		attr.setKey(i == 1);
		attr.setRequired(i % 5 == 0);
		attr.setDefaultValue(i % 5 == 0 ? "Default_" + i : "");
		String source = (i % 5) == 0 ? namespace + ".OBJ_" + (obj_no-1) + ".ATTR_" + i : "";
		attr.setSource(source);
		Map<String, String> extnProps = new HashMap<>();
		extnProps.put("field1", "value1");
		attr.setExtendedProperties(extnProps);
		attr.setSummary("Summary " + i);
		attr.setDescription("Description " + i);
		if (i % 25 == 0) {
			attr.setCodes(this.generateCodes());
		}
		return attr;
	}
	
	private List<ScannedAttributeCode> generateCodes() {
		List<ScannedAttributeCode> codes = new ArrayList<>();
		for (int i = 1; i <= ATTRIBUTE_CODES; i++) {
			codes.add(new ScannedAttributeCode("Value_"+i, "Code Description " + i));
		}
		return codes;
	}
}