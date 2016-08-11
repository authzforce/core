/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.Closeable;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;

/**
 * Root policy evaluator, used by the PDP to find and evaluate the root (a.k.a. top-level) policy matching a given request context.
 * <p>
 * Implements {@link Closeable} because it may very likely hold resources such as network resources to get policies remotely, policy caches to speed up finding, etc. Therefore, you are required to
 * call {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in order to make sure these resources are released properly by
 * each underlying module (e.g. invalidate the policy caches).
 *
 * 
 * @version $Id: $
 */
public interface RootPolicyEvaluator extends Closeable
{
	/**
	 * Finds one and only one policy applicable to the given request context and evaluates the request context against it. This will always do a Target match to make sure that the given policy
	 * applies.
	 *
	 * @param context
	 *            the representation of the request data
	 * @return the result of evaluating the request against the applicable policy; or NotApplicable if none is applicable; or Indeterminate if error determining an applicable policy or more than one
	 *         applies or evaluation of the applicable policy returned Indeterminate Decision
	 */
	PdpDecisionResult findAndEvaluate(EvaluationContext context);

	/**
	 * Get the statically applicable policies for this evaluator, i.e. the root policy and (directly/indirectly) referenced policies, only if statically resolved
	 *
	 * @return the static root and referenced policies; null if any of these policies is not statically resolved (once and for all)
	 */
	StaticApplicablePolicyView getStaticApplicablePolicies();

}
