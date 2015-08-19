package com.thalesgroup.authzforce.core.attr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

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

	private static final IndeterminateEvaluationException MISSING_ATTRIBUTE_IN_CONTEXT_WITH_NO_MODULE_EXCEPTION = new IndeterminateEvaluationException("Not in context and no attribute finder module registered", Status.STATUS_MISSING_ATTRIBUTE);

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
	 * com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder#findAttribute(java.lang.String,
	 * com.thalesgroup.authzforce.core.attr.AttributeGUID,
	 * com.thalesgroup.authzforce.core.eval.EvaluationContext, java.lang.Class)
	 */
	@Override
	public final <T extends AttributeValue> BagResult<T> findAttribute(DatatypeDef datatype, AttributeGUID attributeGUID, EvaluationContext context, Class<T> datatypeClass) throws IndeterminateEvaluationException
	{
		try
		{
			final BagResult<T> contextBag = context.getAttributeDesignatorResult(attributeGUID, datatypeClass, datatype);
			if (contextBag != null)
			{
				LOGGER.debug("Values of attribute {}/type={} found in evaluation context: {}", attributeGUID, datatype, contextBag);
				return contextBag;
			}

			final BagResult<T> result;
			if (designatorModsByAttrId == null || designatorModsByAttrId.isEmpty())
			{
				LOGGER.debug("No value found for attribute {}/type={} in evaluation context and no attribute finder module registered", attributeGUID, datatype);
				throw MISSING_ATTRIBUTE_IN_CONTEXT_WITH_NO_MODULE_EXCEPTION;
			}

			// else attribute not found in context, ask the finder modules, if any
			final AttributeFinderModule designatorMod = designatorModsByAttrId.get(attributeGUID);
			if (designatorMod == null)
			{
				LOGGER.debug("No value found for required attribute {}/type={} in evaluation context and not supported by any attribute finder module", attributeGUID, datatype);
				throw new IndeterminateEvaluationException("Not in context and no attribute finder module supporting attribute " + attributeGUID, Status.STATUS_MISSING_ATTRIBUTE);
			}

			final List<T> bag = designatorMod.findAttribute(datatype, attributeGUID, context, datatypeClass);
			result = new BagResult<>(bag, datatypeClass, datatype);

			/*
			 * Cache the attribute value(s) in context to avoid waste of time querying the module
			 * twice for same attribute
			 */
			context.putAttributeDesignatorResultIfAbsent(attributeGUID, result);
			LOGGER.debug("Values of attribute {}/type={} returned by attribute finder module #{} (cached in context): {}", attributeGUID, datatype, designatorMod, result);

			return result;
		} catch (IndeterminateEvaluationException e)
		{
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
			 */
			final BagResult<T> result = new BagResult<>(e.getStatus(), datatypeClass, datatype);
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
		public final <T extends AttributeValue> BagResult<T> findAttribute(DatatypeDef attributeType, AttributeGUID attributeGUID, EvaluationContext context, Class<T> datatypeClass) throws IndeterminateEvaluationException
		{
			return CloseableAttributeFinderImpl.this.findAttribute(attributeType, attributeGUID, context, datatypeClass);
		}

	}
}