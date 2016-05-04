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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.Decidable;

/**
 * Combining algorithm set. Allows to group combining algorithms, especially when it is actually the same generic algorithm but with different IDs, such as most standard algorithms which are the same
 * for policy combining and rule combining algorithm IDs.
 *
 * TODO: consider making it a PdpExtension like FunctionSet, or generic PdpExtensionSet
 *
 * @version $Id: $
 */
public class CombiningAlgSet
{
	private final Set<CombiningAlg<?>> algs;

	/**
	 * Creates set from multiple combining algorithms
	 *
	 * @param algorithms
	 *            XACML policy/rule combining algorithms added to the set
	 */
	public CombiningAlgSet(CombiningAlg<?>... algorithms)
	{
		this(new HashSet<>(Arrays.asList(algorithms)));
	}

	/**
	 * Creates a set as a copy of an existing set
	 *
	 * @param algorithms
	 *            XACML policy/rule combining algorithms added to the set
	 */
	public CombiningAlgSet(Set<CombiningAlg<?>> algorithms)
	{
		this.algs = Collections.unmodifiableSet(algorithms);
	}

	/**
	 * Returns a single instance of each of the functions supported by some class. The <code>Set</code> must contain instances of <code>Function</code>, and it must be both non-null and non-empty. It
	 * may contain only a single <code>Function</code>.
	 *
	 * @return the functions members of this group
	 */
	public Set<CombiningAlg<? extends Decidable>> getSupportedAlgorithms()
	{
		return algs;
	}

}
