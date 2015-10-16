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
package com.thalesgroup.authzforce.core.policy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.util.ResourceUtils;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.pdp.model._2015._06.BaseStaticRefPolicyFinder;

/**
 * This is a simple implementation of <code>RefPolicyFinderModule</code> that supports static
 * retrieval of the policies referenced by Policy(Set)IdReference. Its constructor accepts locations
 * that represent Spring-compatible resource URLs, and they are resolved to the actual policies when
 * the module is initialized. Beyond this, there is no modifying or re-loading the policies handled
 * by this class.
 * <p>
 * Note that this class is designed to complement <code>BaseStaticPolicyFinderModule</code>. The
 * reason is that when you define a configuration for your PDP, it's easier to specify the two sets
 * of policies by using two different finder modules.
 */
public class BaseStaticRefPolicyFinderModule implements RefPolicyFinderModule
{
	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RefPolicyFinderModule.Factory<BaseStaticRefPolicyFinder>
	{

		@Override
		public Class<BaseStaticRefPolicyFinder> getJaxbClass()
		{
			return BaseStaticRefPolicyFinder.class;
		}

		@Override
		public RefPolicyFinderModule getInstance(BaseStaticRefPolicyFinder conf, int maxPolicySetRefDepth, Expression.Factory expressionFactory, CombiningAlgRegistry combiningAlgRegistry)
		{
			final URL[] policyURLs = new URL[conf.getPolicyLocations().size()];
			int i = 0;
			for (final String policyLocation : conf.getPolicyLocations())
			{
				final URL policyURL;
				try
				{
					// try to load the policy location as a Spring resource
					policyURL = ResourceUtils.getURL(policyLocation);
				} catch (FileNotFoundException ioe)
				{
					throw new IllegalArgumentException("Error loading policy (as Spring resource) from the following URL: " + policyLocation, ioe);
				}

				if (policyURL == null)
				{
					throw new IllegalArgumentException("No policy file found at the specified location: " + policyLocation);
				}

				policyURLs[i] = policyURL;
				i++;
			}

			return BaseStaticRefPolicyFinderModule.getInstance(policyURLs, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
		}
	}

	private static class PolicyVersions<P> implements Iterable<Entry<PolicyVersion, P>>
	{
		/*
		 * Version-to-policy map with reverse ordering, to have the latest version first, since, by
		 * default, the latest version is always preferred. See §5.10 of XACML core spec:
		 * "In the case that more than one matching version can be obtained, then the most recent one SHOULD be used."
		 */
		final TreeMap<PolicyVersion, P> policiesByVersion = new TreeMap<>(Collections.reverseOrder());

		private P put(PolicyVersion version, P policy)
		{
			return policiesByVersion.put(version, policy);
		}

		private P get(PolicyVersion version)
		{
			return policiesByVersion.get(version);
		}

		private Entry<PolicyVersion, P> getLatest(VersionConstraints constraints)
		{
			// policiesByVersion is not empty -> at least one value
			final Iterator<Entry<PolicyVersion, P>> versionPolicyPairsIterator = policiesByVersion.entrySet().iterator();
			if (constraints == null)
			{
				/*
				 * Return the latest version which is the first element by design (TreeMap
				 * initialized with reverse order on version keys). See §5.10 of XACML core spec:
				 * "In the case that more than one matching version can be obtained, then the most recent one SHOULD be used."
				 */
				return versionPolicyPairsIterator.next();
			}

			// constraints not null
			// in the loop, go on until LatestVersion matched, then go on as long as EarliestVersion
			// matched, if Version matched, return the result
			boolean latestVersionMatched = false;
			boolean earliestVersionMatched = false;
			while (versionPolicyPairsIterator.hasNext())
			{
				final Entry<PolicyVersion, P> versionPolicyPair = versionPolicyPairsIterator.next();
				final PolicyVersion version = versionPolicyPair.getKey();
				/*
				 * Versions ordered by latest first, so check against constraints' LatestVersion
				 * pattern first. If LatestVersion is matched by this version, no need to check
				 * again for the next versions, as they are already sorted from latest to earliest.
				 * If LatestVersion not matched yet, we check now.
				 */
				if (!latestVersionMatched)
				{
					latestVersionMatched = constraints.matchLatestVersion(version);
				}

				// If LatestVersion matched, check other constraints, else do nothing (check next
				// version)
				if (latestVersionMatched)
				{
					/*
					 * If EarliestVersion already checked and not matched before, we would have
					 * returned null (see below). So at this point, EarliestVersion is either not
					 * checked yet or already matched. So EarliestVersion no checked iff not already
					 * matched.
					 */
					if (!earliestVersionMatched)
					{
						// EarliestVersion not checked yet
						// check against EarliestVersion pattern
						earliestVersionMatched = constraints.matchEarliestVersion(version);
						/*
						 * If still not matched, version cannot be in the [EarliestVersion,
						 * LatestVersion] interval. All next versions are earlier, so they cannot be
						 * either -> no match
						 */
						if (!earliestVersionMatched)
						{
							return null;
						}
					}

					// EarliestVersion and LatestVersion matched.
					// Check against Version pattern
					if (constraints.matchVersion(version))
					{
						// all constraints matched, return the associated policy
						return versionPolicyPair;
					}

					// constraints not met, so try next version
				}
			}

			// no match found
			return null;
		}

		@Override
		public Iterator<Entry<PolicyVersion, P>> iterator()
		{
			return policiesByVersion.entrySet().iterator();
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

		private Entry<PolicyVersion, P> get(String id, VersionConstraints constraints)
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
	 * Ref policy finder module used only for initialization, more particularly for parsing the
	 * PolicySets when they are referred to by others (in PolicySetIdReferences) at initialization
	 * time
	 */
	private static class InitOnlyRefPolicyFinderModule implements RefPolicyFinderModule
	{

		private final int maxPolicySetRefDepth;
		private final Expression.Factory expressionFactory;
		private final CombiningAlgRegistry combiningAlgRegistry;
		private final PolicyMap<Policy> policyMap;
		private final PolicyMap<PolicySet> policySetMapToUpdate;
		private final PolicyMap<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetMap;

		private InitOnlyRefPolicyFinderModule(PolicyMap<Policy> policyMap, PolicyMap<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetMap, PolicyMap<PolicySet> policySetMapToUpdate, int maxPolicySetRefDepth, Expression.Factory expressionFactory,
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
		public void close() throws IOException
		{
		}

		@Override
		public boolean isStatic()
		{
			return true;
		}

		@Override
		public <POLICY_T extends IPolicy> POLICY_T findPolicy(String id, Class<POLICY_T> policyType, VersionConstraints constraints, Deque<String> policySetRefChain) throws IndeterminateEvaluationException, ParsingException
		{
			// If this is a request for Policy (from PolicyIdReference)
			if (policyType == Policy.class)
			{
				final Entry<PolicyVersion, Policy> policyEntry = policyMap.get(id, constraints);
				return policyEntry == null ? null : policyType.cast(policyEntry.getValue());
			}

			// Else this is a request for PolicySet (from PolicySetIdReference)
			final Entry<PolicyVersion, oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetEntry = jaxbPolicySetMap.get(id, constraints);
			if (jaxbPolicySetEntry == null)
			{
				// no such policy
				return null;
			}

			final PolicyVersion jaxbPolicySetVersion = jaxbPolicySetEntry.getKey();
			// Check whether already parsed
			final PolicyVersions<PolicySet> policySetVersions = policySetMapToUpdate.get(id);
			final PolicyVersions<PolicySet> newPolicySetVersions;
			if (policySetVersions != null)
			{
				final PolicySet policySet = policySetVersions.get(jaxbPolicySetVersion);
				if (policySet != null)
				{
					return policyType.cast(policySet);
				}

				/*
				 * No matching version already parsed, but there are versions already parsed, so
				 * we'll add the new one - that we are about to parse - to them.
				 */
				newPolicySetVersions = policySetVersions;
			} else
			{
				/*
				 * No matching version already parsed, and this is the first version to be parsed
				 * for this PolicySetId
				 */
				newPolicySetVersions = new PolicyVersions<>();
				policySetMapToUpdate.put(id, newPolicySetVersions);
			}

			// Create the PolicySet
			final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet jaxbPolicySet = jaxbPolicySetEntry.getValue();
			final RefPolicyFinder refPolicyFinder = new RefPolicyFinder(InitOnlyRefPolicyFinderModule.this, maxPolicySetRefDepth);
			final PolicySet policySet;
			try
			{
				policySet = new PolicySet(jaxbPolicySet, null, expressionFactory, combiningAlgRegistry, refPolicyFinder, policySetRefChain);
			} catch (ParsingException e)
			{
				throw new IllegalArgumentException("Error parsing PolicySet with PolicySetId=" + id + ", Version=" + jaxbPolicySetVersion, e);
			}

			newPolicySetVersions.put(jaxbPolicySetVersion, policySet);
			return policyType.cast(policySet);
		}
	}

	private final PolicyMap<Policy> policyMap;
	private final PolicyMap<PolicySet> policySetMap;

	private BaseStaticRefPolicyFinderModule(PolicyMap<Policy> policyMap, PolicyMap<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetMap, int maxPolicySetRefDepth, final Expression.Factory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
			throws IllegalArgumentException
	{
		assert policyMap != null && jaxbPolicySetMap != null && expressionFactory != null && combiningAlgRegistry != null;

		this.policyMap = policyMap;
		this.policySetMap = new PolicyMap<>();
		if (jaxbPolicySetMap != null)
		{
			/*
			 * Ref policy finder module used only for initialization, more particularly for parsing
			 * the PolicySets when they are referred to by others (in PolicySetIdReferences)
			 */
			final RefPolicyFinderModule initOnlyRefPolicyFinderMod = new InitOnlyRefPolicyFinderModule(policyMap, jaxbPolicySetMap, policySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
			for (final Entry<String, PolicyVersions<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet>> jaxbPolicySet : jaxbPolicySetMap.entrySet())
			{
				final String policySetId = jaxbPolicySet.getKey();
				// Get corresponding PolicySet (versions) in policySetMap to check whether it is not
				// already there, i.e. already parsed
				final PolicyVersions<PolicySet> oldPolicySetVersions = policySetMap.get(policySetId);
				final PolicyVersions<PolicySet> newPolicySetVersions;
				if (oldPolicySetVersions == null)
				{
					// no corresponding PolicySet already in policySetMap, so prepare to add one
					newPolicySetVersions = new PolicyVersions<>();
					policySetMap.put(policySetId, newPolicySetVersions);
				} else
				{
					newPolicySetVersions = oldPolicySetVersions;
				}

				final PolicyVersions<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetVersions = jaxbPolicySet.getValue();
				for (final Entry<PolicyVersion, oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetEntry : jaxbPolicySetVersions)
				{
					final PolicyVersion policySetVersion = jaxbPolicySetEntry.getKey();
					// check whether not already parsed
					final PolicySet policySet = newPolicySetVersions.get(policySetVersion);
					if (policySet != null)
					{
						// we're done, next!
						continue;
					}

					// not already parsed, parse now
					final RefPolicyFinder refPolicyFinder = new RefPolicyFinder(initOnlyRefPolicyFinderMod, maxPolicySetRefDepth);
					final PolicySet newPolicySet;
					try
					{
						newPolicySet = new PolicySet(jaxbPolicySetEntry.getValue(), null, expressionFactory, combiningAlgRegistry, refPolicyFinder, null);
					} catch (ParsingException e)
					{
						throw new IllegalArgumentException("Error parsing PolicySet with PolicySetId=" + policySetId + ", Version=" + policySetVersion, e);
					}

					newPolicySetVersions.put(policySetVersion, newPolicySet);
				}
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
	 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference):
	 *            PolicySet1 -> PolicySet2 -> ...
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @return instance of this module
	 * @throws IllegalArgumentException
	 *             if both {@code jaxbPoliciesByIdAndVersion} and
	 *             {@code jaxbPolicySetsByIdAndVersion} are null/empty, or
	 *             expressionFactory/combiningAlgRegistry undefined; or one of the Policy(Set)s is
	 *             not valid or conflicts with another because it has same Policy(Set)Id and
	 *             Version.
	 */
	public static BaseStaticRefPolicyFinderModule getInstance(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy> jaxbPolicies, List<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySets, int maxPolicySetRefDepth, Expression.Factory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		if ((jaxbPolicies == null || jaxbPolicies.isEmpty()) && (jaxbPolicySets == null || jaxbPolicySets.isEmpty()))
		{
			throw new IllegalArgumentException("No Policy(Set) specified");
		}

		if (expressionFactory == null)
		{
			throw new IllegalArgumentException("Undefined Expression factory");
		}

		if (combiningAlgRegistry == null)
		{
			throw new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		}

		final PolicyMap<Policy> policyMap = new PolicyMap<>();
		if (jaxbPolicies != null)
		{
			for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy jaxbPolicy : jaxbPolicies)
			{
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final Policy policy;
				try
				{
					policy = new Policy(jaxbPolicy, null, expressionFactory, combiningAlgRegistry);
				} catch (ParsingException e)
				{
					throw new IllegalArgumentException("Error parsing Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final Policy previousValue = policyMap.put(policyId, policyVersion, policy);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two <Policy>s with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}
			}
		}

		final PolicyMap<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetMap = new PolicyMap<>();
		if (jaxbPolicySets != null)
		{
			for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet jaxbPolicySet : jaxbPolicySets)
			{
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet previousValue = jaxbPolicySetMap.put(policyId, policyVersion, jaxbPolicySet);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each
				 * PolicySet may refer to others via PolicySetIdReferences
				 */
			}
		}

		return new BaseStaticRefPolicyFinderModule(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	/**
	 * Creates an instance from policy locations
	 * 
	 * @param policyURLs
	 *            location of Policy(Set) elements (JAXB) to be parsed for future reference by
	 *            Policy(Set)IdReferences
	 * @param maxPolicySetRefDepth
	 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference):
	 *            PolicySet1 -> PolicySet2 -> ...
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @return instance of this class
	 * @throws IllegalArgumentException
	 *             if {@code policyURLs == null || policyURLs.length == 0}, or
	 *             expressionFactory/combiningAlgRegistry undefined; or one of {@code policyURLs} is
	 *             null or is not a valid XACML Policy(Set) or conflicts with another because it has
	 *             same Policy(Set)Id and Version. Beware that the Policy(Set)Issuer is ignored from
	 *             this check!
	 * 
	 */
	public static BaseStaticRefPolicyFinderModule getInstance(URL[] policyURLs, int maxPolicySetRefDepth, Expression.Factory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		if (policyURLs == null || policyURLs.length == 0)
		{
			throw new IllegalArgumentException("Undefined policy URL(s)");
		}

		if (expressionFactory == null)
		{
			throw new IllegalArgumentException("Undefined Expression factory");
		}

		if (combiningAlgRegistry == null)
		{
			throw new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		}

		final PolicyMap<Policy> policyMap = new PolicyMap<>();
		final PolicyMap<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> jaxbPolicySetMap = new PolicyMap<>();
		for (int i = 0; i < policyURLs.length; i++)
		{
			final URL policyURL = policyURLs[i];
			if (policyURL == null)
			{
				throw new IllegalArgumentException("policyURLs[" + i + "] undefined");
			}

			final Unmarshaller unmarshaller;
			try
			{
				unmarshaller = XACMLBindingUtils.createXacml3Unmarshaller();
			} catch (JAXBException e)
			{
				throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
			}

			final Object jaxbPolicyOrPolicySetObj;
			try
			{
				jaxbPolicyOrPolicySetObj = unmarshaller.unmarshal(policyURL);
			} catch (JAXBException e)
			{
				throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + policyURL, e);
			}

			if (jaxbPolicyOrPolicySetObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy jaxbPolicy = (oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicy.getPolicyId();
				final String policyVersion = jaxbPolicy.getVersion();
				final Policy policy;
				try
				{
					policy = new Policy(jaxbPolicy, null, expressionFactory, combiningAlgRegistry);
				} catch (ParsingException e)
				{
					throw new IllegalArgumentException("Error parsing Policy with PolicyId=" + policyId + ", Version=" + policyVersion, e);
				}

				final Policy previousValue = policyMap.put(policyId, policyVersion, policy);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two policies with same PolicyId=" + policyId + ", Version=" + policyVersion);
				}

			} else if (jaxbPolicyOrPolicySetObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet jaxbPolicySet = (oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) jaxbPolicyOrPolicySetObj;
				final String policyId = jaxbPolicySet.getPolicySetId();
				final String policyVersion = jaxbPolicySet.getVersion();
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet previousValue = jaxbPolicySetMap.put(policyId, policyVersion, jaxbPolicySet);
				if (previousValue != null)
				{
					throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersion);
				}

				/*
				 * PolicySets cannot be parsed before we have collected them all, because each
				 * PolicySet may refer to others via PolicySetIdReferences
				 */
			} else
			{
				throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
			}
		}

		return new BaseStaticRefPolicyFinderModule(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry);
	}

	@Override
	public final boolean isStatic()
	{
		return true;
	}

	@Override
	public <POLICY_T extends IPolicy> POLICY_T findPolicy(String id, Class<POLICY_T> policyType, VersionConstraints constraints, Deque<String> policySetRefChain) throws IndeterminateEvaluationException, ParsingException
	{
		final Entry<PolicyVersion, ? extends IPolicy> policyEntry;
		if (policyType == Policy.class)
		{
			// Request for Policy (from PolicyIdReference)
			policyEntry = policyMap.get(id, constraints);
		} else
		{
			// Request for PolicySet (from PolicySetIdReference)
			policyEntry = policySetMap.get(id, constraints);
		}

		return policyEntry == null ? null : policyType.cast(policyEntry.getValue());
	}

	@Override
	public void close() throws IOException
	{
		this.policyMap.clear();
		this.policySetMap.clear();
	}

}
