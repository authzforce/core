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

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;

/**
 * AttributeSelector identifier (category, contextSelectorId, path). Why not use AttributeSelector
 * directly? Because we don't care about MustBePresent or Datatype for lookup here. This is used for
 * example as key in a map to retrieve corresponding AttributeValue when it has already been
 * evaluated.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ContextSelectorId, because not
 * equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
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

	// cached method results
	private int hashCode = 0;
	private String toString = null;

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
		if (hashCode == 0)
		{
			hashCode = Objects.hash(category, path, contextSelectorId);
		}

		return hashCode;
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
		if (toString == null)
		{
			toString = "[Category=" + category + ", ContextSelectorId=" + contextSelectorId + ", Path=" + path + "]";
		}

		return toString;
	}
}
