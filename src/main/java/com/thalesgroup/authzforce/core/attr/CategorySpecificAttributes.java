package com.thalesgroup.authzforce.core.attr;

import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import com.thalesgroup.authzforce.core.eval.BagResult;

/**
 * 
 * Internal equivalent of XACML Attributes element to be used by the policy evaluation engine
 * 
 */
public class CategorySpecificAttributes
{
	private final Map<AttributeGUID, BagResult<? extends AttributeValue>> attributeMap;

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
	public CategorySpecificAttributes(Map<AttributeGUID, BagResult<? extends AttributeValue>> attributeMap, Attributes attributesToIncludeInResult, XdmNode extraContent)
	{
		this.attributeMap = attributeMap == null ? new HashMap<AttributeGUID, BagResult<? extends AttributeValue>>() : attributeMap;
		this.attrsToIncludeInResult = attributesToIncludeInResult;
		this.extraContent = extraContent;
	}

	/**
	 * Get named attributes
	 * 
	 * @return attribute map where each key is the name of an attribute, and the value is its bag of
	 *         values
	 */
	public Map<AttributeGUID, BagResult<? extends AttributeValue>> getAttributeMap()
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