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
package org.ow2.authzforce.core.pdp.impl.test.pep.cxf;

import static org.ow2.authzforce.core.pdp.api.value.StandardDatatypes.ANYURI_FACTORY;
import static org.ow2.authzforce.core.pdp.api.value.StandardDatatypes.STRING_FACTORY;
import static org.ow2.authzforce.xacml.identifiers.XACMLAttributeCategory.XACML_1_0_ACCESS_SUBJECT;
import static org.ow2.authzforce.xacml.identifiers.XACMLAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XACMLAttributeCategory.XACML_3_0_RESOURCE;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

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
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.ImmutablePdpDecisionRequest;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;
import org.slf4j.LoggerFactory;

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
 * For a REST service the request URL is the resource. You can also configure the ability to send the truncated request URI instead for a SOAP or REST service.
 */
public class EmbeddedPdpBasedAuthzInterceptor extends AbstractPhaseInterceptor<Message>
{

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmbeddedPdpBasedAuthzInterceptor.class);

	private static final String defaultSOAPAction = "execute";

	private final BasePdpEngine pdp;

	/**
	 * Create Authorization interceptor (XACML PEP) using input {@code pdp} as XACML PDP
	 * 
	 * @param pdp
	 *            XACML PDP
	 */
	public EmbeddedPdpBasedAuthzInterceptor(final BasePdpEngine pdp)
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
			final Set<String> roles;
			if (principalRoles == null)
			{
				roles = Collections.emptySet();
			}
			else
			{
				roles = HashCollections.newUpdatableSet(principalRoles.size());
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
				LOGGER.debug("Unauthorized", e);
				throw new AccessDeniedException("Unauthorized");
			}
		}
		else
		{
			LOGGER.debug("The SecurityContext was not an instance of LoginSecurityContext. No authorization is possible as a result");
		}

		throw new AccessDeniedException("Unauthorized");
	}

	protected boolean authorize(final Principal principal, final Set<String> roles, final Message message) throws Exception
	{
		final ImmutablePdpDecisionRequest request = createRequest(principal, roles, message);
		LOGGER.debug("XACML Request: {}", request);

		// Evaluate the request
		final PdpDecisionResult result = pdp.evaluate(request);

		if (result == null)
		{
			return false;
		}

		// Handle any Obligations returned by the PDP
		handleObligationsOrAdvice(request, principal, message, result);

		LOGGER.debug("XACML authorization result: {}", result);
		return result.getDecision() == DecisionType.PERMIT;
	}

	private ImmutablePdpDecisionRequest createRequest(final Principal principal, final Set<String> roles, final Message message) throws WSSecurityException
	{
		assert roles != null;

		final CXFMessageParser messageParser = new CXFMessageParser(message);
		final String issuer = messageParser.getIssuer();

		/*
		 * 3 attribute categories, 7 total attributes
		 */
		final PdpDecisionRequestBuilder<ImmutablePdpDecisionRequest> requestBuilder = pdp.newRequestBuilder(3, 7);

		// Subject ID
		final AttributeGUID subjectIdAttributeId = new AttributeGUID(XACML_1_0_ACCESS_SUBJECT.value(), Optional.ofNullable(issuer), XACMLAttributeId.XACML_1_0_SUBJECT_ID.value());
		final Bag<?> subjectIdAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(principal.getName()));
		requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

		// Subject role(s)
		final AttributeGUID subjectRoleAttributeId = new AttributeGUID(XACML_1_0_ACCESS_SUBJECT.value(), Optional.ofNullable(issuer), XACMLAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, stringsToAnyURIBag(roles));

		// Resource ID
		final AttributeGUID resourceIdAttributeId = new AttributeGUID(XACML_3_0_RESOURCE.value(), Optional.empty(), XACMLAttributeId.XACML_1_0_RESOURCE_ID.value());
		final Bag<?> resourceIdAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(getResourceId(messageParser)));
		requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);

		// Resource - WSDL-defined Service ID / Operation / Endpoint
		if (messageParser.isSOAPService())
		{
			// WSDL Service
			final QName wsdlService = messageParser.getWSDLService();
			if (wsdlService != null)
			{
				final AttributeGUID resourceServiceIdAttributeId = new AttributeGUID(XACML_3_0_RESOURCE.value(), Optional.empty(), XACMLConstants.RESOURCE_WSDL_SERVICE_ID);
				final Bag<?> resourceServiceIdAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(wsdlService.toString()));
				requestBuilder.putNamedAttributeIfAbsent(resourceServiceIdAttributeId, resourceServiceIdAttributeValues);
			}

			// WSDL Operation
			final QName wsdlOperation = messageParser.getWSDLOperation();
			final AttributeGUID resourceOperationIdAttributeId = new AttributeGUID(XACML_3_0_RESOURCE.value(), Optional.empty(), XACMLConstants.RESOURCE_WSDL_OPERATION_ID);
			final Bag<?> resourceOperationIddAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(wsdlOperation.toString()));
			requestBuilder.putNamedAttributeIfAbsent(resourceOperationIdAttributeId, resourceOperationIddAttributeValues);

			// WSDL Endpoint
			final String endpointURI = messageParser.getResourceURI(false);
			final AttributeGUID resourceWSDLEndpointAttributeId = new AttributeGUID(XACML_3_0_RESOURCE.value(), Optional.empty(), XACMLConstants.RESOURCE_WSDL_ENDPOINT);
			final Bag<?> resourceWSDLEndpointAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(endpointURI));
			requestBuilder.putNamedAttributeIfAbsent(resourceWSDLEndpointAttributeId, resourceWSDLEndpointAttributeValues);
		}

		// Action ID
		final String actionToUse = messageParser.getAction(defaultSOAPAction);
		final AttributeGUID actionIdAttributeId = new AttributeGUID(XACML_3_0_ACTION.value(), Optional.empty(), XACMLAttributeId.XACML_1_0_ACTION_ID.value());
		final Bag<?> actionIdAttributeValues = Bags.singleton(STRING_FACTORY.getDatatype(), new StringValue(actionToUse));
		requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

		// Environment - current date/time will be set by the PDP
		return requestBuilder.build(false);
	}

	private static Bag<?> stringsToAnyURIBag(final Set<String> strings)
	{
		assert strings != null;

		final Set<AnyURIValue> anyURIs = HashCollections.newUpdatableSet(strings.size());
		for (final String string : strings)
		{
			anyURIs.add(new AnyURIValue(string));
		}

		return Bags.getInstance(ANYURI_FACTORY.getDatatype(), anyURIs);
	}

	private static String getResourceId(final CXFMessageParser messageParser)
	{
		final String resourceId;
		if (messageParser.isSOAPService())
		{
			final QName serviceName = messageParser.getWSDLService();
			final QName operationName = messageParser.getWSDLOperation();

			if (serviceName != null)
			{
				final String resourceIdPrefix = serviceName.toString() + "#";
				if (serviceName.getNamespaceURI() != null && serviceName.getNamespaceURI().equals(operationName.getNamespaceURI()))
				{
					resourceId = resourceIdPrefix + operationName.getLocalPart();
				}
				else
				{
					resourceId = resourceIdPrefix + operationName.toString();
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
	 * Handle any Obligations returned by the PDP. Does nothing by default. Override this method if you want to handle Obligations/Advice in a specific way
	 */
	protected void handleObligationsOrAdvice(final PdpDecisionRequest request, final Principal principal, final Message message, final PdpDecisionResult result) throws Exception
	{
		// Do nothing by default
	}

}
