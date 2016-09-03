/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * This is the core implementation of <code>RefPolicyProviderModule</code> that
 * supports static retrieval of the policies referenced by
 * Policy(Set)IdReference. It is configured by a list of locations that
 * represent Spring-compatible resource URLs, corresponding to XACML Policy(Set)
 * files - each file content is expected to be a XACML Policy(Set) document -
 * when the module is initialized. Beyond this, there is no modifying or
 * re-loading the policies handled by this class.
 * <p>
 * A policy location may also be a file pattern in the following form:
 * "file://DIRECTORY_PATH/*SUFFIX" using wilcard character '*'; in which case
 * the location is expanded to all regular files (not subdirectories) in
 * directory located at DIRECTORY_PATH with suffix SUFFIX (SUFFIX may be empty,
 * i.e. no suffix). The files are NOT searched recursively on sub-directories.
 * <p>
 * Note that this class is designed to complement
 * {@link CoreRootPolicyProviderModule} in charge of the root policy(set) which
 * may refer to policies resolved by this {@link CoreRefPolicyProviderModule}.
 *
 * 
 * @version $Id: $
 */
public class CoreRefPolicyProviderModule implements StaticRefPolicyProviderModule
{
	private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined CombiningAlgorithm registry");
	private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined Expression factory");
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined XACML parser factory");
	private static final IllegalArgumentException ILLEGAL_POLICY_URLS_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined policy URL(s)");
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreRefPolicyProviderModule.class);

	/**
	 * Policy wrapper to keep the association between the namespace prefix-URIs
	 * from a XACML policy document and the Java instance of the policy
	 * resulting from parsing the same document
	 *
	 * @param
	 * 			<P>
	 *            policy type
	 */
	public static final class PolicyWithNamespaces<P>
	{
		private final Map<String, String> nsPrefixUriMap;

		/**
		 * Get namespace prefix-URI mappings
		 * 
		 * @return namespace prefix-URI mappings
		 */
		public Map<String, String> getNsPrefixUriMap()
		{
			return nsPrefixUriMap;
		}

		/**
		 * Get policy
		 * 
		 * @return policy(Set)
		 */
		public P getPolicy()
		{
			return policy;
		}

		private final P policy;

		private PolicyWithNamespaces(final P policy, final Map<String, String> nsPrefixUriMap)
		{
			this.policy = policy;
			this.nsPrefixUriMap = nsPrefixUriMap;
		}

	}

	/**
	 * Module factory
	 * 
	 */
	public static class Factory
			extends RefPolicyProviderModule.Factory<org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider>
	{

		private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException(
				"RefPolicyProvider configuration undefined");

		@Override
		public Class<org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider> getJaxbClass()
		{
			return org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider.class;
		}

		@Override
		public RefPolicyProviderModule getInstance(final org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider conf,
				final XACMLParserFactory xacmlParserFactory, final int maxPolicySetRefDepth,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry,
				final EnvironmentProperties environmentProperties)
		{
			if (conf == null)
			{
				throw NULL_CONF_ARGUMENT_EXCEPTION;
			}

			final List<URL> policyURLs = new ArrayList<>();
			int policyLocationIndex = 0;
			for (final String policyLocation : conf.getPolicyLocations())
			{
				// Check whether the location is a file path pattern
				if (policyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX))
				{
					// location on the filesystem
					final int index = policyLocation.indexOf("/*");
					if (index > 0)
					{
						// this is a file path pattern
						final String directoryLocation = policyLocation.substring(0, index);
						final String suffix = policyLocation.substring(index + 2);
						if (LOGGER.isDebugEnabled())
						{
							// Beware of autoboxing which causes call to
							// Integer.valueOf(...) on policyLocationIndex
							LOGGER.debug(
									"Policy location #{} is a filepath pattern (found '/*') -> expanding to all files in directory '{}' with suffix '{}'",
									policyLocationIndex, directoryLocation, suffix);
						}

						final String dirLocation = environmentProperties.replacePlaceholders(directoryLocation);
						final URL directoryURL;
						try
						{
							directoryURL = ResourceUtils.getURL(dirLocation);
						}
						catch (final FileNotFoundException e)
						{
							throw new IllegalArgumentException("Invalid directory location: '" + dirLocation
									+ "' extracted from policy location (pattern) '" + policyLocation + "'", e);
						}

						final Path directoryPath;
						try
						{
							directoryPath = Paths.get(directoryURL.toURI());
						}
						catch (final URISyntaxException e)
						{
							throw new RuntimeException("Error converting policy directory URL '" + directoryURL
									+ "' - extracted from policy location (pattern) '" + policyLocation
									+ "' - to a Java Path (via URI)", e);
						}

						try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath))
						{
							for (final Path path : stream)
							{
								if (Files.isRegularFile(path))
								{
									final Path lastPathElement = path.getFileName();
									if (lastPathElement != null && lastPathElement.toString().endsWith(suffix))
									{
										policyURLs.add(lastPathElement.toUri().toURL());
									}
								}
							}
						}
						catch (final DirectoryIteratorException ex)
						{
							// I/O error encounted during the iteration, the
							// cause is an IOException
							throw new RuntimeException("Error iterating over files in directory '" + dirLocation
									+ "' to get policies at locations matching pattern '" + policyLocation + "'",
									ex.getCause());
						}
						catch (final IOException e)
						{
							throw new RuntimeException("Error getting policy files in '" + dirLocation
									+ "' according to policy location pattern '" + policyLocation + "'", e);
						}

						continue;
					}
				}

				// not a file path pattern
				final String policyLoc = environmentProperties.replacePlaceholders(policyLocation);
				final URL policyURL;
				try
				{
					// try to load the policy location as a Spring resource
					policyURL = ResourceUtils.getURL(policyLoc);
				}
				catch (final FileNotFoundException e)
				{
					throw new IllegalArgumentException(
							"Error loading policy (as Spring resource) from the following URL: " + policyLoc, e);
				}

				if (policyURL == null)
				{
					throw new IllegalArgumentException("No policy file found at the specified location: " + policyLoc);
				}

				policyURLs.add(policyURL);
				policyLocationIndex++;
			}

			return CoreRefPolicyProviderModule.getInstance(policyURLs, xacmlParserFactory, maxPolicySetRefDepth,
					expressionFactory, combiningAlgRegistry);
		}
	}

	/*
	 * Ref policy Provider used only for initialization, more particularly for
	 * parsing the PolicySets when they are referred to by others (in
	 * PolicySetIdReferences) at initialization time
	 */
	private static class InitOnlyRefPolicyProvider implements StaticRefPolicyProvider
	{

		private final int maxPolicySetRefDepth;
		private final ExpressionFactory expressionFactory;
		private final CombiningAlgRegistry combiningAlgRegistry;

		// will be updated by get(...)
		private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap;
		private final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;
		private final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> policySetMapToUpdate;

		private InitOnlyRefPolicyProvider(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap,
				final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap,
				final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> outPolicySetEvaluatorMap,
				final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory,
				final CombiningAlgRegistry combiningAlgRegistry)
		{
			this.policyMap = policyMap;
			this.policySetMapToUpdate = outPolicySetEvaluatorMap;
			this.jaxbPolicySetMap = jaxbPolicySetMap;
			this.maxPolicySetRefDepth = maxPolicySetRefDepth;
			this.expressionFactory = expressionFactory;
			this.combiningAlgRegistry = combiningAlgRegistry;
		}

		@Override
		public TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String id,
				final VersionPatterns versionConstraints, final Deque<String> ancestorPolicyRefChain,
				final EvaluationContext evaluationContext)
						throws IndeterminateEvaluationException, IllegalArgumentException
		{
			return get(policyType, id, versionConstraints, ancestorPolicyRefChain);
		}

		@Override
		public StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String id,
				final VersionPatterns versionConstraints, final Deque<String> ancestorPolicyRefChain)
		{
			// If this is a request for Policy (from PolicyIdReference)
			if (policyType == TopLevelPolicyElementType.POLICY)
			{
				final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyMap.get(id,
						versionConstraints);
				return policyEntry == null ? null : policyEntry.getValue();
			}

			// Else this is a request for PolicySet (from PolicySetIdReference)
			final Deque<String> newPolicySetRefChain = Utils.appendAndCheckPolicyRefChain(ancestorPolicyRefChain,
					Collections.singletonList(id), maxPolicySetRefDepth);
			final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry = jaxbPolicySetMap.get(id,
					versionConstraints);
			if (jaxbPolicySetEntry == null)
			{
				// no such policy
				return null;
			}

			final PolicyVersion jaxbPolicySetVersion = jaxbPolicySetEntry.getKey();
			// Check whether already parsed
			final StaticTopLevelPolicyElementEvaluator policySetEvaluator = policySetMapToUpdate.get(id,
					jaxbPolicySetVersion);
			final StaticTopLevelPolicyElementEvaluator resultPolicySetEvaluator;
			if (policySetEvaluator == null)
			{
				/*
				 * No matching version already parsed. Instantiate the policy
				 * evaluator
				 */
				final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
				try
				{
					resultPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, null,
							jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry, this,
							newPolicySetRefChain);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(
							"Invalid PolicySet with PolicySetId=" + id + ", Version=" + jaxbPolicySetVersion, e);
				}

				policySetMapToUpdate.put(id, jaxbPolicySetVersion, resultPolicySetEvaluator);
			}
			else
			{
				// policySet already parsed
				resultPolicySetEvaluator = policySetEvaluator;
				/*
				 * check total policy ref depth, i.e. length of
				 * (newAncestorPolicySetRefChain + parsed policySet's longest
				 * (nested) policy ref chain) <= maxPolicySetRefDepth
				 */
				Utils.appendAndCheckPolicyRefChain(newPolicySetRefChain,
						policySetEvaluator.getExtraPolicyMetadata().getLongestPolicyRefChain(), maxPolicySetRefDepth);
			}

			return resultPolicySetEvaluator;
		}
	}

	private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyEvaluatorMap;
	private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policySetEvaluatorMap;
	private final int maxPolicySetRefDepth;

	private CoreRefPolicyProviderModule(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap,
			final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
					throws IllegalArgumentException
	{
		assert policyMap != null && jaxbPolicySetMap != null && expressionFactory != null
				&& combiningAlgRegistry != null;

		this.maxPolicySetRefDepth = maxPolicySetRefDepth < 0 ? Utils.UNLIMITED_POLICY_REF_DEPTH : maxPolicySetRefDepth;
		this.policyEvaluatorMap = policyMap;
		final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicySetEvaluatorTable = HashBasedTable
				.create();
		/*
		 * Ref policy Provider module used only for initialization, more
		 * particularly for parsing the PolicySets when they are referred to by
		 * others (in PolicySetIdReferences)
		 */
		final StaticRefPolicyProvider bootstrapRefPolicyProvider = new InitOnlyRefPolicyProvider(
				this.policyEvaluatorMap, jaxbPolicySetMap, updatablePolicySetEvaluatorTable, this.maxPolicySetRefDepth,
				expressionFactory, combiningAlgRegistry);

		for (final Entry<String, PolicyVersions<PolicyWithNamespaces<PolicySet>>> jaxbPolicySetWithNsEntry : jaxbPolicySetMap
				.entrySet())
		{
			final String policySetId = jaxbPolicySetWithNsEntry.getKey();
			// instantiate all policy versions for this policyId now
			final PolicyVersions<PolicyWithNamespaces<PolicySet>> jaxbPolicySetVersions = jaxbPolicySetWithNsEntry
					.getValue();
			for (final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry : jaxbPolicySetVersions)
			{

				final PolicyVersion policySetVersion = jaxbPolicySetEntry.getKey();
				/*
				 * Check corresponding PolicySet evaluator in
				 * policySetEvaluatorTable to check whether it is not already
				 * there, i.e. already instantiated by
				 * refPolicyProvider.get(...) because of Policy references in
				 * previously instantiated policies (when calling
				 * PolicyEvaluators.getInstanceStatic() down below)
				 */
				final StaticTopLevelPolicyElementEvaluator old = updatablePolicySetEvaluatorTable.get(policySetId,
						policySetVersion);
				if (old == null)
				{
					// no policyset with such ID/Version instantiated yet
					// do it now
					final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
					final StaticTopLevelPolicyElementEvaluator newPolicySetEvaluator;
					try
					{
						newPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, null,
								jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry,
								bootstrapRefPolicyProvider, null);
					}
					catch (final IllegalArgumentException e)
					{
						throw new IllegalArgumentException(
								"Invalid PolicySet with PolicySetId=" + policySetId + ", Version=" + policySetVersion,
								e);
					}

					updatablePolicySetEvaluatorTable.put(policySetId, policySetVersion, newPolicySetEvaluator);
				}
			}
		}

		this.policySetEvaluatorMap = new PolicyMap<>(updatablePolicySetEvaluatorTable.rowMap());
	}

	/**
	 * Creates an instance from XACML/JAXB Policy(Set) elements
	 *
	 * @param jaxbPolicies
	 *            XACML Policy elements
	 * @param jaxbPolicySets
	 *            XACML PolicySets
	 * @param maxPolicySetRefDepth
	 *            maximum allowed depth of PolicySet reference chain (via
	 *            PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the
	 *            policy(set)
	 * @return instance of this module
	 * @throws java.lang.IllegalArgumentException
	 *             if both {@code jaxbPoliciesByIdAndVersion} and
	 *             {@code jaxbPolicySetsByIdAndVersion} are null/empty, or
	 *             expressionFactory/combiningAlgRegistry undefined; or one of
	 *             the Policy(Set)s is not valid or conflicts with another
	 *             because it has same Policy(Set)Id and Version.
	 */
	public static CoreRefPolicyProviderModule getInstance(final List<PolicyWithNamespaces<Policy>> jaxbPolicies,
			final List<PolicyWithNamespaces<PolicySet>> jaxbPolicySets, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
					throws IllegalArgumentException
	{
		if ((jaxbPolicies == null || jaxbPolicies.isEmpty()) && (jaxbPolicySets == null || jaxbPolicySets.isEmpty()))
		{
			throw new IllegalArgumentException("No Policy(Set) specified");
		}

		if (expressionFactory == null)
		{
			throw ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION;
		}

		if (combiningAlgRegistry == null)
		{
			throw ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION;
		}

		final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap;
		if (jaxbPolicies == null)
		{
			policyMap = new PolicyMap<>(
					Collections.<String, Map<PolicyVersion, StaticTopLevelPolicyElementEvaluator>> emptyMap());
		}
		else
		{
			final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicyTable = HashBasedTable
					.create();
			for (final PolicyWithNamespaces<Policy> jaxbPolicyWithNs : jaxbPolicies)
			{
				final Policy jaxbPolicy = jaxbPolicyWithNs.policy;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final StaticTopLevelPolicyElementEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, null, jaxbPolicyWithNs.nsPrefixUriMap,
							expressionFactory, combiningAlgRegistry);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(
							"Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final StaticTopLevelPolicyElementEvaluator previousValue = updatablePolicyTable.put(policyId,
						new PolicyVersion(policyVersion), policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two <Policy>s with same PolicyId=" + policyId
							+ ", Version=" + policyVersion);
				}
			}

			policyMap = new PolicyMap<>(updatablePolicyTable.rowMap());
		}

		final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;
		if (jaxbPolicySets == null)
		{
			jaxbPolicySetMap = new PolicyMap<>(
					Collections.<String, Map<PolicyVersion, PolicyWithNamespaces<PolicySet>>> emptyMap());
		}
		else
		{
			final Table<String, PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicySetTable = HashBasedTable
					.create();
			for (final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs : jaxbPolicySets)
			{
				final PolicySet jaxbPolicySet = jaxbPolicySetWithNs.policy;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				// check if any version of the same policy exist in the map
				final PolicyWithNamespaces<PolicySet> previousValue = updatablePolicySetTable.put(policyId,
						new PolicyVersion(policyVersion), jaxbPolicySetWithNs);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId="
							+ policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them
				 * all, because each PolicySet may refer to others via
				 * PolicySetIdReferences
				 */
			}

			jaxbPolicySetMap = new PolicyMap<>(updatablePolicySetTable.rowMap());
		}

		return new CoreRefPolicyProviderModule(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory,
				combiningAlgRegistry);
	}

	/**
	 * Creates an instance from policy locations
	 *
	 * @param policyURLs
	 *            location of Policy(Set) elements (JAXB) to be parsed for
	 *            future reference by Policy(Set)IdReferences
	 * @param xacmlParserFactory
	 *            XACML parser factory for parsing any XACML Policy(Set)
	 * @param maxPolicySetRefDepth
	 *            maximum allowed depth of PolicySet reference chain (via
	 *            PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; a
	 *            strictly negative value means no limit
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the
	 *            policy(set)
	 * @return instance of this class
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code policyURLs == null || policyURLs.length == 0}, or
	 *             expressionFactory/combiningAlgRegistry undefined; or one of
	 *             {@code policyURLs} is null or is not a valid XACML
	 *             Policy(Set) or conflicts with another because it has same
	 *             Policy(Set)Id and Version. Beware that the Policy(Set)Issuer
	 *             is ignored from this check!
	 */
	public static CoreRefPolicyProviderModule getInstance(final Collection<URL> policyURLs,
			final XACMLParserFactory xacmlParserFactory, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
					throws IllegalArgumentException
	{
		if (policyURLs == null || policyURLs.isEmpty())
		{
			throw ILLEGAL_POLICY_URLS_ARGUMENT_EXCEPTION;
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

		final NamespaceFilteringParser parser;
		try
		{
			parser = xacmlParserFactory.getInstance();
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablepolicyTable = HashBasedTable
				.create();
		final Table<String, PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicySetTable = HashBasedTable
				.create();
		int policyUrlIndex = 0;
		for (final URL policyURL : policyURLs)
		{
			if (policyURL == null)
			{
				throw new IllegalArgumentException("policyURL #" + policyUrlIndex + " undefined");
			}

			final Object jaxbPolicyOrPolicySetObj;
			try
			{
				jaxbPolicyOrPolicySetObj = parser.parse(policyURL);
			}
			catch (final JAXBException e)
			{
				throw new IllegalArgumentException(
						"Failed to unmarshall Policy(Set) XML document from policy location: " + policyURL, e);
			}

			final Map<String, String> nsPrefixUriMap = parser.getNamespacePrefixUriMap();
			if (jaxbPolicyOrPolicySetObj instanceof Policy)
			{
				final Policy jaxbPolicy = (Policy) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final StaticTopLevelPolicyElementEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, null, nsPrefixUriMap, expressionFactory,
							combiningAlgRegistry);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(
							"Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final StaticTopLevelPolicyElementEvaluator previousValue = updatablepolicyTable.put(policyId,
						new PolicyVersion(policyVersion), policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two policies with same PolicyId=" + policyId
							+ ", Version=" + policyVersion);
				}

			}
			else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
			{
				final PolicySet jaxbPolicySet = (PolicySet) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final PolicyWithNamespaces<PolicySet> previousValue = updatablePolicySetTable.put(policyId,
						new PolicyVersion(policyVersion), new PolicyWithNamespaces<>(jaxbPolicySet, nsPrefixUriMap));
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId="
							+ policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them
				 * all, because each PolicySet may refer to others via
				 * PolicySetIdReferences
				 */
			}
			else
			{
				throw new IllegalArgumentException("Unexpected element found as root of the policy document: "
						+ jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
			}

			policyUrlIndex++;
		}

		final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap = new PolicyMap<>(
				updatablepolicyTable.rowMap());
		final PolicyMap<PolicyWithNamespaces<PolicySet>> policySetMap = new PolicyMap<>(
				updatablePolicySetTable.rowMap());
		return new CoreRefPolicyProviderModule(policyMap, policySetMap, maxPolicySetRefDepth, expressionFactory,
				combiningAlgRegistry);
	}

	/** {@inheritDoc} */
	@Override
	public StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String id,
			final VersionPatterns constraints, final Deque<String> policySetRefChain)
	{
		if (policyType == TopLevelPolicyElementType.POLICY)
		{
			// Request for Policy (from PolicyIdReference)
			final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyEvaluatorMap.get(id,
					constraints);
			if (policyEntry == null)
			{
				return null;
			}

			return policyEntry.getValue();
		}

		// Request for PolicySet (from PolicySetIdReference)
		final Deque<String> newPolicySetRefChain = Utils.appendAndCheckPolicyRefChain(policySetRefChain,
				Collections.singletonList(id), maxPolicySetRefDepth);
		final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policySetEvaluatorMap.get(id,
				constraints);
		if (policyEntry == null)
		{
			return null;
		}

		/*
		 * check total policy ref depth, i.e. length of
		 * (newAncestorPolicySetRefChain + parsed policySet's longest (nested)
		 * policy ref chain) <= maxPolicySetRefDepth
		 */
		final StaticTopLevelPolicyElementEvaluator policy = policyEntry.getValue();
		Utils.appendAndCheckPolicyRefChain(newPolicySetRefChain,
				policy.getExtraPolicyMetadata().getLongestPolicyRefChain(), maxPolicySetRefDepth);
		return policy;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		// maps are immutable, nothing to clear
	}

	/** {@inheritDoc} */
	@Override
	public TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId,
			final VersionPatterns policyVersionConstraints, final Deque<String> policySetRefChain,
			final EvaluationContext evaluationCtx) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		return get(policyType, policyId, policyVersionConstraints, policySetRefChain);
	}

}
