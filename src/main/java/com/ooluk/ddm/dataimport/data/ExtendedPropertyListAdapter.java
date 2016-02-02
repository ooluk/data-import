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
package com.ooluk.ddm.dataimport.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This class is a XmlJavaTypeAdapter to convert extended properties list to the following XML representation
 * 
 * <pre>
 * 
 * {@code
 * <extendedProperties>
 * 		<property name="..." value="..."></property>
 * 		<property name="..." value="..."></property>
 * 		...
 * </extendedProperties>
 * }
 * 
 * <pre>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see ExtendedPropertyList
 * @see ExtendedProperty
 * 
 */
public class ExtendedPropertyListAdapter extends XmlAdapter<ExtendedPropertyList, Map<String, String>> {
	
    @Override
	public ExtendedPropertyList marshal(Map<String, String> extnProps) throws Exception {
    	ExtendedPropertyList propsList = new ExtendedPropertyList();
        for (Map.Entry<String, String> entry : extnProps.entrySet()) {
        	propsList.addProperty(new ExtendedProperty(entry.getKey(), entry.getValue()));
        }
        propsList.sort();
        return propsList;
    }

    @Override
	public Map<String, String> unmarshal(ExtendedPropertyList propsList) throws Exception {
        Map<String, String> extnProps = new HashMap<String, String>();
        List<ExtendedProperty> list = propsList.getProperties();
        for (ExtendedProperty property : list) {
        	extnProps.put(property.name, property.value);
        }
        return extnProps;
    }
} 