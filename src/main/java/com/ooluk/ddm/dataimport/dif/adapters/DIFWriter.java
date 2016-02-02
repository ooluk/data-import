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
package com.ooluk.ddm.dataimport.dif.adapters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.workers.xml.XMLDataObjectReader;

/**
 * This class creates the DIF (Data Import Format) XML document for DDM data import. The output of this writer can
 * be fed to the XMLDataObjectReader.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see XMLDataObjectReader
 * 
 */
public class DIFWriter {

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private XMLStreamWriter xsw;
	private boolean initialized = false;
	private boolean namespaceSet = false;
	
	/**
	 * Constructs a DIFWriter
	 */
	public DIFWriter() {
		try {
			jaxbContext = JAXBContext.newInstance(ScannedDataObject.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the DIF writer to write the XML to the specified file.
	 * 
	 * @param fileName
	 *            the file to use to output the XML
	 */
	public void init(String fileName) {
		Path file = Paths.get(fileName);
		try {
			BufferedWriter out = Files.newBufferedWriter(file, Charset.forName("UTF-8"));
			XMLOutputFactory xof = XMLOutputFactory.newFactory();
			xsw = xof.createXMLStreamWriter(out);
			xsw.writeStartElement("ddm");
		} catch(IOException | XMLStreamException e) {
			throw new RuntimeException(e);
		}
		initialized = true;
	}
	
	/**
	 * Closes the DIF writer.
	 */
	public void close() {
		try {
			xsw.writeEndDocument();
			xsw.close();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		initialized = false;
	}

	/**
	 * Creates a new namespace element. All data objects within this namespace element will be assigned this namespace.
	 * 
	 * @param namespace
	 *            namespace name
	 */
	public void beginNamespace(String namespace) {
		if (!initialized) {
			throw new RuntimeException("Please call init() before calling beginNamespace()");
		}
		try {
			xsw.writeStartElement("namespace");
			xsw.writeAttribute("name", namespace);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		namespaceSet = true;
	}
	
	/**
	 * Ends the namespace element.
	 */
	public void endNamespace() {
		try {
			xsw.writeEndElement();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		namespaceSet = false;
	}

	/**
	 * Creates a data object element within the current namespace. The current namespace is the namespace that has been
	 * last passed to beginNamespace()
	 * 
	 * @param obj
	 *            data object
	 */
	public void writeDataObject(ScannedDataObject obj) {
		if (!namespaceSet) {
			throw new RuntimeException("Please call beginNamespace() before calling writeDataObject()");
		}
		try {
			marshaller.marshal(obj, xsw);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}	
}