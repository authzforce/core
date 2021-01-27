/**
 * Copyright 2012-2021 THALES.
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.testutil.ext;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.io.NamedXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.NonIssuedLikeIssuedStrictXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils.NamedXacmlJaxbAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.XacmlRequestAttributeParser;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.core.pdp.testutil.ext.xmlns.TestAttributeProviderDescriptor;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 
 * Fake AttributeProviderModule for test purposes only that can be configured to support a specific set of attribute Providers, but always return an empty bag as attribute value.
 * 
 */
public class TestAttributeProvider extends BaseNamedAttributeProvider
{

	private static AttributeDesignatorType newAttributeDesignator(final Entry<AttributeFqn, AttributeBag<?>> attributeEntry)
	{
		final AttributeFqn attrKey = attributeEntry.getKey();
		final Bag<?> attrVals = attributeEntry.getValue();
		return new AttributeDesignatorType(attrKey.getCategory(), attrKey.getId(), attrVals.getElementDatatype().getId(), attrKey.getIssuer().orElse(null), false);
	}

	private final Set<AttributeDesignatorType> supportedDesignatorTypes;
	private final Map<AttributeFqn, AttributeBag<?>> attrMap;

	private TestAttributeProvider(final String id, final Map<AttributeFqn, AttributeBag<?>> attributeMap) throws IllegalArgumentException
	{
		super(id);
		attrMap = Collections.unmodifiableMap(attributeMap);
		this.supportedDesignatorTypes = attrMap.entrySet().stream().map(TestAttributeProvider::newAttributeDesignator).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public void close()
	{
		// nothing to close
	}

	@Override
	public Set<AttributeDesignatorType> getProvidedAttributes()
	{
		return supportedDesignatorTypes;
	}

	@Override
	public <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeGUID, final Datatype<AV> attributeDatatype, final EvaluationContext context)
	        throws IndeterminateEvaluationException
	{
		final AttributeBag<?> attrVals = attrMap.get(attributeGUID);
		if (attrVals == null)
		{
			return null;
		}

		if (attrVals.getElementDatatype().equals(attributeDatatype))
		{
			return (AttributeBag<AV>) attrVals;
		}

		throw new IndeterminateEvaluationException("Requested datatype (" + attributeDatatype + ") != provided by " + this + " (" + attrVals.getElementDatatype() + ")",
		        XacmlStatusCode.MISSING_ATTRIBUTE.value());
	}

	private static class DepAwareAttProviderFactory implements DependencyAwareFactory
	{
		private final String providerId;
		private final List<Attributes> jaxbAttCats;

		private DepAwareAttProviderFactory(final String providerId, final List<Attributes> jaxbAttributeCategories) {
			assert providerId != null && jaxbAttributeCategories != null;
			this.providerId = providerId;
			this.jaxbAttCats = jaxbAttributeCategories;
		}

		@Override
		public Set<AttributeDesignatorType> getDependencies()
		{
			// no dependency
			return null;
		}

		@Override
		public CloseableNamedAttributeProvider getInstance(final AttributeValueFactoryRegistry attributeValueFactories, final AttributeProvider depAttrProvider)
		{
			final NamedXacmlAttributeParser<Attribute> namedXacmlAttParser = new NamedXacmlJaxbAttributeParser(attributeValueFactories);
			final XacmlRequestAttributeParser<Attribute, AttributeBag<?>> xacmlAttributeParser = new NonIssuedLikeIssuedStrictXacmlAttributeParser<>(namedXacmlAttParser);
			final Set<String> attrCategoryNames = new HashSet<>();
			final Map<AttributeFqn, AttributeBag<?>> mutableAttMap = new HashMap<>();
			for (final Attributes jaxbAttributes : this.jaxbAttCats)
			{
				final String categoryName = jaxbAttributes.getCategory();
				if (!attrCategoryNames.add(categoryName))
				{
					throw new IllegalArgumentException("Unsupported repetition of Attributes[@Category='" + categoryName + "']");
				}

				for (final Attribute jaxbAttr : jaxbAttributes.getAttributes())
				{
					xacmlAttributeParser.parseNamedAttribute(categoryName, jaxbAttr, null, mutableAttMap);
				}
			}

			return new TestAttributeProvider(this.providerId, mutableAttMap);
		}
	}

	/**
	 * {@link TestAttributeProvider} factory
	 * 
	 */
	public static class Factory extends CloseableNamedAttributeProvider.FactoryBuilder<TestAttributeProviderDescriptor>
	{

		@Override
		public Class<TestAttributeProviderDescriptor> getJaxbClass()
		{
			return TestAttributeProviderDescriptor.class;
		}

		@Override
		public DependencyAwareFactory getInstance(final TestAttributeProviderDescriptor conf, final EnvironmentProperties environmentProperties)
		{
			return new DepAwareAttProviderFactory(conf.getId(), conf.getAttributes());
		}

	}

}
