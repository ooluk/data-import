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
package com.ooluk.ddm.dataimport.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This class is a XmlJavaTypeAdapter to convert tags list to a single element with comma separated values.
 * 
 * Tags tag1, tag2, tag3 will be represented as &lt;tags&gt;tag1, tag2, tag3&lt;/tags&gt;
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class TagsListAdapter extends XmlAdapter<String, List<String>> {
	
    public TagsListAdapter() {}

    @Override
	public String marshal(List<String> tags) throws Exception {
    	StringBuilder sb = new StringBuilder();
    	for(String tag : tags) {
    		sb.append(tag+",");
    	}
    	String tagString = sb.toString();
    	return sb.toString().substring(0, tagString.length()-1);
    }

    @Override
	public List<String> unmarshal(String tagsString) throws Exception {
        String[] tagsArray = tagsString.split("(\\s)*,(\\s)*");
        return new ArrayList<>(Arrays.asList(tagsArray));
    }
}