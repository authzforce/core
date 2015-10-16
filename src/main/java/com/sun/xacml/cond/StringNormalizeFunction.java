/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.cond;

import java.util.Deque;
import java.util.List;
import java.util.Locale;

import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunction;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import com.thalesgroup.authzforce.core.func.FunctionSet;

/**
 * *-string-normalize-* function
 * 
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public abstract class StringNormalizeFunction extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard identifier for the string-normalize-space function.
	 */
	public static final String NAME_STRING_NORMALIZE_SPACE = FUNCTION_NS_1 + "string-normalize-space";

	/**
	 * Standard identifier for the string-normalize-to-lower-case function.
	 */
	public static final String NAME_STRING_NORMALIZE_TO_LOWER_CASE = FUNCTION_NS_1 + "string-normalize-to-lower-case";

	/**
	 * Creates a new <code>StringNormalizeFunction</code> object.
	 * 
	 * @param functionName
	 *            the standard XACML function URI
	 * 
	 */
	public StringNormalizeFunction(String functionName)
	{
		super(functionName, DatatypeConstants.STRING.TYPE, false, DatatypeConstants.STRING.TYPE);
	}

	/**
	 * *-string-normalize-* function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "string-normalize", new NormalizeSpaceFunction(), new NormalizeToLowerCaseFunction());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected final FirstOrderFunctionCall<StringAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new EagerSinglePrimitiveTypeEval<StringAttributeValue, StringAttributeValue>(signature, DatatypeConstants.STRING.TYPE, argExpressions, remainingArgTypes)
		{
			@Override
			protected StringAttributeValue evaluate(Deque<StringAttributeValue> args) throws IndeterminateEvaluationException
			{
				return eval(args.getFirst());
			}

		};
	}

	protected abstract StringAttributeValue eval(StringAttributeValue value);

	private static class NormalizeSpaceFunction extends StringNormalizeFunction
	{

		private NormalizeSpaceFunction()
		{
			super(NAME_STRING_NORMALIZE_SPACE);
		}

		@Override
		protected StringAttributeValue eval(StringAttributeValue value)
		{
			return value.trim();
		}

	}

	private static class NormalizeToLowerCaseFunction extends StringNormalizeFunction
	{

		public NormalizeToLowerCaseFunction()
		{
			super(NAME_STRING_NORMALIZE_TO_LOWER_CASE);
		}

		@Override
		protected StringAttributeValue eval(StringAttributeValue value)
		{
			/*
			 * Specified by fn:lower-case function in [XF]. Looking at Saxon HE as our reference for
			 * Java open source implementation of XPath functions, we can check in Saxon
			 * implementation of fn:lower-case (LowerCase class), that this is equivalent to
			 * String#toLowerCase(); English locale to be used for Locale-insensitive strings, see
			 * String.toLowerCase()
			 */
			return value.toLowerCase(Locale.ENGLISH);
		}

	}

}
