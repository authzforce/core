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
import java.util.List;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


/**
 * A class that implements the round function. It takes one double
 * operand, rounds that value to an integer and returns that integer.
 * If the operand is indeterminate, an indeterminate result is returned.
 *
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public class RoundFunction extends FunctionBase
{

    /**
     * Standard identifier for the round function.
     */
    public static final String NAME_ROUND = FUNCTION_NS + "round";

    /**
     * Creates a new <code>RoundFunction</code> object.
     *
     * @param functionName the standard XACML name of the function to be
     *                     handled by this object, including the full namespace
     *
     * @throws IllegalArgumentException if the function is unknown
     */
    public RoundFunction(String functionName) {
        super(NAME_ROUND, 0, DoubleAttribute.identifier, false, 1,
              DoubleAttribute.identifier, false);

        if (! functionName.equals(NAME_ROUND))
            throw new IllegalArgumentException("unknown round function: "
                                               + functionName);
    }

    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set getSupportedIdentifiers() {
        Set set = new HashSet();

        set.add(NAME_ROUND);

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

        // Evaluate the arguments
        AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null)
            return result;

        // Now that we have real values, perform the round operation
        double arg = ((DoubleAttribute) argValues[0]).getValue();
        double roundValue = Math.round(arg);
        
        // Make it round half even, not round nearest
        double lower = Math.floor(arg);
        double higher = lower + 1;

        if ((arg - lower) == (higher - arg)) {
            if ((lower % 2) == 0)
                roundValue = lower;
            else
                roundValue = higher;
        }
        
        return new EvaluationResult(new DoubleAttribute(roundValue));
    }
}
