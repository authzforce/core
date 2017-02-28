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
package org.apache.coheigea.cxf.sts.xacml.common;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class CommonCallbackHandler implements CallbackHandler
{

	@Override
	public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException
	{
		for (final Callback callback : callbacks)
		{
			if (callback instanceof WSPasswordCallback)
			{ // CXF
				final WSPasswordCallback pc = (WSPasswordCallback) callback;
				if ("myclientkey".equals(pc.getIdentifier()))
				{
					pc.setPassword("ckpass");
					break;
				}
				else if ("myservicekey".equals(pc.getIdentifier()))
				{
					pc.setPassword("skpass");
					break;
				}
				else if ("alice".equals(pc.getIdentifier()))
				{
					pc.setPassword("security");
					break;
				}
				else if ("bob".equals(pc.getIdentifier()))
				{
					pc.setPassword("security");
					break;
				}
				else if ("mystskey".equals(pc.getIdentifier()))
				{
					pc.setPassword("stskpass");
					break;
				}
			}
		}
	}
}
