<<<<<<< HEAD
=======
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
>>>>>>> 3.x
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.cond;

<<<<<<< HEAD
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.attr.AttributeValue;
=======
>>>>>>> 3.x
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

<<<<<<< HEAD
=======
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

>>>>>>> 3.x
/**
 *
 * @author najmi
 */
<<<<<<< HEAD
public abstract class AbstractFunction implements Function {
=======
public abstract class AbstractFunction extends Function {
>>>>>>> 3.x

    URI identifier;
    URI returnType;
    boolean returnsBag;

    public AbstractFunction(URI identifier, URI returnType, boolean returnsBag) {
        this.identifier = identifier;
        this.returnType = returnType;
        this.returnsBag = returnsBag;
    }

    public URI getIdentifier() {
        return identifier;
    }

    public URI getReturnType() {
        return returnType;
    }

    public boolean returnsBag() {
        return returnsBag;
    }

    /**
     * Encodes this <code>FunctionBase</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>FunctionBase</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        out.println(indenter.makeString() + "<Function FunctionId=\"" +
                    getIdentifier().toString() + "\"/>");
    }

    public URI getType() {
        return getReturnType();
    }

    /**
     * Evaluates each of the parameters, and returns their parameter values
     * If any error occurs, this method
     * returns the error, otherwise null is returned, signalling that
     * evaluation was successful for all inputs, and the resulting argument
     * list can be used.
     *
     * @param params a <code>List</code> of <code>Evaluatable</code>
     *               objects representing the parameters to evaluate
     * @param context the representation of the request
     * @param paramValues the List of parameter values
     *
     * @return <code>null</code> if no errors were encountered, otherwise
     *         an <code>EvaluationResult</code> representing the error
     */
    protected EvaluationResult evalArgs(List<Evaluatable> params, EvaluationCtx context,
<<<<<<< HEAD
                                        List<AttributeValue> paramValues) {
=======
                                        List<AttributeValueType> paramValues) {
>>>>>>> 3.x
        for (Evaluatable param : params) {
            EvaluationResult result = param.evaluate(context);

            // If there was an error, pass it back...
            if (result.indeterminate())
                return result;

            // ...otherwise save it and keep going
            paramValues.add(result.getAttributeValue());
        }

        // if no error occurred then we got here, so we return no errors
        return null;
    }

}
