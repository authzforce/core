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
package com.thalesgroup.authzforce.core.eval;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;

/**
 * This class consists exclusively of static methods that operate on or return {@link Bag}s.
 */
public final class Bags
{
	private static final IllegalArgumentException NULL_BAG_ELEMENT_EXCEPTION = new IllegalArgumentException("Null value in bag");

	// Suppresses default constructor, ensuring non-instantiability.
	private Bags()
	{
	}

	private static class EmptyBag<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		// cached method results
		private AV[] all = null;

		private EmptyBag(Datatype<AV> elementDatatype)
		{
			super(elementDatatype);

		}

		private EmptyBag(BagDatatype<AV> bagDatatype, IndeterminateEvaluationException causeForEmpty)
		{
			super(bagDatatype, causeForEmpty);

		}

		@Override
		public AV one()
		{
			return null;
		}

		@Override
		public AV[] all()
		{
			// lazy init
			if (all == null)
			{
				all = (AV[]) Array.newInstance(bagDatatype.getElementType().getValueClass(), 0);
			}

			// FIXME: return defensive copy not the field itself
			return all;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}
	}

	private static class SingletonBag<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private final AV one;

		// cached method results
		private AV[] all = null;

		private SingletonBag(BagDatatype<AV> bagDatatype, AV val)
		{
			super(bagDatatype, null);
			assert val != null;
			this.one = val;
		}

		@Override
		public AV one()
		{
			return one;
		}

		@Override
		public AV[] all()
		{
			// lazy init
			if (all == null)
			{
				all = (AV[]) Array.newInstance(bagDatatype.getElementType().getValueClass(), 1);
				all[0] = one;
			}

			// FIXME: return defensive copy not the field itself
			return all;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}
	}

	private static class CollectionBasedBag<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private final AV one;
		private final Collection<AV> values;

		// cached method results
		private AV[] all = null;

		private CollectionBasedBag(BagDatatype<AV> bagDatatype, AV val0, Collection<AV> allVals)
		{
			super(bagDatatype, null);
			assert val0 != null && allVals != null;
			this.one = val0;
			this.values = allVals;
		}

		@Override
		public AV one()
		{
			return one;
		}

		@Override
		public AV[] all()
		{
			// lazy init
			if (all == null)
			{
				all = (AV[]) Array.newInstance(bagDatatype.getElementType().getValueClass(), values.size());
				values.toArray(all);
			}

			// FIXME: return defensive copy not the field itself
			return all;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}
	}

	private static class ArrayBag<AV extends AttributeValue<AV>> extends Bag<AV>
	{
		private final AV one;
		private final AV[] all;

		private ArrayBag(BagDatatype<AV> bagDatatype, AV val0, AV[] allVals)
		{
			super(bagDatatype, null);
			assert val0 != null && allVals != null;
			this.one = val0;
			this.all = allVals;
		}

		@Override
		public AV one()
		{
			return one;
		}

		@Override
		public AV[] all()
		{
			// FIXME: return defensive copy not the field itself
			return all;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}
	}

	/**
	 * Creates instance of empty bag from primitive datatype used as element datatype.
	 * <p>
	 * This method is mostly used for internal purposes. You should use
	 * {@link Bags#empty(BagDatatype, IndeterminateEvaluationException)} instead whenever possible,
	 * as it allows to specify the reason why the bag is empty (e.g. evaluation error) in the
	 * {@code causeForEmpty} in order to optimize troubleshooting.
	 * </p>
	 * 
	 * @param elementDatatype
	 *            bag element (primitive) datatype
	 * @return bag
	 */
	public static final <AV extends AttributeValue<AV>> Bag<AV> empty(Datatype<AV> elementDatatype)
	{
		return new EmptyBag<>(elementDatatype);
	}

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
	public static final <AV extends AttributeValue<AV>> Bag<AV> empty(BagDatatype<AV> bagDatatype, IndeterminateEvaluationException causeForEmpty)
	{
		return new EmptyBag<>(bagDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of bag containing one and only one value
	 * 
	 * @param bagDatatype
	 *            bag datatype
	 * @param val
	 *            the one and only one value in the bag
	 * @return bag
	 */
	public static final <AV extends AttributeValue<AV>> Bag<AV> singleton(BagDatatype<AV> bagDatatype, AV val)
	{
		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		return new SingletonBag<>(bagDatatype, val);
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
	public static <AV extends AttributeValue<AV>> Bag<AV> getInstance(BagDatatype<AV> bagDatatype, Collection<AV> values)
	{
		if (values == null || values.isEmpty())
		{
			return new EmptyBag<>(bagDatatype, null);
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
			return new SingletonBag<>(bagDatatype, val0);
		}

		// more than one value
		return new CollectionBasedBag<>(bagDatatype, val0, values);
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
	public static <AV extends AttributeValue<AV>> Bag<AV> getInstance(BagDatatype<AV> bagDatatype, AV[] values)
	{
		if (values == null || values.length == 0)
		{
			return new EmptyBag<>(bagDatatype, null);
		}

		final AV val0 = values[0];
		if (val0 == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (values.length == 1)
		{
			// only one value
			return new SingletonBag<>(bagDatatype, val0);
		}

		// more than one value
		return new ArrayBag<>(bagDatatype, val0, values);
	}

}