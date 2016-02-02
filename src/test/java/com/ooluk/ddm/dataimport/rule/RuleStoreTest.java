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
public class RuleStoreTest {

	/*
	 * Test addRule()
	 * 
	 * This method is indirectly tested while testing the fetcher methods.   
	 */
	
	private void addRulesToStore(RuleStore store) {
		store.addRule("C1", "N11", "R11");
		store.addRule("C1", "N12", "R12");
		store.addRule("C2", "N21", "R21");
		store.addRule("C2", "N22", "R22");			
	}
	
	/*
	 * Test getRulesByCategory()
	 */
	@Test 
	public void getRulesByCategory() {	
		RuleStore store = new RuleStore();
		addRulesToStore(store);
		RuleMap rules = store.getRulesByCategory("C1");
		assertEquals(2, rules.size());
		assertEquals("R11", rules.getRule("N11"));
		assertEquals("R12", rules.getRule("N12"));
	}
	
	@Test 
	public void getRulesByCategory_For_NonExistent_Category() {	
		RuleStore store = new RuleStore();
		addRulesToStore(store);
		RuleMap rules = store.getRulesByCategory("C100");
		assertNull(rules);
	}
	
	/*
	 * Test getRule()
	 */
	@Test 
	public void getRule() {	
		RuleStore store = new RuleStore();
		addRulesToStore(store);
		assertEquals("R11", store.getRule("C1", "N11"));
	}
	
	@Test 
	public void getRule_For_NonExistent_Category() {	
		RuleStore store = new RuleStore();
		addRulesToStore(store);
		assertNull(store.getRule("C100", "N11"));
	}
	
	@Test 
	public void getRule_For_NonExistent_Rule() {	
		RuleStore store = new RuleStore();
		addRulesToStore(store);
		assertNull(store.getRule("C1", "N100"));
	}
}