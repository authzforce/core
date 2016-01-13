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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.VersionPatterns;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * This is the core implementation of <code>RefPolicyProviderModule</code> that supports static retrieval of the policies referenced by Policy(Set)IdReference.
 * It is configured by a list of locations that represent Spring-compatible resource URLs, corresponding to XACML Policy(Set) files - each file content is
 * expected to be a XACML Policy(Set) document - when the module is initialized. Beyond this, there is no modifying or re-loading the policies handled by this
 * class.
 * <p>
 * A policy location may also be a file pattern in the following form: "file://DIRECTORY_PATH/*SUFFIX" using wilcard character '*'; in which case the location
 * is expanded to all regular files (not subdirectories) in directory located at DIRECTORY_PATH with suffix SUFFIX (SUFFIX may be empty, i.e. no suffix). The
 * files are NOT searched recursively on sub-directories.
 * <p>
 * Note that this class is designed to complement {@link CoreRootPolicyProviderModule} in charge of the root policy(set) which may refer to policies resolved by
 * this {@link CoreRefPolicyProviderModule}.
 */
public class CoreRefPolicyProviderModule implements RefPolicyProviderModule
{
	private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined CombiningAlgorithm registry");
	private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Expression factory");
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined XACML parser factory");
	private static final IllegalArgumentException ILLEGAL_POLICY_URLS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined policy URL(s)");
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreRefPolicyProviderModule.class);

	/**
	 * Policy wrapper to link a XACML policy to the namespace prefix-URIs in original policy XML document
	 *
	 * @param <P>
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

		private PolicyWithNamespaces(P policy, Map<String, String> nsPrefixUriMap)
		{
			this.policy = policy;
			this.nsPrefixUriMap = nsPrefixUriMap;
		}

	}

	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RefPolicyProviderModule.Factory<StaticRefPolicyProvider>
	{

		private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException("RefPolicyProvider configuration undefined");

		@Override
		public Class<StaticRefPolicyProvider> getJaxbClass()
		{
			return StaticRefPolicyProvider.class;
		}

		@Override
		public RefPolicyProviderModule getInstance(StaticRefPolicyProvider conf, XACMLParserFactory xacmlParserFactory, int maxPolicySetRefDepth,
				ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, EnvironmentProperties environmentProperties)
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
						LOGGER.debug("Policy location #{} is a filepath pattern (found '/*') -> expanding to all files in directory '{}' with suffix '{}'",
								policyLocationIndex, directoryLocation, suffix);

						final String dirLocation = environmentProperties.replacePlaceholders(directoryLocation);
						final URL directoryURL;
						try
						{
							directoryURL = ResourceUtils.getURL(dirLocation);
						} catch (FileNotFoundException e)
						{
							throw new IllegalArgumentException("Invalid directory location: '" + dirLocation + "' extracted from policy location (pattern) '"
									+ policyLocation + "'", e);
						}

						final Path directoryPath;
						try
						{
							directoryPath = Paths.get(directoryURL.toURI());
						} catch (URISyntaxException e)
						{
							throw new RuntimeException("Error converting policy directory URL '" + directoryURL
									+ "' - extracted from policy location (pattern) '" + policyLocation + "' - to a Java Path (via URI)", e);
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
						} catch (DirectoryIteratorException ex)
						{
							// I/O error encounted during the iteration, the cause is an IOException
							throw new RuntimeException("Error iterating over files in directory '" + dirLocation
									+ "' to get policies at locations matching pattern '" + policyLocation + "'", ex.getCause());
						} catch (IOException e)
						{
							throw new RuntimeException("Error getting policy files in '" + dirLocation + "' according to policy location pattern '"
									+ policyLocation + "'", e);
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
				} catch (FileNotFoundException e)
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

			return CoreRefPolicyProviderModule.getInstance(policyURLs, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
		}
	}

	private static class PolicyMap<P>
	{
		// Map: Policy(Set)Id -> Version -> Policy(Set), versions sorted from latest to earliest
		final Map<String, PolicyVersions<P>> policiesById = new HashMap<>();

		/**
		 * Adds policy
		 * 
		 * @param policyId
		 *            policy ID
		 * @param policyVersion
		 *            policy version
		 * @return previous value with same ID and version, or null if none
		 */
		private P put(String policyId, String policyVersion, P policy)
		{
			final PolicyVersion version = new PolicyVersion(policyVersion);
			final PolicyVersions<P> oldPolicyVersions = policiesById.get(policyId);
			final PolicyVersions<P> newPolicyVersions;
			if (oldPolicyVersions == null)
			{
				newPolicyVersions = new PolicyVersions<>();
				policiesById.put(policyId, newPolicyVersions);
			} else
			{
				newPolicyVersions = oldPolicyVersions;
			}

			return newPolicyVersions.put(version, policy);
		}

		private Entry<PolicyVersion, P> get(String id, VersionPatterns constraints)
		{
			final PolicyVersions<P> policyVersions = policiesById.get(id);
			// id not matched
			if (policyVersions == null)
			{
				return null;
			}

			return policyVersions.getLatest(constraints);
		}

		private PolicyVersions<P> get(String id)
		{
			return policiesById.get(id);
		}

		private PolicyVersions<P> put(String id, PolicyVersions<P> policyVersions)
		{
			return policiesById.put(id, policyVersions);
		}

		public Set<Entry<String, PolicyVersions<P>>> entrySet()
		{
			return policiesById.entrySet();
		}

		public void clear()
		{
			policiesById.clear();
		}
	}

	/*
	 * Ref policy Provider used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in
	 * PolicySetIdReferences) at initialization time
	 */
	private static class InitOnlyRefPolicyProvider implements RefPolicyProvider
	{

		private final int maxPolicySetRefDepth;
		private final ExpressionFactory expressionFactory;
		private final CombiningAlgRegistry combiningAlgRegistry;
		private final PolicyMap<PolicyEvaluator> policyMap;
		private final PolicyMap<PolicySetEvaluator> policySetMapToUpdate;
		private final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;

		private InitOnlyRefPolicyProvider(PolicyMap<PolicyEvaluator> policyMap, PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap,
				PolicyMap<PolicySetEvaluator> policySetMapToUpdate, int maxPolicySetRefDepth, ExpressionFactory expressionFactory,
				CombiningAlgRegistry combiningAlgRegistry)
		{
			this.policyMap = policyMap;
			this.policySetMapToUpdate = policySetMapToUpdate;
			this.jaxbPolicySetMap = jaxbPolicySetMap;
			this.maxPolicySetRefDepth = maxPolicySetRefDepth;
			this.expressionFactory = expressionFactory;
			this.combiningAlgRegistry = combiningAlgRegistry;
		}

		@Override
		public boolean isStatic()
		{
			return true;
		}

		@Override
		public <POLICY_T extends IPolicyEvaluator> POLICY_T get(Class<POLICY_T> policyType, String id, VersionPatterns constraints,
				Deque<String> ancestorPolicyRefChain) throws IndeterminateEvaluationException, IllegalArgumentException
		{
			// If this is a request for Policy (from PolicyIdReference)
			if (policyType == PolicyEvaluator.class)
			{
				final Entry<PolicyVersion, PolicyEvaluator> policyEntry = policyMap.get(id, constraints);
				return policyEntry == null ? null : policyType.cast(policyEntry.getValue());
			}

			// Else this is a request for PolicySet (from PolicySetIdReference)
			final Deque<String> newPolicySetRefChain = Utils.appendAndCheckPolicyRefChain(ancestorPolicyRefChain, Collections.singletonList(id),
					maxPolicySetRefDepth);
			final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry = jaxbPolicySetMap.get(id, constraints);
			if (jaxbPolicySetEntry == null)
			{
				// no such policy
				return null;
			}

			final PolicyVersion jaxbPolicySetVersion = jaxbPolicySetEntry.getKey();
			// Check whether already parsed
			final PolicyVersions<PolicySetEvaluator> policySetVersions = policySetMapToUpdate.get(id);
			final PolicyVersions<PolicySetEvaluator> newPolicySetVersions;
			if (policySetVersions == null)
			{
				/*
				 * No matching version already parsed, and this is the first version to be parsed for this PolicySetId
				 */
				newPolicySetVersions = new PolicyVersions<>();
				policySetMapToUpdate.put(id, newPolicySetVersions);
			} else
			{
				// policySet already parsed
				final PolicySetEvaluator policySetEvaluator = policySetVersions.get(jaxbPolicySetVersion);
				if (policySetEvaluator != null)
				{
					/*
					 * check total policy ref depth, i.e. length of (newAncestorPolicySetRefChain + parsed policySet's longest (nested) policy ref chain) <=
					 * maxPolicySetRefDepth
					 */
					Utils.appendAndCheckPolicyRefChain(newPolicySetRefChain, policySetEvaluator.getLongestPolicyReferenceChain(), maxPolicySetRefDepth);
					return policyType.cast(policySetEvaluator);
				}

				/*
				 * No matching version already parsed, but there are versions already parsed, so we'll add the new one - that we are about to parse - to them.
				 */
				newPolicySetVersions = policySetVersions;
			}

			// Create the PolicySet
			final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
			final PolicySetEvaluator policySetEvaluator;
			try
			{
				policySetEvaluator = PolicySetEvaluator.getInstance(jaxbPolicySetWithNs.policy, null, jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory,
						combiningAlgRegistry, this, newPolicySetRefChain);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid PolicySet with PolicySetId=" + id + ", Version=" + jaxbPolicySetVersion, e);
			}

			newPolicySetVersions.put(jaxbPolicySetVersion, policySetEvaluator);
			return policyType.cast(policySetEvaluator);
		}
	}

	private final PolicyMap<PolicyEvaluator> policyMap;
	private final PolicyMap<PolicySetEvaluator> policySetMap;
	private final int maxPolicySetRefDepth;

	private CoreRefPolicyProviderModule(PolicyMap<PolicyEvaluator> policyMap, PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap,
			int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
			throws IllegalArgumentException
	{
		assert policyMap != null && jaxbPolicySetMap != null && expressionFactory != null && combiningAlgRegistry != null;

		this.maxPolicySetRefDepth = maxPolicySetRefDepth;
		this.policyMap = policyMap;
		this.policySetMap = new PolicyMap<>();
		/*
		 * Ref policy Provider module used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in
		 * PolicySetIdReferences)
		 */
		final RefPolicyProvider refPolicyProvider = new InitOnlyRefPolicyProvider(policyMap, jaxbPolicySetMap, policySetMap, maxPolicySetRefDepth,
				expressionFactory, combiningAlgRegistry);

		for (final Entry<String, PolicyVersions<PolicyWithNamespaces<PolicySet>>> jaxbPolicySetWithNsEntry : jaxbPolicySetMap.entrySet())
		{
			final String policySetId = jaxbPolicySetWithNsEntry.getKey();
			// Get corresponding PolicySet (versions) in policySetMap to check whether it is not
			// already there, i.e. already parsed
			final PolicyVersions<PolicySetEvaluator> oldPolicySetVersions = policySetMap.get(policySetId);
			final PolicyVersions<PolicySetEvaluator> newPolicySetVersions;
			if (oldPolicySetVersions == null)
			{
				// no corresponding PolicySet already in policySetMap, so prepare to add one
				newPolicySetVersions = new PolicyVersions<>();
				policySetMap.put(policySetId, newPolicySetVersions);
			} else
			{
				newPolicySetVersions = oldPolicySetVersions;
			}

			final PolicyVersions<PolicyWithNamespaces<PolicySet>> jaxbPolicySetVersions = jaxbPolicySetWithNsEntry.getValue();
			for (final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry : jaxbPolicySetVersions)
			{
				final PolicyVersion policySetVersion = jaxbPolicySetEntry.getKey();
				// check whether not already parsed
				final PolicySetEvaluator policySetEvaluator = newPolicySetVersions.get(policySetVersion);
				if (policySetEvaluator != null)
				{
					// we're done, next!
					continue;
				}

				// not already parsed, parse now
				final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
				final PolicySetEvaluator newPolicySet;
				try
				{
					newPolicySet = PolicySetEvaluator.getInstance(jaxbPolicySetWithNs.policy, null, jaxbPolicySetWithNs.nsPrefixUriMap, expressionFactory,
							combiningAlgRegistry, refPolicyProvider, null);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid PolicySet with PolicySetId=" + policySetId + ", Version=" + policySetVersion, e);
				}

				newPolicySetVersions.put(policySetVersion, newPolicySet);
			}
		}
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
	 * @throws IllegalArgumentException
	 *             if both {@code jaxbPoliciesByIdAndVersion} and {@code jaxbPolicySetsByIdAndVersion} are null/empty, or expressionFactory/combiningAlgRegistry
	 *             undefined; or one of the Policy(Set)s is not valid or conflicts with another because it has same Policy(Set)Id and Version.
	 */
	public static CoreRefPolicyProviderModule getInstance(List<PolicyWithNamespaces<Policy>> jaxbPolicies,
			List<PolicyWithNamespaces<PolicySet>> jaxbPolicySets, int maxPolicySetRefDepth, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
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

		final PolicyMap<PolicyEvaluator> policyMap = new PolicyMap<>();
		if (jaxbPolicies != null)
		{
			for (final PolicyWithNamespaces<Policy> jaxbPolicyWithNs : jaxbPolicies)
			{
				final Policy jaxbPolicy = jaxbPolicyWithNs.policy;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final PolicyEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluator.getInstance(jaxbPolicy, null, jaxbPolicyWithNs.nsPrefixUriMap, expressionFactory, combiningAlgRegistry);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final PolicyEvaluator previousValue = policyMap.put(policyId, policyVersion, policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two <Policy>s with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}
			}
		}

		final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap = new PolicyMap<>();
		if (jaxbPolicySets != null)
		{
			for (final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs : jaxbPolicySets)
			{
				final PolicySet jaxbPolicySet = jaxbPolicySetWithNs.policy;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final PolicyWithNamespaces<PolicySet> previousValue = jaxbPolicySetMap.put(policyId, policyVersion, jaxbPolicySetWithNs);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each PolicySet may refer to others via PolicySetIdReferences
				 */
			}
		}

		return new CoreRefPolicyProviderModule(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	/**
	 * Creates an instance from policy locations
	 * 
	 * @param policyURLs
	 *            location of Policy(Set) elements (JAXB) to be parsed for future reference by Policy(Set)IdReferences
	 * @param xacmlParserFactory
	 *            XACML parser factory for parsing any XACML Policy(Set)
	 * @param maxPolicySetRefDepth
	 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @return instance of this class
	 * @throws IllegalArgumentException
	 *             if {@code policyURLs == null || policyURLs.length == 0}, or expressionFactory/combiningAlgRegistry undefined; or one of {@code policyURLs} is
	 *             null or is not a valid XACML Policy(Set) or conflicts with another because it has same Policy(Set)Id and Version. Beware that the
	 *             Policy(Set)Issuer is ignored from this check!
	 * 
	 */
	public static CoreRefPolicyProviderModule getInstance(Collection<URL> policyURLs, XACMLParserFactory xacmlParserFactory, int maxPolicySetRefDepth,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
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
		} catch (JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final PolicyMap<PolicyEvaluator> policyMap = new PolicyMap<>();
		final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap = new PolicyMap<>();
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
			} catch (JAXBException e)
			{
				throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + policyURL, e);
			}

			final Map<String, String> nsPrefixUriMap = parser.getNamespacePrefixUriMap();
			if (jaxbPolicyOrPolicySetObj instanceof Policy)
			{
				final Policy jaxbPolicy = (Policy) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final PolicyEvaluator policyEvaluator;
				try
				{
					policyEvaluator = PolicyEvaluator.getInstance(jaxbPolicy, null, nsPrefixUriMap, expressionFactory, combiningAlgRegistry);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final PolicyEvaluator previousValue = policyMap.put(policyId, policyVersion, policyEvaluator);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two policies with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}

			} else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
			{
				final PolicySet jaxbPolicySet = (PolicySet) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final PolicyWithNamespaces<PolicySet> previousValue = jaxbPolicySetMap.put(policyId, policyVersion, new PolicyWithNamespaces<>(jaxbPolicySet,
						nsPrefixUriMap));
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each PolicySet may refer to others via PolicySetIdReferences
				 */
			} else
			{
				throw new IllegalArgumentException("Unexpected element found as root of the policy document: "
						+ jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
			}

			policyUrlIndex++;
		}

		return new CoreRefPolicyProviderModule(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	@Override
	public final boolean isStatic()
	{
		return true;
	}

	@Override
	public <POLICY_T extends IPolicyEvaluator> POLICY_T get(Class<POLICY_T> policyType, String id, VersionPatterns constraints, Deque<String> policySetRefChain)
			throws IndeterminateEvaluationException, IllegalArgumentException
	{
		if (policyType == PolicyEvaluator.class)
		{
			// Request for Policy (from PolicyIdReference)
			final Entry<PolicyVersion, PolicyEvaluator> policyEntry = policyMap.get(id, constraints);
			if (policyEntry == null)
			{
				return null;
			}

			return policyType.cast(policyEntry.getValue());
		}

		// Request for PolicySet (from PolicySetIdReference)
		final Deque<String> newPolicySetRefChain = Utils.appendAndCheckPolicyRefChain(policySetRefChain, Collections.singletonList(id), maxPolicySetRefDepth);
		final Entry<PolicyVersion, PolicySetEvaluator> policyEntry = policySetMap.get(id, constraints);
		if (policyEntry == null)
		{
			return null;
		}

		/*
		 * check total policy ref depth, i.e. length of (newAncestorPolicySetRefChain + parsed policySet's longest (nested) policy ref chain) <=
		 * maxPolicySetRefDepth
		 */
		final IPolicyEvaluator policy = policyEntry.getValue();
		Utils.appendAndCheckPolicyRefChain(newPolicySetRefChain, policy.getLongestPolicyReferenceChain(), maxPolicySetRefDepth);
		return policyType.cast(policy);
	}

	@Override
	public void close() throws IOException
	{
		this.policyMap.clear();
		this.policySetMap.clear();
	}

}
