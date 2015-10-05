/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.eval;

import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.IndividualDecisionRequest;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeSelectorId;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

/**
 * An {@link EvaluationContext} associated to an XACML Individual Decision Request, i.e. for
 * evaluation to a single authorization decision Result (see Multiple Decision Profile spec for more
 * information on Individual Decision Request as opposed to Multiple Decision Request).
 * 
 */
public class IndividualDecisionRequestContext implements EvaluationContext
{
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

	private static IndeterminateEvaluationException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new IndeterminateEvaluationException("Unsupported XACML feature (optional): <AttributeSelector>", Status.STATUS_SYNTAX_ERROR);

	private final Map<AttributeGUID, Bag<?>> attributes;

	private final Map<String, Value<?, ?>> varValsById = new HashMap<>();

	/*
	 * Corresponds to Attributes/Content (by attribute category) marshalled to XPath data model for
	 * XPath evaluation: AttributeSelector evaluation, XPath-based functions, etc. This may be null
	 * if no Content in Request or no feature requiring XPath evaluation against Content is
	 * supported/enabled.
	 */
	private final Map<String, XdmNode> extraContentsByAttributeCategory;

	/*
	 * AttributeSelector evaluation results This may be null if no AttributeSelector evaluation
	 * disabled/not supported.
	 */
	private final Map<AttributeSelectorId, Bag<?>> attributeSelectorResults;

	private final XPathCompiler defaultXPathCompiler;

	/**
	 * Constructs a new <code>IndividualDecisionRequestContext</code> based on the given request
	 * attributes and extra contents with support for XPath evaluation against Content element in
	 * Attributes
	 * 
	 * @param attributeMap
	 *            attribute key and value pairs from the original Request. An attribute key is a
	 *            global ID based on attribute category,issuer,id. An attribute value is a bag of
	 *            primitive values.
	 * @param extraContentsByAttributeCategory
	 *            extra contents by attribute category (equivalent to XACML Attributes/Content
	 *            elements); null iff no Content in the attribute category.
	 * @param requestDefaultXPathCompiler
	 *            Request's default XPath Compiler (derived from
	 *            {@link RequestDefaults#getXPathVersion()})
	 * 
	 */
	public IndividualDecisionRequestContext(Map<AttributeGUID, Bag<?>> attributeMap, Map<String, XdmNode> extraContentsByAttributeCategory, XPathCompiler requestDefaultXPathCompiler)
	{
		this.attributes = attributeMap == null ? new HashMap<AttributeGUID, Bag<?>>() : attributeMap;
		this.extraContentsByAttributeCategory = extraContentsByAttributeCategory;
		this.attributeSelectorResults = extraContentsByAttributeCategory == null ? null : new HashMap<AttributeSelectorId, Bag<?>>();
		this.defaultXPathCompiler = requestDefaultXPathCompiler;
	}

	/**
	 * Creates evaluation context from Individual Decision Request
	 * 
	 * @param individualDecisionReq
	 *            individual decision request
	 */
	public IndividualDecisionRequestContext(IndividualDecisionRequest individualDecisionReq)
	{
		this(individualDecisionReq.getNamedAttributes(), individualDecisionReq.getExtraContentsByCategory(), individualDecisionReq.getDefaultXPathCompiler());
	}

	@Override
	public <AV extends AttributeValue<AV>> Bag<AV> getAttributeDesignatorResult(AttributeGUID attributeGUID, Datatype<Bag<AV>> datatype) throws IndeterminateEvaluationException
	{
		final Bag<?> bagResult = attributes.get(attributeGUID);
		if (bagResult == null)
		{
			return null;
		}

		if (!bagResult.getDatatype().equals(datatype))
		{
			throw new IndeterminateEvaluationException("Datatype of AttributeDesignator " + attributeGUID + " in context (" + bagResult.getDatatype() + ") is different from actually expected/requested (" + datatype + ")", Status.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should have same type as datatypeClass.
		 * 
		 * TODO: to avoid unchecked cast, we might want to return a new Bag after casting all values
		 * in bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (Bag<AV>) bagResult;
	}

	@Override
	public boolean putAttributeDesignatorResultIfAbsent(AttributeGUID attributeGUID, Bag<?> result)
	{
		if (attributes.containsKey(attributeGUID))
		{
			/*
			 * This should never happen, as getAttributeDesignatorResult() should have been called
			 * first (for same id) and returned this oldResult, and no further call to
			 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not support
			 * setting a different result for same id (but different datatype URI/datatype class) in
			 * the same context
			 */
			LOGGER.error("Attempt to override value of AttributeDesignator {} already set in evaluation context. Overriding value: {}", attributeGUID, result);
			return false;
		}

		/*
		 * Attribute value cannot change during evaluation context, so if old value already there,
		 * put it back
		 */
		return attributes.put(attributeGUID, result) == null;
	}

	@Override
	public XdmNode getAttributesContent(String category)
	{
		return extraContentsByAttributeCategory == null ? null : extraContentsByAttributeCategory.get(category);
	}

	@Override
	public <V extends Value<?, ?>> V getVariableValue(String variableId, Datatype<V> expectedDatatype) throws IndeterminateEvaluationException
	{
		final Value<?, ?> val = varValsById.get(variableId);
		if (val == null)
		{
			return null;
		}

		final Datatype<?> actualType = val.getReturnType();
		if (!actualType.equals(expectedDatatype))
		{
			throw new IndeterminateEvaluationException("Datatype (" + actualType + ") of value of variable '" + variableId + "' in context does not match expected datatype: " + expectedDatatype, Status.STATUS_PROCESSING_ERROR);
		}

		return (V) val;
	}

	@Override
	public boolean putVariableIfAbsent(String variableId, Value<?, ?> value)
	{
		if (varValsById.containsKey(variableId))
		{
			LOGGER.error("Attempt to override value of Variable '{}' already set in evaluation context. Overriding value: {}", variableId, value);
			return false;
		}

		return varValsById.put(variableId, value) == null;
	}

	@Override
	public Value<?, ?> removeVariable(String variableId)
	{
		return varValsById.remove(variableId);
	}

	@Override
	public <AV extends AttributeValue<AV>> Bag<AV> getAttributeSelectorResult(AttributeSelectorId id, Datatype<Bag<AV>> datatype) throws IndeterminateEvaluationException
	{
		if (attributeSelectorResults == null)
		{
			throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		final Bag<?> bagResult = attributeSelectorResults.get(id);
		if (bagResult == null)
		{
			return null;
		}

		if (!bagResult.getDatatype().equals(datatype))
		{
			throw new IndeterminateEvaluationException("Datatype of AttributeSelector " + id + " in context (" + bagResult.getDatatype() + ") is different from actually expected/requested (" + datatype + ")", Status.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should has same type as datatypeClass.
		 * 
		 * TODO: to avoid unchecked cast, we might want to return a new Bag after casting all values
		 * in bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (Bag<AV>) bagResult;
	}

	@Override
	public boolean putAttributeSelectorResultIfAbsent(AttributeSelectorId id, Bag<?> result) throws IndeterminateEvaluationException
	{
		if (attributeSelectorResults == null)
		{
			throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		if (attributeSelectorResults.containsKey(id))
		{
			LOGGER.error("Attempt to override value of AttributeSelector {} already set in evaluation context. Overriding value: {}", id, result);
			return false;
		}

		return attributeSelectorResults.put(id, result) == null;
	}

	private final Map<String, Object> updatableProperties = new HashMap<>();

	@Override
	public Object getOther(String key)
	{
		return updatableProperties.get(key);
	}

	@Override
	public boolean containsKey(String key)
	{
		return updatableProperties.containsKey(key);
	}

	@Override
	public void putOther(String key, Object val)
	{
		updatableProperties.put(key, val);
	}

	@Override
	public Object remove(String key)
	{
		return updatableProperties.remove(key);
	}

	@Override
	public XPathCompiler getDefaultXPathCompiler()
	{
		return this.defaultXPathCompiler;
	}
}
