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

import com.ooluk.ddm.dataimport.CaseMode;

/**
 * This class represents an intermediate attribute code created by a DataObjectReader from a data object repository.
 * 
 * <p>
 * This class does not maintain a reference to the containing attribute and it is therefore vital that you use objects
 * of this class from within an attribute context only. Otherwise the semantics used in equals() (codes are equal
 * if their values are equal) could cause issues with collections. Also note that the equals() methods uses getClass()
 * instead of instanceof. 
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class ScannedAttributeCode implements Comparable<ScannedAttributeCode> {
	
	private String value;
	private String description;
	private final CaseMode mode; 

	/**
	 * Constructs a ScannedAttributeCode with the specified case mode. The case mode causes all string fields to be
	 * converted to the specified case. Mixed case will leave the string fields untouched.
	 * 
	 * @param mode
	 *            the case mode
	 */
    public ScannedAttributeCode(CaseMode mode, String value, String description) {
    	this.mode = mode;
    } 

	/**
	 * Creates an attribute code with the specified value and description in the default case (MIXED).
	 * 
	 * @param value
	 *            code value
	 * @param description
	 *            code value description
	 */
    public ScannedAttributeCode(String value, String description) {
    	this(CaseMode.MIXED, value, description);
    	this.value = mode.convert(value);
    	this.description = mode.convert(description);
    }
	
    /**
     * Constructs a ScannedAttributeCode.
     */
	public ScannedAttributeCode() {
    	this(CaseMode.MIXED, "", "");
    } 
	
	@XmlAttribute
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@XmlAttribute
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int compareTo(ScannedAttributeCode other) {
		return this.value.compareTo(other.value);
	}
    
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        
        ScannedAttributeCode other = (ScannedAttributeCode) object;        
        return Objects.equals(this.value, other.value);
    }
}