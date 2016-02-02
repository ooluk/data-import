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
 * DataObjectTransformer transforms a data object read by a DataObjectReader.
 *   
 * <h6>Data Object Reader Life Cycle</h6>
 * 
 * <pre>            
 *     init()
 *       |
 *   transform() <------+ 
 *       |              |
 *  ( EndOfData? ) --> "N"
 *       |
 *      "Y"
 *       |
 *    close()
 * </pre>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public interface DataObjectTransformer extends ImportWorker {

	/**
	 * Transforms a scanned data object. The transformation must be performed on the object not on a copy.
	 * 
	 * @param dObj
	 *            the data object to be transformed
	 * 
	 * @throws ImportException
	 *             if any exceptional conditions or checked exceptions arise during processing
	 */
	public void transform(ScannedDataObject dObj);
}