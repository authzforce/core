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

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

import com.google.common.base.Preconditions;

/**
 * This is an immutable <code>CombiningAlgRegistry</code>.
 *
 * @version $Id: $
 */
public final class ImmutableCombiningAlgRegistry extends BasePdpExtensionRegistry<CombiningAlg<?>>
		implements CombiningAlgRegistry
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
		super(CombiningAlg.class,
				Preconditions.checkNotNull(algorithms, "Input Combining Algorithms undefined (algorithms == null)"));
	}

	/** {@inheritDoc} */
	@Override
	public <T extends Decidable> CombiningAlg<T> getAlgorithm(String algId, Class<T> combinedEltType)
			throws IllegalArgumentException
	{
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
		throw new IllegalArgumentException("Registered combining algorithm for ID=" + algId
				+ " combines instances of type '" + alg.getCombinedElementType()
				+ "' which is not compatible (not same or supertype) with requested type of combined elements : "
				+ combinedEltType);
	}

}
