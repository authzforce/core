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
package com.sun.xacml;

import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;


/**
 * This class contains a group of <code>TargetMatch</code> instances and
 * represents the Subject, Resource, Action, and Environment elements in
 * an XACML Target.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class TargetMatchGroup
{

    // the list of matches
    private List matches;

    // the match type contained in this group
    private int matchType;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(Target.class.getName());

    /**
     * Constructor that creates a new <code>TargetMatchGroup</code> based
     * on the given elements.
     *
     * @param matchElements a <code>List</code> of <code>TargetMatch</code>
     * @param matchType the match type as defined in <code>TargetMatch</code>
     */
    public TargetMatchGroup(List matchElements, int matchType) {
        if (matchElements == null)
            matches = Collections.unmodifiableList(new ArrayList());
        else
            matches =
                Collections.unmodifiableList(new ArrayList(matchElements));
        this.matchType = matchType;
    }

    /**
     * Creates a <code>Target</code> based on its DOM node.
     *
     * @param root the node to parse for the target group
     * @param matchType the type of the match
     * @param metaData meta-date associated with the policy
     *
     * @return a new <code>TargetMatchGroup</code> constructed by parsing
     *
     * @throws ParsingException if the DOM node is invalid
     */
    public static TargetMatchGroup getInstance(Node root, int matchType,
                                               PolicyMetaData metaData)
        throws ParsingException
    {
    	List matches = new ArrayList();
        
        /*
         * XACML 3.0 hook
         */
        if (PolicyMetaData.XACML_VERSION_3_0 == metaData.getXACMLVersion()) {        	
        	NodeList myRoot = (NodeList)root;
        	String name = DOMHelper.getLocalName(root);
            if (name.equals(TargetMatch.NAMES[TargetMatch.MATCH])) {
                matches.add(TargetMatch.getInstance(root, matchType, metaData));
            } 
        } else {
            NodeList children = root.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            String name = child.getNodeName();
	
	            if (name.equals(TargetMatch.NAMES[matchType] + "Match")) {
	                matches.add(TargetMatch.getInstance(child, matchType,
	                                                    metaData));
	            } 
	        }
        }

        return new TargetMatchGroup(matches, matchType);
    }

    /**
     * Determines whether this <code>TargetMatchGroup</code> matches
     * the input request (whether it is applicable). 
     * 
     * @param context the representation of the request
     *
     * @return the result of trying to match the group with the context
     */
    public MatchResult match(EvaluationCtx context) {
        Iterator it = matches.iterator();
        MatchResult result = null;

        while (it.hasNext()) {
            TargetMatch tm = (TargetMatch)(it.next());
            result = tm.match(context);
            if (result.getResult() != MatchResult.MATCH)
                break;
        }

        return result;
    }

    /**
     * Encodes this <code>TargetMatchGroup</code> into its XML representation
     * and writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>TargetMatchGroup</code> into its XML representation
     * and writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();
        Iterator it = matches.iterator();
        String name = TargetMatch.NAMES[matchType];

        out.println(indent + "<" + name + ">");
        indenter.in();
        
        while (it.hasNext()) {
            TargetMatch tm = (TargetMatch)(it.next());
            tm.encode(output, indenter);
        }
        
        out.println(indent + "</" + name + ">");
        indenter.out();
    }

}
