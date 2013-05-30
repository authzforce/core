<<<<<<< HEAD

/*
 * @(#)TargetSection.java
 *
 * Copyright 2005-2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

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
package com.sun.xacml;

import com.sun.xacml.ctx.Status;

import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

<<<<<<< HEAD
=======
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

>>>>>>> 3.x
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This is a container class for instances of <code>TargetMatchGroup</code>
 * and represents the Subjects, Resources, Actions, and Environments
 * sections of an XACML Target. This section may apply to any request.
 *
 * @since 2.0
 * @author Seth Proctor
 */
<<<<<<< HEAD
public class TargetSection
{
=======
public class TargetSection {
>>>>>>> 3.x

    // the list of match groups
    private List matchGroups;

<<<<<<< HEAD
    // the match type contained in this group
=======
	// the match type contained in this group
>>>>>>> 3.x
    private int matchType;

    // the version of XACML used by the containing Target
    private int xacmlVersion;

    /**
     * Constructor that takes a group and a version. If the group is
     * null or empty, then this represents a section that matches any request.
     *
     * @param matchGroups a possibly null <code>List</code> of
     *                    <code>TargetMatchGroup</code>s
     * @param matchType the type as defined in <code>TargetMatch</code>
     * @param xacmlVersion the version XACML being used
     */
    public TargetSection(List matchGroups, int matchType, int xacmlVersion) {
<<<<<<< HEAD
        if (matchGroups == null)
            this.matchGroups = Collections.unmodifiableList(new ArrayList());
        else
            this.matchGroups = Collections.
                unmodifiableList(new ArrayList(matchGroups));
=======
        if (matchGroups == null) {
            this.matchGroups = Collections.unmodifiableList(new ArrayList());
        } else {
            this.matchGroups = Collections.
                unmodifiableList(new ArrayList(matchGroups));
        }
>>>>>>> 3.x
        this.matchType = matchType;
        this.xacmlVersion = xacmlVersion;
    }

    /**
     * Creates a <code>Target</code> by parsing a node.
     *
     * @param root the node to parse for the <code>Target</code>
     * @param matchType the type as defined in <code>TargetMatch</code>
     * @param metaData the meta-data from the enclosing policy
     *
     * @return a new <code>Target</code> constructed by parsing
     *
     * @throws ParsingException if the DOM node is invalid
     */
    public static TargetSection getInstance(Node root, int matchType,
                                            PolicyMetaData metaData)
        throws ParsingException
    {
<<<<<<< HEAD
        List groups = new ArrayList();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();
            String typeName = TargetMatch.NAMES[matchType];

            if (name.equals(typeName)) {
                groups.add(TargetMatchGroup.getInstance(child, matchType,
=======
        List<TargetMatchGroup> groups = new ArrayList<TargetMatchGroup>();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            String name = root.getNodeName();
            String typeName = TargetMatch.NAMES[matchType];

            /*
             * FIXME: RF for XACML 3.0
             */
            if (name.equals(typeName) || name.equals(TargetMatch.NAMES[TargetMatch.MATCH]) ) {
                groups.add(TargetMatchGroup.getInstance(root, matchType,
>>>>>>> 3.x
                                                        metaData));
            } else if (name.equals("Any" + typeName)) {
                // in a schema-valid policy, the Any element will always be
                // the only element, so if we find this we stop
                break;
            }
        }

        // at this point the list is non-empty (it has specific groups to
        // match) or is empty (it applies to any request using the 1.x or
        // 2.0 syntax)
        return new TargetSection(groups, matchType,
                                 metaData.getXACMLVersion());
    }

    /**
     * Returns the <code>TargetMatchGroup</code>s contained in this group.
     *
     * @return a <code>List</code> of <code>TargetMatchGroup</code>s
     */
    public List getMatchGroups() {
        return matchGroups;
    }

    /**
     * Returns whether this section matches any request.
     *
     * @return true if this section matches any request, false otherwise
     */
    public boolean matchesAny() {
        return matchGroups.isEmpty();
    }
<<<<<<< HEAD
=======
    
    /**
	 * @return the matchType
	 */
	public int getMatchType() {
		return matchType;
	}

	/**
	 * @param matchType the matchType to set
	 */
	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}
>>>>>>> 3.x

    /**
     * Determines whether this <code>TargetSection</code> matches
     * the input request (whether it is applicable).
     * 
     * @param context the representation of the request
     *
     * @return the result of trying to match the target and the request
     */
    public MatchResult match(EvaluationCtx context) {
        // if we apply to anything, then we always match
<<<<<<< HEAD
        if (matchGroups.isEmpty())
            return new MatchResult(MatchResult.MATCH);
=======
        if (matchGroups.isEmpty()) {
            return new MatchResult(MatchResult.MATCH);
        }
>>>>>>> 3.x

        // there are specific matching elements, so prepare to iterate
        // through the list
        Iterator it = matchGroups.iterator();
        Status firstIndeterminateStatus = null;

        // in order for this section to match, one of the groups must match 
        while (it.hasNext()) {
            // get the next group and try matching it
            TargetMatchGroup group = (TargetMatchGroup)(it.next());
            MatchResult result = group.match(context);

            // we only need one match, so if this matched, then we're done
<<<<<<< HEAD
            if (result.getResult() == MatchResult.MATCH)
                return result;
=======
            if (result.getResult() == MatchResult.MATCH) {
                return result;
            }
>>>>>>> 3.x

            // if we didn't match then it was either a NO_MATCH or
            // INDETERMINATE...in the second case, we need to remember
            // it happened, 'cause if we don't get a MATCH, then we'll
            // be returning INDETERMINATE
            if (result.getResult() == MatchResult.INDETERMINATE) {
<<<<<<< HEAD
                if (firstIndeterminateStatus == null)
                    firstIndeterminateStatus = result.getStatus();
=======
                if (firstIndeterminateStatus == null) {
                    firstIndeterminateStatus = result.getStatus();
                }
>>>>>>> 3.x
            }
        }

        // if we got here, then none of the sub-matches passed, so
        // we have to see if we got any INDETERMINATE cases
<<<<<<< HEAD
        if (firstIndeterminateStatus == null)
            return new MatchResult(MatchResult.NO_MATCH);
        else
            return new MatchResult(MatchResult.INDETERMINATE,
                                   firstIndeterminateStatus);
=======
        if (firstIndeterminateStatus == null) {
            return new MatchResult(MatchResult.NO_MATCH);
        } else {
            return new MatchResult(MatchResult.INDETERMINATE,
                                   firstIndeterminateStatus);
        }
>>>>>>> 3.x
    }

    /**
     * Encodes this <code>TargetSection</code> into its XML representation
     * and writes  this encoding to the given <code>OutputStream</code> with
     * no indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>TargetSection</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();
        String name = TargetMatch.NAMES[matchType];
        
        // figure out if this section applies to any request
        if (matchGroups.isEmpty()) {
            // this applies to any, so now we need to encode it based on
            // what version of XACML we're using...in 2.0, we encode an Any
            // by simply omitting the element, so we'll only actually include
            // something if this is a 1.x policy
            if (xacmlVersion == PolicyMetaData.XACML_VERSION_1_0) {
                out.println(indent + "<" + name + "s>");
                indenter.in();
                out.println(indenter.makeString() + "<Any" + name + "/>");
                indenter.out();
                out.println(indent + "</" + name + "s>");
            }
        } else {
            // this has specific rules, so we can now encode them
            out.println(indent + "<" + name + "s>");

            Iterator it = matchGroups.iterator();
            indenter.in();
            while (it.hasNext()) {
                TargetMatchGroup group = (TargetMatchGroup)(it.next());
                group.encode(output, indenter);
            }
            indenter.out();

            out.println(indent + "</" + name + "s>");
        }
    }
<<<<<<< HEAD
=======

	public static TargetSection getInstance(AttributeDesignatorType instance) {
		// TODO Auto-generated method stub
		return null;
	}
>>>>>>> 3.x
    
}
