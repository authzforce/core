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
package com.sun.xacml.ctx;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.parser.ParserException;

import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;


/**
 * Represents the response to a request made to the XACML PDP.
 *
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public class ResponseCtx
{

    // The set of Result objects returned by the PDP
    private Set<ResultType> results = null;

    /**
     * Constructor that creates a new <code>ResponseCtx</code> with only a
     * single <code>Result</code> (a common case).
     *
     * @param result the single result in the response
     */
    public ResponseCtx(Result result) {
        results = new HashSet();
        results.add(result);
    }
    
    /**
     * Constructor that creates a new <code>ResponseCtx</code> with a
     * <code>Set</code> of <code>Result</code>s. The <code>Set</code> must
     * be non-empty.
     *
     * @param results a <code>Set</code> of <code>Result</code> objects
     */
    public ResponseCtx(Set results) {
        this.results = Collections.unmodifiableSet(new HashSet(results));
    }

    /**
     * Creates a new instance of <code>ResponseCtx</code> based on the given
     * DOM root node. A <code>ParsingException</code> is thrown if the DOM
     * root doesn't represent a valid ResponseType.
     *
     * @param root the DOM root of a ResponseType
     *
     * @return a new <code>ResponseCtx</code>
     *
     * @throws ParsingException if the node is invalid
     */
    public static ResponseCtx getInstance(Node root) throws ParsingException {
        Set results = new HashSet();
        
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("Result")) {
                results.add(Result.getInstance(node));
            }
        }

        if (results.size() == 0)
            throw new ParsingException("must have at least one Result");

        return new ResponseCtx(results);
    }

    /**
     * Creates a new <code>ResponseCtx</code> by parsing XML from an
     * input stream. Note that this is a convenience method, and it will
     * not do schema validation by default. You should be parsing the data
     * yourself, and then providing the root node to the other
     * <code>getInstance</code> method. If you use this convenience
     * method, you probably want to turn on validation by setting the
     * context schema file (see the programmer guide for more information
     * on this).
     *
     * @param input a stream providing the XML data
     *
     * @return a new <code>ResponseCtx</code>
     *
     * @throws ParserException if there is an error parsing the input
     */
    public static ResponseCtx getInstance(InputStream input)
        throws ParsingException
    {
        return getInstance(InputParser.parseInput(input, "Response"));
    }

    /**
     * Get the set of <code>Result</code>s from this response.
     * 
     * @return a <code>Set</code> of results
     */
    public Set<ResultType> getResults() {
        return results;
    }

    /**
     * Encodes this context into its XML representation and writes this
     * encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this context into its XML representation and writes
     * this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {

        // Make a PrintStream for a nicer printing interface
        PrintStream out = new PrintStream(output);

        // Prepare the indentation string
        String indent = indenter.makeString();

        // Now write the XML...

        out.println(indent + "<Response>");

        // Go through all results
        Iterator it = results.iterator();
        indenter.in();

        while (it.hasNext()) {
            Result result = (Result)(it.next());
            result.encode(out, indenter);
        }

        indenter.out();

        // Finish the XML for a response
        out.println(indent + "</Response>");

    }
    
    /** 
     * Return encoded context of XML representation
     *
     * @return as String 
     */
    public String getEncoded() {

        OutputStream output = new ByteArrayOutputStream();
        encode(output, new Indenter(0));
        return output.toString();
    } 

}
