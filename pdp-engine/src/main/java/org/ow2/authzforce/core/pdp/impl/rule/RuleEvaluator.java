/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.impl.rule;

import com.google.common.collect.ImmutableList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluators;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates a XACML Rule to a Decision.
 *
 * 
 * @version $Id: $
 */
public abstract class RuleEvaluator implements Decidable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluators.class);

	// non-null
	private final String ruleId;

	private final transient String toString;

	protected RuleEvaluator(final String ruleId)
	{
		assert ruleId != null;

		// JAXB fields initialization
		this.ruleId = ruleId;
		this.toString = "Rule['" + ruleId + "']";
	}

	/**
	 * Get evaluated rule ID
	 *
	 * @return evaluated rule ID
	 */
	public final String getRuleId()
	{
		return this.ruleId;
	}

	/**
	 * Get evaluated rule Effect (Permit/Deny) when applicable, null if always NotApplicable (Condition is constant False, see section 7.11 of XACML 3.0)
	 *
	 * @return evaluated rule Effect
	 */
	public abstract EffectType getEffect();

	/**
	 * Is the rule always applicable, i.e. applies to all requests, i.e. the rule's Target matches all, and the condition is undefined or always evaluates to True?
	 * <p>
	 * Knowing that a rule is always applicable is useful for optimizing combining algorithm evaluators at initialization time, e.g. First-applicable algorithm.
	 * 
	 * @return true iff it has no PEP action
	 */
	public abstract boolean isAlwaysApplicable();
	/**
	 * Does the rule has any PEP action (obligation/advice) ?
	 * <p>
	 * Knowing that a rule has no PEP action is useful for optimizing combining algorithm evaluators at initialization time, e.g. deny-unless-permit/permit-unless-deny algorithms.
	 * 
	 * @return true iff it has any PEP action
	 */
	public abstract boolean hasAnyPepAction();

	/**
	 * Is the rule (equivalent to) an empty rule? I.e. the rule's Target matches all, the condition is undefined or always evaluates to True, and there is no PEP action (obligation/advice), in other
	 * words the rule always evaluates to the simple Permit/Deny decision corresponding to its Effect.
	 * <p>
	 * Knowing that a rule is empty(-equivalent) is useful for optimizing combining algorithm evaluators at initialization time, e.g. (ordered-)permit-overrides/deny-overrides algorithms.
	 * 
	 * @return true iff it is empty equivalent (empty rule or equivalent to an empty rule)
	 */
	public abstract boolean isEmptyEquivalent();

	/** {@inheritDoc} */
	@Override
	public final String toString()
	{
		return toString;
	}

}
