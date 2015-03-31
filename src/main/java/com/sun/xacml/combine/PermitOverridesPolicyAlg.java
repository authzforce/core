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

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.IPolicy;

/**
 * This is the standard Permit Overrides policy combining algorithm. It allows a single evaluation
 * of Permit to take precedence over any number of deny, not applicable or indeterminate results.
 * Note that since this implementation does an ordered evaluation, this class also supports the
 * Ordered Permit Overrides algorithm.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PermitOverridesPolicyAlg extends PolicyCombiningAlgorithm
{

	private static final Logger LOGGER = LoggerFactory.getLogger(PermitOverridesPolicyAlg.class);

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);

	/**
	 * Standard constructor.
	 */
	public PermitOverridesPolicyAlg()
	{
		super(identifierURI);
	}

	/**
	 * Protected constructor used by the ordered version of this algorithm.
	 * 
	 * @param identifier
	 *            the algorithm's identifier
	 */
	protected PermitOverridesPolicyAlg(URI identifier)
	{
		super(identifier);
	}

	/**
	 * Applies the combining rule to the set of policies based on the evaluation context.
	 * 
	 * @param context
	 *            the context from the request
	 * @param parameters
	 *            a (possibly empty) non-null <code>List</code> of <code>CombinerParameter<code>s
	 * @param policyElements
	 *            the policies to combine
	 * 
	 * @return the result of running the combining algorithm
	 */
	@Override
	public Result combine(EvaluationCtx context, CombinerParametersType parameters, List<IPolicy> policyElements)
	{
		boolean atLeastOneDeny = false;
		
		/*
		 * Replaces atLeastOneError from XACML spec.
		 * atLeastOneError == true <=> firstIndeterminateResult != null
		 */
		Result firstIndeterminateResult = null;
		
		final Obligations combinedObligations = new Obligations();
		final AssociatedAdvice combinedAssociatedAdvice = new AssociatedAdvice();
		for (final IPolicy policy : policyElements)
		{
			// make sure that the policy matches the context
			final MatchResult match = policy.match(context);
			final int matchResultCode = match.getResult();
			if (matchResultCode == MatchResult.INDETERMINATE)
			{
				/*
				 * FIXME: implement extended Indeterminate decisions.
				 */
				firstIndeterminateResult = new Result(DecisionType.INDETERMINATE, match.getStatus());
			} else if (matchResultCode == MatchResult.MATCH)
			{
				// evaluate the policy
				final Result result = policy.evaluate(context);
				switch (result.getDecision())
				{
					case PERMIT:
						return result;
					case DENY:
						atLeastOneDeny = true;
						final Obligations resultObligations = result.getObligations();
						if (resultObligations != null)
						{
							combinedObligations.getObligations().addAll(resultObligations.getObligations());
						}

						final AssociatedAdvice resultAssociatedAdvice = result.getAssociatedAdvice();
						if (resultAssociatedAdvice != null)
						{
							combinedAssociatedAdvice.getAdvices().addAll(resultAssociatedAdvice.getAdvices());
						}
						break;
					case INDETERMINATE:
						/*
						 * FIXME: implement extended Indeterminate decisions (result differs if
						 * Indeterminate{P} or Indeterminate{D})
						 */
						firstIndeterminateResult = result;
						break;
					default:
				}
			}
		}

		/*
		 * FIXME: implement extended Indeterminate decisions as the algorithm distinguishes them.
		 */
		if (firstIndeterminateResult != null)
		{
			return firstIndeterminateResult;
		}

		if (atLeastOneDeny)
		{
			/*
			 * Obligations/AssociatedAdvice arguments must be null if no obligation/advice combined,
			 * to avoid creating empty <Obligations>/<AssociatedAdvice> element (no valid by XACML
			 * schema) when marshalling with JAXB
			 */
			return new Result(DecisionType.DENY, null, combinedObligations.getObligations().isEmpty() ? null : combinedObligations,
					combinedAssociatedAdvice.getAdvices().isEmpty() ? null : combinedAssociatedAdvice, null);
		}

		return new Result(DecisionType.NOT_APPLICABLE);
	}
}
