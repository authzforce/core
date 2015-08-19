package com.thalesgroup.authzforce.core.eval;

import java.lang.reflect.Array;
import java.util.Collection;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * Multi-valued expression evaluation result: bag of attribute values.
 * 
 * @param <T>
 *            bag datatype class
 */
public class BagResult<T extends AttributeValue> implements ExpressionResult<T, BagResult<T>>
{
	protected static <V extends AttributeValue> V[] toArray(V val)
	{
		if (val == null)
		{
			return null;
		}

		final V[] array = (V[]) Array.newInstance(val.getClass(), 1);
		array[0] = val;
		return array;
	}

	protected final T[] values;
	protected final DatatypeDef datatype;
	private final Class<T> datatypeClass;
	private final Status status;

	private BagResult(T[] values, Class<T> datatypeClass, DatatypeDef datatype, Status status)
	{
		this.datatype = datatype;
		this.values = values;
		this.datatypeClass = datatypeClass;
		this.status = status;
	}

	/**
	 * Creates instance of a valid result with the bag of values.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which
	 *            order matters; or it may be a Set for result of bag/Set functions (intersection,
	 *            union...)
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            bag datatype definition
	 */
	public BagResult(Collection<T> values, Class<T> datatypeClass, DatatypeDef datatype)
	{
		this(values == null ? null : values.toArray((T[]) Array.newInstance(datatypeClass, values.size())), datatypeClass, datatype);
	}

	/**
	 * Creates instance of a valid result with the bag of values
	 * 
	 * @param values
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            bag datatype definition
	 */
	public BagResult(T[] values, Class<T> datatypeClass, DatatypeDef datatype)
	{
		this(values, datatypeClass, datatype, null);
	}

	/**
	 * Instantiates with a bag of one single value
	 * 
	 * @param val
	 *            the single value
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            bag datatype definition
	 */
	public BagResult(T val, Class<T> datatypeClass, DatatypeDef datatype)
	{
		this(toArray(val), datatypeClass, datatype);
	}

	/**
	 * Creates instance of empty bag result with given status as reason for begin empty (no
	 * attribute value), e.g. error occurred during evaluation
	 * 
	 * @param status
	 *            reason for empty bag
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            bag datatype definition
	 */
	public BagResult(Status status, Class<T> datatypeClass, DatatypeDef datatype)
	{
		this(null, datatypeClass, datatype, status);
	}

	/**
	 * Returns the (first) attribute value or null if no value found
	 * 
	 * @return the first attribute value for a result bag, the single attribute value if not a bag;
	 *         or null in both cases if no value
	 */
	@Override
	public T value()
	{
		return this.values == null || values.length == 0 ? null : values[0];
	}

	/**
	 * Returns the attribute value(s) in the result
	 * 
	 * @return <code>Collection</code> of attribute value(s); may be empty if no value
	 */
	@Override
	public T[] values()
	{
		// the constructor makes sure it is unmodifiable (defensive copy)
		return values;
	}

	/**
	 * Get the type of the values in the bag
	 * 
	 * @return the bag value datatype class
	 */
	public Class<T> getDatatypeClass()
	{
		return datatypeClass;
	}

	@Override
	public DatatypeDef getReturnType()
	{
		return datatype;
	}

	@Override
	public BagResult<T> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public JAXBElement<? extends ExpressionType> getJAXBElement()
	{
		/*
		 * TODO: we could return the Apply/AttributeDesignator/AttributeSelector that was evaluated
		 * to this bag. Not useful so far.
		 */
		return null;
	}

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public boolean isEmpty()
	{
		return values == null || values.length == 0;
	}

	/**
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if
	 * reason is unknown.
	 * 
	 * @return reason why the bag is empty, if it is
	 */
	public Status getReasonWhyEmpty()
	{
		return status;
	}

}