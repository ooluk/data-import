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
package com.ooluk.ddm.dataimport;

/**
 * Enum of letter cases.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public enum CaseMode {
	
	MIXED, UPPER, LOWER;
	
	/**
	 * Converts the argument string to the case represented by the Enum constant on which this method is called.
	 * 
	 * @param string
	 *            the input string.
	 * 
	 * @return the input string converted to the appropriate case.
	 */
	public String convert(String string) {
		switch (this) {
			case UPPER:
				return string.toUpperCase();
			case LOWER:
				return string.toLowerCase();
			default:
				return string;
		}
	}
}