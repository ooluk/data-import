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

import java.io.IOException;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ooluk.ddm.dataimport.ImportException;

/**
 * An abstract implementation of DataObjectWriter to provide common functionality. 
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public abstract class AbstractDataObjectWriter implements DataObjectWriter {

	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private final Logger log = LogManager.getLogger();
    
	// Writer name
	private String name;
	
	// Reference to the log status writer
	private Writer writer;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
        
    @Override
	public void setLogWriter(Writer writer) {
    	this.writer = writer;
    }

	/**
	 * Appends a new status message to the log.
	 * 
	 * @param msg
	 *            status message
	 */
	protected void appendStatusLine(String msg) {
		try {
			writer.write(msg + NEW_LINE);
			writer.flush();
		} catch (IOException e) {
			log.error(msg, e);
			throw new ImportException(e);
		}
	}
}