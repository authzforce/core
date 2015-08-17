package com.thalesgroup.authzforce.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.JAXBBoundExpression;
import com.thalesgroup.authzforce.core.func.BaseFunction;
import com.thalesgroup.authzforce.core.func.FunctionCall;

/**
 * Evaluates XACML Apply
 */
public class Apply extends ApplyType implements JAXBBoundExpression<ApplyType, ExpressionResult<? extends AttributeValue>>
{
	private final FunctionCall<? extends ExpressionResult<? extends AttributeValue>> functionCall;

	private final boolean isStatic;

	private static final UnsupportedOperationException UNSUPPORTED_FUNCTION_ID_CHANGE_EXCEPTION = new UnsupportedOperationException("FunctionId is read-only");

	private static final IllegalArgumentException NULL_FUNCTION_ID_EXCEPTION = new IllegalArgumentException("Undefined function ID argument");

	private static final IllegalArgumentException NULL_EXPRESSION_FACTORY_EXCEPTION = new IllegalArgumentException("Undefined expression factory argument");

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType#getExpressions()
	 */
	@Override
	public final List<JAXBElement<? extends ExpressionType>> getExpressions()
	{
		// Make it read-only to avoid being de-synchronized with functionCall field derived from it
		return Collections.unmodifiableList(super.getExpressions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType#setFunctionId(java.lang.String)
	 */
	@Override
	public final void setFunctionId(String value)
	{
		// Make it read-only to avoid being de-synchronized with functionCall field derived from it
		throw UNSUPPORTED_FUNCTION_ID_CHANGE_EXCEPTION;
	}

	private static List<Expression<? extends ExpressionResult<? extends AttributeValue>>> getFunctionInputs(List<JAXBElement<? extends ExpressionType>> expressions, ExpressionFactory expFactory, DefaultsType policyDefaults, List<String> longestVarRefChain) throws ParsingException
	{
		// function args
		final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs = new ArrayList<>();
		for (final JAXBElement<? extends ExpressionType> exprElt : expressions)
		{
			final Expression<? extends ExpressionResult<? extends AttributeValue>> exprHandler;
			try
			{
				exprHandler = expFactory.getInstance(exprElt.getValue(), policyDefaults, longestVarRefChain);
			} catch (ParsingException e)
			{
				throw new ParsingException("Error parsing one of Apply's function arguments (Expressions)", e);
			}

			inputs.add(exprHandler);
		}

		return inputs;
	}

	/**
	 * Creates instance from XACML Apply element
	 * 
	 * @param xacmlApply
	 *            XACML Apply element
	 * @param expFactory
	 *            expression factory for instantiating Apply's parameters
	 * @param policyDefaults
	 *            policy(set) default parameters, e.g. XPath version
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references leading to this Apply, when
	 *            evaluating a VariableDefinitions, i.e. list of VariableIds, such that V1-> V2
	 *            ->... -> Vn -> <code>this</code>, where "V1 -> V2" means: the expression in
	 *            VariableDefinition of V1 contains a VariableReference to V2. This is used to
	 *            detect exceeding depth of VariableReference reference when a new VariableReference
	 *            occurs in a VariableDefinition's expression. May be null, if this expression does
	 *            not belong to any VariableDefinition.
	 * @throws ParsingException
	 */
	public Apply(ApplyType xacmlApply, DefaultsType policyDefaults, ExpressionFactory expFactory, List<String> longestVarRefChain) throws ParsingException
	{
		this(xacmlApply.getFunctionId(), getFunctionInputs(xacmlApply.getExpressions(), expFactory, policyDefaults, longestVarRefChain), xacmlApply.getDescription(), expFactory);
	}

	/**
	 * Constructs an <code>Apply</code> instance.
	 * 
	 * @param functionId
	 *            identifies the function to apply to the elements in the apply. If this is a
	 *            higher-order function, the sub-function is expected to be the first item of
	 *            {@code xprs}
	 * @param xprs
	 *            the contents of the apply which will be the parameters to the function, each of
	 *            which is an <code>Expression</code>
	 * @param description
	 *            Description; may be null if no description
	 * @param expFactory
	 *            expression factory that instantiates the evaluable Function as required by the
	 *            evaluation engine
	 * 
	 * @throws ParsingException
	 *             if the input expressions don't match the signature of the function
	 * @throws IllegalArgumentException
	 *             if {@code functionId} or {@code expFactory} is null
	 */
	public Apply(String functionId, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> xprs, String description, ExpressionFactory expFactory) throws ParsingException, IllegalArgumentException
	{
		if (functionId == null)
		{
			throw NULL_FUNCTION_ID_EXCEPTION;
		}

		if (expFactory == null)
		{
			throw NULL_EXPRESSION_FACTORY_EXCEPTION;
		}

		// set the JAXB and internal member attributes
		this.description = description;
		this.functionId = functionId;

		// get the function instance
		// Determine whether this is a higher-order function, i.e. first parameter is a sub-function
		final BaseFunction<? extends ExpressionResult<? extends AttributeValue>> subFunc;
		if (xprs.isEmpty())
		{
			subFunc = null;
		} else
		{
			final Expression<? extends ExpressionResult<? extends AttributeValue>> xpr0 = xprs.get(0);
			if (xpr0 instanceof Function<?>)
			{
				if (!(xpr0 instanceof BaseFunction))
				{
					throw new ParsingException(this + ": Invalid sub-function used as first argument: " + xpr0 + ". Expected to be a first-order function (subclass of " + BaseFunction.class + ").");
				}

				subFunc = (BaseFunction<?>) xpr0;
			} else
			{
				subFunc = null;
			}
		}

		final Function<? extends ExpressionResult<? extends AttributeValue>> function;
		if (subFunc == null)
		{
			function = expFactory.getFunction(functionId);
		} else
		{
			try
			{
				function = expFactory.getHigherOrderFunction(functionId, subFunc);
			} catch (UnknownIdentifierException uie)
			{
				throw new ParsingException(this + ": Invalid return type of function '" + subFunc + "' used as sub-function of Apply Function '" + functionId + "'", uie);
			}
		}

		if (function == null)
		{
			throw new ParsingException(this + ": Invalid Apply Function: function ID '" + functionId + "' not supported");
		}

		// check that the given inputs work for the function and get the optimized functionCall
		final FunctionCall<? extends ExpressionResult<? extends AttributeValue>> funcCall = function.parseInputs(Collections.unmodifiableList(xprs));

		// Are all input expressions static?
		boolean allStatic = true;
		for (final Expression<? extends ExpressionResult<? extends AttributeValue>> xpr : xprs)
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
			final ExpressionResult<? extends AttributeValue> staticEvalResult;
			try
			{
				staticEvalResult = funcCall.evaluate(null);
			} catch (IndeterminateEvaluationException e)
			{
				throw new ParsingException(this + ": Error pre-evaluating the function " + function + " with static arguments: " + xprs);
			}

			/*
			 * Create a static function call, i.e. that returns the same constant (pre-evaluated)
			 * result right away, to avoid useless re-evaluation.
			 */
			this.functionCall = new FunctionCall<ExpressionResult<? extends AttributeValue>>()
			{

				@Override
				public ExpressionResult<? extends AttributeValue> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
				{
					return staticEvalResult;
				}

				@Override
				public DatatypeDef getReturnType()
				{
					return funcCall.getReturnType();
				}

			};
		} else
		{
			this.functionCall = funcCall;
		}

		isStatic = allStatic;
	}

	@Override
	public boolean isStatic()
	{
		return isStatic;
	}

	/**
	 * Evaluates the apply object using the given function. This will in turn call evaluate on all
	 * the given parameters, some of which may be other <code>Apply</code> objects.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to evaluate this apply object
	 * @throws IndeterminateEvaluationException
	 */
	@Override
	public ExpressionResult<? extends AttributeValue> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return functionCall.evaluate(context);
	}

	/**
	 * Returns the type of attribute that this object will return on a call to <code>evaluate</code>
	 * . In practice, this will always be the same as the result of calling
	 * <code>getReturnType</code> on the function used by this object.
	 * 
	 * @return the type returned by <code>evaluate</code>
	 */
	@Override
	public DatatypeDef getReturnType()
	{
		return functionCall.getReturnType();
	}

	@Override
	public JAXBElement<ApplyType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createApply(this);
	}

}
