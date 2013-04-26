/*
 * @(#)PDP.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCodeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
//import com.sun.xacml.ctx.xacmlv3.RequestCtx;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.finder.ResourceFinder;
import com.sun.xacml.finder.ResourceFinderResult;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * This is the core class for the XACML engine, providing the starting point for
 * request evaluation. To build an XACML policy engine, you start by
 * instantiating this object.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PDP {

	// the single attribute finder that can be used to find external values
	private AttributeFinder attributeFinder;

	// the single policy finder that will be used to resolve policies
	private PolicyFinder policyFinder;

	// the single resource finder that will be used to resolve resources
	private ResourceFinder resourceFinder;

	// the logger we'll use for all messages
//	private static final Logger OLD_LOGGER = Logger.getLogger(PDP.class
//			.getName());
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(PDP.class.getName());

	private static CacheManager cacheManager;

	private static PDP authzforce;

	private PDPConfig config;

	public static PDP getInstance() {
		if (authzforce == null) {
			authzforce = new PDP();
		}
		return authzforce;
	}

	public PDPConfig getPDPConfig() {
		return config;
	}

	public PDP() {
		config = new PDPConfig(null, null, null, null);
	}

	/**
	 * Constructs a new <code>PDP</code> object with the given configuration
	 * information.
	 * 
	 * @param config
	 *            user configuration data defining how to find policies, resolve
	 *            external attributes, etc.
	 */
	public PDP(PDPConfig config) {
		LOGGER.info("creating a PDP");

		this.config = config;

		attributeFinder = config.getAttributeFinder();

		policyFinder = config.getPolicyFinder();
		policyFinder.init();

		resourceFinder = config.getResourceFinder();

		cacheManager = config.getCacheManager();

		/*
		 * Cache initialization
		 * 
		 * AUTHOR: romain.ferrari[AT]thalesgroup.com
		 */
		if (cacheManager.isActivate()) {
			cacheManager = CacheManager.getInstance();
		}
	}

	private String getHashCode(RequestType myEvaluationCtx) {
		int hash = 0;

		for (AttributesType avts : myEvaluationCtx.getAttributes()) {
			for (AttributeType att : avts.getAttribute()) {
				hash += att.getAttributeId().hashCode();
				for (AttributeValueType attvt : att.getAttributeValue()) {
					for (Object attContent : attvt.getContent()) {
						hash += attContent.hashCode();
					}
				}
			}

		}
		// // Searching within the Action for AttributeType
		// for (AttributeType myAttributeType : myEvaluationCtx.getAction()
		// .getAttribute()) {
		// hash += myAttributeType.getAttributeId().hashCode();
		// // Searching within the AttributeType for AttributeValueType
		// for (AttributeValueType myAttributeValueType : myAttributeType
		// .getAttributeValue()) {
		// // Searching within the AttributeValueType for Object
		// // (Defined
		// // as Object in the model but it's actually String)
		// for (Object myContent : myAttributeValueType.getContent()) {
		// hash += myContent.hashCode();
		// }
		// }
		// }
		// // Searching within the Subject for AttributeType
		// for (SubjectType mySubjectType : myEvaluationCtx.getSubject()) {
		// // Searching within the AttributeType for AttributeValueType
		// for (AttributeType myAttributeType : mySubjectType
		// .getAttribute()) {
		// hash += myAttributeType.getAttributeId().hashCode();
		// for (AttributeValueType myAttributeValueType : myAttributeType
		// .getAttributeValue()) {
		// // Searching within the AttributeValueType for Object
		// // (Defined
		// // as Object in the model but it's actually String)
		// for (Object myContent : myAttributeValueType
		// .getContent()) {
		// hash += myContent.hashCode();
		// }
		// }
		// }
		// }
		// // Searching within the Resource for AttributeType
		// for (ResourceType myResourceType : myEvaluationCtx.getResource()) {
		// // Searching within the AttributeType for AttributeValueType
		// for (AttributeType myAttributeType : myResourceType
		// .getAttribute()) {
		// hash += myAttributeType.getAttributeId().hashCode();
		// for (AttributeValueType myAttributeValueType : myAttributeType
		// .getAttributeValue()) {
		// // Searching within the AttributeValueType for Object
		// // (Defined
		// // as Object in the model but it's actually String)
		// for (Object myContent : myAttributeValueType
		// .getContent()) {
		// hash += myContent.hashCode();
		// }
		// }
		// }
		// }
		// }
		return String.valueOf(hash);
	}

	/**
	 * Used to initiate a reload of the policies without reload the whole server
	 * 
	 * @author romain.ferrari[AT]thalesgroup.com
	 * @return the PolicyFinder used by the PDP
	 */
	public PolicyFinder getPolicyFinder() {
		return policyFinder;
	}

	public void setPolicyFinder(PolicyFinder policyFinder) {
		this.policyFinder = policyFinder;
		// Used to reload
		getPolicyFinder();
	}

	/**
	 * Attempts to evaluate the request against the policies known to this PDP.
	 * This is really the core method of the entire XACML specification, and for
	 * most people will provide what you want. If you need any special handling,
	 * you should look at the version of this method that takes an
	 * <code>EvaluationCtx</code>.
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required
	 * attribute, it was using an unsupported scope, etc), then the result will
	 * be a decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @deprecated As of release2.0, replaced by {@link #evaluate(RequestType)}
	 * @return a response paired to the request
	 */
//	public ResponseCtx evaluate(RequestCtx request) {
//		ResponseCtx response = null;
//
//		JAXBElement<RequestType> jaxbRequestElem = null;
//		RequestType jaxbRequest = null;
//		try {
//			if (request.getDocumentRoot() != null) {
//				jaxbRequestElem = BindingUtility.getUnmarshaller().unmarshal(
//						request.getDocumentRoot(), RequestType.class);
//				jaxbRequest = jaxbRequestElem.getValue();
//			} else {
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				request.encode(bos);
//
//				byte[] buf = bos.toByteArray();
//				ByteArrayInputStream bis = new ByteArrayInputStream(buf);
//
//				jaxbRequestElem = (JAXBElement<RequestType>) BindingUtility
//						.getUnmarshaller().unmarshal(bis);
//				jaxbRequest = jaxbRequestElem.getValue();
//			}
//			response = this.evaluate(jaxbRequest);
//		} catch (JAXBException e) {
//			LOGGER.info("the PDP receieved an invalid request",
//					e);
//
//			// there was something wrong with the request, so we return
//			// Indeterminate with a status of syntax error...though this
//			// may change if a more appropriate status type exists
//			ArrayList code = new ArrayList();
//			code.add(Status.STATUS_SYNTAX_ERROR);
//			Status status = new Status(code, e.getMessage());
//
//			response = new ResponseCtx(new Result(
//					DecisionType.INDETERMINATE, status));
//		}
//		return response;
//	}

	public List<ResponseCtx> evaluateList(RequestType request) {

		List<AttributesType> subjects = new ArrayList<AttributesType>();
		List<AttributesType> actions = new ArrayList<AttributesType>();
		List<AttributesType> resources = new ArrayList<AttributesType>();
		List<AttributesType> environments = new ArrayList<AttributesType>();
		List<RequestType> requests = new ArrayList<RequestType>();
		List<ResponseCtx> responses = new ArrayList<ResponseCtx>();

		if (request.getMultiRequests() != null) {
			// TODO: Implement multirequest
		} else {

			for (AttributesType myAttr : request.getAttributes()) {
				if (myAttr.getCategory().equals(
						XACMLAttributeId.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value())) {
					resources.add(myAttr);
				} else if (myAttr.getCategory().equals(
						XACMLAttributeId.XACML_1_0_SUBJECT_CATEGORY_SUBJECT.value())) {
					subjects.add(myAttr);
				} else if (myAttr.getCategory().equals(
						XACMLAttributeId.XACML_3_0_ACTION_CATEGORY_ACTION.value())) {
					actions.add(myAttr);
				} else if (myAttr.getCategory().equals(
						XACMLAttributeId.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value())) {
					environments.add(myAttr);
				}
			}

			for (AttributesType subjectAttr : subjects) {
				for (AttributesType actionsAttr : actions) {
					for (AttributesType resourcesAttr : resources) {
						RequestType tmpRequest = new RequestType();
						tmpRequest.setMultiRequests(request.getMultiRequests());
						tmpRequest.setRequestDefaults(request.getRequestDefaults());
						tmpRequest.setReturnPolicyIdList(request.isReturnPolicyIdList());
						tmpRequest.setCombinedDecision(request.isCombinedDecision());
						tmpRequest.getAttributes().add(resourcesAttr);
						tmpRequest.getAttributes().add(actionsAttr);
						tmpRequest.getAttributes().add(subjectAttr);
						requests.add(tmpRequest);
					}
				}
			}
			for (RequestType requestList : requests) {
				requestList.getAttributes().addAll(environments);
			}
			for (RequestType requestType : requests) {
				ResponseCtx response = this.evaluate(requestType);
				responses.add(response);
			}
		}
		
		return responses;
	}

	/**
	 * Attempts to evaluate the request against the policies known to this PDP.
	 * This is really the core method of the entire XACML specification, and for
	 * most people will provide what you want. If you need any special handling,
	 * you should look at the version of this method that takes an
	 * <code>EvaluationCtx</code>.
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required
	 * attribute, it was using an unsupported scope, etc), then the result will
	 * be a decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * 
	 * @return a response paired to the request
	 */
	public ResponseCtx evaluate(RequestType request) {
		String hash = "";

		// try to create the EvaluationCtx out of the request
		// FIXME: finish implementation
		try {
			BasicEvaluationCtx myEvaluationCtx = new BasicEvaluationCtx(
					request,
					attributeFinder,
					Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
			/*
			 * 
			 */
			// @author: romain.ferrari@thalesgroup.com
			// Using the cache if defined
			if (cacheManager.isActivate()) {
				hash = getHashCode(request);
				ResponseCtx cacheResult = (ResponseCtx) cacheManager
						.checkCache(hash);
				if (cacheResult != null) {
					LOGGER.debug("Response found in cache");
					return cacheResult;
				}
			}
			ResponseCtx myResponse = evaluate(myEvaluationCtx);
			// Using the cache if defined
			if (cacheManager.isActivate()) {
				cacheManager.updateCache(hash, myResponse);
			}
			/*
			 * 
			 */

			return myResponse;
		} catch (ParsingException pe) {
			LOGGER.debug("the PDP receieved an invalid request", pe);

			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			ArrayList code = new ArrayList();
			code.add(Status.STATUS_SYNTAX_ERROR);
			Status status = new Status(code, pe.getMessage());

			return new ResponseCtx(new Result(DecisionType.INDETERMINATE,
					status));
		} catch (NumberFormatException e) {
			LOGGER.error(e);
			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			ArrayList code = new ArrayList();
			code.add(Status.STATUS_SYNTAX_ERROR);
			Status status = new Status(code, e.getLocalizedMessage());
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE,
					status));
		} catch (UnknownIdentifierException e) {
			LOGGER.error(e);
			// there was something wrong with the request, so we return
			// Indeterminate with a status of syntax error...though this
			// may change if a more appropriate status type exists
			StatusCodeType code = new StatusCodeType();
			code.setValue(Status.STATUS_SYNTAX_ERROR);
			StatusType status = new StatusType();
			status.setStatusCode(code);
			status.setStatusMessage(e.getLocalizedMessage());
			return new ResponseCtx(new Result(DecisionType.INDETERMINATE, status));
		}
	}

	/**
	 * Uses the given <code>EvaluationCtx</code> against the available policies
	 * to determine a response. If you are starting with a standard XACML
	 * Request, then you should use the version of this method that takes a
	 * <code>RequestType</code>. This method should be used only if you have a
	 * real need to directly construct an evaluation context (or if you need to
	 * use an <code>EvaluationCtx</code> implementation other than
	 * <code>BasicEvaluationCtx</code>).
	 * 
	 * @param context
	 *            representation of the request and the context used for
	 *            evaluation
	 * 
	 * @return a response based on the contents of the context
	 */
	public ResponseCtx evaluate(EvaluationCtx context) {

		// see if we need to call the resource finder
		if (context.getScope() != EvaluationCtx.SCOPE_IMMEDIATE) {
			AttributeValue parent = context.getResourceId();
			ResourceFinderResult resourceResult = null;

			if (context.getScope() == EvaluationCtx.SCOPE_CHILDREN) {
				resourceResult = resourceFinder.findChildResources(parent,
						context);
			} else {
				resourceResult = resourceFinder.findDescendantResources(parent,
						context);
			}

			// see if we actually found anything
			if (resourceResult.isEmpty()) {
				// this is a problem, since we couldn't find any resources
				// to work on...the spec is not explicit about what kind of
				// error this is, so we're treating it as a processing error
				ArrayList code = new ArrayList();
				code.add(Status.STATUS_PROCESSING_ERROR);
				String msg = "Couldn't find any resources to work on.";

				return new ResponseCtx(new Result(
						DecisionType.INDETERMINATE, new Status(code, msg),
						context.getResourceId().encode()));
			}

			// setup a set to keep track of the results
			HashSet results = new HashSet();

			// at this point, we need to go through all the resources we
			// successfully found and start collecting results
			Iterator it = resourceResult.getResources().iterator();
			while (it.hasNext()) {
				// get the next resource, and set it in the EvaluationCtx
				AttributeValue resource = (AttributeValue) (it.next());
				context.setResourceId(resource);

				// do the evaluation, and set the resource in the result
				Result result = evaluateContext(context);
				result.setResource(resource.encode());

				// add the result
				results.add(result);
			}

			// now that we've done all the successes, we add all the failures
			// from the finder result
			Map failureMap = resourceResult.getFailures();
			it = failureMap.keySet().iterator();
			while (it.hasNext()) {
				// get the next resource, and use it to get its Status data
				AttributeValue resource = (AttributeValue) (it.next());
				Status status = (Status) (failureMap.get(resource));

				// add a new result
				results.add(new Result(DecisionType.INDETERMINATE, status,
						resource.encode()));
			}

			// return the set of results
			return new ResponseCtx(results);
		} else {
			// the scope was IMMEDIATE (or missing), so we can just evaluate
			// the request and return whatever we get back
			return new ResponseCtx(evaluateContext(context));
		}
	}

	/**
	 * A private helper routine that resolves a policy for the given context,
	 * and then tries to evaluate based on the policy
	 */
	private Result evaluateContext(EvaluationCtx context) {
		// first off, try to find a policy
		PolicyFinderResult finderResult = policyFinder.findPolicy(context);

		// see if there weren't any applicable policies
		if (finderResult.notApplicable()) {
			AttributeValue resourceId = context.getResourceId();
			if(resourceId != null) {
				return new Result(DecisionType.NOT_APPLICABLE, null, context
						.getResourceId().encode(), null, null, context.getIncludeInResults());	
			}
			return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, context.getIncludeInResults());
		}

		// see if there were any errors in trying to get a policy
		if (finderResult.indeterminate()) {
			return new Result(DecisionType.INDETERMINATE,
					finderResult.getStatus(), context.getResourceId().encode(), null, null, context.getIncludeInResults());
		}

		// we found a valid policy, so we can do the evaluation
		if (finderResult.getType().equals("PolicySet")) {
			return finderResult.getPolicySet().evaluate(context);	
		}
		return finderResult.getPolicy().evaluate(context);
	}
}
