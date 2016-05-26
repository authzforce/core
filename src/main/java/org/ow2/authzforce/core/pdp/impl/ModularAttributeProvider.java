/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.AttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AttributeProvider working with sub-modules, each responsible of finding specific attributes in a specific way from a specific source. This attribute Provider tries to resolve attribute values in
 * current evaluation context first, then if not there, query the sub-modules.
 *
 * @version $Id: $
 */
public class ModularAttributeProvider implements AttributeProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ModularAttributeProvider.class);

	// AttributeDesignator Provider modules by supported/provided attribute ID (global ID: category, issuer,
	// AttributeId)
	private final Map<AttributeGUID, AttributeProviderModule> designatorModsByAttrId;

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query sub-modules providing the requested attribute ID, if any.
	 *
	 * @param attributeProviderModulesByAttributeId
	 *            attribute Provider modules sorted by supported attribute ID; may be null if none
	 */
	public ModularAttributeProvider(Map<AttributeGUID, AttributeProviderModule> attributeProviderModulesByAttributeId)
	{
		this(attributeProviderModulesByAttributeId, null);
	}

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query sub-modules providing the requested attribute ID, if any.
	 *
	 * @param attributeProviderModulesByAttributeId
	 *            attribute Provider modules sorted by supported attribute ID; may be null if none
	 * @param selectedAttributeSupport
	 *            (optional) selection of attributes to be supported, i.e. only attributes from this set may be supported/resolved by this attribute Provider; therefore, only the part of
	 *            {@code attributeProviderModulesByAttributeId} matching these attributes are to be used by this Provider. If {@code selectedAttributeSupport == null}, this parameter is ignored, i.e.
	 *            this is equivalent to {@link ModularAttributeProvider#ModularAttributeProvider(Map)}
	 */
	public ModularAttributeProvider(Map<AttributeGUID, AttributeProviderModule> attributeProviderModulesByAttributeId, Set<AttributeDesignatorType> selectedAttributeSupport)
	{
		if (attributeProviderModulesByAttributeId == null)
		{
			this.designatorModsByAttrId = Collections.emptyMap();
		} else if (selectedAttributeSupport == null)
		{
			designatorModsByAttrId = new HashMap<>(attributeProviderModulesByAttributeId);
		} else
		{
			designatorModsByAttrId = new HashMap<>(selectedAttributeSupport.size());
			for (final AttributeDesignatorType requiredAttr : selectedAttributeSupport)
			{
				final AttributeGUID requiredAttrGUID = new AttributeGUID(requiredAttr);
				final AttributeProviderModule requiredAttrProviderMod = attributeProviderModulesByAttributeId.get(requiredAttrGUID);
				// requiredAttrProviderMod = null means it should be provided by the request
				// context (in the initial request from PEP)
				if (requiredAttrProviderMod != null)
				{

					designatorModsByAttrId.put(requiredAttrGUID, requiredAttrProviderMod);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public final <AV extends AttributeValue> Bag<AV> get(AttributeGUID attributeGUID, Datatype<AV> attributeDatatype, EvaluationContext context) throws IndeterminateEvaluationException
	{
		try
		{
			final Bag<AV> contextBag = context.getAttributeDesignatorResult(attributeGUID, attributeDatatype);
			if (contextBag != null)
			{
				LOGGER.debug("Values of attribute {}, type={} found in evaluation context: {}", attributeGUID, attributeDatatype, contextBag);
				return contextBag;
			}

			// else attribute not found in context, ask the Provider modules, if any
			LOGGER.debug("Requesting attribute {} from Provider modules (by provided attribute ID): {}", attributeGUID, designatorModsByAttrId);
			final AttributeProviderModule attrProviderModule = designatorModsByAttrId.get(attributeGUID);
			if (attrProviderModule == null)
			{
				LOGGER.debug("No value found for required attribute {}, type={} in evaluation context and not supported by any attribute Provider module", attributeGUID, attributeDatatype);
				throw new IndeterminateEvaluationException("Not in context and no attribute Provider module supporting attribute: " + attributeGUID, StatusHelper.STATUS_MISSING_ATTRIBUTE);
			}

			final Bag<AV> result = attrProviderModule.get(attributeGUID, attributeDatatype, context);

			/*
			 * Cache the attribute value(s) in context to avoid waste of time querying the module twice for same attribute
			 */
			context.putAttributeDesignatorResultIfAbsent(attributeGUID, result);
			LOGGER.debug("Values of attribute {}, type={} returned by attribute Provider module #{} (cached in context): {}", attributeGUID, attributeDatatype, attrProviderModule, result);
			return result;
		} catch (IndeterminateEvaluationException e)
		{
			/*
			 * This error does not necessarily matter, it depends on whether the attribute is required, i.e. MustBePresent=true for AttributeDesignator/Selector So we let
			 * AttributeDesignator/Select#evaluate() method log the errors if MustBePresent=true. Here debug level is enough
			 */
			LOGGER.debug("Error finding attribute {}, type={}", attributeGUID, attributeDatatype, e);

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
			 * final Bag<T> contextBag = context.getAttributeDesignatorResult(attributeGUID,...)
			 * </code>
			 * </p>
			 * <p>
			 * Indeed, contextBag could be null for one of these two reasons:
			 * <ol>
			 * <li>The attribute ('attributeGUID') has never been requested in this context;
			 * <li>It has been requested before in this context but could not be found: error occurred (IndeterminateEvaluationException)</li>
			 * </ol>
			 * To avoid this confusion, we put an empty Bag (with some error info saying why this is empty).
			 * </p>
			 */
			final Bag<AV> result = Bags.empty(attributeDatatype, e);
			/*
			 * NOTE: It might happen - e.g. in conformance test IIB033 (Request's resource-id attribute datatype is different from datatype used in Policy) - that
			 * context.getAttributeDesignatorResult(attributeGUID, bagDatatype) threw IndeterminateEvaluationException although a value for 'attributeGUID' exists in context, because the existing
			 * datatype is different from requested 'bagDatatype'. In this case, the call below will return false (the value should not be overridden). We don't care about the result; what matters is
			 * that the value is set to an empty bag if there was no value.
			 */
			context.putAttributeDesignatorResultIfAbsent(attributeGUID, result);
			return result;
		}
	}
}
