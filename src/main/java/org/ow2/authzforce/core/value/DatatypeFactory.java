/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.PdpExtension;
import org.w3c.dom.Element;

/**
 * Datatype-specific Attribute Value Factory.
 * 
 * @param <INSTANCE_AV>
 *            type of instance (attribute values) created by this factory
 */
public abstract class DatatypeFactory<INSTANCE_AV extends AttributeValue> implements PdpExtension
{
	/**
	 * Primitive datatype
	 *
	 * @param <AV>
	 *            attribute value type
	 */
	private static class PrimitiveDatatype<AV extends AttributeValue> extends Datatype<AV>
	{
		private final transient int hashCode;

		/**
		 * Instantiates primitive datatype
		 * 
		 * @param valueClass
		 *            class implementing this primitive datatype
		 * 
		 * @param id
		 *            datatype ID (e.g. XACML datatype URI) which identifies this primitive datatype
		 * @throws IllegalArgumentException
		 *             if {@code valueClass == null || id == null }
		 */
		private PrimitiveDatatype(Class<AV> valueClass, String id) throws IllegalArgumentException
		{
			super(valueClass, id);
			// there should be one-to-one mapping between valueClass and id, so hashing
			// only one of these two is necessary
			hashCode = getValueClass().hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getId();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof PrimitiveDatatype))
			{
				return false;
			}

			final PrimitiveDatatype<?> other = (PrimitiveDatatype<?>) obj;
			// there should be a one-to-one mapping between valueClass and id, so checking
			// only one of these two is necessary
			return this.getValueClass() == other.getValueClass();
		}

		@Override
		public Datatype<?> getTypeParameter()
		{
			return null;
		}
	}

	private static final IllegalArgumentException NULL_DATATYPE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined instanceClass argument");
	private static final IllegalArgumentException NULL_DATATYPE_ID_EXCEPTION = new IllegalArgumentException("Undefined datatypeId argument");

	protected final Datatype<INSTANCE_AV> instanceDatatype;
	private final Bag<INSTANCE_AV> emptyBag;
	private final BagDatatype<INSTANCE_AV> bagDatatype;

	// cached method result
	private final transient int hashCode;
	private final transient String toString;

	protected DatatypeFactory(Class<INSTANCE_AV> instanceClass, String datatypeId)
	{
		if (instanceClass == null)
		{
			throw NULL_DATATYPE_CLASS_EXCEPTION;
		}

		if (datatypeId == null)
		{
			throw NULL_DATATYPE_ID_EXCEPTION;
		}

		this.instanceDatatype = new PrimitiveDatatype<>(instanceClass, datatypeId);
		this.emptyBag = Bags.empty(instanceDatatype, null);
		this.bagDatatype = new BagDatatype<>(instanceDatatype);
		this.toString = getClass().getName() + "[datatype=" + instanceDatatype + "]";
		this.hashCode = instanceDatatype.getValueClass().hashCode();
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
	public final Bag<INSTANCE_AV> getEmptyBag()
	{
		return emptyBag;
	}

	/**
	 * Gets empty bag
	 * 
	 * @return empty bag
	 */
	public final BagDatatype<INSTANCE_AV> getBagDatatype()
	{
		return bagDatatype;
	}

	/**
	 * Create attribute value from XML/JAXB mixed content and other XML attributes
	 * 
	 * @param content
	 *            list of (XACML/JAXB) AttributeValueType's mixed content elements of the following types: {@link String}, {@link Element}
	 * @param otherAttributes
	 *            other XML attributes
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, e.g. {@link XPathValue}
	 * @return attribute value in internal model compatible with expression evaluator
	 * @throws IllegalArgumentException
	 *             if content/otherAttributes are not valid for the datatype handled by this factory
	 */
	public abstract INSTANCE_AV getInstance(List<Serializable> content, Map<QName, String> otherAttributes, XPathCompiler xPathCompiler)
			throws IllegalArgumentException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
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
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof DatatypeFactory))
		{
			return false;
		}

		final DatatypeFactory<?> other = (DatatypeFactory<?>) obj;
		/*
		 * if (instanceClass == null) { if (other.instanceClass != null) { return false; } } else
		 */
		return instanceDatatype.equals(other.instanceDatatype);
	}

	/**
	 * True iff the expression based on this value always evaluates to the same constant in any evaluation context (not the case of xpathExpressions for
	 * instance).
	 */
	protected abstract boolean isExpressionStatic();
}