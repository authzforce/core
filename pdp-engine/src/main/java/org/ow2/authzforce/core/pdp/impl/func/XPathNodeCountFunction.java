/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.List;

import net.sf.saxon.s9api.XdmValue;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * A class that implements the optional XACML 3.0 xpath-node-count function.
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:3.0:function:xpath-node-count': This function SHALL take an 'urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression' as an
 * argument and evaluates to an 'http://www.w3.org/2001/XMLSchema#integer'. The value returned from the function SHALL be the count of the nodes within the node-set that match the given XPath
 * expression. If the &lt;Content&gt; element of the category to which the XPath expression applies to is not present in the request, this function SHALL return a value of zero.
 *
 * 
 * @version $Id: $
 */
final class XPathNodeCountFunction extends SingleParameterTypedFirstOrderFunction<IntegerValue, XPathValue>
{

	private static final class CallFactory
	{
		private static final class Call extends BaseFirstOrderFunctionCall<IntegerValue>
		{
			private final String invalidArgTypeMsg;
			private final String indeterminateArgMsg;
			private final String indeterminateArgEvalMsg;
			private final List<Expression<?>> checkedArgExpressions;

			private Call(final FirstOrderFunctionSignature<IntegerValue> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
			{
				super(functionSig, argExpressions, remainingArgTypes);
				this.checkedArgExpressions = argExpressions;
				invalidArgTypeMsg = "Function " + functionSig.getName() + ": Invalid type (expected = " + StandardDatatypes.XPATH + ") of arg#0: ";
				indeterminateArgMsg = "Function " + functionSig.getName() + ": Indeterminate arg #0";
				indeterminateArgEvalMsg = "Function " + functionSig.getName() + ": Error evaluating xpathExpression arg #0";
			}

			@Override
			public IntegerValue evaluate(final EvaluationContext context, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
			{
				// Evaluate the argument
				final XPathValue xpathVal;

				if (checkedArgExpressions.isEmpty())
				{
					try
					{
						xpathVal = XPathValue.class.cast(remainingArgs[0]);
					}
					catch (final ClassCastException e)
					{
						throw new IndeterminateEvaluationException(invalidArgTypeMsg + remainingArgs[0].getDataType(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
					}
				}
				else
				{
					final Expression<?> arg = checkedArgExpressions.get(0);
					try
					{
						xpathVal = Expressions.eval(arg, context, StandardDatatypes.XPATH);

					}
					catch (final IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(indeterminateArgMsg, e.getStatusCode(), e);
					}
				}

				final XdmValue xdmResult;
				try
				{
					xdmResult = xpathVal.evaluate(context);
				}
				catch (final IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(indeterminateArgEvalMsg, e.getStatusCode(), e);
				}

				return IntegerValue.valueOf(xdmResult.size());
			}
		}

		private final SingleParameterTypedFirstOrderFunctionSignature<IntegerValue, XPathValue> funcSig;

		private CallFactory(final SingleParameterTypedFirstOrderFunctionSignature<IntegerValue, XPathValue> functionSignature)
		{
			this.funcSig = functionSignature;
		}

		protected FirstOrderFunctionCall<IntegerValue> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes)
		{
			return new Call(funcSig, argExpressions, remainingArgTypes);
		}
	}

	private final CallFactory funcCallFactory;

	XPathNodeCountFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.INTEGER, true, Arrays.asList(StandardDatatypes.XPATH));
		this.funcCallFactory = new CallFactory(this.functionSignature);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<IntegerValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
