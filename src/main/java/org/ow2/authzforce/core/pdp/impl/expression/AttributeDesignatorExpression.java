/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * AttributeDesignator evaluator
 *
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML datatype/category/ID, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * <p>
 * See also:
 * </p>
 * <p>
 * https://java.net/projects/jaxb/lists/users/archive/2011-07/message/16
 * </p>
 *
 * @param <AV>
 *            AttributeDesignator evaluation result value's primitive datatype
 * 
 * @version $Id: $
 */
public final class AttributeDesignatorExpression<AV extends AttributeValue> implements Expression<Bag<AV>>
{
	private static final IllegalArgumentException NULL_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute designator category");
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute designator datatype");
	private static final IllegalArgumentException NULL_ATTRIBUTE_ID_EXCEPTION = new IllegalArgumentException("Undefined attribute designator AttribtueId");
	private static final IllegalArgumentException NULL_ATTRIBUTE_Provider_EXCEPTION = new IllegalArgumentException("Undefined attribute Provider");
	private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();

	private final transient AttributeGUID attrGUID;
	private final transient AttributeProvider attrProvider;
	private final transient BagDatatype<AV> returnType;
	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
	private final transient Bag.Validator mustBePresentEnforcer;

	// lazy initialization
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public Bag<AV> getValue()
	{
		// depends on the context
		return null;
	}

	/**
	 * Return an instance of an AttributeDesignator based on an AttributeDesignatorType
	 *
	 * @param attrDesignator
	 *            the AttributeDesignatorType we want to convert
	 * @param resultDatatype
	 *            expected datatype of the result of evaluating this AttributeDesignator ( {@code AV is the expected type of every element in the bag})
	 * @param attrProvider
	 *            Attribute Provider responsible for finding the attribute designated by this in a given evaluation context at runtime
	 */
	public AttributeDesignatorExpression(final AttributeDesignatorType attrDesignator, final BagDatatype<AV> resultDatatype, final AttributeProvider attrProvider)
	{
		final String categoryURI = attrDesignator.getCategory();
		if (categoryURI == null)
		{
			throw NULL_CATEGORY_EXCEPTION;
		}

		final String datatypeURI = attrDesignator.getDataType();
		if (datatypeURI == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		final String id = attrDesignator.getAttributeId();
		if (id == null)
		{
			throw NULL_ATTRIBUTE_ID_EXCEPTION;
		}

		if (attrProvider == null)
		{
			throw NULL_ATTRIBUTE_Provider_EXCEPTION;
		}

		this.attrGUID = new AttributeGUID(categoryURI, attrDesignator.getIssuer(), id);
		this.returnType = resultDatatype;
		this.attrProvider = attrProvider;

		// error messages/exceptions
		final String missingAttributeMessage = this + " not found in context";
		final boolean mustBePresentFlag = attrDesignator.isMustBePresent();
		this.mustBePresentEnforcer = mustBePresentFlag ? new Bags.NonEmptinessValidator(missingAttributeMessage) : Bags.DUMB_VALIDATOR;

		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(missingAttributeMessage + " for unknown reason", StatusHelper.STATUS_MISSING_ATTRIBUTE);
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attrGUID
				+ "' because request context undefined", StatusHelper.STATUS_MISSING_ATTRIBUTE);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching values.
	 */
	@Override
	public Bag<AV> evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> bag = attrProvider.get(attrGUID, this.returnType.getElementType(), context);
		if (bag == null)
		{
			throw this.missingAttributeForUnknownReasonException;
		}

		mustBePresentEnforcer.validate(bag);

		/*
		 * if we got here the bag wasn't empty, or mustBePresent was false, so we just return the result
		 */
		return bag;
	}

	/** {@inheritDoc} */
	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	/** {@inheritDoc} */
	@Override
	public JAXBElement<AttributeDesignatorType> getJAXBElement()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "AttributeDesignator [" + this.attrGUID + ", dataType= " + this.returnType.getElementType() + ", mustBePresent= "
					+ (mustBePresentEnforcer == Bags.DUMB_VALIDATOR ? "false" : "true") + "]";
		}

		return toString;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.attrGUID.hashCode();
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof AttributeDesignatorExpression))
		{
			return false;
		}

		final AttributeDesignatorExpression<?> other = (AttributeDesignatorExpression<?>) obj;
		return this.attrGUID.equals(other.attrGUID);
	}

}
