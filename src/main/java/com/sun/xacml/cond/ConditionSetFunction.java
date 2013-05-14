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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


/**
 * Specific <code>SetFunction</code> class that supports all of the
 * condition set functions: type-at-least-one-member-of, type-subset, and
 * type-set-equals.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public class ConditionSetFunction extends SetFunction
{

    // private identifiers for the supported functions
    private static final int ID_BASE_AT_LEAST_ONE_MEMBER_OF = 0;
    private static final int ID_BASE_SUBSET = 1;
    private static final int ID_BASE_SET_EQUALS = 2;

    // mapping of function name to its associated id and parameter type
    private static HashMap<String, Integer> idMap;
    private static HashMap<String, String> typeMap;

    // the actual supported ids
    private static Set<String> supportedIds;

    /**
     * Static initializer that sets up the paramater info for all the
     * supported functions.
     */
    static {
        idMap = new HashMap<String, Integer>();
        typeMap = new HashMap<String, String>();

        for (int i = 0; i < baseTypes.length; i++) {
            String baseName = FUNCTION_NS + simpleTypes[i];
            String baseType = baseTypes[i];

            idMap.put(baseName + NAME_BASE_AT_LEAST_ONE_MEMBER_OF,
                      Integer.valueOf(ID_BASE_AT_LEAST_ONE_MEMBER_OF));
            idMap.put(baseName + NAME_BASE_SUBSET,
                      Integer.valueOf(ID_BASE_SUBSET));
            idMap.put(baseName + NAME_BASE_SET_EQUALS,
                      Integer.valueOf(ID_BASE_SET_EQUALS));

            typeMap.put(baseName + NAME_BASE_AT_LEAST_ONE_MEMBER_OF, baseType);
            typeMap.put(baseName + NAME_BASE_SUBSET, baseType);
            typeMap.put(baseName + NAME_BASE_SET_EQUALS, baseType);
        }

        for (int i = 0; i < baseTypes2.length; i++) {
            String baseName = FUNCTION_NS_2 + simpleTypes2[i];
            String baseType = baseTypes2[i];

            idMap.put(baseName + NAME_BASE_AT_LEAST_ONE_MEMBER_OF,
                      Integer.valueOf(ID_BASE_AT_LEAST_ONE_MEMBER_OF));
            idMap.put(baseName + NAME_BASE_SUBSET,
                      Integer.valueOf(ID_BASE_SUBSET));
            idMap.put(baseName + NAME_BASE_SET_EQUALS,
                      Integer.valueOf(ID_BASE_SET_EQUALS));

            typeMap.put(baseName + NAME_BASE_AT_LEAST_ONE_MEMBER_OF, baseType);
            typeMap.put(baseName + NAME_BASE_SUBSET, baseType);
            typeMap.put(baseName + NAME_BASE_SET_EQUALS, baseType);
        }

        supportedIds = Collections.
            unmodifiableSet(new HashSet<String>(idMap.keySet()));

        idMap.put(NAME_BASE_AT_LEAST_ONE_MEMBER_OF,
                  Integer.valueOf(ID_BASE_AT_LEAST_ONE_MEMBER_OF));
        idMap.put(NAME_BASE_SUBSET, Integer.valueOf(ID_BASE_SUBSET));
        idMap.put(NAME_BASE_SET_EQUALS, Integer.valueOf(ID_BASE_SET_EQUALS));
    };
    
    /**
     * Constructor that is used to create one of the condition standard
     * set functions. The name supplied must be one of the standard XACML
     * functions supported by this class, including the full namespace,
     * otherwise an exception is thrown. Look in <code>SetFunction</code>
     * for details about the supported names.
     *
     * @param functionName the name of the function to create
     *
     * @throws IllegalArgumentException if the function is unknown
     */
    public ConditionSetFunction(String functionName) {
        super(functionName, getId(functionName), getArgumentType(functionName),
              BooleanAttribute.identifier, false);
    }

    /**
     * Constructor that is used to create instances of condition set
     * functions for new (non-standard) datatypes. This is equivalent to
     * using the <code>getInstance</code> methods in <code>SetFunction</code>
     * and is generally only used by the run-time configuration code.
     *
     * @param functionName the name of the new function
     * @param datatype the full identifier for the supported datatype
     * @param functionType which kind of Set function, based on the
     *                     <code>NAME_BASE_*</code> fields
     */
    public ConditionSetFunction(String functionName, String datatype,
                                String functionType) {
        super(functionName, getId(functionType), datatype,
              BooleanAttribute.identifier, false);
    }

    /**
     * Private helper that returns the internal identifier used for the
     * given standard function.
     */
    private static int getId(String functionName) {
        Integer id = (Integer)(idMap.get(functionName));

        if (id == null)
            throw new IllegalArgumentException("unknown set function " +
                                               functionName);

        return id.intValue();
    }

    /**
     * Private helper that returns the argument type for the given standard
     * function. Note that this doesn't check on the return value since the
     * method always is called after getId, so we assume that the function
     * is present.
     */
    private static String getArgumentType(String functionName) {
        return (String)(typeMap.get(functionName));
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set<String> getSupportedIdentifiers() {
        return supportedIds;
    }

    /**
     * Evaluates the function, using the specified parameters.
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
        EvaluationResult evalResult = evalArgs(inputs, context, argValues);
        if (evalResult != null)
            return evalResult;

        // setup the two bags we'll be using
        BagAttribute [] bags = new BagAttribute[2];
        bags[0] = (BagAttribute)(argValues[0]);
        bags[1] = (BagAttribute)(argValues[1]);

        AttributeValue result = null;
        
        switch(Integer.parseInt(getFunctionId())) {
            // *-at-least-one-member-of takes two bags of the same type and
            // returns a boolean
        case ID_BASE_AT_LEAST_ONE_MEMBER_OF:
            // true if at least one element in the first argument is in the
            // second argument (using the *-is-in semantics)

            result = BooleanAttribute.getFalseInstance();
            Iterator<?> it = bags[0].iterator();

            while (it.hasNext()) {
                if (bags[1].contains((AttributeValue)(it.next()))) {
                    result = BooleanAttribute.getTrueInstance();
                    break;
                }
            }
            
            break;

            // *-set-equals takes two bags of the same type and returns
            // a boolean
        case ID_BASE_SUBSET:
            // returns true if the first argument is a subset of the second
            // argument (ie, all the elements in the first bag appear in
            // the second bag) ... ignore all duplicate values in both
            // input bags

            boolean subset = bags[1].containsAll(bags[0]);
            result = BooleanAttribute.getInstance(subset);
            
            break;

            // *-set-equals takes two bags of the same type and returns
            // a boolean
        case ID_BASE_SET_EQUALS:

            // returns true if the two inputs contain the same elements
            // discounting any duplicates in either input ... this is the same
            // as applying the and function on the subset function with
            // the two inputs, and then the two inputs reversed (ie, are the
            // two inputs subsets of each other)

            boolean equals = (bags[1].containsAll(bags[0]) &&
                              bags[0].containsAll(bags[1]));
            result = BooleanAttribute.getInstance(equals);

            break;
        }
        
        return new EvaluationResult(result);
    }

}
