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

/**
 * Interface for metadata adapters. Metadata adapters extract metadata from metadata stores and create a DIF (Data
 * Import Format) file that can be fed to the XMLDataObjectReader.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public interface MetadataAdapter {

	/**
	 * Extracts metadata for the configured source and writes it to the target XML file.
	 */
	public void extractAndWriteMetadata();
}