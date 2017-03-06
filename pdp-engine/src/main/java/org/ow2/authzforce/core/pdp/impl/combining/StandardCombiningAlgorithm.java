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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Utilities to handle the XACML core standard combining algorithms
 * 
 * @version $Id: $
 */
public enum StandardCombiningAlgorithm
{
	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides
	 */
	XACML_3_0_POLICY_COMBINING_DENY_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides
	 */
	XACML_3_0_RULE_COMBINING_DENY_OVERRIDES("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny- overrides
	 */
	XACML_3_0_POLICY_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny- overrides
	 */
	XACML_3_0_RULE_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides
	 */
	XACML_3_0_POLICY_COMBINING_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides
	 */
	XACML_3_0_RULE_COMBINING_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit- overrides
	 */
	XACML_3_0_POLICY_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny- overrides
	 */
	XACML_3_0_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless- permit
	 */
	XACML_3_0_POLICY_COMBINING_DENY_UNLESS_PERMIT("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit
	 */
	XACML_3_0_RULE_COMBINING_DENY_UNLESS_PERMIT("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless- deny
	 */
	XACML_3_0_POLICY_COMBINING_PERMIT_UNLESS_DENY("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny"),

	/**
	 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny
	 */
	XACML_3_0_RULE_COMBINING_PERMIT_UNLESS_DENY("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable
	 */
	XACML_1_0_POLICY_COMBINING_FIRST_APPLICABLE("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable
	 */
	XACML_1_0_RULE_COMBINING_FIRST_APPLICABLE("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable
	 */
	XACML_1_0_POLICY_COMBINING_ONLY_ONE_APPLICABLE("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable"),

	/**
	 * overridingEffect Legacy/deprecated algorithms
	 */

	/**
	 * urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides
	 */
	XACML_1_0_POLICY_COMBINING_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides
	 */
	XACML_1_0_RULE_COMBINING_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides
	 */
	XACML_1_1_POLICY_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides
	 */
	XACML_1_1_RULE_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides
	 */
	XACML_1_0_POLICY_COMBINING_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides
	 */
	XACML_1_0_RULE_COMBINING_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides
	 */
	XACML_1_1_POLICY_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides"),

	/**
	 * urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides
	 */
	XACML_1_1_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides");

	private final String id;

	private StandardCombiningAlgorithm(final String id)
	{
		this.id = id;
	}

	/**
	 * @return standard identifier of the algorithm, as defined in the XACML spec
	 */
	public String getId()
	{
		return this.id;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardCombiningAlgorithm.class);

	private static final PdpExtensionComparator<CombiningAlg<?>> COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * Singleton immutable instance of combining algorithm registry for standard algorithms
	 */
	public static final CombiningAlgRegistry REGISTRY;

	static
	{
		final Set<CombiningAlg<? extends Decidable>> standardAlgorithms = HashCollections.newUpdatableSet(StandardCombiningAlgorithm.values().length);
		// XACML 3.0 algorithms
		// deny-overrides and ordered-deny-overrides
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_OVERRIDES.id, PolicyEvaluator.class, EffectType.DENY, false));
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_DENY_OVERRIDES.id, RuleEvaluator.class, EffectType.DENY, false));

		standardAlgorithms.add(new DPOverridesCombiningAlg<>(XACML_3_0_POLICY_COMBINING_ORDERED_DENY_OVERRIDES.id, PolicyEvaluator.class, EffectType.DENY, true));
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(XACML_3_0_RULE_COMBINING_ORDERED_DENY_OVERRIDES.id, RuleEvaluator.class, EffectType.DENY, true));

		// permit-overrides and ordered-permit-overrides
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_OVERRIDES.id, PolicyEvaluator.class, EffectType.PERMIT, false));
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_OVERRIDES.id, RuleEvaluator.class, EffectType.PERMIT, false));

		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_ORDERED_PERMIT_OVERRIDES.id, PolicyEvaluator.class, EffectType.PERMIT, true));
		standardAlgorithms.add(new DPOverridesCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES.id, RuleEvaluator.class, EffectType.PERMIT, true));

		// deny-unless-permit
		standardAlgorithms.add(new DPUnlessPDCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_UNLESS_PERMIT.id, PolicyEvaluator.class, EffectType.PERMIT));
		standardAlgorithms.add(new DPUnlessPDCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_DENY_UNLESS_PERMIT.id, RuleEvaluator.class, EffectType.PERMIT));

		// permit-unless-deny
		standardAlgorithms.add(new DPUnlessPDCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_UNLESS_DENY.id, PolicyEvaluator.class, EffectType.DENY));
		standardAlgorithms.add(new DPUnlessPDCombiningAlg<>(StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_UNLESS_DENY.id, RuleEvaluator.class, EffectType.DENY));

		// first-applicable
		standardAlgorithms.add(new FirstApplicableCombiningAlg<>(StandardCombiningAlgorithm.XACML_1_0_POLICY_COMBINING_FIRST_APPLICABLE.id, PolicyEvaluator.class));
		standardAlgorithms.add(new FirstApplicableCombiningAlg<>(StandardCombiningAlgorithm.XACML_1_0_RULE_COMBINING_FIRST_APPLICABLE.id, RuleEvaluator.class));

		// only-one-applicable
		standardAlgorithms.add(new OnlyOneApplicableCombiningAlg(StandardCombiningAlgorithm.XACML_1_0_POLICY_COMBINING_ONLY_ONE_APPLICABLE.id));

		//
		// Legacy
		// (ordered-)deny-overrides
		for (final StandardCombiningAlgorithm alg : EnumSet.range(StandardCombiningAlgorithm.XACML_1_0_POLICY_COMBINING_DENY_OVERRIDES,
				StandardCombiningAlgorithm.XACML_1_1_RULE_COMBINING_ORDERED_DENY_OVERRIDES))
		{
			standardAlgorithms.add(new LegacyDenyOverridesCombiningAlg(alg.id));
		}

		// (orderered-)permit-overrides
		for (final StandardCombiningAlgorithm alg : EnumSet.range(StandardCombiningAlgorithm.XACML_1_0_POLICY_COMBINING_PERMIT_OVERRIDES,
				StandardCombiningAlgorithm.XACML_1_1_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES))
		{
			standardAlgorithms.add(new LegacyPermitOverridesCombiningAlg(alg.id));
		}

		REGISTRY = new ImmutableCombiningAlgRegistry(standardAlgorithms);
		if (LOGGER.isDebugEnabled())
		{
			final TreeSet<CombiningAlg<?>> sortedAlgorithms = new TreeSet<>(COMPARATOR);
			sortedAlgorithms.addAll(standardAlgorithms);
			LOGGER.debug("Loaded XACML standard combining algorithms: {}", sortedAlgorithms);
		}
	}

	private static final Map<String, StandardCombiningAlgorithm> ID_TO_STD_ALG_MAP = Maps.uniqueIndex(Arrays.asList(StandardCombiningAlgorithm.values()),
			new com.google.common.base.Function<StandardCombiningAlgorithm, String>()
			{

				@Override
				public String apply(final StandardCombiningAlgorithm input)
				{
					assert input != null;
					return input.getId();
				}

			});

	/**
	 * Get the standard combining algorithm with a given ID
	 * 
	 * @param algId
	 *            standard combining algorithm ID
	 * @return StandardCombiningAlgorithm with given ID, or null if there is no standard combining algorithm with such ID
	 */
	public static StandardCombiningAlgorithm getInstance(final String algId)
	{
		return ID_TO_STD_ALG_MAP.get(algId);
	}
}
