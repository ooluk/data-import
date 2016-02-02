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
package com.ooluk.ddm.dataimport.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * A rule store is a collection of rules belonging to a particular group. Since a rule map stores all rules for a given
 * category within a group and a group comprises of multiple categories a rule store is represented as a mapping between
 * categories and their corresponding rule maps.
 * 
 * <p>
 * Example: Data type, common type and namespace rules for the SQL group
 * </p>
 * 
 * <p>
 * <pre>
 * {@code
 * -------------------------------------------------------------
 * Group   Category        Name        Rule Specification
 * -------------------------------------------------------------
 * "SQL"   "data-type"    "DECIMAL"    "%type%(%size%,%scale%)"
 * "SQL"   "data-type"    "VARCHAR"    "%type%(%size%)"
 * "SQL"   "common-type"  "DECIMAL"    "%type%([!%size%-%scale!],%scale%"
 * "SQL"   "common-type"  "VARCHAR"    "CHAR(%size%)"
 * "SQL"   "namespace"    "NSPACE"     "%prefix%.%schema%"
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * This class is not synchronized.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public class RuleStore {
    
    private final Map<String, RuleMap> rules;
    
    public RuleStore() {
        rules = new HashMap<>();
	}

	/**
	 * Adds a rule to the rule store. 
	 * 
	 * @param category
	 *            rule category
	 * @param name
	 *            rule name
	 * @param rule
	 *            rule specification
	 */
	public void addRule(String category, String name, String rule) {
		
		RuleMap rulesMap = null;
		if (rules.containsKey(category)) {
			rulesMap = rules.get(category);
		} else {
			rulesMap = new RuleMap();
			rules.put(category, rulesMap);
		}
		rulesMap.addRule(name, rule);
	}

	/**
	 * Returns all rules for a given category.
	 * 
	 * @param category
	 *            rule category
	 *            
	 * @return all rules for the specified category.
	 */
	public RuleMap getRulesByCategory(String category) {
		return rules.get(category);
	}

	/**
	 * Returns the rule specification for the specified category and name.
	 * 
	 * @param category
	 *            rule category
	 * @param name
	 *            rule name
	 *            
	 * @return the rule specification.
	 */
	public String getRule(String category, String name) {
		if (rules.containsKey(category)) {
			return rules.get(category).getRule(name);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return rules.toString();
	}
}