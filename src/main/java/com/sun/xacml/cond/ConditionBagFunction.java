/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


/**
 * Specific <code>BagFunction</code> class that supports the single
 * condition bag function: type-is-in.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public class ConditionBagFunction extends BagFunction
{

    // mapping of function name to its associated argument type
    private static HashMap argMap;

    /**
     * Static initializer that sets up the argument info for all the
     * supported functions.
     */
    static {
        argMap = new HashMap();

        for (int i = 0; i < baseTypes.length; i++) {
            String [] args = { baseTypes[i], baseTypes[i] };
            
            argMap.put(FUNCTION_NS + simpleTypes[i] + NAME_BASE_IS_IN, args);
        }

        for (int i = 0; i < baseTypes2.length; i++) {
            String [] args = { baseTypes2[i], baseTypes2[i] };
            
            argMap.put(FUNCTION_NS_2 + simpleTypes2[i] + NAME_BASE_IS_IN,
                       args);
        }
    }

    /**
     * Constructor that is used to create one of the condition standard bag
     * functions. The name supplied must be one of the standard XACML
     * functions supported by this class, including the full namespace,
     * otherwise an exception is thrown. Look in <code>BagFunction</code>
     * for details about the supported names.
     *
     * @param functionName the name of the function to create
     *
     * @throws IllegalArgumentException if the function is unknown
     */
    public ConditionBagFunction(String functionName) {
        super(functionName, 0, getArguments(functionName));
    }

    /**
     * Constructor that is used to create instances of condition bag
     * functions for new (non-standard) datatypes. This is equivalent to
     * using the <code>getInstance</code> methods in <code>BagFunction</code>
     * and is generally only used by the run-time configuration code.
     *
     * @param functionName the name of the new function
     * @param datatype the full identifier for the supported datatype
     */
    public ConditionBagFunction(String functionName, String datatype) {
        super(functionName, 0, new String [] {datatype, datatype});
    }

    /**
     * Private helper that returns the argument types for the given standard
     * function.
     */
    private static String [] getArguments(String functionName) {
        String [] args = (String [])(argMap.get(functionName));

        if (args == null)
            throw new IllegalArgumentException("unknown bag function: " +
                                               functionName);

        return args;
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set getSupportedIdentifiers() {
        return Collections.unmodifiableSet(argMap.keySet());
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
        if (result != null)
            return result;
        
        // *-is-in takes a bag and an element of baseType and
        // returns a single boolean value
        AttributeValue item = (AttributeValue)(argValues[0]);
        BagAttribute bag = (BagAttribute)(argValues[1]);
        
        return new EvaluationResult(BooleanAttribute.
                                    getInstance(bag.contains(item)));
    }

}
