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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public class RuleMapTest {

	/*
	 * Test addRule()
	 * 
	 * This method is indirectly tested while testing the fetcher methods.   
	 */
	
	private void addRules(RuleMap rules) {
		rules.addRule("N11", "R11");
		rules.addRule("N12", "R12");			
	}
	
	/*
	 * Test getRule()
	 */
	@Test 
	public void getRule() {	
		RuleMap rules = new RuleMap();
		addRules(rules);
		assertEquals("R11", rules.getRule("N11"));
	}
	
	@Test 
	public void getRule_For_NonExistent_Rule() {	
		RuleMap rules = new RuleMap();
		addRules(rules);
		assertNull(rules.getRule("N100"));
	}
	
	/*
	 * Test size()
	 */
	@Test 
	public void size() {	
		RuleMap rules = new RuleMap();
		addRules(rules);
		assertEquals(2, rules.size());
	}
	
	@Test 
	public void size_For_Empty_RuleMap() {	
		RuleMap rules = new RuleMap();
		assertEquals(0, rules.size());
	}
}