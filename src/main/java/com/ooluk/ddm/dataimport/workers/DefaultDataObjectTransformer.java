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

import java.util.Map;

import com.ooluk.ddm.dataimport.data.ScannedDataObject;

/**
 * DefaultDataObjectTransformer defines the transform method as a no operation method. This class is to be used when no
 * transformation is required.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public class DefaultDataObjectTransformer extends AbstractDataObjectTransformer {
        
    @Override
    public void init() {        
    }
    
    @Override
    public void init(Map<String, Object> params) {        
    }
    
    @Override
    public void transform(ScannedDataObject dObj) {	    
    }

    @Override
    public void close() {	       
    }
}