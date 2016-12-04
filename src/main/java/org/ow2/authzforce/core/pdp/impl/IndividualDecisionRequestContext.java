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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableMap;
import org.ow2.authzforce.core.pdp.api.UpdatableSet;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link EvaluationContext} associated to an XACML Individual Decision Request, i.e. for evaluation to a single authorization decision Result (see Multiple Decision Profile spec for more
 * information on Individual Decision Request as opposed to Multiple Decision Request).
 *
 * @version $Id: $
 */
public final class IndividualDecisionRequestContext implements EvaluationContext
{
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

	private final Map<AttributeGUID, Bag<?>> namedAttributes;

	private final Map<String, Value> varValsById = HashCollections.newMutableMap();

	private final Map<String, Object> mutableProperties = HashCollections.newMutableMap();

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

	// not null
	private final UpdatableSet<AttributeGUID> usedNamedAttributeIdSet;

	// not null
	private final UpdatableSet<AttributeSelectorId> usedAttributeSelectorIdSet;

	private final boolean returnApplicablePolicyIdList;

	/**
	 * Constructs a new <code>IndividualDecisionRequestContext</code> based on the given request attributes and extra contents with support for XPath evaluation against Content element in Attributes
	 *
	 * @param namedAttributeMap
	 *            updatable named attribute map (attribute key and value pairs) from the original Request; null iff none. An attribute key is a global ID based on attribute category,issuer,id. An
	 *            attribute value is a bag of primitive values.
	 * @param extraContentsByAttributeCategory
	 *            extra contents by attribute category (equivalent to XACML Attributes/Content elements); null iff no Content in the attribute category.
	 * @param returnApplicablePolicyIdList
	 *            true iff list of IDs of policies matched during evaluation must be returned
	 * @param returnUsedAttributes
	 *            true iff the list of attributes used during evaluation may be requested by
	 */
	public IndividualDecisionRequestContext(final Map<AttributeGUID, Bag<?>> namedAttributeMap, final Map<String, XdmNode> extraContentsByAttributeCategory,
			final boolean returnApplicablePolicyIdList, final boolean returnUsedAttributes)
	{
		this.namedAttributes = namedAttributeMap == null ? HashCollections.<AttributeGUID, Bag<?>> newUpdatableMap() : namedAttributeMap;
		this.returnApplicablePolicyIdList = returnApplicablePolicyIdList;
		this.usedNamedAttributeIdSet = returnUsedAttributes ? UpdatableCollections.<AttributeGUID> newUpdatableSet() : UpdatableCollections.<AttributeGUID> emptySet();
		if (extraContentsByAttributeCategory == null)
		{
			this.extraContentsByAttributeCategory = Collections.emptyMap();
			this.attributeSelectorResults = UpdatableCollections.emptyMap();
			// not used since there is no <Content>
			// (extraContentsByAttributeCategory == null)
			this.usedAttributeSelectorIdSet = UpdatableCollections.emptySet();
		}
		else
		{
			this.extraContentsByAttributeCategory = extraContentsByAttributeCategory;
			this.attributeSelectorResults = UpdatableCollections.newUpdatableMap();
			this.usedAttributeSelectorIdSet = returnUsedAttributes ? UpdatableCollections.<AttributeSelectorId> newUpdatableSet() : UpdatableCollections.<AttributeSelectorId> emptySet();
		}
	}

	/**
	 * Creates evaluation context from Individual Decision Request
	 *
	 * @param individualDecisionReq
	 *            individual decision request
	 * @param returnUsedAttributes
	 *            true iff the list of attributes used during evaluation may be requested by
	 */
	public IndividualDecisionRequestContext(final IndividualDecisionRequest individualDecisionReq, final boolean returnUsedAttributes)
	{
		this(individualDecisionReq.getNamedAttributes(), individualDecisionReq.getExtraContentsByCategory(), individualDecisionReq.isApplicablePolicyIdListReturned(), returnUsedAttributes);
	}

	/** {@inheritDoc} */
	@Override
	public <AV extends AttributeValue> Bag<AV> getAttributeDesignatorResult(final AttributeGUID attributeGUID, final Datatype<AV> attributeDatatype) throws IndeterminateEvaluationException
	{
		this.usedNamedAttributeIdSet.add(attributeGUID);
		final Bag<?> bagResult = namedAttributes.get(attributeGUID);
		if (bagResult == null)
		{
			return null;
		}

		if (!bagResult.getElementDatatype().equals(attributeDatatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ") of AttributeDesignator " + attributeGUID
					+ " in context is different from expected/requested (" + attributeDatatype
					+ "). May be caused by refering to the same Attribute Category/Id/Issuer with different Datatypes in different policy elements and/or attribute providers, which is not allowed.",
					StatusHelper.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should have same type as datatypeClass.
		 * 
		 * TODO: to avoid unchecked cast, we might want to return a new Bag after casting all values in bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (Bag<AV>) bagResult;
	}

	/** {@inheritDoc} */
	@Override
	public boolean putAttributeDesignatorResultIfAbsent(final AttributeGUID id, final Bag<?> result)
	{
		final Bag<?> duplicate = namedAttributes.putIfAbsent(id, result);
		if (duplicate != null)
		{
			/*
			 * This should never happen, as getAttributeDesignatorResult() should have been called first (for same id) and returned this oldResult, and no further call to
			 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not support setting a different result for same id (but different datatype URI/datatype class) in the same
			 * context
			 */
			LOGGER.error("Attempt to override value of AttributeDesignator {} already set in evaluation context. Overriding value: {}", id, result);
			return false;
		}

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
					StatusHelper.STATUS_PROCESSING_ERROR, e);
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
	public <AV extends AttributeValue> Bag<AV> getAttributeSelectorResult(final AttributeSelectorId id, final Datatype<AV> datatype) throws IndeterminateEvaluationException
	{
		this.usedAttributeSelectorIdSet.add(id);
		final Bag<?> bagResult = attributeSelectorResults.get(id);
		if (bagResult == null)
		{
			return null;
		}

		if (!bagResult.getElementDatatype().equals(datatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ")of AttributeSelector " + id + " in context is different from actually expected/requested ("
					+ datatype
					+ "). May be caused by use of same AttributeSelector Category/Path/ContextSelectorId with different Datatypes in different in different policy elements, which is not allowed.",
					StatusHelper.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should has same type as datatypeClass.
		 * 
		 * TODO: to avoid unchecked cast, we might want to return a new Bag after casting all values in bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (Bag<AV>) bagResult;
	}

	/** {@inheritDoc} */
	@Override
	public boolean putAttributeSelectorResultIfAbsent(final AttributeSelectorId id, final Bag<?> result) throws IndeterminateEvaluationException
	{
		if (attributeSelectorResults.putIfAbsent(id, result) != null)
		{
			LOGGER.error("Attempt to override value of AttributeSelector {} already set in evaluation context. Overriding value: {}", id, result);
			return false;
		}

		return true;
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
	public Iterator<Entry<AttributeGUID, Bag<?>>> getAttributes()
	{
		final Set<Entry<AttributeGUID, Bag<?>>> immutableAttributeSet = Collections.unmodifiableSet(namedAttributes.entrySet());
		return immutableAttributeSet.iterator();
	}

	@Override
	public Set<AttributeGUID> getUsedNamedAttributes()
	{
		return usedNamedAttributeIdSet.copy();
	}

	@Override
	public Set<AttributeSelectorId> getUsedExtraAttributeContents()
	{
		return usedAttributeSelectorIdSet.copy();
	}

	@Override
	public boolean isApplicablePolicyIdListRequested()
	{
		return returnApplicablePolicyIdList;
	}
}
