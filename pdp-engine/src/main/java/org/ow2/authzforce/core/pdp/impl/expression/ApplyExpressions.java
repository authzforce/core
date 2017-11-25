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
package org.ow2.authzforce.core.pdp.impl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.FunctionExpression;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility methods pertaining to {@link ApplyType} evaluators.
 *
 */
public final class ApplyExpressions
{
	private ApplyExpressions()
	{
		// prevent instantiation
	}

	private static final class ConstantApplyExpression<V extends Value> extends ConstantExpression<V>
	{

		private ConstantApplyExpression(final Datatype<V> datatype, final V v) throws IllegalArgumentException
		{
			super(datatype, v);
		}

	}

	private static final class VariableApplyExpression<V extends Value> implements Expression<V>
	{

		private final FunctionCall<V> functionCall;

		private VariableApplyExpression(final FunctionCall<V> funcCall)
		{
			this.functionCall = funcCall;
		}

		@Override
		public Datatype<V> getReturnType()
		{
			return functionCall.getReturnType();
		}

		@Override
		public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			return functionCall.evaluate(context);
		}

		@Override
		public Optional<V> getValue()
		{
			return Optional.empty();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplyExpressions.class);

	private static final IllegalArgumentException NULL_EXPRESSION_FACTORY_EXCEPTION = new IllegalArgumentException("Undefined expression factory argument");

	private static final IllegalArgumentException NULL_XACML_APPLY_ELEMENT_EXCEPTION = new IllegalArgumentException("Undefined argument: XACML Apply element");

	private static <V extends Value> Expression<V> newInstance(final FunctionCall<V> functionCall, final String description)
	{
		/*
		 * Check whether the Apply Expression is constant -> try to pre-evaluate the result statically (out of context, i.e. in null context), to prevent useless re-evaluation of the same thing
		 */
		V staticEvalResult = null;
		try
		{
			staticEvalResult = functionCall.evaluate(null);
			LOGGER.debug("Apply[Description = " + description + "]: static evaluation OK -> expression is constant -> optimizing: using constant result as evaluation result");
		}
		catch (final IndeterminateEvaluationException e)
		{
			LOGGER.debug("Apply[Description = " + description + "]: static evaluation failed -> expression is not constant -> not optimizing");
		}

		return staticEvalResult == null ? new VariableApplyExpression<>(functionCall) : new ConstantApplyExpression<>(functionCall.getReturnType(), staticEvalResult);
	}

	/**
	 * Creates instance of Apply evaluator from XACML Apply element
	 *
	 * @param xacmlApply
	 *            XACML Apply element
	 * @param xPathCompiler
	 *            Enclosing Policy(Set)'s default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element.
	 * @param expFactory
	 *            expression factory for instantiating Apply's parameters
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references leading to this Apply, when evaluating a VariableDefinitions, i.e. list of VariableIds, such that V1-> V2 ->... -> Vn ->
	 *            <code>this</code>, where "V1 -> V2" means: the expression in VariableDefinition of V1 contains a VariableReference to V2. This is used to detect exceeding depth of VariableReference
	 *            reference when a new VariableReference occurs in a VariableDefinition's expression. May be null, if this expression does not belong to any VariableDefinition.
	 * @return Apply evaluator instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code xacmlApply} is invalid or {@code expFactory} is null; or function ID not supported/unknown; if {@code xprs} are invalid expressions, or invalid arguments for this
	 *             function; or if all {@code xprs} are static but calling the function statically (with these static arguments) failed
	 */
	public static Expression<?> newInstance(final ApplyType xacmlApply, final XPathCompiler xPathCompiler, final ExpressionFactory expFactory, final Deque<String> longestVarRefChain)
			throws IllegalArgumentException
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
				throw new IllegalArgumentException("Error parsing one of Apply [description=" + applyDesc + "]'s function arguments (Expressions)", e);
			}

			funcInputs.add(exprHandler);
		}

		// get the function instance
		// Determine whether this is a higher-order function, i.e. first parameter is a sub-function
		final Datatype<? extends AttributeValue> subFuncReturnType;
		if (funcInputs.isEmpty())
		{
			subFuncReturnType = null;
		}
		else
		{
			final Expression<?> xpr0 = funcInputs.get(0);
			if (xpr0 instanceof FunctionExpression)
			{
				subFuncReturnType = ((FunctionExpression) xpr0).getValue().get().getReturnType();
			}
			else
			{
				subFuncReturnType = null;
			}
		}

		final String functionId = xacmlApply.getFunctionId();
		final FunctionExpression functionExp;
		try
		{
			functionExp = expFactory.getFunction(functionId, subFuncReturnType);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Error parsing Apply[description=" + applyDesc + "]: Invalid return type (" + subFuncReturnType
					+ ") of sub-function (first-parameter) of Apply Function '" + functionId + "'", e);
		}

		if (functionExp == null)
		{
			throw new IllegalArgumentException("Error parsing Apply[description=" + applyDesc + "]: Invalid Function: function ID '" + functionId + "' not supported");
		}

		final Function<?> function = functionExp.getValue().get();

		// check that the given inputs work for the function and get the optimized functionCall
		final FunctionCall<?> funcCall;
		try
		{
			funcCall = function.newCall(Collections.unmodifiableList(funcInputs));
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Error parsing Apply[Description = " + applyDesc + "]: Invalid args for function " + function, e);
		}

		return newInstance(funcCall, applyDesc);
	}

}
