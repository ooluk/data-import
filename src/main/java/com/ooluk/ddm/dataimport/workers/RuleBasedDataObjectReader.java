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
 * RuleBasedDataObjectReader adds rules facility support to the DataObjectReader interface. It also adds a namespace
 * prefix to support namespace rules.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public interface RuleBasedDataObjectReader {

	/**
	 * Gets the namespace prefix for this data object reader.
	 * 
	 * @return the namespace prefix for this data object reader.
	 */
	public abstract String getNamespacePrefix();

	/**
	 * Sets the namespace prefix for this data object reader.
	 * 
	 * @param namespacePrefix
	 *            the target namespace prefix for this data object reader
	 */
	public abstract void setNamespacePrefix(String namespacePrefix);

	/**
	 * Gets the rule group for this data object reader.
	 * 
	 * @return rule group for this data object reader.
	 */
	public abstract String getRuleGroup();

	/**
	 * Sets the rule group for this data object reader.
	 * 
	 * @param ruleGroup
	 *            rule group name
	 */
	public abstract void setRuleGroup(String ruleGroup);

	/**
	 * Gets the rule store for this data object reader.
	 * 
	 * @return the rule store for this data object reader.
	 */
	public abstract RuleStore getRuleStore();

	/**
	 * Sets the rules store for this data object reader.
	 * 
	 * @param ruleStore
	 *            the rule store for this data object reader
	 */
	public abstract void setRuleStore(RuleStore ruleStore);
}