<<<<<<< HEAD

/*
 * @(#)CombinerElement.java
 *
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.xacml.combine;

import com.sun.xacml.Indenter;
import com.sun.xacml.PolicyTreeElement;

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
package com.sun.xacml.combine;

import java.io.OutputStream;
>>>>>>> 3.x
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

<<<<<<< HEAD
import java.io.OutputStream;
=======
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;

import com.sun.xacml.Indenter;
import com.sun.xacml.PolicyTreeElement;
import com.sun.xacml.Rule;
import com.sun.xacml.xacmlv3.Policy;
>>>>>>> 3.x


/**
 * Represents one input (a Rule, Policy, PolicySet, or reference) to a
 * combining algorithm and combiner parameters associated with that input.
 *
 * @since 2.0
 * @author Seth Proctor
 */
<<<<<<< HEAD
public abstract class CombinerElement
{

    // the element to be combined
    private PolicyTreeElement element;
=======
public abstract class CombinerElement extends PolicyTreeElement
{

    // the element to be combined
    private Object element;
>>>>>>> 3.x

    // the parameters used with this element
    private List parameters;

    /**
     * Constructor that only takes an element. No parameters are associated
     * with this element when combining.
     *
     * @param element a <code>PolicyTreeElement</code> to use in combining
     */
<<<<<<< HEAD
    public CombinerElement(PolicyTreeElement element) {
=======
    public CombinerElement(PolicyType element) {
        this(element, null);
    }
    
    public CombinerElement(Rule element) {
>>>>>>> 3.x
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
<<<<<<< HEAD
    public CombinerElement(PolicyTreeElement element, List parameters) {
        this.element = element;

        if (parameters == null)
            this.parameters = Collections.unmodifiableList(new ArrayList());
        else
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
=======
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
>>>>>>> 3.x
    }

    /**
     * Returns the <code>PolicyTreeElement</code> in this element.
     *
     * @return the <code>PolicyTreeElement</code>
     */
<<<<<<< HEAD
    public PolicyTreeElement getElement() {
=======
    public Object getElement() {
>>>>>>> 3.x
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
