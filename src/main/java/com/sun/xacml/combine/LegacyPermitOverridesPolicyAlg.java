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
import java.util.List;

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
public class LegacyPermitOverridesPolicyAlg extends PolicyCombiningAlgorithm
{

	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyPermitOverridesPolicyAlg.class);

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);

	/**
	 * Standard constructor.
	 */
	public LegacyPermitOverridesPolicyAlg()
	{
		super(identifierURI);
	}

	/**
	 * Protected constructor used by the ordered version of this algorithm.
	 * 
	 * @param identifier
	 *            the algorithm's identifier
	 */
	protected LegacyPermitOverridesPolicyAlg(URI identifier)
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
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		Obligations denyObligations = new Obligations();
		Status firstIndeterminateStatus = null;

		// List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>();
		for (final IPolicy policyElement : policyElements)
		{
			MatchResult match = null;
			Result result = null;
			// make sure that the policy matches the context
			match = policyElement.match(context);
			LOGGER.debug("{} - {}", policyElement, match);
			if (match == null)
			{
				atLeastOneError = true;
			} else if (match.getResult() == MatchResult.INDETERMINATE)
			{
				atLeastOneError = true;

				// keep track of the first error, regardless of cause
				if (firstIndeterminateStatus == null)
					firstIndeterminateStatus = match.getStatus();
			} else if (match.getResult() == MatchResult.MATCH)
			{
				// now we evaluate the policy
				result = policyElement.evaluate(context);

				int effect = result.getDecision().ordinal();

				// this is a little different from DenyOverrides...

				if (effect == Result.DECISION_PERMIT)
				{
					return result;
				}
				if (effect == Result.DECISION_DENY)
				{
					atLeastOneDeny = true;
					denyObligations = result.getObligations();
				} else if (effect == Result.DECISION_INDETERMINATE)
				{
					atLeastOneError = true;

					// keep track of the first error, regardless of cause
					if (firstIndeterminateStatus == null)
						firstIndeterminateStatus = result.getStatus();
				}
			}
		}

		// if we got a DENY, return it
		if (atLeastOneDeny)
			return new Result(DecisionType.DENY, denyObligations);

		// if we got an INDETERMINATE, return it
		if (atLeastOneError)
			return new Result(DecisionType.INDETERMINATE, firstIndeterminateStatus);

		// if we got here, then nothing applied to us
		return new Result(DecisionType.NOT_APPLICABLE);
	}
}
