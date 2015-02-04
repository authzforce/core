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
package com.sun.xacml.ctx;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.Indenter;
import com.thalesgroup.authzforce.core.PdpModelHandler;


/**
 * Represents the response to a request made to the XACML PDP.
 *
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public class ResponseCtx
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCtx.class);

    // The set of Result objects returned by the PDP
    private final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> results;

    /**
     * Constructor that creates a new <code>ResponseCtx</code> with only a
     * single <code>Result</code> (a common case).
     *
     * @param result the single result in the response
     */
    public ResponseCtx(Result result) {
        results = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result>singletonList(result);
    }
    
    /**
     * Constructor that creates a new <code>ResponseCtx</code> with a
     * <code>Set</code> of <code>Result</code>s. The <code>Set</code> must
     * be non-empty.
     *
     * @param results a <code>Set</code> of <code>Result</code> objects
     */
    public ResponseCtx(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> results) {
        this.results = results;
    }

    /**
     * Get the set of <code>Result</code>s from this response.
     * 
     * @return a <code>Set</code> of results
     */
    public List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> getResults() {
        return results;
    }

    /**
     * Encodes this context into its XML representation and writes this
     * encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this context into its XML representation and writes
     * this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
    	final Response resp = new Response();
    	for(final oasis.names.tc.xacml._3_0.core.schema.wd_17.Result result: results) {
        	resp.getResults().add(result);
    	}

    	PrintStream out = new PrintStream(output);
		try
		{
			Marshaller marshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(resp, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Response", e);
		}
    }
    
    /** 
     * Return encoded context of XML representation
     *
     * @return as String 
     */
    public String getEncoded() {

        OutputStream output = new ByteArrayOutputStream();
        encode(output, new Indenter(0));
        return output.toString();
    } 

}
