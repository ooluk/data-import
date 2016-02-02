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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A representation of an extended property (custom field) of a data object or attribute. 
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see ExtendedPropertyList
 * 
 */
public class ExtendedProperty implements Comparable<ExtendedProperty> {

	@XmlAttribute
	String name;
	
	@XmlAttribute
	String value;
	
	/**
	 * Constructs a new ExtendedProperty object.
	 * 
	 * @param name
	 *            extended property name
	 * @param value
	 *            extended property value
	 */
	public ExtendedProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return name + "=" + value;
	}

	@Override
	public int compareTo(ExtendedProperty other) {		
		return this.name.compareTo(other.name);
	}
}