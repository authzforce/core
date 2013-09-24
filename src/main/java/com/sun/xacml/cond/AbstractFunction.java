/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.cond;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 *
 * @author najmi
 */
public abstract class AbstractFunction extends Function {

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
                                        List<AttributeValueType> paramValues) {
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
