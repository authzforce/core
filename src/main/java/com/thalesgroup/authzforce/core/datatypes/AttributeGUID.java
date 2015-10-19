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
package com.thalesgroup.authzforce.core.datatypes;

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

/**
 * Attribute's Globally unique identifier, as opposed to AttributeId which is local to a specific
 * category and/or issuer. Why not use AttributeDesignator? Because we don't care about
 * MustBePresent or Datatype for lookup here. This is used for example as key in a map to retrieve
 * corresponding AttributeValue or AttributeFinder module.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to
 * XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * 
 */
public final class AttributeGUID
{
	private final String category;

	/**
	 * @return the category
	 */
	public String getCategory()
	{
		return category;
	}

	private final String issuer;
	private final String id;

	// cached method results
	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Creates instance from XACML AttributeDesignator
	 * 
	 * @param attrDes
	 *            attribute designator
	 */
	public AttributeGUID(AttributeDesignatorType attrDes)
	{
		this(attrDes.getCategory(), attrDes.getIssuer(), attrDes.getAttributeId());
	}

	/**
	 * Creates instance from attribute category, issuer and ID
	 * 
	 * @param attrCat
	 *            attribute category (non-null)
	 * @param attrIssuer
	 *            attribute issuer (may be null)
	 * @param attrId
	 *            (non-null)
	 */
	public AttributeGUID(String attrCat, String attrIssuer, String attrId)
	{
		if (attrCat == null || attrId == null)
		{
			throw new IllegalArgumentException("Undefined attribute category or ID");
		}

		category = attrCat;
		issuer = attrIssuer;
		id = attrId;
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
			hashCode = Objects.hash(category, issuer, id);
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
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof AttributeGUID))
		{
			return false;
		}

		final AttributeGUID other = (AttributeGUID) obj;
		// category cannot be null (see constructor)
		// id cannot be null (see constructor)
		/*
		 * According to XACML Core spec, 7.3.4 Attribute Matching, if the Issuer is not supplied,
		 * ignore it in the match.
		 */
		if (!category.equals(other.category) || !id.equals(other.id))
		{
			return false;
		}

		return issuer == null || other.issuer == null || issuer.equals(other.issuer);
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
			toString = "[category=" + category + ", issuer=" + issuer + ", id=" + id + "]";
		}

		return toString;
	}
}
