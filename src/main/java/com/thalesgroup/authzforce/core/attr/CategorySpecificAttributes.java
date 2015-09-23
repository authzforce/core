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
package com.thalesgroup.authzforce.core.attr;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.Bags;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.Bag;

/**
 * 
 * Internal equivalent of XACML Attributes element to be used by the policy evaluation engine
 * 
 */
public class CategorySpecificAttributes
{
	/**
	 * Growable bag, i.e. mutable bag of attribute values to which you can add as many values as you
	 * can. Used only when the total number of values for a given attribute - typically in a XACML
	 * request - is not known in advance. For example, for the same AttributeId (e.g. with Issuer =
	 * null), there might be multiple <Attribute> elements, in which case values must be merged for
	 * later matching <AttributeDesignator> evaluation. Indeed, as discussed on the xacml-dev
	 * mailing list (see https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html), the
	 * following excerpt from the XACML 3.0 core spec, §7.3.3, indicates that multiple occurrences
	 * of the same <Attribute> with same meta-data but different values should be considered
	 * equivalent to a single <Attribute> element with same meta-data and merged values
	 * (multi-valued Attribute). Moreover, the conformance test 'IIIA024' expects this behavior: the
	 * multiple subject-id Attributes are expected to result in a multi-value bag during evaluation
	 * of the AttributeDesignator.
	 * 
	 * 
	 * @param <AV>
	 *            element type (primitive). Indeed, XACML spec says for Attribute Bags (7.3.2):
	 *            "There SHALL be no notion of a bag containing bags, or a bag containing values of
	 *            differing types; i.e., a bag in XACML SHALL contain only values that are of the
	 *            same data-type."
	 */
	public static class MutableBag<AV extends AttributeValue<AV>>
	{
		private final AttributeValue.Factory<AV> elementDatatypeFactory;

		/*
		 * ArrayDeque as the most basic subclass of Collection since we only need very minimal
		 * features
		 */
		final Collection<AV> vals = new ArrayDeque<>();

		/**
		 * @param elementDatatypeFactory
		 *            primitive datatype factory to create every element/value in the bag
		 */
		public MutableBag(AttributeValue.Factory<AV> elementDatatypeFactory)
		{
			this.elementDatatypeFactory = elementDatatypeFactory;
		}

		/**
		 * Parses and adds resuling XACML/JAXB AttributeValues to bag
		 * 
		 * @param jaxbAttributeValues
		 *            XACML/JAXB AttributeValues from a XACML Attribute element
		 * 
		 * @throws IndeterminateEvaluationException
		 */
		public void add(List<AttributeValueType> jaxbAttributeValues) throws IndeterminateEvaluationException
		{
			final Datatype<AV> datatype = this.elementDatatypeFactory.getDatatype();
			// Parse attribute values to Java type compatible with evaluation engine
			int jaxbValIndex = 0;
			for (final AttributeValueType jaxbAttrVal : jaxbAttributeValues)
			{
				// if wrong datatype
				if (!jaxbAttrVal.getDataType().equals(datatype.getId()))
				{
					throw new IndeterminateEvaluationException("Invalid datatype of AttributeValue #" + jaxbValIndex + " in Attribute element: " + jaxbAttrVal.getDataType() + ". Expected: " + datatype, Status.STATUS_SYNTAX_ERROR);
				}

				final AV resultValue;
				try
				{
					resultValue = this.elementDatatypeFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes());
				} catch (IllegalArgumentException | ClassCastException e)
				{
					throw new IndeterminateEvaluationException("Invalid AttributeValue #" + jaxbValIndex + " in Attribute element for datatype " + datatype, Status.STATUS_SYNTAX_ERROR, e);
				}

				this.vals.add(resultValue);
				jaxbValIndex++;
			}
		}

		/**
		 * Create immutable version for request evaluation (value must remain constant during
		 * evaluation of the request)
		 * 
		 * @return immutable bag
		 */
		public Bag<AV> toImmutable()
		{
			return Bags.getInstance(this.elementDatatypeFactory.getBagDatatype(), vals);
		}

	}

	private final Map<AttributeGUID, MutableBag<?>> attributeMap;

	private final Attributes attrsToIncludeInResult;

	/*
	 * Corresponds to Attributes/Content marshalled to XPath data model for XPath evaluation (e.g.
	 * AttributeSelector or XPath-based evaluation). This is set to null if no Content provided or
	 * no feature using XPath evaluation against Content is enabled.
	 */
	private final XdmNode extraContent;

	/**
	 * Instantiates this class
	 * 
	 * @param attributeMap
	 *            Attribute map where each key is the name of an attribute, and the value is its bag
	 *            of values
	 * @param attributesToIncludeInResult
	 *            Attributes with only the Attribute elements to include in final Result
	 *            (IncludeInResult = true in original XACML request) or null if there was none
	 * @param extraContent
	 *            Attributes/Content parsed into XPath data model for XPath evaluation
	 */
	public CategorySpecificAttributes(Map<AttributeGUID, MutableBag<?>> attributeMap, Attributes attributesToIncludeInResult, XdmNode extraContent)
	{
		this.attributeMap = attributeMap == null ? new HashMap<AttributeGUID, MutableBag<?>>() : attributeMap;
		this.attrsToIncludeInResult = attributesToIncludeInResult;
		this.extraContent = extraContent;
	}

	/**
	 * Get named attributes
	 * 
	 * @return attribute map where each key is the name of an attribute, and the value is its bag of
	 *         values
	 */
	public Map<AttributeGUID, MutableBag<?>> getAttributeMap()
	{
		return attributeMap;
	}

	/**
	 * Gets the Content parsed into XPath data model for XPath evaluation; or null if no Content
	 * 
	 * @return the Content in XPath data model
	 */
	public XdmNode getExtraContent()
	{
		return extraContent;
	}

	/**
	 * Get Attributes to include in the final Result (IncludeInResult = true in original XACML
	 * request)
	 * 
	 * @return the attributes to include in the final Result; null if nothing to include
	 */
	public Attributes getAttributesToIncludeInResult()
	{
		return attrsToIncludeInResult;
	}
}