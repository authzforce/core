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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.Decidable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registry supports the standard set of algorithms specified in XACML.
 * <p>
 * Note that because this supports only the standard algorithms, this factory does not allow the addition of any other algorithms. If you need a standard
 * factory that is modifiable, you should create a new <code>BaseCombiningAlgRegistry</code> by passing this to
 * {@link BaseCombiningAlgRegistry#BaseCombiningAlgRegistry(org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry)}.
 *
 * @author cdangerv
 * @version $Id: $
 */
public final class StandardCombiningAlgRegistry extends BaseCombiningAlgRegistry
{
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
		standardAlgorithms.addAll(DenyOverridesAlg.SET.getSupportedAlgorithms());
		// permit-overrides and ordered-permit-overrides
		standardAlgorithms.addAll(PermitOverridesAlg.SET.getSupportedAlgorithms());
		// deny-unless-permit
		standardAlgorithms.addAll(DenyUnlessPermitAlg.SET.getSupportedAlgorithms());
		// permit-unless-deny
		standardAlgorithms.addAll(PermitUnlessDenyAlg.SET.getSupportedAlgorithms());
		// first-applicable
		standardAlgorithms.addAll(FirstApplicableAlg.SET.getSupportedAlgorithms());
		// only-one-applicable
		standardAlgorithms.add(new OnlyOneApplicableAlg());
		//
		// Legacy
		// (ordered-)deny-overrides
		standardAlgorithms.addAll(LegacyDenyOverridesAlg.SET.getSupportedAlgorithms());
		// (orderered-)permit-overrides
		standardAlgorithms.addAll(LegacyPermitOverridesAlg.SET.getSupportedAlgorithms());

		INSTANCE = new StandardCombiningAlgRegistry(standardAlgorithms);
		if (LOGGER.isDebugEnabled())
		{
			final TreeSet<CombiningAlg<?>> sortedAlgorithms = new TreeSet<>(COMPARATOR);
			sortedAlgorithms.addAll(standardAlgorithms);
			LOGGER.debug("Loaded XACML standard combining algorithms: {}", sortedAlgorithms);
		}
	}

	private StandardCombiningAlgRegistry(Set<CombiningAlg<?>> standardExtensions)
	{
		super(standardExtensions);
	}

	/** {@inheritDoc} */
	@Override
	public void addExtension(CombiningAlg<? extends Decidable> alg)
	{
		throw new UnsupportedOperationException("a standard factory cannot be modified");
	}

}
