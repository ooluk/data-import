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

import org.junit.Test;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public class DIFWriterTest {
	
    /**
	 * <pre>
	 * ----------------------------------
	 * Case: [1] 
	 * ----------------------------------
	 * Test 
	 * 
	 * ----------------------------------
	 * Action & Expectation
	 * ----------------------------------
	 * 
	 * extendedProperties
	 * </pre>
	 */  
	@Test
	public void testDDIFWriter() {
		DummyMetadataAdapter adapter = new DummyMetadataAdapter("D:/ddm/output/test.xml");
		adapter.extractAndWriteMetadata();
	}
}