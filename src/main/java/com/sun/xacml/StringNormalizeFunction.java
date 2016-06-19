/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

/**
 * string-normalize-* function
 *
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 * @version $Id: $
 */
public final class StringNormalizeFunction extends SingleParameterTypedFirstOrderFunction<StringValue, StringValue>
{

	/**
	 * Standard identifier for the string-normalize-space function.
	 */
	public static final String NAME_STRING_NORMALIZE_SPACE = XACML_NS_1_0 + "string-normalize-space";

	/**
	 * Standard identifier for the string-normalize-to-lower-case function.
	 */
	public static final String NAME_STRING_NORMALIZE_TO_LOWER_CASE = XACML_NS_1_0 + "string-normalize-to-lower-case";

	private interface StringNormalizer
	{
		StringValue normalize(StringValue value);
	}

	private static final class CallFactory
	{

		private final StringNormalizer strNormalizer;
		private final SingleParameterTypedFirstOrderFunctionSignature<StringValue, StringValue> funcSig;

		public CallFactory(SingleParameterTypedFirstOrderFunctionSignature<StringValue, StringValue> functionSignature, StringNormalizer stringNormalizer)
		{
			this.funcSig = functionSignature;
			this.strNormalizer = stringNormalizer;
		}

		private FirstOrderFunctionCall<StringValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<StringValue, StringValue>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(Deque<StringValue> argStack) throws IndeterminateEvaluationException
				{
					return strNormalizer.normalize(argStack.getFirst());
				}

			};
		}
	}

	private static final StringNormalizer STRING_NORMALIZE_SPACE_FUNCTION_CALL_FACTORY = new StringNormalizer()
	{
		@Override
		public StringValue normalize(StringValue value)
		{
			return value.trim();
		}

	};

	private static final StringNormalizer STRING_NORMALIZE_TO_LOWER_CASE_FUNCTION_CALL_FACTORY = new StringNormalizer()
	{
		@Override
		public StringValue normalize(StringValue value)
		{
			/*
			 * Specified by fn:lower-case function in [XF]. Looking at Saxon HE as our reference for Java open source implementation of XPath functions, we can check in Saxon implementation of
			 * fn:lower-case (LowerCase class), that this is equivalent to String#toLowerCase(); English locale to be used for Locale-insensitive strings, see String.toLowerCase()
			 */
			return value.toLowerCase(Locale.ENGLISH);
		}

	};

	private final CallFactory funcCallFactory;

	/**
	 * Creates a new <code>StringNormalizeFunction</code> object.
	 * 
	 * @param functionName
	 *            the standard XACML function URI
	 * 
	 */
	private StringNormalizeFunction(String functionName, StringNormalizer stringNormalizer)
	{
		super(functionName, StandardDatatypes.STRING_FACTORY.getDatatype(), false, Collections.singletonList(StandardDatatypes.STRING_FACTORY.getDatatype()));
		this.funcCallFactory = new CallFactory(functionSignature, stringNormalizer);
	}

	/**
	 * *-string-normalize-* function cluster
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "string-normalize", //
			new StringNormalizeFunction(NAME_STRING_NORMALIZE_SPACE, STRING_NORMALIZE_SPACE_FUNCTION_CALL_FACTORY), //
			new StringNormalizeFunction(NAME_STRING_NORMALIZE_TO_LOWER_CASE, STRING_NORMALIZE_TO_LOWER_CASE_FUNCTION_CALL_FACTORY));

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<StringValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

}
