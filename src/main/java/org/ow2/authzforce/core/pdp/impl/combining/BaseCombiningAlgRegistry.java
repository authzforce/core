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

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

/**
 * This is a com.thalesgroup.authzforce.core.test.basic implementation of <code>CombiningAlgRegistry</code>.
 *
 * @version $Id: $
 */
public class BaseCombiningAlgRegistry extends BasePdpExtensionRegistry<CombiningAlg<?>> implements CombiningAlgRegistry
{
	/**
	 * <p>
	 * Constructor for BaseCombiningAlgRegistry.
	 * </p>
	 *
	 * @param algorithms
	 *            a {@link java.util.Set} object.
	 */
	protected BaseCombiningAlgRegistry(Set<CombiningAlg<?>> algorithms)
	{
		super(CombiningAlg.class, algorithms);
	}

	/**
	 * <p>
	 * Constructor for BaseCombiningAlgRegistry.
	 * </p>
	 *
	 * @param baseRegistry
	 *            parent registry from which this inherits all entries
	 * @see BasePdpExtensionRegistry#BasePdpExtensionRegistry(Class, BasePdpExtensionRegistry)
	 */
	public BaseCombiningAlgRegistry(BasePdpExtensionRegistry<CombiningAlg<?>> baseRegistry)
	{
		super(CombiningAlg.class, baseRegistry);
	}

	/**
	 * Default constructor.
	 */
	public BaseCombiningAlgRegistry()
	{
		super(CombiningAlg.class);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends Decidable> CombiningAlg<T> getAlgorithm(String algId, Class<T> combinedEltType) throws IllegalArgumentException
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
		throw new IllegalArgumentException("Registered combining algorithm for ID=" + algId + " combines instances of type '" + alg.getCombinedElementType()
				+ "' which is not compatible (not same or supertype) with requested type of combined elements : " + combinedEltType);
	}

}
