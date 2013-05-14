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

import com.sun.xacml.ctx.Result;

import java.io.OutputStream;

import java.net.URI;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;


/**
 * This represents a single node in a policy tree. A node is either a policy
 * set, a policy, or a rule. This interface is used to interact with these
 * node types in a general way. Note that rules are leaf nodes in a policy
 * tree as they never contain children.
 *
 * @since 1.1
 * @author seth proctor
 */
public abstract class PolicyTreeElement extends PolicyType
{

    /**
     * Returns the <code>List</code> of <code>PolicyTreeElement</code> objects
     * that are the children of this node. If this node has no children then
     * this list is empty. The children are returned as a <code>List</code>
     * instead of some unordered collection because in cases like combining
     * or evaluation the order is often important.
     *
     * @return the non-null <code>List</code> of children of this node
     */
    public abstract List getChildren();

    /**
     * Returns the given description of this element or null if 
     * there is no description
     *
     * @return the description or null
     */
    public abstract String getDescription();

    /**
     * Returns the id of this element
     *
     * @return the element's identifier
     */
    public abstract URI getId();

    /**
     * Returns the target for this element or null if there
     * is no target
     *
     * @return the element's target
     */
    public abstract TargetType getTarget();

    /**
     * Given the input context sees whether or not the request matches this
     * element's target. The rules for matching are different depending on
     * the type of element being matched.
     *
     * @param context the representation of the request
     *
     * @return the result of trying to match this element and the request
     */
    public abstract MatchResult match(EvaluationCtx context);

    /**
     * Evaluates this element in the policy tree, and therefore all elements
     * underneath this element. The rules for evaluation are different
     * depending on the type of element being evaluated.
     *
     * @param context the representation of the request we're evaluating
     *
     * @return the result of the evaluation
     */
    public abstract Result evaluate(EvaluationCtx context);

    /**
     * Encodes this element into its XML representation and writes
     * this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public abstract void encode(OutputStream output);

    /**
     * Encodes this element into its XML representation and writes
     * this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public abstract void encode(OutputStream output, Indenter indenter);

}
