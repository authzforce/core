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
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.w3c.dom.Element;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.eval.BagDatatype;
import com.thalesgroup.authzforce.core.eval.Bags;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression.Value;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.Bag;

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
 * @param <V>
 *            concrete type subclass
 * 
 */
public abstract class AttributeValue<V extends AttributeValue<V>> extends AttributeValueType implements Serializable, Value<V, V>
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
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class Factory<AV extends AttributeValue<AV>> implements PdpExtension
	{
		private static final IllegalArgumentException NULL_DATATYPE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined instanceClass argument");
		private static final IllegalArgumentException NULL_DATATYPE_ID_EXCEPTION = new IllegalArgumentException("Undefined datatypeId argument");

		protected final Datatype<AV> instanceDatatype;
		private final Bag<AV> emptyBag;

		// cached method result
		private int hashCode = 0;
		private String toString = null;

		protected Factory(Class<AV> instanceClass, String datatypeId)
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
			this.emptyBag = Bags.empty(instanceDatatype);
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
		public final Datatype<AV> getDatatype()
		{
			return instanceDatatype;
		}

		/**
		 * Gets empty bag
		 * 
		 * @return empty bag
		 */
		public Bag<AV> getEmptyBag()
		{
			return emptyBag;
		}

		/**
		 * Gets empty bag
		 * 
		 * @return empty bag
		 */
		public BagDatatype<AV> getBagDatatype()
		{
			return emptyBag.getDatatype();
		}

		/**
		 * Create attribute value from XML/JAXB mixed content and other XML attributes
		 * 
		 * @param content
		 *            list of (XACML/JAXB) AttributeValueType's mixed content elements of the
		 *            following types: {@link String}, {@link Element}
		 * @param otherAttributes
		 *            other XML attributes
		 * @return attribute value in internal model compatible with expression evaluator
		 * @throws IllegalArgumentException
		 *             if content/otherAttributes are not valid for the datatype handled by this
		 *             factory
		 */
		public abstract AV getInstance(List<Serializable> content, Map<QName, String> otherAttributes) throws IllegalArgumentException;

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
				hashCode = Objects.hash(instanceDatatype.getValueClass());
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
			 */if (!instanceDatatype.equals(other.instanceDatatype))
			{
				return false;
			}
			return true;
		}
	}

	private static IllegalArgumentException UNDEF_ATTR_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype");

	private static final UnsupportedOperationException UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION = new UnsupportedOperationException("AttributeValue.setDataType() not allowed");

	private final Datatype<V> datatype;

	// cached method result(s)
	private V[] all = null;

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
	protected AttributeValue(Datatype<V> datatype, List<Serializable> content, Map<QName, String> otherAttributes) throws IllegalArgumentException
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
	public Datatype<V> getReturnType()
	{
		return this.datatype;
	}

	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(this);
	}

	@Override
	public V evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return one();
	}

	@Override
	public V[] all()
	{
		if (all == null)
		{
			all = (V[]) Array.newInstance(datatype.getValueClass(), 1);
			all[0] = one();
		}

		return all;
	}

}
