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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

/**
 * Superclass of all Primitive Attribute Value (for primitive datatypes) which actually represents
 * all XACML standard datatypes //*@param <T> concrete attribute value type as subclass of this
 * class
 * 
 * @param <V>
 *            actual type of the underlying primitive value
 * 
 */
public abstract class PrimitiveAttributeValue<V> extends AttributeValue
{

	/*
	 * Make it final to prevent unexpected value change resulting from some function side-effects
	 */
	protected final V value;

	/**
	 * Validate and parse string representation into the actual Java type
	 * 
	 * @param stringForm
	 *            string representation of attribute value
	 * @return actual Java-typed value
	 * @throws IllegalArgumentException
	 *             if <code>stringForm</code> is not a valid string representation for this value
	 *             datatype
	 */
	protected abstract V parse(String stringForm) throws IllegalArgumentException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType#getContent()
	 */
	@Override
	public final List<Serializable> getContent()
	{
		// return immutable list
		return Collections.unmodifiableList(super.getContent());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType#getOtherAttributes()
	 */
	@Override
	public final Map<QName, String> getOtherAttributes()
	{
		// return immutable map
		final Map<QName, String> otherAttributes = super.getOtherAttributes();
		return otherAttributes == null ? null : Collections.unmodifiableMap(otherAttributes);
	}

	/**
	 * Constructs primitive AttributeValue from XACML AttributeValueType where content must be a
	 * single String value that can be parsed into <code>V</code> to create the internal Java value.
	 * 
	 * @param jaxbAttrVal
	 *            XACML attribute value
	 * @throws IllegalArgumentException
	 *             if first element of {@link jaxbAttrVal#getContent()} is not a valid string
	 *             representation for this value datatype
	 */
	protected PrimitiveAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
		if (content.isEmpty())
		{
			throw new IllegalArgumentException("Invalid primitive AttributeValueType: content is empty");
		}

		final Serializable content0 = content.get(0);
		if (!(content0 instanceof String))
		{
			throw new IllegalArgumentException("Invalid primitive AttributeValueType: content contains instance of " + content0.getClass().getName() + ". Expected: " + String.class);
		}

		try
		{
			value = parse((String) content0);
		} catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid AttributeValue for type '" + dataType + "': " + content0, e);
		}
	}

	/**
	 * Constructs primitive AttributeValue from XACML AttributeValueType where content must be a
	 * single String value that can be parsed into <code>V</code> to create the internal Java value.
	 * 
	 * @param jaxbAttrVal
	 *            XACML attribute value
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	protected PrimitiveAttributeValue(String datatype, String val) throws IllegalArgumentException
	{
		super(datatype, Collections.<Serializable> singletonList(val));
		if (val == null)
		{
			throw new IllegalArgumentException("Undefined attribute value");
		}

		value = parse(val);
	}

	/**
	 * Constructor from Java type of value. A Serializable JAXB-compatible form of the value must be
	 * provided to be used directly as first value in {@link #getContent()}
	 * 
	 * @param datatype
	 *            attribute value datatype
	 * @param rawVal
	 *            internal Java native value
	 * @param jaxbVal
	 *            JAXB-compatible type {@link Serializable} form of <code>val</code>
	 */
	protected PrimitiveAttributeValue(String datatype, V val, Serializable jaxbVal)
	{
		super(datatype, Collections.<Serializable> singletonList(jaxbVal));
		if (val == null || jaxbVal == null)
		{
			throw new IllegalArgumentException("Undefined attribute value");
		}

		value = val;
	}

	/**
	 * Returns the Java value represented by this object, used by XACML function implementations.
	 * 
	 * @return the value
	 */
	public final V getValue()
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(this.value);
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

		// if (!super.equals(obj))
		// return false;

		if (getClass() != obj.getClass())
			return false;
		final PrimitiveAttributeValue<?> other = (PrimitiveAttributeValue<?>) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
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
		return "AttributeValue[type=" + dataType + ", value=" + toString(this.value) + "]";
	}

	/**
	 * Get string representation of internal value using {@link #toString()} by default. You must
	 * override this method for attribute values whose toString() does not return proper String for
	 * XML/display (e.g. byte[])
	 * 
	 * @param val
	 * @return string form of internal value
	 */
	public String toString(V val)
	{
		return val.toString();
	}

}
