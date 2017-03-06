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

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;

public class Server extends AbstractBusTestServerBase
{

	public Server()
	{

	}

	@Override
	protected void run()
	{
		final URL busFile = Server.class.getResource("cxf-doubleit-service.xml");
		final Bus busLocal = new SpringBusFactory().createBus(busFile);
		BusFactory.setDefaultBus(busLocal);
		setBus(busLocal);

		try
		{
			new Server();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
