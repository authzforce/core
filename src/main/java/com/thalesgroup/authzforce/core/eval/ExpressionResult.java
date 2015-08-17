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
package com.thalesgroup.authzforce.core.eval;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * This is used for valid results of evaluation of Expressions. This is not used for returning
 * errors because Exceptions are a better and more natural way (from a Java standpoint) to propagate
 * error information with a full traceable stacktrace. The XACML Status is used for returning errors
 * but is not usable for stacktrace if you want more than error codes in your error info, which is
 * the case here. (The StatusCode is the only recursive structure in Status and therefore the only
 * part that could be used for stacktraces).
 * 
 * Therefore for "Indeterminate" results of evaluation, use {@link IndeterminateEvaluationException}
 * instead. Most of the error information will be used at least for logging and troubleshooting. But
 * for the final response of the PDP to the PEP, at least in production, most of the errors
 * will/should be filtered out.
 * 
 * @param <T>
 *            type of attribute value(s) in result
 */
public abstract class ExpressionResult<T extends AttributeValue>
{

	protected static <V extends AttributeValue> V[] toArray(V val)
	{
		if (val == null)
		{
			return null;
		}

		final V[] array = (V[]) Array.newInstance(val.getClass(), 1);
		array[0] = val;
		return array;
	}

	private Status status;

	protected final T[] values;

	protected final DatatypeDef datatype;

	/**
	 * Constructor for erroneous result (status indicating type of error)
	 */
	protected ExpressionResult(Status status, DatatypeDef datatype)
	{
		this.setStatus(status == null ? Status.OK : status);
		this.datatype = datatype;
		this.values = null;
	}

	/**
	 * Constructor for valid result with multiple values
	 * 
	 * @param isBag
	 *            true iff this is a multi-valued result
	 */
	protected ExpressionResult(T[] values, DatatypeDef datatype)
	{
		this.setStatus(Status.OK);
		this.datatype = datatype;
		this.values = values;
	}

	/**
	 * Constructor for valid result with single value
	 */
	protected ExpressionResult(T value, DatatypeDef datatype)
	{
		this(toArray(value), datatype);
	}

	/**
	 * Get instance of erroneous ExpressionResult of the proper kind (BagResult if bag,
	 * PrimitiveResult if not)
	 * 
	 * @param status
	 *            error info
	 * @param datatypeClass
	 *            expected bag datatype class
	 * @param datatype
	 *            expected bag datatype
	 * @return instance
	 */
	public final static <T extends AttributeValue> ExpressionResult<T> getInstance(Status status, Class<T> datatypeClass, DatatypeDef datatype)
	{
		return datatype.isBag() ? new BagResult<>(status, datatypeClass, datatype) : new PrimitiveResult<T>(status, datatype);
	}

	/**
	 * Returns the (first) attribute value or null if no value found
	 * 
	 * @return the first attribute value for a result bag, the single attribute value if not a bag;
	 *         or null in both cases if no value
	 */
	public T value()
	{
		return this.values == null || values.length == 0 ? null : values[0];
	}

	/**
	 * Returns the attribute value(s) in the result
	 * 
	 * @return <code>Collection</code> of attribute value(s); may be empty if no value, or null if
	 *         undefined (error occurred evaluating the expression, in which case
	 *         {@link #getStatus()} gives information about the error)
	 */
	public T[] values()
	{
		// the constructor makes sure it is unmodifiable (defensive copy)
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(values);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		/*
		 * if (!super.equals(obj)) { return false; }
		 */
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final ExpressionResult<?> other = (ExpressionResult<?>) obj;
		if (values == null)
		{
			if (other.values != null)
			{
				return false;
			}
		} else if (!Arrays.equals(values, other.values))
		{
			return false;
		}
		return true;
	}

	/**
	 * @return the status
	 */
	public Status getStatus()
	{
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}

}
