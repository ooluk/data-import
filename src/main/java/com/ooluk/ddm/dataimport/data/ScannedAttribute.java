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
/**
 * 
 */
package com.ooluk.ddm.dataimport.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ooluk.ddm.dataimport.CaseMode;

/**
 * This class represents an intermediate attribute created by a DataObjectReader from a data object repository.
 * 
 * <p>
 * This class does not maintain a reference to the containing data object and it is therefore vital that you use objects
 * of this class from within a data object context only. Otherwise the semantics used in equals() (attributes are equal
 * if their names are equal) could cause issues with collections. Also note that the equals() methods uses getClass()
 * instead of instanceof. 
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */

@XmlRootElement(name = "attribute")
@XmlType(propOrder = { "name", "logicalName", "seqNo", "dataType", "commonType", "key", "parentAttribute", "required",
        "defaultValue", "source", "localSources", "summary", "description", "tags", "extendedProperties", "codes" })
public class ScannedAttribute {
    
	private String name = "";
    private String logicalName = "";
    private Integer seqNo = 0;
    private String dataType = "";
    private String commonType = "";
    private boolean key = false;
    private String parentAttribute = "";
    private boolean required = false;
    private String defaultValue = "";
    private String source = "";
    private String summary = "";
    private String description = "";
    private List<String> tags;
    private List<ScannedAttributeSource> localSources;
    private Map<String, String> extnProps;
    private List<ScannedAttributeCode> attributeCodes;
    private final CaseMode mode;
    
    /**
     * Constructs a ScannedAttribute.
     */
    public ScannedAttribute() {
    	this(CaseMode.MIXED);
	}

	/**
	 * Constructs a ScannedAttribute with the specified case mode. The case mode causes all string fields to be
	 * converted to the specified case. Mixed case will leave the string fields untouched.
	 * 
	 * @param mode
	 *            the case mode
	 */
    public ScannedAttribute(CaseMode mode) {
    	this.mode = mode;
    	tags = new ArrayList<>();
    	localSources = new ArrayList<>();
    	attributeCodes = new ArrayList<>();
    	extnProps = new HashMap<String, String>();
    }
    
    @XmlElement(name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = mode.convert(name);
    }

    @XmlElement(name="position")
    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    @XmlElement(name="logical-name")
    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
    	this.logicalName = mode.convert(logicalName);
    }

    @XmlElement(name="data-type")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = mode.convert(dataType);
    }

    @XmlElement(name="common-type")
    public String getCommonType() {
        return commonType;
    }

    public void setCommonType(String commonType) {
        this.commonType = mode.convert(commonType);
    }

    @XmlElement(name="key")
    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    @XmlElement(name="parent-attribute")
    public String getParentAttribute() {
        return parentAttribute;
    }

    public void setParentAttribute(String parentAttribute) {
        this.parentAttribute = mode.convert(parentAttribute);
    }

    @XmlElement(name="required")
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @XmlElement(name="default-value")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = mode.convert(defaultValue);
    }

    @XmlElement(name="external-sources")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = mode.convert(source);
    }

    @XmlElementWrapper(name="local-sources")
    @XmlElement(name="local-source")
    public List<ScannedAttributeSource> getLocalSources() {
        return localSources;
    }

    public void setLocalSources(List<ScannedAttributeSource> localSources) {
        this.localSources = localSources;
    } 

    @XmlElement(name="summary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @XmlElement(name="description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name="tags")
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    @XmlJavaTypeAdapter(ExtendedPropertyListAdapter.class)
    public Map<String, String> getExtendedProperties() {
    	return extnProps;
    }
    
    public void setExtendedProperties(Map<String, String> extnProps) {
    	this.extnProps = extnProps;
    }

    @XmlElementWrapper(name="codes")
    @XmlElement(name="code")
    public List<ScannedAttributeCode> getCodes() {
        return attributeCodes;
    }

    public void setCodes(List<ScannedAttributeCode> codes) {
        this.attributeCodes = codes;
    }

    public void addCode(ScannedAttributeCode code) {
        attributeCodes.add(code);
    }
    
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
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
	    
	    ScannedAttribute other = (ScannedAttribute) object;        
	    return Objects.equals(this.name, other.name);
	}
}