/*
 * Copyright 2012-2024 THALES.
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

import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * AttributeProvider stub used as common base for ModularMultiNamedAttributeProvider and EvaluationContextBasedSingleNamedAttributeProvider.
 *
 * @version $Id: $
 */
public abstract class EvaluationContextBasedNamedAttributeProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationContextBasedNamedAttributeProvider.class);

	/**
	 * Delegate AttributeProvider combinining one or more sub-modules (PIP extensions), either mono-module (using a single PIP extension) or composite (combining multiple PIPs)
	 * @param <AV> type of AttributeValue(s) returned by this provider
	 */
	@FunctionalInterface
	protected interface DelegateAttributeProvider<AV extends AttributeValue> {

		/**
		 * Retrieve/resolve an attribute
		 * @param attributeFqn attribute name
		 * @param datatype attribute datatype
		 * @param context request evaluation context
		 * @param mdpContext optional Multiple Decision context when the Multiple Decision Profile is used (shared by all Individual Decision requests of the same Multiple Decision request)
		 * @return attribute values (bag)
		 * @throws IndeterminateEvaluationException error resolving the attribute
		 */
		AttributeBag<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;
	}

	/**
	 * Creates the proper delegate combining provider based on input sub-modules (PIP extensions)
	 * @param composedProviders composed AttributeProvider (aka PIP) sub-modules
	 * @return combining delegate
	 * @param <AV> type of AttributeValue(s) returned by the new instance
	 */
	protected static <AV extends AttributeValue> DelegateAttributeProvider<AV> newDelegate(final List<NamedAttributeProvider> composedProviders) {
		assert composedProviders != null && !composedProviders.isEmpty();
		if(composedProviders.size() == 1) {
			final NamedAttributeProvider subProvider = composedProviders.get(0);
			return  (name, type, ctx, mdpCtx) ->
			{
				LOGGER.debug("Requesting attribute {} from Provider module: {}", name, subProvider);
				final AttributeBag<AV> result = subProvider.get(name, type, ctx, mdpCtx);
				LOGGER.debug("Values of attribute {}, type={} returned by Attribute Provider module #{}: {}", name, type, subProvider, result);
				return result;
			};
		}

		// composedProviders.size > 1
		return (name, type, ctx, mdpCtx) -> {
			/*
			 * Query all sub-providers in order
			 */
			final Collection<AV> values = new ArrayList<>();
			for (final NamedAttributeProvider subProvider : composedProviders)
			{
				LOGGER.debug("Requesting attribute {} from Provider module: {}", name, subProvider);
				final AttributeBag<AV> result = subProvider.get(name, type, ctx, mdpCtx);
				LOGGER.debug("Values of attribute {}, type={} returned by Attribute Provider module #{}: {}", name, type, subProvider, result);
				if (result != null)
				{
					values.addAll(result.elements());
				}
			}

			return Bags.newAttributeBag( type, values, AttributeSources.PDP);
		};
	}

	private interface IssuedToNonIssuedAttributeCopyMode
	{
		void process(AttributeFqn attributeFqn, AttributeBag<?> result, EvaluationContext context);
	}

	private static final IssuedToNonIssuedAttributeCopyMode ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_ENABLED_MODE = (attributeFqn, result, context) -> {
		if (attributeFqn.getIssuer().isEmpty())
		{
			// Attribute already without Issuer -> nothing to copy
			return;
		}
		/*
		 * Attribute with Issuer -> make Issuer-less copy and put same result in context for match by Issuer-less AttributeDesignator
		 */
		final AttributeFqn issuerLessAttributeFqn = AttributeFqns.newInstance(attributeFqn.getCategory(), Optional.empty(), attributeFqn.getId());

		/*
		 * Cache the attribute value(s) for the issuer-less attribute in context in case there is a matching Issuer-less AttributeDesignator to evaluate
		 */
		context.putNamedAttributeValue(issuerLessAttributeFqn, result, true);
		LOGGER.debug("strictAttributeIssuerMatch=false -> Cached values of attribute {}, type={}, derived, by removing Issuer, from attribute {} provided by AttributeProvider module: values= {}",
		        attributeFqn, result.getElementDatatype(), attributeFqn, result);
	};

	private static final IssuedToNonIssuedAttributeCopyMode ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_DISABLED_MODE = (attributeFqn, result, context) -> {
		// do not copy the result to any Issuer-less attribute
	};

	private static final IndeterminateEvaluationException INDETERMINATE_EXCEPTION_NULL_RESULT_WITHOUT_CAUSE = new IndeterminateEvaluationException(
			"Null result returned by attribute provider(s) without throwing any error", XacmlStatusCode.PROCESSING_ERROR.value());

	private final IssuedToNonIssuedAttributeCopyMode issuedToNonIssuedAttributeCopyMode;

	/**
	 * Constructor
	 * @param strictAttributeIssuerMatch true iff the returned attribute must match the Issuer in the AttributeDesignator/AttributeSelector expression exactly
	 */
	protected EvaluationContextBasedNamedAttributeProvider(final boolean strictAttributeIssuerMatch)
	{
		this.issuedToNonIssuedAttributeCopyMode = strictAttributeIssuerMatch ? ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_DISABLED_MODE : ISSUED_TO_NON_ISSUED_ATTRIBUTE_COPY_ENABLED_MODE;
	}

	/**
	 * Add attribute values to the request evaluation context
	 * @param attributeFqn attribute name
	 * @param vals attribute values (bag)
	 * @param context request evaluation context
	 * @param <AV> type of AttributeValue(s) added to the evaluation context
	 */
	protected final <AV extends AttributeValue> void addAttributeValuesToContext(final AttributeFqn attributeFqn, final AttributeBag<AV> vals, final EvaluationContext context)
	{
		/*
		 * Cache the attribute value(s) in the evaluation context to avoid waste of time querying the module twice for the same attribute
		 */
		context.putNamedAttributeValue(attributeFqn, vals, true);
		issuedToNonIssuedAttributeCopyMode.process(attributeFqn, vals, context);
	}

	/**
	 * Retrieve/resolve the values of the attribute with given name and datatype
	 * @param attributeFqn attribute name
	 * @param datatype attribute datatype
	 * @param context request evaluation context
	 * @param mdpContext optional Multiple Decision context when the Multiple Decision Profile is used (shared by all Individual Decision requests of the same Multiple Decision request)
	 * @param delegate delegate attribute provider
	 * @return attribute values (bag)
	 * @param <AV> type of AttributeValue(s) added to the evaluation context
	 */
	protected final <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype, final EvaluationContext context,  final Optional<EvaluationContext> mdpContext, final DelegateAttributeProvider<AV> delegate)
	{
		assert context != null && delegate != null;
		try
		{
			final AttributeBag<AV> contextBag = context.getNamedAttributeValue(attributeFqn, datatype);
			if (contextBag != null)
			{
				LOGGER.debug("Values of attribute {}, type={} found in evaluation context: {}", attributeFqn, datatype, contextBag);
				return contextBag;
			}

			final AttributeBag<AV> result = delegate.get(attributeFqn, datatype, context, mdpContext);
			/*
			Null result indicates an error and should have been avoided by throwing IndeterminateEvaluation Exception instead
			 */
			final AttributeBag<AV>	finalResult =  result == null? Bags.emptyAttributeBag(datatype, INDETERMINATE_EXCEPTION_NULL_RESULT_WITHOUT_CAUSE): result;
			addAttributeValuesToContext(attributeFqn, finalResult, context);
			LOGGER.debug("Values of attribute {}, type={} (empty bag cached in context): {}", attributeFqn, datatype, finalResult);
			return finalResult;
		}
		catch (final IndeterminateEvaluationException e)
		{
			/*
			 * This error does not necessarily matter, it depends on whether the attribute is required, i.e. MustBePresent=true for AttributeDesignator/Selector So we let
			 * AttributeDesignator/Select#evaluate() method log the errors if MustBePresent=true. Here debug level is enough
			 */
			LOGGER.debug("Error finding attribute {}, type={}", attributeFqn, datatype, e);

			/*
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
			 * <li>The attribute ('attributeFqn') has never been requested in this context;
			 * <li>It has been requested before in this context but could not be found: error occurred (IndeterminateEvaluationException)</li>
			 * </ol>
			 * To avoid this confusion, we put an empty Bag (with some error info saying why this is empty).
			 * </p>
			 */
			final AttributeBag<AV> result = Bags.emptyAttributeBag(datatype, e);
			/*
			 * NOTE: It might happen - e.g. in conformance test IIB033 (Request's resource-id attribute datatype is different from datatype used in Policy) - that
			 * context.getAttributeDesignatorResult(AttributeFqn, bagDatatype) threw IndeterminateEvaluationException although a value for 'attributeFqn' exists in context, because the existing
			 * datatype is different from requested 'bagDatatype'. In this case, the call below will return false (the value should not be overridden). We don't care about the result; what matters is
			 * that the value is set to an empty bag if there was no value.
			 */
			addAttributeValuesToContext(attributeFqn, result, context);
			return result;
		}
		catch (final UnsupportedOperationException e)
		{
			/*
			 * Should not happen, this is highly unexpected and should be considered a fatal error (it means one of the other AttributeProviders called in getFromOtherProviders() does not respect its contract)
			 */
			throw new RuntimeException("Inconsistent AttributeProvider: throwing UnsupportedOperationException for an attribute (name=" + attributeFqn + ", type=" + datatype
			        + ") that should be supported according to the provider's contract (cf. NamedAttributeProvider#getProvidedAttributes() ) ", e);
		}
	}

}
