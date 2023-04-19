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

import com.google.common.collect.ImmutableMap;
import net.sf.saxon.s9api.XdmNode;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.AttributeSelectorExpression;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * An {@link EvaluationContext} associated to an XACML Individual Decision Request, i.e. for evaluation to a single authorization decision Result (see Multiple Decision Profile spec for more
 * information on Individual Decision Request as opposed to Multiple Decision Request). This is the default {@link EvaluationContext} implementation used by the PDP engine. It is also meant to be used
 * particularly in unit tests of PDP extensions depending on evaluation context, e.g. {@link NamedAttributeProvider}, {@link DecisionCache}, etc.
 *
 *
 * @version $Id: $
 */
public final class IndividualDecisionRequestContext extends BaseEvaluationContext
{
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

	/*
	 * Corresponds to Attributes/Content (by attribute category) marshalled to XPath data model for XPath evaluation: AttributeSelector evaluation, XPath-based functions, etc. This may be empty if no
	 * Content in Request or no feature requiring XPath evaluation against Content is supported/enabled.
	 */
	// Not null
	private final ImmutableMap<String, XdmNode> extraContentsByAttributeCategory;

	/*
	 * AttributeSelector evaluation results. Not null
	 */
	private final UpdatableMap<AttributeSelectorId, Bag<?>> attributeSelectorResults;

	/**
	 * Constructs a new <code>IndividualDecisionRequestContext</code> based on the given request attributes and extra contents with support for XPath evaluation against Content element in Attributes
	 *
	 * @param namedAttributeMap
	 *            updatable named attribute map (attribute key and value pairs) from the original Request; null iff none. An attribute key is a global ID based on attribute category,issuer,id. An
	 *            attribute value is a bag of primitive values.
	 * @param extraContentsByCategory
	 *            extra contents by attribute category (equivalent to XACML Attributes/Content elements); null iff no Content in the attribute category.
	 * @param returnApplicablePolicyIdList
	 *            true iff list of IDs of policies matched during evaluation must be returned
	 */
	public IndividualDecisionRequestContext(final Map<AttributeFqn, AttributeBag<?>> namedAttributeMap, final ImmutableMap<String, XdmNode> extraContentsByCategory, final boolean returnApplicablePolicyIdList, Optional<Instant> requestTimestamp)
	{
		super(namedAttributeMap, returnApplicablePolicyIdList, requestTimestamp);
		if (extraContentsByCategory == null)
		{
			this.extraContentsByAttributeCategory = ImmutableMap.of();
			this.attributeSelectorResults = UpdatableCollections.emptyMap();
		} else
		{
			this.extraContentsByAttributeCategory = extraContentsByCategory;
			this.attributeSelectorResults = UpdatableCollections.newUpdatableMap();
		}
	}

	/** {@inheritDoc} */
	@Override
	public XdmNode getAttributesContent(final String category)
	{
		return extraContentsByAttributeCategory.get(category);
	}

	/** {@inheritDoc} */
	@Override
	public <AV extends AttributeValue> Bag<AV> getAttributeSelectorResult(final AttributeSelectorExpression<AV> attributeSelector) throws IndeterminateEvaluationException
	{
		final Bag<?> bagResult = attributeSelectorResults.get(attributeSelector.getAttributeSelectorId());
		if (bagResult == null)
		{
			return null;
		}

		final Datatype<Bag<AV>> expectedBagDatatype = attributeSelector.getReturnType();
		final Optional<? extends Datatype<?>> expectedElementType = expectedBagDatatype.getTypeParameter();
		assert expectedElementType.isPresent();
		final Datatype<?> expectedElementDatatype = expectedElementType.get();
		if (!bagResult.getElementDatatype().equals(expectedElementDatatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ")of AttributeSelector " + attributeSelector.getAttributeSelectorId()
					+ " in context is different from actually expected/requested (" + expectedElementDatatype
					+ "). May be caused by use of same AttributeSelector Category/Path/ContextSelectorId with different Datatypes in different in different policy elements, which is not allowed.",
					XacmlStatusCode.SYNTAX_ERROR.value());
		}

		/*
		 * If datatype classes match, bagResult should have the same type as datatypeClass.
		 */
		final Bag<AV> result = expectedBagDatatype.cast(bagResult);
		this.listeners.forEach((lt, l) -> l.attributeSelectorResultConsumed(attributeSelector, result));
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public <AV extends AttributeValue> boolean putAttributeSelectorResultIfAbsent(final AttributeSelectorExpression<AV> attributeSelector, final Bag<AV> result) throws IndeterminateEvaluationException
	{
		final AttributeSelectorId attSelectorId = attributeSelector.getAttributeSelectorId();
		if (attributeSelectorResults.putIfAbsent(attSelectorId, result) != null)
		{
			LOGGER.error("Attempt to override value of AttributeSelector {} already set in evaluation context. Overriding value: {}", attSelectorId, result);
			return false;
		}

		for (final Listener listener : this.listeners.values())
		{
			final Optional<AttributeFqn> optionalContextSelectorFQN = attributeSelector.getContextSelectorFQN();
			final Optional<AttributeBag<XPathValue>> optContextSelectorValue;
			if(optionalContextSelectorFQN.isPresent()) {
				final AttributeBag<XPathValue> contextSelectorValue = getNamedAttributeValue(optionalContextSelectorFQN.get(), StandardDatatypes.XPATH);
				if(contextSelectorValue == null) {
					throw new IndeterminateEvaluationException("Error processing ContextSelectorId of " + attributeSelector +": can't resolve its AttributeValue", XacmlStatusCode.PROCESSING_ERROR.value());
				}

				optContextSelectorValue = Optional.of(contextSelectorValue);
			} else {
				optContextSelectorValue = Optional.empty();
			}

			listener.attributeSelectorResultProduced(attributeSelector, optContextSelectorValue, result);
		}

		return true;
	}
}