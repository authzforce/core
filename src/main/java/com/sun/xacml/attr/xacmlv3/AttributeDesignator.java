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

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.PdpModelHandler;
import com.thalesgroup.authzforce.xacml.schema.XACMLCategory;

public class AttributeDesignator extends AttributeDesignatorType implements Evaluatable
{

	/**
	 * Tells designator to search in the subject section of the request
	 */
	public static final int SUBJECT_TARGET = 0;

	/**
	 * Tells designator to search in the resource section of the request
	 */
	public static final int RESOURCE_TARGET = 1;

	/**
	 * Tells designator to search in the action section of the request
	 */
	public static final int ACTION_TARGET = 2;

	/**
	 * Tells designator to search in the environment section of the request
	 */
	public static final int ENVIRONMENT_TARGET = 3;

	/**
	 * The standard URI for the default subject category value
	 */
	public static final String SUBJECT_CATEGORY_DEFAULT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

	// helper array of strings
	public static final String[] targetTypes = { "Subject", "Resource", "Action", "Environment" };

	// the type of designator we are
	private int target;

	// if we're a subject this is the category
	// private URI category;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeDesignator.class);

	/**
	 * Return an instance of an AttributeDesignator based on an AttributeDesignatorType
	 * 
	 * @param attrDesignator
	 *            the AttributeDesignatorType we want to convert
	 * 
	 * @return the AttributeDesignator
	 */
	public static AttributeDesignator getInstance(AttributeDesignatorType attrDesignator)
	{
		int target = -1;
		URI attrId = null;
		URI datatype = null;
		URI issuer = null;
		URI categoryURI = null;

		try
		{
			final XACMLCategory category = XACMLCategory.fromValue(attrDesignator.getCategory());
			switch (category)
			{
				case XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_CODEBASE:
				case XACML_1_0_SUBJECT_CATEGORY_INTERMEDIARY_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_RECIPIENT_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_REQUESTING_MACHINE:
					target = 0;
					break;
				case XACML_3_0_RESOURCE_CATEGORY_RESOURCE:
					target = 1;
					break;
				case XACML_3_0_ACTION_CATEGORY_ACTION:
					target = 2;
					break;
				case XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT:
					target = 3;
					break;
			}
		} catch (IllegalArgumentException e)
		{
			// unknown category
			// target = -1;
		}

		if (attrDesignator.getDataType() != null)
		{
			datatype = URI.create(attrDesignator.getDataType());
		}
		if (attrDesignator.getIssuer() != null)
		{
			issuer = URI.create(attrDesignator.getIssuer());
		}
		if (attrDesignator.getAttributeId() != null)
		{
			attrId = URI.create(attrDesignator.getAttributeId());
		}
		if (attrDesignator.getCategory() != null)
		{
			categoryURI = URI.create(attrDesignator.getCategory());
		}
		return new AttributeDesignator(target, datatype, attrId, categoryURI, attrDesignator.isMustBePresent(), issuer);
	}

	public static AttributeDesignator getInstance(Node root, String myCategory, PolicyMetaData metadata)
	{
		return getInstance(root);
	}

	public static AttributeDesignator getInstance(Node root)
	{
		final JAXBElement<AttributeDesignatorType> match;
		try
		{
			Unmarshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			match = u.unmarshal(root, AttributeDesignatorType.class);
			AttributeDesignatorType attrDes = match.getValue();
			AttributeDesignator myAttr = getInstance(attrDes);
			return myAttr;
		} catch (Exception e)
		{
			LOGGER.error("Error unmarshalling AttributeDesignator", e);
		}

		return null;
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> without the optional issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 */
	public AttributeDesignator(int target, URI type, URI id, boolean mustBePresent)
	{
		this(target, type, id, mustBePresent, null);
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> with the optional issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param issuer
	 *            the issuer of the values to search for or null if no issuer is specified
	 * 
	 * @throws IllegalArgumentException
	 *             if the input target isn't a valid value
	 */
	public AttributeDesignator(int target, URI type, URI id, boolean mustBePresent, URI issuer)
	{
		this(target, type, id, null, mustBePresent, null);
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> with the optional issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param category
	 *            the category looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param issuer
	 *            the issuer of the values to search for or null if no issuer is specified
	 * 
	 * @throws IllegalArgumentException
	 *             if the input target isn't a valid value
	 */
	public AttributeDesignator(int target, URI type, URI id, URI category, boolean mustBePresent, URI issuer) throws IllegalArgumentException
	{
		this.target = target;
		this.dataType = type.toASCIIString();
		this.attributeId = id.toASCIIString();
		this.mustBePresent = mustBePresent;
		if (issuer != null)
		{
			this.issuer = issuer.toASCIIString();
		} 

		this.category = category.toASCIIString();
	}

	/**
	 * Sets the category if this is a SubjectAttributeDesignatorType
	 * 
	 * @param category
	 *            the subject category
	 */
	public void setCategory(URI category)
	{
		this.category = category.toASCIIString();
	}

	/**
	 * Returns the type of this designator as specified by the *_TARGET fields.
	 * 
	 * @return the designator type
	 */
	public int getDesignatorType()
	{
		return target;
	}

	/**
	 * Returns the type of attribute that is resolved by this designator. While an AD will always
	 * return a bag, this method will always return the type that is stored in the bag.
	 * 
	 * @return the attribute type
	 */
	public URI getType()
	{
		return URI.create(dataType);
	}

	/**
	 * Returns the AttributeId of the values resolved by this designator.
	 * 
	 * @return identifier for the values to resolve
	 */
	public URI getId()
	{
		return URI.create(attributeId);
	}

	/**
	 * Returns whether or not a value is required to be resolved by this designator.
	 * 
	 * @return true if a value is required, false otherwise
	 */
	public boolean mustBePresent()
	{
		return mustBePresent;
	}

	/**
	 * Always returns true, since a designator always returns a bag of attribute values.
	 * 
	 * @return true
	 */
	public boolean returnsBag()
	{
		return true;
	}

	/**
	 * Always returns true, since a designator always returns a bag of attribute values.
	 * 
	 * @deprecated As of 2.0, you should use the <code>returnsBag</code> method from the
	 *             super-interface <code>Expression</code>.
	 * 
	 * @return true
	 */
	@Override
	public boolean evaluatesToBag()
	{
		return true;
	}

	/**
	 * Always returns an empty list since designators never have children.
	 * 
	 * @return an empty <code>List</code>
	 */
	@Override
	public List getChildren()
	{
		return Collections.EMPTY_LIST;
	}

	/**
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching
	 * values.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a result containing a bag either empty because no values were found or containing at
	 *         least one value, or status associated with an Indeterminate result
	 */
	@Override
	public EvaluationResult evaluate(EvaluationCtx context)
	{
		EvaluationResult result = null;
		URI issuerURI = null;
		if (this.getIssuer() != null)
		{
			issuerURI = URI.create(this.getIssuer());
		}

		// look in the right section for some attribute values
		/*
		 * TODO: simplify this using method EvaluationCtx#getAttribute(AttribtueDesignator) (to be
		 * implemented)
		 */
		switch (target)
		{
			case SUBJECT_TARGET:
				result = context.getSubjectAttribute(this.getType(), this.getId(), issuerURI, URI.create(category));
				break;
			case RESOURCE_TARGET:
				result = context.getResourceAttribute(this.getType(), this.getId(), issuerURI);
				break;
			case ACTION_TARGET:
				result = context.getActionAttribute(this.getType(), this.getId(), issuerURI);
				break;
			case ENVIRONMENT_TARGET:
				result = context.getEnvironmentAttribute(this.getType(), this.getId(), issuerURI);
				break;
			default:
				result = context.getCustomAttribute(this.getType(), this.getId(), issuerURI);
				break;
		}

		// if the lookup was indeterminate, then we return immediately
		if (result.indeterminate())
		{
			return result;
		}

		BagAttribute bag = (BagAttribute) (result.getAttributeValue());

		if (bag.isEmpty())
		{
			// if it's empty, this may be an error
			if (mustBePresent)
			{
				LOGGER.info("AttributeDesignator failed to resolve a " + "value for a required attribute: " + this.getId().toASCIIString());

				List<String> codes = new ArrayList<>();
				codes.add(Status.STATUS_MISSING_ATTRIBUTE);

				String message = "Couldn't find " + targetTypes[target] + "AttributeDesignator attribute";

				// Note that there is a bug in the XACML spec. You can't
				// specify an identifier without specifying acceptable
				// values. Until this is fixed, this code will only
				// return the status code, and not any hints about what
				// was missing

				/*
				 * List attrs = new ArrayList(); attrs.add(new Attribute(id, ((issuer == null) ?
				 * null : issuer.toString()), null, null)); StatusDetail detail = new
				 * StatusDetail(attrs);
				 */

				return new EvaluationResult(new Status(codes, message));
			}
		}

		// if we got here the bag wasn't empty, or mustBePresent was false,
		// so we just return the result
		return result;
	}

	/**
	 * Encodes this designator into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this designator into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		String indent = indenter.makeString();
		out.println(indent + this);
	}
	
	@Override
	public String toString() {
		String tag = "<AttributeDesignator";

		if ((target == SUBJECT_TARGET) && (category != null))
			tag += " Category=\"" + category.toString() + "\"";

		tag += " AttributeId=\"" + this.getId().toASCIIString() + "\"";
		tag += " DataType=\"" + dataType.toString() + "\"";

		if (issuer != null)
			tag += " Issuer=\"" + issuer.toString() + "\"";

		if (mustBePresent)
			tag += " MustBePresent=\"true\"";

		tag += "/>";
		
		return tag;
	}
	
	private final static String ATTRIBUTE_DESIGNATOR_DESCRIPTION = "AttributeDesignator[category=%s, id=%s, issuer=%s, datatype=%s]";

	/**
	 * Get description
	 * 
	 * @param attrDes
	 * @return description
	 */
	public static String toString(AttributeDesignatorType attrDes)
	{
		return String.format(ATTRIBUTE_DESIGNATOR_DESCRIPTION, attrDes.getCategory(), attrDes.getAttributeId(), attrDes.getIssuer(),
				attrDes.getDataType());
	}
	
	/**
	 * Creates JAXB SubjectAttributeDesignator
	 * 
	 * @param category
	 *            attribute category
	 * @param id
	 *            attribute ID
	 * @param type
	 *            attribtue datatype
	 * @param issuer
	 *            attribute issuer
	 * @return JAXB SubjectAttributeDesignatorType instance
	 */
	public static AttributeDesignatorType getAttributeDesignatorType(URI category, URI id, URI type, URI issuer)
	{
		if (id == null)
		{
			throw new IllegalArgumentException(
					"Undefined id for AttributeDesignatorType ('AttributeId' is a required attribute of this element)");
		}

		if (type == null)
		{
			throw new IllegalArgumentException(
					"Undefined datatype for AttributeDesignatorType ('DataType' is a required attribute of this element)");
		}

		final AttributeDesignatorType attrDes = new AttributeDesignatorType();
		if (category != null)
		{
			attrDes.setCategory(category.toString());
		}

		attrDes.setAttributeId(id.toString());
		attrDes.setDataType(type.toString());

		if (issuer != null)
		{
			attrDes.setIssuer(issuer.toString());
		}

		return attrDes;
	}
}
