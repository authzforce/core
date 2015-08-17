package com.thalesgroup.authzforce.core.eval;

import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import com.sun.xacml.ProcessingException;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * This class defines a VariableReference built from VariableReference after the referenced
 * VariableDefinition has been resolved and therefore its expression. As a result, Variables are
 * simply Expressions identified by an ID (VariableId) and replace original XACML VariableReferences
 * for actual evaluation.
 */
public class VariableReference extends VariableReferenceType implements JAXBBoundExpression<VariableReferenceType, ExpressionResult<? extends AttributeValue>>
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_VARIABLE_OPERATION_EXCEPTION = new UnsupportedOperationException("VariableReference.setVariableId() not allowed");
	private final Expression<? extends ExpressionResult<? extends AttributeValue>> expression;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType#setVariableId(java.lang
	 * .String)
	 */
	@Override
	public final void setVariableId(String value)
	{
		// variable ID cannot be changed (must remain identical during evaluation context)
		throw UNSUPPORTED_SET_VARIABLE_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		return expression.isStatic();
	}

	/**
	 * Get the referenced VariableDefinition's Expression. For example, used to check whether the
	 * actual expression type behind a VariableReference is a Function in Higher-order function's
	 * arguments
	 * 
	 * @return the expression
	 */
	public Expression<? extends ExpressionResult<? extends AttributeValue>> getReferencedExpression()
	{
		return expression;
	}

	private final List<String> longestVariableReferenceChain;

	/**
	 * Constructor that takes the reference identifier
	 * 
	 * @param varId
	 *            input VariableReference from XACML model
	 * @param varExpr
	 *            Expression of referenced VariableDefinition
	 * @param longestVarRefChain
	 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ...
	 *            -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or
	 *            more VariableReferences to V2)
	 */
	public VariableReference(String varId, Expression<? extends ExpressionResult<? extends AttributeValue>> varExpr, List<String> longestVarRefChain)
	{
		this.variableId = varId;
		this.expression = varExpr;
		this.longestVariableReferenceChain = longestVarRefChain;
	}

	/**
	 * Evaluates the referenced expression using the given context, and either returns an error or a
	 * resulting value. If this doesn't reference an evaluatable expression (eg, a single Function)
	 * then this will throw an exception.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	@Override
	public ExpressionResult<? extends AttributeValue> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		/*
		 * Even if context == null, evaluation may work because expression may be static/constant
		 * (e.g. AttributeValue or Apply on AttributeValues). This is called static evaluation and
		 * is used for pre-evaluating/optimizing certain function calls.
		 */
		if (context == null)
		{
			return expression.evaluate(null);
		}

		final ExpressionResult<? extends AttributeValue> ctxVal = context.getVariableValue(this.variableId);
		final ExpressionResult<? extends AttributeValue> result;
		if (ctxVal == null)
		{
			result = expression.evaluate(context);
			context.putVariableIfAbsent(this.variableId, result);
		} else
		{
			result = ctxVal;
		}

		return result;
	}

	/**
	 * Returns the type of the referenced expression.
	 * 
	 * @return the attribute return type of the referenced expression
	 * 
	 * @throws ProcessingException
	 *             if the type couldn't be resolved
	 */
	@Override
	public DatatypeDef getReturnType()
	{
		return expression.getReturnType();
	}

	/**
	 * @return the longestVariableReferenceChain
	 */
	public List<String> getLongestVariableReferenceChain()
	{
		return longestVariableReferenceChain;
	}

	@Override
	public JAXBElement<VariableReferenceType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createVariableReference(this);
	}

}
