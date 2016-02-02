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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * COBOLMetadata class provide the metadata information for a COBOL declaration. The metadata comprises of
 * 
 * <ul>
 * <li>Level
 * <li>Data name
 * <li>Type
 * <li>Size
 * <li>Decimal digits
 * <li>Usage
 * <li>Value
 * </ul>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 */
public class COBOLMetaData {
	
	private final Logger log = LogManager.getLogger();
	
	private int level;
	private String dataName;
	private String type;
	private int size;
	private int decimalDigits;
	private String usage;
	private String value;
	private String declaredType;
	
	public COBOLMetaData(String line) {
		List<String> tokens = tokenize(line);
        parse(tokens);
	}

	public int getLevel() {
		return level;
	}

	public String getDataName() {
		return dataName;
	}

	public String getType() {
		return type;
	}

	public int getSize() {
		return size;
	}

	public int getDecimalDigits() {
		return decimalDigits;
	}

	public String getUsage() {
		return usage;
	}

	public String getValue() {
		return value;
	}
	
	public String getDeclaredType() {
		return declaredType;
	}

	/**
	 * Tokenizes the specified string using whitespace delimiters.
	 * 
	 * @param line
	 *            the string to be tokenized.
	 *            
	 * @return list of tokens
	 */
    private List<String> tokenize(String line) {
        String[] tokenArray = line.trim().split("[\\s]+");
        List<String> tokens = Arrays.asList(tokenArray);
        return tokens;
	}

	/**
	 * Parses the list of tokens and populates the metadata.
	 * 
	 * @param tokens
	 *            list of tokens in the line
	 */
    private void parse(List<String> tokens) {
    	
    	// Level-number
        level = COBOLSyntax.getLevelNumber(tokens);
        
        // Data-Name
        dataName = COBOLSyntax.getDataName(tokens);
        
        // PICTURE string
        String pictureString = COBOLSyntax.getPictureString(tokens); 
        
        // USAGE         
        String usagePhrase = COBOLSyntax.getUsagePhrase(tokens);
        createInterfaceUsage(usagePhrase);
        
        // Type, Size and Decimal Digits
        String eType = COBOLSyntax.expandDeclaration(pictureString);
        type = "";            
        if (COBOLSyntax.isNumericPlus(eType)) {
        	createInterfaceTypeForNumeric(eType);
        } else if (COBOLSyntax.isAlphabetic(eType)) {
        	createInterfaceTypeForAlphabetic(eType);
        } else if (COBOLSyntax.isAlphanumericPlus(eType)) {
        	createInterfaceTypeForAlphanumericPlus(eType);
        } else if(COBOLSyntax.isFloat4(usagePhrase)) {
        	type = "FLOAT4";
        } else if(COBOLSyntax.isFloat8(usagePhrase)) {
        	type = "FLOAT8";
        } else if (!pictureString.isEmpty() || !usagePhrase.isEmpty()){
        	type = "[CK] " + pictureString + (usagePhrase.isEmpty() ? "" :" USAGE " + usagePhrase);
        } else {
        	type = "";
        }
        
        // Declared Type
        declaredType = pictureString + (usagePhrase.isEmpty() ? "" : (pictureString.isEmpty() ? "" : " ") + usagePhrase);
        log.trace(declaredType + " = " + type, this, "");
        
        // VALUE
        value = COBOLSyntax.getValue(tokens);
	}

	/**
	 * Creates the interface type for the alphabetic type.
	 * 
	 * @param picture
	 *            the picture character string for the alphabetic type
	 */
    private void createInterfaceTypeForAlphabetic(String picture) {
        String eType = COBOLSyntax.expandDeclaration(picture);
        type = "ALPHA";
        size = eType.length();
	}

	/**
	 * Creates the interface type for the alphanumeric / alphanumeric-edited type.
	 * 
	 * @param picture
	 *            the picture character string for the alphanumeric / alphanumeric-edited type
	 */
    private void createInterfaceTypeForAlphanumericPlus(String picture) {
        String eType = COBOLSyntax.expandDeclaration(picture);
        type = "ALPHANUM";
        size = eType.replaceAll("[B0/]", "").length();
    }

	/**
	 * Creates the interface type for the numeric / numeric-edited type.
	 * 
	 * @param picture
	 *            the picture character string for the numeric / numeric-edited type
	 */
    private void createInterfaceTypeForNumeric(String picture) {
        String eType = COBOLSyntax.expandDeclaration(picture);
        int cDigits = COBOLSyntax.getCharacteristicDigits(eType);
        int mDigits = COBOLSyntax.getMantissaDigits(eType);
        if (mDigits == 0) {
            if (COBOLSyntax.isSigned(eType)) {
                type = "SINT";
            } else {
                type = "UINT";
            }
            size = cDigits;
        } else {
            if(COBOLSyntax.isSigned(eType)) {
                type = "SNUM";
            } else {
            	type = "UNUM";
            }
            size = cDigits+mDigits;
            decimalDigits = mDigits;
        }
	}

	/**
	 * Creates the usage interface for the numeric / numeric-edited type.
	 * 
	 * @param usagePhrase
	 *            the usage phrase
	 */
    private void createInterfaceUsage(String usagePhrase) {
		switch (usagePhrase) {
			case "COMPUTATIONAL-1":
				usage = "COMP-1";
				break;
			case "COMPUTATIONAL-2":
				usage = "COMP-2";
				break;
			case "COMPUTATIONAL-3":
				usage = "COMP-3";
				break;
			case "COMP-4":
			case "COMPUTATIONAL-4":
			case "BINARY":
				usage = "COMP";
				break;
			case "COMPUTATIONAL-5":
				usage = "COMP-5";
				break;
			default:
				usage = usagePhrase;
		}
    }

    @Override
	public String toString() {
        String pLevel = String.format("%03d", level);
        String pDataName = String.format("%15s", dataName);
        String pType = String.format("%15s", type);
        String pSize = String.format("%03d", size);
        String pDec = String.format("%03d", decimalDigits);
        String pUsage = String.format("%20s", usage);
        String pValue = String.format("%20s", value);
        return pLevel + " : " + pDataName + " : " + pType + " : " + pSize + " : " + pDec + " : " + pUsage + " : " + pValue;    	
    }	
}