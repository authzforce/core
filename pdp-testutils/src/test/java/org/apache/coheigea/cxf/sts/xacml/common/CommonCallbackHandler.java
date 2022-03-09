package org.apache.coheigea.cxf.sts.xacml.common;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class CommonCallbackHandler implements CallbackHandler
{

	@Override
	public void handle(final Callback[] callbacks)
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
