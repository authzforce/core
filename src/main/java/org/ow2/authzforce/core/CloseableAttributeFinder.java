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
package com.thalesgroup.authzforce.core.datatypes;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;

/**
 * Implementation of CloseAttributeFinder initialized with sub-modules, each responsible of finding
 * attributes in a specific way from a specific source. This attribute finder tries to resolve
 * attribute values in current evaluation context first, then if not there, query the sub-modules.
 * <p>
 * The sub-modules may very likely hold resources such as network resources to get attributes
 * remotely, or attribute caches to speed up finding, etc. Therefore, you are required to call
 * {@link #close()} when you no longer need an instance - especially before replacing with a new
 * instance (with different modules) - in order to make sure these resources are released properly
 * by each underlying module (e.g. close the attribute caches).
 * 
 */
public class CloseableAttributeFinderImpl implements CloseableAttributeFinder
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeFinder.class);

	private static final IndeterminateEvaluationException MISSING_ATTRIBUTE_IN_CONTEXT_WITH_NO_MODULE_EXCEPTION = new IndeterminateEvaluationException("Not in context and no attribute finder module registered", StatusHelper.STATUS_MISSING_ATTRIBUTE);

	// AttributeDesignator finder modules by global attribute ID (global ID: category, issuer,
	// AttributeId)
	private final Map<AttributeGUID, AttributeFinderModule> designatorModsByAttrId;

	private final ReadOnly readOnlyView = new ReadOnly();

	/**
	 * Instantiates attribute finder that tries to find attribute values in evaluation context,
	 * then, if not there, query the {@code module} providing the requested attribute ID, if any.
	 * 
	 * @param attributeFinderModulesByAttributeId
	 *            attribute finder modules sorted by supported attribute ID; may be null if none
	 */
	public CloseableAttributeFinderImpl(Map<AttributeGUID, AttributeFinderModule> attributeFinderModulesByAttributeId)
	{
		designatorModsByAttrId = attributeFinderModulesByAttributeId;
	}

	@Override
	public final void close() throws IOException
	{
		if (designatorModsByAttrId == null)
		{
			return;
		}

		// designatorModsByAttrId not null

		/**
		 * Invalidate caches of old/replaced modules since they are no longer used
		 */
		for (final AttributeFinderModule module : this.designatorModsByAttrId.values())
		{
			module.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thalesgroup.authzforce.core.datatypes.CloseableAttributeFinder#findAttribute(java.lang.String,
	 * com.thalesgroup.authzforce.core.datatypes.AttributeGUID,
	 * com.thalesgroup.authzforce.core.eval.EvaluationContext, java.lang.Class)
	 */
	@Override
	public final <AV extends AttributeValue<AV>> Bag<AV> findAttribute(AttributeGUID attributeGUID, EvaluationContext context, Bag.Datatype<AV> bagDatatype) throws IndeterminateEvaluationException
	{
		try
		{
			final Bag<AV> contextBag = context.getAttributeDesignatorResult(attributeGUID, bagDatatype);
			if (contextBag != null)
			{
				LOGGER.debug("Values of attribute {}, type={} found in evaluation context: {}", attributeGUID, bagDatatype, contextBag);
				return contextBag;
			}

			final Bag<AV> result;
			if (designatorModsByAttrId == null || designatorModsByAttrId.isEmpty())
			{
				LOGGER.debug("No value found for attribute {}, type={} in evaluation context and no attribute finder module registered", attributeGUID, bagDatatype);
				throw MISSING_ATTRIBUTE_IN_CONTEXT_WITH_NO_MODULE_EXCEPTION;
			}

			// else attribute not found in context, ask the finder modules, if any
			final AttributeFinderModule designatorMod = designatorModsByAttrId.get(attributeGUID);
			if (designatorMod == null)
			{
				LOGGER.debug("No value found for required attribute {}, type={} in evaluation context and not supported by any attribute finder module", attributeGUID, bagDatatype);
				throw new IndeterminateEvaluationException("Not in context and no attribute finder module supporting attribute " + attributeGUID, StatusHelper.STATUS_MISSING_ATTRIBUTE);
			}

			result = designatorMod.findAttribute(attributeGUID, context, bagDatatype);

			/*
			 * Cache the attribute value(s) in context to avoid waste of time querying the module
			 * twice for same attribute
			 */
			context.putAttributeDesignatorResultIfAbsent(attributeGUID, result);
			LOGGER.debug("Values of attribute {}, type={} returned by attribute finder module #{} (cached in context): {}", attributeGUID, bagDatatype, designatorMod, result);
			return result;
		} catch (IndeterminateEvaluationException e)
		{
			/*
			 * This error does not necessarily matter, it depends on whether the attribute is
			 * required, i.e. MustBePresent=true for AttributeDesignator/Selector So we let
			 * AttributeDesignator/Select#evaluate() method log the errors if MustBePresent=true.
			 * Here debug level is enough
			 */
			LOGGER.debug("Error finding attribute {}, type={}", attributeGUID, bagDatatype, e);

			/**
			 * If error occurred, we put the empty value to prevent retry in the same context, which
			 * may succeed at another time in the same context, resulting in different value of the
			 * same attribute at different times during evaluation within the same context,
			 * therefore inconsistencies. The value(s) must remain constant during the evaluation
			 * context, as explained in section 7.3.5 Attribute Retrieval of XACML core spec:
			 * <p>
			 * Regardless of any dynamic modifications of the request context during policy
			 * evaluation, the PDP SHALL behave as if each bag of attribute values is fully
			 * populated in the context before it is first tested, and is thereafter immutable
			 * during evaluation. (That is, every subsequent test of that attribute shall use 3313
			 * the same bag of values that was initially tested.)
			 * </p>
			 * Therefore, if no value found, we keep it that way until evaluation is done for the
			 * current request context.
			 * <p>
			 * We could put the null value to indicate the evaluation error, instead of an empty
			 * Bag, but it would make the result of the code used at the start of this method
			 * ambiguous/confusing:
			 * <p>
			 * <code>
			 * final Bag<T> contextBag = context.getAttributeDesignatorResult(attributeGUID,...)
			 * </code>
			 * </p>
			 * <p>
			 * Indeed, contextBag could be null for one of these two reasons:
			 * <ol>
			 * <li>The attribute ('attributeGUID') has never been requested in this context;
			 * <li>It has been requested before in this context but could not be found: error
			 * occurred (IndeterminateEvaluationException)</li>
			 * </ol>
			 * To avoid this confusion, we put an empty Bag (with some error info saying why this is
			 * empty).
			 * </p>
			 */
			final Bag<AV> result = Bag.empty(bagDatatype, e);
			context.putAttributeDesignatorResultIfAbsent(attributeGUID, result);
			return result;
		}
	}

	/**
	 * Get this as read-only
	 * 
	 * @return the read-only view of this
	 */
	public ReadOnly getReadOnlyView()
	{
		return readOnlyView;
	}

	private class ReadOnly implements AttributeFinder
	{

		@Override
		public final <AV extends AttributeValue<AV>> Bag<AV> findAttribute(AttributeGUID attributeGUID, EvaluationContext context, Bag.Datatype<AV> resultDatatype) throws IndeterminateEvaluationException
		{
			return CloseableAttributeFinderImpl.this.findAttribute(attributeGUID, context, resultDatatype);
		}

	}
}