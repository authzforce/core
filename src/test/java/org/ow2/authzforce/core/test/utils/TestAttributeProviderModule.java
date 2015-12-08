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
/**
 * 
 */
package org.ow2.authzforce.core.test.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.AttributeProvider;
import org.ow2.authzforce.core.BaseAttributeProviderModule;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.XACMLParsers.JaxbXACMLAttributeParser;
import org.ow2.authzforce.core.XACMLParsers.NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser;
import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;
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
	public static class Factory extends BaseAttributeProviderModule.Factory<TestAttributeProvider>
	{

		@Override
		public Class<TestAttributeProvider> getJaxbClass()
		{
			return TestAttributeProvider.class;
		}

		@Override
		public DependencyAwareFactory parseDependencies(final TestAttributeProvider conf)
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
				public BaseAttributeProviderModule getInstance(DatatypeFactoryRegistry attrDatatypeFactory, AttributeProvider depAttrProvider)
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
