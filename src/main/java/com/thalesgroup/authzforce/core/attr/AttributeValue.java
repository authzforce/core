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

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.w3c.dom.Element;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

/**
 * The base type for all attribute value datatypes used in a policy or request/response, this
 * abstract class represents a value for a given attribute type. All the required types defined in
 * the XACML specification are provided as instances of <code>AttributeValue<code>s. If you want to
 * provide a new type, extend {@link Factory}.
 * Following JAXB fields (inherited from {@link AttributeValueType}) are made immutable by this class:
 * <ul>
 * <li>content (also accessible via {@link #getContent()} )</li>
 * <li>dataType (also accessible via {@link #getDataType()}) </li>
 * <li>otherAttributes (accessible via {@link #getOtherAttributes()}) </li>
 * </ul>
 * 
 * 
 * 
 * @param <AV>
 *            concrete type subclass
 * 
 */
public abstract class AttributeValue<AV extends AttributeValue<AV>> extends AttributeValueType implements Serializable, Value<AV>
{
	/**
	 * XML datatype factory for parsing XML-Schema-compliant date/time/duration values into Java
	 * types. DatatypeFactory's official javadoc does not say whether it is thread-safe. But bug
	 * report indicates it should be and has been so far:
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177 Reusing the same instance matters
	 * for performance: https://www.java.net/node/666491 The alternative would be to use ThreadLocal
	 * to limit thread-safety issues in the future.
	 */
	protected static final DatatypeFactory XML_TEMPORAL_DATATYPE_FACTORY;
	static
	{
		try
		{
			XML_TEMPORAL_DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e)
		{
			throw new RuntimeException("Error instantiating XML datatype factory for parsing strings corresponding to XML schema date/time/duration values into Java types", e);
		}
	}

	/**
	 * Datatype-specific Attribute Value Factory.
	 * 
	 * @param <INSTANCE_AV>
	 *            type of instance (attribute values) created by this factory
	 */
	public static abstract class Factory<INSTANCE_AV extends AttributeValue<INSTANCE_AV>> implements PdpExtension
	{
		private static final IllegalArgumentException NULL_DATATYPE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined instanceClass argument");
		private static final IllegalArgumentException NULL_DATATYPE_ID_EXCEPTION = new IllegalArgumentException("Undefined datatypeId argument");

		protected final Datatype<INSTANCE_AV> instanceDatatype;
		private final Bag<INSTANCE_AV> emptyBag;
		private final Bag.Datatype<INSTANCE_AV> bagDatatype;

		// cached method result
		private int hashCode = 0;
		private String toString = null;

		protected Factory(Class<INSTANCE_AV> instanceClass, String datatypeId)
		{
			if (instanceClass == null)
			{
				throw NULL_DATATYPE_CLASS_EXCEPTION;
			}

			if (datatypeId == null)
			{
				throw NULL_DATATYPE_ID_EXCEPTION;
			}

			this.instanceDatatype = new Datatype<>(instanceClass, datatypeId);
			this.bagDatatype = Bag.Datatype.getInstance(instanceDatatype);
			this.emptyBag = Bag.empty(bagDatatype, null);
		}

		@Override
		public final String getId()
		{
			return this.instanceDatatype.getId();
		}

		/**
		 * Get datatype of values created by this factory
		 * 
		 * @return supported attribute value datatype
		 */
		public final Datatype<INSTANCE_AV> getDatatype()
		{
			return instanceDatatype;
		}

		/**
		 * Gets empty bag
		 * 
		 * @return empty bag
		 */
		public Bag<INSTANCE_AV> getEmptyBag()
		{
			return emptyBag;
		}

		/**
		 * Gets empty bag
		 * 
		 * @return empty bag
		 */
		public Bag.Datatype<INSTANCE_AV> getBagDatatype()
		{
			return bagDatatype;
		}

		/**
		 * Create attribute value from XML/JAXB mixed content and other XML attributes
		 * 
		 * @param content
		 *            list of (XACML/JAXB) AttributeValueType's mixed content elements of the
		 *            following types: {@link String}, {@link Element}
		 * @param otherAttributes
		 *            other XML attributes
		 * @param xPathCompiler
		 *            XPath compiler for compiling/evaluating XPath expressions in values, e.g.
		 *            {@link XPathAttributeValue}
		 * @return attribute value in internal model compatible with expression evaluator
		 * @throws IllegalArgumentException
		 *             if content/otherAttributes are not valid for the datatype handled by this
		 *             factory
		 */
		public abstract INSTANCE_AV getInstance(List<Serializable> content, Map<QName, String> otherAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public final String toString()
		{
			// immutable class -> we can cache the result
			if (toString == null)
			{
				toString = getClass().getName() + "[datatype=" + instanceDatatype + "]";
			}
			return toString;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public final int hashCode()
		{
			// immutable class -> we can cache the result
			if (hashCode == 0)
			{
				hashCode = instanceDatatype.getValueClass().hashCode();
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
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}

			final Factory<?> other = (Factory<?>) obj;
			/*
			 * if (instanceClass == null) { if (other.instanceClass != null) { return false; } }
			 * else
			 */
			return instanceDatatype.equals(other.instanceDatatype);
		}
	}

	private static final IllegalArgumentException UNDEF_ATTR_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype");

	private static final UnsupportedOperationException UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION = new UnsupportedOperationException("AttributeValue.setDataType() not allowed");

	private final Datatype<AV> datatype;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType#setDataType(java.lang.String)
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
	 * @param datatype
	 *            datatype.
	 * @param content
	 *            list of JAXB content elements of the following types: {@link String},
	 *            {@link Element}. Made immutable by this constructor.
	 * @param otherAttributes
	 *            other attributes, made immutable by this constructor.
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null}
	 */
	protected AttributeValue(Datatype<AV> datatype, List<Serializable> content, Map<QName, String> otherAttributes) throws IllegalArgumentException
	{
		// assert datatype != null;
		// assert content != null;
		// make fields immutable (datatype made immutable through overriding setDatatype())
		super(content == null ? null : Collections.unmodifiableList(content), validateAndGetId(datatype), otherAttributes == null ? null : Collections.unmodifiableMap(otherAttributes));
		this.datatype = datatype;
	}

	private static String validateAndGetId(Datatype<?> datatype)
	{
		if (datatype == null)
		{
			throw UNDEF_ATTR_DATATYPE_EXCEPTION;
		}

		return datatype.getId();
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public Datatype<AV> getReturnType()
	{
		return this.datatype;
	}

	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(this);
	}

}
