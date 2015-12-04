/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core;

import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;

import org.ow2.authzforce.core.XACMLParsers.ContentSkippingJaxbXACMLAttributesParserFactory;
import org.ow2.authzforce.core.XACMLParsers.FullJaxbXACMLAttributesParserFactory;
import org.ow2.authzforce.core.XACMLParsers.IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser;
import org.ow2.authzforce.core.XACMLParsers.JaxbXACMLAttributeParser;
import org.ow2.authzforce.core.XACMLParsers.JaxbXACMLAttributesParser;
import org.ow2.authzforce.core.XACMLParsers.JaxbXACMLAttributesParserFactory;
import org.ow2.authzforce.core.XACMLParsers.NonIssuedLikeIssuedLaxJaxbXACMLAttributeParser;
import org.ow2.authzforce.core.XACMLParsers.NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser;
import org.ow2.authzforce.core.expression.Expressions;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;

/**
 * Convenient base class for {@link RequestFilter} implementations
 * 
 */
public abstract class BaseRequestFilter implements RequestFilter
{
	private static final UnsupportedOperationException UNSUPPORTED_MODE_EXCEPTION = new UnsupportedOperationException(
			"Unsupported BaseRequestFilter mode: allowAttributeDuplicates == false && strictAttributeIssuerMatch == false");

	/**
	 * Indeterminate exception to be thrown iff RequestDefaults element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_REQUEST_DEFAULTS_EXCEPTION = new IndeterminateEvaluationException(
			"Unsupported feature: <RequestDefaults>", StatusHelper.STATUS_SYNTAX_ERROR);

	/**
	 * Indeterminate exception to be thrown iff MultiRequests element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTI_REQUESTS_EXCEPTION = new IndeterminateEvaluationException(
			"Unsupported feature: <MultiRequests>", StatusHelper.STATUS_SYNTAX_ERROR);

	private final JaxbXACMLAttributesParserFactory<?> xacmlAttrsParserFactory;

	/**
	 * Creates instance of request filter.
	 * 
	 * @param datatypeFactoryRegistry
	 *            registry of factories for attribute datatypes
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant
	 *            with XACML 3.0, §5.29, in the case that the Issuer is not present; but it performs better and is recommended when all AttributeDesignators
	 *            have an Issuer (best practice). Set it to false, if you want full compliance with the XACML 3.0 Attribute Evaluation: "If the Issuer is not
	 *            present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType
	 *            attributes alone."
	 * @param allowAttributeDuplicates
	 *            true iff the filter should allow defining multi-valued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML
	 *            Attributes element (same Category). Indeed, not allowing this is not fully compliant with the XACML spec according to a discussion on the
	 *            xacml-dev mailing list (see {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), referring to the XACML 3.0
	 *            core spec, §7.3.3, that indicates that multiple occurrences of the same &lt;Attribute&gt; with same meta-data but different values should be
	 *            considered equivalent to a single &lt;Attribute&gt; element with same meta-data and merged values (multi-valued Attribute). Moreover, the
	 *            XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in a multi-value bag
	 *            during evaluation of the &lt;AttributeDesignator&gt;.
	 *            <p>
	 *            Setting this parameter to {@code false} is not fully compliant, but provides better performance, especially if you know the Requests to be
	 *            well-formed, i.e. all AttributeValues of a given Attribute are grouped together in the same &lt;Attribute&gt; element. Combined with
	 *            {@code strictAttributeIssuerMatch == true}, this is the most efficient alternative (although not fully compliant).
	 * @param requireContentForXPath
	 *            true iff Attributes/Content parsing (into XDM) for XPath evaluation is required
	 * 
	 * @param xmlProcessor
	 *            XML processor for parsing Attributes/Content elements into XDM for XPath evaluation. May be null if {@code requireContentForXPath} is false.
	 * @throws UnsupportedOperationException
	 *             if {@code strictAttributeIssuerMatch == false && allowAttributeDuplicates == false} which is not supported
	 */
	protected BaseRequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean allowAttributeDuplicates,
			boolean requireContentForXPath, Processor xmlProcessor) throws UnsupportedOperationException
	{

		if (allowAttributeDuplicates)
		{
			final JaxbXACMLAttributeParser<MutableBag<?>> xacmlAttributeParser = strictAttributeIssuerMatch ? new NonIssuedLikeIssuedLaxJaxbXACMLAttributeParser(
					datatypeFactoryRegistry) : new IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser(datatypeFactoryRegistry);
			this.xacmlAttrsParserFactory = requireContentForXPath ? new FullJaxbXACMLAttributesParserFactory<>(xacmlAttributeParser,
					SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER, xmlProcessor)
					: new ContentSkippingJaxbXACMLAttributesParserFactory<>(xacmlAttributeParser,
							SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER);
		} else // allowAttributeDuplicates == false
		if (strictAttributeIssuerMatch)
		{
			final JaxbXACMLAttributeParser<Bag<?>> xacmlAttributeParser = new NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser(datatypeFactoryRegistry);
			this.xacmlAttrsParserFactory = requireContentForXPath ? new FullJaxbXACMLAttributesParserFactory<>(xacmlAttributeParser,
					SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER, xmlProcessor) : new ContentSkippingJaxbXACMLAttributesParserFactory<>(
					xacmlAttributeParser, SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER);
		} else
		{
			/*
			 * allowAttributeDuplicates == false && strictAttributeIssuerMatch == false is not supported, because it would require using mutable bags for
			 * "Issuer-less" attributes (updated for each possible Attribute with same meta-data except a defined Issuer), whereas the goal of
			 * 'allowAttributeDuplicates == false' is to use immutable Bags in the first place, i.e. avoid going through mutable bags. A solution would consist
			 * to create two collections of attributes, one with immutable bags, and the other with mutable ones for Issuer-less attributes. However, we
			 * consider it is not worth providing an implementation for this natively, so far. Can always been a custom RequestFilter provided externally.
			 */
			throw UNSUPPORTED_MODE_EXCEPTION;
		}

	}

	@Override
	public final List<? extends IndividualDecisionRequest> filter(Request jaxbRequest, Map<String, String> namespaceURIsByPrefix)
			throws IndeterminateEvaluationException
	{
		/*
		 * No support for MultiRequests (§2.4 of Multiple Decision Profile).
		 */
		if (jaxbRequest.getMultiRequests() != null)
		{
			/*
			 * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error code for unsupported element
			 */
			throw UNSUPPORTED_MULTI_REQUESTS_EXCEPTION;
		}

		final RequestDefaults jaxbReqDefaults = jaxbRequest.getRequestDefaults();
		final XPathCompiler xPathCompiler = jaxbReqDefaults == null ? null : Expressions.newXPathCompiler(jaxbReqDefaults.getXPathVersion(),
				namespaceURIsByPrefix);
		final JaxbXACMLAttributesParser xacmlAttrsParser = xacmlAttrsParserFactory.getInstance();
		return filter(jaxbRequest.getAttributes(), xacmlAttrsParser, jaxbRequest.isReturnPolicyIdList(), jaxbRequest.isCombinedDecision(), xPathCompiler,
				namespaceURIsByPrefix);
	}

	/**
	 * Filters (validates and/or transforms) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile
	 * or Hierarchical Resource profile
	 * 
	 * @param attributesList
	 *            list of XACML Request Attributes elements
	 * @param xacmlAttrsParser
	 *            XACML Attributes element Parser instance, used to parse each Attributes in {@code attributesList}.
	 *
	 * @param isApplicablePolicyIdListReturned
	 *            XACML Request's property {@code returnPolicyIdList}.
	 * @param combinedDecision
	 *            XACML Request's property {@code isCombinedDecision}
	 * @param xPathCompiler
	 *            xpathExpression compiler, corresponding to the XACML RequestDefaults element, or null if no RequestDefaults element.
	 * 
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context
	 *            for XPath evaluation
	 * 
	 * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by
	 *         the filter
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this filter
	 */
	public abstract List<? extends IndividualDecisionRequest> filter(List<Attributes> attributesList, JaxbXACMLAttributesParser xacmlAttrsParser,
			boolean isApplicablePolicyIdListReturned, boolean combinedDecision, XPathCompiler xPathCompiler, Map<String, String> namespaceURIsByPrefix)
			throws IndeterminateEvaluationException;

}
