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
 * A rule map stores rules of a particular group and category. Within a rule map a rule is identified by its name.
 * 
 * <p>
 * Example: Data type (category="data-type") rules for the SQL (group="SQL") group.
 * </p>
 * 
 * <p>
 * <pre>
 * {@code
 * -------------------------------------------------------------
 * Group   Category      Name       Rule Specification
 * -------------------------------------------------------------
 * "SQL"   "data-type"   "DECIMAL"  "%type%(%size%,%scale%)"
 * "SQL"   "data-type"   "VARCHAR"  "%type%(%size%)"
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
 * @see RuleStore
 */
public class RuleMap {
    
    private final Map<String, String> ruleMap;
    
    public RuleMap() {
        ruleMap = new HashMap<>();
    }

	/**
	 * Adds a rule to the rule map. If an entry with the name exists it will be overwritten.
	 * 
	 * @param name
	 *            the rule name
	 * @param rule
	 *            the rule specification
	 */
	public void addRule(String name, String rule) {
		ruleMap.put(name, rule);
	}

	/**
	 * Returns the specification for a rule.
	 * 
	 * @param name
	 *            rule name
	 *            
	 * @return the rule specification.
	 */
	public String getRule(String name) {
		return ruleMap.get(name);
	}
	
	/**
	 * Returns the rule map size.
	 * 
	 * @return number of entries in the rule map
	 */
	public int size() {
		return ruleMap.size();
	}

	@Override
	public String toString() {
		return ruleMap.toString();
	}
}