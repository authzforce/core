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
import java.util.List;

import net.sf.saxon.s9api.XdmValue;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.Expressions;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.pdp.impl.value.XPathValue;

/**
 * A class that implements the optional XACML 3.0 xpath-node-count function.
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:3.0:function:xpath-node-count': This function SHALL take an
 * 'urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression' as an argument and evaluates to an 'http://www.w3.org/2001/XMLSchema#integer'. The value returned
 * from the function SHALL be the count of the nodes within the node-set that match the given XPath expression. If the &lt;Content&gt; element of the category
 * to which the XPath expression applies to is not present in the request, this function SHALL return a value of zero.
 * 
 */
public final class XPathNodeCountFunction extends FirstOrderFunction.SingleParameterTyped<IntegerValue, XPathValue>
{
	private static final String NAME = XACML_NS_3_0 + "xpath-node-count";

	/**
	 * Singleton instance of "and" logical function
	 */
	public static final XPathNodeCountFunction INSTANCE = new XPathNodeCountFunction();

	private static final class CallFactory
	{

		private static final class Call extends FirstOrderFunctionCall<IntegerValue>
		{
			private final List<Expression<?>> checkedArgExpressions;

			private Call(FunctionSignature<IntegerValue> functionSig, List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
					throws IllegalArgumentException
			{
				super(functionSig, argExpressions, remainingArgTypes);
				this.checkedArgExpressions = argExpressions;
			}

			@Override
			public IntegerValue evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
			{
				// Evaluate the argument
				final XPathValue xpathVal;

				if (checkedArgExpressions.isEmpty())
				{
					try
					{
						xpathVal = XPathValue.class.cast(remainingArgs[0]);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE + remainingArgs[0].getDataType(),
								StatusHelper.STATUS_PROCESSING_ERROR, e);
					}
				} else
				{
					final Expression<?> arg = checkedArgExpressions.get(0);
					try
					{
						xpathVal = Expressions.eval(arg, context, DatatypeConstants.XPATH.TYPE);

					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(INDETERMINATE_ARG_MESSAGE, StatusHelper.STATUS_PROCESSING_ERROR, e);
					}
				}

				final XdmValue xdmResult;
				try
				{
					xdmResult = xpathVal.evaluate(context);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(INDETERMINATE_ARG_EVAL_MESSAGE, StatusHelper.STATUS_PROCESSING_ERROR, e);
				}

				return new IntegerValue(xdmResult.size());
			}
		}

		private static final String INVALID_ARG_TYPE_MESSAGE = "Function " + NAME + ": Invalid type (expected = " + DatatypeConstants.XPATH.TYPE
				+ ") of arg#0: ";
		private static final String INDETERMINATE_ARG_MESSAGE = "Function " + NAME + ": Indeterminate arg #0";
		private static final String INDETERMINATE_ARG_EVAL_MESSAGE = "Function " + NAME + ": Error evaluating xpathExpression arg #0";

		private final FunctionSignature.SingleParameterTyped<IntegerValue, XPathValue> funcSig;

		private CallFactory(FunctionSignature.SingleParameterTyped<IntegerValue, XPathValue> functionSignature)
		{
			this.funcSig = functionSignature;
		}

		protected FirstOrderFunctionCall<IntegerValue> getInstance(final List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			return new Call(funcSig, argExpressions, remainingArgTypes);
		}
	}

	private final CallFactory funcCallFactory;

	private XPathNodeCountFunction()
	{
		super(NAME, DatatypeConstants.INTEGER.TYPE, true, Arrays.asList(DatatypeConstants.XPATH.TYPE));
		this.funcCallFactory = new CallFactory(this.functionSignature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	public FirstOrderFunctionCall<IntegerValue> newCall(final List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
			throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
