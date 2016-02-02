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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents an intermediate data object source created by a DataObjectReader from a data object repository.
 * Note that the equals implementation uses getClass() instead of instanceof.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */

@XmlRootElement(name = "source")
public class ScannedDataObjectSource {
	
	private String namespace;
	private String name;

	@XmlAttribute
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object object) {
	    if (this == object)
		    return true;
	    if (object == null)
		    return false;
	    if (getClass() != object.getClass())
		    return false;
	    
	    ScannedDataObjectSource other = (ScannedDataObjectSource) object;
	    return
	    		Objects.equals(namespace, other.namespace) &&
	    		Objects.equals(name, other.name);
    }
}