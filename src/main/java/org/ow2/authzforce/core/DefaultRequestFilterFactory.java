package org.ow2.authzforce.core;

import javax.xml.bind.JAXBContext;

import net.sf.saxon.s9api.Processor;

import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;

final class DefaultRequestFilterFactory implements RequestFilter.Factory
{
	private static final String ID = "urn:thalesgroup:xacml:request-filter:default";

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx,
			Processor xmlProcessor)
	{
		return new DefaultRequestFilter(datatypeFactoryRegistry, requireContentForXPath, attributesContentJaxbCtx, xmlProcessor);
	}

	/**
	 * Factory for creating instances of DefaultRequestFilter
	 * 
	 */
	public static final RequestFilter.Factory INSTANCE = new DefaultRequestFilterFactory();
}