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
package com.ooluk.ddm.dataimport.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TypeMetaData provides metadata information that can be used to create data import rules. The type metadata
 * information is maintained as a list of property-value mappings for each data element whose metadata is requested.
 * TypeMetaData is designed to be used with DataObjectReaders. The following example will assist in understanding how
 * this data structure works.
 * 
 * <p>
 * Example: JDBCDataObjectReader provides 4 metadata properties in TypeMode
 * <ol>
 * <li>Column Name
 * <li>Column Type
 * <li>Column Size
 * <li>Column Decimal Digits
 * </ol>
 * 
 * <p>
 * It provides the values of the 4 properties for each column in the table passed to the JDBCDataObjectReader. The
 * TypeMode is only valid for TABLE scope of the JDBC DataObjectReader. The type metadata could be stored as
 * 
 * <pre>
 * properties = {"name", "type", "size", "decimal" }
 * typeMetaData = [
 *   {"name"="column-1", "type":"type-1", "size":"size-1", "decimal":"decimal-1"},
 *   {"name"="column-2", "type":"type-2", "size":"size-2", "decimal":"decimal-2"},
 *   {"name"="column-3", "type":"type-3", "size":"size-3", "decimal":"decimal-3"}
 * ]
 * </pre>
 * 
 * <p>
 * This class is not thread safe.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class TypeMetaData  {
	
	// Properties are the keys for each map in the typeMetadata list.	
	private List<String> properties;
	
	// List of mappings of metadata properties
	private List<Map<String, String>> typeMetaData;

	public TypeMetaData() {
		properties = new ArrayList<>();
		typeMetaData = new ArrayList<>();
	}
	
	/**
	 * Returns the type metadata represented as a list of property-value mappings.
	 * 
	 * @return list of property-value mappings.
	 */
	public List<Map<String, String>> getTypeMetaData() {
		return typeMetaData;
	}

	/**
	 * Sets the type metadata information.
	 * 
	 * @param typeMetaData list of property-value mappings.
	 */
	public void setTypeMetaData(List<Map<String, String>> typeMetaData) {
		this.typeMetaData = typeMetaData;
	}
	
	/**
	 * Puts the specified attribute and its type metadata.
	 * 
	 * @param propMap property-value map for each component of type metadata
	 */
	public void add(Map<String, String> propMap) {
		typeMetaData.add(propMap);
	}

	/**
	 * Returns the metadata property names.
	 * 
	 * @return list of metadata property names
	 */
	public List<String> getProperties() {
		return properties;
	}

	/**
	 * Sets the metadata property names
	 * 
	 * @param properties list of metadata property names
	 */
	public void setProperties(List<String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Adds the specified property to the properties list.
	 * 
	 * @param property property name
	 */
	public void addProperty(String property) {
		properties.add(property);
	}
	
	@Override
    public String toString() {
		return typeMetaData.toString();
	}
}