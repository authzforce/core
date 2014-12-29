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
/**
 * 
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.IPolicy;

public class PermitUnlessDenyPolicyAlg extends PolicyCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:"
			+ "permit-unless-deny";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);
	
	/**
	 * Standard constructor
	 */
	public PermitUnlessDenyPolicyAlg() {
		super(identifierURI);
	}

	/**
	 * @param identifier
	 */
	public PermitUnlessDenyPolicyAlg(URI identifier) {
		super(identifier);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.xacml.combine.PolicyCombiningAlgorithm#combine(com.sun.xacml.
	 * EvaluationCtx, java.util.List, java.util.List)
	 */
	@Override
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List<IPolicy> policyElements) {
		final Obligations combinedObligations = new Obligations();
		final AssociatedAdvice combinedAssociatedAdvice = new AssociatedAdvice();
		
		for (IPolicy policy : policyElements) {
			// make sure that the policy matches the context
			final MatchResult match = policy.match(context);
			if (match.getResult() == MatchResult.MATCH) {
				final Result result = policy.evaluate(context);
				if (result.getDecision() == DecisionType.DENY) {
					return result;
				} 
				
				final Obligations resultObligations = result.getObligations();
				if(resultObligations != null) {
					combinedObligations.getObligations().addAll(resultObligations.getObligations());
				}
				
				final AssociatedAdvice resultAssociatedAdvice = result.getAssociatedAdvice();
				if(resultAssociatedAdvice != null) {
					combinedAssociatedAdvice.getAdvices().addAll(resultAssociatedAdvice.getAdvices());
				}
			}
		}
		
		return new Result(DecisionType.PERMIT, null, context.getResourceId().encode(), combinedObligations, combinedAssociatedAdvice, null);
	}

}
