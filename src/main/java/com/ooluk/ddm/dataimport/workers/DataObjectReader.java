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

import com.ooluk.ddm.dataimport.ImportException;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;

/**
 * <p>
 * A DataObjectReader reads data object metadata from a metadata source. The specifics of connecting to a metadata
 * source and reading the metadata are left to concrete implementations.
 * </p>
 *  
 * <h6>Data Object Reader Life Cycle</h6>
 * 
 * <pre>            
 *     init()
 *       |
 *     read() <----------------------+ 
 *       |                           |
 *  ( EndOfData? ) --> "N" --> << process >>
 *       |
 *      "Y"
 *       |
 *    close()
 * </pre>
 * 
 * <p>
 * End of Data is indicated by read() returning {@code null}.
 * </p>
 * 
 * <h6>Import Status</h6>
 * <p>
 * The data object reader accepts a {@link java.io.Writer} as a status log. All status messages and written to the
 * writer.
 * </p>
 * 
 * <h6>Type Metadata</h6>
 * <p>
 * The data object reader also provides an interface to retrieve type metadata. To fetch type metadata a user must first
 * call enableForTypeMode() and then call getTypeMetaData(). See the source for
 * {@link com.ooluk.ddm.dataimport.workers.jdbc.JDBCDataObjectReader} for a better understanding of
 * these methods.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * @see TypeMetaData
 * 
 */
public interface DataObjectReader extends ImportWorker {
    
	/**
	 * Reads a data object from the data source.
	 * 
	 * @return the data object read
	 * 
	 * @throws ImportException
	 *             if any exceptional conditions or checked exceptions arise during processing
	 */
	public ScannedDataObject read();

	/**
	 * Enables the reader for type metadata retrieval
	 */
	public void enableForTypeMode();

	/**
	 * Determines if the reader is enabled for type mode.
	 * 
	 * @return true if enabled for type mode, false otherwise
	 */
	public boolean isEnabledForTypeMode();

	/**
	 * Returns the type metadata for each column. This method call must be preceded with a call to read().
	 */
	public TypeMetaData getTypeMetaData();
}