package com.thalesgroup.authzforce.core.eval;

import java.lang.reflect.Array;
import java.util.Collection;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * ExpressionResult with bag of attribute values. Such result may be itself used as argument
 * expression in a bigger/enclosing expression.
 * 
 * @param <T>
 *            bag datatype class
 */
public class BagResult<T extends AttributeValue> extends ExpressionResult<T> implements Expression<BagResult<T>>
{
	private final Class<T> datatypeClass;

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
	 *            bag datatype
	 */
	public BagResult(Collection<T> values, Class<T> datatypeClass, DatatypeDef datatype)
	{
		super(values == null ? null : values.toArray((T[]) Array.newInstance(datatypeClass, values.size())), datatype);
		this.datatypeClass = datatypeClass;
	}

	/**
	 * Creates instance of a valid result with the bag of values
	 * 
	 * @param values
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            datatype
	 */
	public BagResult(T[] values, Class<T> datatypeClass, DatatypeDef datatype)
	{
		super(values, datatype);
		this.datatypeClass = datatypeClass;
	}

	/**
	 * Instantiates with a bag of one single value
	 * 
	 * @param val
	 *            the single value
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            datatype of {@code val}
	 */
	public BagResult(T val, Class<T> datatypeClass, DatatypeDef datatype)
	{
		this(ExpressionResult.toArray(val), datatypeClass, datatype);
	}

	/**
	 * Creates instance of erroneous result with given status as error info (no attribute value)
	 * 
	 * @param status
	 * @param datatypeClass
	 *            bag datatype class
	 * @param datatype
	 *            bag datatype
	 */
	public BagResult(Status status, Class<T> datatypeClass, DatatypeDef datatype)
	{
		super(status, datatype);
		this.datatypeClass = datatypeClass;
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

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public boolean isEmpty()
	{
		return values == null || values.length == 0;
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

}