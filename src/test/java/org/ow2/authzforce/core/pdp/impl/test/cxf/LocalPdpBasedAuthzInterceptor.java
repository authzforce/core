/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.test.cxf;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.rt.security.saml.xacml.CXFMessageParser;
import org.apache.cxf.rt.security.saml.xacml.XACMLConstants;
import org.apache.cxf.security.LoginSecurityContext;
import org.apache.cxf.security.SecurityContext;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.ImmutablePdpDecisionRequest;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;

/**
 * This class represents a so-called XACML PEP that, for every CXF service request, creates an XACML 3.0 authorization decision Request to a PDP using AuthzForce's native API, given a Principal, list
 * of roles - typically coming from SAML token - and MessageContext. The principal name is inserted as the Subject ID, and the list of roles associated with that principal are inserted as Subject
 * roles. The action to send defaults to "execute". It is an adaptation of
 * https://github.com/coheigea/testcases/blob/master/apache/cxf/cxf-sts-xacml/src/test/java/org/apache/coheigea/cxf/sts/xacml/authorization/xacml3/XACML3AuthorizingInterceptor.java, except it uses
 * AuthzForce native API for PDP evaluation instead of OpenAZ API.
 * 
 * For a SOAP Service, the resource-id Attribute refers to the "{serviceNamespace}serviceName#{operationNamespace}operationName" String (shortened to "{serviceNamespace}serviceName#operationName" if
 * the namespaces are identical). The "{serviceNamespace}serviceName", "{operationNamespace}operationName" and resource URI are also sent to simplify processing at the PDP side.
 * 
 * For a REST service the request URL is the resource. You can also configure the ability to send the truncated request URI instead for a SOAP or REST service. The current DateTime is also sent in an
 * Environment, however this can be disabled via configuration.
 */
public class LocalPdpBasedAuthzInterceptor extends AbstractPhaseInterceptor<Message>
{

	private static final Logger LOG = LogUtils.getL7dLogger(LocalPdpBasedAuthzInterceptor.class);

	private static final String defaultSOAPAction = "execute";

	private final BasePdpEngine pdp;

	/**
	 * Create Authorization interceptor (XACML PEP) using input {@code pdp} as XACML PDP
	 * 
	 * @param pdp
	 *            XACML PDP
	 */
	public LocalPdpBasedAuthzInterceptor(final BasePdpEngine pdp)
	{
		super(Phase.PRE_INVOKE);
		this.pdp = pdp;
	}

	@Override
	public void handleMessage(final Message message) throws Fault
	{
		final SecurityContext sc = message.get(SecurityContext.class);

		if (sc instanceof LoginSecurityContext)
		{
			final Principal principal = sc.getUserPrincipal();

			final LoginSecurityContext loginSecurityContext = (LoginSecurityContext) sc;
			final Set<Principal> principalRoles = loginSecurityContext.getUserRoles();
			final List<String> roles = new ArrayList<>();
			if (principalRoles != null)
			{
				for (final Principal p : principalRoles)
				{
					if (p != principal)
					{
						roles.add(p.getName());
					}
				}
			}

			try
			{
				if (authorize(principal, roles, message))
				{
					return;
				}
			}
			catch (final Exception e)
			{
				LOG.log(Level.FINE, "Unauthorized: " + e.getMessage(), e);
				throw new AccessDeniedException("Unauthorized");
			}
		}
		else
		{
			LOG.log(Level.FINE, "The SecurityContext was not an instance of LoginSecurityContext. No authorization is possible as a result");
		}

		throw new AccessDeniedException("Unauthorized");
	}

	protected boolean authorize(final Principal principal, final List<String> roles, final Message message) throws Exception
	{
		final ImmutablePdpDecisionRequest request = createRequest(principal, roles, message);
		// final String jsonRequest = JSONRequest.toString(request);
		if (LOG.isLoggable(Level.FINE))
		{
			LOG.fine("XACML Request: " + request);
		}

		// Evaluate the request
		final PdpDecisionResult result = pdp.evaluate(request);

		if (result == null)
		{
			return false;
		}

		// Handle any Obligations returned by the PDP
		handleObligations(request, principal, message, result);

		LOG.fine("XACML authorization result: " + result);
		final DecisionType decision = result.getDecision();
		return decision == DecisionType.PERMIT;
	}

	private ImmutablePdpDecisionRequest createRequest(final Principal principal, final List<String> roles, final Message message) throws WSSecurityException
	{
		final CXFMessageParser messageParser = new CXFMessageParser(message);
		final String issuer = messageParser.getIssuer();

		/*
		 * 3 attribute categories, 7 total attributes
		 */
		final PdpDecisionRequestBuilder<ImmutablePdpDecisionRequest> requestBuilder = pdp.newRequestBuilder(3, 7);

		// Subject ID
		final AttributeGUID subjectIdAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), issuer, XACMLAttributeId.XACML_1_0_SUBJECT_ID.value());
		final Bag<?> subjectIdAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(principal.getName()));
		requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

		// Subject role(s)
		if (roles != null)
		{
			final AttributeGUID subjectRoleAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), issuer, XACMLAttributeId.XACML_2_0_SUBJECT_ROLE.value());
			requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, createSubjectRoleAttributeValues(roles));
		}

		// Resource ID
		final AttributeGUID resourceIdAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_3_0_RESOURCE.value(), null, XACMLAttributeId.XACML_1_0_RESOURCE_ID.value());
		final Bag<?> resourceIdAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(getResourceId(messageParser)));
		requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);

		// Resource - WSDL-defined Service ID / Operation / Endpoint
		if (messageParser.isSOAPService())
		{
			// WSDL Service
			final QName wsdlService = messageParser.getWSDLService();
			if (wsdlService != null)
			{
				final AttributeGUID resourceServiceIdAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_3_0_RESOURCE.value(), null, XACMLConstants.RESOURCE_WSDL_SERVICE_ID);
				final Bag<?> resourceServiceIdAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(wsdlService.toString()));
				requestBuilder.putNamedAttributeIfAbsent(resourceServiceIdAttributeId, resourceServiceIdAttributeValues);
			}

			// WSDL Operation
			final QName wsdlOperation = messageParser.getWSDLOperation();
			final AttributeGUID resourceOperationIdAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_3_0_RESOURCE.value(), null, XACMLConstants.RESOURCE_WSDL_OPERATION_ID);
			final Bag<?> resourceOperationIddAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(wsdlOperation.toString()));
			requestBuilder.putNamedAttributeIfAbsent(resourceOperationIdAttributeId, resourceOperationIddAttributeValues);

			// WSDL Endpoint
			final String endpointURI = messageParser.getResourceURI(false);
			final AttributeGUID resourceWSDLEndpointAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_3_0_RESOURCE.value(), null, XACMLConstants.RESOURCE_WSDL_ENDPOINT);
			final Bag<?> resourceWSDLEndpointAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(endpointURI));
			requestBuilder.putNamedAttributeIfAbsent(resourceWSDLEndpointAttributeId, resourceWSDLEndpointAttributeValues);
		}

		// Action ID
		final String actionToUse = messageParser.getAction(defaultSOAPAction);
		final AttributeGUID actionIdAttributeId = new AttributeGUID(XACMLAttributeCategory.XACML_3_0_ACTION.value(), null, XACMLAttributeId.XACML_1_0_ACTION_ID.value());
		final Bag<?> actionIdAttributeValues = Bags.singleton(StandardDatatypes.STRING_FACTORY.getDatatype(), new StringValue(actionToUse));
		requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

		// Environment - current date/time will be set by the PDP

		return requestBuilder.build(false);
	}

	private static Bag<?> createSubjectRoleAttributeValues(final List<String> roles)
	{
		assert roles != null;

		final List<AnyURIValue> subjectRoleAttributeValues = new ArrayList<>(roles.size());
		for (final String role : roles)
		{
			subjectRoleAttributeValues.add(new AnyURIValue(role));
		}

		return Bags.getInstance(StandardDatatypes.ANYURI_FACTORY.getDatatype(), subjectRoleAttributeValues);
	}

	private static String getResourceId(final CXFMessageParser messageParser)
	{
		String resourceId = "";
		if (messageParser.isSOAPService())
		{
			final QName serviceName = messageParser.getWSDLService();
			final QName operationName = messageParser.getWSDLOperation();

			if (serviceName != null)
			{
				resourceId = serviceName.toString() + "#";
				if (serviceName.getNamespaceURI() != null && serviceName.getNamespaceURI().equals(operationName.getNamespaceURI()))
				{
					resourceId += operationName.getLocalPart();
				}
				else
				{
					resourceId += operationName.toString();
				}
			}
			else
			{
				resourceId = operationName.toString();
			}
		}
		else
		{
			resourceId = messageParser.getResourceURI(false);
		}

		return resourceId;
	}

	/**
	 * Handle any Obligations returned by the PDP
	 */
	protected void handleObligations(final PdpDecisionRequest request, final Principal principal, final Message message, final PdpDecisionResult result) throws Exception
	{
		// Do nothing by default
	}

}
