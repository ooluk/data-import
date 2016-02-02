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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ooluk.ddm.dataimport.CaseMode;

/**
 * This class represents an intermediate data object created by a DataObjectReader from a data object repository. Note
 * that the equals implementation uses getClass() instead of instanceof.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */

@XmlRootElement(name = "data-object")
@XmlType(propOrder = { "name", "logicalName", "source", "summary", "description", "tags", 
		"localSources", "extendedProperties", "attributes" })
public class ScannedDataObject { 
    
	private String namespace = "";
    private String name = "";
    private String logicalName = "";
    private String source = "";
    private String summary = "";
    private String description = "";
    private List<String> tags;
    private List<ScannedDataObjectSource> localSources;
    private List<ScannedAttribute> attributes;
    private Map<String, String> extnProps;
    private final CaseMode mode;
    
    /**
     * Constructs a ScannedDataObject.
     */
    public ScannedDataObject() {
    	this(CaseMode.MIXED);
    }

	/**
	 * Constructs a ScannedDataObject with the specified case mode. The case mode causes all string fields to be
	 * converted to the specified case. Mixed case will leave the string fields untouched.
	 * 
	 * @param mode
	 *            the case mode
	 */
    public ScannedDataObject(CaseMode mode) {
    	this.mode = mode;
    	tags = new ArrayList<>();
    	localSources = new ArrayList<>();
    	attributes = new ArrayList<>();
    	extnProps = new TreeMap<String, String>();
    }
    
    @XmlTransient
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = mode.convert(namespace);
    }

    @XmlElement(name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = mode.convert(name);
    }

    @XmlElement(name="logical-name")
    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = mode.convert(logicalName);
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
    public List<ScannedDataObjectSource> getLocalSources() {
        return localSources;
    }

    public void setLocalSources(List<ScannedDataObjectSource> localSources) {
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

    @XmlJavaTypeAdapter(TagsListAdapter.class)
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @XmlElementWrapper(name="attributes")
    @XmlElement(name="attribute")
    public List<ScannedAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ScannedAttribute> attributes) {
        this.attributes = attributes;
    } 
   
    @XmlJavaTypeAdapter(ExtendedPropertyListAdapter.class)
    public Map<String, String> getExtendedProperties() {
    	return extnProps;
    }
    
    public void setExtendedProperties(Map<String, String> extnProps) {
    	this.extnProps = extnProps;
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
        
        ScannedDataObject other = (ScannedDataObject) object;
        return Objects.equals(this.namespace, other.namespace) &&
        		Objects.equals(this.name, other.name);
    }
}