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
/**
 * 
 */
package org.ow2.authzforce.core.test.custom;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.BaseAttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.CloseableAttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.JaxbXACMLAttributeParser;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.xmlns.test.TestAttributeProvider;

/**
 * 
 * Fake AttributeProviderModule for test purposes only that can be configured to support a specific set of attribute Providers, but always return an empty bag
 * as attribute value.
 * 
 */
public class TestAttributeProviderModule extends BaseAttributeProviderModule
{
	/**
	 * module factory
	 * 
	 */
	public static class Factory extends CloseableAttributeProviderModule.FactoryBuilder<TestAttributeProvider>
	{

		@Override
		public Class<TestAttributeProvider> getJaxbClass()
		{
			return TestAttributeProvider.class;
		}

		@Override
		public DependencyAwareFactory getInstance(final TestAttributeProvider conf)
		{
			return new DependencyAwareFactory()
			{

				@Override
				public Set<AttributeDesignatorType> getDependencies()
				{
					// no dependency
					return null;
				}

				@Override
				public CloseableAttributeProviderModule getInstance(DatatypeFactoryRegistry attrDatatypeFactory, AttributeProvider depAttrProvider)
				{
					return new TestAttributeProviderModule(conf, attrDatatypeFactory);
				}
			};
		}

	}

	private final Set<AttributeDesignatorType> supportedDesignatorTypes = new HashSet<>();
	private final Map<AttributeGUID, Bag<?>> attrMap = new HashMap<>();

	private TestAttributeProviderModule(TestAttributeProvider conf, DatatypeFactoryRegistry attrDatatypeFactory) throws IllegalArgumentException
	{
		super(conf.getId());
		final JaxbXACMLAttributeParser<Bag<?>> xacmlAttributeParser = new NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser(attrDatatypeFactory);
		final Set<String> attrCategoryNames = new HashSet<>();
		for (final Attributes jaxbAttributes : conf.getAttributes())
		{
			final String categoryName = jaxbAttributes.getCategory();
			if (!attrCategoryNames.add(categoryName))
			{
				throw new IllegalArgumentException("Unsupported repetition of Attributes[@Category='" + categoryName + "']");
			}

			for (final Attribute jaxbAttr : jaxbAttributes.getAttributes())
			{
				xacmlAttributeParser.parseAttribute(attrMap, new AttributeGUID(categoryName, jaxbAttr.getIssuer(), jaxbAttr.getAttributeId()),
						jaxbAttr.getAttributeValues(), null);
			}
		}

		for (final Entry<AttributeGUID, Bag<?>> attrEntry : attrMap.entrySet())
		{
			final AttributeGUID attrKey = attrEntry.getKey();
			final Bag<?> attrVals = attrEntry.getValue();
			supportedDesignatorTypes.add(new AttributeDesignatorType(attrKey.getCategory(), attrKey.getId(), attrVals.getElementDatatype().getId(), attrKey
					.getIssuer(), false));
		}
	}

	@Override
	public void close() throws IOException
	{
		// nothing to close
	}

	@Override
	public Set<AttributeDesignatorType> getProvidedAttributes()
	{
		return supportedDesignatorTypes;
	}

	@Override
	public <AV extends AttributeValue> Bag<AV> get(AttributeGUID attributeGUID, Datatype<AV> attributeDatatype, EvaluationContext context)
			throws IndeterminateEvaluationException
	{
		final Bag<?> attrVals = attrMap.get(attributeGUID);
		if (attrVals == null)
		{
			return null;
		}

		if (attrVals.getElementDatatype().equals(attributeDatatype))
		{
			return (Bag<AV>) attrVals;
		}

		throw new IndeterminateEvaluationException("Requested datatype (" + attributeDatatype + ") != provided by " + this + " ("
				+ attrVals.getElementDatatype() + ")", StatusHelper.STATUS_MISSING_ATTRIBUTE);
	}

}
