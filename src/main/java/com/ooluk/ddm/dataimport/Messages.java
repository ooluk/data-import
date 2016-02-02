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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Messages class uses resource bundle to support internationalization of messages. The messages are loaded in a static
 * initialization block and are later looked up using the getMessage() static method. This class only contains static
 * methods and should therefore never require an instantiation.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public final class Messages {

	private static ResourceBundle messages = null;
	
	/**
	 * Make the class uninstantiable by creating a private default constructor.
	 */
	private Messages() {		
	}
	
	static {
		Locale locale = Locale.getDefault();
	    messages = ResourceBundle.getBundle("messages.MessagesBundle", locale);
	}

	/**
	 * Returns the message for the specified key. This method is designed to support Internationalization.
	 * 
	 * @param key
	 *            the key for the message
	 * @param params
	 *            the parameters for the message
	 *            
	 * @return the message corresponding to the key
	 */
	public static String getMessage(MessageKey key, Object... params) {
		return MessageFormat.format(messages.getString(key.toString()), params);
	}
}