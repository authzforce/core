package com.thalesgroup.authzforce.core.eval;

import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.IndividualDecisionRequest;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeSelectorId;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

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
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(IndividualDecisionRequestContext.class);

	private static IndeterminateEvaluationException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new IndeterminateEvaluationException("Unsupported XACML feature (optional): <AttributeSelector>", Status.STATUS_SYNTAX_ERROR);

	private final Map<AttributeGUID, BagResult<? extends AttributeValue>> attributes;

	private final Map<String, ExpressionResult<? extends AttributeValue>> varValsById = new HashMap<>();

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
	private final Map<AttributeSelectorId, BagResult<? extends AttributeValue>> attributeSelectorResults;

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
	 * 
	 */
	public IndividualDecisionRequestContext(Map<AttributeGUID, BagResult<? extends AttributeValue>> attributeMap, Map<String, XdmNode> extraContentsByAttributeCategory)
	{
		attributes = attributeMap == null ? new HashMap<AttributeGUID, BagResult<? extends AttributeValue>>() : attributeMap;
		this.extraContentsByAttributeCategory = extraContentsByAttributeCategory;
		attributeSelectorResults = extraContentsByAttributeCategory == null ? null : new HashMap<AttributeSelectorId, BagResult<? extends AttributeValue>>();
	}

	/**
	 * Creates evaluation context from Individual Decision Request
	 * 
	 * @param individualDecisionReq
	 *            individual decision request
	 */
	public IndividualDecisionRequestContext(IndividualDecisionRequest individualDecisionReq)
	{
		this(individualDecisionReq.getNamedAttributes(), individualDecisionReq.getExtraContentsByCategory());
	}

	@Override
	public <T extends AttributeValue> BagResult<T> getAttributeDesignatorResult(AttributeGUID attributeGUID, Class<T> datatypeClass, DatatypeDef datatype) throws IndeterminateEvaluationException
	{
		final BagResult<? extends AttributeValue> bagResult = attributes.get(attributeGUID);
		if (bagResult == null)
		{
			return null;
		}

		if (bagResult.getDatatypeClass() != datatypeClass)
		{
			/*
			 * This is a request for an AttributeDesignator value with same (category, id, issuer)
			 * as another one but different datatype. This case is not supported.
			 */
			throw new IndeterminateEvaluationException("Unsupported reuse of same AttributeDesignator fields " + attributeGUID + " but with different datatypes: " + bagResult.getReturnType() + " (" + bagResult.getDatatypeClass() + ") and " + datatype + " (" + datatypeClass + ")",
					Status.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should has same type as datatypeClass. TODO: to
		 * avoid unchecked cast, we might want to return a new BagResult after casting all values in
		 * bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (BagResult<T>) bagResult;
	}

	@Override
	public <T extends AttributeValue> BagResult<T> putAttributeDesignatorResultIfAbsent(AttributeGUID attributeGUID, BagResult<T> result) throws IndeterminateEvaluationException
	{
		if (attributes.containsKey(attributeGUID))
		{
			final BagResult<? extends AttributeValue> oldResult = attributes.get(attributeGUID);
			if (oldResult.getDatatypeClass() != result.getDatatypeClass())
			{
				/*
				 * This should never happen, as getAttributeDesignatorResult() should have been
				 * called first (for same id) and returned this oldResult, and no further call to
				 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not
				 * support setting a different result for same id (but different datatype
				 * URI/datatype class) in the same context
				 */
				throw new IndeterminateEvaluationException("Unsupported reuse of same AttributeDesignator fields " + attributeGUID + " but with different datatypes: " + oldResult.getReturnType() + " (" + oldResult.getDatatypeClass() + ") and " + result.getReturnType() + " ("
						+ result.getDatatypeClass() + ")", Status.STATUS_SYNTAX_ERROR);
			}

			/*
			 * If datatype classes match, oldResult should has same type as result. TODO: to avoid
			 * unchecked cast, we might want to return a new BagResult after casting all values in
			 * bagResult with datatypeClass. Is it worth the trouble?
			 */
			return (BagResult<T>) oldResult;
		}

		/*
		 * Attribute value cannot change during evaluation context, so if old value already there,
		 * put it back
		 */
		attributes.put(attributeGUID, result);
		return null;
	}

	@Override
	public XdmNode getAttributesContent(String category)
	{
		return extraContentsByAttributeCategory == null ? null : extraContentsByAttributeCategory.get(category);
	}

	@Override
	public ExpressionResult<? extends AttributeValue> getVariableValue(String variableId)
	{
		return varValsById.get(variableId);
	}

	@Override
	public ExpressionResult<? extends AttributeValue> putVariableIfAbsent(String variableId, ExpressionResult<? extends AttributeValue> value)
	{
		if (varValsById.containsKey(variableId))
		{
			return varValsById.get(variableId);
		}

		return varValsById.put(variableId, value);
	}

	@Override
	public <T extends AttributeValue> BagResult<T> getAttributeSelectorResult(AttributeSelectorId id, Class<T> datatypeClass, String datatypeURI) throws IndeterminateEvaluationException
	{
		if (attributeSelectorResults == null)
		{
			throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		final BagResult<? extends AttributeValue> bagResult = attributeSelectorResults.get(id);
		if (bagResult == null)
		{
			return null;
		}

		if (bagResult.getDatatypeClass() != datatypeClass)
		{
			/*
			 * This is a request for an AttributeDesignator value with same (category, id, issuer)
			 * as another one but different datatype. This case is not supported.
			 */
			throw new IndeterminateEvaluationException("Unsupported reuse of same AttributeSelector fields " + id + " with different datatypes: " + bagResult.getReturnType() + " (" + bagResult.getDatatypeClass() + ") and " + datatypeURI + " (" + datatypeClass + ")", Status.STATUS_SYNTAX_ERROR);
		}

		/*
		 * If datatype classes match, bagResult should has same type as datatypeClass.
		 * 
		 * TODO: to avoid unchecked cast, we might want to return a new BagResult after casting all
		 * values in bagResult with datatypeClass. Is it worth the trouble?
		 */
		return (BagResult<T>) bagResult;
	}

	@Override
	public <T extends AttributeValue> BagResult<T> putAttributeSelectorResultIfAbsent(AttributeSelectorId id, BagResult<T> result) throws IndeterminateEvaluationException
	{
		if (attributeSelectorResults == null)
		{
			throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		if (attributeSelectorResults.containsKey(id))
		{
			final BagResult<? extends AttributeValue> oldResult = attributeSelectorResults.get(id);
			if (oldResult.getDatatypeClass() != result.getDatatypeClass())
			{
				/*
				 * This should never happen, as getAttributeDesignatorResult() should have been
				 * called first (for same id) and returned this oldResult, and no further call to
				 * putAttributeDesignatorResultIfAbsent() in this case. In any case, we do not
				 * support setting a different result for same id (but different datatype
				 * URI/datatype class) in the same context
				 */
				throw new IndeterminateEvaluationException("Unsupported reuse of same AttributeSelector fields " + id + " but with different datatypes: " + oldResult.getReturnType() + " (" + oldResult.getDatatypeClass() + ") and " + result.getReturnType() + " (" + result.getDatatypeClass() + ")",
						Status.STATUS_SYNTAX_ERROR);
			}

			/*
			 * If datatype classes match, oldResult should has same type as result.
			 * 
			 * TODO: to avoid unchecked cast, we might want to return a new BagResult after casting
			 * all values in bagResult with datatypeClass. Is it worth the trouble?
			 */
			return (BagResult<T>) oldResult;
		}

		attributeSelectorResults.put(id, result);
		return null;
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
}
