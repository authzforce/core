<<<<<<< HEAD

/*
 * @(#)PolicyCombinerElement.java
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

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Indenter;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.PolicySet;

import java.io.OutputStream;
import java.io.PrintStream;

import java.util.Iterator;
import java.util.List;

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
import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;

>>>>>>> 3.x

/**
 * Specific version of <code>CombinerElement</code> used for policy combining.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class PolicyCombinerElement extends CombinerElement
{

    /**
     * Constructor that only takes an <code>AbstractPolicy</code. No parameters
     * are associated with this <code>AbstractPolicy</code> when combining.
     *
     * @param policy an <code>AbstractPolicy</code> to use in combining
     */
<<<<<<< HEAD
    public PolicyCombinerElement(AbstractPolicy policy) {
        super(policy);
    }
    
=======
    public PolicyCombinerElement(PolicyType policy) {
        super(policy);
    }
    
    public PolicyCombinerElement(PolicyType policy, List args) {
        super(policy, args);
    }
    
    public PolicyCombinerElement(PolicySetType policy, List args) {
        super(policy, args);
    }
    
>>>>>>> 3.x
    /**
     * Constructor that takes both the <code>AbstractPolicy</code> to combine
     * and its associated combiner parameters.
     *
     * @param policy an <code>AbstractPolicy</code> to use in combining
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s provided for general
     *                   use (for all pre-2.0 policies this must be empty)
     */
    public PolicyCombinerElement(AbstractPolicy policy, List parameters) {
        super(policy, parameters);
    }

<<<<<<< HEAD
    /**
=======
	/**
>>>>>>> 3.x
     * Returns the <code>AbstractPolicy</code> in this element.
     *
     * @return the element's <code>AbstractPolicy</code>
     */
<<<<<<< HEAD
    public AbstractPolicy getPolicy() {
        return (AbstractPolicy)(getElement());
=======
    public Policy getPolicy() {
        return (Policy)getElement();
>>>>>>> 3.x
    }

    /**
     * Encodes this element's <code>AbstractPolicy</code> and parameters into
     * their XML representation and writes this encoding to the given
     * <code>OutputStream</code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
<<<<<<< HEAD
        if (! getParameters().isEmpty()) {
            AbstractPolicy policy = getPolicy();

            // FIXME: This is ugly and happens in several places...maybe this
            // should get folded into the AbstractPolicy API?
            if (policy instanceof Policy) {
                encodeParamaters(output, indenter, "Policy",
                                 policy.getId().toString());
            } else if (policy instanceof PolicySet) {
                encodeParamaters(output, indenter, "PolicySet",
                                 policy.getId().toString());
            } else {
                PolicyReference ref = (PolicyReference)policy;
                if (ref.getReferenceType() == PolicyReference.POLICY_REFERENCE)
                    encodeParamaters(output, indenter, "Policy",
                                     ref.getReference().toString());
                else
                    encodeParamaters(output, indenter, "PolicySet",
                                     ref.getReference().toString());
            }
        }

        getPolicy().encode(output, indenter);
=======
>>>>>>> 3.x
    }

    /**
     * Private helper that encodes the parameters based on the type
     */
    private void encodeParamaters(OutputStream output, Indenter indenter,
                                  String prefix, String id) {
<<<<<<< HEAD
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();
        Iterator it = getParameters().iterator();

        out.println(indent + "<" + prefix + "CombinerParameters " +
                    prefix + "IdRef=\"" + id + "\">");            
        indenter.in();

        while (it.hasNext()) {
            CombinerParameter param = (CombinerParameter)(it.next());
            param.encode(output, indenter);
        }
            
        out.println(indent + "</" + prefix + "CombinerParameters>");
        indenter.out();
    }

=======
    }

	@Override
	public List getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TargetType getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchResult match(EvaluationCtx context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result evaluate(EvaluationCtx context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void encode(OutputStream output) {
		// TODO Auto-generated method stub
		
	}

>>>>>>> 3.x
}
