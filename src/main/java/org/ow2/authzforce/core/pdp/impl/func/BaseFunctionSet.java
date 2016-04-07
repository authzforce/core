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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.FunctionSet;

/**
 * Base class for {@link FunctionSet}s
 */
public class BaseFunctionSet implements FunctionSet
{
	/**
	 * Namespace to be used as default prefix for internal function set IDs
	 */
	public static final String DEFAULT_ID_NAMESPACE = "urn:ow2:authzforce:xacml:function-set:";

	private final String id;

	private final Set<Function<?>> functions;

	/**
	 * @param id
	 *            globally unique ID of this function set, to be used as PDP extension ID
	 * @param functions
	 */
	public BaseFunctionSet(String id, Function<?>... functions)
	{
		this(id, new HashSet<>(Arrays.asList(functions)));
	}

	/**
	 * @param id
	 * @param functions
	 */
	public BaseFunctionSet(String id, Set<Function<?>> functions)
	{
		this.id = id;
		this.functions = Collections.unmodifiableSet(functions);
	}

	/**
	 * Returns a single instance of each of the functions supported by some class. The <code>Set</code> must contain instances of <code>Function</code>, and it
	 * must be both non-null and non-empty. It may contain only a single <code>Function</code>.
	 * 
	 * @return the functions members of this group
	 */
	@Override
	public Set<Function<?>> getSupportedFunctions()
	{
		return functions;
	}

	@Override
	public String getId()
	{
		return id;
	}

	private volatile int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = id.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BaseFunctionSet))
		{
			return false;
		}
		BaseFunctionSet other = (BaseFunctionSet) obj;
		return this.id.equals(other.id);
	}

}
