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

import com.ooluk.ddm.dataimport.rule.RuleStore;

/**
 * An abstract implementation of RuleBasedDataObjectReader to provide common functionality. 
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public abstract class AbstractRuleBasedDataObjectReader 
	extends AbstractDataObjectReader 
	implements RuleBasedDataObjectReader {
        
    // The namespace prefix for this data object reader.
    protected String namespacePrefix;
    
    // The rule group this reader uses
    private String ruleGroup;
    
    // The rule store for this data object reader
    protected RuleStore ruleStore;
    	
	@Override
    public String getNamespacePrefix() {
		return namespacePrefix;
	}

	@Override
    public void setNamespacePrefix(String namespacePrefix) {
		this.namespacePrefix = namespacePrefix;
	}

	@Override
    public String getRuleGroup() {
		return ruleGroup;
	}

	@Override
    public void setRuleGroup(String ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	@Override
    public RuleStore getRuleStore() {
		return ruleStore;
	}

	@Override
    public void setRuleStore(RuleStore ruleStore) {
		this.ruleStore = ruleStore;
	}
}