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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A log4j wrapper to provide application specific logging.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public class DataImportLog {
	
	/**
	 * Creates a custom log record.
	 * 
	 * @param method
	 *            method name
	 * @param message
	 *            the message to write to the log
	 * @param methodParams
	 *            method parameters
	 *            
	 * @return a string representing the log record
	 */
	private static String getLogRecord(String method, String message, Object... methodParams) {
		List<Object> params = methodParams == null ? Collections.emptyList() : Arrays.asList(methodParams);
		String logMessage = "Data Import > "
				+ "method:[" + method + "] "
				+ "message:[" + message + "] "
				+ (params.size() > 0 ? "params:" + params : "");
		return logMessage;
	}
	
	/**
	 * Writes a informational log record.
	 * 
	 * @param message
	 *            log message
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */
	public static void info(String message, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.info(getLogRecord(method, message, methodParams));
	}
	
	/**
	 * Writes a trace log record.
	 * 
	 * @param message
	 *            log message
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */
	public static void trace(String message, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.trace(getLogRecord(method, message, methodParams));
	}
	
	/**
	 * Writes a debug log record.
	 * 
	 * @param message
	 *            log message
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */
	public static void debug(String message, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.debug(getLogRecord(method, message, methodParams));
	}
	
	/**
	 * Writes a warning log record.
	 * 
	 * @param message
	 *            log message
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */	
	public static void warn(String message, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.warn(getLogRecord( method, message, methodParams));
	}
	
	/**
	 * Writes an error log record.
	 * 
	 * @param message
	 *            log message
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */
	public static void error(String message, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.error(getLogRecord( method, message, methodParams));
	}
	
	/**
	 * Writes an error log record with an exception/error reference.
	 * 
	 * @param ex
	 *            reference to the Throwable
	 * @param caller
	 *            reference to the class calling this method
	 * @param method
	 *            calling method's name
	 * @param methodParams
	 *            the parameters of the calling method
	 */
	public static void error(Throwable ex, Object caller, String method, Object... methodParams) {
		Logger log = LogManager.getLogger(caller.getClass().getName());
		log.fatal(getLogRecord( method, "", methodParams), ex);
	}
}