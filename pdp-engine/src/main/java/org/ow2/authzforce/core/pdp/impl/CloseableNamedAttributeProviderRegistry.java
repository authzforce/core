/*
 * Copyright 2012-2023 THALES.
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
import com.google.common.collect.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of {@link CloseableNamedAttributeProvider}s
 * <p>
 * The AttributeProviders may very likely hold resources such as network resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore, you are required to call
 * {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in order to make sure these resources are released properly by each
 * underlying module (e.g. close the attribute caches).
 * </p>
 *
 * @version $Id: $
 */
public final class CloseableNamedAttributeProviderRegistry implements Closeable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CloseableNamedAttributeProviderRegistry.class);

	private static final NamedAttributeProvider NOOP_NAMED_ATT_PROVIDER = new NamedAttributeProvider()
	{
		@Override
		public Set<AttributeDesignatorType> getProvidedAttributes()
		{
			return Set.of();
		}

		@Override
		public <AV extends AttributeValue> AttributeBag<AV> get(AttributeFqn attributeFqn, Datatype<AV> datatype, EvaluationContext evaluationContext, Optional<EvaluationContext> optional) throws IllegalArgumentException
		{
			return Bags.emptyAttributeBag(datatype, null);
		}
	};

	/**
	 * Named Attribute Provider based only on the evaluation context, i.e. it does not use any extra attribute provider module to get attribute values if not found in the context
	 */
	private static final class EvaluationContextOnlyScopedMultiNamedAttributeProvider extends EvaluationContextBasedMultiNamedAttributeProvider
	{
		private static final DelegateSupplier NO_OP_DELEGATE_SUPPLIER = new DelegateSupplier()
		{
			@Override
			public <AV extends AttributeValue> DelegateAttributeProvider<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype)
			{
				return (attributeFqn1, datatype1, context, mdpContext) -> Bags.emptyAttributeBag(datatype1, null);
			}
		};

		/**
		 * Creates new instance for given provided attribute and delegate attribute provider to be called if not found in evaluation context
		 *
		 * @param providedAttributes              provided attributes
		 * @param strictAttributeIssuerMatch whether to apply strict match on the attribute Issuer
		 */
		private EvaluationContextOnlyScopedMultiNamedAttributeProvider(final ImmutableSet<AttributeDesignatorType> providedAttributes, final boolean strictAttributeIssuerMatch)
		{
			super(providedAttributes, strictAttributeIssuerMatch, NO_OP_DELEGATE_SUPPLIER);
		}
	}

	private static final class CompositeMultiNamedAttributeProvider extends EvaluationContextBasedMultiNamedAttributeProvider
	{

		private static DelegateSupplier newDelegateSupplier(final ListMultimap<AttributeFqn, NamedAttributeProvider> providersByAttributeName, final Set<AttributeDesignatorType> requiredProvidedAttributes, boolean strictAttributeIssuerMatch)
		{
			/*
			Recreate a ListMultimap that consists of:
			 - a subset of entries from providersByAttributeName "matching" requiredProvidedAttributes (all providers not providing any of requiredProvidedAttributes should be filtered out as a result)
			 - EvaluationContext-only-based (extracting from the request context only) attribute provider as default provider if there is no match for a given providedAttribute, as all requiredProvidedAttributes are... required (must be provided somehow).
			 */
			final MultimapBuilder.MultimapBuilderWithKeys<@Nullable Object> multimapBuilder = MultimapBuilder.hashKeys(requiredProvidedAttributes.size());
			//assert multimapBuilder != null;
			final MultimapBuilder.ListMultimapBuilder<@Nullable Object, @Nullable Object> listMultimapBuilder = multimapBuilder.arrayListValues(1);
			//assert listMultimapBuilder != null;
			final ListMultimap<AttributeFqn, NamedAttributeProvider> mutableMatchingProvidersByAttrName = listMultimapBuilder.build();
			for (final AttributeDesignatorType providedAttDes : requiredProvidedAttributes)
			{
				final AttributeFqn providedAttName = AttributeFqns.newInstance(providedAttDes);
				final List<NamedAttributeProvider> matchingProviders = providersByAttributeName.get(providedAttName);
				/*
				 * Empty matchingProviders list returned if no provider of providedAttName, in which case it means it should be provided by the request context (in the initial request from PEP)
				 */
				if (matchingProviders.isEmpty())
				{
					mutableMatchingProvidersByAttrName.put(providedAttName, new EvaluationContextOnlyScopedMultiNamedAttributeProvider(ImmutableSet.of(providedAttDes), strictAttributeIssuerMatch));
				} else {
					mutableMatchingProvidersByAttrName.putAll(providedAttName, matchingProviders);
				}
			}

			final ImmutableListMultimap<AttributeFqn, NamedAttributeProvider> matchingProvidersByAttName = ImmutableListMultimap.copyOf(mutableMatchingProvidersByAttrName);

			return new DelegateSupplier()
			{
				@Override
				public <AV extends AttributeValue> DelegateAttributeProvider<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype) throws IndeterminateEvaluationException
				{
					final List<NamedAttributeProvider> subProviders = matchingProvidersByAttName.get(attributeFqn);
					/*
					 * A non-null empty list is expected if no mappings
					 */
					//assert subProviders != null;
					if (subProviders.isEmpty())
					{
						LOGGER.debug("No value found for required attribute {}, type={} in evaluation context and not supported by any Attribute Provider module", attributeFqn, datatype);
						throw new IndeterminateEvaluationException("Not in context and no Attribute Provider module supporting requested attribute: " + attributeFqn,
								XacmlStatusCode.MISSING_ATTRIBUTE.value());
					}

					return newDelegate(subProviders);
				}
			};
		}

		private CompositeMultiNamedAttributeProvider(final ImmutableSet<AttributeDesignatorType> providedAttributes, final boolean strictAttributeIssuerMatch, final ListMultimap<AttributeFqn, NamedAttributeProvider> providersByAttributeName)
		{
			super(providedAttributes, strictAttributeIssuerMatch, newDelegateSupplier(providersByAttributeName, providedAttributes, strictAttributeIssuerMatch));
		}

	}

	private static void close(final Set<CloseableNamedAttributeProvider> closeableProviders) throws IOException
	{
		/* An error occurring on closing one module should not stop from closing
		 the others
		 But we keep the exception in memory if any, to throw it at the end as
		 we do not want to hide that an error occurred
		*/
		IOException latestEx = null;
		for (final CloseableNamedAttributeProvider subProvider : closeableProviders)
		{
			try
			{
				subProvider.close();
			} catch (final IOException e)
			{
				latestEx = e;
			}
		}

		if (latestEx != null)
		{
			throw latestEx;
		}
	}

	/*
	 * Attribute Providers by supported/provided attribute name (category, issuer, AttributeId), supporting only specific attribute names
	 */
	// not-null
	private final ListMultimap<AttributeFqn, NamedAttributeProvider> namedAttProvidersByAttFqn;

	/*
	 * Attribute Providers by supported/provided attribute category, supporting any attribute in the given category
	 * E.g. the XacmlVariableBasedAttributeProvider provide any attribute in a given defined category if a XACML VariableId matching the AttributeId exists
	 */
	// not null
	private final ListMultimap<String, NamedAttributeProvider> categoryWideNamedAttProvidersByCategory;

	// not-null
	private final Set<CloseableNamedAttributeProvider> closeableProviders;

	private final List<NamedAttributeProvider> mdpReqBeginners;

	private final List<NamedAttributeProvider> individualReqBeginners;

	/**
	 * Instantiates a "composite/modular" Attribute Provider that tries to find attribute values in evaluation context, then, if not there, query dedicated sub-provider(s) (created from {@code attributeProviderFactories}) providing the requested attribute ID, if there is any.
	 *
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * @param attributeProviderFactories
	 *            Factories of all the Attribute Providers to be combined in the created instance (Attribute Providers resolve values of attributes absent from the request context). Empty if none.
	 *            <b>We assume that they are listed in dependency order, i.e. for any AttributeProvider AP (at index N) in the list, if AP depends on attribute(s) A, B, etc. then A, B, etc. are assumed to be provided by either another AttributeProvider preceding AP in the list (at index n < N), or the PDP input request directly.</b>
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
	 *            the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
	 *            the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute SHALL be governed by
	 *            AttributeId and DataType attributes alone."
	 * @throws java.lang.IllegalArgumentException
	 *             If any Attribute Provider created from {@code attributeProviderFactories} does not provide any attribute.
	 * @throws java.io.IOException
	 *             error closing the Attribute Providers created from {@code attributeProviderFactories}, when a {@link IllegalArgumentException} is raised
	 */
	public CloseableNamedAttributeProviderRegistry(final List<CloseableNamedAttributeProvider.DependencyAwareFactory> attributeProviderFactories,
																	  final AttributeValueFactoryRegistry attributeFactory, final boolean strictAttributeIssuerMatch) throws IOException
	{
		Preconditions.checkArgument(attributeProviderFactories != null && !attributeProviderFactories.isEmpty(), "No input AttributeProvider");
		Preconditions.checkArgument(attributeFactory != null, "No input AttributeValue factory");

		// attributeProviderFactories != null && attributeProviderFactories.size() >= 1
		final ListMultimap<AttributeFqn, NamedAttributeProvider> mutableListOfAttNameSpecificProvidersByAttName = ArrayListMultimap.create();
		final ListMultimap<String, NamedAttributeProvider> mutableListOfCategoryWideProvidersByAttCategory = ArrayListMultimap.create();
		final int moduleCount = attributeProviderFactories.size();
		final Set<CloseableNamedAttributeProvider> mutableSubProviderSet = HashCollections.newUpdatableSet(moduleCount);
		final List<NamedAttributeProvider> mutableMdpReqBeginners = new ArrayList<>();
		final List<NamedAttributeProvider> mutableIndividualReqBeginners = new ArrayList<>();
		for (final CloseableNamedAttributeProvider.DependencyAwareFactory attProviderFactory : attributeProviderFactories)
		{
			/*
			Create the sub-provider from {@code attProviderFactory}
			 */
			final Set<AttributeDesignatorType> requiredAttrs = attProviderFactory.getDependencies();

			try
			{
				/*
				 * The sub-provider will be given a secondary (read-only) AttributeProvider - aka "dependency Attribute Provider" - to help it find any attribute it requires (dependency). According to the description of the {@code attributeProviderFactories} parameter of this method, we create this "dependency attribute provider" as a combination of the Attribute
				 * Provider modules that precede the current sub-provider ( {@code attProviderFactory} ) in the list {@code attributeProviderFactories} and provide any (required) attribute of {@code requiredAttrs}.
				 *
				 * The "dependency attribute provider" is made read-only so that it may be used only to get required attributes, nothing else.
				 *
				 * Let's create this dependency attribute Provider first.
				 */
				final NamedAttributeProvider depAttrProvider;
				if (requiredAttrs == null || requiredAttrs.isEmpty())
				{
					/*
					No dependency, so we create an "empty" dependency attribute provider that does nothing.
					 */
					depAttrProvider = NOOP_NAMED_ATT_PROVIDER;
				} else
				{
					// requiredAttrs != null && requiredAttrs.size() >= 1
					final ImmutableListMultimap<AttributeFqn, NamedAttributeProvider> immutableCopyOfAttrProviderModsByAttrId = ImmutableListMultimap.copyOf(mutableListOfAttNameSpecificProvidersByAttName);
					depAttrProvider = new CompositeMultiNamedAttributeProvider(ImmutableSet.copyOf(requiredAttrs), strictAttributeIssuerMatch, immutableCopyOfAttrProviderModsByAttrId);
				}
				/*
				 * sub-provider closing isn't done in this method but handled in close() method when closing all modules
				 */
				final CloseableNamedAttributeProvider subProvider = attProviderFactory.getInstance(attributeFactory, depAttrProvider);
				/*
				Validate the sub-provider's list of provided attributes
				 */
                final Set<AttributeDesignatorType> providedAttributes = subProvider.getProvidedAttributes();
                if (providedAttributes == null || providedAttributes.isEmpty()) {
                    subProvider.close();
                    throw new IllegalArgumentException("Invalid named Attribute Provider '" + subProvider + "' : list of supported AttributeDesignators is null or empty");
                }

				// providedAttributes != null && providedAttributes.size() >= 1
				mutableSubProviderSet.add(subProvider);
				if(subProvider.supportsBeginMultipleDecisionRequest()) {
					mutableMdpReqBeginners.add(subProvider);
				}

				if(subProvider.supportsBeginIndividualDecisionRequest()) {
					mutableIndividualReqBeginners.add(subProvider);
				}

				for (final AttributeDesignatorType attrDesignator : providedAttributes)
				{
					final String providedAttCat = attrDesignator.getCategory();
					Preconditions.checkArgument(providedAttCat != null && !providedAttCat.isEmpty(), "Invalid AttributeDesignator returned by the attribute provider <"+attProviderFactory+">'s getProvidedAttribute() method: null/empty Category ");
					final String providedAttId = attrDesignator.getAttributeId();
					if(providedAttId == null) {
						// Category-wide attribute provider (any AttributeId in the given category providedAttCat)
						mutableListOfCategoryWideProvidersByAttCategory.put(providedAttCat, subProvider);
					}else
					{

						final AttributeFqn providedAttName = AttributeFqns.newInstance(providedAttCat, Optional.ofNullable(attrDesignator.getIssuer()), providedAttId);
						/*
						 * We allow multiple modules supporting the same attribute designator (as fall-back: if one does not find any value, the next one comes in)
						 */
						mutableListOfAttNameSpecificProvidersByAttName.put(providedAttName, subProvider);
					}
				}
				// mutableListOfAttNameSpecificProvidersByAttName.size() >= 1
			} catch (final IllegalArgumentException e)
			{
				close(mutableSubProviderSet);
				throw e;
			}
		}

		// mutableListOfAttNameSpecificProvidersByAttName.size() >= 1 && mutableSubProviderSet.size() >= 1
		assert !mutableListOfAttNameSpecificProvidersByAttName.isEmpty() && !mutableSubProviderSet.isEmpty();
		this.namedAttProvidersByAttFqn = ImmutableListMultimap.copyOf(mutableListOfAttNameSpecificProvidersByAttName);
		this.categoryWideNamedAttProvidersByCategory = ImmutableListMultimap.copyOf(mutableListOfCategoryWideProvidersByAttCategory);
		this.closeableProviders = HashCollections.newImmutableSet(mutableSubProviderSet);
		this.mdpReqBeginners = ImmutableList.copyOf(mutableMdpReqBeginners);
		this.individualReqBeginners = ImmutableList.copyOf(mutableIndividualReqBeginners);
	}

	/**
	 * Get AttributeProviders for a given attribute
	 * @param attributeName attribute name
	 * @return providers, empty list if there is none
	 */
	public List<NamedAttributeProvider> getProviders(final AttributeFqn attributeName) {
		/*
		If there is no provider for the full attribute name (Category/Issuer/Id), try to get providers for the same attribute category (category-wide, i.e. any attribute in the category)
		 */
		final List<NamedAttributeProvider> attNameMatchingProviders = this.namedAttProvidersByAttFqn.get(attributeName);
		return attNameMatchingProviders.isEmpty()? this.categoryWideNamedAttProvidersByCategory.get(attributeName.getCategory()): attNameMatchingProviders;
	}

	/**
	 * When the Multiple Decision Profile is used, the PDP engine calls this method before evaluating the Individual Decision Requests of a given Multiple Decision request.
	 * This call is passed on to all AttributeProviders (used in this factory) that have {@link NamedAttributeProvider#supportsBeginMultipleDecisionRequest()} return true
	 * @param mdpContext context of a Multiple Decision request evaluation, will be passed on as {@code mdpContext} argument of each AttributeProvider ( {@link NamedAttributeProvider#get(AttributeFqn, Datatype, EvaluationContext, Optional)} ) when Individual Decision requests are evaluated.
	 */
	public void beginMultipleDecisionRequest(final EvaluationContext mdpContext) {
		this.mdpReqBeginners.forEach(provider -> provider.beginMultipleDecisionRequest(mdpContext));
	}

	/**
	 * For each Individual Decision request, the PDP engine calls this method before the evaluation against the policy.
	 * This call is passed on to all AttributeProviders (used in this factory) that have {@link NamedAttributeProvider#supportsBeginIndividualDecisionRequest()} return true
	 * @param context individual decision request context, will be passed on as {@code context} argument of each AttributeProvider ( {@link NamedAttributeProvider#get(AttributeFqn, Datatype, EvaluationContext, Optional)} ) when the Individual Decision request is evaluated against an AttributeDesignator or AttributeSelector with ContextSelectorId.
	 * @param mdpContext context of a Multiple Decision request evaluation, will be passed on as {@code mdpContext} argument of each AttributeProvider ( {@link NamedAttributeProvider#get(AttributeFqn, Datatype, EvaluationContext, Optional)} ) when Individual Decision requests are evaluated.
	 */
	public void beginIndividualDecisionRequest(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException {
		for (final NamedAttributeProvider provider : this.individualReqBeginners)
		{
			provider.beginIndividualDecisionRequest(context, mdpContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		close(this.closeableProviders);
	}
}
