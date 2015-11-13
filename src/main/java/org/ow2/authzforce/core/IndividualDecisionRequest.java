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
package org.ow2.authzforce.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.authzforce.core.CategorySpecificAttributes.MutableBag;
import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.Bag;

import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;

/**
 * Individual Decision Request, i.e. conceptual request context that corresponds to one XACML Result
 * element
 */
public class IndividualDecisionRequest
{
	private final Map<AttributeGUID, Bag<?>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;
	private final XPathCompiler defaultXPathCompiler;

	/**
	 * Creates empty request (no attribute)
	 * 
	 * @param returnPolicyIdList
	 *            equivalent of XACML ReturnPolicyIdList
	 * @param defaultXPathCompiler
	 *            default XPath compiler derived from request's
	 *            {@link RequestDefaults#getXPathVersion()}; null if none defined
	 */
	public IndividualDecisionRequest(boolean returnPolicyIdList, XPathCompiler defaultXPathCompiler)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = new HashMap<>();
		extraContentsByCategory = new HashMap<>();
		attributesToIncludeInResult = new ArrayList<>();
		returnApplicablePolicyIdList = returnPolicyIdList;
		this.defaultXPathCompiler = defaultXPathCompiler;
	}

	/**
	 * Create new instance as a clone of an existing request.
	 * 
	 * @param baseRequest
	 *            replicated existing request. Further changes to it are not reflected back to this
	 *            new instance.
	 */
	public IndividualDecisionRequest(IndividualDecisionRequest baseRequest)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = new HashMap<>(baseRequest.attributes);
		extraContentsByCategory = new HashMap<>(baseRequest.extraContentsByCategory);
		attributesToIncludeInResult = new ArrayList<>(baseRequest.attributesToIncludeInResult);
		returnApplicablePolicyIdList = baseRequest.returnApplicablePolicyIdList;
		this.defaultXPathCompiler = baseRequest.defaultXPathCompiler;
	}

	/**
	 * Put attributes of a specific category in request.
	 * 
	 * @param categoryName
	 *            category URI
	 * @param categorySpecificAttributes
	 *            attributes in category {@code categoryName}
	 * @throws IllegalArgumentException
	 *             if {@code categoryName} or {@code attributes} is null
	 */
	public void put(String categoryName, CategorySpecificAttributes categorySpecificAttributes) throws IllegalArgumentException
	{
		if (categoryName == null)
		{
			throw new IllegalArgumentException("Undefined attribute category");
		}

		if (categorySpecificAttributes == null)
		{
			throw new IllegalArgumentException("Undefined attributes");
		}

		/*
		 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed,
		 * we must guarantee that attribute values remain constant during the evaluation of the
		 * request, as mandated by the XACML spec, section 7.3.5: <p> <i>
		 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
		 * </i></p>
		 */
		for (final Entry<AttributeGUID, MutableBag<?>> attrEntry : categorySpecificAttributes.getAttributeMap().entrySet())
		{
			final AttributeGUID attrGUID = attrEntry.getKey();
			final MutableBag<?> mutableBag = attrEntry.getValue();
			attributes.put(attrGUID, mutableBag.toImmutable());
		}

		extraContentsByCategory.put(categoryName, categorySpecificAttributes.getExtraContent());
		final Attributes catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
		if (catSpecificAttrsToIncludeInResult != null)
		{
			attributesToIncludeInResult.add(catSpecificAttrsToIncludeInResult);
		}

	}

	/**
	 * Get named attributes by name
	 * 
	 * @return map of attribute name-value pairs
	 */
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return attributes;
	}

	/**
	 * Get Attributes elements containing only child Attribute elements with IncludeInResult=true
	 * 
	 * @return list of Attributes elements to include in final Result
	 */
	public List<Attributes> getAttributesIncludedInResult()
	{
		return this.attributesToIncludeInResult;
	}

	/**
	 * Get Attributes/Contents (parsed into XDM data model for XPath evaluation) by attribute
	 * category
	 * 
	 * @return Contents by category
	 */
	public Map<String, XdmNode> getExtraContentsByCategory()
	{
		return this.extraContentsByCategory;
	}

	/**
	 * Get RequestDefaults XPath version
	 * 
	 * @return default XPath version
	 */
	public XPathCompiler getDefaultXPathCompiler()
	{
		return defaultXPathCompiler;
	}

}
