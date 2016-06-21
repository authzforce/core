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
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * AttributeDesignator
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
public class AttributeDesignator<AV extends AttributeValue> extends AttributeDesignatorType implements Expression<Bag<AV>>
{
	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER = LoggerFactory.getLogger(AttributeDesignator.class);
	private static final IllegalArgumentException NULL_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute designator category");
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute designator datatype");
	private static final IllegalArgumentException NULL_ATTRIBUTE_ID_EXCEPTION = new IllegalArgumentException("Undefined attribute designator AttribtueId");
	private static final IllegalArgumentException NULL_ATTRIBUTE_Provider_EXCEPTION = new IllegalArgumentException("Undefined attribute Provider");
	private static final UnsupportedOperationException UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("DataType field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ID_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("AttributeId field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Category field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ISSUER_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Issuer field is read-only");

	private final transient String missingAttributeMessage;
	private final transient AttributeGUID attrGUID;
	private final transient AttributeProvider attrProvider;
	private final transient BagDatatype<AV> returnType;
	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
	private final transient Datatype<AV> attributeType;

	// lazy initialization
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setCategory(java.lang .String)
	 */
	/** {@inheritDoc} */
	@Override
	public final void setCategory(String value)
	{
		// prevent de-synchronization with this.attrGUID's Category while keeping field final
		throw UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setAttributeId(java.lang .String)
	 */
	/** {@inheritDoc} */
	@Override
	public final void setAttributeId(String value)
	{
		// prevent de-synchronization of this.attrGUID's AttributeId while keeping field final
		throw UNSUPPORTED_ATTRIBUTE_ID_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setIssuer(java.lang.String )
	 */
	/** {@inheritDoc} */
	@Override
	public final void setIssuer(String value)
	{
		// prevent de-synchronization with this.attrGUID's issuer while keeping field final
		throw UNSUPPORTED_ISSUER_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang .String)
	 */
	/** {@inheritDoc} */
	@Override
	public final void setDataType(String value)
	{
		// prevent de-synchronization with this.returnType while keeping field final
		throw UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isStatic()
	{
		// depends on the context
		return false;
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
	public AttributeDesignator(AttributeDesignatorType attrDesignator, BagDatatype<AV> resultDatatype, AttributeProvider attrProvider)
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

		// JAXB attributes
		this.category = categoryURI;
		this.attributeId = id;
		this.dataType = datatypeURI;
		this.issuer = attrDesignator.getIssuer();
		this.mustBePresent = attrDesignator.isMustBePresent();

		// others
		this.attrGUID = new AttributeGUID(category, issuer, id);
		this.returnType = resultDatatype;
		this.attributeType = resultDatatype.getElementType();
		this.attrProvider = attrProvider;

		// error messages/exceptions
		this.missingAttributeMessage = this + " not found in context";
		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(StatusHelper.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage + " for unknown reason");
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attrGUID
				+ "' because request context undefined", StatusHelper.STATUS_MISSING_ATTRIBUTE);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching values.
	 */
	@Override
	public Bag<AV> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> bag = attrProvider.get(attrGUID, attributeType, context);
		if (bag == null)
		{
			throw this.missingAttributeForUnknownReasonException;
		}

		if (mustBePresent && bag.isEmpty())
		{
			throw new IndeterminateEvaluationException(StatusHelper.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage, bag.getReasonWhyEmpty());
		}

		// if we got here the bag wasn't empty, or mustBePresent was false,
		// so we just return the result
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
		return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createAttributeDesignator(this);
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
			toString = "AttributeDesignator [category=" + category + ", attributeId=" + attributeId + ", dataType=" + dataType + ", issuer=" + issuer + ", mustBePresent=" + mustBePresent + "]";
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
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof AttributeDesignator))
		{
			return false;
		}

		final AttributeDesignator<?> other = (AttributeDesignator<?>) obj;
		return this.attrGUID.equals(other.attrGUID);
	}

}
