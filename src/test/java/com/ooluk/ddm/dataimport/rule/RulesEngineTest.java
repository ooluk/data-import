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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public class RulesEngineTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	/*
	 * Test isValid()
	 */
	
	@Test
	public void isValid_For_Beginning_With_Operator() {
		assertFalse(RulesEngine.isValid("+"));
		assertFalse(RulesEngine.isValid("-"));
		assertFalse(RulesEngine.isValid("+1"));
		assertFalse(RulesEngine.isValid("-1"));
	}
	
	@Test
	public void isValid_For_Terminating_With_Operator() {
		assertFalse(RulesEngine.isValid("1+"));
		assertFalse(RulesEngine.isValid("1-"));
	}
	
	@Test
	public void isValid_For_Valid_Expression_With_Single_Digit_Numbers() {
		assertTrue(RulesEngine.isValid("1+1"));
		assertTrue(RulesEngine.isValid("1-1"));
		assertTrue(RulesEngine.isValid("1+1-1"));
		assertTrue(RulesEngine.isValid("1-1+1"));
	}
	
	@Test
	public void isValid_For_Valid_Expression_With_Multiple_Digit_Numbers() {
		assertTrue(RulesEngine.isValid("10+10"));
		assertTrue(RulesEngine.isValid("10-10"));
		assertTrue(RulesEngine.isValid("10+10-10"));
		assertTrue(RulesEngine.isValid("10-10+10"));
	}
	
	@Test
	public void isValid_For_Valid_Expression_With_Invalid_Operators() {
		assertFalse(RulesEngine.isValid("1*1"));
		assertFalse(RulesEngine.isValid("1+1-1/1"));
	}
	
	/*
	 * Test evaluateExpression()
	 */	
	@Test
	public void evaluateExpression_For_Single_Addition() {
		String result = RulesEngine.evaluateExpression("30+20");
		assertEquals("50", result);
	}
	
	@Test
	public void evaluateExpression_For_Single_Subtraction() {
		String result = RulesEngine.evaluateExpression("30-20");
		assertEquals("10", result);
	}
	
	@Test
	public void evaluateExpression_For_Multiple_Operations() {
		String result = RulesEngine.evaluateExpression("30+20-15");
		assertEquals("35", result);
	}
	
	/**
	 * A single invalid expression is sufficient as different cases are tested in isValid() 
	 */
	@Test
	public void evaluateExpression_For_Invalid_Expression() {
		String expr = "30+20*15";
		exception.expect(RuntimeException.class);
		exception.expectMessage(equalTo("Invalid expression \"" + expr + "\""));
		RulesEngine.evaluateExpression(expr);
	}

	/*
	 * Test processRule()
	 */	
	@Test
	public void processRule_For_No_Expressions() {
		String rule = "ASIS(1,1)";
		String eval = RulesEngine.processRule(rule);
		assertEquals(rule, eval);
	}
	
	@Test
	public void processRule_For_Beginning_With_Expression() {
		String rule = "[!3+5-2!]_END";
		String eval = RulesEngine.processRule(rule);
		assertEquals("6_END", eval);
	}
	
	@Test
	public void processRule_For_Ending_With_Expression() {
		String rule = "BEGIN_[!3+5-2!]";
		String eval = RulesEngine.processRule(rule);
		assertEquals("BEGIN_6", eval);
	}
	
	@Test
	public void processRule_For_Containing_Expression() {
		String rule = "BEGIN_[!3+5-2!]_END";
		String eval = RulesEngine.processRule(rule);
		assertEquals("BEGIN_6_END", eval);
	}
	
	@Test
	public void evaluateExpression_For_Invalid_Rule() {
		String rule = "BEGIN_[!3+5-2!_END";
		exception.expect(RuntimeException.class);
		exception.expectMessage(equalTo("Invalid rule \"" + rule + "\""));
		RulesEngine.processRule(rule);
	}
}