/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.Closeable;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;

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
	DecisionResult findAndEvaluate(EvaluationContext context);

	/**
	 * Get the statically applicable policies for this evaluator, i.e. the root policy and (directly/indirectly) referenced policies, only if statically resolved
	 *
	 * @return the static root and referenced policies; null if any of these policies is not statically resolved (once and for all)
	 */
	FlattenedPolicyTree getStaticApplicablePolicies();

}
