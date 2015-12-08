/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.value;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.ow2.authzforce.core.IndeterminateEvaluationException;

/**
 * Immutable bag of values (elements) as defined in §7.3.2 of XACML core specification (Attribute bags): <i>The values in a bag are not ordered, and some of the
 * values may be duplicates. There SHALL be no notion of a bag containing bags, or a bag containing values of differing types; i.e., a bag in XACML SHALL
 * contain only values that are of the same data-type</i>
 * <p>
 * Immutability is required to ensure values of a given attribute remain constant during an evaluation of a request, as mandated by the XACML spec, section
 * 7.3.5:
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
public class Bag<AV extends AttributeValue> implements Value, Iterable<AV>
{
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined bag datatype argument");

	private final Collection<AV> values;
	private final IndeterminateEvaluationException causeForEmpty;
	protected Datatype<AV> elementDatatype;

	/**
	 * Constructor specifying bag datatype. On the contrary to {@link #Bag(BagDatatype)}, this constructor allows to reuse an existing bag Datatype object,
	 * saving the allocation of such object.
	 * 
	 * @param elementDatatype
	 * 
	 * @param bagDatatype
	 *            bag datatype
	 * @param values
	 *            bag values (content).
	 * @param causeForEmpty
	 *            reason why this bag is empty if it is; null if it isn't
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	protected Bag(Datatype<AV> elementDatatype, Collection<AV> values, IndeterminateEvaluationException causeForEmpty) throws IllegalArgumentException
	{
		assert values != null;

		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		this.elementDatatype = elementDatatype;
		/*
		 * We need to make sure that this.values cannot be modified. However, using Collections.unmodifiableCollection(values) is a bad idea here, because the
		 * result (UnmodifiableCollection class) does not override Object#hashCode() and Object#equals(). But we want deeper equals, i.e. take internal values
		 * of collection into account for hashCode() and equals(). At the same time, to preserve immutability, since the only way to modify is to call remove()
		 * on iterator(), we override iterator() method to make sure the remove() method is not supported.
		 */
		this.values = values;
		this.causeForEmpty = causeForEmpty;
	}

	/**
	 * Empty bag Constructor
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	protected Bag(Datatype<AV> elementType) throws IllegalArgumentException
	{
		this(elementType, Collections.<AV> emptySet(), null);
	}

	/**
	 * Get this bag's element datatype (datatype of every element in the bag)
	 *
	 * @return this bag's element datatype
	 */
	public final Datatype<AV> getElementDatatype()
	{
		return elementDatatype;
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
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if reason is unknown.
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
	 * Returns true if this bag contains the specified element. More formally, returns true if and only if this bag contains at least one element e such that
	 * (v==null ? e==null : v.equals(e)).
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
	 * @return the one-and-only one value in the bag; null if bag is empty or contains multiple values
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
		return this.elementDatatype.equals(otherBag.elementDatatype) && this.values.equals(otherBag.values);
	}

	@Override
	public int hashCode()
	{// immutable class -> cache this method result
		if (hashCode == 0)
		{
			/*
			 * There must be one-to-one mapping between datatype and datatype, so no need to hash both. Plus, causeForEmpty is just some optional info, ignore
			 * it in hash.
			 */
			hashCode = Objects.hash(elementDatatype, values);
		}

		return hashCode;
	}

	@Override
	public final String toString()
	{
		// immutable class -> cache this method result
		if (toString == null)
		{
			toString = "Bag[elementType = " + elementDatatype + ", values = " + values + ", causeForEmpty = " + causeForEmpty + "]";
		}

		return toString;
	}

	@Override
	public final Iterator<AV> iterator()
	{
		return values.iterator();
	}

}