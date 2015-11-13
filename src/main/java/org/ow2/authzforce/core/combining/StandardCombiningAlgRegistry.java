/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.combining;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.OnlyOneApplicablePolicyAlg;
import com.thalesgroup.authzforce.core.Decidable;

/**
 * This registry supports the standard set of algorithms specified in XACML.
 * <p>
 * Note that because this supports only the standard algorithms, this factory does not allow the
 * addition of any other algorithms. If you need a standard factory that is modifiable, you should
 * create a new <code>BaseCombiningAlgRegistry</code> by passing this to
 * {@link BaseCombiningAlgRegistry#BaseCombiningAlgRegistry(com.thalesgroup.authzforce.core.BasePdpExtensionRegistry)}.
 * 
 */
public final class StandardCombiningAlgRegistry extends BaseCombiningAlgRegistry
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StandardCombiningAlgRegistry.class);
	/**
	 * Singleton function registry instance for standard functions
	 */
	public static final StandardCombiningAlgRegistry INSTANCE;
	static
	{
		final Set<CombiningAlgorithm<? extends Decidable>> standardExtensions = new HashSet<>();
		// XACML 3.0 algorithms
		// deny-overrides and ordered-deny-overrides
		standardExtensions.addAll(DenyOverridesAlg.SET.getSupportedAlgorithms());
		// permit-overrides and ordered-permit-overrides
		standardExtensions.addAll(PermitOverridesAlg.SET.getSupportedAlgorithms());
		// deny-unless-permit
		standardExtensions.addAll(DenyUnlessPermitAlg.SET.getSupportedAlgorithms());
		// permit-unless-deny
		standardExtensions.addAll(PermitUnlessDenyAlg.SET.getSupportedAlgorithms());
		// first-applicable
		standardExtensions.addAll(FirstApplicableAlg.SET.getSupportedAlgorithms());
		// only-one-applicable
		standardExtensions.add(new OnlyOneApplicablePolicyAlg());
		//
		// Legacy
		// (ordered-)deny-overrides
		standardExtensions.addAll(LegacyDenyOverridesAlg.SET.getSupportedAlgorithms());
		// (orderered-)permit-overrides
		standardExtensions.addAll(LegacyPermitOverridesAlg.SET.getSupportedAlgorithms());

		final Map<String, CombiningAlgorithm<? extends Decidable>> stdExtMap = new HashMap<>();
		for (final CombiningAlgorithm<? extends Decidable> stdExt : standardExtensions)
		{
			stdExtMap.put(stdExt.getId(), stdExt);
		}

		INSTANCE = new StandardCombiningAlgRegistry(stdExtMap);
		LOGGER.info("Loaded XACML standard combining algorithms: {}", stdExtMap.keySet());
	}

	private StandardCombiningAlgRegistry(Map<String, CombiningAlgorithm<? extends Decidable>> stdExtMap)
	{
		super(Collections.unmodifiableMap(stdExtMap));
	}

	@Override
	public void addExtension(CombiningAlgorithm<? extends Decidable> alg)
	{
		throw new UnsupportedOperationException("a standard factory cannot be modified");
	}

}
