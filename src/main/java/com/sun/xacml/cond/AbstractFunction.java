/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.cond;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.attr.AttributeValue;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

/**
 *
 * @author najmi
 */
public abstract class AbstractFunction implements Function {

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
                                        List<AttributeValue> paramValues) {
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
