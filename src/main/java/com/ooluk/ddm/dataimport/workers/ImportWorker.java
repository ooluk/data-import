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

import java.io.Writer;
import java.util.Map;

import com.ooluk.ddm.dataimport.ImportException;

/**
 * An import worker performs a certain task during import processing. As of this version there are three types of import
 * workers. 
 * 
 * <ul>
 * <li>Data Object Readers
 * <li>Data Object Transformers
 * <li>Data Object Writers
 * </ul>
 * 
 * <p>
 * A import worker is optionally identified by a name. The name feature is provided in case a user wants to identity a
 * worker in logs, console messages or elsewhere.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public interface ImportWorker {

	/**
	 * Gets the name of the worker.
	 * 
	 * @return the name of the worker.
	 */
	public String getName();

	/**
	 * Sets the name of the worker.
	 * 
	 * @param name
	 *            name of the worker
	 */
	public void setName(String name);

	/**
	 * Initializes the worker. It is unlikely a user would be directly calling this method. In most cases a user would
	 * call the parameterized init().
	 * 
	 * @throws ImportException
	 *             if any exceptional conditions or checked exceptions arise during processing
	 */
	public void init();

	/**
	 * Initializes the worker. This method accepts initialization parameters as a property-value map.
	 * 
	 * @param params
	 *            parameters required to initialize the worker. Each map element key must correspond to a parameter
	 *            name and the element value must correspond to the corresponding parameter value.
	 * 
	 * @throws ImportException
	 *            if any exceptional conditions or checked exceptions arise during processing
	 */
	public void init(Map<String, Object> params);
	
	/**
	 * Sets the log writer for the worker.
	 * 
	 * @param writer
	 *            log writer
	 */
	public void setLogWriter(Writer writer);
	
	/**
	 * Performs closing actions on the worker.
	 * 
	 * @throws ImportException
	 *             if any exceptional conditions or checked exceptions arise during processing
	 */
	public void close();
}