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

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * The COBOLSyntax class provides static methods that allow querying a COBOL copybook declaration and extracting
 * fragments from it. This class designed to be non-instantiable.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public final class COBOLSyntax {
		
	// List of keywords that can directly follow a level-number in a data description.
	private static String[] keywords = {
            "FILLER", "RENAMES", "REDEFINES", "BLANK", "EXTERNAL", "GLOBAL", "GROUP-USAGE", "JUSTIFIED", 
            "JUST", "PICTURE", "PIC", "SIGN", "SYNCHRONIZED", "SYNC", "USAGE", "VALUE", "VALUES"
     };
	
	// Supported USAGE phrases
	private static String[] usages = {
			"BINARY", "COMP", "COMP-1", "COMP-2", "COMP-3", "COMP-4", "COMP-5",
			"COMPUTATIONAL", "COMPUTATIONAL-1", "COMPUTATIONAL-2", 
			"COMPUTATIONAL-3", "COMPUTATIONAL-4", "COMPUTATIONAL-5",
	};
	
     private static List<String> keywordList = Arrays.asList(keywords);
     private static List<String> usageList = Arrays.asList(usages);
     
     /**
      * Make the class uninstantiable.
      */
     private COBOLSyntax() {    	 
     }

	/**
	 * Determines if the specified sentence is a COBOL comment.
	 * 
	 * <p>
	 * Syntax: A COBOL comment starts with '/' or '*' in column 7
	 * 
	 * @param sentence
	 *            COBOL sentence
	 *            
	 * @return true if the sentence is a comment; false otherwise.
	 */
	public static boolean isComment(String sentence) {
		if (sentence.length() < 7) {
			return true;
		}
		
        if (sentence.charAt(6) == '/' || sentence.charAt(6) == '*') {
            return true;
        }
        return false;
	}

	/**
	 * Determines if the current line is continued.
	 * 
	 * <p>
	 * Syntax: A COBOL declaration is continued if it does not end with a period.
	 * 
	 * @param currline
	 *            current line
	 *            
	 * @return true if the current line is continued; false otherwise.
	 */
    public static boolean isContinued(String currline) {
    	if (!currline.trim().endsWith(".")) {
            return true;
        }
        return false;
	}

	/**
	 * Determines if the next line is a continuation of the current line.
	 * 
	 * <p>
	 * Syntax: A COBOL continuation requires a '-' in column 7
	 * 
	 * @param nextline
	 *            next line
	 *            
	 * @return true if the next line is a continuation; false otherwise.
	 */
    public static boolean isContinuation(String nextline) {
        if (nextline != null && nextline.length() > 6 && nextline.charAt(6) == '-') {
            return true;
        }
        return false;
	}

	/**
	 * Expands a declaration (PIC clause character string).
	 * 
	 * <p>
	 * Example: Expands 99(2)V99(3) to 999V9999
	 * 
	 * <p>
	 * Logic: Scans the input string once from beginning to end. Saves the output to a StringBuilder. Copies any portion
	 * of the string before a shorthand notation of the type X(n) to the output. Expands X(n) to a string of n "X" and
	 * appends it to the output. Repeats the previous two steps for the remaining portion of the string.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 * 
	 * @return expanded declaration.
	 */
    public static String expandDeclaration(String decl) {
    	int idx = 0;
    	StringBuilder result = new StringBuilder();
        StringBuilder exp = new StringBuilder();
    	while (idx < decl.length()) {
	        int pos_open = decl.indexOf('(', idx);
	        if (pos_open == -1) {
	            return result.append(decl.substring(idx)).toString();
	        }
	        char pre_char = decl.charAt(pos_open - 1);
	        int pos_close = decl.indexOf(')', idx);
	        int len = Integer.parseInt(decl.substring(pos_open + 1, pos_close));
	        exp.delete(0, exp.length());
	        for (int i = 1; i <= len; i++) {
	            exp.append(pre_char);
	        }
	        result.append(decl.substring(idx, pos_open - 1)).append(exp.toString());
	        idx = pos_close+1;
    	}
    	return result.toString();
	}

	/**
	 * Compacts an expanded declaration.
	 * 
	 * Example: 999ZZZ = 9(3)Z(3)
	 * 
	 * @param expDecl
	 *            expanded declaration
	 *            
	 * @return a compacted version of the expanded declaration.
	 */
	public static String compactDeclaration(String expDecl) {
        StringBuilder mergeDecl = new StringBuilder();
        StringBuilder repeatedType = new StringBuilder();
        int count = 1;
        char prev_char = expDecl.charAt(0);
        for (int i = 1; i <= expDecl.length(); i++) {
            char curr_char = i < expDecl.length() ? expDecl.charAt(i) : ' ';
            if (COBOLSyntax.isRepeatable(prev_char) && prev_char == curr_char) {
                count++;
            } else {
                if (count > 1) {
                    repeatedType.append(prev_char).append('(').append(count).append(')');
                    mergeDecl.append(repeatedType);
                } else {
                    mergeDecl.append(prev_char);
                }
                repeatedType.delete(0, repeatedType.length());
                count = 1;
                prev_char = curr_char;
            }
        }
        return mergeDecl.toString();
	}

	/**
	 * Determines if the specified token is a keyword that can follow the level-number.
	 * 
	 * @param token
	 *            the token to be examined
	 *            
	 * @return true if the token is a keyword that can follow a level-number; false otherwise.
	 */
    private static boolean isKeyword(String token) {
        return keywordList.contains(token);                        
	}

	/**
	 * Extracts the level-number from a data description entry.
	 * 
	 * <p>
	 * Syntax: Level-number is mandatory and always the first token.
	 * 
	 * @param tokens
	 *            list of tokens from a data description entry.
	 *            
	 * @return the level number of the data description entry.
	 */
    public static int getLevelNumber(List<String> tokens) {
        return Integer.parseInt(tokens.get(0));
	}

	/**
	 * Extracts the data-name from a data description entry.
	 * 
	 * <p>
	 * Syntax: Data name, if present, follows the level-number.
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry.
	 *            
	 * @return the data name of the data description entry.
	 */
    public static String getDataName(List<String> tokens) {
        String possibleDataName = tokens.get(1);
        return isKeyword(possibleDataName) ? "" : possibleDataName;
	}

	/**
	 * Determines if a data description entry has a picture clause.
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry
	 *            
	 * @return true if picture clause is present; false otherwise.
	 */
    private static boolean hasPictureClause(List<String> tokens) {
    	return tokens.contains("PIC") || tokens.contains("PICTURE");
	}

	/**
	 * Extracts the picture character string from a data description entry.
	 * 
	 * <p>
     * Syntax
     * <ul> 
     * <li>PICTURE xxx 
     * <li>PIC xxx
     * <li>PICTURE IS xxx
     * <li>PIC IS xxx 
     * </ul>
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry
	 *            
	 * @return the picture character string of the data description entry.
	 */
    public static String getPictureString(List<String> tokens) {
        String chStr = "";
        if (hasPictureClause(tokens)) {
            int idx = tokens.contains("PIC") ? tokens.indexOf("PIC") : tokens.indexOf("PICTURE");
            chStr = tokens.get(idx + 1).equals("IS") ? tokens.get(idx + 2) : tokens.get(idx + 1);
        }
        return chStr;
	}

	/**
	 * Determines if a data description entry has a usage clause.
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry
	 *            
	 * @return true if usage clause is present; false otherwise.
	 */
    private static boolean hasUsageClause(List<String> tokens) {
    	return tokens.contains("USAGE");
	}

	/**
	 * Extracts the USAGE phrase from a data description entry.
	 * 
	 * <p>
     * Syntax
     * <ul> 
     * <li>USAGE xxx 
     * <li>USAGE IS xxx
     * <li>A 'usage-phrase' without the USAGE keyword
     * </ul>
     * 
     * </pre>
	 * 
	 * @param tokens
	 *            list of tokens from a data description entry
	 *            
	 * @return the USAGE string of the data description entry.
	 */
    public static String getUsagePhrase(List<String> tokens) {
        String phrase = "";
        if (hasUsageClause(tokens)) {
            int idx = tokens.indexOf("USAGE");
            phrase = tokens.get(idx + 1).equals("IS") ? tokens.get(idx + 2) : tokens.get(idx + 1);
        } else {
        	for(String usage : usageList) {
        		if (tokens.contains(usage)) {
        			return usage;
        		}
        	}
        }
        return phrase;
	}

	/**
	 * Determines if a data description entry has a VALUE clause.
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry
	 *            
	 * @return true if VALUE clause is present; false otherwise.
	 */
	private static boolean hasValueClause(List<String> tokens) {
		return tokens.contains("VALUE");
	}

	/**
	 * Determines if a data description entry has a VALUES clause.
	 * 
	 * @param tokens
	 *            list of tokens in the data description entry
	 *            
	 * @return true if VALUES clause is present; false otherwise.
	 */
    private static boolean hasValuesClause(List<String> tokens) {
    	return tokens.contains("VALUES");
    }

    /**
     * This method assume values are always at the end.
     * 
     * @param tokens
	 *            list of tokens in the data description entry
     * @param idx
	 *            the index at which the values begin
     * @return
     */
    private static String getValues(List<String> tokens, int idx) {
    	String values= "";
    	int i = idx;
    	for ( ; i < tokens.size(); i++) {
    		values += " " + tokens.get(i);
    	}
    	values = values.trim();
    	
    	// Single value
    	if (i == idx + 1) {
	        if (values.startsWith("\"") || values.startsWith("'")) {
	        	values = values.substring(1, values.length()-1);
	        }
    	}
    	return values;
	}

	/**
	 * Extracts the value from a data description entry.
	 * 
	 * <p>
     * Syntax
     * <ul> 
     * <li>VALUE xxx 
     * <li>VALUE IS xxx 
     * <li>VALUES ARE xxx
     * </ul>
	 * 
	 * @param tokens
	 *            list of tokens from a data description entry
	 *            
	 * @return the VALUE of the data description entry.
	 */
    public static String getValue(List<String> tokens) {
        String value = "";
        if (hasValueClause(tokens)) {
            int idx = tokens.indexOf("VALUE");
            value = tokens.get(idx + 1).equals("IS") ? getValues(tokens, (idx + 2)) : getValues(tokens, (idx + 1));
        } else if (hasValuesClause(tokens)) {
            int idx = tokens.indexOf("VALUES");
            value = tokens.get(idx + 1).equals("ARE") ? getValues(tokens, (idx + 2)) : getValues(tokens, (idx + 1));
        }
        return value;
	}

	/**
	 * Determines if the declaration is numeric or numeric-edited (certain combinations only). This method must be
	 * called on a expanded declaration only.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isNumericPlus(String decl) {
        String uDecl = decl.toUpperCase();
        return uDecl.matches("([S+-]?)([0Z9.V])+[+-]?");
	}

	/**
	 * Determines if the declaration is of alphabetic type. This method must be called on a expanded declaration only.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isAlphabetic(String decl) {
        String uDecl = decl.toUpperCase();
        return uDecl.matches("A+");
	}

	/**
	 * Determines if the declaration is of alphabetic or alphanumeric or alphanumeric-edited type (only 9s). This method
	 * must be called on a expanded declaration only.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isAlphanumericPlus(String decl) {
        return isAlphanumeric(decl) || isAlphanumericEdited(decl);
	}

	/**
	 * Determines if the declaration is of alphanumeric type. This method must be called on a expanded declaration only.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isAlphanumeric(String decl) {
    	String uDecl = decl.toUpperCase();
        return uDecl.matches("[AX9]*X{1}[AX9]*");
	}

	/**
	 * Determines if the declaration is of alphanumeric-edited type. This method must be called on a expanded
	 * declaration only.
	 * 
	 * @param decl
	 *            the character string of the PIC clause
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isAlphanumericEdited(String decl) {
    	String uDecl = decl.toUpperCase();
        return uDecl.matches("[AX90B/]*(([AX]{1}9*[B0/]{1})|([B0/]{1}9*[AX]{1}))[AX90B/]*");
	}

	/**
	 * Determines if the usage string is of COMP-1 internal floating-point type.
	 * 
	 * @param usage
	 *            the usage string
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isFloat4(String usage) {
    	return usage.equals("COMP-1") ||
    			usage.equals("COMPUTATIONAL-1");
	}

	/**
	 * Determines if the usage string is of COMP-2 internal floating-point type.
	 * 
	 * @param usage
	 *            the usage string
	 *            
	 * @return true if the declaration is of the type; false otherwise.
	 */
    public static boolean isFloat8(String usage) {
    	return usage.equals("COMP-2") ||
    			usage.equals("COMPUTATIONAL-2");
	}

	/**
	 * Determines if the character can repeat in the PIC character string.
	 * 
	 * @param ch
	 *            the character to check
	 *            
	 * @return true if the character can repeat; false otherwise.
	 */
    private static boolean isRepeatable(char ch) {    	
        return (ch == 'X' || ch == 'A' || ch == '9' || ch == 'Z');
	}

	/**
	 * Determines if the "numeric plus" declaration if signed. This method must be called on a expanded declaration
	 * only.
	 * 
	 * @param decl
	 *            the "expanded" character string of the PIC clause
	 *            
	 * @return true if it is signed; false otherwise.
	 */
    public static boolean isSigned(String decl) {
        if (!isNumericPlus(decl)) {
            throw new RuntimeException("Invalid Call");
        }
        if (decl.matches("(.)*[S+-](.)*")) {
        	return true;
        } 
        return false;
	}

	/**
	 * Removes the sign indicator from the PIC character string. This method must be called on a expanded declaration
	 * only.
	 * 
	 * @param decl
	 *            the "expanded" character string of the PIC clause
	 *            
	 * @return the expanded declaration without the sign.
	 */
    private static String removeSignIndicator(String decl) {
        if (!isSigned(decl)) {
            throw new RuntimeException("Invalid Call");
        }
        if (decl.startsWith("S") || decl.startsWith("+") || decl.startsWith("-")) {
            return decl.substring(1);
        } else {
            return decl.substring(0, decl.length()-1);
        }
	}

	/**
	 * Gets the number of digits to the left of the decimal. This method must be called on a expanded declaration only.
	 * 
	 * @param type
	 *            the numeric declaration
	 *            
	 * @return number of digits to the left of the decimal
	 */
    public static int getCharacteristicDigits(String type) {
        if (isSigned(type)) {
            type = removeSignIndicator(type);
        }
        int cDigits = 0;
        if (!type.matches("(.)*[V.]+(.)*")) {
        	cDigits = type.length();
        } else if (type.matches("(.)*[.]+(.)*")) {
        	cDigits = type.indexOf('.');
        } else if (type.matches("(.)*[V]+(.)*")) {
        	cDigits = type.indexOf('V');
        }
        return cDigits;
	}

	/**
	 * Gets the number of digits to the right of the decimal. This method must be called on a expanded declaration only.
	 * 
	 * @param type
	 *            the numeric declaration
	 *            
	 * @return number of digits to the right of the decimal.
	 */
    public static int getMantissaDigits(String type) {
        if (isSigned(type)) {
            type = removeSignIndicator(type);
        }
        int dec_pos = 0;
        if (!type.matches("(.)*[V.]+(.)*")) {
            return 0;
        } else if (type.matches("(.)*[.]+(.)*")) {
            dec_pos = type.indexOf('.');
        } else if (type.matches("(.)*[V]+(.)*")) {
            dec_pos = type.indexOf('V');
        }
        int mDigits = type.length() - dec_pos - 1;
        return mDigits;
    }
}