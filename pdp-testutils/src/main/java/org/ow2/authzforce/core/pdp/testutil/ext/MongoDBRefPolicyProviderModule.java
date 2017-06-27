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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.testutil.ext;

import java.io.IOException;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersionPattern;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.pdp.impl.policy.PolicyEvaluators;
import org.ow2.authzforce.core.pdp.testutil.ext.xmlns.MongoDBBasedPolicyProvider;
import org.ow2.authzforce.xacml.identifiers.XACMLNodeName;
import org.ow2.authzforce.xacml.identifiers.XACMLVersion;
import org.xml.sax.InputSource;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * 
 * Policy provider module that retrieves policies (given a Policy(Set)IdReference) from documents in a MongoDB-hosted collection. The document structure must correspond (be mappable) to
 * {@link PolicyPOJO} , where the 'type' is either "{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Policy" for XACML 3.0 Policies or "{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySet" for
 * XACML 3.0 PolicySets, the 'id' is the XACML Policy(Set)Id, the 'version' is the XACML Policy(Set)'s Version, and the 'content' property holds the actual XACML Policy(Set) document - depending on
 * 'type' - as plain text XML.
 * <p>
 * This module does not support Policy(Set)IdReferences with LatestVersion and EarliestVersion attributes.
 * <p>
 * TODO: performance optimization: cache results of {@link #get(TopLevelPolicyElementType, String, Optional, Deque)} to avoid repetitive requests to database server
 * 
 */
public class MongoDBRefPolicyProviderModule implements StaticRefPolicyProviderModule
{
	/**
	 * 'type' value expected in policy documents stored in database for XACML Policies
	 */
	public static final String XACML3_POLICY_TYPE_ID = "{" + XACMLVersion.V3_0.getNamespace() + "}" + XACMLNodeName.POLICY.value();

	/**
	 * 'type' value expected in policy documents stored in database for XACML PolicySets
	 */
	public static final String XACML3_POLICYSET_TYPE_ID = "{" + XACMLVersion.V3_0.getNamespace() + "}" + XACMLNodeName.POLICYSET.value();

	private final String id;
	private final MongoClient dbClient;
	private final MongoCollection policyCollection;
	private final XACMLParserFactory xacmlParserFactory;
	private final ExpressionFactory expressionFactory;
	private final CombiningAlgRegistry combiningAlgRegistry;
	private final int maxPolicySetRefDepth;

	private MongoDBRefPolicyProviderModule(final String id, final ServerAddress serverAddress, final String dbName, final String collectionName, final XACMLParserFactory xacmlParserFactory,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final int maxPolicySetRefDepth)
	{
		assert id != null && !id.isEmpty() && dbName != null && !dbName.isEmpty() && collectionName != null && !collectionName.isEmpty() && xacmlParserFactory != null && expressionFactory != null
				&& combiningAlgRegistry != null;

		this.id = id;
		this.dbClient = new MongoClient(serverAddress);
		final Jongo dbApiWrapper = new Jongo(dbClient.getDB(dbName));
		this.policyCollection = dbApiWrapper.getCollection(collectionName);
		this.xacmlParserFactory = xacmlParserFactory;
		this.expressionFactory = expressionFactory;
		this.combiningAlgRegistry = combiningAlgRegistry;
		this.maxPolicySetRefDepth = maxPolicySetRefDepth;
	}

	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RefPolicyProviderModule.Factory<MongoDBBasedPolicyProvider>
	{
		private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Expression factory");
		private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML parser factory");
		private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException("PolicyProvider configuration undefined");

		@Override
		public Class<MongoDBBasedPolicyProvider> getJaxbClass()
		{
			return MongoDBBasedPolicyProvider.class;
		}

		@Override
		public RefPolicyProviderModule getInstance(final MongoDBBasedPolicyProvider conf, final XACMLParserFactory xacmlParserFactory, final int maxPolicySetRefDepth,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final EnvironmentProperties environmentProperties) throws IllegalArgumentException
		{
			if (conf == null)
			{
				throw NULL_CONF_ARGUMENT_EXCEPTION;
			}

			if (xacmlParserFactory == null)
			{
				throw ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION;
			}

			if (expressionFactory == null)
			{
				throw ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION;
			}

			if (combiningAlgRegistry == null)
			{
				throw ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION;
			}

			final ServerAddress serverAddress;
			try
			{
				serverAddress = new ServerAddress(conf.getServerHost(), conf.getServerPort());
			}
			catch (final UnknownHostException e)
			{
				throw new IllegalArgumentException("Invalid database server host", e);
			}
			return new MongoDBRefPolicyProviderModule(conf.getId(), serverAddress, conf.getDbName(), conf.getCollectionName(), xacmlParserFactory, expressionFactory, combiningAlgRegistry,
					maxPolicySetRefDepth);
		}

	}

	@Override
	public void close() throws IOException
	{
		this.dbClient.close();
	}

	@Override
	public StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId, final Optional<VersionPatterns> policyVersionPatterns,
			final Deque<String> policySetRefChain) throws IndeterminateEvaluationException
	{
		/**
		 * TODO: use a policy cache and check it before requesting the database. If we found a matching policy in cache, and it is a policyset, we would check the depth of policy references as well:
		 * <p>
		 * Utils.appendAndCheckPolicyRefChain(newPolicySetRefChain, cachedPolicy.getExtraPolicyMetadata().getLongestPolicyRefChain(), maxPolicySetRefDepth);
		 */

		final String policyTypeId = policyType == TopLevelPolicyElementType.POLICY ? XACML3_POLICY_TYPE_ID : XACML3_POLICYSET_TYPE_ID;

		final Optional<PolicyVersionPattern> versionPattern;
		if (policyVersionPatterns.isPresent())
		{
			/*
			 * TODO: the following code does not support LatestVersion and EarliestVersion patterns. Beware that comparing versions (XACML VersionType) to each other - and also comparing literal
			 * version to version pattern (XACML VersionMatchType) - is NOT the same as sorting strings in lexicographical order or matching standard regular expressions. Indeed, in XACML, a version
			 * (VersionType) is a sequence/array of decimal numbers actually, therefore it relies on number comparison; and version pattern use wildcard characters '*' and '+' with a special meaning
			 * that is different from PCRE or other regex engines.
			 */
			final VersionPatterns nonNullPolicyVersionPatterns = policyVersionPatterns.get();
			if (nonNullPolicyVersionPatterns.getEarliestVersionPattern().isPresent())
			{
				throw new IllegalArgumentException("PolicyProvider '" + id + "': EarliestVersion in input policy reference is not supported");
			}

			if (nonNullPolicyVersionPatterns.getLatestVersionPattern().isPresent())
			{
				throw new IllegalArgumentException("PolicyProvider '" + id + "': LatestVersion in input policy reference is not supported");
			}

			versionPattern = nonNullPolicyVersionPatterns.getVersionPattern();
		}
		else
		{
			versionPattern = Optional.empty();
		}

		final PolicyPOJO policyPOJO;
		/*
		 * TODO: the following code will get any policy version that matches the policy type, id and optional VersionMatch. It may be smarter to always get the latest if there are multiple matches.
		 * But this adds complexity as mentioned in previous TODO comment.
		 */
		if (versionPattern.isPresent())
		{
			final PolicyVersionPattern nonNullVersionPattern = versionPattern.get();
			final PolicyVersion versionLiteral = nonNullVersionPattern.toLiteral();
			if (versionLiteral != null)
			{
				policyPOJO = policyCollection.findOne("{type: #, id: #, version: #}", policyTypeId, policyId, versionLiteral.toString()).as(PolicyPOJO.class);
			}
			else
			{
				/*
				 * versionPattern is not a literal/constant version (contains wildcard '*' or '+') -> convert to PCRE regex for MongoDB server-side evaluation
				 */
				final String regex = "^" + nonNullVersionPattern.toRegex() + "$";
				policyPOJO = policyCollection.findOne("{type: #, id: #, version: { $regex: # }}", policyTypeId, policyId, regex).as(PolicyPOJO.class);
			}
		}
		else
		{
			// no version pattern specified
			policyPOJO = policyCollection.findOne("{type: #, id: #}", policyTypeId, policyId).as(PolicyPOJO.class);
		}

		if (policyPOJO == null)
		{
			return null;
		}

		final NamespaceFilteringParser parser;
		try
		{
			parser = xacmlParserFactory.getInstance();
		}
		catch (final JAXBException e)
		{
			throw new IndeterminateEvaluationException("PolicyProvider " + id + ": Failed to create JAXB unmarshaller for XACML Policy(Set)", StatusHelper.STATUS_PROCESSING_ERROR, e);
		}

		final Object jaxbPolicyOrPolicySetObj;
		try
		{
			/*
			 * TODO: support more efficient formats of XML content, e.g. gzipped XML, Fast Infoset, EXI.
			 */
			jaxbPolicyOrPolicySetObj = parser.parse(new InputSource(new StringReader(policyPOJO.getContent())));
		}
		catch (final JAXBException e)
		{
			throw new IndeterminateEvaluationException("PolicyProvider " + id + ": failed to parse Policy(Set) XML document from 'content' value of the policy document " + policyPOJO
					+ " retrieved from database", StatusHelper.STATUS_PROCESSING_ERROR, e);
		}

		final Map<String, String> nsPrefixUriMap = parser.getNamespacePrefixUriMap();
		if (jaxbPolicyOrPolicySetObj instanceof Policy)
		{
			if (policyType != TopLevelPolicyElementType.POLICY)
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with 'type'. Actual content type: XACML 3.0 Policy (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			final Policy jaxbPolicy = (Policy) jaxbPolicyOrPolicySetObj;

			final String contentPolicyId = jaxbPolicy.getPolicyId();
			if (!contentPolicyId.equals(policyPOJO.getId()))
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": PolicyId in 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with 'id'. Actual PolicyId: " + contentPolicyId + " (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			final String contentPolicyVersion = jaxbPolicy.getVersion();
			if (!contentPolicyVersion.equals(policyPOJO.getVersion()))
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": Version in 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with 'version'. Actual Version: " + contentPolicyVersion + " (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			try
			{
				return PolicyEvaluators.getInstance(jaxbPolicy, null, nsPrefixUriMap, expressionFactory, combiningAlgRegistry);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid Policy in 'content' of the policy document " + policyPOJO + " retrieved from database", e);
			}
		}

		/*
		 * Content is not a XACML Policy
		 */
		if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
		{
			if (policyType != TopLevelPolicyElementType.POLICY_SET)
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with its 'type'. Actual content type: XACML 3.0 PolicySet (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			final PolicySet jaxbPolicySet = (PolicySet) jaxbPolicyOrPolicySetObj;

			final String contentPolicyId = jaxbPolicySet.getPolicySetId();
			if (!contentPolicyId.equals(policyPOJO.getId()))
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": PolicyId in 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with 'id'. Actual PolicyId: " + contentPolicyId + " (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			final String contentPolicyVersion = jaxbPolicySet.getVersion();
			if (!contentPolicyVersion.equals(policyPOJO.getVersion()))
			{
				throw new IndeterminateEvaluationException("PolicyProvider " + id + ": Version in 'content' of the policy document " + policyPOJO
						+ " retrieved from database is not consistent with 'version'. Actual Version: " + contentPolicyVersion + " (corrupted database?).", StatusHelper.STATUS_PROCESSING_ERROR);
			}

			try
			{
				return PolicyEvaluators.getInstanceStatic(jaxbPolicySet, null, nsPrefixUriMap, expressionFactory, combiningAlgRegistry, this, policySetRefChain, maxPolicySetRefDepth);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IndeterminateEvaluationException("Invalid PolicySet in 'content' of the policy document " + policyPOJO + " retrieved from database", StatusHelper.STATUS_PROCESSING_ERROR, e);
			}
		}

		// content is neither a Policy nor a PolicySet
		throw new IndeterminateEvaluationException("Unexpected 'content' found in policy document '" + policyPOJO + "' (corrupted database?). Corresponding Java type: "
				+ jaxbPolicyOrPolicySetObj.getClass().getCanonicalName() + ". Expected: " + Policy.class.getCanonicalName() + ", " + PolicySet.class.getCanonicalName(),
				StatusHelper.STATUS_PROCESSING_ERROR);
	}

	@Override
	public TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId, final Optional<VersionPatterns> policyVersionPatterns,
			final Deque<String> policySetRefChain, final EvaluationContext evaluationCtx) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		return get(policyType, policyId, policyVersionPatterns, policySetRefChain);
	}
}
