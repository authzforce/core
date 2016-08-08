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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registry supports the standard set of algorithms specified in XACML.
 * <p>
 * Note that because this supports only the standard algorithms, this factory does not allow the addition of any other algorithms. If you need a standard factory that is modifiable, you should create
 * a new <code>BaseCombiningAlgRegistry</code> by passing this to {@link BaseCombiningAlgRegistry#BaseCombiningAlgRegistry(org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry)}.
 *
 * 
 * @version $Id: $
 */
public final class StandardCombiningAlgRegistry extends BaseCombiningAlgRegistry
{

	/**
	 * The standard policy combining algorithms (identifiers)
	 */
	public enum StdAlgorithm
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
		 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides
		 */
		XACML_3_0_POLICY_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides
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
		 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides
		 */
		XACML_3_0_POLICY_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides
		 */
		XACML_3_0_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit
		 */
		XACML_3_0_POLICY_COMBINING_DENY_UNLESS_PERMIT("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit
		 */
		XACML_3_0_RULE_COMBINING_DENY_UNLESS_PERMIT("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny
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
		 * urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides
		 */
		XACML_1_1_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides");

		private final String id;

		private StdAlgorithm(final String id)
		{
			this.id = id;
		}
	}

	private static final UnsupportedOperationException UNSUPPORTED_ADD_EXTENSION_EXCEPTION = new UnsupportedOperationException("A standard factory cannot be modified");

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardCombiningAlgRegistry.class);

	private static final PdpExtensionComparator<CombiningAlg<?>> COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * Singleton function registry instance for standard functions
	 */
	public static final StandardCombiningAlgRegistry INSTANCE;
	static
	{
		final Set<CombiningAlg<? extends Decidable>> standardAlgorithms = new HashSet<>();
		// XACML 3.0 algorithms
		// deny-overrides and ordered-deny-overrides
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_OVERRIDES, StdAlgorithm.XACML_3_0_RULE_COMBINING_ORDERED_DENY_OVERRIDES))
		{
			standardAlgorithms.add(new DenyOverridesAlg(alg.id));
		}

		// permit-overrides and ordered-permit-overrides
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_OVERRIDES, StdAlgorithm.XACML_3_0_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES))
		{
			standardAlgorithms.add(new PermitOverridesAlg(alg.id));
		}

		// deny-unless-permit
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_UNLESS_PERMIT, StdAlgorithm.XACML_3_0_RULE_COMBINING_DENY_UNLESS_PERMIT))
		{
			standardAlgorithms.add(new DenyUnlessPermitAlg(alg.id));
		}

		// permit-unless-deny
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_UNLESS_DENY, StdAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_UNLESS_DENY))
		{
			standardAlgorithms.add(new PermitUnlessDenyAlg(alg.id));
		}

		// first-applicable
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_1_0_POLICY_COMBINING_FIRST_APPLICABLE, StdAlgorithm.XACML_1_0_RULE_COMBINING_FIRST_APPLICABLE))
		{
			standardAlgorithms.add(new FirstApplicableAlg(alg.id));
		}

		// only-one-applicable
		standardAlgorithms.add(new OnlyOneApplicableAlg(StdAlgorithm.XACML_1_0_POLICY_COMBINING_ONLY_ONE_APPLICABLE.id));

		//
		// Legacy
		// (ordered-)deny-overrides
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_1_0_POLICY_COMBINING_DENY_OVERRIDES, StdAlgorithm.XACML_1_1_RULE_COMBINING_ORDERED_DENY_OVERRIDES))
		{
			standardAlgorithms.add(new LegacyDenyOverridesAlg(alg.id));
		}

		// (orderered-)permit-overrides
		for (final StdAlgorithm alg : EnumSet.range(StdAlgorithm.XACML_1_0_POLICY_COMBINING_PERMIT_OVERRIDES, StdAlgorithm.XACML_1_1_RULE_COMBINING_ORDERED_PERMIT_OVERRIDES))
		{
			standardAlgorithms.add(new LegacyPermitOverridesAlg(alg.id));
		}

		INSTANCE = new StandardCombiningAlgRegistry(standardAlgorithms);
		if (LOGGER.isDebugEnabled())
		{
			final TreeSet<CombiningAlg<?>> sortedAlgorithms = new TreeSet<>(COMPARATOR);
			sortedAlgorithms.addAll(standardAlgorithms);
			LOGGER.debug("Loaded XACML standard combining algorithms: {}", sortedAlgorithms);
		}
	}

	private StandardCombiningAlgRegistry(final Set<CombiningAlg<?>> standardExtensions)
	{
		super(standardExtensions);
	}

	/** {@inheritDoc} */
	@Override
	public void addExtension(final CombiningAlg<? extends Decidable> alg)
	{
		throw UNSUPPORTED_ADD_EXTENSION_EXCEPTION;
	}

}
