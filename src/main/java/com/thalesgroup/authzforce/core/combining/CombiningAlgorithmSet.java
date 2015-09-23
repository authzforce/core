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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.eval.Decidable;

/**
 * Combining algorithm set. Allows to group combining algorithms, especially when it is actually the
 * same generic algorithm but with different IDs, such as most standard algorithms which are the
 * same for policy combining and rule combining algorithm IDs.
 * 
 * TODO: consider making it a PdpExtension like FunctionSet, or generic PdpExtensionSet
 */
public class CombiningAlgorithmSet // implements PdpExtension
{
//	/**
//	 * Namespace to be used as default prefix for internal algorithm set IDs
//	 */
//	public static final String DEFAULT_ID_NAMESPACE = "urn:thalesgroup:xacml:combining-algorithm-set:";

//	private final String id;

	private final Set<CombiningAlgorithm<?>> algs;

	/**
//	 * @param id
//	 *            globally unique ID of this function set, to be used as PDP extension ID
	 * @param algorithms
	 */
	public CombiningAlgorithmSet(/*String id,*/ CombiningAlgorithm<?>... algorithms)
	{
		this(/*id,*/ new HashSet<>(Arrays.asList(algorithms)));
	}

	/**
//	 * @param id
	 * @param algorithms
	 */
	public CombiningAlgorithmSet(/*String id,*/ Set<CombiningAlgorithm<?>> algorithms)
	{
//		this.id = id;
		this.algs = Collections.unmodifiableSet(algorithms);
	}

	/**
	 * Returns a single instance of each of the functions supported by some class. The
	 * <code>Set</code> must contain instances of <code>Function</code>, and it must be both
	 * non-null and non-empty. It may contain only a single <code>Function</code>.
	 * 
	 * @return the functions members of this group
	 */
	public Set<CombiningAlgorithm<? extends Decidable>> getSupportedAlgorithms()
	{
		return algs;
	}
//
//	@Override
//	public String getId()
//	{
//		return id;
//	}

}
