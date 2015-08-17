package com.thalesgroup.authzforce.core.attr;

import java.io.Serializable;
import java.util.Collections;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Superclass of date/time attribute values, i.e. XML schema date/time values. The choice of the
 * Java type <code>XMLGregorianCalendar</code> is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 * @param <T>
 *            type of result returned by arithmetic functions with this type of arguments:
 *            {@link #add(DurationAttributeValue)}, {@link #subtract(DurationAttributeValue)}, etc.
 *            Basically, we expect that arithmetic functions applied to this type T will return a
 *            result of the same type T.
 */
/*
 * Do not replace "Time" with "Temporal" in the class name because it is NOT used for Durations
 * (dayTimeDuration, yearMonthDuration...)
 */
public abstract class BaseTimeAttributeValue<T extends BaseTimeAttributeValue<T>> extends PrimitiveAttributeValue<XMLGregorianCalendar>
{
	protected abstract QName getXmlSchemaType();

	/**
	 * Instantiate date/time attribute value
	 * 
	 * @param datatype
	 *            datatype URI
	 * @param val
	 *            string representation of instance of this datatype
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	protected BaseTimeAttributeValue(String datatype, String val) throws IllegalArgumentException
	{
		this(new AttributeValueType(Collections.<Serializable> singletonList(val), datatype, null));
	}

	/**
	 * @see PrimitiveAttributeValue#BasePrimitiveAttributeValue(AttributeValueType)
	 */
	protected BaseTimeAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	/**
	 * Instantiate date/time attribute value
	 * 
	 * @param datatype
	 *            datatype URI
	 * @param val
	 *            string representation of instance of this datatype
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	protected BaseTimeAttributeValue(String datatype, XMLGregorianCalendar val) throws IllegalArgumentException
	{
		super(datatype, val, val.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue#parse(java.lang.String)
	 */
	@Override
	protected XMLGregorianCalendar parse(String stringForm)
	{
		final XMLGregorianCalendar cal = XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(stringForm);
		final QName expectedSchemaType = getXmlSchemaType();
		if (!cal.getXMLSchemaType().equals(expectedSchemaType))
		{
			throw new IllegalArgumentException("Invalid XML schema type (" + cal.getXMLSchemaType() + ") of value '" + stringForm + "'. Expected: " + expectedSchemaType);
		}

		return cal;
	}

	/**
	 * Add duration to this time
	 * 
	 * @param durationVal
	 *            duration value
	 * @return this + durationVal
	 */
	abstract public T add(DurationAttributeValue durationVal);

	/**
	 * Subtract duration to this time
	 * 
	 * @param durationVal
	 *            duration value
	 * @return this - durationVal
	 */
	abstract public T subtract(DurationAttributeValue durationVal);

	/**
	 * Compares internal date/time value ({@link XMLGregorianCalendar}) to another, using
	 * {@link XMLGregorianCalendar#compare(XMLGregorianCalendar)}
	 * 
	 * @param o
	 *            compared date/time value
	 * @return result of {@link XMLGregorianCalendar#compare(XMLGregorianCalendar)}
	 * @throws IndeterminateEvaluationException
	 *             if and only if result is {@link DatatypeConstants#INDETERMINATE}
	 */
	public final int compare(BaseTimeAttributeValue<T> o) throws IndeterminateEvaluationException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IndeterminateEvaluationException(Status.STATUS_PROCESSING_ERROR, "Comparison of XML schema date/time '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}
}
