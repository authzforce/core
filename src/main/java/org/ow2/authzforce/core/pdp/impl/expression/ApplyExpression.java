/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

/**
 * Evaluates XACML Apply
 *
 * @param <V>
 *            evaluation's return type
 * 
 * @version $Id: $
 */
public final class ApplyExpression<V extends Value> implements Expression<V>
{
	private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();

	private final transient FunctionCall<V> functionCall;

	private final transient boolean isStatic;

	private static final IllegalArgumentException NULL_EXPRESSION_FACTORY_EXCEPTION = new IllegalArgumentException(
			"Undefined expression factory argument");

	private static final IllegalArgumentException NULL_XACML_APPLY_ELEMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined argument: XACML Apply element");

	/**
	 * Creates instance from XACML Apply element
	 *
	 * @param xacmlApply
	 *            XACML Apply element
	 * @param xPathCompiler
	 *            Enclosing Policy(Set)'s default XPath compiler, corresponding to the Policy(Set)'s default XPath
	 *            version specified in {@link DefaultsType} element.
	 * @param expFactory
	 *            expression factory for instantiating Apply's parameters
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references leading to this Apply, when evaluating a
	 *            VariableDefinitions, i.e. list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>this</code>,
	 *            where "V1 -> V2" means: the expression in VariableDefinition of V1 contains a VariableReference to V2.
	 *            This is used to detect exceeding depth of VariableReference reference when a new VariableReference
	 *            occurs in a VariableDefinition's expression. May be null, if this expression does not belong to any
	 *            VariableDefinition.
	 * @return Apply instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code xacmlApply} is invalid or {@code expFactory} is null; or function ID not supported/unknown;
	 *             if {@code xprs} are invalid expressions, or invalid arguments for this function; or if all
	 *             {@code xprs} are static but calling the function statically (with these static arguments) failed
	 */
	public static ApplyExpression<?> getInstance(final ApplyType xacmlApply, final XPathCompiler xPathCompiler,
			final ExpressionFactory expFactory, final Deque<String> longestVarRefChain) throws IllegalArgumentException
	{
		if (xacmlApply == null)
		{
			throw NULL_XACML_APPLY_ELEMENT_EXCEPTION;
		}

		if (expFactory == null)
		{
			throw NULL_EXPRESSION_FACTORY_EXCEPTION;
		}

		final String applyDesc = xacmlApply.getDescription();
		// function args
		final List<JAXBElement<? extends ExpressionType>> applyArgExpressions = xacmlApply.getExpressions();
		final List<Expression<?>> funcInputs = new ArrayList<>(applyArgExpressions.size());
		for (final JAXBElement<? extends ExpressionType> exprElt : applyArgExpressions)
		{
			final Expression<?> exprHandler;
			try
			{
				exprHandler = expFactory.getInstance(exprElt.getValue(), xPathCompiler, longestVarRefChain);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
						"Error parsing one of Apply [description=" + applyDesc + "]'s function arguments (Expressions)",
						e);
			}

			funcInputs.add(exprHandler);
		}

		// get the function instance
		// Determine whether this is a higher-order function, i.e. first parameter is a sub-function
		final Datatype<?> subFuncReturnType;
		if (funcInputs.isEmpty())
		{
			subFuncReturnType = null;
		}
		else
		{
			final Expression<?> xpr0 = funcInputs.get(0);
			if (xpr0 instanceof Function<?>)
			{
				subFuncReturnType = xpr0.getReturnType();
			}
			else
			{
				subFuncReturnType = null;
			}
		}

		final String functionId = xacmlApply.getFunctionId();
		final Function<?> function;
		try
		{
			function = expFactory.getFunction(functionId, subFuncReturnType);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException(
					"Error parsing Apply[description=" + applyDesc + "]: Invalid return type (" + subFuncReturnType
							+ ") of sub-function (first-parameter) of Apply Function '" + functionId + "'",
					e);
		}

		if (function == null)
		{
			throw new IllegalArgumentException("Error parsing Apply[description=" + applyDesc
					+ "]: Invalid Function: function ID '" + functionId + "' not supported");
		}

		return new ApplyExpression<>(function, funcInputs, applyDesc);
	}

	/**
	 * Constructs an <code>Apply</code> instance.
	 * 
	 * @param function
	 *            function to apply to {@code xprs}. If this is a higher-order function, the sub-function is expected to
	 *            be the first item of {@code xprs}
	 * @param xprs
	 *            the arguments to the function
	 * @param originalXacmlExpressions
	 *            XACML Expressions from which {@code xprs} are parsed
	 * @param description
	 *            Description; may be null if no description
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code xprs} are invalid arguments for this function;
	 * 
	 */
	private ApplyExpression(final Function<V> function, final List<Expression<?>> xprs, final String description)
			throws IllegalArgumentException
	{
		assert function != null;

		// Others
		// check that the given inputs work for the function and get the optimized functionCall
		final FunctionCall<V> funcCall;
		try
		{
			funcCall = function.newCall(Collections.unmodifiableList(xprs));
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException(
					"Error parsing Apply[Description = " + description + "]: Invalid args for function " + function, e);
		}

		// Are all input expressions static?
		boolean allStatic = true;
		for (final Expression<?> xpr : xprs)
		{
			if (!xpr.isStatic())
			{
				allStatic = false;
				break;
			}
		}

		// if all input expressions static, the Apply is static, we can pre-evaluate the result
		if (allStatic)
		{
			final V staticEvalResult;
			try
			{
				staticEvalResult = funcCall.evaluate(null);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IllegalArgumentException("Invalid Apply[Description = " + description + "]: function "
						+ function + " is not applicable to arguments (all static): " + xprs, e);
			}

			/*
			 * Create a static function call, i.e. that returns the same constant (pre-evaluated) result right away, to
			 * avoid useless re-evaluation.
			 */
			this.functionCall = new FunctionCall<V>()
			{

				@Override
				public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
				{
					return staticEvalResult;
				}

				@Override
				public Datatype<V> getReturnType()
				{
					return funcCall.getReturnType();
				}

			};
		}
		else
		{
			this.functionCall = funcCall;
		}

		isStatic = allStatic;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isStatic()
	{
		return isStatic;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the apply object using the given function. This will in turn call evaluate on all the given parameters,
	 * some of which may be other <code>Apply</code> objects.
	 */
	@Override
	public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		return functionCall.evaluate(context);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns the type of attribute that this object will return on a call to <code>evaluate</code> . In practice, this
	 * will always be the same as the result of calling <code>getReturnType</code> on the function used by this object.
	 */
	@Override
	public Datatype<V> getReturnType()
	{
		return functionCall.getReturnType();
	}

	/** {@inheritDoc} */
	@Override
	public JAXBElement<ApplyType> getJAXBElement()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

}
