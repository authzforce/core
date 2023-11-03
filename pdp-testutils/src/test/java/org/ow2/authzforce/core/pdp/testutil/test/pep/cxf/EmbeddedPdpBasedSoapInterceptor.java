/*
 * Copyright 2012-2023 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.testutil.test.pep.cxf;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import java.util.Optional;

/**
 * This class represents a so-called XACML PEP that, for every CXF service request, creates an XACML 3.0 authorization decision Request to a PDP using AuthzForce's native API, given a Principal, list
 * of roles - typically coming from SAML token - and MessageContext. The principal name is inserted as the Subject ID, and the list of roles associated with that principal are inserted as Subject
 * roles. The action to send defaults to "execute". It is an adaptation of
 * <a href="https://github.com/coheigea/testcases/blob/master/apache/cxf/cxf-sts-xacml/src/test/java/org/apache/coheigea/cxf/sts/xacml/authorization/xacml3/XACML3AuthorizingInterceptor.java">XACML3AuthorizingInterceptor class from Apache CXF tests</a>, except it uses
 * AuthzForce native API for PDP evaluation instead of OpenAZ API.
 * <p>
 * For a SOAP Service, the resource-id Attribute refers to the "{serviceNamespace}serviceName#{operationNamespace}operationName" String (shortened to "{serviceNamespace}serviceName#operationName" if
 * the namespaces are identical). The "{serviceNamespace}serviceName", "{operationNamespace}operationName" and resource URI are also sent to simplify processing at the PDP side.
 * <p>
 * For a REST service the request URL is the resource. You can also configure the ability to send the truncated request URI instead for a SOAP or REST service.
 */
public class EmbeddedPdpBasedSoapInterceptor extends AbstractSoapInterceptor
{

	/**
	 * Create Authorization interceptor (XACML PEP) using input {@code pdp} as XACML PDP
	 *
	 */
	public EmbeddedPdpBasedSoapInterceptor()
	{
		super(Phase.PRE_STREAM);
	}

	private static void setHttpResponseStatus(final Message message, final int status)
	{
		// skip if inbound
		if (!MessageUtils.isOutbound(message))
		{
			return;
		}

		// outbound
		final Exchange exchange = message.getExchange();
		final Message outMessage = Optional.ofNullable(exchange.getOutMessage()).orElse(exchange.getOutFaultMessage());
		final HttpServletResponse httpResponse = (HttpServletResponse) outMessage.get(AbstractHTTPDestination.HTTP_RESPONSE);
		if (httpResponse != null)
		{
			httpResponse.setStatus(status);
		}
		/*
		 * else this is likely SOAP over some non-HTTP transport protocol
		 */

		/*
		 * Prevent any further processing by other interceptors
		 */
		message.getInterceptorChain().abort();

	}

	@Override
	public void handleMessage(final SoapMessage message) throws Fault
	{
		// example setting HTTP status to 400
		setHttpResponseStatus(message, 400);
	}

	@Override
	public void handleFault(final SoapMessage message)
	{
		// example setting HTTP status to 400
		setHttpResponseStatus(message, 400);
	}

}
