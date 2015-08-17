package com.thalesgroup.authzforce.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.CategorySpecificAttributes;
import com.thalesgroup.authzforce.core.eval.BagResult;

/**
 * Individual Decision Request, i.e. conceptual request context that corresponds to one XACML Result
 * element
 */
public class IndividualDecisionRequest
{
	private final Map<AttributeGUID, BagResult<? extends AttributeValue>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;

	/**
	 * Creates empty request (no attribute)
	 * 
	 * @param returnPolicyIdList
	 *            equivalent of XACML ReturnPolicyIdList
	 */
	public IndividualDecisionRequest(boolean returnPolicyIdList)
	{
		attributes = new HashMap<>();
		extraContentsByCategory = new HashMap<>();
		returnApplicablePolicyIdList = returnPolicyIdList;
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
		attributes = new HashMap<>(baseRequest.attributes);
		extraContentsByCategory = new HashMap<>(baseRequest.extraContentsByCategory);
		returnApplicablePolicyIdList = baseRequest.returnApplicablePolicyIdList;
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

		attributes.putAll(categorySpecificAttributes.getAttributeMap());
		extraContentsByCategory.put(categoryName, categorySpecificAttributes.getExtraContent());
		attributesToIncludeInResult.add(categorySpecificAttributes.getAttributesToIncludeInResult());
	}

	/**
	 * Get named attributes by name
	 * 
	 * @return map of attribute name-value pairs
	 */
	public Map<AttributeGUID, BagResult<? extends AttributeValue>> getNamedAttributes()
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
}
