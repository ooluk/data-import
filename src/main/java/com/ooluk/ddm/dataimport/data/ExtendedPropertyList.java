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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ExtendedPropertyList maintains a list of extended properties each represented by {@link ExtendedProperty}. This class
 * is used to convert the Map<String, String> extended properties to the following XML representation.
 * 
 * <pre>
 * {@code
 * <extendedProperties>
 * 		<property name="..." value="..."></property>
 * 		<property name="..." value="..."></property>
 * 		...
 * </extendedProperties>
 * }
 * </pre>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see ExtendedProperty
 * @see ExtendedPropertyListAdapter
 * 
 */
@XmlRootElement(name="extendedProperties")
public class ExtendedPropertyList {
	
    private List<ExtendedProperty> properties;
    
    /**
     * Constructs a new empty extended property list.
     */
    public ExtendedPropertyList() {
    	properties = new ArrayList<>();
    }

    @XmlElement(name="property")
	public List<ExtendedProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ExtendedProperty> properties) {
		this.properties = properties;
	}
	
	public void addProperty(ExtendedProperty prop) {
		properties.add(prop);
	}
	
	public void sort() {
		Collections.sort(properties);
	}
}