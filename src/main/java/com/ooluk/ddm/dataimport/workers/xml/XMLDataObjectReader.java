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
package com.ooluk.ddm.dataimport.workers.xml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.MessageKey;
import com.ooluk.ddm.dataimport.Messages;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedAttributeCode;
import com.ooluk.ddm.dataimport.data.ScannedAttributeSource;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.data.ScannedDataObjectSource;
import com.ooluk.ddm.dataimport.workers.AbstractDataObjectReader;


/**
 * XMLDataObjectReader is an implementation of the DataObjectReader for reading data from DIF (Data Import Format) XML
 * file. This implementation uses StAX (Streaming API for XML) available in java.xml.stream.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class XMLDataObjectReader extends AbstractDataObjectReader {
	
	private final Logger log = LogManager.getLogger();

    /*
     * Parameters passed on call to init(...) that need to be saved.
     */
    // The case mode is optional and defaults to MIXED case 
	private CaseMode caseMode = CaseMode.MIXED;
	
	// The XML file path and name
    private String xmlFile;    
    
    /*
     * Member variables
     */
	// Namespace being processed. We save this because this applies to all data objects nested in the namespace tag.
    private String namespace = "";
    
    // A flag used to signal the completion of initial configuration	
    private boolean configured = false;
    
    // The XML reader
    private XMLEventReader reader;
        
    /**
	 * Parameterless constructor 
	 */
    public XMLDataObjectReader() {    	
    } 
	
	@Override
	public void init() {

		if (!configured) {
			String msg = Messages.getMessage(MessageKey.READ_NOT_CONFIG, "XMLDataObjectReader");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		XMLInputFactory factory = XMLInputFactory.newInstance();	
		try {
			reader = factory.createXMLEventReader(new FileReader(xmlFile));
		} catch (FileNotFoundException | XMLStreamException e) {
			log.error(e.getMessage(), e);
			String msg = Messages.getMessage(MessageKey.XML_FILE_OPEN_ERR, e.getMessage());
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
	}
		
	@Override
	public void init(Map<String, Object> params) {
    	
		extractXmlFile(params);
    	configured = true;
        init();
        extractCaseMode(params);
	}
	
	/**
	 * Extracts the XML file name (full path and name) from the initialization parameters.
	 * 
	 * <p>
	 * The XML file is specified with the parameter name "file".
	 * </p>
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractXmlFile(Map<String, Object> params) {

    	xmlFile = (String) params.get("file");
    	if (xmlFile == null) {
        	String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "file");
        	super.appendStatusLine(msg);
    		throwImportException(msg);
    	}
	}

	/**
	 * Extracts the case mode from the initialization parameters. The case mode is optional.
	 * 
	 * <p>
	 * The case mode is specified with the parameter name "case".
	 * </p>
	 * 
	 * @param params
	 *            initialization parameters
	 */
	private void extractCaseMode(Map<String, Object> params) {

		// Case Conversion - not mandatory
		String mode = (String) params.get("case");
		if (mode != null) {
			try {
				caseMode = CaseMode.valueOf(mode.toUpperCase());
			} catch (IllegalArgumentException ex) {
				String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "case");
				super.appendStatusLine(msg);
				throwImportException(msg);
			}
		}
   	}	
    
	@Override
	public ScannedDataObject read() {  
    	ScannedDataObject dObj = readDataObject();
    	return dObj;
	}

	@Override
	public void close() {   
		
		try {
			if (reader != null)
				reader.close();
		} catch (XMLStreamException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Reads the next data object from the XML file.
	 * 
	 * @return the read data object.
	 */
	private ScannedDataObject readDataObject() {
				
		ScannedDataObject dObj = null;
		
		// Character data buffer 
		StringBuilder sb = new StringBuilder();
		
		/*
		 * Character events can be thrown for chunks of data within an element. We need to capture all the chunks and
		 * combine them. This flag is used to buffer the data chunks while an element is being processed. We set this
		 * flag to true upon element start and set it to false after element end. We do not need to match start and end
		 * elements because elements can cannot contain data and sub elements. Elements with character data start and
		 * end immediately and we are only concerned with these for character data buffering.
		 */
		boolean processing = false;
		
		/*
		 * Elements that have data in attributes are processed under the START_ELEMENT event. Elements that have
		 * character data are processed upon END_ELEMENT event after the contents have been gathered.
		 */
        try {
        	
			while (reader.hasNext()) {
				
				XMLEvent event = reader.nextEvent();
				
				switch (event.getEventType()) {
					
					case XMLEvent.START_ELEMENT:
						StartElement elStart = event.asStartElement();
						String elStartName = elStart.getName().toString();						
						switch(elStartName) {
							
							case "namespace":
								namespace = extractNamespace(elStart);
								break;
								
							case "data-object":
								// If <data-object> element start is detected begin DataObject processing
								dObj = createDataObject();
								break;
								
							case "local-source":
								dObj.getLocalSources().add(extractObjectSource(elStart));
								break;
								
							case "property":
								addExtendedProperty(dObj, elStart);
								break;
								
							case "attributes":
								// If <attributes> element is detected branch to process attributes
								dObj.setAttributes(createAttributes());
								break;
						}
						// Clear the character buffer and set element processing to true
						sb.delete(0, sb.length());
						processing = true;
						break;
						
					case XMLEvent.END_ELEMENT:
						EndElement elEnd = event.asEndElement();
						String elEndName = elEnd.getName().toString();
						
						switch (elEndName) {
							case "data-object": 
								if (dObj.getAttributes() == null) {
									dObj.setAttributes(Collections.<ScannedAttribute> emptyList());
								}
								// We return on encountering </data-object> as we only process one data object at a time. 
								return dObj;
						}
						processDataObjectElementEnd(elEndName, dObj, sb.toString());
						processing = false;
						sb.delete(0, sb.length());
						break;
						
					case XMLEvent.CHARACTERS:
						if (processing) {
							sb.append(event.asCharacters().getData());
						}
						break;
				}
			}
		} catch (XMLStreamException e) {
			log.error(e.getMessage(), e);
			throwImportException(e);
		} 
		return dObj;
	}
	
	/**
	 * Extracts the namespace from the "namespace" element.
	 * 
	 * @param elStart
	 *            start element for namespace
	 *            
	 * @return the namespace name
	 */
	private String extractNamespace(StartElement elStart) {
		return elStart.getAttributeByName(QName.valueOf("name")).getValue();
	}

	/**
	 * Creates a ScannedDataObject.
	 * 
	 * @return created ScannedDataObject
	 */
	private ScannedDataObject createDataObject() {
		ScannedDataObject dObj = new ScannedDataObject(caseMode);
		// We assume the namespace has been extracted before this method is called
		dObj.setNamespace(namespace);
		return dObj;
	}

	/**
	 * Extracts a data object source from a "local source" element representing a data object
	 * 
	 * @param elStart
	 *            start element
	 *            
	 * @return data object source represented by the element.
	 */
	private ScannedDataObjectSource extractObjectSource(StartElement elStart) {
		String namespace = elStart.getAttributeByName(QName.valueOf("namespace")).getValue();
		String name = elStart.getAttributeByName(QName.valueOf("name")).getValue();		
		ScannedDataObjectSource source = new ScannedDataObjectSource();
		source.setNamespace(namespace);
		source.setName(name);
		return source;
	}
	
	/**
	 * Extracts the name and value of an extended property from the "extended property" element and adds it to the data
	 * object.
	 * 
	 * @param dObj
	 *            data object
	 * @param elStart
	 *            extended property start element
	 */
	private void addExtendedProperty(ScannedDataObject dObj, StartElement elStart) {
		String extnProperty = elStart.getAttributeByName(QName.valueOf("name")).getValue();
		String value = elStart.getAttributeByName(QName.valueOf("value")).getValue();
		dObj.getExtendedProperties().put(extnProperty, value);		
	}

	/**
	 * Performs processing upon encountering element end event.
	 * 
	 * @param elName
	 *            element name
	 * @param dObj
	 *            reference to data object
	 * @param data
	 *            the character data of the element
	 */
	private void processDataObjectElementEnd(String elName, ScannedDataObject dObj, String data) {
		
		switch (elName) {
			
			case "name":
				dObj.setName(data);
		        String msg = Messages.getMessage(MessageKey.READ_IMPORTING, data);
		        super.appendStatusLine("-------------------------------------------");
		        super.appendStatusLine(msg);
		        super.appendStatusLine("-------------------------------------------");
		        break;
		        
			case "logical-name":
				dObj.setLogicalName(data);
		        break;
		        
			case "summary":
				dObj.setSummary(data);
		        break;
		        
			case "description":
				dObj.setDescription(data);
		        break;
		        
			case "tags":
				dObj.setTags(Arrays.asList(data.split("(\\s)*,(\\s)*")));
		        break;
		        
			case "external-sources":
				dObj.setSource(data);			
		        break;
		}  
	}
    
    /**
     * Creates the attributes for the current ScannedDataObject
     */
    private List<ScannedAttribute> createAttributes() {
		    			
        List<ScannedAttribute> attributes = new ArrayList<>();
        ScannedAttribute attr = null;
    	
    	// Character data buffer 
		StringBuilder sb = new StringBuilder();

		/*
		 * Character events can be thrown for chunks of data within an element. We need to capture all the chunks and
		 * combine them. This flag is used to buffer the data chunks while an element is being processed. We set this
		 * flag to true upon element start and set it to false after element end. We do not need to match start and end
		 * elements because elements can cannot contain data and sub elements. Elements with character data start and
		 * end immediately and we are only concerned with these for character data buffering.
		 */
		boolean processing = false;
		
		/*
		 * Elements that have data in attributes are processed under the START_ELEMENT event. Elements that have
		 * character data are processed upon END_ELEMENT event after the contents have been gathered.
		 */
        try {
        	
			while(reader.hasNext()) {
				
				XMLEvent event = reader.nextEvent();
				
				switch (event.getEventType()) {
					
					case XMLEvent.START_ELEMENT:
						StartElement elStart = event.asStartElement();
						String elStartName = elStart.getName().toString();
						switch (elStartName) {
							case "attribute": 
								// If <attribute> element start is detected begin Attribute processing
								attr = new ScannedAttribute();		
								break;
								
							case "local-source":
								attr.getLocalSources().add(extractAttributeSource(elStart));
								break;
								
							case "property":
								addExtendedProperty(attr, elStart);
								break;
								
							case "code":
								addAttributeCode(attr, elStart);
								break;
						}	
						sb.delete(0, sb.length());
						processing = true;						
						break;
						
					case XMLEvent.END_ELEMENT: 
						EndElement elEnd = event.asEndElement();
						String elEndName = elEnd.getName().toString();
						switch (elEndName) {
							
							case "attributes":
								return attributes;
								
							case "attribute":
								attributes.add(attr);
								break;
								
							default:
								processAttributeElementEnd(elEndName, attr, sb.toString());
								break;
						}
						processing = false;
						sb.delete(0, sb.length());
						break;
						
					case XMLEvent.CHARACTERS:
						if (processing) {
							sb.append(event.asCharacters().getData());
						}
						break;
				}
			}
		} catch (XMLStreamException e) {
			log.error(e.getMessage(), e);
			throwImportException(e);
		}         
        return attributes;
	}

	/**
	 * Extracts an attribute source from a "local source" element representing an attribute
	 * 
	 * @param elStart
	 *            start element
	 *            
	 * @return attribute source represented by the element.
	 */
	private ScannedAttributeSource extractAttributeSource(StartElement elStart) {
		String namespace = elStart.getAttributeByName(QName.valueOf("namespace")).getValue();
		String oName = elStart.getAttributeByName(QName.valueOf("object-name")).getValue();	
		String aName = elStart.getAttributeByName(QName.valueOf("attribute-name")).getValue();		
		ScannedAttributeSource source = new ScannedAttributeSource();
		source.setNamespace(namespace);
		source.setObjectName(oName);
		source.setAttributeName(aName);
		return source;
	}
	
	/**
	 * Extracts the name and value of an extended property from the "extended property" element and adds it to the
	 * attribute.
	 * 
	 * @param attr
	 *            attribute
	 * @param elStart
	 *            extended property start element
	 */
	private void addExtendedProperty(ScannedAttribute attr, StartElement elStart) {
		String extnProperty = elStart.getAttributeByName(QName.valueOf("name")).getValue();
		String value = elStart.getAttributeByName(QName.valueOf("value")).getValue();
		attr.getExtendedProperties().put(extnProperty, value);		
	}
	
	/**
	 * Extracts the value and description of an attribute property from the "code" element and adds it to the
	 * attribute's list of codes.
	 * 
	 * @param attr
	 *            attribute
	 * @param elStart
	 *            extended property start element
	 */
	private void addAttributeCode(ScannedAttribute attr, StartElement elStart) {
		String codeValue = elStart.getAttributeByName(QName.valueOf("value")).getValue();
		String valueDesc = elStart.getAttributeByName(QName.valueOf("description")).getValue();
		attr.getCodes().add(new ScannedAttributeCode(codeValue, valueDesc));
	}

	/**
	 * Performs processing upon encountering element end event.
	 * 
	 * @param elName
	 *            element name
	 * @param attr
	 *            reference to the attribute
	 * @param data
	 *            the character data of the element
	 */
	private void processAttributeElementEnd(String elName, ScannedAttribute attr, String data) {
				
		switch (elName) {
			
			case "name":
				attr.setName(data);
				break;
			
			case "logical-name":
				attr.setLogicalName(data);
				break;
				
			case "position":
				int position = 0;
				try {
					position = Integer.parseInt(data);
				} catch (NumberFormatException ex) {
					log.error(ex.getMessage(), ex);
				}
				attr.setSeqNo(position);
				break;
							
			case "data-type":
				attr.setDataType(data);
				break;
				
			case "common-type":
				attr.setCommonType(data);
				break;
				
			case "key":
				attr.setKey(data.equalsIgnoreCase("Y"));
				break;
			
			case "required":
				attr.setRequired(data.equalsIgnoreCase("Y"));
				break;
			
			case "parent-attribute":
				attr.setParentAttribute(data);
				break;
				
			case "default-value":
				attr.setDefaultValue(data);
				break;
				
			case "external-sources":
				attr.setSource(data);
				break;
				
			case "summary":
				attr.setSummary(data);
				break;
				
			case "description":
				attr.setDescription(data);
				break;
				
			case "tags":
				attr.setTags(Arrays.asList(data.split("(\\s)*,(\\s)*")));
				break;
		}
	}

	/**
	 * Returns the case mode configured for this data object reader.
	 * 
	 * @return reader's case mode.
	 */
	public CaseMode getCaseMode() {
		return caseMode;
	}
	
	/**
	 * Returns the file path for the XML file configured for this reader.
	 * 
	 * @return XML file.
	 */
	public String getXMLFile() {
		return xmlFile;
	}
 }