/**
 * Copyright 2012-2021 THALES.
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

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.BasePdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;

import com.google.common.base.Preconditions;

/**
 * This is an immutable <code>CombiningAlgRegistry</code>.
 *
 * @version $Id: $
 */
public final class ImmutableCombiningAlgRegistry extends BasePdpExtensionRegistry<CombiningAlg<?>> implements CombiningAlgRegistry
{
	/**
	 * <p>
	 * Constructor for BaseCombiningAlgRegistry.
	 * </p>
	 *
	 * @param algorithms
	 *            combining algorithms.
	 */
	public ImmutableCombiningAlgRegistry(Set<CombiningAlg<?>> algorithms)
	{
		super(CombiningAlg.class, Preconditions.checkNotNull(algorithms, "Input Combining Algorithms undefined (algorithms == null)"));
	}

	private static String toString(Class<? extends Decidable> combinedElementType) {
		return combinedElementType == PolicyEvaluator.class ? "Policy(Set)" : combinedElementType == RuleEvaluator.class ? "Rule" : combinedElementType.getCanonicalName();
	}

	/** {@inheritDoc} */
	@Override
	public <T extends Decidable> CombiningAlg<T> getAlgorithm(String algId, Class<T> combinedEltType) throws IllegalArgumentException {
		final CombiningAlg<? extends Decidable> alg = this.getExtension(algId);
		if (alg == null)
		{
			throw new IllegalArgumentException("Unsupported combining algorithm: '" + algId + "'");
		}

		if (alg.getCombinedElementType().isAssignableFrom(combinedEltType))
		{
			return (CombiningAlg<T>) alg;
		}

		// wrong type of alg
		throw new IllegalArgumentException(
				"Combining algorithm '" + algId + "': invalid type of input elements (to be combined): " + toString(combinedEltType) + "; expected: " + toString(alg.getCombinedElementType()) + ".");
	}

}
