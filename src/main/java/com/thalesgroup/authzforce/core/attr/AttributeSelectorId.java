package com.thalesgroup.authzforce.core.attr;

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;

/**
 * AttributeSelector TYPE_URI (category, contextSelectorId, path). Why not use AttributeSelector directly?
 * Because we don't care about MustBePresent or Datatype for lookup here. This is used for example
 * as key in a map to retrieve corresponding AttributeValue when it has already been evaluated.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ContextSelectorId, because not equivalent to
 * XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * 
 */
public class AttributeSelectorId
{
	private final String category;
	private final String path;
	private final String contextSelectorId;

	/**
	 * Creates instance from XACML AttributeSelector
	 * 
	 * @param attrSelector
	 *            attribute selector
	 */
	public AttributeSelectorId(AttributeSelectorType attrSelector)
	{

		category = attrSelector.getCategory();
		path = attrSelector.getPath();
		if (category == null || path == null)
		{
			throw new IllegalArgumentException("Undefined AttributeSelector Category or Path");
		}
		
		contextSelectorId = attrSelector.getContextSelectorId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(category, path, contextSelectorId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AttributeSelectorId other = (AttributeSelectorId) obj;
		// category cannot be null (see constructor)
		// if (category == null)
		// {
		// if (other.category != null)
		// return false;
		// } else
		if (!category.equals(other.category))
			return false;
		// path cannot be null (see constructor)
		// if (path == null)
		// {
		// if (other.path != null)
		// return false;
		// } else
		if (!path.equals(other.path))
			return false;
		if (contextSelectorId == null)
		{
			if (other.contextSelectorId != null)
				return false;
		} else if (!contextSelectorId.equals(other.contextSelectorId))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Category=" + category + ", ContextSelectorId=" + contextSelectorId + ", Path=" + path + "]";
	}
}
