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

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.FunctionExpression;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	/**
	 * Function call with constant result
	 *
	 * @param <RETURN_T>
	 *            return type
	 */
	private static final class ConstantResultFunctionCall<RETURN_T extends Value> implements FunctionCall<RETURN_T>
	{
		private final RETURN_T constant;
		private final Datatype<RETURN_T> constantDatatype;

		/**
		 * Constructor
		 * 
		 * @param constant
		 *            constant result
		 * @param constantDatatype
		 *            constant/return datatype
		 */
		public ConstantResultFunctionCall(final RETURN_T constant, final Datatype<RETURN_T> constantDatatype)
		{
			this.constant = constant;
			this.constantDatatype = constantDatatype;
		}

		@Override
		public RETURN_T evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			return constant;
		}

		@Override
		public Datatype<RETURN_T> getReturnType()
		{
			return constantDatatype;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplyExpression.class);

	private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();

	private static final IllegalArgumentException NULL_EXPRESSION_FACTORY_EXCEPTION = new IllegalArgumentException("Undefined expression factory argument");

	private static final IllegalArgumentException NULL_XACML_APPLY_ELEMENT_EXCEPTION = new IllegalArgumentException("Undefined argument: XACML Apply element");

	/**
	 * Creates instance from XACML Apply element
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
	 * @return Apply instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code xacmlApply} is invalid or {@code expFactory} is null; or function ID not supported/unknown; if {@code xprs} are invalid expressions, or invalid arguments for this
	 *             function; or if all {@code xprs} are static but calling the function statically (with these static arguments) failed
	 */
	public static ApplyExpression<?> getInstance(final ApplyType xacmlApply, final XPathCompiler xPathCompiler, final ExpressionFactory expFactory, final Deque<String> longestVarRefChain)
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
		final Datatype<?> subFuncReturnType;
		if (funcInputs.isEmpty())
		{
			subFuncReturnType = null;
		}
		else
		{
			final Expression<?> xpr0 = funcInputs.get(0);
			if (xpr0 instanceof FunctionExpression)
			{
				subFuncReturnType = ((FunctionExpression) xpr0).getValue().getReturnType();
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

		return new ApplyExpression<>(functionExp.getValue(), funcInputs, applyDesc);
	}

	private final transient FunctionCall<V> functionCall;

	private final transient V constantValue;

	/**
	 * Constructs an <code>Apply</code> instance.
	 * 
	 * @param function
	 *            function to apply to {@code xprs}. If this is a higher-order function, the sub-function is expected to be the first item of {@code xprs}
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
	private ApplyExpression(final Function<V> function, final List<Expression<?>> xprs, final String description) throws IllegalArgumentException
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
			throw new IllegalArgumentException("Error parsing Apply[Description = " + description + "]: Invalid args for function " + function, e);
		}

		/*
		 * Check whether the Apply Expression is constant -> try to pre-evaluate the result statically (out of context, i.e. in null context)
		 */
		V staticEvalResult = null;
		try
		{
			staticEvalResult = funcCall.evaluate(null);
			LOGGER.debug("Apply[Description = " + description + "]: static evaluation OK -> expression is constant -> optimizing: using constant result as evaluation result");
		}
		catch (final IndeterminateEvaluationException e)
		{
			LOGGER.debug("Apply[Description = " + description + "]: static evaluation failed -> expression is not constant -> not optimizing");
		}

		this.constantValue = staticEvalResult;
		/*
		 * If result is constant, create a function call with constant result, i.e. that returns the same constant (pre-evaluated) result right away, to avoid useless re-evaluation.
		 */
		this.functionCall = constantValue == null ? funcCall : new ConstantResultFunctionCall<>(constantValue, funcCall.getReturnType());
	}

	/** {@inheritDoc} */
	@Override
	public V getValue()
	{
		return constantValue;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the apply object using the given function. This will in turn call evaluate on all the given parameters, some of which may be other <code>Apply</code> objects.
	 */
	@Override
	public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		return functionCall.evaluate(context);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns the type of attribute that this object will return on a call to <code>evaluate</code> . In practice, this will always be the same as the result of calling <code>getReturnType</code> on
	 * the function used by this object.
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
