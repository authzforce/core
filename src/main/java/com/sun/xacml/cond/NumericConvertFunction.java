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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


/**
 * A class that implements all the numeric type conversion functions
 * (double-to-integer and integer-to-double). It takes one argument
 * of the appropriate type, converts that argument to the other type,
 * and returns the result. If the argument is indeterminate, an
 * indeterminate result is returned.
 *
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public class NumericConvertFunction extends FunctionBase
{

    /**
     * Standard identifier for the double-to-integer function.
     */
    public static final String NAME_DOUBLE_TO_INTEGER =
        FUNCTION_NS + "double-to-integer";

    /**
     * Standard identifier for the integer-to-double function.
     */
    public static final String NAME_INTEGER_TO_DOUBLE =
        FUNCTION_NS + "integer-to-double";

    // private identifiers for the supported functions
    private static final int ID_DOUBLE_TO_INTEGER = 0;
    private static final int ID_INTEGER_TO_DOUBLE = 1;

    /**
     * Creates a new <code>NumericConvertFunction</code> object.
     *
     * @param functionName the standard XACML name of the function to be
     *                     handled by this object, including the full namespace
     *
     * @throws IllegalArgumentException if the function is unknwon
     */
    public NumericConvertFunction(String functionName) {
        super(functionName, getId(functionName), getArgumentType(functionName),
              false, 1, getReturnType(functionName), false);
    }

    /**
     * Private helper that returns the internal identifier used for the
     * given standard function.
     */
    private static int getId(String functionName) {
        if (functionName.equals(NAME_DOUBLE_TO_INTEGER))
            return ID_DOUBLE_TO_INTEGER;
        else if (functionName.equals(NAME_INTEGER_TO_DOUBLE))
            return ID_INTEGER_TO_DOUBLE;
        else
            throw new IllegalArgumentException("unknown convert function " +
                                               functionName);
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set getSupportedIdentifiers() {
        Set set = new HashSet();

        set.add(NAME_DOUBLE_TO_INTEGER);
        set.add(NAME_INTEGER_TO_DOUBLE);

        return set;
    }

    /**
     * Private helper that returns the type used for the given standard
     * function. Note that this doesn't check on the return value since the
     * method always is called after getId, so we assume that the function
     * is present.
     */
    private static String getArgumentType(String functionName) {
        if (functionName.equals(NAME_DOUBLE_TO_INTEGER))
            return DoubleAttribute.identifier;
        else
            return IntegerAttribute.identifier;
    }

    /**
     * Private helper that returns the return type for the given standard
     * function. Note that this doesn't check on the return value since the
     * method always is called after getId, so we assume that the function
     * is present.
     */
    private static String getReturnType(String functionName) {
        if (functionName.equals(NAME_DOUBLE_TO_INTEGER))
            return IntegerAttribute.identifier;
        else
            return DoubleAttribute.identifier;
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

        // Now that we have real values, perform the numeric conversion
        // operation in the manner appropriate for this function.
        switch (getId(getFunctionName())) {
        case ID_DOUBLE_TO_INTEGER: {
            double arg0 = ((DoubleAttribute) argValues[0]).getValue();
            long longValue = (long) arg0;

            result = new EvaluationResult(new IntegerAttribute(longValue));
            break;
        }
        case ID_INTEGER_TO_DOUBLE: {
            long arg0 = ((IntegerAttribute) argValues[0]).getValue();
            double doubleValue = (double) arg0;

            result = new EvaluationResult(new DoubleAttribute(doubleValue));
            break;
        }
        }

        return result;
    }
}
