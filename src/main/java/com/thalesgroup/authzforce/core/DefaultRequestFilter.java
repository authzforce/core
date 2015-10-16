/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.CategorySpecificAttributes;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.Expression.Utils;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Default Request filter for Individual Decision Requests only (no support of Multiple Decision
 * Profile in particular)
 * 
 */
public final class DefaultRequestFilter extends RequestFilter
{
	/**
	 * Factory for creating instances of DefaultRequestFilter
	 * 
	 */
	public static final RequestFilter.Factory FACTORY = new RequestFilter.Factory()
	{
		private static final String ID = "urn:thalesgroup:xacml:request-filter:default";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
		{
			return new DefaultRequestFilter(datatypeFactoryRegistry, requireContentForXPath, attributesContentJaxbCtx, xmlProcessor);
		}
	};

	/**
	 * Creates instance
	 * 
	 * @param datatypeFactoryRegistry
	 *            registry of attribute datatype factories for parsing XACML Request AttributeValues
	 *            into Java types compatible with/optimized for the policy evaluation engine
	 * @param requireContentForXPath
	 *            true iff XPath evaluation against Attributes/Content element is required (e.g.
	 *            AttributeSelector, XPath-based funtion...). A preprocessor may skip Content
	 *            parsing for XPath evaluation, if and only if this is false. (Be aware that a
	 *            preprocessor may support the MultipleDecision Profile or Hierarchical Profile and
	 *            therefore require Content parsing for other purposes defined by these profiles.)
	 * @param attributesContentJaxbCtx
	 *            JAXBContext that was used to unmarshall Attributes/Content elements in the
	 *            Request. This context is used to create a new instance of marshaller to pass it as
	 *            JAXBSource to xmlProcesor to convert to XDM data model for XPATH evaluation. May
	 *            be null if requireContentForXPath is false.
	 * 
	 * @param xmlProcessor
	 *            XML processor for parsing Attributes/Content prior to XPATH evaluation (e.g.
	 *            AttributeSelectors). May be null if requireContentForXPath is false.
	 */
	public DefaultRequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, requireContentForXPath, attributesContentJaxbCtx, xmlProcessor);
	}

	@Override
	public List<IndividualDecisionRequest> filter(Request jaxbRequest) throws IndeterminateEvaluationException
	{
		// MultiRequests element not supported (optional XACML feature)
		if (jaxbRequest.getMultiRequests() != null)
		{
			/*
			 * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error
			 * code for unsupported element
			 */
			throw UNSUPPORTED_MULTI_REQUESTS_EXCEPTION;
		}

		// RequestDefaults element is supported for XPath expressions (optional XACML feature)
		final RequestDefaults reqDefs = jaxbRequest.getRequestDefaults();
		final XPathCompiler reqDefXPathCompiler;
		if (reqDefs == null)
		{
			reqDefXPathCompiler = null;
		} else
		{
			reqDefXPathCompiler = Utils.XPATH_COMPILERS_BY_VERSION.get(reqDefs.getXPathVersion());
			if (reqDefXPathCompiler == null)
			{
				throw new IndeterminateEvaluationException("Invalid <RequestDefaults>/XPathVersion: " + reqDefs.getXPathVersion(), Status.STATUS_SYNTAX_ERROR);
			}
		}

		/*
		 * No support for Multiple Decision Profile -> no support for repeated categories as
		 * specified in Multiple Decision Profile. So we keep track of attribute categories to check
		 * duplicates.
		 */
		final Set<String> attrCategoryNames = new HashSet<>();
		final XACMLAttributesParser xacmlAttrsParser = getXACMLAttributesParserInstance();
		final IndividualDecisionRequest individualDecisionRequest;
		try
		{
			individualDecisionRequest = new IndividualDecisionRequest(jaxbRequest.isReturnPolicyIdList(), reqDefXPathCompiler);
		} catch (IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion", Status.STATUS_SYNTAX_ERROR, e);
		}

		for (final Attributes jaxbAttributes : jaxbRequest.getAttributes())
		{
			final String categoryName = jaxbAttributes.getCategory();
			if (!attrCategoryNames.add(categoryName))
			{
				throw new IndeterminateEvaluationException("Unsupported repetition of Attributes[@Category='" + categoryName + "'] (feature 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories' is not supported)", Status.STATUS_SYNTAX_ERROR);
			}

			final CategorySpecificAttributes categorySpecificAttributes = xacmlAttrsParser.parse(jaxbAttributes, reqDefXPathCompiler);
			if (categorySpecificAttributes == null)
			{
				// skip this empty Attributes
				continue;
			}

			individualDecisionRequest.put(categoryName, categorySpecificAttributes);
		}

		return Collections.singletonList(individualDecisionRequest);
	}
}
