/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.finder.ResourceFinder;
import com.sun.xacml.finder.ResourceFinderResult;
import com.thalesgroup.appsec.util.Utils;
import com.thalesgroup.authzforce.xacml.schema.XACMLCategory;

/**
 * This is the core class for the XACML engine, providing the starting point for request evaluation.
 * To build an XACML policy engine, you start by instantiating this object.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PDP
{

	// the single attribute finder that can be used to find external values
	private AttributeFinder attributeFinder;

	// the single policy finder that will be used to resolve policies
	private PolicyFinder policyFinder;

	// the single resource finder that will be used to resolve resources
	private ResourceFinder resourceFinder;

	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(PDP.class);

	private static Cache cache;

	private static PDP authzforce;

	private PDPConfig config;

	public static PDP getInstance()
	{
		if (authzforce == null)
		{
			authzforce = new PDP();
		}
		return authzforce;
	}

	public PDPConfig getPDPConfig()
	{
		return config;
	}

	public PDP()
	{
		config = new PDPConfig(null, null, null, null);
	}

	/**
	 * Constructs a new <code>PDP</code> object with the given configuration information.
	 * 
	 * @param config
	 *            user configuration data defining how to find policies, resolve external
	 *            attributes, etc.
	 */
	public PDP(PDPConfig config)
	{
		LOGGER.info("creating a PDP");

		this.config = config;

		attributeFinder = config.getAttributeFinder();

		policyFinder = config.getPolicyFinder();
		policyFinder.init();

		resourceFinder = config.getResourceFinder();

		cache = config.getCache();
	}

	/**
	 * This method is used to calculate the hashcode for caching and comparing Requests FIXME: Use
	 * {@link Request#hashCode()} instead, since already provided by XSD-to-JAXB generation
	 * 
	 * @param request
	 *            Request
	 * @return the calculated hashCode as a String
	 */
	public static String getHashCode(Request request)
	{
		int hash = 0;

		/**
		 * FIXME: MultiRequests, RequestDefaults, ReturnPolicyIdList, CombinedDecisions are ignored
		 * by this hash algorithm, why?
		 * Look at Request.hashCode() as a reference (generated by JAXB)
		 */
		for (Attributes avts : request.getAttributes())
		{
			for (Attribute att : avts.getAttributes())
			{
				hash += att.getAttributeId().hashCode();
				for (AttributeValueType attvt : att.getAttributeValues())
				{
					for (Object attContent : attvt.getContent())
					{
						hash += attContent.hashCode();
					}
				}
			}

		}

		return String.valueOf(hash);
	}

	/**
	 * Used to initiate a reload of the policies without reload the whole server
	 * 
	 * @return the PolicyFinder used by the PDP
	 */
	public PolicyFinder getPolicyFinder()
	{
		return policyFinder;
	}

	public void setPolicyFinder(PolicyFinder policyFinder)
	{
		this.policyFinder = policyFinder;
		// Used to reload
		getPolicyFinder();
	}

	/**
	 * Get the Attribute Finder, in order to update its attribute finder modules
	 * 
	 * @return the AttributeFinder used by the PDP
	 */
	public AttributeFinder getAttributeFinder()
	{
		return attributeFinder;
	}

	/**
	 * Attempts to evaluate the request against the policies known to this PDP. This is really the
	 * core method of the entire XACML specification, and for most people will provide what you
	 * want. If you need any special handling, you should look at the version of this method that
	 * takes an <code>EvaluationCtx</code>.
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required attribute, it was
	 * using an unsupported scope, etc), then the result will be a decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return a response paired to the request
	 */
	public ResponseCtx evaluate(Request request)
	{
		/*
		 * TODO: make this code more category-independent. In the profile spec, nothing is specific
		 * to subject, action or environment category for instance. So should be this code.
		 */
		List<Attributes> subjects = new ArrayList<>();
		List<Attributes> actions = new ArrayList<>();
		List<Attributes> resources = new ArrayList<>();
		List<Attributes> environments = new ArrayList<>();
		List<Attributes> customs = new ArrayList<>();
		List<Request> requests = new ArrayList<>();
		List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> results = new ArrayList<>();

		if (request.getMultiRequests() != null)
		{
			// TODO: Implement multiRequest
			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			StatusCode code = new StatusCode();
			code.setValue(Status.STATUS_SYNTAX_ERROR);
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status = new oasis.names.tc.xacml._3_0.core.schema.wd_17.Status();
			status.setStatusCode(code);
			status.setStatusMessage("Multi Request not implemented yet");
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE, status));
		}

		// no MultiRequests
		if (request.isCombinedDecision())
		{
			// TODO: Implement combinedDecision
			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			StatusCode code = new StatusCode();
			code.setValue(Status.STATUS_SYNTAX_ERROR);
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status = new oasis.names.tc.xacml._3_0.core.schema.wd_17.Status();
			status.setStatusCode(code);
			status.setStatusMessage("Combined decision not implemented yet");
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE, status));
		}

		// no MultiRequests and CombinedDecision=false
		for (Attributes myAttrs : request.getAttributes())
		{
			try
			{
				final XACMLCategory category = XACMLCategory.fromValue(myAttrs.getCategory());
				switch (category)
				{
					case XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT:
					case XACML_1_0_SUBJECT_CATEGORY_CODEBASE:
					case XACML_1_0_SUBJECT_CATEGORY_INTERMEDIARY_SUBJECT:
					case XACML_1_0_SUBJECT_CATEGORY_RECIPIENT_SUBJECT:
					case XACML_1_0_SUBJECT_CATEGORY_REQUESTING_MACHINE:
						subjects.add(myAttrs);
						break;
					case XACML_3_0_RESOURCE_CATEGORY_RESOURCE:
						/* Searching for resource */
						resources.add(myAttrs);
						break;
					case XACML_3_0_ACTION_CATEGORY_ACTION:
						/* Searching for action */
						actions.add(myAttrs);
						break;
					case XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT:
						// finally, set up the environment data, which is also generic
						environments.add(myAttrs);
						break;
				}
			} catch (IllegalArgumentException e)
			{
				// Attribute category didn't match any known category so we store
				// the attributes in an custom list
				customs.add(myAttrs);
			}
		}

		if (subjects.isEmpty() && actions.isEmpty() && resources.isEmpty())
		{
			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			StatusCode code = new StatusCode();
			code.setValue(Status.STATUS_SYNTAX_ERROR);
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status = new oasis.names.tc.xacml._3_0.core.schema.wd_17.Status();
			status.setStatusCode(code);
			status.setStatusMessage("Resource or Subject or Action attributes needs to be filled");
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE, status));
		}
		if (subjects.isEmpty())
		{
			Attributes subject = new Attributes();
			subjects.add(subject);
		}
		if (actions.isEmpty())
		{
			Attributes action = new Attributes();
			actions.add(action);
		}
		if (resources.isEmpty())
		{
			Attributes resource = new Attributes();
			resources.add(resource);
		}

		for (Attributes subjectAttr : subjects)
		{
			for (Attributes actionsAttr : actions)
			{
				for (Attributes resourcesAttr : resources)
				{
					Request tmpRequest = new Request();
					tmpRequest.setMultiRequests(request.getMultiRequests());
					tmpRequest.setRequestDefaults(request.getRequestDefaults());
					tmpRequest.setReturnPolicyIdList(request.isReturnPolicyIdList());
					tmpRequest.setCombinedDecision(request.isCombinedDecision());
					if (!subjectAttr.getAttributes().isEmpty())
					{
						tmpRequest.getAttributes().add(subjectAttr);
					}
					if (!actionsAttr.getAttributes().isEmpty())
					{
						tmpRequest.getAttributes().add(actionsAttr);
					}
					if (!resourcesAttr.getAttributes().isEmpty())
					{
						tmpRequest.getAttributes().add(resourcesAttr);
					}

					requests.add(tmpRequest);
				}
			}
		}

		/*
		 * Evaluate each request and add each result to final response results
		 * 
		 * Try-finally block to clean ThreadLocal used in evaluatePrivate()
		 */
		try
		{
			for (Request requestList : requests)
			{
				requestList.getAttributes().addAll(environments);
				requestList.getAttributes().addAll(customs);
				ResponseCtx response = this.evaluatePrivate(requestList);
				results.addAll(response.getResults());
			}
		} finally
		{
			Utils.THREAD_LOCAL_NS_AWARE_DOC_BUILDER.remove();
		}

		return new ResponseCtx(results);
	}

	/**
	 * Uses {@code Utils#THREAD_LOCAL_NS_AWARE_DOC_BUILDER } Uses
	 * {@link Utils#THREAD_LOCAL_NS_AWARE_DOC_BUILDER}. Call
	 * {@code Utils.THREAD_LOCAL_NS_AWARE_DOC_BUILDER.remove()} after calling this method (in
	 * finally block).
	 */
	private ResponseCtx evaluatePrivate(Request request)
	{
		// try to create the EvaluationCtx out of the request
		try
		{
			final BasicEvaluationCtx evalCtx = new BasicEvaluationCtx(request, attributeFinder, PolicyMetaData.XACML_VERSION_3_0);

			// Using the cache if defined
			if (cache != null && !cache.isDisabled())
			{
				final String cacheKey = getHashCode(request);
				final Element cacheResult = cache.get(cacheKey);
				LOGGER.debug("cache.get({}) -> '{}'", cacheKey, cacheResult);
				if (cacheResult != null)
				{
					LOGGER.debug("Response found in cache");
					return (ResponseCtx) cacheResult.getObjectValue();
				}

				final ResponseCtx respCtx = evaluate(evalCtx);
				cache.put(new Element(cacheKey, respCtx));
				return respCtx;
			}

			return evaluate(evalCtx);
		} catch (ParsingException | NumberFormatException | UnknownIdentifierException pe)
		{
			LOGGER.error("Invalid request to PDP", pe);

			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			final List<String> codes = new ArrayList<>();
			codes.add(Status.STATUS_SYNTAX_ERROR);
			Status status = new Status(codes, pe.getMessage());
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE, status));
		}
	}

	/**
	 * Uses the given <code>EvaluationCtx</code> against the available policies to determine a
	 * response. If you are starting with a standard XACML Request, then you should use the version
	 * of this method that takes a <code>Request</code>. This method should be used only if you have
	 * a real need to directly construct an evaluation context (or if you need to use an
	 * <code>EvaluationCtx</code> implementation other than <code>BasicEvaluationCtx</code>).
	 * 
	 * @param context
	 *            representation of the request and the context used for evaluation
	 * 
	 * @return a response based on the contents of the context
	 * 
	 * @deprecated Use ResponseCtx(Request request)
	 */
	public ResponseCtx evaluate(EvaluationCtx context)
	{

		// see if we need to call the resource finder
		if (context.getScope() != EvaluationCtx.SCOPE_IMMEDIATE)
		{
			AttributeValue parent = context.getResourceId();
			ResourceFinderResult resourceResult = null;

			if (context.getScope() == EvaluationCtx.SCOPE_CHILDREN)
			{
				resourceResult = resourceFinder.findChildResources(parent, context);
			} else
			{
				resourceResult = resourceFinder.findDescendantResources(parent, context);
			}

			// see if we actually found anything
			if (resourceResult.isEmpty())
			{
				// this is a problem, since we couldn't find any resources
				// to work on...the spec is not explicit about what kind of
				// error this is, so we're treating it as a processing error
				List<String> codes = new ArrayList<>();
				codes.add(Status.STATUS_PROCESSING_ERROR);
				String msg = "Couldn't find any resources to work on.";

				return new ResponseCtx(new Result(DecisionType.INDETERMINATE, new Status(codes, msg)));
			}

			// setup a list to keep track of the results
			List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> results = new ArrayList<>();

			// at this point, we need to go through all the resources we
			// successfully found and start collecting results
			Iterator it = resourceResult.getResources().iterator();
			while (it.hasNext())
			{
				// get the next resource, and set it in the EvaluationCtx
				AttributeValue resource = (AttributeValue) (it.next());
				context.setResourceId(resource);

				// do the evaluation, and set the resource in the result
				Result result = evaluateContext(context);

				// add the result
				results.add(result);
			}

			// now that we've done all the successes, we add all the failures
			// from the finder result
			Map failureMap = resourceResult.getFailures();
			it = failureMap.keySet().iterator();
			while (it.hasNext())
			{
				// get the next resource, and use it to get its Status data
				AttributeValue resource = (AttributeValue) (it.next());
				Status status = (Status) (failureMap.get(resource));

				// add a new result
				results.add(new Result(DecisionType.INDETERMINATE, status));
			}

			// return the set of results
			return new ResponseCtx(results);
		}

		// the scope was IMMEDIATE (or missing), so we can just evaluate
		// the request and return whatever we get back
		return new ResponseCtx(evaluateContext(context));
	}

	/**
	 * A private helper routine that resolves a policy for the given context, and then tries to
	 * evaluate based on the policy
	 */
	private Result evaluateContext(EvaluationCtx context)
	{
		// first off, try to find a policy
		PolicyFinderResult finderResult = policyFinder.findPolicy(context);

		// see if there weren't any applicable policies
		if (finderResult.notApplicable())
		{
			AttributeValue resourceId = context.getResourceId();
			if (resourceId != null)
			{
				return new Result(DecisionType.NOT_APPLICABLE, null, null, null, context.getIncludeInResults());
			}
			return new Result(DecisionType.NOT_APPLICABLE, null, null, null, context.getIncludeInResults());
		}

		// see if there were any errors in trying to get a policy
		if (finderResult.indeterminate())
		{
			return new Result(DecisionType.INDETERMINATE, finderResult.getStatus(), null, null, context.getIncludeInResults());
		}

		// we found a valid policy, so we can do the evaluation
		final Result result = finderResult.getPolicy().evaluate(context);
		/*
		 * Handle IncludeInResults only in final result, if we leave it to Policy.evaluate(), each
		 * Policy will add IncludeInResults causing duplicates,
		 * unless there are already IncludeInResults in the result.
		 */
		if(result.getAttributes().isEmpty() && context.getIncludeInResults() != null) {
			result.getAttributes().addAll(context.getIncludeInResults());
		}
		
		return result;
	}
}
