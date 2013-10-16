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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Romain Ferrari
 */
public class BindingUtility {
	
	/**
	 * JAXB context for (un)marshalling from/to JAXB objects derived from XACML 3.0 schema
	 */	
    public static final JAXBContext XACML30_JAXB_CONTEXT;
    static {
    	try
		{
			XACML30_JAXB_CONTEXT = JAXBContext.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17", BindingUtility.class.getClassLoader());
		} catch (JAXBException e)
		{
			throw new RuntimeException("Error instantiating JAXB context for (un)marshalling from/to XACML 3.0 objects", e);
		}
    }
    
    /**
	 * Thread-local namespace-aware document builder to build DOM elements from XML strings.
	 * DocumentBuilder(Factory) is not guaranteed to be thread-safe, therefore this thread local
	 * variable. 
	 * 
	 * WARNING: the application must call {@link DocumentBuilder#reset()} on the instance result of
	 * {@link ThreadLocal#get()} on {@link #THREAD_LOCAL_NS_AWARE_DOC_BUILDER}, after calling
	 * one of parse(...) or {@link DocumentBuilder#newDocument()} methods to preserve thread-safety.
	 */
	private static final ThreadLocal<DocumentBuilder> THREAD_LOCAL_NS_AWARE_DOC_BUILDER = new ThreadLocal<DocumentBuilder>()
	{
		@Override
		protected DocumentBuilder initialValue()
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setIgnoringComments(true);
			final DocumentBuilder docBuilder;
			try
			{
				docBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e)
			{
				throw new RuntimeException("Failed to initialize namespace-aware DOM DocumentBuilder for parsing XML to DOM documents", e);
			}

			return docBuilder;
		}
	};
	
	/**
	 * Thread-local namespace-unaware document builder to build DOM elements from XML strings.
	 * DocumentBuilder(Factory) is not guaranteed to be thread-safe, therefore this thread local
	 * variable. 
	 * 
	 * WARNING: the application must call {@link DocumentBuilder#reset()} on the instance result of
	 * {@link ThreadLocal#get()} on {@link #THREAD_LOCAL_NS_AWARE_DOC_BUILDER}, after calling
	 * one of parse(...) methods to preserve thread-safety.
	 */
	private static final ThreadLocal<DocumentBuilder> THREAD_LOCAL_NS_UNAWARE_DOC_BUILDER = new ThreadLocal<DocumentBuilder>()
	{
		@Override
		protected DocumentBuilder initialValue()
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setIgnoringComments(true);
			final DocumentBuilder docBuilder;
			try
			{
				docBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e)
			{
				throw new RuntimeException("Failed to initialize namespace-aware DOM DocumentBuilder for parsing XML to DOM documents", e);
			}

			return docBuilder;
		}
	};

    public static  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory contextFac =
            new  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory();
//    public static oasis.names.tc.xacml._2_0.policy.schema.os.ObjectFactory policyFac =
//            new oasis.names.tc.xacml._2_0.policy.schema.os.ObjectFactory();
    public static  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory policyFac =
            new  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory();
    

    //public static String jaxbContextPath = "net.opengis.gml.v_3_2_1:org.isotc211.iso19139.d_2007_04_17.gco:org.isotc211.iso19139.d_2007_04_17.gmd:org.isotc211.iso19139.d_2007_04_17.gmx:org.isotc211.iso19139.d_2007_04_17.gsr:org.isotc211.iso19139.d_2007_04_17.gss:org.isotc211.iso19139.d_2007_04_17.gts";
    
//    public static Unmarshaller getUnmarshaller() throws JAXBException {
//        Unmarshaller unmarshaller = XACML30_JAXB_CONTEXT.createUnmarshaller();
//
//        //unmarshaller.setValidating(true);
//        unmarshaller.setEventHandler(new ValidationEventHandler() {
//
//            public boolean handleEvent(ValidationEvent event) {
//                boolean keepOn = false;
//
//                return keepOn;
//            }
//        });
//
//        return unmarshaller;
//    }

    
    /**
     * Get thread-local document builder. The client must call the {@link DocumentBuilder#reset()} after calling any parse() methods to preserve thread-safety..
     * @param namespaceAware true if and only returned documentBuilder must be namespace-aware
     * @return thread-local instance of DocumentBuilder (thread-local)
     */
    public static DocumentBuilder getDocumentBuilder(boolean namespaceAware){
    	final DocumentBuilder docBuilder;
    	if(namespaceAware) {
    		docBuilder = THREAD_LOCAL_NS_AWARE_DOC_BUILDER.get();
    	} else {
    		docBuilder = THREAD_LOCAL_NS_UNAWARE_DOC_BUILDER.get();
    	}
    	
    	return docBuilder;
    }
    
}
