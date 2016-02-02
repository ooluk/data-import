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
package com.ooluk.ddm.dataimport.workers.cobol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Siddhesh
 *
 */
public class COBOLSyntaxTest {
	

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testIsComment() {
		// Less than 7 characters
		assertTrue(COBOLSyntax.isComment("12345*"));
		// '*' in 7th position
		assertTrue(COBOLSyntax.isComment("123456*"));
		// '/' in 7th position
		assertTrue(COBOLSyntax.isComment("123456/"));
		// ' ' in 7th position
		assertFalse(COBOLSyntax.isComment("123456 "));
	}
	
	@Test
	public void testIsContinued() {
		String currline = "000000   01  MAILING-RECORD.                                            ";
		assertFalse(COBOLSyntax.isContinued(currline));
		currline = "000000   01  MAILING-RECORD                                            .";
		assertFalse(COBOLSyntax.isContinued(currline));
		currline = "123456 01 FIELD PIC X(10) VALUE 'HELLO ";
		assertTrue(COBOLSyntax.isContinued(currline));
	}
	
	@Test
	public void testIsContinuation() {
		String nextline = "123456-WORLD'";
		assertTrue(COBOLSyntax.isContinuation(nextline));
		nextline = "123456 WORLD'";
		assertFalse(COBOLSyntax.isContinuation(nextline));
		nextline = "123456";
		assertFalse(COBOLSyntax.isContinuation(nextline));
		nextline = null;
		assertFalse(COBOLSyntax.isContinuation(nextline));
	}
	
	@Test
	public void testExpandDeclaration() {
		String expExp = "99999";
		String actExp = COBOLSyntax.expandDeclaration("9(5)");
		assertEquals(expExp, actExp);
		actExp = COBOLSyntax.expandDeclaration("999(3)");
		assertEquals(expExp, actExp);
		actExp = COBOLSyntax.expandDeclaration("9(3)99");
		assertEquals(expExp, actExp);
		actExp = COBOLSyntax.expandDeclaration("9(3)9(2)");
		assertEquals(expExp, actExp);
	}
	
	@Test
	public void testCompactDeclaration() {
		// All repeating
		String expDecl = "A(2)X(2)Z(2)9(2)X(2)";
		String actDecl = COBOLSyntax.compactDeclaration("AAXXZZ99XX");
		assertEquals(expDecl, actDecl);
		// Beginning non-repeating, end repeating
		expDecl = "BA(2)";
		actDecl = COBOLSyntax.compactDeclaration("BAA");
		assertEquals(expDecl, actDecl);
		// Beginning repeating, end non-repeating
		expDecl = "A(2)B";
		actDecl = COBOLSyntax.compactDeclaration("AAB");
		assertEquals(expDecl, actDecl);
		// Beginning non-repeating, middle repeating, end non-repeating
		expDecl = "BA(2)B";
		actDecl = COBOLSyntax.compactDeclaration("BAAB");
		assertEquals(expDecl, actDecl);
		// All non-repeating
		expDecl = "BAB";
		actDecl = COBOLSyntax.compactDeclaration("BAB");
		assertEquals(expDecl, actDecl);
	}
	
	@Test
	public void testGetLevelNumber() {
		List<String> tokens = Arrays.asList(new String[] {"01", "data-name"});
		assertEquals(1, COBOLSyntax.getLevelNumber(tokens));
	}
	
	@Test
	public void testGetDataName() {
		List<String> tokens = Arrays.asList(new String[] {"01", "data-name", "REDEFINES"});
		assertEquals("data-name", COBOLSyntax.getDataName(tokens));
		tokens = Arrays.asList(new String[] {"01", "REDEFINES", "data-name"});
		assertEquals("", COBOLSyntax.getDataName(tokens));
		tokens = Arrays.asList(new String[] {"01", "FILLER", "PIC"});
		assertEquals("", COBOLSyntax.getDataName(tokens));
	}
	
	@Test
	public void testGetPictureString() {
		List<String> tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)"});
		assertEquals("9(5)", COBOLSyntax.getPictureString(tokens));		
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "IS", "9(5)"});
		assertEquals("9(5)", COBOLSyntax.getPictureString(tokens));
		tokens = Arrays.asList(new String[] {"01", "data-name", "PICTURE", "9(5)"});
		assertEquals("9(5)", COBOLSyntax.getPictureString(tokens));		
		tokens = Arrays.asList(new String[] {"01", "data-name", "PICTURE", "IS", "9(5)"});
		assertEquals("9(5)", COBOLSyntax.getPictureString(tokens));
	}
		
	@Test
	public void testGetUsagePhrase() {
		List<String> tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "USAGE", "COMP"});
		assertEquals("COMP", COBOLSyntax.getUsagePhrase(tokens));		
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "USAGE", "IS", "COMP"});
		assertEquals("COMP", COBOLSyntax.getUsagePhrase(tokens));
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "COMP"});
		assertEquals("COMP", COBOLSyntax.getUsagePhrase(tokens));		
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)"});
		assertEquals("", COBOLSyntax.getUsagePhrase(tokens));
	}
		
	@Test
	public void testGetValue() {
		List<String> tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "VALUE", "ZERO"});
		assertEquals("ZERO", COBOLSyntax.getValue(tokens));		
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "VALUE", "IS", "ZERO"});
		assertEquals("ZERO", COBOLSyntax.getValue(tokens));
		tokens = Arrays.asList(new String[] {"01", "data-name", "PIC", "9(5)", "ZERO"});
		assertEquals("", COBOLSyntax.getValue(tokens));		
	}
	
	@Test
	public void testIsNumericPlus() {
		assertTrue(COBOLSyntax.isNumericPlus("99"));
		assertTrue(COBOLSyntax.isNumericPlus("S99"));
		assertTrue(COBOLSyntax.isNumericPlus("99V"));
		assertTrue(COBOLSyntax.isNumericPlus("S99V"));
		assertTrue(COBOLSyntax.isNumericPlus("V99"));
		assertTrue(COBOLSyntax.isNumericPlus("SV99"));
		assertTrue(COBOLSyntax.isNumericPlus("99V99"));
		assertTrue(COBOLSyntax.isNumericPlus("S99V99"));
		
		assertTrue(COBOLSyntax.isNumericPlus("0Z"));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9"));
		assertTrue(COBOLSyntax.isNumericPlus("+0Z9"));
		assertTrue(COBOLSyntax.isNumericPlus("-0Z9"));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9+"));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9-"));
		
		assertTrue(COBOLSyntax.isNumericPlus("99."));
		assertTrue(COBOLSyntax.isNumericPlus("+99."));
		assertTrue(COBOLSyntax.isNumericPlus("-99."));
		assertTrue(COBOLSyntax.isNumericPlus("99.+"));
		assertTrue(COBOLSyntax.isNumericPlus("99.-"));
		
		assertTrue(COBOLSyntax.isNumericPlus("0Z9."));
		assertTrue(COBOLSyntax.isNumericPlus("+0Z9."));		
		assertTrue(COBOLSyntax.isNumericPlus("-0Z9."));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9.+"));		
		assertTrue(COBOLSyntax.isNumericPlus("0Z9.-"));
		
		assertTrue(COBOLSyntax.isNumericPlus("0Z9.9"));		
		assertTrue(COBOLSyntax.isNumericPlus("+0Z9.9"));
		assertTrue(COBOLSyntax.isNumericPlus("-0Z9.9"));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9.9+"));
		assertTrue(COBOLSyntax.isNumericPlus("0Z9.9-"));
	}
	
	@Test
	public void testIsAlphabetic() {
		assertTrue(COBOLSyntax.isAlphabetic("A"));
		assertTrue(COBOLSyntax.isAlphabetic("AAAA"));
		assertFalse(COBOLSyntax.isAlphabetic(""));
	}
	
	@Test
	public void testIsAlphanumeric() {
		assertFalse(COBOLSyntax.isAlphanumeric("A"));
		assertFalse(COBOLSyntax.isAlphanumeric("9"));
		assertFalse(COBOLSyntax.isAlphanumeric("A9"));
		assertFalse(COBOLSyntax.isAlphanumeric("9A"));
		assertTrue(COBOLSyntax.isAlphanumeric("X"));
		assertTrue(COBOLSyntax.isAlphanumeric("X9"));
		assertTrue(COBOLSyntax.isAlphanumeric("9X"));
		assertTrue(COBOLSyntax.isAlphanumeric("XA"));
		assertTrue(COBOLSyntax.isAlphanumeric("AX"));
		assertTrue(COBOLSyntax.isAlphanumeric("XA9"));
		assertTrue(COBOLSyntax.isAlphanumeric("A9X"));
		assertTrue(COBOLSyntax.isAlphanumeric("X9A"));
		assertTrue(COBOLSyntax.isAlphanumeric("9AX"));
		assertTrue(COBOLSyntax.isAlphanumeric("9AXA9"));
		assertTrue(COBOLSyntax.isAlphanumeric("9AX9A"));
		assertTrue(COBOLSyntax.isAlphanumeric("A9X9A"));
		assertTrue(COBOLSyntax.isAlphanumeric("A9XA9"));
		assertTrue(COBOLSyntax.isAlphanumeric("99X99"));
		assertTrue(COBOLSyntax.isAlphanumeric("AAXAA"));
	}
	
	@Test
	public void testIsAlphanumericEdited() {
		assertFalse(COBOLSyntax.isAlphanumericEdited("X"));
		assertFalse(COBOLSyntax.isAlphanumericEdited("A"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("X/"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("/X"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("XB"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("BX"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("X0"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("0X"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("A/"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("/A"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("AB"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("BA"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("A0"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("0A"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("09A"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("A90"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("B9X"));
		assertTrue(COBOLSyntax.isAlphanumericEdited("X9B"));
	}	
	
	@Test
	public void testIsFloat4() {
		assertTrue(COBOLSyntax.isFloat4("COMP-1"));
		assertTrue(COBOLSyntax.isFloat4("COMPUTATIONAL-1"));
		assertFalse(COBOLSyntax.isFloat4("COMPUTATIONAL-2"));
	}	
	
	@Test
	public void testIsFloat8() {
		assertTrue(COBOLSyntax.isFloat8("COMP-2"));
		assertTrue(COBOLSyntax.isFloat8("COMPUTATIONAL-2"));
		assertFalse(COBOLSyntax.isFloat8("COMPUTATIONAL-1"));
	}		
	
	@Test
	public void testIsSigned() {
		assertTrue(COBOLSyntax.isSigned("S99"));
		assertTrue(COBOLSyntax.isSigned("+99"));
		assertTrue(COBOLSyntax.isSigned("-99"));
		assertTrue(COBOLSyntax.isSigned("99+"));
		assertTrue(COBOLSyntax.isSigned("99-"));
	}		
	
	@Test(expected = RuntimeException.class)  
	public void testIsSignedForException() {
		assertTrue(COBOLSyntax.isSigned("99S"));
	}			
	
	@Test  
	public void testGetCharacteristicDigits() {
		assertEquals(0, COBOLSyntax.getCharacteristicDigits(".999"));
		assertEquals(0, COBOLSyntax.getCharacteristicDigits("V999"));
		assertEquals(3, COBOLSyntax.getCharacteristicDigits("999."));
		assertEquals(3, COBOLSyntax.getCharacteristicDigits("999V"));
		assertEquals(3, COBOLSyntax.getCharacteristicDigits("999.99"));
		assertEquals(3, COBOLSyntax.getCharacteristicDigits("999V99"));
	}			
	
	@Test  
	public void testGetMantissaDigits() {
		assertEquals(3, COBOLSyntax.getMantissaDigits(".999"));
		assertEquals(3, COBOLSyntax.getMantissaDigits("V999"));
		assertEquals(0, COBOLSyntax.getMantissaDigits("999."));
		assertEquals(0, COBOLSyntax.getMantissaDigits("999V"));
		assertEquals(2, COBOLSyntax.getMantissaDigits("999.99"));
		assertEquals(2, COBOLSyntax.getMantissaDigits("999V99"));
	}			
}