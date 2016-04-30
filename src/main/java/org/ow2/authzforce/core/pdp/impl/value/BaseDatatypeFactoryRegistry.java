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

import java.util.HashSet;
import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.impl.expression.PrimitiveValueExpression;

/**
 * Basic implementation of <code>DatatypeFactoryRegistry</code>.
 *
 * @author cdangerv
 * @version $Id: $
 */
public class BaseDatatypeFactoryRegistry extends BasePdpExtensionRegistry<DatatypeFactory<?>> implements DatatypeFactoryRegistry
{

	/** Constant <code>COMPARATOR</code> */
	protected static final PdpExtensionComparator<DatatypeFactory<?>> COMPARATOR = new PdpExtensionComparator<>();

	private final Set<DatatypeFactory<?>> datatypeFactoryClasses = new HashSet<>();

	/**
	 * <p>Constructor for BaseDatatypeFactoryRegistry.</p>
	 *
	 * @param attributeValueFactories a {@link java.util.Set} object.
	 */
	protected BaseDatatypeFactoryRegistry(Set<DatatypeFactory<?>> attributeValueFactories)
	{
		super(DatatypeFactory.class, attributeValueFactories);
	}

	/**
	 * Constructor that configures this factory with an initial set of supported datatypes.
	 *
	 * @param baseFactory
	 *            (base) factory that this factory extends, i.e. this factory inherits all the datatype factories of {@code baseFactory}. If null, this is
	 *            equivalent to {@link #BaseDatatypeFactoryRegistry()}.
	 */
	public BaseDatatypeFactoryRegistry(BaseDatatypeFactoryRegistry baseFactory)
	{
		super(DatatypeFactory.class, baseFactory);
	}

	/**
	 * Creates instance without any registered datatype
	 */
	public BaseDatatypeFactoryRegistry()
	{
		super(DatatypeFactory.class);
	}

	/**
	 * <p>get</p>
	 *
	 * @param typeId a {@link java.lang.String} object.
	 * @return a {@link org.ow2.authzforce.core.pdp.api.DatatypeFactory} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	protected DatatypeFactory<?> get(String typeId) throws IllegalArgumentException
	{
		final DatatypeFactory<?> datatypeFactory = getExtension(typeId);
		if (datatypeFactory == null)
		{
			throw new IllegalArgumentException("Attribute datatype '" + typeId + "' is not supported.");
		}

		return datatypeFactory;
	}

	/**
	 * <p>createValue</p>
	 *
	 * @param datatypeFactory a {@link org.ow2.authzforce.core.pdp.api.DatatypeFactory} object.
	 * @param jaxbAttrVal a {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType} object.
	 * @param xPathCompiler a {@link net.sf.saxon.s9api.XPathCompiler} object.
	 * @param <V> a V object.
	 * @return a V object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	protected static final <V extends AttributeValue> V createValue(DatatypeFactory<V> datatypeFactory, AttributeValueType jaxbAttrVal,
			XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		final V attrVal;
		try
		{
			attrVal = datatypeFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), xPathCompiler);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid Attribute value for datatype '" + datatypeFactory.getDatatype() + "'", e);
		}

		return attrVal;
	}

	private static final <V extends AttributeValue> PrimitiveValueExpression<V> createValueExpression(DatatypeFactory<V> datatypeFactory,
			AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler, boolean isStatic) throws IllegalArgumentException
	{
		final V rawValue = createValue(datatypeFactory, jaxbAttrVal, xPathCompiler);
		return new PrimitiveValueExpression<>(datatypeFactory.getDatatype(), rawValue, isStatic);
	}

	/** {@inheritDoc} */
	@Override
	public PrimitiveValueExpression<?> createValueExpression(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		final DatatypeFactory<?> datatypeFactory = get(jaxbAttrVal.getDataType());
		return createValueExpression(datatypeFactory, jaxbAttrVal, xPathCompiler, datatypeFactory.isExpressionStatic());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.BasePdpExtensionRegistry#addExtension(com.thalesgroup.authzforce .core.PdpExtension)
	 */
	/** {@inheritDoc} */
	@Override
	public void addExtension(DatatypeFactory<?> datatypeFactory) throws IllegalArgumentException
	{
		if (!datatypeFactoryClasses.add(datatypeFactory))
		{
			throw new IllegalArgumentException(
					"Attribute value factory ("
							+ datatypeFactory
							+ ") already registered for another datatype, or conflicting with another factory registered for the same datatype class in this list of registered factories: "
							+ datatypeFactoryClasses);
		}

		super.addExtension(datatypeFactory);
	}

}
