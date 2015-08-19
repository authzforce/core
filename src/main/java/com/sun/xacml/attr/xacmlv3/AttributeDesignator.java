/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.attr.xacmlv3;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinder;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * AttributeDesignator
 * 
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML datatype/category/ID, because not equivalent
 * to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
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
 * @param <T>
 *            AttributeDesignator evaluation result value's primitive datatype
 * 
 */
public class AttributeDesignator<T extends AttributeValue> extends AttributeDesignatorType implements Expression<BagResult<T>>
{
	/**
	 * The standard URI for the default subject category value
	 */
	// public static final String SUBJECT_CATEGORY_DEFAULT =
	// XACMLCategory.XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT.value();

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeDesignator.class);
	private static final IllegalArgumentException NULL_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute designator category");
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute designator datatype");
	private static final IllegalArgumentException NULL_ATTRIBUTE_ID_EXCEPTION = new IllegalArgumentException("Undefined attribute designator AttribtueId");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FINDER_EXCEPTION = new IllegalArgumentException("Undefined attribute finder");

	private final IndeterminateEvaluationException missingAttributeException;
	private final String missingAttributeMessage;
	private final DatatypeDef returnType;
	private final AttributeGUID attrGUID;
	private final AttributeFinder attrFinder;
	private final Class<T> dataTypeClass;

	private static final UnsupportedOperationException UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("DataType field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ID_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("AttributeId field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Category field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ISSUER_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Issuer field is read-only");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setCategory(java.lang
	 * .String)
	 */
	@Override
	public final void setCategory(String value)
	{
		// prevent de-synchronization with this.attrGUID's Category while keeping field final
		throw UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setAttributeId(java.lang
	 * .String)
	 */
	@Override
	public final void setAttributeId(String value)
	{
		// prevent de-synchronization of this.attrGUID's AttributeId while keeping field final
		throw UNSUPPORTED_ATTRIBUTE_ID_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setIssuer(java.lang.String
	 * )
	 */
	@Override
	public final void setIssuer(String value)
	{
		// prevent de-synchronization with this.attrGUID's issuer while keeping field final
		throw UNSUPPORTED_ISSUER_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang
	 * .String)
	 */
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
	 * @param datatypeClass
	 *            the class corresponding to the data type of the attribute (values) designated by
	 *            by this designator
	 * @param attrFinder
	 *            Attribute Finder responsible for finding the attribute designated by this in a
	 *            given evaluation context at runtime
	 */
	public AttributeDesignator(AttributeDesignatorType attrDesignator, Class<T> datatypeClass, AttributeFinder attrFinder)
	{
		this(attrDesignator.getCategory(), attrDesignator.getDataType(), datatypeClass, attrDesignator.getAttributeId(), attrDesignator.isMustBePresent(), attrDesignator.getIssuer(), attrFinder);
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> without the optional issuer.
	 * 
	 * @param category
	 *            the category
	 * @param datatypeURI
	 *            attribute datatype URI
	 * @param datatypeClass
	 *            the class corresponding to the data type of the attribute (values) designated by
	 *            by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param attrFinder
	 *            Attribute Finder responsible for finding the attribute designated by this in a
	 *            given evaluation context at runtime
	 */
	public AttributeDesignator(String category, String datatypeURI, Class<T> datatypeClass, String id, boolean mustBePresent, AttributeFinder attrFinder)
	{
		this(category, datatypeURI, datatypeClass, id, mustBePresent, null, attrFinder);
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> with the optional issuer.
	 * 
	 * @param category
	 *            attribute category
	 * @param datatypeURI
	 *            attribute datatype URI
	 * @param datatypeClass
	 *            the class corresponding to the data type of the attribute (values) designated by
	 *            by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param issuer
	 *            the issuer of the values to search for or null if no issuer is specified
	 * @param attrFinder
	 *            Attribute Finder responsible for finding the attribute designated by this in a
	 *            given evaluation context at runtime
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code category}, {@code datatypeURI}, {@code id} or {@code attrFinder} is
	 *             null
	 */
	public AttributeDesignator(String category, String datatypeURI, Class<T> datatypeClass, String id, boolean mustBePresent, String issuer, AttributeFinder attrFinder)
	{
		if (category == null)
		{
			throw NULL_CATEGORY_EXCEPTION;
		}

		if (datatypeURI == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (id == null)
		{
			throw NULL_ATTRIBUTE_ID_EXCEPTION;
		}

		if (attrFinder == null)
		{
			throw NULL_ATTRIBUTE_FINDER_EXCEPTION;
		}

		// JAXB attributes
		this.category = category;
		this.attributeId = id;
		this.dataType = datatypeURI;
		this.issuer = issuer;
		this.mustBePresent = mustBePresent;

		// others
		this.missingAttributeMessage = "No attribute matching " + this;
		this.missingAttributeException = new IndeterminateEvaluationException(Status.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage);
		this.attrGUID = new AttributeGUID(category, issuer, id);
		this.returnType = new DatatypeDef(dataType, true);
		this.dataTypeClass = datatypeClass;
		this.attrFinder = attrFinder;
	}

	/**
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching
	 * values.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a result containing a bag either empty because no values were found or containing at
	 *         least one value
	 */
	@Override
	public BagResult<T> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw new IndeterminateEvaluationException("Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attrGUID + "' because request context undefined", Status.STATUS_MISSING_ATTRIBUTE);
		}

		final BagResult<T> bag = attrFinder.findAttribute(returnType, attrGUID, context, dataTypeClass);
		if (bag.isEmpty())
		{
			// if it's empty, this may be an error
			if (mustBePresent)
			{
				LOGGER.info(missingAttributeMessage);
				throw missingAttributeException;
			}
		}

		// if we got here the bag wasn't empty, or mustBePresent was false,
		// so we just return the result
		return bag;
	}

	@Override
	public DatatypeDef getReturnType()
	{
		return this.returnType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "AttributeDesignator [category=" + category + ", attributeId=" + attributeId + ", dataType=" + dataType + ", issuer=" + issuer + ", mustBePresent=" + mustBePresent + "]";
	}

	@Override
	public JAXBElement<AttributeDesignatorType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeDesignator(this);
	}

}
