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
import com.ooluk.ddm.dataimport.MessageKey;
import com.ooluk.ddm.dataimport.Messages;

/**
 * An abstract implementation of DataObjectReader to provide common functionality. 
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public abstract class AbstractDataObjectReader implements DataObjectReader {
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private final Logger log = LogManager.getLogger();
    
	// Reader name
	private String name;
	
	// Reference to the log status writer
	private Writer writer;
	
	// Flag for type mode
    private boolean isTypeMode = false; 
    
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
	 * Appends a new status message to the status log. Status messages are not created for TypeMode. The method silently
	 * ignores status messages for TypeMode.
	 * 
	 * @param msg
	 *            the status line to append
	 */
    protected void appendStatusLine(String msg) {
    	if (!isTypeMode) {
	        try {
	        	writer.write(msg + NEW_LINE);
	        	writer.flush();
			} catch (IOException e) {
				log.error(msg, e);
				throw new ImportException(e);
			}
    	}
    }
	
	/**
	 * This logic is wrapped in a separate method to ensure we always close resources on an exception. This method also
	 * writes the exception message, if an exception is present, to the status log.
	 * 
	 * @param msg
	 *            message
	 * @param cause
	 *            exception cause
	 */
	protected void throwImportException(String msg, Exception cause) {
		this.close();
		if (cause != null)
        	appendStatusLine(cause.getMessage());
		
		if (msg != null && cause != null) { 
			throw new ImportException(msg, cause);
		} else if (cause != null) {
			throw new ImportException(cause);
		} else if (msg != null) { 
			throw new ImportException(msg);
		}
	}
	
	protected void throwImportException(Exception cause) {
		throwImportException(null, cause);
	}
	
	protected void throwImportException(String msg) {
		throwImportException(msg, null);
	}
    
    @Override
	public void enableForTypeMode() {
    	isTypeMode = true;
    }
    
    @Override
    public boolean isEnabledForTypeMode() {
    	return isTypeMode;
    }
    
    @Override
	public TypeMetaData getTypeMetaData() {
    	throw new ImportException(Messages.getMessage(MessageKey.IMP_TYPE_INFO_UNSUPPORTED));    
    }
}