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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * The rules engine processes a rule by evaluating the embedded expressions. Expressions are specified between [! and
 * !]. The rules engine is currently designed to handle arithmetic expressions with + (addition) and - (subtraction)
 * only.
 * 
 * <p>
 * Example: DECIMAL([!10+4!],[!10-4!]) = DECIMAL(14,6)
 * </p>
 * 
 * <p>
 * This class is designed with static only methods as it maintains no state. Therefore it is designed to be uninstantiable.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public final class RulesEngine {

	private static final String EXPR_BEGIN = "[!";
	private static final String EXPR_END = "!]";	
	
	/**
	 * Make the class uninstantiable.
	 */
	private RulesEngine() {		
	}

	/**
	 * Evaluates the expressions in the rule.
	 * 
	 * @param rule
	 *            the rule
	 *            
	 * @return rule with expressions evaluated.
	 */
    public static String processRule(String rule) {
    	// If there are no expressions no evaluation is required
    	if (!rule.contains(EXPR_BEGIN)) {
    		return rule;
    	}
    	
    	// Replace each expression in a rule with its evaluation. 
    	StringBuilder sb = new StringBuilder();
    	int fromIndex = 0;
    	while (fromIndex < rule.length()) {
	    	int start = rule.indexOf(EXPR_BEGIN, fromIndex);
	    	if (start == -1) {
	    		break;
	    	}
			/*
			 * If we have reached here, an expression beginning has been detected. Without an expression end the rule is
			 * invalid.
			 */		
	    	int end = rule.indexOf(EXPR_END, fromIndex);	 
	    	if (end == -1) {
	    		throw new RuntimeException(MessageFormat.format("Invalid rule \"{0}\"", rule));
	    	}
	    	String expr = rule.substring(start+2, end);
	    	String evalExpr = evaluateExpression(expr);
	    	sb.append(rule.substring(fromIndex, start));
	    	sb.append(evalExpr);
	    	fromIndex = end+2;
    	}
    	sb.append(rule.substring(fromIndex));
    	return sb.toString();
	}

	/**
	 * Evaluates the specified expression. 
	 * 
	 * @param expr
	 *            the expression to evaluate
	 * 
	 * @return the result of the evaluation.
	 */
    public static String evaluateExpression(String expr) {
    	
    	if (!isValid(expr)) {
    		throw new RuntimeException(MessageFormat.format("Invalid expression \"{0}\"", expr));
    	}
    	
    	LinkedList<String> numbers = new LinkedList<>(Arrays.asList(expr.split("\\+|-")));
    	int result = Integer.parseInt(numbers.poll().trim());
    	for (int i = 0; i < expr.length(); i++) {
    		char ch = expr.charAt(i);
    		if (ch == '+' || ch == '-') {
				switch (ch) {
					case '+':
						result += Integer.parseInt(numbers.poll());
						break;
					case '-':
						result -= Integer.parseInt(numbers.poll());
						break;
				}
    		}
    	}
    	return String.valueOf(result);
    }
    
	/**
	 * 
	 * Validates an expression. A valid expression (expr) is
	 * 
	 * {@code 
	 * expr ::= integer | expr operator expr
	 * integer ::= digit | integer digit
	 * digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
	 * operator = '+' | '-'
	 * }
	 * 
	 * Using regular expressions this can be expressed as
	 * 
	 * (one or more digits) followed by zero or more [ ('+' or '-') and (one or more digits) ]
	 * 
	 * @param expr
	 *            expression to validate
	 * 
	 * @return true if the expression is valid, false otherwise.
	 */
    public static boolean isValid(String expr) {
    	return expr.matches("(\\d)+((\\+|-)(\\d)+)*");
    }
}