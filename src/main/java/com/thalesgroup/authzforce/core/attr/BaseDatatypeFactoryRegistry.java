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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.attr.AttributeValue.Factory;

/**
 * Basic implementation of <code>DatatypeFactoryRegistry</code>.
 */
public class BaseDatatypeFactoryRegistry extends BasePdpExtensionRegistry<AttributeValue.Factory<?>> implements DatatypeFactoryRegistry
{

	private final Set<Factory<?>> datatypeFactoryClasses = new HashSet<>();

	protected BaseDatatypeFactoryRegistry(Map<String, AttributeValue.Factory<?>> attributeValueFactoriesByDatatypeURI)
	{
		super(AttributeValue.Factory.class, attributeValueFactoriesByDatatypeURI);
	}

	/**
	 * Constructor that configures this factory with an initial set of supported datatypes.
	 * 
	 * @param baseFactory
	 *            (base) factory that this factory extends, i.e. this factory inherits all the
	 *            datatype factories of {@code baseFactory}. If null, this is equivalent to
	 *            {@link #BaseDatatypeFactoryRegistry()}.
	 */
	public BaseDatatypeFactoryRegistry(BaseDatatypeFactoryRegistry baseFactory)
	{
		super(AttributeValue.Factory.class, baseFactory);
	}

	/**
	 * Creates instance without any registered datatype
	 */
	public BaseDatatypeFactoryRegistry()
	{
		super(AttributeValue.Factory.class);
	}

	@Override
	public AttributeValue<?> createValue(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws UnknownIdentifierException, ParsingException
	{
		final String type = jaxbAttrVal.getDataType();
		final AttributeValue.Factory<?> attrFactory = getExtension(type);
		if (attrFactory == null)
		{
			throw new UnknownIdentifierException("Attribute datatype '" + type + "' is not supported.");
		}

		final AttributeValue<?> attrVal;
		try
		{
			attrVal = attrFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), xPathCompiler);
		} catch (IllegalArgumentException e)
		{
			throw new ParsingException("Invalid Attribute value of type '" + type + "'", e);
		}

		return attrVal;
	}

	@Override
	public <T extends AttributeValue<T>> T createValue(AttributeValueType jaxbAttrVal, Class<T> valClass, XPathCompiler xPathCompiler) throws UnknownIdentifierException, ParsingException
	{
		final AttributeValue<?> attrVal = createValue(jaxbAttrVal, xPathCompiler);

		try
		{
			return valClass.cast(attrVal);
		} catch (ClassCastException e)
		{
			throw new IllegalArgumentException("Required attribute value class (" + valClass + ") does not match actual class (" + attrVal.getClass() + ") returned by datatype factory for input XACML/JAXB AttributeValue's datatype URI  (" + jaxbAttrVal.getDataType() + ")", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thalesgroup.authzforce.core.BasePdpExtensionRegistry#addExtension(com.thalesgroup.authzforce
	 * .core.PdpExtension)
	 */
	@Override
	public void addExtension(AttributeValue.Factory<?> datatypeFactory) throws IllegalArgumentException
	{
		if (!datatypeFactoryClasses.add(datatypeFactory))
		{
			throw new IllegalArgumentException("Attribute value factory (" + datatypeFactory + ") already registered for another datatype, or conflicting with another factory registered for the same datatype class in this list of registered factories: " + datatypeFactoryClasses);
		}

		super.addExtension(datatypeFactory);
	}

}
