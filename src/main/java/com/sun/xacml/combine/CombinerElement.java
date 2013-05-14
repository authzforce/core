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
package com.sun.xacml.combine;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;

import com.sun.xacml.Indenter;
import com.sun.xacml.PolicyTreeElement;
import com.sun.xacml.Rule;
import com.sun.xacml.xacmlv3.Policy;


/**
 * Represents one input (a Rule, Policy, PolicySet, or reference) to a
 * combining algorithm and combiner parameters associated with that input.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public abstract class CombinerElement extends PolicyTreeElement
{

    // the element to be combined
    private Object element;

    // the parameters used with this element
    private List parameters;

    /**
     * Constructor that only takes an element. No parameters are associated
     * with this element when combining.
     *
     * @param element a <code>PolicyTreeElement</code> to use in combining
     */
    public CombinerElement(PolicyType element) {
        this(element, null);
    }
    
    public CombinerElement(Rule element) {
        this(element, null);
    }

    /**
     * Constructor that takes both the element to combine and its associated
     * combiner parameters.
     *
     * @param element a <code>PolicyTreeElement</code> to use in combining
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s provided for general
     *                   use (for all pre-2.0 policies this must be empty)
     */
    public CombinerElement(PolicyType element, List parameters) {
        this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
        }
    }
    
    public CombinerElement(PolicySetType element, List parameters) {
        this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
        }
    }
    
    public CombinerElement(RuleType element, List parameters) {
    	this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
            this.combinerParametersOrRuleCombinerParametersOrVariableDefinition = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
            this.combinerParametersOrRuleCombinerParametersOrVariableDefinition = Collections.unmodifiableList(parameters);
        }
    }

    /**
     * Returns the <code>PolicyTreeElement</code> in this element.
     *
     * @return the <code>PolicyTreeElement</code>
     */
    public Object getElement() {
        return element;
    }

    /**
     * Returns the <code>CombinerParameter</code>s associated with this
     * element.
     *
     * @return a <code>List</code> of <code>CombinerParameter</code>s
     */
    public List getParameters() {
        return parameters;
    }

    /**
     * Encodes the element and parameters in this <code>CombinerElement</code>
     * into their XML representation and writes this encoding to the given
     * <code>OutputStream</code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public abstract void encode(OutputStream output, Indenter indenter);

}
