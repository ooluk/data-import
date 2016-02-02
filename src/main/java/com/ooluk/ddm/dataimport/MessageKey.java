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
 * This enum stores the keys for the resource bundle that supports internationalization of server messages. 
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see Messages
 * 
 */
public enum MessageKey {
	
	/*
	 * Import related
	*/	
	IMP_READER_MISSING,
	IMP_TRANSFORMER_MISSING,
	IMP_WRITER_MISSING,
	IMP_WORKERS_NOT_FOUND,
	IMP_WORKER_NOT_FOUND,
	IMP_PARAM_NOT_FOUND,
	IMP_PARAM_VALUE_NOT_FOUND,
	IMP_TYPE_INFO_UNSUPPORTED,
	IMP_LOG_READ_ERR,
	IMP_LOG_DEL_ERR,
		
	/*
	 * DataObjectReader
	 */
	READ_NOT_CONFIG,
	WORKER_PARAM_MISSING,
	WORKER_PARAM_INVALID,
	WORKER_PARAM_EMPTY,
	READ_IMPORTING,	
	
	/*
	 * JDBC
	 */
	JDBC_TYPE_MODE_INVALID,
	JDBC_READ_EXCP,
	
	/*
	 * COBOL
	 */
	COB_TYPE_MODE_INVALID,
	COB_DIR_NOT_PRESENT,
	COB_NOT_A_DIR,
	COB_COPY_NOT_PRESENT,
	COB_COPY_NOT_FILE,
	COB_READER_NOT_INIT,
	
	/*
	 * XML
	 */
	XML_FILE_OPEN_ERR,
}