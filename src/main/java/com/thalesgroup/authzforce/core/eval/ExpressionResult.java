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

import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * This is used for valid results of evaluation of Expressions. This is not used for returning
 * errors because Exceptions are a better and more natural way (from a Java standpoint) to propagate
 * error information with a full traceable stacktrace. The XACML Status is used for returning errors
 * but is not usable for stacktrace if you want more than error codes in your error info, which is
 * the case here. (The StatusCode is the only recursive structure in Status and therefore the only
 * part that could be used for stacktraces).
 * <p>
 * Therefore for "Indeterminate" results of evaluation, use {@link IndeterminateEvaluationException}
 * instead. Most of the error information will be used at least for logging and troubleshooting. But
 * for the final response of the PDP to the PEP, at least in production, most of the errors
 * will/should be filtered out.
 * </p>
 * <p>
 * Last but not least, an ExpressionResult may be itself used as an input {@link Expression} of a
 * function for instance, therefore extends {@link Expression}.
 * </p>
 * 
 * @param <T>
 *            type of attribute value(s) in result
 * @param <V>
 *            the type of ExpressionResult implementation itself (e.g. AttributeValue)
 */
public interface ExpressionResult<T extends AttributeValue, V extends ExpressionResult<T, V>> extends Expression<V>
{
	/**
	 * Returns the (first) attribute value or null if no value found
	 * 
	 * @return the first attribute value for a result bag, the single attribute value if not a bag;
	 *         or null in both cases if no value
	 */
	T value();

	/**
	 * Returns the attribute value(s) in the result
	 * 
	 * @return <code>Collection</code> of attribute value(s); may be empty if no value
	 */
	public T[] values();

}
