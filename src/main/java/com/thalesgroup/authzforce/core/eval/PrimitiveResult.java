package com.thalesgroup.authzforce.core.eval;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;

/**
 * Evaluation result with primitive attribute (single value), as opposed to bag result
 * (multi-valued)
 * 
 * @param <T>
 *            type of attribute value
 * 
 * @see ExpressionResult
 */
public class PrimitiveResult<T extends AttributeValue> extends ExpressionResult<T> implements JAXBBoundExpression<AttributeValueType, PrimitiveResult<T>>
{
	/**
	 * Single instance of ExpressionResult with false BooleanAttribute in them. This avoids the need
	 * to create new objects when performing boolean operations, which we do a lot of.
	 */
	public static PrimitiveResult<BooleanAttributeValue> FALSE = new PrimitiveResult<>(BooleanAttributeValue.FALSE, BooleanAttributeValue.TYPE);

	/**
	 * Single instance of EvaluationResults with true BooleanAttribute in them. This avoids the need
	 * to create new objects when performing boolean operations, which we do a lot of.
	 */
	public static PrimitiveResult<BooleanAttributeValue> TRUE = new PrimitiveResult<>(BooleanAttributeValue.TRUE, BooleanAttributeValue.TYPE);

	/**
	 * Creates instance for a valid result with attribute value
	 * 
	 * @param attrVal
	 * @param datatype
	 *            {@code attrVal} datatype
	 */
	public PrimitiveResult(T attrVal, DatatypeDef datatype)
	{
		super(attrVal, datatype);
	}

	/**
	 * Creates instance for an erroneous result with a given status as error info (no attribute
	 * value)
	 * 
	 * @param datatype
	 *            {@code attrVal} datatype
	 * @param status
	 */
	public PrimitiveResult(Status status, DatatypeDef datatype)
	{
		super(status, datatype);
	}

	/**
	 * Get instance with given boolean argument as attribute value
	 * 
	 * @param bool
	 *            boolean parameter
	 * @return instance
	 */
	public static PrimitiveResult<BooleanAttributeValue> getInstance(boolean bool)
	{
		return bool ? TRUE : FALSE;
	}

	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		return values == null || values.length == 0 ? null : XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(values[0]);
	}

	@Override
	public DatatypeDef getReturnType()
	{
		return datatype;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public PrimitiveResult<T> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}