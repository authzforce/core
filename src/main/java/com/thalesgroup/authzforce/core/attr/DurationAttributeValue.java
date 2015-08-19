package com.thalesgroup.authzforce.core.attr;

import java.io.Serializable;
import java.util.Collections;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Superclass of duration attribute values, i.e. XML schema dayTime/yearMonthDuration values. The
 * choice of the Java type Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of
 * javax.xml.datatype package.
 */
public abstract class DurationAttributeValue extends PrimitiveAttributeValue<Duration>
{

	/**
	 * Instantiates duration attribute value from string representation
	 * 
	 * @param datatype
	 *            duration datatype
	 * @param val
	 *            string representation of the XML duration
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */
	protected DurationAttributeValue(DatatypeDef datatype, String val) throws IllegalArgumentException
	{
		this(datatype, new AttributeValueType(Collections.<Serializable> singletonList(val), datatype.datatypeURI(), null));
	}

	/**
	 * @see PrimitiveAttributeValue#BasePrimitiveAttributeValue(AttributeValueType)
	 */
	protected DurationAttributeValue(DatatypeDef datatype, AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(datatype, jaxbAttrVal);
	}

	// /**
	// *
	// * @param datatype
	// * @param val
	// * @throws IllegalArgumentException
	// */
	// protected DurationAttributeValue(String datatype, Duration val) throws
	// IllegalArgumentException
	// {
	// this(new AttributeValueType(Collections.<Serializable>singletonList(val.toString()),
	// datatype, null));
	// }

	/**
	 * Compares internal duration value ({@link Duration}) to another, using
	 * {@link Duration#compare(Duration)}
	 * 
	 * @param o
	 *            compared duration value
	 * @return result of {@link Duration#compare(Duration)}
	 * @throws IndeterminateEvaluationException
	 *             if and only if result is {@link DatatypeConstants#INDETERMINATE}
	 */
	public final int compare(DurationAttributeValue o) throws IndeterminateEvaluationException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IndeterminateEvaluationException(Status.STATUS_PROCESSING_ERROR, "Comparison of XML schema duration '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}
}
