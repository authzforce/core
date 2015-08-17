package com.thalesgroup.authzforce.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import javax.xml.bind.JAXBContext;

import net.sf.saxon.s9api.Processor;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import com.thalesgroup.authzforce.core.attr.CategorySpecificAttributes;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Request filter implementing Multiple Decision Request, section 2.3 (repeated attribute
 * categories).
 * 
 */
public final class MultiDecisionRequestFilter extends RequestFilter
{
	/**
	 * RefPolicyFinderModuleFactory for creating instances of DefaultRequestFilter
	 * 
	 */
	public static class Factory implements RequestFilter.Factory
	{
		private static final String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
		{
			return new MultiDecisionRequestFilter(datatypeFactoryRegistry, requireContentForXPath, attributesContentJaxbCtx, xmlProcessor);
		}

	}

	// private static Logger LOGGER = LoggerFactory.getLogger(MultiDecisionRequestFilter.class);

	private MultiDecisionRequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, requireContentForXPath, attributesContentJaxbCtx, xmlProcessor);
	}

	@Override
	public final List<IndividualDecisionRequest> filter(Request request) throws IndeterminateEvaluationException
	{
		// RequestDefaults element not supported (optional XACML feature)
		if (request.getRequestDefaults() != null)
		{
			/*
			 * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error
			 * code for unsupported element
			 */
			throw UNSUPPORTED_REQUEST_DEFAULTS_EXCEPTION;
		}

		/*
		 * No support for MultiRequests (§2.4 of Multiple Decision Profile). We only support §2.3
		 * for now.
		 */
		if (request.getMultiRequests() != null)
		{
			/*
			 * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error
			 * code for unsupported element
			 */
			throw UNSUPPORTED_MULTI_REQUESTS_EXCEPTION;
		}

		/*
		 * Parse Request attributes and group possibly repeated categories to implement Multiple
		 * Decision Profile, §2.3.
		 */
		final XACMLAttributesParser xacmlAttrsParser = getXACMLAttributesParserInstance();
		final Map<String, Queue<CategorySpecificAttributes>> multiReqAttrAlternativesByCategory = new HashMap<>();
		for (final Attributes jaxbAttributes : request.getAttributes())
		{
			final String categoryName = jaxbAttributes.getCategory();
			final CategorySpecificAttributes categoryAttributesAlternative = xacmlAttrsParser.parse(jaxbAttributes);
			if (categoryAttributesAlternative == null)
			{
				// skip this empty Attributes
				continue;
			}

			final Queue<CategorySpecificAttributes> oldAttrAlternatives = multiReqAttrAlternativesByCategory.get(categoryName);
			final Queue<CategorySpecificAttributes> newAttrAlternatives;
			if (oldAttrAlternatives == null)
			{
				newAttrAlternatives = new ArrayDeque<>();
				multiReqAttrAlternativesByCategory.put(categoryName, newAttrAlternatives);
			} else
			{
				newAttrAlternatives = oldAttrAlternatives;
			}

			newAttrAlternatives.add(categoryAttributesAlternative);
		}

		/*
		 * Create initial individual request from which all others will be created/cloned
		 */
		// returnPolicyIdList not supported so always set to false
		final IndividualDecisionRequest initialIndividualReq = new IndividualDecisionRequest(request.isReturnPolicyIdList());
		/*
		 * Generate the Multiple Individual Decision Requests starting with initialIndividualReq and
		 * cloning/adding new attributes/content for each new attribute category's Attributes
		 * alternative in requestAttrAlternativesByCategory
		 */
		/*
		 * XACML Multiple Decision Profile, § 2.3.3: "For each combination of repeated <Attributes>
		 * elements, one Individual Decision Request SHALL be created. This Individual Request SHALL
		 * be identical to the original request context with one exception: only one <Attributes>
		 * element of each repeated category SHALL be present."
		 */
		final List<IndividualDecisionRequest> individualRequests = new ArrayList<>();
		individualRequests.add(initialIndividualReq);
		// for each attribute category
		for (final Entry<String, Queue<CategorySpecificAttributes>> multiReqAttrAlternativesByCategoryEntry : multiReqAttrAlternativesByCategory.entrySet())
		{
			final String categoryName = multiReqAttrAlternativesByCategoryEntry.getKey();
			final Queue<CategorySpecificAttributes> categoryAlternatives = multiReqAttrAlternativesByCategoryEntry.getValue();
			/*
			 * Get the first category (<Attributes>) alternative to be added to the individual
			 * requests existing in the individualRequests already, i.e. the "old" ones; whereas the
			 * other alternatives (if any) will be added to new individual request cloned from these
			 * "old" ones.
			 */
			final CategorySpecificAttributes categoryAlternative0 = categoryAlternatives.poll();
			if (categoryAlternative0 == null)
			{
				// no alternative / no repeated category
				continue;
			}

			final ListIterator<IndividualDecisionRequest> individualRequestsIterator = individualRequests.listIterator();
			while (individualRequestsIterator.hasNext())
			{
				final IndividualDecisionRequest oldReq = individualRequestsIterator.next();
				/*
				 * Before we add the first category alternative (categoryAlternative0) to the oldReq
				 * already created (the "old" one), we clone it for every other alternative, then
				 * add this other alternative to the new clone. Note that we called
				 * categoryAlternatives.poll() before, removing the first alternative, so
				 * categoryAlternatives only contains the other alternatives now.
				 */
				for (final CategorySpecificAttributes otherCategoryAlternative : categoryAlternatives)
				{
					// clone the request
					final IndividualDecisionRequest newReq = new IndividualDecisionRequest(oldReq);
					newReq.put(categoryName, otherCategoryAlternative);
					// add it to the final list of individual requests
					individualRequestsIterator.add(newReq);
				}

				// Now we are done cloning, we can add the first category alternative to
				// individualReqCtx
				oldReq.put(categoryName, categoryAlternative0);
			}

		}

		return individualRequests;
	}
}
