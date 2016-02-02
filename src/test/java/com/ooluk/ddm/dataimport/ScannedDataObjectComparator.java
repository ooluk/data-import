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
package com.ooluk.ddm.dataimport;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
public class ScannedDataObjectComparator {
	
	private static void compare(Object exp, Object act, Method method) {
		try {
	        assertEquals(method.invoke(exp), method.invoke(act));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	        e.printStackTrace();
        }	
	}
	
	private static void compare(Object exp, Object act, List<String> gettersToCompare) {
		Method[] methods = exp.getClass().getDeclaredMethods();
		for (Method m : methods) {
			if (gettersToCompare.contains(m.getName())) {
				compare(exp, act, m);
			}
		}	
	}

	public static void compare(ScannedDataObjectSource exp, ScannedDataObjectSource act) {
		
		compare(exp, act, Arrays.asList(new String[] {
				"getNamespace", 
				"getName"
				})
		);
	}

	public static void compare(ScannedDataObject exp, ScannedDataObject act) {

		compare(exp, act, Arrays.asList(new String[] {
				"getNamespace", 
				"getName", 
				"getLogicalName", 
				"getSource", 
				"getSummary", 
				"getDescription", 
				"getTags", 
				"getExtendedProperties"
				})
		);
		
		// Compare ScannedDataObjectSource(s)
		assertEquals(exp.getLocalSources().size(), act.getLocalSources().size());
		for (int i = 0; i < exp.getLocalSources().size(); i++) {
			compare(exp.getLocalSources().get(i), act.getLocalSources().get(i));
		}
		
		// Compare ScannedAttribute(s)
		assertEquals(exp.getAttributes().size(), act.getAttributes().size());
		for (int i = 0; i < exp.getAttributes().size(); i++) {
			compare(exp.getAttributes().get(i), act.getAttributes().get(i));
		}
	}

	public static void compare(ScannedAttributeSource exp, ScannedAttributeSource act) {
		
		compare(exp, act, Arrays.asList(new String[] {
				"getNamespace", 
				"getObjectName", 
				"getAttributeName"
				})
		);
	}

	public static void compare(ScannedAttributeCode exp, ScannedAttributeCode act) {
		
		compare(exp, act, Arrays.asList(new String[] {
				"getValue", 
				"getDescription"
				})
		);
	}

	public static void compare(ScannedAttribute exp, ScannedAttribute act) {
		
		compare(exp, act, Arrays.asList(new String[] {
				"getName", 
				"getLogicalName", 
				"getSeqNo", 
				"getDataType", 
				"getCommonType", 
				"isKey", 
				"getParentAttribute", 
				"isRequired", 
				"getDefaultValue", 
				"getSource", 
				"getSummary", 
				"getDescription", 
				"getTags", 
				"getExtendedProperties"
				})
		);
		
		// Compare ScannedAttributeSource(s)
		assertEquals(exp.getLocalSources().size(), act.getLocalSources().size());
		for (int i = 0; i < exp.getLocalSources().size(); i++) {
			compare(exp.getLocalSources().get(i), act.getLocalSources().get(i));
		}
		
		// Compare ScannedAttributeCode(s)
		assertEquals(exp.getCodes().size(), act.getCodes().size());
		for (int i = 0; i < exp.getCodes().size(); i++) {
			compare(exp.getCodes().get(i), act.getCodes().get(i));
		}
	}
}
