/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core.eval;

import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * Super interface of any kinds of expression in a policy that the PDP evaluation engine may
 * evaluate in a given authorization request context:
 * <ul>
 * <li>AttributeValue</li>
 * <li>Apply</li>
 * <li>AttributeSelector</li>
 * <li>VariableReference</li>
 * <li>AttributeDesignator</li>
 * <li>Function</li>
 * </ul>
 * 
 * @param <V>
 *            type of result from evaluating the expression
 */
public interface Expression<V extends Expression.Value<?, V>>
{
	/**
	 * Expression evaluation result value. A Value may be itself used as an input {@link Expression}
	 * of a function for instance, therefore itself extends {@link Expression}. Not used for
	 * returning evaluation errors, i.e. "Indeterminate" results, in which case,
	 * {@link IndeterminateEvaluationException} should be used instead.
	 * 
	 * @param <AV>
	 *            concrete type subclass of:
	 *            <ul>
	 *            <li>the value itself if single-valued,</li>
	 *            <li>every element value if multi-valued (collection of values).</li>
	 *            </ul>
	 * @param <V>
	 *            concrete type subclass, same as V iff single-valued type, else multi-valued type
	 */
	public interface Value<AV extends AttributeValue<AV>, V extends Value<AV, V>> extends Expression<V>
	{
		/**
		 * Returns the (first) attribute value or null if no value found
		 * 
		 * @return the first attribute value if this is multi-valed (bag), the one and only
		 *         attribute value if this is single-valued (not a bag); or null in both cases if no
		 *         value
		 */
		AV one();

		/**
		 * Returns attribute value(s) in the result
		 * 
		 * @return all attribute value(s); may be empty if no value, but never null. It is the
		 *         responsability of the implementation to ensure empty (zero-length) array is
		 *         returned instead of null, according to "Effective Java (2nd Edition)" by J.
		 *         Bloch, "Item 43: Return empty arrays or collections, not nulls"
		 *         <p>
		 *         Although it is usally recommended to use Collection instead of array in API, we
		 *         use here array as return type to allow for type-safe generic cast, e.g. see
		 *         FirstOrderFunctionCall#evalBagArg(). In general, if we are expecting a bag of
		 *         type V (extends AttributeValue<V>) from a given input bag (of type originally
		 *         unknown), we want to be able to cast the input bag values simply and safely. In
		 *         this case, when using an array for the bag values returned by this method, we can
		 *         use the class of the array of V, e.g. some variable {@code Class<V[]> vClass} to
		 *         cast the input bag values to what we want as bag type:
		 *         {@code vClass.cast(inputBag.all())}. This would be more difficult with Collection
		 *         (requires to iterate over all collection items for type-safety). Indeed,
		 *         {@code vClass} is easy to instantiate for array {@code V[]} (e.g. for V =
		 *         StringAttributeValue, {@code vClass = StringAttributeValue[].class}, but not for
		 *         {@code Collection<V>} ({@code vClass = Collection<V>.class} is not valid for
		 *         instance).
		 *         </p>
		 */
		public AV[] all();

	}

	/**
	 * Expression evaluation return type
	 * 
	 * @param <V>
	 *            Java value type, which is one of the following:
	 */
	public static class Datatype<V extends Value<?, ?>>
	{

		private static final IllegalArgumentException NULL_VALUE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined value (datatype implementation) class arg");
		private static final IllegalArgumentException NULL_VALUE_TYPE_URI_EXCEPTION = new IllegalArgumentException("Undefined datatype ID arg");

		// private final boolean isBag;
		private final String id;
		private final Class<V> valueClass;
		protected final Datatype<?> subTypeParam;

		// cached method results
		private String toString = null;
		private int hashCode = 0;

		/**
		 * Instantiates generic datatype, i.e. taking a datatype parameter, like Java Generics, but
		 * more like Java Collection since there is only one type parameter in this case.
		 * 
		 * @param valueClass
		 *            Java (implementation) class of values of this datatype
		 * @param id
		 *            datatype ID
		 * @param subType
		 *            datatype of sub-elements
		 * @throws IllegalArgumentException
		 *             if {@code valueClass == null || id == null }
		 */
		protected Datatype(Class<V> valueClass, String id, Datatype<?> typeParameter) throws IllegalArgumentException
		{
			if (valueClass == null)
			{
				throw NULL_VALUE_CLASS_EXCEPTION;
			}

			if (id == null)
			{
				throw NULL_VALUE_TYPE_URI_EXCEPTION;
			}

			this.valueClass = valueClass;
			this.id = id;
			this.subTypeParam = typeParameter;
		}

		/**
		 * Instantiates primitive datatype
		 * 
		 * @param valueClass
		 *            class implementing this primitive datatype
		 * 
		 * @param id
		 *            datatype ID (e.g. XACML datatype URI) which identifies this primitive datatype
		 * @throws IllegalArgumentException
		 *             if {@code valueClass == null || id == null }
		 */
		public Datatype(Class<V> valueClass, String id) throws IllegalArgumentException
		{
			this(valueClass, id, null);
		}

		/**
		 * Get value class, which is the Java (implementation) class of all instances of this
		 * datatype
		 * 
		 * @return value class
		 */
		public Class<V> getValueClass()
		{
			return valueClass;
		}

		/**
		 * Get ID (URI) of this datatype
		 * 
		 * @return datatype ID
		 */
		public String getId()
		{
			return this.id;
		}

		/**
		 * Return true iff bag datatype
		 * 
		 * @return true iff it is a bag datatype
		 */
		public boolean isBag()
		{
			return subTypeParam != null;
		}

		/**
		 * Return datatype of sub-elements for this datatype, e.g. the bag element datatype
		 * (datatype of every element in a bag of this datatype); null if this is a primitive type
		 * (no sub-elements)
		 * 
		 * @return datatype parameter, null for non-bag/primitive values
		 */
		public Datatype<?> getTypeParameter()
		{
			return subTypeParam;
		}

		/**
		 * Casts a value to the class or interface represented by this datatype.
		 * 
		 * @param val
		 *            value to be cast
		 * @return the value after casting, or null if {@code val} is null
		 * @throws ClassCastException
		 *             if the value is not null and is not assignable to the type V.
		 */
		public V cast(Value<?, ?> val) throws ClassCastException
		{
			return this.valueClass.cast(val);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = subTypeParam == null ? id : id + "<" + subTypeParam + ">";
			}

			return toString;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				// there should be one-to-one mapping between valueClass and id, so hashing
				// only one of these two is necessary
				hashCode = Objects.hash(valueClass, id, subTypeParam);
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}

			final Datatype<?> other = (Datatype<?>) obj;
			if (!valueClass.equals(other.valueClass))
			{
				return false;
			}
			if (!this.id.equals(other.id))
			{
				return false;
			}

			if (this.subTypeParam == null)
			{
				if (other.subTypeParam == null)
				{
					return true;
				}

				return false;
			}

			// this.elementDatatype != null
			if (other.subTypeParam == null)
			{
				return false;
			}

			if (!this.subTypeParam.equals(other.subTypeParam))
			{
				return false;
			}

			return true;
		}
	}

	/**
	 * Gets the expected return type of the expression if evaluated.
	 * 
	 * @return expression evaluation's return type
	 */
	Datatype<V> getReturnType();

	/**
	 * Evaluates the expression using the given context.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation that may be a single value T (e.g. function result,
	 *         AttributeValue, Condition, Match...) or bag of values (e.g. AttributeDesignator,
	 *         AttributeSelector)
	 * @throws IndeterminateEvaluationException
	 *             if evaluation "Indeterminate" (see XACML core specification)
	 */
	V evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Tells whether this expression is actually a static value, i.e. independent from the
	 * evaluation context (e.g. AttributeValue, VariableReference to AttributeValue...). This
	 * enables expression consumers to do optimizations, e.g. functions may pre-compile/pre-evaluate
	 * parts of their inputs knowing some are constant values.
	 * 
	 * @return true iff a static/fixed/constant value
	 */
	boolean isStatic();

	/**
	 * Gets the instance of the Java representation of the XACML-schema-defined Expression
	 * bound/equivalent to this expression
	 * 
	 * @return JAXB element equivalent
	 */
	JAXBElement<? extends ExpressionType> getJAXBElement();

	/**
	 * Utility class that provide functions to help evaluate Expressions
	 * 
	 */
	public static class Utils
	{
		private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
		private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException("No value returned by arg evaluation in the current context", Status.STATUS_PROCESSING_ERROR);

		/**
		 * Evaluate single-valued (primitive) argument expression
		 * 
		 * @param arg
		 *            argument expression
		 * @param context
		 *            context in which argument expression is evaluated
		 * @param returnType
		 *            type of returned attribute value
		 * @return result of evaluation
		 * @throws IndeterminateEvaluationException
		 *             if no value returned from evaluation, or <code>returnType</code> is not a
		 *             supertype of the result value datatype
		 */
		public static <AV extends AttributeValue<?>> AV evalSingle(Expression<?> arg, EvaluationContext context, Class<AV> returnType) throws IndeterminateEvaluationException
		{
			final AttributeValue<?> val = arg.evaluate(context).one();
			LOGGER.debug("evalSingle( arg = <{}>, <context>, expectedType = <{}> ) -> <{}>", arg, returnType, val);
			if (val == null)
			{
				throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
			}

			try
			{
				return returnType.cast(val);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Invalid expresion evaluation result type: " + val.getClass().getName() + ". Expected: " + returnType.getName(), Status.STATUS_PROCESSING_ERROR, e);
			}
		}
	}

}
