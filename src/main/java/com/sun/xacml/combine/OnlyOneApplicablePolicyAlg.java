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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.policy.IPolicy;

/**
 * This is the standard Only One Applicable Policy combining algorithm. This is a special algorithm
 * used at the root of a policy/pdp to make sure that pdp only selects one policy per request.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class OnlyOneApplicablePolicyAlg extends CombiningAlgorithm<IPolicy>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OnlyOneApplicablePolicyAlg.class);

	/**
	 * The standard URI used to identify this algorithm
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";

	private static final DecisionResult TOO_MANY_APPLICABLE_POLICIES_INDETERMINATE_RESULT = new DecisionResult(new Status(Status.STATUS_PROCESSING_ERROR, "Too many (more than one) applicable policies for algorithm: " + ID));

	/**
	 * Standard constructor.
	 */
	public OnlyOneApplicablePolicyAlg()
	{
		super(ID, false, IPolicy.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends IPolicy>> parameters, List<? extends IPolicy> policyElements)
	{
		// atLeastOne == true iff selectedPolicy != null
		IPolicy selectedPolicy = null;

		for (final IPolicy policy : policyElements)
		{
			// see if the policy applies to the context
			final boolean isApplicable;
			try
			{
				isApplicable = policy.isApplicable(context);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.info("Error checking whether {} is applicable", policy, e);
				return new DecisionResult(e.getStatus());
			}

			if (isApplicable)
			{
				// if one selected (found applicable) already
				if (selectedPolicy != null)
				{
					return TOO_MANY_APPLICABLE_POLICIES_INDETERMINATE_RESULT;
				}

				// if this was the first applicable policy in the set, then
				// remember it for later
				selectedPolicy = policy;
			}
		}

		// if we got through the loop, it means we found at most one match, then
		// we return the evaluation result of that policy if there is a match
		if (selectedPolicy != null)
		{
			return selectedPolicy.evaluate(context, true);
		}

		return DecisionResult.NOT_APPLICABLE;
	}
}
