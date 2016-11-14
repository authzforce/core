/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.ConstantPrimitiveAttributeValueExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

import com.google.common.base.Preconditions;

/**
 * Immutable <code>DatatypeFactoryRegistry</code>.
 *
 * 
 * @version $Id: $
 */
public final class ImmutableDatatypeFactoryRegistry extends BasePdpExtensionRegistry<DatatypeFactory<?>> implements DatatypeFactoryRegistry
{
	/**
	 * <p>
	 * Constructor for BaseDatatypeFactoryRegistry.
	 * </p>
	 *
	 * @param attributeValueFactories
	 *            attribute value factories
	 */
	public ImmutableDatatypeFactoryRegistry(final Set<DatatypeFactory<?>> attributeValueFactories)
	{
		super(DatatypeFactory.class, Preconditions.checkNotNull(attributeValueFactories, "Input attribute datatype factories undefined (attributeValueFactories == null)"));
	}

	/**
	 * <p>
	 * createValue
	 * </p>
	 *
	 * @param datatypeFactory
	 *            a {@link org.ow2.authzforce.core.pdp.api.value.DatatypeFactory} object.
	 * @param jaxbAttrVal
	 *            a {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType} object.
	 * @param xPathCompiler
	 *            a {@link net.sf.saxon.s9api.XPathCompiler} object.
	 * @return a V object.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	private static <V extends AttributeValue> V newValue(final DatatypeFactory<V> datatypeFactory, final AttributeValueType jaxbAttrVal, final XPathCompiler xPathCompiler)
			throws IllegalArgumentException
	{
		final V attrVal;
		try
		{
			attrVal = datatypeFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), xPathCompiler);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid Attribute value for datatype '" + datatypeFactory.getDatatype() + "'", e);
		}

		return attrVal;
	}

	private static <V extends AttributeValue> ConstantExpression<V> newExpression(final DatatypeFactory<V> datatypeFactory, final AttributeValueType jaxbAttrVal,
			final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		final V rawValue = newValue(datatypeFactory, jaxbAttrVal, xPathCompiler);
		return new ConstantPrimitiveAttributeValueExpression<>(datatypeFactory.getDatatype(), rawValue);
	}

	/** {@inheritDoc} */
	@Override
	public ConstantExpression<? extends AttributeValue> newExpression(final AttributeValueType jaxbAttrVal, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		final DatatypeFactory<?> datatypeFactory = getExtension(jaxbAttrVal.getDataType());
		return newExpression(datatypeFactory, jaxbAttrVal, xPathCompiler);
	}

}
