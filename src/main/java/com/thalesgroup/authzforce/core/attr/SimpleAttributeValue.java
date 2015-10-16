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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.w3c.dom.Element;

/**
 * <p>
 * Superclass of all "simple" Attribute Values, including values of any XACML standard datatype;
 * "simple" as in "simple type" or "simple content" of XML schema. This means the value can be
 * represented as characted data only (String) with no sub-elements (no XML elements) - but with
 * possibly extra XML attributes - as opposed to structured values that have sub-elements. In this
 * definition, all XACML core standard primitive types are "simple" types, and their values extend
 * this class.
 * </p>
 * <p>
 * Following JAXB fields (inherited from superclass {@link AttributeValueType}) are immutable:
 * <ul>
 * <li>content (also accessible via {@link #getContent()} )</li>
 * <li>dataType (also accessible via {@link #getDataType()})</li>
 * <li>otherAttributes (accessible via {@link #getOtherAttributes()})</li>
 * </ul>
 * </p>
 * 
 * @param <V>
 *            underlying Java value type
 * @param <AV>
 *            <AV> The SimpleAttributeValue type subclass
 * 
 */
public abstract class SimpleAttributeValue<V, AV extends SimpleAttributeValue<V, AV>> extends AttributeValue<AV>
{
	/**
	 * Datatype-specific Attribute Value Factory that supports values based on string content with
	 * extra XML attributes.
	 * 
	 * @param <F_AV>
	 *            type of attribute values created by this factory
	 */
	protected static abstract class Factory<F_AV extends AttributeValue<F_AV>> extends AttributeValue.Factory<F_AV>
	{

		/**
		 * @see AttributeValue.Factory#Factory(Class, String)
		 */
		protected Factory(Class<F_AV> instanceClass, String datatypeId)
		{
			super(instanceClass, datatypeId);
		}

		/**
		 * Creates attribute value from string representation and possibly extra XML attributes
		 * 
		 * @param val
		 *            string representation
		 * @param otherXmlAttributes
		 *            other XML attributes (optional, i.e. null if none; if always null, use
		 *            {@link SimpleAttributeValue.StringContentOnlyFactory} instead)
		 * @param xPathCompiler
		 *            XPath compiler for compiling/evaluating any XPath expression in the value,
		 *            e.g. {@link XPathAttributeValue}
		 * @return instance of {@code F_AV}
		 */
		public abstract F_AV getInstance(String val, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler);

		/**
		 * Creates an instance of {@code F_AV} from a XACML AttributeValueType-originating content (
		 * {@code jaxbAttrVal.getContent()}) of which must contain a single value that is a valid
		 * String representation for datatype {@code datatype} and possibly other XML attributes; or
		 * no value at all, in which case it is considered as the empty string. An example of the
		 * latter case is:
		 * 
		 * <pre>
		 * {@literal <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"/>}
		 * </pre>
		 * 
		 * @param content
		 *            XACML AttributeValue content, i.e. list of JAXB content elements of the
		 *            following types: {@link String}, {@link Element}
		 * @throws IllegalArgumentException
		 *             i if {@code datatype == null || content == null} or if there is more than one
		 *             element in {@code content}, or first element in {@code content} is not a
		 *             valid string representation for this datatype
		 */
		@Override
		public F_AV getInstance(List<Serializable> content, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			final String inputStrVal;

			/*
			 * If content is empty, e.g. <AttributeValue
			 * DataType="http://www.w3.org/2001/XMLSchema#string"/>, assume value is empty string.
			 */
			final Iterator<Serializable> contentIterator = content.iterator();
			if (!contentIterator.hasNext())
			{
				inputStrVal = "";
			} else
			{
				final Serializable content0 = contentIterator.next();
				if (!(content0 instanceof String))
				{
					throw new IllegalArgumentException("Invalid primitive AttributeValueType: content contains instance of " + content0.getClass().getName() + ". Expected: " + String.class);
				}

				inputStrVal = (String) content0;
				if (contentIterator.hasNext())
				{
					throw MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION;
				}
			}

			return getInstance(inputStrVal, otherXmlAttributes, xPathCompiler);
		}

	}

	/**
	 * Datatype-specific Attribute Value Factory that supports values only based on string content,
	 * without any XML attributes, and independent from the context, i.e. constant values.
	 * 
	 * @param <SCOF_AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class StringContentOnlyFactory<SCOF_AV extends AttributeValue<SCOF_AV>> extends Factory<SCOF_AV>
	{
		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException("Invalid value content: extra XML attributes are not supported by this primitive datatype, only string content.");

		protected StringContentOnlyFactory(Class<SCOF_AV> instanceClass, String datatypeId)
		{
			super(instanceClass, datatypeId);
		}

		@Override
		public SCOF_AV getInstance(String val, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler)
		{
			if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return getInstance(val);
		}

		/**
		 * Creates attribute value from string representation
		 * 
		 * @param val
		 *            string representation
		 * @return instance of {@code SCOF_AV}
		 */
		public abstract SCOF_AV getInstance(String val);
	}

	private static final IllegalArgumentException UNDEF_ATTR_CONTENT_EXCEPTION = new IllegalArgumentException("Undefined attribute value");

	private static final IllegalArgumentException MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION = new IllegalArgumentException("Invalid primitive AttributeValueType: content has more than one element. Expected: empty or single String element ");
	/*
	 * Make it final to prevent unexpected value change resulting from some function side-effects
	 */
	protected final V value;

	// cached method results (because class is immutable)
	private String toString = null;

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

	/**
	 * Constructs primitive AttributeValue from String representation of this datatype.
	 * 
	 * @param datatype
	 *            datatype. MUST NOT be null.
	 * @param val
	 *            string representation
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null} or {@code val} is null or is not a valid string
	 *             representation for this value datatype
	 */
	protected SimpleAttributeValue(Datatype<AV> datatype, String val) throws IllegalArgumentException
	{
		super(datatype, validate(val), null);
		value = parse(val);
	}

	private static List<Serializable> validate(Serializable val)
	{
		if (val == null)
		{
			throw UNDEF_ATTR_CONTENT_EXCEPTION;
		}

		return Collections.<Serializable> singletonList(val);
	}

	/**
	 * Constructor from Java type of value. A Serializable JAXB-compatible form of the value must be
	 * provided to be used directly as first value in {@link #getContent()}
	 * 
	 * @param datatype
	 *            attribute value datatype. MUST NOT be null.
	 * @param rawVal
	 *            internal Java native value
	 * @param jaxbVal
	 *            JAXB-compatible type {@link Serializable} form of <code>val</code>
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || jaxbVal == null}
	 */
	protected SimpleAttributeValue(Datatype<AV> datatype, V val, Serializable jaxbVal) throws IllegalArgumentException
	{
		super(datatype, validate(jaxbVal), null);
		if (val == null)
		{
			throw UNDEF_ATTR_CONTENT_EXCEPTION;
		}

		value = val;
	}

	/**
	 * Returns the internal low-level Java value on which this AttributeValue is based off. This
	 * method is provided mostly for convenience, especially for low-level operations. However, you
	 * should not use it unless there is no other way. Prefer the high-level methods provided by the
	 * concrete {@link SimpleAttributeValue} implementation if you need to do operations on it.
	 * 
	 * @return the value
	 */
	public final V getUnderlyingValue()
	{
		return value;
	}

	/**
	 * Convert to StringAttributeValue
	 * 
	 * @return StringAttributeValue based on string representation of {@link #getUnderlyingValue()}
	 */
	public StringAttributeValue toStringAttributeValue()
	{
		return new StringAttributeValue(content.get(0).toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// class is immutable -> cache method result
		if (toString == null)
		{
			toString = "AttributeValue[type=" + dataType + ", value=" + this.content.get(0) + "]";
		}

		return toString;
	}

	private int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			/*
			 * WARNING: this part is not correct for array comparison, so we need to override equals
			 * if V is an array type.
			 */
			hashCode = value.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		final SimpleAttributeValue<?, ?> other = (SimpleAttributeValue<?, ?>) obj;
		// no need to check datatype as there is one-to-one mapping between class (already checked
		// above) and datatype

		/*
		 * if (value == null) { if (other.value != null) { return false; } } else
		 */
		/*
		 * WARNING: this part is not correct for array comparison, so we need to override equals if
		 * V is an array type.
		 */
		return value.equals(other.value);
	}

}
