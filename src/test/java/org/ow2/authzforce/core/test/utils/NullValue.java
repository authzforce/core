package org.ow2.authzforce.core.test.utils;

import org.ow2.authzforce.core.value.Value;

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
