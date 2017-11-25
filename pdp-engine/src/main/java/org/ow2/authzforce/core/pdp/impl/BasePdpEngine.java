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

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableMap;
import org.ow2.authzforce.core.pdp.api.expression.AttributeSelectorExpression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluators;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 * This is the core XACML PDP engine implementation.
 *
 * @version $Id: $
 */
public final class BasePdpEngine implements CloseablePdpEngine
{
	private static final String NULL_STD_ENV_ATTRIBUTE_SOURCE_ARG = "Undefined stdEnvAttributeSource arg (source of standard curent-* environment attributes)";

	private static final IllegalArgumentException NULL_REQUEST_ARGUMENT_EXCEPTION = new IllegalArgumentException("No input Decision Request");

	private interface StandardEnvironmentAttributeIssuer
	{
		Map<AttributeFqn, AttributeBag<?>> get();
	}

	private static final StandardEnvironmentAttributeIssuer NULL_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer()
	{

		@Override
		public Map<AttributeFqn, AttributeBag<?>> get()
		{
			return null;
		}
	};

	private static final StandardEnvironmentAttributeIssuer DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer()
	{

		@Override
		public Map<AttributeFqn, AttributeBag<?>> get()
		{
			/*
			 * Set the standard current date/time attribute according to XACML core spec:
			 * "This identifier indicates the current time at the context handler. In practice it is the time at which the request context was created." (§B.7). XACML standard (§10.2.5) says: "If
			 * values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler".
			 */
			// current datetime in default timezone
			final DateTimeValue currentDateTimeValue = new DateTimeValue(new GregorianCalendar());
			return HashCollections.<AttributeFqn, AttributeBag<?>> newImmutableMap(
					// current date-time
					StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN(),
					Bags.singletonAttributeBag(StandardDatatypes.DATETIME, currentDateTimeValue, AttributeSources.PDP),
					// current date
					StandardEnvironmentAttribute.CURRENT_DATE.getFQN(),
					Bags.singletonAttributeBag(StandardDatatypes.DATE, DateValue.getInstance((XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone()), AttributeSources.PDP),
					// current time
					StandardEnvironmentAttribute.CURRENT_TIME.getFQN(),
					Bags.singletonAttributeBag(StandardDatatypes.TIME, TimeValue.getInstance((XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone()), AttributeSources.PDP));
		}
	};

	private static class NonIssuedLikeIssuedAttributeHandlingRequestBuilder implements DecisionRequestBuilder<ImmutableDecisionRequest>
	{
		private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;
		private final Map<String, XdmNode> extraContentsByCategory;

		private NonIssuedLikeIssuedAttributeHandlingRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
		{
			namedAttributes = expectedTotalNumOfAttributes < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedTotalNumOfAttributes);
			extraContentsByCategory = expectedNumOfAttributeCategories < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedNumOfAttributeCategories);
		}

		@Override
		public Bag<?> putNamedAttributeIfAbsent(final AttributeFqn attributeId, final AttributeBag<?> attributeValues)
		{
			return namedAttributes.putIfAbsent(attributeId, attributeValues);
		}

		@Override
		public final XdmNode putContentIfAbsent(final String category, final XdmNode content)
		{
			return extraContentsByCategory.putIfAbsent(category, content);
		}

		@Override
		public final ImmutableDecisionRequest build(final boolean returnApplicablePolicies)
		{
			return ImmutableDecisionRequest.getInstance(namedAttributes, extraContentsByCategory, returnApplicablePolicies);
		}

		@Override
		public final void reset()
		{
			namedAttributes.clear();
			extraContentsByCategory.clear();
		}
	}

	private static final class IssuedToNonIssuedAttributeCopyingRequestBuilder extends NonIssuedLikeIssuedAttributeHandlingRequestBuilder
	{
		private IssuedToNonIssuedAttributeCopyingRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
		{
			/*
			 * Since copying attributes with non-null Issuer to equivalent attributes but without Issuer, we'll get at most twice as many attributes as the ones put by the caller
			 */
			super(expectedNumOfAttributeCategories, 2 * expectedTotalNumOfAttributes);
		}

		@Override
		public Bag<?> putNamedAttributeIfAbsent(final AttributeFqn AttributeFqn, final AttributeBag<?> attributeValues)
		{
			/*
			 * Put the non-issued version of the attribute first
			 */
			final AttributeFqn nonAttributeFqn = AttributeFqns.newInstance(AttributeFqn.getCategory(), Optional.empty(), AttributeFqn.getId());
			super.putNamedAttributeIfAbsent(nonAttributeFqn, attributeValues);
			return super.putNamedAttributeIfAbsent(AttributeFqn, attributeValues);
		}
	}

	/**
	 * An {@link EvaluationContext} associated to an XACML Individual Decision Request, i.e. for evaluation to a single authorization decision Result (see Multiple Decision Profile spec for more
	 * information on Individual Decision Request as opposed to Multiple Decision Request).
	 *
	 * @version $Id: $
	 */
	private static final class IndividualDecisionRequestContext implements EvaluationContext
	{
		/**
		 * Logger used for all classes
		 */
		private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

		private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;

		/*
		 * Corresponds to Attributes/Content (by attribute category) marshalled to XPath data model for XPath evaluation: AttributeSelector evaluation, XPath-based functions, etc. This may be empty if
		 * no Content in Request or no feature requiring XPath evaluation against Content is supported/enabled.
		 */
		// Not null
		private final Map<String, XdmNode> extraContentsByAttributeCategory;

		/*
		 * AttributeSelector evaluation results. Not null
		 */
		private final UpdatableMap<AttributeSelectorId, Bag<?>> attributeSelectorResults;

		private final Map<String, Value> varValsById = HashCollections.newMutableMap();

		private final Map<String, Object> mutableProperties = HashCollections.newMutableMap();

		private final boolean returnApplicablePolicyIdList;

		private final ClassToInstanceMap<Listener> listeners = MutableClassToInstanceMap.create();

		/**
		 * Constructs a new <code>IndividualDecisionRequestContext</code> based on the given request attributes and extra contents with support for XPath evaluation against Content element in
		 * Attributes
		 *
		 * @param namedAttributeMap
		 *            updatable named attribute map (attribute key and value pairs) from the original Request; null iff none. An attribute key is a global ID based on attribute category,issuer,id. An
		 *            attribute value is a bag of primitive values.
		 * @param extraContentsByCategory
		 *            extra contents by attribute category (equivalent to XACML Attributes/Content elements); null iff no Content in the attribute category.
		 * @param returnApplicablePolicyIdList
		 *            true iff list of IDs of policies matched during evaluation must be returned
		 */
		public IndividualDecisionRequestContext(final Map<AttributeFqn, AttributeBag<?>> namedAttributeMap, final Map<String, XdmNode> extraContentsByCategory,
				final boolean returnApplicablePolicyIdList)
		{
			this.namedAttributes = namedAttributeMap == null ? HashCollections.<AttributeFqn, AttributeBag<?>> newUpdatableMap() : HashCollections
					.<AttributeFqn, AttributeBag<?>> newUpdatableMap(namedAttributeMap);
			this.returnApplicablePolicyIdList = returnApplicablePolicyIdList;
			if (extraContentsByCategory == null)
			{
				this.extraContentsByAttributeCategory = Collections.emptyMap();
				this.attributeSelectorResults = UpdatableCollections.emptyMap();
			}
			else
			{
				this.extraContentsByAttributeCategory = extraContentsByCategory;
				this.attributeSelectorResults = UpdatableCollections.newUpdatableMap();
			}
		}

		/** {@inheritDoc} */
		@Override
		public <AV extends AttributeValue> AttributeBag<AV> getNamedAttributeValue(final AttributeFqn AttributeFqn, final BagDatatype<AV> attributeBagDatatype) throws IndeterminateEvaluationException
		{
			final AttributeBag<?> bagResult = namedAttributes.get(AttributeFqn);
			if (bagResult == null)
			{
				return null;
			}

			final Datatype<?> expectedElementDatatype = attributeBagDatatype.getElementType();
			if (!bagResult.getElementDatatype().equals(expectedElementDatatype))
			{
				throw new IndeterminateEvaluationException(
						"Datatype ("
								+ bagResult.getElementDatatype()
								+ ") of AttributeDesignator "
								+ AttributeFqn
								+ " in context is different from expected/requested ("
								+ expectedElementDatatype
								+ "). May be caused by refering to the same Attribute Category/Id/Issuer with different Datatypes in different policy elements and/or attribute providers, which is not allowed.",
						XacmlStatusCode.SYNTAX_ERROR.value());
			}

			/*
			 * If datatype classes match, bagResult should have same type as datatypeClass.
			 */
			final AttributeBag<AV> result = (AttributeBag<AV>) bagResult;
			this.listeners.forEach((lt, l) -> l.namedAttributeValueConsumed(AttributeFqn, result));
			return result;
		}

		@Override
		public boolean putNamedAttributeValueIfAbsent(final AttributeFqn AttributeFqn, final AttributeBag<?> result)
		{
			final Bag<?> duplicate = namedAttributes.putIfAbsent(AttributeFqn, result);
			if (duplicate != null)
			{
				/*
				 * This should never happen, as getAttributeDesignatorResult() should have been called first (for same id) and returned this oldResult, and no further call to
				 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not support setting a different result for same id (but different datatype URI/datatype class) in the same
				 * context
				 */
				LOGGER.warn("Attempt to override value of AttributeDesignator {} already set in evaluation context. Overriding value: {}", AttributeFqn, result);
				return false;
			}

			this.listeners.forEach((lt, l) -> l.namedAttributeValueProduced(AttributeFqn, result));
			/*
			 * Attribute value cannot change during evaluation context, so if old value already there, put it back
			 */
			return true;
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
			final Datatype<?> expectedElementDatatype = expectedBagDatatype.getTypeParameter().get();
			if (!bagResult.getElementDatatype().equals(expectedElementDatatype))
			{
				throw new IndeterminateEvaluationException(
						"Datatype ("
								+ bagResult.getElementDatatype()
								+ ")of AttributeSelector "
								+ attributeSelector.getAttributeSelectorId()
								+ " in context is different from actually expected/requested ("
								+ expectedElementDatatype
								+ "). May be caused by use of same AttributeSelector Category/Path/ContextSelectorId with different Datatypes in different in different policy elements, which is not allowed.",
						XacmlStatusCode.SYNTAX_ERROR.value());
			}

			/*
			 * If datatype classes match, bagResult should has same type as datatypeClass.
			 */
			final Bag<AV> result = expectedBagDatatype.cast(bagResult);
			this.listeners.forEach((lt, l) -> l.attributeSelectorResultConsumed(attributeSelector, result));
			return result;
		}

		/** {@inheritDoc} */
		@Override
		public <AV extends AttributeValue> boolean putAttributeSelectorResultIfAbsent(final AttributeSelectorExpression<AV> attributeSelector, final Bag<AV> result)
				throws IndeterminateEvaluationException
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
				final Optional<AttributeBag<XPathValue>> contextSelectorValue = optionalContextSelectorFQN.isPresent() ? Optional.of(getNamedAttributeValue(optionalContextSelectorFQN.get(),
						StandardDatatypes.XPATH.getBagDatatype())) : Optional.empty();
				listener.attributeSelectorResultProduced(attributeSelector, contextSelectorValue, result);
			}

			return true;
		}

		/** {@inheritDoc} */
		@Override
		public <V extends Value> V getVariableValue(final String variableId, final Datatype<V> expectedDatatype) throws IndeterminateEvaluationException
		{
			final Value val = varValsById.get(variableId);
			if (val == null)
			{
				return null;
			}

			try
			{
				return expectedDatatype.cast(val);
			}
			catch (final ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Datatype of variable '" + variableId + "' in context does not match expected datatype: " + expectedDatatype,
						XacmlStatusCode.PROCESSING_ERROR.value(), e);
			}
		}

		/** {@inheritDoc} */
		@Override
		public boolean putVariableIfAbsent(final String variableId, final Value value)
		{
			if (varValsById.putIfAbsent(variableId, value) != null)
			{
				LOGGER.error("Attempt to override value of Variable '{}' already set in evaluation context. Overriding value: {}", variableId, value);
				return false;
			}

			return true;
		}

		/** {@inheritDoc} */
		@Override
		public Value removeVariable(final String variableId)
		{
			return varValsById.remove(variableId);
		}

		/** {@inheritDoc} */
		@Override
		public Object getOther(final String key)
		{
			return mutableProperties.get(key);
		}

		/** {@inheritDoc} */
		@Override
		public boolean containsKey(final String key)
		{
			return mutableProperties.containsKey(key);
		}

		/** {@inheritDoc} */
		@Override
		public void putOther(final String key, final Object val)
		{
			mutableProperties.put(key, val);
		}

		/** {@inheritDoc} */
		@Override
		public Object remove(final String key)
		{
			return mutableProperties.remove(key);
		}

		/** {@inheritDoc} */
		@Override
		public Iterator<Entry<AttributeFqn, AttributeBag<?>>> getNamedAttributes()
		{
			final Set<Entry<AttributeFqn, AttributeBag<?>>> immutableAttributeSet = Collections.unmodifiableSet(namedAttributes.entrySet());
			return immutableAttributeSet.iterator();
		}

		@Override
		public boolean isApplicablePolicyIdListRequested()
		{
			return returnApplicablePolicyIdList;
		}

		@Override
		public <L extends Listener> L putListener(final Class<L> listenerType, final L listener)
		{
			return this.listeners.putInstance(listenerType, listener);
		}

		@Override
		public <L extends Listener> L getListener(final Class<L> listenerType)
		{
			return this.listeners.getInstance(listenerType);
		}
	}

	/**
	 * Individual decision request evaluator
	 *
	 * @version $Id: $
	 */
	private static abstract class IndividualDecisionRequestEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestEvaluator.class);

		private interface RequestAndPdpIssuedNamedAttributesMerger
		{
			/**
			 * Return an updatable map after merging {@code pdpIssuedAttributes} and {@code requestAttributes} or one of each into it, depending on the implementation
			 * 
			 * @param pdpIssuedAttributes
			 * @param requestAttributes
			 * @return updatable map resulting from merger, or null if nothing merged
			 */
			Map<AttributeFqn, AttributeBag<?>> merge(final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes, final Map<AttributeFqn, AttributeBag<?>> requestAttributes);
		}

		private static final IndeterminateEvaluationException newReqMissingStdEnvAttrException(final AttributeFqn attrGUID)
		{
			return new IndeterminateEvaluationException("The standard environment attribute ( " + attrGUID
					+ " ) is not present in the REQUEST although at least one of the others is! (PDP standardEnvironmentAttributeSource = REQUEST_ELSE_PDP.)",
					XacmlStatusCode.MISSING_ATTRIBUTE.value());
		}

		private static final Map<AttributeFqn, AttributeBag<?>> STD_ENV_RESET_MAP = HashCollections.<AttributeFqn, AttributeBag<?>> newImmutableMap(
				StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN(),
				Bags.emptyAttributeBag(StandardDatatypes.DATETIME, newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN())),
				StandardEnvironmentAttribute.CURRENT_DATE.getFQN(),
				Bags.emptyAttributeBag(StandardDatatypes.DATE, newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_DATE.getFQN())),
				StandardEnvironmentAttribute.CURRENT_TIME.getFQN(),
				Bags.emptyAttributeBag(StandardDatatypes.TIME, newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_TIME.getFQN())));

		private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_OVERRIDES_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
		{

			@Override
			public Map<AttributeFqn, AttributeBag<?>> merge(final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes, final Map<AttributeFqn, AttributeBag<?>> requestAttributes)
			{
				/*
				 * Request attribute values override PDP issued ones. Do not modify pdpIssuedAttributes directly as this may be used for other requests (Multiple Decision Profile) as well. so we must
				 * not modify it but clone it before individual decision request processing.
				 */
				if (pdpIssuedAttributes == null)
				{
					return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
				}

				// pdpIssuedAttributes != null
				if (requestAttributes == null)
				{
					return HashCollections.newUpdatableMap(pdpIssuedAttributes);
				}
				// requestAttributes != null

				/**
				 * 
				 * XACML standard (§10.2.5) says: "If values for these [the standard environment attributes, i.e. current-time, current-date, current-dateTime] attributes are not present in the
				 * decision request, then their values MUST be supplied by the context handler ". In our case, "context handler" means the PDP. In other words, the attribute values come from request
				 * by default, or from the PDP if (and *only if* in this case) they are not set in the request. More precisely, if any of these standard environment attributes is provided in the
				 * request, none of the PDP values is used, even if some policy requires one that is missing from the request. Indeed, this is to avoid such case when the decision request specifies at
				 * least one date/time attribute, e.g. current-time, but not all of them, e.g. not current-dateTime, and the policy requires both the one(s) provided and the one(s) not provided. In
				 * this case, if the PDP provides its own value(s) for the missing attributes (e.g. current-dateTime), this may cause some inconsistencies since we end up having date/time attributes
				 * coming from two different sources/environments (current-time and current-dateTime for instance).
				 */
				if (requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN()) || requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_DATE.getFQN())
						|| requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_TIME.getFQN()))
				{
					/*
					 * Request has at least one standard env attribute -> make sure all PDP values are ignored (overridden by STD_ENV_RESET_MAP no matter whether requestAttributes contains all of them
					 * or not)
					 */
					// mappings in order of increasing priority
					return HashCollections.newUpdatableMap(pdpIssuedAttributes, STD_ENV_RESET_MAP, requestAttributes);
				}

				// mappings in order of increasing priority
				return HashCollections.newUpdatableMap(pdpIssuedAttributes, requestAttributes);
			}

		};

		private static final RequestAndPdpIssuedNamedAttributesMerger PDP_OVERRIDES_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
		{

			@Override
			public Map<AttributeFqn, AttributeBag<?>> merge(final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes, final Map<AttributeFqn, AttributeBag<?>> requestAttributes)
			{

				// PDP issued attribute values override request attribute values
				/*
				 * Do not modify pdpIssuedAttributes directly as this may be used for other requests (Multiple Decision Profile) as well. so we must not modify it but clone it before individual
				 * decision request processing.
				 */
				if (pdpIssuedAttributes == null)
				{
					return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
				}

				// pdpIssuedAttributes != null
				if (requestAttributes == null)
				{
					return HashCollections.newUpdatableMap(pdpIssuedAttributes);
				}
				// requestAttributes != null

				// mappings of pdpIssuedAttributes have priority
				return HashCollections.newUpdatableMap(requestAttributes, pdpIssuedAttributes);

			}

		};

		private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_ONLY_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
		{

			@Override
			public Map<AttributeFqn, AttributeBag<?>> merge(final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes, final Map<AttributeFqn, AttributeBag<?>> requestAttributes)
			{
				// PDP values completely ignored
				return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
			}

		};

		private final RootPolicyEvaluator rootPolicyEvaluator;
		private final RequestAndPdpIssuedNamedAttributesMerger reqAndPdpIssuedAttributesMerger;

		/**
		 * Creates an evaluator
		 *
		 * @param rootPolicyEvaluator
		 *            root policy evaluator that this request evaluator uses to evaluate individual decision request
		 * @param stdEnvAttributeSource
		 *            (mandatory) Defines the source for the standard environment attributes specified in §10.2.5: current-time, current-date and current-dateTime. The options are:
		 *            <ul>
		 *            <li>REQUEST_ELSE_PDP: the default choice, that complies with the XACML standard (§10.2.5): "If values for these attributes are not present in the decision request, then their
		 *            values MUST be supplied by the context handler", in our case, " context handler" means the PDP. In other words, the attribute values come from request by default, or from the PDP
		 *            if (and *only if* in this case) they are not set in the request. Issue: what if the decision request only specifies current-time but not current-dateTime, and the policy requires
		 *            both? Should the PDP provides its own value for current-dateTime? This could cause some inconsistencies since current-time and current-dateTime would come from two different
		 *            sources/environments. With this option, we have a strict interpretation of the spec, i.e. if any of these attribute is not set in the request, the PDP uses its own value instead.
		 *            So BEWARE. Else you have the other options below.</li>
		 *            <li>REQUEST_ONLY: always use the standard environment attribute value from the request, or nothing if the value is not set in the request, in which case this results in
		 *            Indeterminate (missing attribute) if the policy evaluation requires it.</li>
		 *            <li>PDP_ONLY: always use the standard environment attribute values from the PDP. In other words, Request values are simply ignored; PDP values for standard environment attributes
		 *            systematically override the ones from the request. This also guarantees that they are always set (by the PDP). NB: note that the XACML standard (§10.2.5) says: "If values for
		 *            these attributes are not present in the decision request, then their values MUST be supplied by the context handler " but it does NOT say "If AND ONLY IF values..." So this
		 *            option could still be considered XACML compliant in a strict sense.</li>
		 *            </ul>
		 * @throws IllegalArgumentException
		 *             if {@code stdEnvAttributeSource} is null or not supported
		 */
		protected IndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource) throws IllegalArgumentException
		{
			assert rootPolicyEvaluator != null && stdEnvAttributeSource != null;
			this.rootPolicyEvaluator = rootPolicyEvaluator;
			switch (stdEnvAttributeSource)
			{
				case PDP_ONLY:
					/*
					 * PDP_ONLY means the standard environment attribute values come from the PDP only (not the Request), this does not affect other attributes. In other words, only PDP's standard
					 * environment attribute values override.
					 */
					this.reqAndPdpIssuedAttributesMerger = PDP_OVERRIDES_ATTRIBUTES_MERGER;
					break;
				case REQUEST_ONLY:
					this.reqAndPdpIssuedAttributesMerger = REQUEST_ONLY_ATTRIBUTES_MERGER;
					break;
				case REQUEST_ELSE_PDP:
					this.reqAndPdpIssuedAttributesMerger = REQUEST_OVERRIDES_ATTRIBUTES_MERGER;
					break;
				default:
					throw new IllegalArgumentException("Unsupported standardEnvAttributeSource: " + stdEnvAttributeSource + ". Expected: "
							+ Arrays.toString(StandardEnvironmentAttributeSource.values()));
			}
		}

		protected final EvaluationContext newEvaluationContext(final DecisionRequest request, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes)
		{
			final Map<AttributeFqn, AttributeBag<?>> mergedNamedAttributes = reqAndPdpIssuedAttributesMerger.merge(pdpIssuedAttributes, request.getNamedAttributes());
			return new IndividualDecisionRequestContext(mergedNamedAttributes, request.getExtraContentsByCategory(), request.isApplicablePolicyIdListReturned());
		}

		/**
		 * <p>
		 * Evaluate Individual Decision Request in an existing request context
		 * </p>
		 *
		 * @param evalCtx
		 *            existing evaluation context
		 * @return the evaluation result.
		 */
		protected final DecisionResult evaluateReusingContext(final EvaluationContext evalCtx)
		{
			return rootPolicyEvaluator.findAndEvaluate(evalCtx);
		}

		/**
		 * <p>
		 * Evaluate an Individual Decision Request from which a new request context is created to evaluate the request
		 * </p>
		 *
		 * @param request
		 *            a non-null {@link DecisionRequest} object.
		 * @param pdpIssuedAttributes
		 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
		 * @return the evaluation result.
		 */
		protected final DecisionResult evaluateInNewContext(final DecisionRequest request, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes)
		{
			assert request != null;
			LOGGER.debug("Evaluating Individual Decision Request: {}", request);
			final EvaluationContext evalCtx = newEvaluationContext(request, pdpIssuedAttributes);
			return rootPolicyEvaluator.findAndEvaluate(evalCtx);
		}

		/**
		 * <p>
		 * Evaluate multiple Individual Decision Requests with same PDP-issued attribute values (e.g. current date/time) in order to return decision results in internal model.
		 * </p>
		 *
		 * @param individualDecisionRequests
		 *            a {@link java.util.List} of individual decision requests.
		 * @param pdpIssuedAttributes
		 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
		 * @return individual decision request-result pairs, where the list of the requests is the same as {@code individualDecisionRequests}.
		 * @throws IndeterminateEvaluationException
		 *             if an error occurred preventing any request evaluation
		 */
		protected abstract <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
				List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException;

	}

	private static final class NonCachingIndividualDecisionRequestEvaluator extends IndividualDecisionRequestEvaluator
	{
		private static final RuntimeException NULL_INDIVIDUAL_DECISION_REQUEST_EXCEPTION = new RuntimeException(
				"One of the individual decision requests returned by the request filter is invalid (null).");

		private NonCachingIndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource)
		{
			super(rootPolicyEvaluator, stdEnvAttributeSource);
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException
		{
			assert individualDecisionRequests != null;

			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> resultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				if (individualDecisionRequest == null)
				{
					throw NULL_INDIVIDUAL_DECISION_REQUEST_EXCEPTION;
				}

				final DecisionResult decisionResult = evaluateInNewContext(individualDecisionRequest, pdpIssuedAttributes);
				resultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, decisionResult));
			}

			return resultsByRequest;
		}

	}

	private static abstract class CachingIndividualRequestEvaluator extends IndividualDecisionRequestEvaluator
	{

		protected static final IndeterminateEvaluationException INDETERMINATE_EVALUATION_EXCEPTION = new IndeterminateEvaluationException("Internal error in decision cache: null result",
				XacmlStatusCode.PROCESSING_ERROR.value());

		protected final DecisionCache decisionCache;

		private CachingIndividualRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource, final DecisionCache decisionCache)
		{
			super(rootPolicyEvaluator, stdEnvAttributeSource);
			assert decisionCache != null;
			this.decisionCache = decisionCache;
		}
	}

	private static final class IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext extends CachingIndividualRequestEvaluator
	{
		// the logger we'll use for all messages
		private static final Logger _LOGGER = LoggerFactory.getLogger(IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext.class);

		private IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource,
				final DecisionCache decisionCache)
		{
			super(rootPolicyEvaluator, stdEnvAttributeSource, decisionCache);
		}

		@Override
		public <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException
		{
			final Map<INDIVIDUAL_DECISION_REQ_T, DecisionResult> cachedResultsByRequest = decisionCache.getAll(individualDecisionRequests);
			if (cachedResultsByRequest == null)
			{
				// error, return indeterminate result as only result
				_LOGGER.error("Invalid decision cache result: null");
				throw INDETERMINATE_EVALUATION_EXCEPTION;
			}

			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> finalResultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			final Map<INDIVIDUAL_DECISION_REQ_T, DecisionResult> newResultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				final DecisionResult finalResult;
				/*
				 * Check whether there is any decision result in cache for this request
				 */
				final DecisionResult cachedResult = cachedResultsByRequest.get(individualDecisionRequest);
				if (cachedResult == null)
				{
					// result not in cache -> evaluate request
					finalResult = evaluateInNewContext(individualDecisionRequest, pdpIssuedAttributes);
					newResultsByRequest.put(individualDecisionRequest, finalResult);
				}
				else
				{
					finalResult = cachedResult;
				}

				finalResultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, finalResult));
			}

			if (!newResultsByRequest.isEmpty())
			{
				decisionCache.putAll(newResultsByRequest);
			}

			return finalResultsByRequest;
		}

	}

	private static final class IndividualRequestEvaluatorWithCacheUsingEvaluationContext extends CachingIndividualRequestEvaluator
	{

		public IndividualRequestEvaluatorWithCacheUsingEvaluationContext(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource validStdEnvAttrSrc,
				final DecisionCache decisionCache)
		{
			super(rootPolicyEvaluator, validStdEnvAttrSrc, decisionCache);
		}

		private <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> DecisionResult evaluate(final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest,
				final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes)
		{
			/*
			 * Check whether there is any decision result in cache for this request
			 */
			final EvaluationContext evalCtx = newEvaluationContext(individualDecisionRequest, pdpIssuedAttributes);
			final DecisionResult cachedResult = decisionCache.get(individualDecisionRequest, evalCtx);
			if (cachedResult != null)
			{
				return cachedResult;
			}

			// result not in cache -> evaluate request
			final DecisionResult finalResult = evaluateReusingContext(evalCtx);
			decisionCache.put(individualDecisionRequest, finalResult, evalCtx);
			return finalResult;
		}

		@Override
		public <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeFqn, AttributeBag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException
		{
			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> finalResultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				final DecisionResult finalResult = evaluate(individualDecisionRequest, pdpIssuedAttributes);
				finalResultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, finalResult));
			}

			return finalResultsByRequest;
		}

	}

	private final boolean strictAttributeIssuerMatch;
	private final IndividualDecisionRequestEvaluator individualReqEvaluator;
	private final DecisionCache decisionCache;
	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final StandardEnvironmentAttributeIssuer pdpStdEnvAttributeIssuer;

	/**
	 * Constructs a new PDP engine with the given configuration information.
	 *
	 * @param xacmlExpressionFactory
	 *            XACML Expression parser/factory - mandatory
	 * @param rootPolicyProvider
	 *            Root Policy Provider - mandatory
	 * @param decisionCache
	 *            (optional) decision response cache
	 * @param strictAttributeIssuerMatch
	 *            true iff strict Attribute Issuer matching is enabled, i.e. AttributeDesignators without Issuer only match request Attributes without Issuer (and same AttributeId, Category...). This
	 *            mode is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it performs better and is recommended when all
	 *            AttributeDesignators have an Issuer (best practice). Reminder: the XACML 3.0 specification for AttributeDesignator evaluation (5.29) says: "If the Issuer is not present in the
	 *            attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone."
	 * @param stdEnvAttributeSource
	 *            (mandatory) source for standard environment current-time/current-date/current-dateTime attribute values (request or PDP, etc.).
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the mandatory arguments is null ({@code xacmlExpressionFactory}, {@code rootPolicyProvider})
	 * @throws java.io.IOException
	 *             error closing the root policy Provider when static resolution is to be used
	 */
	public BasePdpEngine(final ExpressionFactory xacmlExpressionFactory, final RootPolicyProvider rootPolicyProvider, final boolean strictAttributeIssuerMatch,
			final StandardEnvironmentAttributeSource stdEnvAttributeSource, final Optional<DecisionCache> decisionCache) throws IllegalArgumentException, IOException
	{
		final RootPolicyEvaluators.Base candidateRootPolicyEvaluator = new RootPolicyEvaluators.Base(xacmlExpressionFactory, rootPolicyProvider);
		// Use static resolution if possible
		final RootPolicyEvaluator staticRootPolicyEvaluator = candidateRootPolicyEvaluator.toStatic();
		if (staticRootPolicyEvaluator == null)
		{
			this.rootPolicyEvaluator = candidateRootPolicyEvaluator;
		}
		else
		{
			this.rootPolicyEvaluator = staticRootPolicyEvaluator;
		}

		this.strictAttributeIssuerMatch = strictAttributeIssuerMatch;

		Preconditions.checkNotNull(stdEnvAttributeSource, NULL_STD_ENV_ATTRIBUTE_SOURCE_ARG);
		this.pdpStdEnvAttributeIssuer = stdEnvAttributeSource == StandardEnvironmentAttributeSource.REQUEST_ONLY ? NULL_STD_ENV_ATTRIBUTE_ISSUER : DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER;

		this.decisionCache = decisionCache.orElse(null);
		if (this.decisionCache == null)
		{
			this.individualReqEvaluator = new NonCachingIndividualDecisionRequestEvaluator(rootPolicyEvaluator, stdEnvAttributeSource);
		}
		else
		{
			this.individualReqEvaluator = this.decisionCache.isEvaluationContextRequired() ? new IndividualRequestEvaluatorWithCacheUsingEvaluationContext(rootPolicyEvaluator, stdEnvAttributeSource,
					this.decisionCache) : new IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext(rootPolicyEvaluator, stdEnvAttributeSource, this.decisionCache);
		}
	}

	/**
	 * Constructs a new PDP engine with the given configuration information.
	 *
	 * @param configuration
	 *            PDP engine configuration
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code configuration.getXacmlExpressionFactory() == null || configuration.getRootPolicyProvider() == null}
	 * @throws java.io.IOException
	 *             error closing {@code configuration.getRootPolicyProvider()} when static resolution is to be used
	 */
	public BasePdpEngine(final PdpEngineConfiguration configuration) throws IllegalArgumentException, IOException
	{
		this(configuration.getXacmlExpressionFactory(), configuration.getRootPolicyProvider(), configuration.isStrictAttributeIssuerMatchEnabled(), configuration.getStdEnvAttributeSource(),
				configuration.getDecisionCache());
	}

	@Override
	public Iterable<PrimaryPolicyMetadata> getApplicablePolicies()
	{
		return this.rootPolicyEvaluator.getStaticApplicablePolicies();
	}

	@Override
	public DecisionRequestBuilder<?> newRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
	{
		return this.strictAttributeIssuerMatch ? new NonIssuedLikeIssuedAttributeHandlingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes)
				: new IssuedToNonIssuedAttributeCopyingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DecisionResult evaluate(final DecisionRequest individualDecisionRequest)
	{
		if (individualDecisionRequest == null)
		{
			throw NULL_REQUEST_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision request with extra common attributes set by the PDP once for all: standard environment attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
		 * before an individual request is evaluated to make sure it uses the same value for current-time/current-date/current-dateTime during the entire evaluation, if they use the one from PDP.
		 */
		return individualReqEvaluator.evaluateInNewContext(individualDecisionRequest, this.pdpStdEnvAttributeIssuer.get());
	}

	/** {@inheritDoc} */
	@Override
	public <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
			final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests) throws IndeterminateEvaluationException
	{
		if (individualDecisionRequests == null)
		{
			throw NULL_REQUEST_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision requests with extra common attributes set by the PDP once for all: standard environment attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
		 * before every individual request is evaluated to make sure they all use the same value for current-time/current-date/current-dateTime, if they use the one from PDP.
		 */
		return individualReqEvaluator.evaluate(individualDecisionRequests, this.pdpStdEnvAttributeIssuer.get());
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		rootPolicyEvaluator.close();
		if (decisionCache != null)
		{
			decisionCache.close();
		}
	}

}
