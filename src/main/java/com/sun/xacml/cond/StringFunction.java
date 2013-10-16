/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;


/**
 * This class implements the string-concatenate function from XACML 2.0.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class StringFunction extends FunctionBase
{

    /**
     * Standard identifier for the string-concatenate function.
     */
    public static final String NAME_STRING_CONCATENATE =
        FUNCTION_NS_2 + "string-concatenate";
    
    /**
     * Standard identifier for the string-concatenate function.
     */
    public static final String NAME_BOOLEAN_FROM_STRING = 
        FUNCTION_NS_3 + "boolean-from-string";

    // private identifiers for the supported functions
    private static final int ID_STRING_CONCATENATE = 0;
    private static final int ID_BOOLEAN_FROM_STRING = 1;
    
    private static HashMap<String, Integer> idMap;
    
    /**
     * Static initializer to setup the id maps.
     */
    static {
        idMap = new HashMap<String, Integer>();

        idMap.put(NAME_STRING_CONCATENATE,
                  Integer.valueOf(ID_STRING_CONCATENATE));
        idMap.put(NAME_BOOLEAN_FROM_STRING,
                Integer.valueOf(ID_BOOLEAN_FROM_STRING));
    };

    /**
     * Creates a new <code>StringFunction</code> object.
     *
     * @param functionName the standard XACML name of the function to be
     *                     handled by this object, including the full namespace
     *
     * @throws IllegalArgumentException if the function is unknown
     */
    public StringFunction(String functionName) {
        super(functionName, ID_STRING_CONCATENATE, StringAttribute.identifier, false, -1, 2, StringAttribute.identifier, false);
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set getSupportedIdentifiers() {
        Set<String> set = new HashSet<String>();

        set.add(NAME_STRING_CONCATENATE);
        set.add(NAME_BOOLEAN_FROM_STRING);

        return set;
    }
    
    private int getId(String functionName) {
    	Integer i = (Integer)(idMap.get(functionName));

        if (i == null)
            throw new IllegalArgumentException("unknown comparison function " +
                                               functionName);

        return i.intValue();
	}

    /**
     * Evaluate the function, using the specified parameters.
     *
     * @param inputs a <code>List</code> of <code>Evaluatable</code>
     *               objects representing the arguments passed to the function
     * @param context an <code>EvaluationCtx</code> so that the
     *                <code>Evaluatable</code> objects can be evaluated
     * @return an <code>EvaluationResult</code> representing the
     *         function's result
     */
    public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
        // Evaluate the arguments
        AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null) {
            return result;
        }

        switch (getId(getFunctionName())) {
        case ID_STRING_CONCATENATE:
            String str = ((StringAttribute)argValues[0]).getValue();

            for (int i = 1; i < argValues.length; i++) {
                str += ((StringAttribute)(argValues[i])).getValue();
            }

            result = new EvaluationResult(new StringAttribute(str));

            break;
        
        case ID_BOOLEAN_FROM_STRING:
        	str = ((StringAttribute)argValues[0]).getValue();
        	boolean funcResult = false;
        	if(str.equalsIgnoreCase("true")) {
        		result = new EvaluationResult(BooleanAttribute.getTrueInstance());
        	} else if(str.equalsIgnoreCase("false")) {
        		result = new EvaluationResult(BooleanAttribute.getFalseInstance());
        	} else {
        		Status status = new Status(Arrays.asList(Status.STATUS_PROCESSING_ERROR));
        		StatusCode code = new StatusCode();
				code.setValue(Status.STATUS_PROCESSING_ERROR);
				status.setStatusCode(code);
        		result = new EvaluationResult(status);
        	}
        	        	
        	
        	break;
        }

        return result;
    }
}
