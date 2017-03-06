/**
 * Copyright 2012-2017 Thales Services SAS.
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

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.coheigea.cxf.sts.xacml.common.STSServer;
import org.apache.coheigea.cxf.sts.xacml.common.TokenTestUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.example.contract.doubleit.DoubleItPortType;
import org.junit.BeforeClass;

/**
 * This test is an adaptation of <a href="https://github.com/coheigea/testcases/tree/master/apache/cxf/cxf-sts-xacml">cxf-sts-xacml test</a> for AuthzForce PDP. The client authenticates to the STS
 * using a username/password, and gets a signed holder-of-key SAML Assertion in return. This is presented to the service, who verifies proof-of-possession + the signature of the STS on the assertion.
 * The CXF endpoint extracts roles from the Assertion + populates the security context. Note that the CXF endpoint requires a "role" Claim via the security policy.
 *
 * The CXF Endpoint has configured the {@link EmbeddedPdpBasedAuthzInterceptor}, which creates a XACML 3.0 request for dispatch to the (co-located) PDP, and then enforces the PDP's decision.
 * 
 * <p>
 * Detailed description:
 * </p>
 * <p>
 * In this test, the client obtains a SAML Token from the STS with the roles of the client embedded in the token. The service provider extracts the roles, and creates a XACML request. This is
 * evaluated by a AuthzForce-based PDP which is co-located with the service. After evaluating the request, the PDP response is then enforced at the service side.
 * </p>
 * <p>
 * The CXF associates the following users with roles:
 * <ul>
 * <li>"alice/boss+employee"</li>
 * <li>"bob/employee"</li>
 * </ul>
 * The client authenticates to the STS using a username/password, and gets a signed holder-of-key SAML Assertion in return. This is presented to the service, who verifies proof-of-possession + the
 * signature of the STS on the assertion. The CXF endpoint extracts roles from the Assertion + populates the security context. Note that the CXF endpoint requires a "role" Claim via the security
 * policy.
 * </p>
 * <p>
 * The CXF Endpoint set up the XACML 3.0 interceptor to create a XACML request for dispatch to the PDP, and then enforces the PDP's decision.
 * </p>
 * <p>
 * The XACML 3.0 PDP evaluates the requests against some policies which are loaded into the PDP. It uses policies based on the RBAC profile of XACML. The user must have role "boss" to "execute" on the
 * service "{http://www.example.org/contract/DoubleIt}DoubleItService#DoubleIt".
 * </p>
 */
public class EmbeddedPdpBasedAuthzInterceptorTest extends AbstractBusClientServerTestBase
{

	private static final String NAMESPACE = "http://www.example.org/contract/DoubleIt";
	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "DoubleItService");

	private static final String PORT = allocatePort(Server.class);
	private static final String STS_PORT = allocatePort(STSServer.class);

	@BeforeClass
	public static void startServers() throws Exception
	{
		assertTrue("Server failed to launch",
		// run the server for the target service (DoubleIt) in the same process
		// set this to false to fork
				launchServer(Server.class, true));
		assertTrue("Server failed to launch",
		// run the server in the same process
		// set this to false to fork
				launchServer(STSServer.class, true));
	}

	@org.junit.Test
	public void testAuthorizedRequest() throws Exception
	{

		final SpringBusFactory bf = new SpringBusFactory();
		final URL busFile = EmbeddedPdpBasedAuthzInterceptorTest.class.getResource("cxf-client.xml");

		final Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		final URL wsdl = EmbeddedPdpBasedAuthzInterceptorTest.class.getResource("DoubleItSecure.wsdl");
		final Service service = Service.create(wsdl, SERVICE_QNAME);
		final QName portQName = new QName(NAMESPACE, "DoubleItTransportPort");
		final DoubleItPortType transportPort = service.getPort(portQName, DoubleItPortType.class);
		updateAddressPort(transportPort, PORT);

		final Client client = ClientProxy.getClient(transportPort);
		client.getRequestContext().put("ws-security.username", "alice");

		TokenTestUtils.updateSTSPort((BindingProvider) transportPort, STS_PORT);

		doubleIt(transportPort, 25);
	}

	@org.junit.Test
	public void testUnauthorizedRequest() throws Exception
	{

		final SpringBusFactory bf = new SpringBusFactory();
		final URL busFile = EmbeddedPdpBasedAuthzInterceptorTest.class.getResource("cxf-client.xml");

		final Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		final URL wsdl = EmbeddedPdpBasedAuthzInterceptorTest.class.getResource("DoubleItSecure.wsdl");
		final Service service = Service.create(wsdl, SERVICE_QNAME);
		final QName portQName = new QName(NAMESPACE, "DoubleItTransportPort");
		final DoubleItPortType transportPort = service.getPort(portQName, DoubleItPortType.class);
		updateAddressPort(transportPort, PORT);

		final Client client = ClientProxy.getClient(transportPort);
		client.getRequestContext().put("ws-security.username", "bob");

		TokenTestUtils.updateSTSPort((BindingProvider) transportPort, STS_PORT);

		try
		{
			doubleIt(transportPort, 25);
			fail("Failure expected on bob");
		}
		catch (final Exception ex)
		{
			// expected
		}
	}

	private static void doubleIt(final DoubleItPortType port, final int numToDouble)
	{
		final int resp = port.doubleIt(numToDouble);
		assertEquals(numToDouble * 2, resp);
	}

}
