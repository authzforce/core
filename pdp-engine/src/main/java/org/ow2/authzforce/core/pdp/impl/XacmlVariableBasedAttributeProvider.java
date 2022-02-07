/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.core.xmlns.pdp.XacmlVarBasedAttributeProviderDescriptor;

import java.util.Optional;
import java.util.Set;

/**
 * AttributeProvider that provides attributes based on XACML VariableDefinitions, allowing to use XACML Variables like AttributeDesignators, e.g. in Match elements.
 * The AttributeId is handled as the VariableId (Issuer ignored).
 * The attribute value is returned as a singleton bag containing the Variable value as singleton value (or empty Bag if no such Variable defined).
 */
public final class XacmlVariableBasedAttributeProvider extends BaseNamedAttributeProvider
{
    private final ImmutableSet<AttributeDesignatorType> supportedAttDesignators;
    private final String supportedAttCategory;
    private final UnsupportedOperationException invalidAttCatEx;
    private final AttributeSource attSrc;


    /**
     * @param attributeCategory Defines the attribute Category that is supported by this Attribute Provider, i.e.
     *                                     any AttributeDesignator or AttributeSelector/ContextSelectorId with this Category is handled exactly like a VariableReference and the AttributeId used as VariableId (Issuer ignored).
     */
    private XacmlVariableBasedAttributeProvider(String id, String attributeCategory)
    {
        super(id);
        assert attributeCategory != null && !attributeCategory.isEmpty();
        supportedAttCategory = attributeCategory;
        invalidAttCatEx = new UnsupportedOperationException("Unsupported attribute category: " + supportedAttCategory);
        supportedAttDesignators = ImmutableSet.of(new AttributeDesignatorType(attributeCategory, null, null, null, false));
        attSrc = AttributeSources.newCustomSource(this.getInstanceID());
    }

    @Override
    public void close()
    {
        // nothing to close
    }

    @Override
    public Set<AttributeDesignatorType> getProvidedAttributes()
    {
        return supportedAttDesignators;
    }

    @Override
    public <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFQN, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
    {
        Preconditions.checkArgument(attributeFQN != null && datatype != null && context != null && mdpContext != null, "Invalid args");
        if(!supportedAttCategory.equals(attributeFQN.getCategory())) {
            throw invalidAttCatEx;
        }

        final AV varVal = context.getVariableValue(attributeFQN.getId(), datatype);
        return Bags.singletonAttributeBag(datatype, varVal, attSrc);
    }


    private static final class DepAwareFactory implements DependencyAwareFactory
    {
        private final String createdInstanceId;
        private final String category;

        private DepAwareFactory(String id, String category)
        {
            assert id != null && category != null;
            this.createdInstanceId = id;
            this.category = category;
        }

        @Override
        public Set<AttributeDesignatorType> getDependencies()
        {
            // no dependency
            return Set.of();
        }

        @Override
        public CloseableNamedAttributeProvider getInstance(AttributeValueFactoryRegistry attributeValueFactoryRegistry, NamedAttributeProvider attributeProvider)
        {
            return new XacmlVariableBasedAttributeProvider(this.createdInstanceId, this.category);
        }
    }

    /**
     * AttributeProvider factory
     */
    public static final class Factory extends FactoryBuilder<XacmlVarBasedAttributeProviderDescriptor>
    {

        @Override
        public Class<XacmlVarBasedAttributeProviderDescriptor> getJaxbClass()
        {
            return XacmlVarBasedAttributeProviderDescriptor.class;
        }

        @Override
        public DependencyAwareFactory getInstance(final XacmlVarBasedAttributeProviderDescriptor conf, final EnvironmentProperties environmentProperties)
        {
            final String id = conf.getId();
            final String category = conf.getCategory();
            Preconditions.checkArgument(id != null && !id.isEmpty() && category != null && !category.isEmpty(), "Invalid args: id or category is null/empty");
            return new DepAwareFactory(conf.getId(), conf.getCategory());
        }

    }

}
