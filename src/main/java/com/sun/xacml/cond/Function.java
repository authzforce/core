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

import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * Interface that all functions in the system must implement.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class Function extends FunctionType
{

    /**
     * Evaluates the <code>Function</code> using the given inputs.
     * The <code>List</code> contains <code>Evaluatable<code>s which are all
     * of the correct type if the <code>Function</code> has been created as
     * part of an <code>Apply</code> or <code>TargetMatch</code>, but which
     * may otherwise be invalid. Each parameter should be evaluated by the
     * <code>Function</code>, unless the <code>Function</code>
     * doesn't need to evaluate all inputs to determine a result (as in the
     * case of the or function). The order of the <code>List</code> is
     * significant, so a <code>Function</code> should have a very good reason
     * if it wants to evaluate the inputs in a different order.
     * <p>
     * Note that if this is a higher-order function, like any-of, then
     * some argument (typically the first) in the <code>List</code> will
     * actually be a Function object representing the function to apply to
     * some bag. A function needs to know if it's a higher-order function,
     * and therefore whether or not to look for this case. Also, a
     * higher-order function is responsible for checking that the inputs
     * that it will pass to the <code>Function</code> provided as the first
     * parameter are valid, ie. it must do a <code>checkInputs</code> on
     * its sub-function when <code>checkInputs</code> is called on the
     * higher-order function.
     *
     * @param expression the <code>List</code> of inputs for the function
     * @param context the representation of the request
     *
     * @return a result containing the <code>AttributeValue</code> computed
     *         when evaluating the function, or <code>Status</code>
     *         specifying some error condition
     */
    public abstract EvaluationResult evaluate(List<JAXBElement<? extends ExpressionType>> expression, EvaluationCtx context);

    /**
     * Returns the identifier of this function as known by the factories.
     * In the case of the standard XACML functions, this will be one of the
     * URIs defined in the standard namespace. This function must always
     * return the complete namespace and identifier of this function.
     *
     * @return the function's identifier
     */
    public abstract URI getIdentifier();

    /**
     * Provides the type of <code>AttributeValue</code> that this function
     * returns from <code>evaluate</code> in a successful evaluation.
     *
     * @return the type returned by this function
     */
    public abstract URI getReturnType();

    /**
     * Tells whether this function will return a bag of values or just a
     * single value.
     *
     * @return true if evaluation will return a bag, false otherwise
     */
    public abstract boolean returnsBag();

    /**
     * Checks that the given inputs are of the right types, in the right
     * order, and are the right number for this function to evaluate. If
     * the function cannot accept the inputs for evaluation, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param inputs a <code>List</code> of <code>Evaluatable</code>s, with
     *               the first argument being a <code>Function</code> if
     *               this is a higher-order function
     *
     * @throws IllegalArgumentException if the inputs do match what the
     *                                  function accepts for evaluation
     */
    public abstract void checkInputs(List<ExpressionType> inputs) throws IllegalArgumentException;

    /**
     * Checks that the given inputs are of the right types, in the right
     * order, and are the right number for this function to evaluate. If
     * the function cannot accept the inputs for evaluation, an
     * <code>IllegalArgumentException</code> is thrown. Unlike the other
     * <code>checkInput</code> method in this interface, this assumes that
     * the parameters will never provide bags of values. This is useful if
     * you're considering a target function which has a designator or
     * selector in its input list, but which passes the values from the
     * derived bags one at a time to the function, so the function doesn't
     * have to deal with the bags that the selector or designator
     * generates.
     *
     * @param inputs a <code>List</code> of <code>Evaluatable</code>s, with
     *               the first argument being a <code>Function</code> if
     *               this is a higher-order function
     *
     * @throws IllegalArgumentException if the inputs do match what the
     *                                  function accepts for evaluation
     */
//    public void checkInputsNoBag(List<Evaluatable> inputs) throws IllegalArgumentException;
    
    public abstract void checkInputsNoBag(List<ExpressionType> inputs) throws IllegalArgumentException;
 
    /**
     * Encodes this <code>Function</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public abstract void encode(OutputStream output);

    /**
     * Encodes this <code>Function</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public abstract void encode(OutputStream output, Indenter indenter);
   
}
