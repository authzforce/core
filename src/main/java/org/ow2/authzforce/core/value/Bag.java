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
package com.thalesgroup.authzforce.core.datatypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.Expression.Value;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;

/**
 * Immutable bag of values (elements) as defined in §7.3.2 of XACML core specification (Attribute
 * bags): <i>The values in a bag are not ordered, and some of the values may be duplicates. There
 * SHALL be no notion of a bag containing bags, or a bag containing values of differing types; i.e.,
 * a bag in XACML SHALL contain only values that are of the same data-type</i>
 * <p>
 * Immutability is required to ensure values of a given attribute remain constant during an
 * evaluation of a request, as mandated by the XACML spec, section 7.3.5:
 * </p>
 * <p>
 * <i>
 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
 * </i>
 * </p>
 * 
 * @param <AV>
 *            type of every element in the bag
 */
public class Bag<AV extends AttributeValue<AV>> implements Value<Bag<AV>>, Iterable<AV>
{
	/**
	 * Bag datatype for bags of primitive datatypes
	 * 
	 * @param <AV>
	 */
	public static class Datatype<AV extends AttributeValue<AV>> extends Expression.Datatype<Bag<AV>>
	{

		private static final IllegalArgumentException NULL_BAG_ELEMENT_TYPE_EXCEPTION = new IllegalArgumentException("Undefined bag elementType arg");

		/**
		 * Bag datatype ID, for internal identification purposes. This is an invalid URI on purpose,
		 * to avoid conflict with any custom XACML datatype URI (datatype extension).
		 */
		private static final String ID = "#BAG#";

		/**
		 * Bad datatype constructor, same
		 * {@link Expression.Datatype#Datatype(Class, String, Datatype)}, except the last parameter
		 * is mandatory (non-null value)
		 * 
		 * @param bagClass
		 * @param elementType
		 * @throws IllegalArgumentException
		 *             if {@code elementType == null}
		 */
		protected Datatype(Class<Bag<AV>> bagClass, Expression.Datatype<AV> elementType) throws IllegalArgumentException
		{
			super(bagClass, ID, elementType);
			if (elementType == null)
			{
				throw NULL_BAG_ELEMENT_TYPE_EXCEPTION;
			}
		}

		/**
		 * Get BagDatatype instance
		 * 
		 * @param elementType
		 *            bag element datatype (primitive)
		 * @return BagDatatype
		 */
		public static final <AV extends AttributeValue<AV>> Datatype<AV> getInstance(Expression.Datatype<AV> elementType)
		{
			return new Bag<>(elementType).getDatatype();
		}

		/**
		 * Returns the bag element datatype (datatype of every element in a bag of this datatype)
		 * 
		 * @return bag element datatype
		 */
		public Expression.Datatype<AV> getElementType()
		{
			return (Expression.Datatype<AV>) this.subTypeParam;
		}

	}

	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined bag datatype argument");
	private static final IllegalArgumentException NON_BAG_DATATYPE_EXCEPTION = new IllegalArgumentException("Illegal bagDatatype argument: not bag");

	private final Datatype<AV> bagDatatype;
	private final Collection<AV> values;
	private final IndeterminateEvaluationException causeForEmpty;

	/**
	 * Constructor specifying bag datatype. On the contrary to {@link #Bag(Datatype)}, this
	 * constructor allows to reuse an existing bag Datatype object, saving the allocation of such
	 * object.
	 * 
	 * @param bagDatatype
	 *            bag datatype
	 * @param values
	 *            bag values (content).
	 * @param causeForEmpty
	 *            reason why this bag is empty if it is; null if it isn't
	 */
	private Bag(Datatype<AV> bagDatatype, Collection<AV> values, IndeterminateEvaluationException causeForEmpty)
	{
		assert values != null;

		if (bagDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (!bagDatatype.isBag())
		{
			throw NON_BAG_DATATYPE_EXCEPTION;
		}

		this.bagDatatype = bagDatatype;
		/*
		 * We need to make sure that this.values cannot be modified. However, using
		 * Collections.unmodifiableCollection(values) is a bad idea here, because the result
		 * (UnmodifiableCollection class) does not override Object#hashCode() and Object#equals().
		 * But we want deeper equals, i.e. take internal values of collection into account for
		 * hashCode() and equals(). At the same time, to preserve immutability, since the only way
		 * to modify is to call remove() on iterator(), we override iterator() method to make sure
		 * the remove() method is not supported.
		 */
		this.values = values;
		this.causeForEmpty = causeForEmpty;
	}

	/**
	 * Constructor based on element (primitive) datatype to be used for instantiating
	 * {@link BagDatatype}s
	 */
	protected Bag(Expression.Datatype<AV> elementType)
	{
		this.causeForEmpty = null;
		this.bagDatatype = new Datatype<>((Class<Bag<AV>>) this.getClass(), elementType);
		this.values = Collections.EMPTY_SET;
	}

	@Override
	public final Expression.Datatype<Bag<AV>> getReturnType()
	{
		return bagDatatype;
	}

	/**
	 * Get this bag's datatype, which happens to be the same as the evaluation result returned by
	 * {@link #getReturnType()}, since this is a constant value
	 * 
	 * @return this bag's datatype
	 */
	public final Datatype<AV> getDatatype()
	{
		return bagDatatype;
	}

	@Override
	public final boolean isStatic()
	{
		return true;
	}

	@Override
	public final JAXBElement<? extends ExpressionType> getJAXBElement()
	{
		/*
		 * TODO: we could return the Apply/AttributeDesignator/AttributeSelector that was evaluated
		 * to this bag. Not useful so far.
		 */
		return null;
	}

	@Override
	public final Bag<AV> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public final boolean isEmpty()
	{
		return values.isEmpty();
	}

	/**
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if
	 * reason is unknown.
	 * 
	 * @return reason why the bag is empty, if it is
	 */
	public final IndeterminateEvaluationException getReasonWhyEmpty()
	{
		return this.causeForEmpty;
	}

	/**
	 * Get bag size
	 * 
	 * @return bag size
	 */
	public final int size()
	{
		return values.size();
	}

	/**
	 * Returns true if this bag contains the specified element. More formally, returns true if and
	 * only if this bag contains at least one element e such that (v==null ? e==null : v.equals(e)).
	 * 
	 * @param v
	 *            element whose presence in this bag is to be tested
	 * @return true if this collection contains the specified element
	 */
	public final boolean contains(AV v)
	{
		return values.contains(v);
	}

	/**
	 * Get the single value in the bag if it is a singleton
	 * 
	 * @return the one-and-only one value in the bag; null if bag is empty or contains multiple
	 *         values
	 */
	public final AV getSingleValue()
	{
		return values.isEmpty() || values.size() > 1 ? null : values.iterator().next();
	}

	// cached toString()/hashCode() results
	protected int hashCode = 0;
	private transient volatile String toString = null; // Effective Java - Item 71

	@Override
	public boolean equals(Object other)
	{
		// Effective Java - Item 8
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof Bag))
		{
			return false;
		}

		final Bag<?> otherBag = (Bag<?>) other;
		return bagDatatype.getElementType().equals(otherBag.bagDatatype.getElementType()) && values.equals(otherBag.values);
	}

	@Override
	public int hashCode()
	{// immutable class -> cache this method result
		if (hashCode == 0)
		{
			/*
			 * There must be one-to-one mapping between datatype and datatype, so no need to hash
			 * both. Plus, causeForEmpty is just some optional info, ignore it in hash.
			 */
			hashCode = Objects.hash(bagDatatype.getElementType(), values);
		}

		return hashCode;
	}

	@Override
	public final String toString()
	{
		// immutable class -> cache this method result
		if (toString == null)
		{
			toString = "Bag[elementType = " + bagDatatype.getElementType() + ", values = " + values + ", causeForEmpty = null]";
		}

		return toString;
	}

	@Override
	public final Iterator<AV> iterator()
	{
		return values.iterator();
	}

	private static final class Empty<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private Empty(Datatype<AV> bagDatatype, IndeterminateEvaluationException causeForEmpty)
		{
			// Collections.EMPTY_SET is immutable
			super(bagDatatype, Collections.EMPTY_SET, causeForEmpty);
		}
	}

	private static final class Singleton<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private Singleton(Datatype<AV> bagDatatype, AV val)
		{
			// Collections.EMPTY_SET is immutable
			super(bagDatatype, Collections.singleton(val), null);
		}
	}

	private static final class Multi<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private Multi(Datatype<AV> bagDatatype, Collection<AV> values)
		{
			super(bagDatatype, Collections.unmodifiableCollection(values), null);
		}

		/**
		 * Override equals() to take internal values into account, because the internal
		 * Collections.unmodifiableCollection(...) does not do it
		 */
		@Override
		public boolean equals(Object other)
		{
			// Effective Java - Item 8
			if (this == other)
			{
				return true;
			}

			if (!(other instanceof Bag))
			{
				return false;
			}

			final Bag<?> otherBag = (Bag<?>) other;
			if (!getDatatype().getElementType().equals(otherBag.getDatatype().getElementType()))
			{
				return false;
			}

			final Iterator<AV> thisIterator = iterator();
			final Iterator<? extends AttributeValue<?>> otherIterator = otherBag.iterator();
			while (thisIterator.hasNext() && otherIterator.hasNext())
			{
				final AV o1 = thisIterator.next();
				final AttributeValue<?> o2 = otherIterator.next();
				if (!(o1 == null ? o2 == null : o1.equals(o2)))
				{
					return false;
				}
			}
			return !(thisIterator.hasNext() || otherIterator.hasNext());
		}

		/**
		 * Override hashCode() to take internal values into account, because the internal
		 * Collections.unmodifiableCollection(...) does not do it
		 */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = 1;
				for (final AV e : this)
				{
					hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
				}
			}

			return hashCode;
		}
	}

	private static final IllegalArgumentException NULL_BAG_ELEMENT_EXCEPTION = new IllegalArgumentException("Null value in bag");

	/**
	 * Creates instance of empty bag with given exception as reason for bag being empty (no
	 * attribute value), e.g. error occurred during evaluation
	 * 
	 * @param causeForEmpty
	 *            reason for empty bag (optional but should be specified whenever possible, to help
	 *            troubleshoot)
	 * @param bagDatatype
	 *            bag datatype
	 * @return bag
	 */
	public static final <AV extends AttributeValue<AV>> Bag<AV> empty(Datatype<AV> bagDatatype, IndeterminateEvaluationException causeForEmpty)
	{
		return new Empty<>(bagDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of bag containing val and only val value
	 * 
	 * @param bagDatatype
	 *            bag datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @return bag
	 */
	public static final <AV extends AttributeValue<AV>> Bag<AV> singleton(Datatype<AV> bagDatatype, AV val)
	{
		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		return new Singleton<>(bagDatatype, val);
	}

	/**
	 * Creates instance of bag of values.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which
	 *            order matters; or it may be a Set for result of bag/Set functions (intersection,
	 *            union...)
	 * @param bagDatatype
	 *            bag datatype
	 * @return bag
	 */
	public static final <AV extends AttributeValue<AV>> Bag<AV> getInstance(Datatype<AV> bagDatatype, Collection<AV> values)
	{
		if (values == null || values.isEmpty())
		{
			return new Empty<>(bagDatatype, null);
		}

		final Iterator<AV> valueIterator = values.iterator();
		final AV val0 = valueIterator.next();
		if (val0 == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (!valueIterator.hasNext())
		{
			// only one value
			return new Singleton<>(bagDatatype, val0);
		}

		// more than one value
		return new Multi<>(bagDatatype, values);
	}

}