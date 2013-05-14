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
 * A class that implements all the *-abs functions. It takes one
 * operand of the appropriate type and returns the absolute value of the
 * operand. If the operand is indeterminate, an indeterminate result
 * is returned.
 *
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public class AbsFunction extends FunctionBase
{

    /**
     * Standard identifier for the integer-abs function.
     */
    public static final String NAME_INTEGER_ABS =
        FUNCTION_NS + "integer-abs";

    /**
     * Standard identifier for the double-abs function.
     */
    public static final String NAME_DOUBLE_ABS =
        FUNCTION_NS + "double-abs";

    // inernal identifiers for each of the supported functions
    private static final int ID_INTEGER_ABS = 0;
    private static final int ID_DOUBLE_ABS = 1;

    /**
     * Creates a new <code>AbsFunction</code> object.
     *
     * @param functionName the standard XACML name of the function to be
     *                     handled by this object, including the full namespace
     *
     * @throws IllegalArgumentException if the function is known
     */
    public AbsFunction(String functionName) {
        super(functionName, getId(functionName), getArgumentType(functionName),
              false, 1, getArgumentType(functionName), false);
    }

    /**
     * Private helper that returns the internal identifier used for the
     * given standard function.
     */
    private static int getId(String functionName) {
        if (functionName.equals(NAME_INTEGER_ABS))
            return ID_INTEGER_ABS;
        else if (functionName.equals(NAME_DOUBLE_ABS))
            return ID_DOUBLE_ABS;
        else
            throw new IllegalArgumentException("unknown abs function " +
                                               functionName);
    }

    /**
     * Private helper that returns the type used for the given standard
     * function. Note that this doesn't check on the return value since the
     * method always is called after getId, so we assume that the function
     * is present.
     */
    private static String getArgumentType(String functionName) {
        if (functionName.equals(NAME_INTEGER_ABS))
            return IntegerAttribute.identifier;
        else
            return DoubleAttribute.identifier;
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set getSupportedIdentifiers() {
        Set set = new HashSet();

        set.add(NAME_INTEGER_ABS);
        set.add(NAME_DOUBLE_ABS);

        return set;
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

        // evaluate the inputs, returning any error that may occur
        AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null)
            return result;

        // Now that we have real values, perform the abs operation
        // in the manner appropriate for the type of the arguments.
        switch (getId(getFunctionName())) {
        case ID_INTEGER_ABS: {
            long arg = ((IntegerAttribute) argValues[0]).getValue();
            long absValue = Math.abs(arg);

            result = new EvaluationResult(new IntegerAttribute(absValue));
            break;
        }
        case ID_DOUBLE_ABS: {
            double arg = ((DoubleAttribute) argValues[0]).getValue();
            double absValue = Math.abs(arg);

            result = new EvaluationResult(new DoubleAttribute(absValue));
            break;
        }
        }

        return result;
    }
}
