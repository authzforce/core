package org.ow2.authzforce.core.pdp.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableMap;
import org.ow2.authzforce.core.pdp.api.expression.AttributeSelectorExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import net.sf.saxon.s9api.XdmNode;

/**
 * An {@link EvaluationContext} associated to an XACML Individual Decision Request, i.e. for evaluation to a single authorization decision Result (see Multiple Decision Profile spec for more
 * information on Individual Decision Request as opposed to Multiple Decision Request). This is the default {@link EvaluationContext} implementation used by the PDP engine. It is also meant to be used
 * particularly in unit tests of PDP extensions depending on evaluation context, e.g. {@link AttributeProvider}, {@link DecisionCache}, etc.
 *
 *
 * @version $Id: $
 */
public final class IndividualDecisionRequestContext implements EvaluationContext
{
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

	private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;

	/*
	 * Corresponds to Attributes/Content (by attribute category) marshalled to XPath data model for XPath evaluation: AttributeSelector evaluation, XPath-based functions, etc. This may be empty if no
	 * Content in Request or no feature requiring XPath evaluation against Content is supported/enabled.
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
	public IndividualDecisionRequestContext(final Map<AttributeFqn, AttributeBag<?>> namedAttributeMap, final Map<String, XdmNode> extraContentsByCategory, final boolean returnApplicablePolicyIdList)
	{
		this.namedAttributes = namedAttributeMap == null ? HashCollections.<AttributeFqn, AttributeBag<?>>newUpdatableMap()
				: HashCollections.<AttributeFqn, AttributeBag<?>>newUpdatableMap(namedAttributeMap);
		this.returnApplicablePolicyIdList = returnApplicablePolicyIdList;
		if (extraContentsByCategory == null)
		{
			this.extraContentsByAttributeCategory = Collections.emptyMap();
			this.attributeSelectorResults = UpdatableCollections.emptyMap();
		} else
		{
			this.extraContentsByAttributeCategory = extraContentsByCategory;
			this.attributeSelectorResults = UpdatableCollections.newUpdatableMap();
		}
	}

	/** {@inheritDoc} */
	@Override
	public <AV extends AttributeValue> AttributeBag<AV> getNamedAttributeValue(final AttributeFqn attributeFqn, final BagDatatype<AV> attributeBagDatatype) throws IndeterminateEvaluationException {
		final AttributeBag<?> bagResult = namedAttributes.get(attributeFqn);
		if (bagResult == null)
		{
			return null;
		}

		final Datatype<?> expectedElementDatatype = attributeBagDatatype.getElementType();
		if (!bagResult.getElementDatatype().equals(expectedElementDatatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ") of AttributeDesignator " + attributeFqn + " in context is different from expected/requested ("
					+ expectedElementDatatype
					+ "). May be caused by refering to the same Attribute Category/Id/Issuer with different Datatypes in different policy elements and/or attribute providers, which is not allowed.",
					XacmlStatusCode.SYNTAX_ERROR.value());
		}

		/*
		 * If datatype classes match, bagResult should have same type as datatypeClass.
		 */
		final AttributeBag<AV> result = (AttributeBag<AV>) bagResult;
		this.listeners.forEach((lt, l) -> l.namedAttributeValueConsumed(attributeFqn, result));
		return result;
	}

	@Override
	public boolean putNamedAttributeValueIfAbsent(final AttributeFqn attributeFqn, final AttributeBag<?> result) {
		final Bag<?> duplicate = namedAttributes.putIfAbsent(attributeFqn, result);
		if (duplicate != null)
		{
			/*
			 * This should never happen, as getAttributeDesignatorResult() should have been called first (for same id) and returned this oldResult, and no further call to
			 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not support setting a different result for same id (but different datatype URI/datatype class) in the same
			 * context
			 */
			LOGGER.warn("Attempt to override value of AttributeDesignator {} already set in evaluation context. Overriding value: {}", attributeFqn, result);
			return false;
		}

		this.listeners.forEach((lt, l) -> l.namedAttributeValueProduced(attributeFqn, result));
		/*
		 * Attribute value cannot change during evaluation context, so if old value already there, put it back
		 */
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public XdmNode getAttributesContent(final String category) {
		return extraContentsByAttributeCategory.get(category);
	}

	/** {@inheritDoc} */
	@Override
	public <AV extends AttributeValue> Bag<AV> getAttributeSelectorResult(final AttributeSelectorExpression<AV> attributeSelector) throws IndeterminateEvaluationException {
		final Bag<?> bagResult = attributeSelectorResults.get(attributeSelector.getAttributeSelectorId());
		if (bagResult == null)
		{
			return null;
		}

		final Datatype<Bag<AV>> expectedBagDatatype = attributeSelector.getReturnType();
		final Datatype<?> expectedElementDatatype = expectedBagDatatype.getTypeParameter().get();
		if (!bagResult.getElementDatatype().equals(expectedElementDatatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ")of AttributeSelector " + attributeSelector.getAttributeSelectorId()
					+ " in context is different from actually expected/requested (" + expectedElementDatatype
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
			throws IndeterminateEvaluationException {
		final AttributeSelectorId attSelectorId = attributeSelector.getAttributeSelectorId();
		if (attributeSelectorResults.putIfAbsent(attSelectorId, result) != null)
		{
			LOGGER.error("Attempt to override value of AttributeSelector {} already set in evaluation context. Overriding value: {}", attSelectorId, result);
			return false;
		}

		for (final Listener listener : this.listeners.values())
		{
			final Optional<AttributeFqn> optionalContextSelectorFQN = attributeSelector.getContextSelectorFQN();
			final Optional<AttributeBag<XPathValue>> contextSelectorValue = optionalContextSelectorFQN.isPresent()
					? Optional.of(getNamedAttributeValue(optionalContextSelectorFQN.get(), StandardDatatypes.XPATH.getBagDatatype()))
					: Optional.empty();
			listener.attributeSelectorResultProduced(attributeSelector, contextSelectorValue, result);
		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public <V extends Value> V getVariableValue(final String variableId, final Datatype<V> expectedDatatype) throws IndeterminateEvaluationException {
		final Value val = varValsById.get(variableId);
		if (val == null)
		{
			return null;
		}

		try
		{
			return expectedDatatype.cast(val);
		} catch (final ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Datatype of variable '" + variableId + "' in context does not match expected datatype: " + expectedDatatype,
					XacmlStatusCode.PROCESSING_ERROR.value(), e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean putVariableIfAbsent(final String variableId, final Value value) {
		if (varValsById.putIfAbsent(variableId, value) != null)
		{
			LOGGER.error("Attempt to override value of Variable '{}' already set in evaluation context. Overriding value: {}", variableId, value);
			return false;
		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Value removeVariable(final String variableId) {
		return varValsById.remove(variableId);
	}

	/** {@inheritDoc} */
	@Override
	public Object getOther(final String key) {
		return mutableProperties.get(key);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final String key) {
		return mutableProperties.containsKey(key);
	}

	/** {@inheritDoc} */
	@Override
	public void putOther(final String key, final Object val) {
		mutableProperties.put(key, val);
	}

	/** {@inheritDoc} */
	@Override
	public Object remove(final String key) {
		return mutableProperties.remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Entry<AttributeFqn, AttributeBag<?>>> getNamedAttributes() {
		final Set<Entry<AttributeFqn, AttributeBag<?>>> immutableAttributeSet = Collections.unmodifiableSet(namedAttributes.entrySet());
		return immutableAttributeSet.iterator();
	}

	@Override
	public boolean isApplicablePolicyIdListRequested() {
		return returnApplicablePolicyIdList;
	}

	@Override
	public <L extends Listener> L putListener(final Class<L> listenerType, final L listener) {
		return this.listeners.putInstance(listenerType, listener);
	}

	@Override
	public <L extends Listener> L getListener(final Class<L> listenerType) {
		return this.listeners.getInstance(listenerType);
	}
}