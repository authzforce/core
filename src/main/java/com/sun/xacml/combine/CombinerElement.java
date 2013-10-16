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
package com.sun.xacml.combine;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

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
    public CombinerElement(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy element) {
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
    public CombinerElement(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy element, List parameters) {
        this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
        }
    }
    
    public CombinerElement(PolicySet element, List parameters) {
        this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
        }
    }
    
    public CombinerElement(oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule element, List parameters) {
    	this.element = element;

        if (parameters == null) {
            this.parameters = Collections.unmodifiableList(new ArrayList());
            this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.unmodifiableList(new ArrayList());
        } else {
            this.parameters = Collections.
                unmodifiableList(new ArrayList(parameters));
            this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.unmodifiableList(parameters);
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
