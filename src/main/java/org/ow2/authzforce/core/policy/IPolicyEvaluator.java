/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.policy;

import java.util.List;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;

/**
 * Policy Element evaluator interface, "Policy Element" referring to all Policy* element: Policy, PolicySet, PolicyIdReference (handled by PolicyReference),
 * PolicySetIdReference (handled by PolicySetReference). All these classes have common behavior which is captured by this interface to benefit from
 * polymorphism, therefore more reusable (-> less) code. Actually this class intends to replace AbstractPolicy which can no longer be used as mother class to
 * (xacmlv3.)Policy(Set) and Policy(Set)Reference (because it is an abstract class), as long as those classes already extend another class from JAXB model (e.g.
 * com.thalesgroup.authzforce.core.test.custom.Policy extends oasis.names...Policy), so we can only make them implement an Interface instead, as Java does not
 * allow extension of multiple abstract classes.
 * 
 * 
 */
public interface IPolicyEvaluator extends Decidable
{
	/**
	 * "isApplicable()" as defined by Only-one-applicable algorithm (section C.9), i.e. applicable by virtue of its target, i.e. the target matches the context.
	 * {@link #evaluate(EvaluationContext)} already checks first if the policy is applicable, therefore you may call isApplicable() only if you only want to
	 * check if the policy is applicable. If you want to evaluate the policy, call {@link #evaluate(EvaluationContext)} right away. To be used by
	 * Only-one-applicable algorithm in particular.
	 * 
	 * @param context
	 *            evaluation context to match
	 * @return whether it is applicable
	 * @throws IndeterminateEvaluationException
	 *             if Target evaluation in this context is "Indeterminate"
	 */
	boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Same as {@link #evaluate(EvaluationContext)} except Target evaluation may be skipped. To be used by Only-one-applicable algorithm with
	 * <code>skipTarget</code>=true, after calling {@link #isApplicable(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target. If false, this must be equivalent to {@link #evaluate(EvaluationContext)}
	 * @return decision result
	 */
	DecisionResult evaluate(EvaluationContext context, boolean skipTarget);

	/**
	 * Get policy ID, e.g. for auditing
	 * 
	 * @return evaluated Policy(Set)Id
	 */
	String getPolicyId();

	/**
	 * Get combining algorithm, e.g. for auditing policy decision
	 * 
	 * @return Policy/Rule combining algorithm
	 */
	String getCombiningAlgId();

	/**
	 * Get longest chain of Policy reference (via Policy(Set)IdReference) starting from this Policy(Set), in order to limit the length of such chain. Note that
	 * in the current XACML 3.0 model, it is safe to ignore Policy elements; since they cannot have references. However, we consider that Policy and PolicySet
	 * types could be merged into one Policy type on the long-term. That's why we define this method at the interface level on top of both Policy and PolicySet
	 * evaluator classes. Indeed, this interface represents common behavior of the two.
	 * 
	 * @return longest policy reference chain in this policy; null if there is no Policy(Set)IdReference in this Policy(Set), or undefined because there are
	 *         Policy references dynamically resolved depending of the evaluation context (as opposed to static policy reference resolved by static policy
	 *         finders).
	 */
	List<String> getLongestPolicyReferenceChain();
}
