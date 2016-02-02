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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * This class overrides the Object's toString() method to print the values of all bean properties. This functionality is
 * used to compare objects during testing.
 * 
 * @author Siddhesh Prabhu
 * @since 1.0
 * 
 */
public abstract class PropertyPrinter {
	
	/**
	 * Checks if the method is an acceptable getter method for printing properties. 
	 * 
	 * @param method reference to a method
	 * 
	 * @return true if the method is acceptable, false otherwise.
	 */
	private boolean isAcceptableGetter(Method method) {
		// Ignore timestamp fields
		return 
				(method.getName().startsWith("get") || method.getName().startsWith("is"))				
				&& !method.getName().equals("getCreateTS") 
				&& !method.getName().equals("getUpdateTS");
	}
	
	/**
	 * Gets the camel case representation of a property from the getter method name. For example converts "getTagName"
	 * to "tagName".
	 * 
	 * @param methodName
	 *            the getter method name.
	 * 
	 * @return camelCase representation of the property
	 */
	private String getCamelCase(String methodName) {
    	// If the getter() doesn't begin with "is" it must begin with "get"
        String property = methodName.substring(methodName.startsWith("is") ? 2 : 3);
		return property.substring(0, 1).toLowerCase() + property.substring(1);
	}

	/**
	 * Returns the properties of the this object sorted by the property name. The properties are returned in a
	 * string representation of the form {property-name=property-value, ...}
	 *  
	 * @return a string of the form {property-name=property-value, ...}
	 */
	@Override
    public String toString() {
        
		Method[] methods = this.getClass().getDeclaredMethods();
		
		// Create initial size assuming equal number of getters and setters and ignoring other methods.
		List<Method> getters = new ArrayList<>(methods.length/2);
		for (Method method : methods) {
			if (isAcceptableGetter(method)) {
                getters.add(method);
            }
        }
                
        Collections.sort(getters, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }   
        });
                
        StringBuilder sb = new StringBuilder();
		for (Method method : getters) {
			if (sb.length() != 0) {
				sb.append(", ");
			}

            try {
                sb.append(getCamelCase(method.getName()))
                		.append("=")
                        .append(Objects.toString(method.invoke(this)));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        return "{" + sb.toString() + "}";
    }
}