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
    public PolicyCombinerElement(PolicyType policy) {
        super(policy);
    }
    
    public PolicyCombinerElement(PolicyType policy, List args) {
        super(policy, args);
    }
    
    public PolicyCombinerElement(PolicySetType policy, List args) {
        super(policy, args);
    }
    
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

	/**
     * Returns the <code>AbstractPolicy</code> in this element.
     *
     * @return the element's <code>AbstractPolicy</code>
     */
    public Policy getPolicy() {
        return (Policy)getElement();
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
    }

    /**
     * Private helper that encodes the parameters based on the type
     */
    private void encodeParamaters(OutputStream output, Indenter indenter,
                                  String prefix, String id) {
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

}
