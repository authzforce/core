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

import java.util.Arrays;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

/**
 * Immutable bag of values (elements). Immutability is required to ensure values of a given
 * attribute remain constant during an evaluation of a request, as mandated by the XACML spec,
 * section 7.3.5:
 * <p>
 * <i>
 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
 * </i>
 * </p>
 * 
 * @param <AV>
 *            type of every element in the bag
 */
public abstract class Bag<AV extends AttributeValue<AV>> implements Value<AV, Bag<AV>>
{
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined bag datatype argument");
	private static final IllegalArgumentException NON_BAG_DATATYPE_EXCEPTION = new IllegalArgumentException("Illegal bagDatatype argument: not bag");

	protected final BagDatatype<AV> bagDatatype;
	protected final IndeterminateEvaluationException causeForEmpty;

	// cached toString()/hashCode() results
	private int hashCode = 0;
	private String toString = null;

	/**
	 * Constructor specifying bag datatype. On the contrary to {@link #Bag(Datatype)}, this
	 * constructor allows to reuse an existing bag Datatype object, saving the allocation of such
	 * object.
	 * 
	 * @param bagDatatype
	 *            bag datatype
	 * @param causeForEmpty
	 *            reason why this bag is empty if it is; null if it isn't
	 */
	protected Bag(BagDatatype<AV> bagDatatype, IndeterminateEvaluationException causeForEmpty)
	{
		if (bagDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (!bagDatatype.isBag())
		{
			throw NON_BAG_DATATYPE_EXCEPTION;
		}

		this.bagDatatype = bagDatatype;
		this.causeForEmpty = causeForEmpty;
	}

	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return bagDatatype;
	}

	/**
	 * Get this bag's datatype, which happens to be the same as the evaluation result returned by
	 * {@link #getReturnType()}, since this is a constant value
	 * 
	 * @return this bag's datatype
	 */
	public BagDatatype<AV> getDatatype()
	{
		return bagDatatype;
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

	@Override
	public Bag<AV> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof Bag))
		{
			return false;
		}

		final Bag<AV> otherBag = (Bag<AV>) other;
		return bagDatatype.getValueClass() == otherBag.bagDatatype.getValueClass() && Arrays.equals(all(), otherBag.all());
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
			hashCode = Objects.hash(bagDatatype.getValueClass(), Arrays.hashCode(all()));
		}

		return hashCode;
	}

	@Override
	public String toString()
	{
		// immutable class -> cache this method result
		if (toString == null)
		{
			toString = "Bag[elementType = " + bagDatatype.getElementType() + ", values = " + Arrays.toString(all()) + ", causeForEmpty = " + causeForEmpty + "]";
		}

		return toString;
	}

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public abstract boolean isEmpty();

	/**
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if
	 * reason is unknown.
	 * 
	 * @return reason why the bag is empty, if it is
	 */
	public IndeterminateEvaluationException getReasonWhyEmpty()
	{
		return this.causeForEmpty;
	}

	/**
	 * Constructor only used internally to create empty bags
	 */
	protected Bag(Datatype<AV> elementType)
	{
		this.causeForEmpty = null;
		this.bagDatatype = new BagDatatype<>((Class<Bag<AV>>) this.getClass(), elementType);
	}

}