package com.sun.xacml.attr.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

public class AttributeDesignator extends AttributeDesignatorType implements Evaluatable {

	public static AttributeDesignator getInstance(AttributeAssignmentExpressionType attrAssignement) {
		JAXBElement<AttributeDesignatorType> myAttrDes = (JAXBElement<AttributeDesignatorType>) attrAssignement
				.getExpression();
		int target = -1;
		if(myAttrDes.getValue().getCategory().equals(XACMLAttributeId.XACML_1_0_SUBJECT_CATEGORY_SUBJECT.value())) {
			target = 0;
		} else if(myAttrDes.getValue().getCategory().equals(XACMLAttributeId.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value())) {
			target = 1;
		} else if(myAttrDes.getValue().getCategory().equals(XACMLAttributeId.XACML_3_0_ACTION_CATEGORY_ACTION.value())) {
			target = 2;
		} else if(myAttrDes.getValue().getCategory().equals(XACMLAttributeId.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value())) {
			target = 3;
		}
		URI attrId = null;
		URI datatype = null;
		URI issuer = null;
		URI category = null;
		
		if(myAttrDes.getValue().getDataType() != null) {
			datatype = URI.create(myAttrDes.getValue().getDataType());
		}
		if(myAttrDes.getValue().getIssuer() != null) {
			issuer = URI.create(myAttrDes.getValue().getIssuer());
		}
		if(myAttrDes.getValue().getAttributeId() != null) {
			attrId = URI.create(myAttrDes.getValue().getAttributeId());
		}
		if(myAttrDes.getValue().getCategory() != null) {
			category = URI.create(myAttrDes.getValue().getCategory());
		}
		return new AttributeDesignator(target, datatype, attrId, category, myAttrDes.getValue().isMustBePresent(), issuer);
	}

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
	static final private String[] targetTypes = { "Subject", "Resource",
			"Action", "Environment" };

	// the type of designator we are
	private int target;

	// if we're a subject this is the category
	private URI category;

	// the logger we'll use for all messages
	private static final Logger logger = Logger
			.getLogger(AttributeDesignator.class.getName());

	/**
	 * Creates a new <code>AttributeDesignator</code> without the optional
	 * issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET
	 *            fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 */
	public AttributeDesignator(int target, URI type, URI id,
			boolean mustBePresent) {
		this(target, type, id, mustBePresent, null);
	}
	
	/**
	 * Creates a new <code>AttributeDesignator</code> with the optional issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET
	 *            fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param issuer
	 *            the issuer of the values to search for or null if no issuer is
	 *            specified
	 * 
	 * @throws IllegalArgumentException
	 *             if the input target isn't a valid value
	 */
	public AttributeDesignator(int target, URI type, URI id,
			boolean mustBePresent, URI issuer) {
		this(target, type, id, null, mustBePresent, null);
	}

	/**
	 * Creates a new <code>AttributeDesignator</code> with the optional issuer.
	 * 
	 * @param target
	 *            the type of designator as specified by the 4 member *_TARGET
	 *            fields
	 * @param type
	 *            the data type resolved by this designator
	 * @param id
	 *            the attribute id looked for by this designator
	 * @param category
	 *            the category looked for by this designator
	 * @param mustBePresent
	 *            whether resolution must find a value
	 * @param issuer
	 *            the issuer of the values to search for or null if no issuer is
	 *            specified
	 * 
	 * @throws IllegalArgumentException
	 *             if the input target isn't a valid value
	 */
	public AttributeDesignator(int target, URI type, URI id, URI category,
			boolean mustBePresent, URI issuer) throws IllegalArgumentException {

		// check if input target is a valid value
		if ((target != SUBJECT_TARGET) && (target != RESOURCE_TARGET)
				&& (target != ACTION_TARGET) && (target != ENVIRONMENT_TARGET))
			throw new IllegalArgumentException("Input target is not a valid"
					+ "value");
		this.target = target;
		this.dataType = type.toASCIIString();		
		this.attributeId = id.toASCIIString();
		this.mustBePresent = mustBePresent;
		if(issuer != null) {
			this.issuer = issuer.toASCIIString();
		} else {
			issuer = null;
		}

		this.category = category;
		this.setCategory(category);
	}

	/**
	 * Sets the category if this is a SubjectAttributeDesignatorType
	 * 
	 * @param category
	 *            the subject category
	 */
	public void setCategory(URI category) {
		this.category = category;
	}

	/**
	 * Returns the type of this designator as specified by the *_TARGET fields.
	 * 
	 * @return the designator type
	 */
	public int getDesignatorType() {
		return target;
	}

	/**
	 * Returns the type of attribute that is resolved by this designator. While
	 * an AD will always return a bag, this method will always return the type
	 * that is stored in the bag.
	 * 
	 * @return the attribute type
	 */
	public URI getType() {
		return URI.create(dataType);
	}

	/**
	 * Returns the AttributeId of the values resolved by this designator.
	 * 
	 * @return identifier for the values to resolve
	 */
	public URI getId() {
		return URI.create(attributeId);
	}
	
	/**
     * Returns the category for this designator.
     *
     * @return the category
     */
    public String getCategory() {
        return category.toASCIIString();
    }

	/**
	 * Returns the issuer of the values resolved by this designator if
	 * specified.
	 * 
	 * @return the attribute issuer or null if unspecified
	 */
	public String getIssuer() {
		return issuer;
	}
	
	/**
	 * Returns whether or not a value is required to be resolved by this
	 * designator.
	 * 
	 * @return true if a value is required, false otherwise
	 */
	public boolean mustBePresent() {
		return mustBePresent;
	}

	/**
	 * Always returns true, since a designator always returns a bag of attribute
	 * values.
	 * 
	 * @return true
	 */
	public boolean returnsBag() {
		return true;
	}

	/**
	 * Always returns true, since a designator always returns a bag of attribute
	 * values.
	 * 
	 * @deprecated As of 2.0, you should use the <code>returnsBag</code> method
	 *             from the super-interface <code>Expression</code>.
	 * 
	 * @return true
	 */
	public boolean evaluatesToBag() {
		return true;
	}

	/**
	 * Always returns an empty list since designators never have children.
	 * 
	 * @return an empty <code>List</code>
	 */
	public List getChildren() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Evaluates the pre-assigned meta-data against the given context, trying to
	 * find some matching values.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a result containing a bag either empty because no values were
	 *         found or containing at least one value, or status associated with
	 *         an Indeterminate result
	 */
	public EvaluationResult evaluate(EvaluationCtx context) {
		EvaluationResult result = null;
		URI issuer = null;
		if (this.getIssuer() != null) {
			issuer = URI.create(this.getIssuer());
		}

		// look in the right section for some attribute values
		switch (target) {
		case SUBJECT_TARGET:
			result = context.getSubjectAttribute(this.getType(), this.getId(), issuer,
					category);
			break;
		case RESOURCE_TARGET:
			result = context.getResourceAttribute(this.getType(), this.getId(), issuer);
			break;
		case ACTION_TARGET:
			result = context.getActionAttribute(this.getType(), this.getId(), issuer);
			break;
		case ENVIRONMENT_TARGET:
			result = context.getEnvironmentAttribute(this.getType(), this.getId(), issuer);
			break;
		}

		// if the lookup was indeterminate, then we return immediately
		if (result.indeterminate()) {
			return result;
		}

		BagAttribute bag = (BagAttribute) (result.getAttributeValue());

		if (bag.isEmpty()) {
			// if it's empty, this may be an error
			if (mustBePresent) {
				if (logger.isLoggable(Level.INFO))
					logger.info("AttributeDesignator failed to resolve a "
							+ "value for a required attribute: "
							+ this.getId().toASCIIString());

				ArrayList code = new ArrayList();
				code.add(Status.STATUS_MISSING_ATTRIBUTE);

				String message = "Couldn't find " + targetTypes[target]
						+ "AttributeDesignator attribute";

				// Note that there is a bug in the XACML spec. You can't
				// specify an identifier without specifying acceptable
				// values. Until this is fixed, this code will only
				// return the status code, and not any hints about what
				// was missing

				/*
				 * List attrs = new ArrayList(); attrs.add(new Attribute(id,
				 * ((issuer == null) ? null : issuer.toString()), null, null));
				 * StatusDetail detail = new StatusDetail(attrs);
				 */

				return new EvaluationResult(new Status(code, message));
			}
		}

		// if we got here the bag wasn't empty, or mustBePresent was false,
		// so we just return the result
		return result;
	}

	/**
	 * Encodes this designator into its XML representation and writes this
	 * encoding to the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this designator into its XML representation and writes this
	 * encoding to the given <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		String indent = indenter.makeString();

		String tag = "<" + targetTypes[target] + "AttributeDesignator";

		if ((target == SUBJECT_TARGET) && (category != null))
			tag += " SubjectCategory=\"" + category.toString() + "\"";

		tag += " AttributeId=\"" + this.getId().toASCIIString() + "\"";
		tag += " DataType=\"" + dataType.toString() + "\"";

		if (issuer != null)
			tag += " Issuer=\"" + issuer.toString() + "\"";

		if (mustBePresent)
			tag += " MustBePresent=\"true\"";

		tag += "/>";

		out.println(indent + tag);
	}
}
