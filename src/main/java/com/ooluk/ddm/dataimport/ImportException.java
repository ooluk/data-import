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
package com.ooluk.ddm.dataimport;

/**
 * ImportException denotes an exception that occurs during metadata import processing. ImportException is a not a
 * checked exception (extends RuntimeException). This is because it is unlikely to be able to recover from an
 * ImportException programmatically.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class ImportException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an ImportException with no detail message.
	 */
	public ImportException() {
        super();
    }

	/**
	 * Constructs an ImportException with the specified detail message.
	 * 
	 * @param msg
	 *            the message
	 */
    public ImportException(String msg) {
        super(msg);
    }

	/**
	 * Constructs an ImportException with the specified causal exception.
	 * 
	 * @param cause
	 *            the causal exception
	 */
    public ImportException(Exception cause) {
        super(cause);
    }
    
    /**
     * Constructs an ImportException with the specified detail message and causal exception.
     * 
     * @param msg
	 *            the message
     * @param cause
	 *            the causal exception
     */
    public ImportException(String msg, Exception cause) {
        super(msg, cause);
    }
}