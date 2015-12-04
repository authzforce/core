/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

/**
 * This is the interface for the XACML PDP engines, providing the starting point for request evaluation.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public interface PDP
{

	/**
	 * Evaluates the request against the policies known to this PDP. This is really the core method of the entire XACML specification, and for most people will
	 * provide what you want.
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required attribute, it was using an unsupported scope, etc), then the result will be a
	 * decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context
	 *            for XPath evaluation
	 * @return the response to the request
	 */
	Response evaluate(Request request, Map<String, String> namespaceURIsByPrefix);

	/**
	 * Evaluates the request against the policies known to this PDP. Equivalent to {@link #evaluate(Request, Map)} with second parameter set to null.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return the response to the request
	 */
	Response evaluate(Request request);

}
