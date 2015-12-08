/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.value;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.w3c.dom.Element;

/**
 * The base type for all atomic/non-bag values used in a policy or request/response, this abstract class represents a value for a given attribute type. All the
 * standard primitive datatypes defined in the XACML specification extend this. If you want to provide a new datatype, extend {@link DatatypeFactory} to provide
 * a factory for it. Following JAXB fields (inherited from {@link AttributeValueType}) are made immutable by this class:
 * <ul>
 * <li>content (also accessible via {@link #getContent()} )</li>
 * <li>dataType (also accessible via {@link #getDataType()})</li>
 * <li>otherAttributes (accessible via {@link #getOtherAttributes()})</li>
 * </ul>
 * 
 */
public abstract class AttributeValue extends AttributeValueType implements Value
{

	/**
	 * XML datatype factory for parsing XML-Schema-compliant date/time/duration values into Java types. DatatypeFactory's official javadoc does not say whether
	 * it is thread-safe. But bug report indicates it should be and has been so far: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177 Reusing the
	 * same instance matters for performance: https://www.java.net/node/666491 The alternative would be to use ThreadLocal to limit thread-safety issues in the
	 * future.
	 */
	protected static final DatatypeFactory XML_TEMPORAL_DATATYPE_FACTORY;
	static
	{
		try
		{
			XML_TEMPORAL_DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e)
		{
			throw new RuntimeException(
					"Error instantiating XML datatype factory for parsing strings corresponding to XML schema date/time/duration values into Java types", e);
		}
	}

	private static final IllegalArgumentException UNDEF_ATTR_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype");

	private static final UnsupportedOperationException UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"AttributeValue.setDataType() not allowed");

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType#setDataType(java.lang.String)
	 */
	@Override
	public final void setDataType(String value)
	{
		// datatype only set with constructor (immutable)
		throw UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION;
	}

	/**
	 * Default constructor
	 * 
	 * @param datatypeId
	 *            datatype ID. Note: Do not use the Datatype class here, because if we do, we break the acyclic dependency principle
	 * @param content
	 *            list of JAXB content elements of the following types: {@link String}, {@link Element}. Made immutable by this constructor.
	 * @param otherAttributes
	 *            other attributes, made immutable by this constructor.
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null}
	 */
	protected AttributeValue(String datatypeId, List<Serializable> content, Map<QName, String> otherAttributes) throws IllegalArgumentException
	{
		// assert datatype != null;
		// assert content != null;
		// make fields immutable (datatype made immutable through overriding setDatatype())
		super(content == null ? null : Collections.unmodifiableList(content), validateAndGetId(datatypeId), otherAttributes == null ? null : Collections
				.unmodifiableMap(otherAttributes));
	}

	private static String validateAndGetId(String datatypeId)
	{
		if (datatypeId == null)
		{
			throw UNDEF_ATTR_DATATYPE_EXCEPTION;
		}

		return datatypeId;
	}

}
