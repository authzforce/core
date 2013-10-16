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
import java.io.PrintStream;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.cond.xacmlv3.Expression;
import com.sun.xacml.cond.xacmlv3.ExpressionTools;


/**
 * This class supports the VariableDefinitionType type introuced in XACML
 * 2.0. It allows a Policy to pre-define any number of expression blocks for
 * general use. Note that it's legal (though not usually useful) to define
 * expressions that don't get referenced within the Policy. It is illegal to
 * have more than one definition with the same identifier within a Policy.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class VariableDefinition extends oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition
{

    // the identitifer for this definition
    private String variableId;

    // the actual expression defined here
    private Expression expression;

    /**
     * Creates a new <code>VariableDefinition</code> with the given
     * identifier and expression.
     *
     * @param variableId the identifier for this definition
     * @param expression the expression defined here
     */
    public VariableDefinition(String variableId, ExpressionType expression) {
        this.variableId = variableId;
        this.expression = (Expression)expression;
    }

    /**
     * Returns a new instance of the <code>VariableDefinition</code> class
     * based on a DOM node. The node must be the root of an XML
     * VariableDefinitionType.
     *
     * @param root the DOM root of a VariableDefinitionType XML type
     * @param metaData the meta-data associated with the containing policy
     * @param manager <code>VariableManager</code> used to connect references
     *                to this definition
     *
     * @throws ParsingException if the VariableDefinitionType is invalid
     */
    public static VariableDefinition getInstance(Node root,
                                                 PolicyMetaData metaData,
                                                 VariableManager manager)
        throws ParsingException
    {
        String variableId = root.getAttributes().getNamedItem("VariableId").
            getNodeValue();

        // get the first element, which is the expression node
        NodeList nodes = root.getChildNodes();
        Node xprNode = nodes.item(0);
        int i = 1;
        while (xprNode.getNodeType() != Node.ELEMENT_NODE)
            xprNode = nodes.item(i++);

        // use that node to get the expression
        ExpressionType xpr = (Expression)ExpressionTools.
            getExpression(xprNode, metaData, manager);

        return new VariableDefinition(variableId, xpr);
    }

    /**
     * Returns the identifier for this definition.
     *
     * @return the definition's identifier
     */
    public String getVariableId() {
        return variableId;
    }

    /**
     * Returns the expression provided by this definition.
     *
     * @return the definition's expression
     */
//    public Expression getExpression() {
//        return expression;
//    }
    
    /**
     * Encodes this class into its XML representation and writes this
     * encoding to the given <code>OutputStream</code> with no indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this class into its XML representation and  writes this
     * encoding to the given <code>OutputStream</code> with  indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();

        out.println(indent + "<VariableDefinition VariableId=\"" +
                    variableId + "\">");
        indenter.in();

        expression.encode(output, indenter);

        out.println("</VariableDefinition>");
        indenter.out();
    }
}
