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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ooluk.ddm.dataimport.CaseMode;
import com.ooluk.ddm.dataimport.MessageKey;
import com.ooluk.ddm.dataimport.Messages;
import com.ooluk.ddm.dataimport.data.ScannedAttribute;
import com.ooluk.ddm.dataimport.data.ScannedAttributeCode;
import com.ooluk.ddm.dataimport.data.ScannedDataObject;
import com.ooluk.ddm.dataimport.rule.RuleStore;
import com.ooluk.ddm.dataimport.rule.RulesEngine;
import com.ooluk.ddm.dataimport.workers.AbstractRuleBasedDataObjectReader;
import com.ooluk.ddm.dataimport.workers.TypeMetaData;

/**
 * COBOLDataObjectReader is an implementation of the DataObjectReader for COBOL copybooks. COBOLDataObjectReader extends
 * the RuleBasedDataObjectReader thus leveraging the rules facility.
 * 
 * <p>
 * This reader can be configured for two scopes of scanning for DataObjects:
 * <ul>
 * <li>COPYBOOK : A single copybook
 * <li>PDS : All copybooks in a PDS
 * </ul>
 * </p>
 * 
 * <p>
 * This implementation is not designed to directly work with the mainframe. You would need to download the copybooks to
 * a file system accessible to this reader. A single copybook should be specified as a file and a PDS should be
 * specified as a directory in which all copybooks are present as files.
 * </p>
 * 
 * <p>
 * This implementation uses three types of rules
 * <ul>
 * <li>Namespace rules
 * <li>Data Type rules
 * <li>Common Type rules
 * </ul>
 * 
 * <p>
 * The COBOLDataObjectReader can also be used to obtain COBOL type information. This is only valid for COPYBOOK scope.
 * Enable the reader for type information using {@link #enableForTypeMode}. To get type information use the usual
 * {@link #read} method and then call {@link #getTypeMetaData}. {@link #enableForTypeMode} should be called before
 * {@link #init(Map)}.
 * 
 * <p>
 * While initializing this reader do not call {@link #init()} instead call {@link #init(Map)}.
 * </p>
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public class COBOLDataObjectReader extends AbstractRuleBasedDataObjectReader {
	
	private final Logger log = LogManager.getLogger();

    /*
     * Parameters passed on call to init(...) that need to be saved.
     */
    // The case mode is optional and defaults to MIXED case 
	private CaseMode caseMode = CaseMode.MIXED;
	
	// The MetaDataScope is mandatory
	private COBOLMetaDataScope scope;
	
	// The copybook directory is mandatory for scope PDS
	private String copybookDirectory;
	
	// The copybook file is mandatory for scope COPYBOOK. 
	// For PDS scope this represents the copybook currently being processed
	private String copybookFile;

	/*
     * Member variables
     */
    // A flag used to signal the completion of initial configuration
	private boolean configured = false;
	
	/*
	 * A flag to indicate processing is complete for COPYBOOK scope. This flag is used because the copybook processing
	 * code is reused for both scopes. In case of PDS scope the code has to be repeatedly called for each copybook
	 * whereas for COPYBOOK scope the processing of the one COPYBOOK indicates end of processing.
	 */
	private boolean processed = false;
	
	/*
	 * Reader for the currently in-process COBOL copybook. This variable is maintained as an instance variable because
	 * it is shared among methods. This could be passed on the stack as well as an argument to every method call but we
	 * keep it simple.
	 */
	private BufferedReader reader;
	
	// Tracks the current copybook line
	private String currline = null;
	
	// List of files (copybook members) in the PDS directory for PDS scope
	private File[] members;
	
	// Index counter into the list of files (copybooks)
	private int memberIdx = 0;
	
	TypeMetaData typeMetaData;

	/**
	 * Parameterless constructor 
	 */
	public COBOLDataObjectReader() {
	}

	@Override
	public void enableForTypeMode() {
		super.enableForTypeMode();
		typeMetaData = new TypeMetaData();
	}

	@Override
	public void init(Map<String, Object> params) {

		extractScope(params);
    	extractNamespacePrefix(params);
    	extractRulesParameters(params);
    	extractCaseMode(params);    	
		configured = true;
		init();
	}

	@Override
	public void init() {
		if (!configured) {
			throwImportException(Messages.getMessage(
					MessageKey.READ_NOT_CONFIG, "COBOLDataObjectReader"));
		}
	}

    /**
	 * This method extracts the scope from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
    private void extractScope(Map<String, Object> params) {

		String readScope = (String) params.get("scope");
		if (readScope == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "scope");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		try {
			scope = COBOLMetaDataScope.valueOf(readScope.toUpperCase());
		} catch (IllegalArgumentException ex) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "scope");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}

		/*
		 * Read parameters: For PDS scope: directory; For COPYBOOK scope: file location.
		 */
		switch (scope) {
			case PDS:
				configureForPDSScope(params);
				break;
			case COPYBOOK:
				configureForCopybookScope(params);
				break;
		}
    	
       	// Type Mode is only valid for COPYBOOK scope		
		if (super.isEnabledForTypeMode() && scope != COBOLMetaDataScope.COPYBOOK) {
			throwImportException(Messages.getMessage(MessageKey.COB_TYPE_MODE_INVALID));
		}
    }

	/**
	 * Configures the reader for PDS scope.
	 * 
	 * @param params
	 *            the initialization parameters specified as a parameter name-value map.
	 */
	private void configureForPDSScope(Map<String, Object> params) {
		
		copybookDirectory = (String) params.get("copybookDirectory");
		if (copybookDirectory == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "copybookDirectory");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		File dir = new File(copybookDirectory);
		if (!dir.exists()) {
			String msg = Messages.getMessage(MessageKey.COB_DIR_NOT_PRESENT, copybookDirectory);
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		if (!dir.isDirectory()) {
			String msg = Messages.getMessage(MessageKey.COB_NOT_A_DIR, copybookDirectory);
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		members = dir.listFiles();
	}

	/**
	 * Configures the reader for COPYBOOK scope.
	 * 
	 * @param params
	 *            the initialization parameters specified as a parameter name-value map.
	 */
	private void configureForCopybookScope(Map<String, Object> params) {
		
		copybookFile = (String) params.get("copybookFile");
		if (copybookFile == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "copybookFile");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		File file = new File(copybookFile);
		if (!file.exists()) {
			String msg = Messages.getMessage(MessageKey.COB_COPY_NOT_PRESENT, copybookFile);
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
		
		if (!file.isFile()) {
			String msg = Messages.getMessage(MessageKey.COB_COPY_NOT_FILE, copybookFile);
			super.appendStatusLine(msg);
			throwImportException(msg);
		}		
	}
    
   /**
	 * Extracts the namespace prefix from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
   private void extractNamespacePrefix(Map<String, Object> params) {
   	
       namespacePrefix = ((String)params.get("namespacePrefix"));
       if (namespacePrefix == null) {
	       	String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "namespacePrefix");
	       	super.appendStatusLine(msg);
	   		throwImportException(msg);
       }
       
       namespacePrefix = namespacePrefix.trim();
       if (namespacePrefix.isEmpty()) {
	       	String msg = Messages.getMessage(MessageKey.WORKER_PARAM_EMPTY, "namespacePrefix");
	       	super.appendStatusLine(msg);
	   		throwImportException(msg);	
       }    
   }
   
   /**
	 * Extracts the import rules from the initialization parameters.
	 * 
	 * @param params
	 *            initialization parameters
	 */
   private void extractRulesParameters(Map<String, Object> params) {
   	
		// Rule group is not mandatory
		String ruleGroup = ((String) params.get("ruleGroup"));
		if (ruleGroup != null) {
			super.setRuleGroup(ruleGroup);
		}

		ruleStore = ((RuleStore) params.get("ruleStore"));
		if (ruleStore == null) {
			String msg = Messages.getMessage(MessageKey.WORKER_PARAM_MISSING, "ruleStore");
			super.appendStatusLine(msg);
			throwImportException(msg);
		}
   }
   
   /**
	 * Extracts the case mode from the initialization parameters. The case mode is optional.
	 * 
	 * @param params
	 *            initialization parameters
	 */
   private void extractCaseMode(Map<String, Object> params) {
   	
		// Case Conversion - not mandatory
		String mode = (String) params.get("case");
		if (mode != null) {
			try {
				caseMode = CaseMode.valueOf(mode.toUpperCase());
			} catch (IllegalArgumentException ex) {
				String msg = Messages.getMessage(MessageKey.WORKER_PARAM_INVALID, "case");
				super.appendStatusLine(msg);
				throwImportException(msg);
			}
		}
   }

	@Override
	public ScannedDataObject read() {

		ScannedDataObject dObj = null;
		switch (scope) {
			case PDS:
				if (memberIdx == members.length) {
					dObj = null;
					break;
				}
				copybookFile = members[memberIdx++].getPath();
				dObj = processCopybook();
				break;
			case COPYBOOK:
				dObj = processed ? null : processCopybook();
				break;
		}
		return dObj;
	}

	/**
	 * Scans a copybook.
	 * 
	 * @return scanned data object.
	 */
	private ScannedDataObject processCopybook() {
		ScannedDataObject dObj = createDataObject();
		processed = true;
		return dObj;
	}

	/**
	 * Creates a ScannedDataObject from the current copybook.
	 * 
	 * @return a ScannedDataObject instance for the the current copybook.
	 */
	private ScannedDataObject createDataObject() {

		// Namespace
		String oNamepsace = ruleStore.getRule("namespace", "name");
		if (oNamepsace != null) {
			oNamepsace = oNamepsace.replace("%prefix%", namespacePrefix);
		} else {
			oNamepsace = namespacePrefix;
		}

		// Name
		File fCopy = new File(copybookFile);
		String oName = fCopy.getName();
		String msg = Messages.getMessage(MessageKey.READ_IMPORTING, oName);
		super.appendStatusLine("-------------------------------------------");
		super.appendStatusLine(msg);
		super.appendStatusLine("-------------------------------------------");
		log.trace(msg);

		// Attributes
		reader = openFile(fCopy);
		List<ScannedAttribute> attributes = createAttributes(oName);
		try {
	        reader.close();
        } catch (IOException e) {
	        log.error(e.getMessage(), e);
	        // Don't throw exception if error occurs on close(). Do log it though.
        }

		// Create ScannedDataObject
		ScannedDataObject dObj = new ScannedDataObject(caseMode);
		dObj.setNamespace(oNamepsace);
		dObj.setName(oName);
		dObj.setAttributes(attributes);
		return dObj;
	}

	/**
	 * Creates the attributes for the current ScannedDataObject.
	 * 
	 * @param copybook
	 *            copybook name
	 * 
	 * @return list of scanned attributes.
	 */
	private List<ScannedAttribute> createAttributes(String copybook) {

		List<ScannedAttribute> attributes = new ArrayList<>();
		ScannedAttribute attr = null;

		String line = getNextDeclaration();
		while (line != null) {
			if (line.isEmpty()) {
				line = getNextDeclaration();
				continue;
			}
			
			COBOLMetaData cbmd = new COBOLMetaData(line);
			if (cbmd.getLevel() == 88) {
				String codeValue = cbmd.getValue();
				String valueDesc = cbmd.getDataName();
				if (!codeValue.isEmpty() && !valueDesc.isEmpty()) {
					// An attribute must have been created here for a syntactically correct COPYBOOK.
					attr.addCode(new ScannedAttributeCode(codeValue, valueDesc));
					line = getNextDeclaration();
					continue;
				}
			}
			
			if (cbmd.getDataName().isEmpty() || cbmd.getType().isEmpty()) {
				line = getNextDeclaration();
				continue;
			}
			
			attr = createAttribute(cbmd, attributes.size() + 1);
			attributes.add(attr);
			line = getNextDeclaration();
		}
		return attributes;
	}

	/**
	 * Creates an attribute from COBOLMetaData information.
	 * 
	 * @param cbmd
	 *            the COBOL Metadata
	 * @param seqNo
	 *            the sequence number of the current declaration
	 *            
	 * @return created ScannedAttribute.
	 */
	private ScannedAttribute createAttribute(COBOLMetaData cbmd, int seqNo) {

		ScannedAttribute attr = new ScannedAttribute();

		// Name
		String aName = cbmd.getDataName();

		// Position
		int aPosition = seqNo;

		String _declaredType = cbmd.getDeclaredType();
		String _type = cbmd.getType();
		String _size = String.valueOf(cbmd.getSize());
		String _decimal = String.valueOf(cbmd.getDecimalDigits());
		String _usage = cbmd.getUsage();

		if (super.isEnabledForTypeMode()) {
			addTypeData(aName, _declaredType, _type, _size, _decimal, _usage);
			// We do not require the remaining logic in the while loop for type information
			return attr;
		}
		// Data type - default: map to _declaredType
		String aDataType = ruleStore.getRule("data-type", _type);
		if (aDataType != null) {
			aDataType = aDataType.replace("%type%", _type);
			aDataType = aDataType.replace("%size%", _size);
			aDataType = aDataType.replace("%scale%", _decimal);
			aDataType = aDataType.replace("%usage%", _usage);
			aDataType = RulesEngine.processRule(aDataType);
		} else {
        	//super.appendStatusLine("WARNING: No data type rules were found... performing default mapping ...");
			aDataType = _declaredType;
		}

		// Common type - default: map to "blanks"
		String aCommonType = ruleStore.getRule("common-type", _type);
		if (aCommonType != null) {
			aCommonType = aCommonType.replace("%type%", _type);
			aCommonType = aCommonType.replace("%size%", _size);
			aCommonType = aCommonType.replace("%scale%", _decimal);
			aCommonType = aCommonType.replace("%usage%", _usage);
			aCommonType = RulesEngine.processRule(aCommonType);
		} else {
        	//super.appendStatusLine("WARNING: No common type rules were found... performing default mapping ...");
			aCommonType = "";
		}

		// Default value
		String aDefault = cbmd.getValue();

		attr.setName(aName);
		attr.setSeqNo(aPosition);
		attr.setDataType(aDataType);
		attr.setCommonType(aCommonType);
		attr.setDefaultValue(aDefault);
		return attr;
	}

	/**
	 * Opens the specified file for reading.
	 * 
	 * @param file
	 *            the file path
	 */
	private BufferedReader openFile(File file) {
		BufferedReader fReader = null;
		try {
			fReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
	        log.error(e.getMessage(), e);
			throwImportException(e);
		}
		return fReader;
	}

	/**
	 * Returns the next declaration from the copybook. A declaration could span multiple physical lines. This method
	 * concatenates all such lines to extract a complete declaration.
	 * 
	 * @return a field declaration.
	 */
	private String getNextDeclaration() {
		StringBuilder sentence = new StringBuilder();
		currline = currline == null ? readLine(reader) : currline;
		while (currline != null) {
			int length = currline.length() > 72 ? 72 : currline.length(); 
			currline = currline.substring(7, length);
			sentence.append(currline);
			String nextline = readLine(reader);
			if (COBOLSyntax.isContinued(currline)) {
				currline = nextline;
				continue;
			}
			currline = nextline;
			String str = sentence.toString().trim();
			if (str.endsWith(".")) {
				str = str.substring(0, str.length() - 1);
			}
			return str;
		}
		return currline;
	}

	/**
	 * Reads a declaration line from the copybook. This method just reads a physical line. A declaration could span
	 * multiple physical lines.
	 * 
	 * @param reader
	 *            reader for the copybook.
	 * 
	 * @return next declaration line.
	 */
	private String readLine(BufferedReader reader) {
		String line = "";
		try {
			line = reader.readLine();
			int length = line == null ? 0 : line.length() > 72 ? 72 : line.length(); 
			// Skip short lines, blank lines and comments
			while (line != null && 
					(line.length() < 7 || line.substring(7, length).isEmpty() || COBOLSyntax.isComment(line))) {
				line = reader.readLine();
				length = line == null ? 0 : line.length() > 72 ? 72 : line.length(); 
			}			
		} catch (IOException e) {
	        log.error(e.getMessage(), e);
			throwImportException(e);
		}
		return line;
	}

	@Override
	public void close() {
		try {
			if (reader != null)
				reader.close();
        } catch (IOException e) {
	        log.error(e.getMessage(), e);
        }
	}

	@Override
	public TypeMetaData getTypeMetaData() {
		typeMetaData.addProperty("attribute");
		typeMetaData.addProperty("declaration");
		typeMetaData.addProperty("type");
		typeMetaData.addProperty("size");
		typeMetaData.addProperty("scale");
		typeMetaData.addProperty("usage");
		return typeMetaData;
	}

	/**
	 * Adds type COBOL type information.
	 * 
	 * @param col
	 *            field name
	 * @param declaration
	 *            the PIC declaration
	 * @param type
	 *            field type
	 * @param size
	 *            field size
	 * @param scale
	 *            decimal digits if applicable
	 * @param usage
	 *            the usage clause if applicable
	 */
	private void addTypeData(String col, String declaration, String type, String size, String scale, String usage) {
		Map<String, String> typeMap = new HashMap<>();
		typeMap.put("attribute", col);
		typeMap.put("declaration", declaration);
		typeMap.put("type", type);
		typeMap.put("size", size);
		typeMap.put("scale", scale);
		typeMap.put("usage", usage);
		typeMetaData.add(typeMap);
	}

	/**
	 * Returns the case mode configured for this data object reader.
	 * 
	 * @return reader's case mode.
	 */
	public CaseMode getCaseMode() {
		return caseMode;
	}

	/**
     * Returns the scope set for this data object reader.
     * 
	 * @return reader's scope.
	 */
	public COBOLMetaDataScope getScope() {
		return scope;
	}

	/**
	 * Returns the directory path for copybooks to be scanned. This method is applicable to the PDS scope only.
	 * 
	 * @return the path to the directory containing all copybooks to be scanned.
	 */
	public String getCopybookDirectory() {
		return copybookDirectory;
	}

	/**
	 * Returns the file path for the copybook to be scanned. This method is applicable to the COPYBOOK scope only.  
	 * 
	 * @return the file path to the copybook to be scanned.
	 */
	public String getCopybookFile() {
		return copybookFile;
	}
}