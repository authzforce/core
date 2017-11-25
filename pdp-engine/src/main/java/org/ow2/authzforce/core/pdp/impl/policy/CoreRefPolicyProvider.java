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
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.BaseStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.PolicyRefsMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This is the core implementation of {@link StaticRefPolicyProvider} that supports static retrieval of the policies referenced by Policy(Set)IdReference. It is configured by a list of locations that
 * represent Spring-compatible resource URLs, corresponding to XACML Policy(Set) files - each file content is expected to be a XACML Policy(Set) document - when the module is initialized. Beyond this,
 * there is no modifying or re-loading of the policies.
 * <p>
 * A policy location may also be a file pattern in the following form: "file://DIRECTORY_PATH/*SUFFIX" using wilcard character '*'; in which case the location is expanded to all regular files (not
 * subdirectories) in directory located at DIRECTORY_PATH with suffix SUFFIX (SUFFIX may be empty, i.e. no suffix). The files are NOT searched recursively on sub-directories.
 * <p>
 * Note that this class is designed to complement {@link CoreRootPolicyProvider} in charge of the root policy(set) which may refer to policies resolved by this {@link CoreRefPolicyProvider}.
 *
 * 
 * @version $Id: $
 */
public class CoreRefPolicyProvider extends BaseStaticRefPolicyProvider
{
	private static final IllegalArgumentException NO_POLICY_ARG_EXCEPTION = new IllegalArgumentException("No Policy(Set) specified");
	private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined CombiningAlgorithm registry");
	private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Expression factory");
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML parser factory");
	private static final IllegalArgumentException ILLEGAL_POLICY_URLS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined policy URL(s)");
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreRefPolicyProvider.class);

	/**
	 * Policy wrapper to keep the association between the namespace prefix-URIs from a XACML policy document and the Java instance of the policy resulting from parsing the same document
	 *
	 * @param <P>
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
	public static class Factory extends CloseableRefPolicyProvider.Factory<org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider>
	{

		private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException("RefPolicyProvider configuration undefined");

		@Override
		public Class<org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider> getJaxbClass()
		{
			return org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider.class;
		}

		@Override
		public CloseableRefPolicyProvider getInstance(final org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider conf, final XmlnsFilteringParserFactory xacmlParserFactory,
				final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final EnvironmentProperties environmentProperties)
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
							LOGGER.debug("Policy location #{} is a filepath pattern (found '/*') -> expanding to all files in directory '{}' with suffix '{}'", policyLocationIndex, directoryLocation,
									suffix);
						}

						final String dirLocation = environmentProperties.replacePlaceholders(directoryLocation);
						final URL directoryURL;
						try
						{
							directoryURL = ResourceUtils.getURL(dirLocation);
						}
						catch (final FileNotFoundException e)
						{
							throw new IllegalArgumentException("Invalid directory location: '" + dirLocation + "' extracted from policy location (pattern) '" + policyLocation + "'", e);
						}

						final Path directoryPath;
						try
						{
							directoryPath = Paths.get(directoryURL.toURI());
						}
						catch (final URISyntaxException e)
						{
							throw new RuntimeException("Error converting policy directory URL '" + directoryURL + "' - extracted from policy location (pattern) '" + policyLocation
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
							throw new RuntimeException("Error iterating over files in directory '" + dirLocation + "' to get policies at locations matching pattern '" + policyLocation + "'",
									ex.getCause());
						}
						catch (final IOException e)
						{
							throw new RuntimeException("Error getting policy files in '" + dirLocation + "' according to policy location pattern '" + policyLocation + "'", e);
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
					throw new IllegalArgumentException("Error loading policy (as Spring resource) from the following URL: " + policyLoc, e);
				}

				if (policyURL == null)
				{
					throw new IllegalArgumentException("No policy file found at the specified location: " + policyLoc);
				}

				policyURLs.add(policyURL);
				policyLocationIndex++;
			}

			return CoreRefPolicyProvider.getInstance(policyURLs, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
		}
	}

	/*
	 * Ref policy Provider used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in PolicySetIdReferences) at initialization time
	 */
	private static class InitOnlyRefPolicyProvider extends BaseStaticRefPolicyProvider
	{

		private final ExpressionFactory expressionFactory;
		private final CombiningAlgRegistry combiningAlgRegistry;

		// will be updated by get(...)
		private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap;
		private final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;
		private final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> policySetMapToUpdate;
		private final Set<String> parsedPolicyIds;
		private final Set<String> parsedPolicySetIds;

		private InitOnlyRefPolicyProvider(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap, final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap,
				final Set<String> parsedPolicyIds, final Set<String> parsedPolicySetIds, final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> outPolicySetEvaluatorMap,
				final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
		{
			super(maxPolicySetRefDepth);
			assert parsedPolicyIds != null && parsedPolicySetIds != null;

			this.policyMap = policyMap;
			this.policySetMapToUpdate = outPolicySetEvaluatorMap;
			this.jaxbPolicySetMap = jaxbPolicySetMap;
			// this.maxPolicySetRefDepth = maxPolicySetRefDepth;
			this.expressionFactory = expressionFactory;
			this.combiningAlgRegistry = combiningAlgRegistry;
			this.parsedPolicyIds = parsedPolicyIds;
			this.parsedPolicySetIds = parsedPolicySetIds;
		}

		@Override
		protected StaticTopLevelPolicyElementEvaluator getPolicy(final String policyIdRef, final Optional<VersionPatterns> constraints) throws IndeterminateEvaluationException
		{
			final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyMap.get(policyIdRef, constraints);
			return policyEntry == null ? null : policyEntry.getValue();
		}

		@Override
		public StaticTopLevelPolicyElementEvaluator getPolicySet(final String id, final Optional<VersionPatterns> versionConstraints, final Deque<String> policySetRefChain)
		{
			final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry = jaxbPolicySetMap.get(id, versionConstraints);
			if (jaxbPolicySetEntry == null)
			{
				// no such policy
				return null;
			}

			final PolicyVersion jaxbPolicySetVersion = jaxbPolicySetEntry.getKey();
			// Check whether already parsed
			final StaticTopLevelPolicyElementEvaluator policySetEvaluator = policySetMapToUpdate.get(id, jaxbPolicySetVersion);
			final StaticTopLevelPolicyElementEvaluator resultPolicySetEvaluator;
			if (policySetEvaluator == null)
			{
				/*
				 * No matching version already parsed. Instantiate the policy evaluator
				 */
				final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
				try
				{
					resultPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, null, jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry,
							this.parsedPolicyIds, this.parsedPolicySetIds, this, policySetRefChain);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid PolicySet with PolicySetId=" + id + ", Version=" + jaxbPolicySetVersion, e);
				}

				policySetMapToUpdate.put(id, jaxbPolicySetVersion, resultPolicySetEvaluator);
			}
			else
			{
				// policySet already parsed
				resultPolicySetEvaluator = policySetEvaluator;
				/*
				 * check total policy ref depth if policySetRefChain != null, i.e. length of (newAncestorPolicySetRefChain + parsed policySet's longest (nested) policy ref chain) <=
				 * maxPolicySetRefDepth
				 */
				if (policySetRefChain != null && !policySetRefChain.isEmpty())
				{
					final Optional<PolicyRefsMetadata> policyRefsMetadata = policySetEvaluator.getPolicyRefsMetadata();
					if (policyRefsMetadata.isPresent())
					{
						joinPolicyRefChains(policySetRefChain, policyRefsMetadata.get().getLongestPolicyRefChain());
					}
				}
			}

			return resultPolicySetEvaluator;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider#close()
		 */
		@Override
		public void close()
		{
			// Nothing to close - erase exception from the close() signature
		}
	}

	private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyEvaluatorMap;
	private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policySetEvaluatorMap;

	private CoreRefPolicyProvider(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap, final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		super(maxPolicySetRefDepth);
		assert policyMap != null && jaxbPolicySetMap != null && expressionFactory != null && combiningAlgRegistry != null;

		this.policyEvaluatorMap = policyMap;
		final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicySetEvaluatorTable = HashBasedTable.create();
		/*
		 * Ref policy Provider module used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in PolicySetIdReferences)
		 */
		final Set<String> parsedPolicyIds = HashCollections.newUpdatableSet();
		final Set<String> parsedPolicySetIds = HashCollections.newUpdatableSet();
		try (final InitOnlyRefPolicyProvider bootstrapRefPolicyProvider = new InitOnlyRefPolicyProvider(this.policyEvaluatorMap, jaxbPolicySetMap, parsedPolicyIds, parsedPolicySetIds,
				updatablePolicySetEvaluatorTable, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry))
		{
			for (final Entry<String, PolicyVersions<PolicyWithNamespaces<PolicySet>>> jaxbPolicySetWithNsEntry : jaxbPolicySetMap.entrySet())
			{
				final String policySetId = jaxbPolicySetWithNsEntry.getKey();
				// instantiate all policy versions for this policyId now
				final PolicyVersions<PolicyWithNamespaces<PolicySet>> jaxbPolicySetVersions = jaxbPolicySetWithNsEntry.getValue();
				for (final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry : jaxbPolicySetVersions)
				{

					final PolicyVersion policySetVersion = jaxbPolicySetEntry.getKey();
					/*
					 * Check corresponding PolicySet evaluator in policySetEvaluatorTable to check whether it is not already there, i.e. already instantiated by refPolicyProvider.get(...) because of
					 * Policy references in previously instantiated policies (when calling PolicyEvaluators.getInstanceStatic() down below)
					 */
					final StaticTopLevelPolicyElementEvaluator old = updatablePolicySetEvaluatorTable.get(policySetId, policySetVersion);
					if (old == null)
					{
						// no policyset with such ID/Version instantiated yet
						// do it now
						final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
						final StaticTopLevelPolicyElementEvaluator newPolicySetEvaluator;
						try
						{
							newPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, null, jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry,
									parsedPolicyIds, parsedPolicySetIds, bootstrapRefPolicyProvider, null);
						}
						catch (final IllegalArgumentException e)
						{
							throw new IllegalArgumentException("Invalid PolicySet with PolicySetId=" + policySetId + ", Version=" + policySetVersion, e);
						}

						updatablePolicySetEvaluatorTable.put(policySetId, policySetVersion, newPolicySetEvaluator);
					}
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
	 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @return instance of this module
	 * @throws java.lang.IllegalArgumentException
	 *             if both {@code jaxbPolicies} and {@code jaxbPolicySets} are null/empty, or expressionFactory/combiningAlgRegistry undefined; or one of the Policy(Set)s is not valid or conflicts
	 *             with another because it has same Policy(Set)Id and Version.
	 */
	public static CoreRefPolicyProvider getInstance(final List<PolicyWithNamespaces<Policy>> jaxbPolicies, final List<PolicyWithNamespaces<PolicySet>> jaxbPolicySets, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		if ((jaxbPolicies == null || jaxbPolicies.isEmpty()) && (jaxbPolicySets == null || jaxbPolicySets.isEmpty()))
		{
			throw NO_POLICY_ARG_EXCEPTION;
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
			policyMap = new PolicyMap<>(Collections.<String, Map<PolicyVersion, StaticTopLevelPolicyElementEvaluator>> emptyMap());
		}
		else
		{
			final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicyTable = HashBasedTable.create();
			for (final PolicyWithNamespaces<Policy> jaxbPolicyWithNs : jaxbPolicies)
			{
				final Policy jaxbPolicy = jaxbPolicyWithNs.policy;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final StaticTopLevelPolicyElementEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, null, jaxbPolicyWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final StaticTopLevelPolicyElementEvaluator previousValue = updatablePolicyTable.put(policyId, new PolicyVersion(policyVersion), policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two <Policy>s with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}
			}

			policyMap = new PolicyMap<>(updatablePolicyTable.rowMap());
		}

		final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;
		if (jaxbPolicySets == null)
		{
			jaxbPolicySetMap = new PolicyMap<>(Collections.<String, Map<PolicyVersion, PolicyWithNamespaces<PolicySet>>> emptyMap());
		}
		else
		{
			final Table<String, PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicySetTable = HashBasedTable.create();
			for (final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs : jaxbPolicySets)
			{
				final PolicySet jaxbPolicySet = jaxbPolicySetWithNs.policy;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				// check if any version of the same policy exist in the map
				final PolicyWithNamespaces<PolicySet> previousValue = updatablePolicySetTable.put(policyId, new PolicyVersion(policyVersion), jaxbPolicySetWithNs);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each PolicySet may refer to others via PolicySetIdReferences
				 */
			}

			jaxbPolicySetMap = new PolicyMap<>(updatablePolicySetTable.rowMap());
		}

		return new CoreRefPolicyProvider(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	/**
	 * Creates an instance from policy locations
	 *
	 * @param policyURLs
	 *            location of Policy(Set) elements (JAXB) to be parsed for future reference by Policy(Set)IdReferences
	 * @param xacmlParserFactory
	 *            XACML parser factory for parsing any XACML Policy(Set)
	 * @param maxPolicySetRefDepth
	 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; a strictly negative value means no limit
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @return instance of this class
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code policyURLs == null || policyURLs.length == 0 || xacmlParserFactory == null || expressionFactory == null || combiningAlgRegistry == null}; or one of {@code policyURLs} is
	 *             null or is not a valid XACML Policy(Set) or conflicts with another because it has same Policy(Set)Id and Version. Beware that the Policy(Set)Issuer is ignored from this check!
	 */
	public static CoreRefPolicyProvider getInstance(final Collection<URL> policyURLs, final XmlnsFilteringParserFactory xacmlParserFactory, final int maxPolicySetRefDepth,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
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

		final XmlnsFilteringParser xacmlParser;
		try
		{
			xacmlParser = xacmlParserFactory.getInstance();
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablepolicyTable = HashBasedTable.create();
		final Table<String, PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicySetTable = HashBasedTable.create();
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
				jaxbPolicyOrPolicySetObj = xacmlParser.parse(policyURL);
			}
			catch (final JAXBException e)
			{
				throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + policyURL, e);
			}

			final Map<String, String> nsPrefixUriMap = xacmlParser.getNamespacePrefixUriMap();
			if (jaxbPolicyOrPolicySetObj instanceof Policy)
			{
				final Policy jaxbPolicy = (Policy) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final StaticTopLevelPolicyElementEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, null, nsPrefixUriMap, expressionFactory, combiningAlgRegistry);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final StaticTopLevelPolicyElementEvaluator previousValue = updatablepolicyTable.put(policyId, new PolicyVersion(policyVersion), policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two policies with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}

			}
			else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
			{
				final PolicySet jaxbPolicySet = (PolicySet) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final PolicyWithNamespaces<PolicySet> previousValue = updatablePolicySetTable
						.put(policyId, new PolicyVersion(policyVersion), new PolicyWithNamespaces<>(jaxbPolicySet, nsPrefixUriMap));
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each PolicySet may refer to others via PolicySetIdReferences
				 */
			}
			else
			{
				throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
			}

			policyUrlIndex++;
		}

		final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap = new PolicyMap<>(updatablepolicyTable.rowMap());
		final PolicyMap<PolicyWithNamespaces<PolicySet>> policySetMap = new PolicyMap<>(updatablePolicySetTable.rowMap());
		return new CoreRefPolicyProvider(policyMap, policySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	@Override
	protected StaticTopLevelPolicyElementEvaluator getPolicy(final String id, final Optional<VersionPatterns> constraints)
	{
		final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyEvaluatorMap.get(id, constraints);
		if (policyEntry == null)
		{
			return null;
		}

		return policyEntry.getValue();
	}

	@Override
	protected StaticTopLevelPolicyElementEvaluator getPolicySet(final String id, final Optional<VersionPatterns> constraints, final Deque<String> policySetRefChainIncludingResult)
	{
		/*
		 * Request for PolicySet (not necessarily from PolicySetIdReference, but also from CoreRefBasedRootPolicyProviderModule#CoreRefBasedRootPolicyProviderModule(...) or else)
		 */
		final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policySetEvaluatorMap.get(id, constraints);
		if (policyEntry == null)
		{
			return null;
		}

		final StaticTopLevelPolicyElementEvaluator policy = policyEntry.getValue();
		if (policySetRefChainIncludingResult != null)
		{
			/*
			 * check total policy ref depth if policySetRefChainIncludingResult != null, i.e. length of (newAncestorPolicySetRefChain + parsed policySet's longest (nested) policy ref chain) <=
			 * maxPolicySetRefDepth
			 */
			final Optional<PolicyRefsMetadata> policyRefsMetadata = policy.getPolicyRefsMetadata();
			if (policyRefsMetadata.isPresent())
			{
				joinPolicyRefChains(policySetRefChainIncludingResult, policyRefsMetadata.get().getLongestPolicyRefChain());
			}
		}

		return policy;
	}

	@Override
	public void close()
	{
		// Nothing to close - erase exception from the close() signature
	}

}
