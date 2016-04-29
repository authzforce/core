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
package org.ow2.authzforce.core.pdp.impl.value;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.BaseDatatypeFactory;
import org.w3c.dom.Element;

/**
 * <p>
 * Superclass of all "simple" Attribute Values, including values of any XACML standard datatype; "simple" as in "simple type" or "simple content" of XML schema. This means the value can be represented
 * as character data only (String) with no sub-elements (no XML elements) - but with possibly extra XML attributes - as opposed to structured values that have sub-elements. In this definition, all
 * XACML core standard primitive types are "simple" types, and their corresponding Java classes extend this class.
 * </p>
 * <p>
 * Following JAXB fields (inherited from superclass {@link AttributeValueType}) are immutable:
 * <ul>
 * <li>content (also accessible via {@link #getContent()} )</li>
 * <li>dataType (also accessible via {@link #getDataType()})</li>
 * <li>otherAttributes (accessible via {@link #getOtherAttributes()})</li>
 * </ul>
 * </p>
 * <p>
 * For reasons of optimizations and in order to be an immutable value, the {@code content} field (from superclass {@link AttributeValueType} is never set here, and setting it in implementations will
 * have no effect, since this class overrides {@link #getContent()} (with 'final' modifier) with its own value returned by {@link #printXML()}. Therefore, implementations customize the result of
 * {@link #getContent()} in implementing {@link #printXML()}. As JAXB marshalls the content by using the annotated the {@code content} field directly, as this is not set in this class, DO NOT use it
 * for marshalling. It is expected that the content is used only when marshalling AttributeAssignments (e.g. in XACML response), in which case the class responsible for creating the
 * AttributeAssignments MUST call {@link #getContent()} to get/marshall the actual content.
 * </p>
 * 
 * @param <V>
 *            underlying Java value type
 * 
 */
public abstract class SimpleValue<V> extends AttributeValue
{

	/**
	 * Datatype-specific Attribute Value Factory that supports values based on string content with extra XML attributes.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class Factory<AV extends AttributeValue> extends BaseDatatypeFactory<AV>
	{

		/**
		 * Creates a datatype factory from the Java datatype implementation class and atatype identifier
		 * 
		 * @param instanceClass
		 *            Java implementation class representing the attribute datatype
		 * @param datatypeId
		 *            datatype identifier
		 */
		protected Factory(Class<AV> instanceClass, String datatypeId)
		{
			super(instanceClass, datatypeId);
		}

		/**
		 * Creates attribute value from string representation and possibly extra XML attributes
		 * 
		 * @param val
		 *            string representation
		 * @param otherXmlAttributes
		 *            other XML attributes (optional, i.e. null if none; if always null, use {@link SimpleValue.StringContentOnlyFactory} instead)
		 * @param xPathCompiler
		 *            (optional) XPath compiler for compiling any XPath expression in the value, e.g. {@link XPathValue}
		 * @return instance of {@code F_AV}
		 */
		public abstract AV getInstance(String val, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler);

		/**
		 * Creates an instance of {@code F_AV} from a XACML AttributeValueType-originating content ( {@code jaxbAttrVal.getContent()}) of which must contain a single value that is a valid String
		 * representation for datatype {@code datatype} and possibly other XML attributes; or no value at all, in which case it is considered as the empty string. An example of the latter case is:
		 * 
		 * <pre>
		 * {@literal <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"/>}
		 * </pre>
		 * 
		 * @param content
		 *            XACML AttributeValue content, i.e. list of JAXB content elements of the following types: {@link String}, {@link Element}
		 * @throws IllegalArgumentException
		 *             i if {@code datatype == null || content == null} or if there is more than one element in {@code content}, or first element in {@code content} is not a valid string
		 *             representation for this datatype
		 */
		@Override
		public AV getInstance(List<Serializable> content, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			final String inputStrVal;

			/*
			 * If content is empty, e.g. <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"/>, assume value is empty string.
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
	 * Datatype-specific Attribute Value Factory that supports values only based on string content, without any XML attributes, and independent from the context, i.e. constant values.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class StringContentOnlyFactory<AV extends AttributeValue> extends Factory<AV>
	{
		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException(
				"Invalid value content: extra XML attributes are not supported by this primitive datatype, only string content.");

		protected StringContentOnlyFactory(Class<AV> instanceClass, String datatypeId)
		{
			super(instanceClass, datatypeId);
		}

		@Override
		public AV getInstance(String val, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler)
		{
			if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return getInstance(val);
		}

		@Override
		public boolean isExpressionStatic()
		{
			return true;
		}

		/**
		 * Creates attribute value from string representation
		 * 
		 * @param val
		 *            string representation
		 * @return instance of {@code SCOF_AV}
		 */
		public abstract AV getInstance(String val);
	}

	private static final IllegalArgumentException UNDEF_ATTR_CONTENT_EXCEPTION = new IllegalArgumentException("Undefined attribute value");

	private static final IllegalArgumentException MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION = new IllegalArgumentException(
			"Invalid primitive AttributeValueType: content has more than one element. Expected: empty or single String element ");
	/*
	 * Make it final to prevent unexpected value change resulting from some function side-effects
	 */
	protected final V value;

	// cached method results (because class is immutable)
	private transient volatile String toString = null; // Effective Java - Item 71
	private transient volatile int hashCode = 0; // Effective Java - Item 9 // Effective Java - Item 9
	private transient volatile List<Serializable> xmlString = null;

	/**
	 * Constructor from Java type of value. A Serializable JAXB-compatible form of the value must be provided to be used directly as first value in {@link #getContent()}
	 * 
	 * @param datatypeId
	 *            attribute datatype ID. MUST NOT be null.
	 * @param rawVal
	 *            internal Java native value
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || jaxbVal == null}
	 */
	protected SimpleValue(String datatypeId, V rawVal) throws IllegalArgumentException
	{
		super(datatypeId, null, null);
		if (rawVal == null)
		{
			throw UNDEF_ATTR_CONTENT_EXCEPTION;
		}

		value = rawVal;
	}

	/**
	 * Returns the internal low-level Java value on which this AttributeValue is based off. This method is provided mostly for convenience, especially for low-level operations. However, you should not
	 * use it unless there is no other way. Prefer the high-level methods provided by the concrete {@link SimpleValue} implementation if you need to do operations on it.
	 * 
	 * @return the value
	 */
	public final V getUnderlyingValue()
	{
		return value;
	}

	/**
	 * Converts the internal value (accessible via {@link #getUnderlyingValue()} to a valid lexical representation for XML marshalling. Equivalent to the 'printMethod' in JAXB 'javaType' binding
	 * customizations. Implementations of this typically call {@link DatatypeConverter}. This method is called by {@link #getContent()} and its result cached by the same method for later use.
	 * Therefore, no need to cache the result in the implementation.
	 * 
	 * @return XML-valid lexical representation.
	 */
	public abstract String printXML();

	@Override
	public final List<Serializable> getContent()
	{
		if (xmlString == null)
		{
			xmlString = Collections.<Serializable> singletonList(printXML());
		}

		return xmlString;
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
			toString = getContent().get(0).toString();
		}
		return toString;
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.dataType, value);
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

		if (!(obj instanceof SimpleValue))
		{
			return false;
		}

		final SimpleValue<?> other = (SimpleValue<?>) obj;
		return this.dataType.equals(other.dataType) && this.value.equals(other.value);
	}

}
