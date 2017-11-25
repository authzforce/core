/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.ConstantPrimitiveAttributeValueExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

import com.google.common.base.Preconditions;

/**
 * Immutable <code>DatatypeFactoryRegistry</code>.
 *
 * 
 * @version $Id: $
 */
public final class ImmutableAttributeValueFactoryRegistry extends BasePdpExtensionRegistry<AttributeValueFactory<?>> implements AttributeValueFactoryRegistry
{
	private static final IllegalArgumentException ILLEGAL_DATATYPE_ID_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined datatype ID");

	/**
	 * <p>
	 * Constructor for BaseDatatypeFactoryRegistry.
	 * </p>
	 *
	 * @param attributeValueFactories
	 *            attribute value factories
	 */
	public ImmutableAttributeValueFactoryRegistry(final Set<? extends AttributeValueFactory<?>> attributeValueFactories)
	{
		super(AttributeValueFactory.class, Preconditions.checkNotNull(attributeValueFactories, "Input attribute datatype factories undefined (attributeValueFactories == null)"));
	}

	/**
	 * <p>
	 * createValue
	 * </p>
	 *
	 * @param attValFactory
	 *            a {@link org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory} object.
	 * @param content
	 *            attribute value's mixed content.
	 * @param otherAttributes
	 *            attribute value's optional XML attributes.
	 * @param xPathCompiler
	 *            a {@link net.sf.saxon.s9api.XPathCompiler} object.
	 * @return a V object.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	private static <V extends AttributeValue> V newValue(final AttributeValueFactory<V> attValFactory, final List<Serializable> content, final Map<QName, String> otherAttributes,
			final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		assert attValFactory != null;
		final V attrVal;
		try
		{
			attrVal = attValFactory.getInstance(content, otherAttributes, xPathCompiler);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid Attribute value for datatype '" + attValFactory.getDatatype() + "'", e);
		}

		return attrVal;
	}

	private static <V extends AttributeValue> ConstantExpression<V> newExpression(final AttributeValueFactory<V> attValFactory, final List<Serializable> content,
			final Map<QName, String> otherAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		assert attValFactory != null;
		final V rawValue = newValue(attValFactory, content, otherAttributes, xPathCompiler);
		return new ConstantPrimitiveAttributeValueExpression<>(attValFactory.getDatatype(), rawValue);
	}

	/** {@inheritDoc} */
	@Override
	public ConstantExpression<? extends AttributeValue> newExpression(final String datatypeId, final List<Serializable> content, final Map<QName, String> otherAttributes,
			final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		if (datatypeId == null)
		{
			throw ILLEGAL_DATATYPE_ID_ARGUMENT_EXCEPTION;
		}

		final AttributeValueFactory<?> datatypeFactory = getExtension(datatypeId);
		if (datatypeFactory == null)
		{
			throw new IllegalArgumentException("Unsupported datatype ID (no supporting attribute value factory/parser): " + datatypeId);
		}

		return newExpression(datatypeFactory, content, otherAttributes, xPathCompiler);
	}

}
