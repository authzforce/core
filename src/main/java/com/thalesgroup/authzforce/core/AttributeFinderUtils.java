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
/**
 * 
 */
package com.thalesgroup.authzforce.core;

import java.net.URI;

import javax.validation.constraints.NotNull;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * Utility methods used in AttributeFinderModules
 * 
 * 
 */
public class AttributeFinderUtils
{
	private final static Logger LOGGER = LoggerFactory.getLogger(AttributeFinderUtils.class);

	/**
	 * Creates JAXB AttributeDesignator
	 * 
	 * @param category
	 *            attribute category
	 * @param id
	 *            attribute ID
	 * @param type
	 *            attribtue datatype
	 * @param issuer
	 *            attribute issuer
	 * @return JAXB AttributeDesignatorType instance
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
	
	/**
	 * Gets attribute value from request context
	 * 
	 * @param target
	 * @param type
	 * @param id
	 * @param issuer
	 * @param subjectCategory
	 * @param context
	 * @return attribute value in request context
	 */
	public static String getAttributeValue(int target, URI type, URI id, String issuer, String subjectCategory, @NotNull EvaluationCtx context) {
		return getAttributeValue(target, type, id, URI.create(issuer), URI.create(subjectCategory), context);
	}

	/**
	 * Get attribute value from policy evaluation context
	 * 
	 * @param target
	 *            attribute target
	 * @param type
	 *            attribute datatype
	 * @param id
	 *            attribute ID
	 * @param issuer
	 *            attribute issuer
	 * @param subjectCategory
	 *            attribute category if target is Subject
	 * @param context
	 *            policy evaluation context
	 * @return attribute value if not a bag or first attribute value if it is a bag, in string form,
	 *         null if none or error
	 */
	public static String getAttributeValue(int target, URI type, URI id, URI issuer, URI subjectCategory, @NotNull EvaluationCtx context)
	{
		final EvaluationResult evalResult;
		switch (target)
		{
			case AttributeDesignator.SUBJECT_TARGET:
				evalResult = context.getSubjectAttribute(type, id, issuer, subjectCategory);
				break;
			case AttributeDesignator.RESOURCE_TARGET:
				evalResult = context.getResourceAttribute(type, id, issuer);
				break;
			case AttributeDesignator.ACTION_TARGET:
				evalResult = context.getActionAttribute(type, id, issuer);
				break;
			case AttributeDesignator.ENVIRONMENT_TARGET:
				evalResult = context.getEnvironmentAttribute(type, id, issuer);
				break;
			default:
				// FIXME: throw exception for invalid target?
				return null;
		}

		final AttributeValue attrVal = evalResult.getAttributeValue();
		if (attrVal == null)
		{
			LOGGER.error("Attribute not found in evaluation context (target='{}',category='{}', id='{}', issuer='{}', datatype='{}')", new Object[] {
					AttributeDesignator.targetTypes[target], subjectCategory, id, issuer, type });
			return null;
		}

		// if subject attribute is bag, get first value in bag, else
		// take
		// it as it is
		if (!attrVal.isBag())
		{
			return attrVal.encode();
		}

		final BagAttribute attrValBag = (BagAttribute) attrVal;
		if (attrValBag.getValues().isEmpty())
		{
			LOGGER.error("Value bag of attribute (target='{}', category='{}', id='{}', issuer='{}', datatype='{}') is empty in evaluation context ",
					new Object[] { AttributeDesignator.targetTypes[target], subjectCategory, id, issuer, type });
			return null;
		}

		// only get the first value in the bag
		final Object firstBagAttrValObj = attrValBag.getValues().iterator().next();
		if (firstBagAttrValObj == null)
		{
			LOGGER.error(
					"First value in bag of values of attribute (target='{}', category='{}', id='{}', issuer='{}', datatype='{}') is null",
					new Object[] { AttributeDesignator.targetTypes[target], subjectCategory, id, issuer, type });
			return null;
		}

		// TODO: should we check that attrVal is instance of AttributeValue
		final AttributeValue firstBagAttrVal = (AttributeValue) firstBagAttrValObj;
		return firstBagAttrVal.encode();
	}

	
}
