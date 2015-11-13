package org.ow2.authzforce.core.expression;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.AttributeProvider;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.XACMLBindingUtils;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.BagDatatype;
import org.ow2.authzforce.core.value.Datatype;

/**
 * AttributeDesignator
 * 
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML datatype/category/ID, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI
 * [1], not in java.net.URI.
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
 */
public class AttributeDesignator<AV extends AttributeValue> extends AttributeDesignatorType implements Expression<Bag<AV>>
{
	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER = LoggerFactory.getLogger(AttributeDesignator.class);
	private static final IllegalArgumentException NULL_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute designator category");
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute designator datatype");
	private static final IllegalArgumentException NULL_ATTRIBUTE_ID_EXCEPTION = new IllegalArgumentException("Undefined attribute designator AttribtueId");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FINDER_EXCEPTION = new IllegalArgumentException("Undefined attribute finder");

	private final transient String missingAttributeMessage;
	private final AttributeGUID attrGUID;
	private final transient AttributeProvider attrFinder;
	private final transient BagDatatype<AV> returnType;
	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
	private final Datatype<AV> attributeType;

	private static final UnsupportedOperationException UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"DataType field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ID_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"AttributeId field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"Category field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_ISSUER_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"Issuer field is read-only");

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setCategory(java.lang .String)
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setAttributeId(java.lang .String)
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setIssuer(java.lang.String )
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang .String)
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
	 * @param resultDatatype
	 *            expected datatype of the result of evaluating this AttributeDesignator ( {@code AV is the expected type of every element in the bag})
	 * @param attrFinder
	 *            Attribute Finder responsible for finding the attribute designated by this in a given evaluation context at runtime
	 */
	public AttributeDesignator(AttributeDesignatorType attrDesignator, BagDatatype<AV> resultDatatype, AttributeProvider attrFinder)
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

		if (attrFinder == null)
		{
			throw NULL_ATTRIBUTE_FINDER_EXCEPTION;
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
		this.attrFinder = attrFinder;

		// error messages/exceptions
		this.missingAttributeMessage = this + " not found in context";
		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(StatusHelper.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage
				+ " for unknown reason");
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException(
				"Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attrGUID + "' because request context undefined",
				StatusHelper.STATUS_MISSING_ATTRIBUTE);
	}

	/**
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching values.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a result containing a bag either empty because no values were found or containing at least one value
	 */
	@Override
	public Bag<AV> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> bag = attrFinder.get(attrGUID, attributeType, context);
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

	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	@Override
	public JAXBElement<AttributeDesignatorType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeDesignator(this);
	}

	// lazy initialization
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

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
			toString = "AttributeDesignator [category=" + category + ", attributeId=" + attributeId + ", dataType=" + dataType + ", issuer=" + issuer
					+ ", mustBePresent=" + mustBePresent + "]";
		}

		return toString;
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.attrGUID.hashCode();
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

		if (!(obj instanceof AttributeDesignator))
		{
			return false;
		}

		final AttributeDesignator<?> other = (AttributeDesignator<?>) obj;
		return this.attrGUID.equals(other.attrGUID);
	}

}
