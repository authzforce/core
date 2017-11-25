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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Optional;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.DesignatedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * AttributeProvider that tries to resolve attributes in current request context first, else delegates to {@link DesignatedAttributeProvider}s.
 *
 * @version $Id: $
 */
public class ModularAttributeProvider implements AttributeProvider
{

	private static final IndeterminateEvaluationException INDETERMINATE_EXCEPTION_NO_VALUE_FROM_ATTRIBUTE_PROVIDERS = new IndeterminateEvaluationException(
			"No value found by any attribute provider module", XacmlStatusCode.PROCESSING_ERROR.value());

	private interface IssuedToNonIssuedAttributeCopyMode
	{
		void process(AttributeFqn AttributeFqn, AttributeBag<?> result, EvaluationContext context);
	}

	private static final IssuedToNonIssuedAttributeCopyMode ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_ENABLED_MODE = new IssuedToNonIssuedAttributeCopyMode()
	{

		@Override
		public void process(final AttributeFqn AttributeFqn, final AttributeBag<?> result, final EvaluationContext context)
		{
			if (!AttributeFqn.getIssuer().isPresent())
			{
				// Attribute already without Issuer -> nothing to copy
				return;
			}
			/*
			 * Attribute with Issuer -> make Issuer-less copy and put same result in context for match by Issuer-less AttributeDesignator
			 */
			final AttributeFqn issuerLessAttributeFqn = AttributeFqns.newInstance(AttributeFqn.getCategory(), Optional.empty(), AttributeFqn.getId());

			/*
			 * Cache the attribute value(s) for the issuer-less attribute in context in case there is a matching Issuer-less AttributeDesignator to evaluate
			 */
			context.putNamedAttributeValueIfAbsent(issuerLessAttributeFqn, result);
			LOGGER.debug("strictAttributeIssuerMatch=false -> Cached values of attribute {}, type={}, derived, by removing Issuer, from attribute {} provided by AttributeProvider module: values= {}",
					AttributeFqn, result.getElementDatatype(), AttributeFqn, result);
		}

	};

	private static final IssuedToNonIssuedAttributeCopyMode ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_DISABLED_MODE = new IssuedToNonIssuedAttributeCopyMode()
	{

		@Override
		public void process(final AttributeFqn AttributeFqn, final AttributeBag<?> result, final EvaluationContext context)
		{
			// do not copy the result to any Issuer-less attribute
		}

	};

	private static final Logger LOGGER = LoggerFactory.getLogger(ModularAttributeProvider.class);

	/*
	 * AttributeDesignator Provider modules by supported/provided attribute ID (global ID: category, issuer, AttributeId)
	 */
	private final ImmutableListMultimap<AttributeFqn, DesignatedAttributeProvider> designatorModsByAttrId;

	private final IssuedToNonIssuedAttributeCopyMode issuedToNonIssuedAttributeCopyMode;

	protected ModularAttributeProvider(final ImmutableListMultimap<AttributeFqn, DesignatedAttributeProvider> attributeProviderModulesByAttributeId,
			final Set<AttributeDesignatorType> selectedAttributeSupport, final boolean strictAttributeIssuerMatch)
	{
		assert attributeProviderModulesByAttributeId != null;

		if (selectedAttributeSupport == null)
		{
			designatorModsByAttrId = attributeProviderModulesByAttributeId;
		}
		else
		{
			final ListMultimap<AttributeFqn, DesignatedAttributeProvider> mutableModsByAttrIdMap = ArrayListMultimap.create(selectedAttributeSupport.size(), 1);
			for (final AttributeDesignatorType requiredAttr : selectedAttributeSupport)
			{
				final AttributeFqn requiredAttrGUID = AttributeFqns.newInstance(requiredAttr);
				final ImmutableList<DesignatedAttributeProvider> requiredAttrProviderMods = attributeProviderModulesByAttributeId.get(requiredAttrGUID);
				/*
				 * According to doc, a non-null empty list is returned if no mappings
				 */
				assert requiredAttrProviderMods != null;
				/*
				 * Empty requiredAttrProviderMod means it should be provided by the request context (in the initial request from PEP)
				 */
				if (!requiredAttrProviderMods.isEmpty())
				{

					mutableModsByAttrIdMap.putAll(requiredAttrGUID, requiredAttrProviderMods);
				}
			}

			designatorModsByAttrId = ImmutableListMultimap.copyOf(mutableModsByAttrIdMap);
		}

		this.issuedToNonIssuedAttributeCopyMode = strictAttributeIssuerMatch ? ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_DISABLED_MODE : ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_ENABLED_MODE;
	}

	/**
	 * Get instance of modular Attribute Provider that tries to find attribute values in evaluation context, then, if not there, query sub-modules providing the requested attribute ID, if any.
	 *
	 * @param attributeProviderModulesByAttributeId
	 *            attribute Provider modules sorted by supported attribute ID; may be null if none
	 * @param selectedAttributeSupport
	 *            (optional) selection of attributes to be supported, i.e. only attributes from this set may be supported/resolved by this attribute Provider; therefore, only the part of
	 *            {@code attributeProviderModulesByAttributeId} matching these attributes are to be used by this Provider.
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
	 *            the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
	 *            the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute SHALL be governed by
	 *            AttributeId and DataType attributes alone."
	 * @return modular attribute provider instance; {@link #EVALUATION_CONTEXT_ONLY_SCOPED_ATTRIBUTE_PROVIDER} iff
	 *         {@code attributeProviderModulesByAttributeId == null || attributeProviderModulesByAttributeId.isEmpty()},
	 */
	public static ModularAttributeProvider getInstance(final ImmutableListMultimap<AttributeFqn, DesignatedAttributeProvider> attributeProviderModulesByAttributeId,
			final Set<AttributeDesignatorType> selectedAttributeSupport, final boolean strictAttributeIssuerMatch)
	{
		if (attributeProviderModulesByAttributeId == null || attributeProviderModulesByAttributeId.isEmpty())
		{
			return EVALUATION_CONTEXT_ONLY_SCOPED_ATTRIBUTE_PROVIDER;
		}

		return new ModularAttributeProvider(attributeProviderModulesByAttributeId, selectedAttributeSupport, strictAttributeIssuerMatch);
	}

	/** {@inheritDoc} */
	@Override
	public final <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn AttributeFqn, final BagDatatype<AV> returnDatatype, final EvaluationContext context)
			throws IndeterminateEvaluationException
	{
		try
		{
			final AttributeBag<AV> contextBag = context.getNamedAttributeValue(AttributeFqn, returnDatatype);
			if (contextBag != null)
			{
				LOGGER.debug("Values of attribute {}, type={} found in evaluation context: {}", AttributeFqn, returnDatatype, contextBag);
				return contextBag;
			}

			// else attribute not found in context, ask the Provider modules, if any
			LOGGER.debug("Requesting attribute {} from Provider modules (by provided attribute ID): {}", AttributeFqn, designatorModsByAttrId);
			final ImmutableList<DesignatedAttributeProvider> attrProviders = designatorModsByAttrId.get(AttributeFqn);
			/*
			 * According to doc, a non-null empty list is returned if no mappings
			 */
			assert attrProviders != null;
			if (attrProviders.isEmpty())
			{
				LOGGER.debug("No value found for required attribute {}, type={} in evaluation context and not supported by any Attribute Provider module", AttributeFqn, returnDatatype);
				throw new IndeterminateEvaluationException("Not in context and no Attribute Provider module supporting requested attribute: " + AttributeFqn, XacmlStatusCode.MISSING_ATTRIBUTE.value());
			}

			AttributeBag<AV> result = null;
			/*
			 * Try all modules supporting this attribute until value found
			 */
			for (final DesignatedAttributeProvider attrProvider : attrProviders)
			{
				result = attrProvider.get(AttributeFqn, returnDatatype, context);
				if (result != null && !result.isEmpty())
				{
					break;
				}
			}

			if (result == null)
			{
				result = Bags.emptyAttributeBag(returnDatatype.getElementType(), INDETERMINATE_EXCEPTION_NO_VALUE_FROM_ATTRIBUTE_PROVIDERS);
			}

			/*
			 * Cache the attribute value(s) in context to avoid waste of time querying the module twice for same attribute
			 */
			context.putNamedAttributeValueIfAbsent(AttributeFqn, result);
			LOGGER.debug("Values of attribute {}, type={} returned by attribute Provider module #{} (cached in context): {}", AttributeFqn, returnDatatype, attrProviders, result);
			issuedToNonIssuedAttributeCopyMode.process(AttributeFqn, result, context);
			return result;
		}
		catch (final IndeterminateEvaluationException e)
		{
			/*
			 * This error does not necessarily matter, it depends on whether the attribute is required, i.e. MustBePresent=true for AttributeDesignator/Selector So we let
			 * AttributeDesignator/Select#evaluate() method log the errors if MustBePresent=true. Here debug level is enough
			 */
			LOGGER.debug("Error finding attribute {}, type={}", AttributeFqn, returnDatatype, e);

			/**
			 * If error occurred, we put the empty value to prevent retry in the same context, which may succeed at another time in the same context, resulting in different value of the same attribute
			 * at different times during evaluation within the same context, therefore inconsistencies. The value(s) must remain constant during the evaluation context, as explained in section 7.3.5
			 * Attribute Retrieval of XACML core spec:
			 * <p>
			 * Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before
			 * it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use 3313 the same bag of values that was initially tested.)
			 * </p>
			 * Therefore, if no value found, we keep it that way until evaluation is done for the current request context.
			 * <p>
			 * We could put the null value to indicate the evaluation error, instead of an empty Bag, but it would make the result of the code used at the start of this method ambiguous/confusing:
			 * <p>
			 * <code>
			 * final Bag<T> contextBag = context.getAttributeDesignatorResult(AttributeFqn,...)
			 * </code>
			 * </p>
			 * <p>
			 * Indeed, contextBag could be null for one of these two reasons:
			 * <ol>
			 * <li>The attribute ('AttributeFqn') has never been requested in this context;
			 * <li>It has been requested before in this context but could not be found: error occurred (IndeterminateEvaluationException)</li>
			 * </ol>
			 * To avoid this confusion, we put an empty Bag (with some error info saying why this is empty).
			 * </p>
			 */
			final AttributeBag<AV> result = Bags.emptyAttributeBag(returnDatatype.getElementType(), e);
			/*
			 * NOTE: It might happen - e.g. in conformance test IIB033 (Request's resource-id attribute datatype is different from datatype used in Policy) - that
			 * context.getAttributeDesignatorResult(AttributeFqn, bagDatatype) threw IndeterminateEvaluationException although a value for 'AttributeFqn' exists in context, because the existing
			 * datatype is different from requested 'bagDatatype'. In this case, the call below will return false (the value should not be overridden). We don't care about the result; what matters is
			 * that the value is set to an empty bag if there was no value.
			 */
			context.putNamedAttributeValueIfAbsent(AttributeFqn, result);
			return result;
		}
	}

	/**
	 * Attribute Provider based only on the evaluation context, i.e. it does not used any extra attribute provider module to get attribute values if not found in the context
	 */
	public static final ModularAttributeProvider EVALUATION_CONTEXT_ONLY_SCOPED_ATTRIBUTE_PROVIDER = new ModularAttributeProvider(ImmutableListMultimap.of(), null, true);
}
