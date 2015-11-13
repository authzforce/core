package org.ow2.authzforce.core.expression;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;

/**
 * 
 * Expression wrapper for constant values to be used as Expression, e.g. to be used as function argument. This is an alternative to {@link Value} extending
 * {@link Expression} directly, which would break the Acyclic Dependency principle since {@link Expression} already has a reference to {@link Value}.
 * 
 * @param <V>
 *            concrete value type
 *
 */
public abstract class ValueExpression<V extends Value> implements Expression<V>
{
	private static final IllegalArgumentException UNDEF_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined expression return type");
	private static final IllegalArgumentException UNDEF_VALUE_EXCEPTION = new IllegalArgumentException("Undefined value");
	private final Datatype<V> datatype;
	protected final V value;

	/**
	 * Creates instance of constant value expression
	 * 
	 * @param datatype
	 *            value datatype
	 * @param v
	 *            constant value
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || v == null}
	 * 
	 */
	protected ValueExpression(Datatype<V> datatype, V v) throws IllegalArgumentException
	{
		if (datatype == null)
		{
			throw UNDEF_DATATYPE_EXCEPTION;
		}

		if (v == null)
		{
			throw UNDEF_VALUE_EXCEPTION;
		}

		this.datatype = datatype;
		this.value = v;
	}

	@Override
	public final Datatype<V> getReturnType()
	{
		return this.datatype;
	}

	/**
	 * Returns the value itself
	 */
	@Override
	public final V evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this.value;
	}

	@Override
	public final boolean isStatic()
	{
		return true;
	}

	/**
	 * Gets underlying value
	 * 
	 * @return value wrapped in this expression
	 */
	public V getValue()
	{
		return this.value;
	}
}
