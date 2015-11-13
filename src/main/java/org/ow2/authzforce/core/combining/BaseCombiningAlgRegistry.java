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

import java.util.Map;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.Decidable;

/**
 * This is a com.thalesgroup.authzforce.core.test.basic implementation of
 * <code>CombiningAlgRegistry</code>.
 */
public class BaseCombiningAlgRegistry extends BasePdpExtensionRegistry<CombiningAlgorithm<?>> implements CombiningAlgRegistry
{
	protected BaseCombiningAlgRegistry(Map<String, CombiningAlgorithm<?>> algorithmsById)
	{
		super(CombiningAlgorithm.class, algorithmsById);
	}

	/**
	 * @param baseRegistry
	 *            parent registry from which this inherits all entries
	 * @see BasePdpExtensionRegistry#BasePdpExtensionRegistry(Class, BasePdpExtensionRegistry)
	 */
	public BaseCombiningAlgRegistry(BasePdpExtensionRegistry<CombiningAlgorithm<?>> baseRegistry)
	{
		super(CombiningAlgorithm.class, baseRegistry);
	}

	/**
	 * Default constructor.
	 */
	public BaseCombiningAlgRegistry()
	{
		super(CombiningAlgorithm.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Decidable> CombiningAlgorithm<T> getAlgorithm(String algId, Class<T> combinedEltType) throws UnknownIdentifierException
	{
		final CombiningAlgorithm<? extends Decidable> alg = this.getExtension(algId);
		if (alg.getCombinedElementType().isAssignableFrom(combinedEltType))
		{
			return (CombiningAlgorithm<T>) alg;
		}

		// wrong type of alg
		throw new IllegalArgumentException("Registered combining algorithm for ID=" + algId + " combines instances of type '" + alg.getCombinedElementType() + "' which is not compatible (not same or supertype) with requested type of combined elements : " + combinedEltType);
	}

}
