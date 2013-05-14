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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


/**
 * A class that implements the logical functions "or" and "and".
 * These functions take any number of boolean arguments and evaluate
 * them one at a time, starting with the first argument. As soon as
 * the result of the function can be determined, evaluation stops and
 * that result is returned. During this process, if any argument
 * evaluates to indeterminate, an indeterminate result is returned.
 *
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public class LogicalFunction extends FunctionBase
{

    /**
     * Standard identifier for the or function.
     */
    public static final String NAME_OR = FUNCTION_NS + "or";

    /**
     * Standard identifier for the and function.
     */
    public static final String NAME_AND = FUNCTION_NS + "and";

    // internal identifiers for each of the supported functions
    private static final int ID_OR = 0;
    private static final int ID_AND = 1;

    /**
     * Creates a new <code>LogicalFunction</code> object.
     *
     * @param functionName the standard XACML name of the function to be
     *                     handled by this object, including the full namespace
     *
     * @throws IllegalArgumentException if the functionName is unknown
     */
    public LogicalFunction(String functionName) {
        super(functionName, getId(functionName), BooleanAttribute.identifier,
              false, -1, BooleanAttribute.identifier, false);
    }

    /**
     * Private helper that looks up the private id based on the function name.
     */
    private static int getId(String functionName) {
        if (functionName.equals(NAME_OR))
            return ID_OR;
        else if (functionName.equals(NAME_AND))
            return ID_AND;
        else
            throw new IllegalArgumentException("unknown logical function: " +
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

        set.add(NAME_OR);
        set.add(NAME_AND);

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

        // Evaluate the arguments one by one. As soon as we can
        // return a result, do so. Return Indeterminate if any argument
        // evaluated is indeterminate.
        Iterator it = inputs.iterator();
        while (it.hasNext()) {
            Evaluatable eval = (Evaluatable)(it.next());

            // Evaluate the argument
            EvaluationResult result = eval.evaluate(context);
            if (result.indeterminate())
                return result;

            AttributeValueType value = result.getAttributeValue();
            boolean argBooleanValue = ((BooleanAttribute)value).getValue();

            switch (getId(getFunctionName())) {
            case ID_OR:
                if (argBooleanValue)
                    return EvaluationResult.getTrueInstance();
                break;
            case ID_AND:
                if (!argBooleanValue)
                    return EvaluationResult.getFalseInstance();
                break;
            }
        }

        if (Integer.parseInt(getFunctionId()) == ID_OR)
            return EvaluationResult.getFalseInstance();
        else
            return EvaluationResult.getTrueInstance();
    }
}
