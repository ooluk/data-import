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
 * A DataObjectWriter writes the properties of a ScannedDataObject. The specifics of where and how the data is written
 * is left to concrete implementations.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public interface DataObjectWriter extends ImportWorker {

	/**
	 * Writes a single ScannedDataObject.
	 * 
	 * @param dObj
	 *            the ScannedDataObject to be written
	 * 
	 * @throws ImportException
	 *             in case of any handled exceptions
	 */
	public void write(ScannedDataObject dObj);
}