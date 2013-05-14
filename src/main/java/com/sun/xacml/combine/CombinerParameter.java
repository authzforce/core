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
package com.sun.xacml.combine;

import java.io.OutputStream;
import java.io.PrintStream;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;

import org.w3c.dom.Node;

import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.xacmlv3.AttributeValue;


/**
 * Represents a single named parameter to a combining algorithm. Parameters
 * are only used by XACML 2.0 and later policies.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class CombinerParameter extends CombinerParametersType
{

    // the name of this parameter
    private String name;

    // the value of this parameter
    private AttributeValue value;

    /**
     * Creates a new CombinerParameter.
     *
     * @param name the parameter's name
     * @param value the parameter's value
     */
    public CombinerParameter(String name, AttributeValue value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns a new instance of the <code>CombinerParameter</code> class
     * based on a DOM node. The node must be the root of an XML
     * CombinerParameterType.
     *
     * @param root the DOM root of a CombinerParameterType XML type
     *
     * @throws ParsingException if the CombinerParameterType is invalid
     */
    public static CombinerParameter getInstance(Node root)
        throws ParsingException
    {
        // get the name, which is a required attribute
        String name = root.getAttributes().getNamedItem("ParameterName").
            getNodeValue();

        // get the attribute value, the only child of this element
        AttributeFactory attrFactory = AttributeFactory.getInstance();
        AttributeValue value = null;

        try {
            value = attrFactory.createValue(root.getFirstChild());
        } catch (UnknownIdentifierException uie) {
            throw new ParsingException("Unknown AttributeId", uie);
        }
        
        return new CombinerParameter(name, value);
    }

    /**
     * Returns the name of this parameter.
     *
     * @return the name of this parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value provided by this parameter.
     *
     * @return the value provided by this parameter
     */
    public AttributeValue getValue() {
        return value;
    }

    /**
     * Encodes this parameter into its  XML representation and writes this
     * encoding to the given <code>OutputStream</code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();

        out.println(indent + "<CombinerParameter ParameterName=\"" +
                    getName() + "\">");
        indenter.in();

        getValue().encode(output, indenter);

        out.println(indent + "</CombinerParameter>");
        indenter.out();
    }

}
