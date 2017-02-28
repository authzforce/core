/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.test.func;

import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Special value to be interpreted as Indeterminate. (Mapped to IndeterminateExpression in FunctionTest class.) For testing only.
 *
 */
public class NullValue implements Value
{
	private final String datatypeId;
	private final boolean isBag;

	public NullValue(String datatype)
	{
		this(datatype, false);
	}

	public NullValue(String datatypeId, boolean isBag)
	{
		this.datatypeId = datatypeId;
		this.isBag = isBag;
	}

	public String getDatatypeId()
	{
		return this.datatypeId;
	}

	public boolean isBag()
	{
		return this.isBag;
	}
}
