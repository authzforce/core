/*
 * Copyright 2012-2023 THALES.
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This is the core implementation of {@link BaseStaticPolicyProvider} that supports static retrieval of the policies referenced by Policy(Set)IdReference. It is configured by a list of locations that
 * represent Spring-compatible resource URLs, corresponding to XACML Policy(Set) files - each file content is expected to be a XACML Policy(Set) document - when the module is initialized. Beyond this,
 * there is no modifying or re-loading of the policies.
 * <p>
 * A policy location may also be a file pattern in the following form: "file://DIRECTORY_PATH/*SUFFIX" using wilcard character '*'; in which case the location is expanded to all regular files (not
 * subdirectories) in directory located at DIRECTORY_PATH with suffix <i>SUFFIX</i> (SUFFIX may be empty, i.e. no suffix). The files are NOT searched recursively on subdirectories.
 * <p>
 *
 * @version $Id: $
 */
public class CoreStaticPolicyProvider extends BaseStaticPolicyProvider
{
    private static final IllegalArgumentException NO_POLICY_ARG_EXCEPTION = new IllegalArgumentException("No Policy(Set) specified");
    private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined CombiningAlgorithm registry");
    private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Expression factory");
    private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML parser factory");
    private static final IllegalArgumentException ILLEGAL_POLICY_URLS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined policy URL(s)");
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreStaticPolicyProvider.class);

    /**
     * Policy wrapper to keep the association between the namespace prefix-URIs from a XACML policy document and the Java instance of the policy resulting from parsing the same document
     *
     * @param <P> policy type
     */
    public static final class PolicyWithNamespaces<P>
    {
        private final ImmutableMap<String, String> nsPrefixUriMap;

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

        private PolicyWithNamespaces(final P policy, final ImmutableMap<String, String> nsPrefixUriMap)
        {
            this.policy = policy;
            this.nsPrefixUriMap = nsPrefixUriMap;
        }

    }

    private interface StaticPolicyProviderInParam
    {
        /*
         * Marker interface for input parameters of a Static Policy Provider (either inline policy or policy locations)
         */
    }

    private static final class XacmlPolicyParam implements StaticPolicyProviderInParam
    {

        private final PolicySet policy;

        private XacmlPolicyParam(final PolicySet policy)
        {
            assert policy != null;
            this.policy = policy;
        }
    }

    private static final class PolicyLocationParam implements StaticPolicyProviderInParam
    {
        private final URL policyLocation;

        private PolicyLocationParam(final URL policyLocation)
        {
            assert policyLocation != null;
            this.policyLocation = policyLocation;
        }
    }

    /**
     * Module factory
     */
    public static class Factory extends CloseablePolicyProvider.Factory<org.ow2.authzforce.core.xmlns.pdp.StaticPolicyProvider>
    {

        private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException("PolicyProvider configuration undefined");

        /*
         * Pattern: **...**.somefileextension -> '**...*'.length = number of directory levels to search and '*.somefileextension' = filename pattern
         */
        private static final Pattern WILDCARD_SEQ_PREFIX_PATTERN = Pattern.compile("^(\\*+)([^*]*)$");

        @Override
        public Class<org.ow2.authzforce.core.xmlns.pdp.StaticPolicyProvider> getJaxbClass()
        {
            return org.ow2.authzforce.core.xmlns.pdp.StaticPolicyProvider.class;
        }

        @Override
        public CloseablePolicyProvider<?> getInstance(final org.ow2.authzforce.core.xmlns.pdp.StaticPolicyProvider conf, final XmlnsFilteringParserFactory xacmlParserFactory,
                                                      final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final EnvironmentProperties environmentProperties,
                                                      final Optional<PolicyProvider<?>> otherHelpingPolicyProvider)
        {
            if (conf == null)
            {
                throw NULL_CONF_ARGUMENT_EXCEPTION;
            }

            final List<StaticPolicyProviderInParam> providerParams = new ArrayList<>();
            for (final Object policySetOrLocationPatternBeforePlaceholderReplacement : conf.getPolicySetsAndPolicyLocations())
            {
                if (policySetOrLocationPatternBeforePlaceholderReplacement instanceof PolicySet)
                {
                    providerParams.add(new XacmlPolicyParam((PolicySet) policySetOrLocationPatternBeforePlaceholderReplacement));
                } else if (policySetOrLocationPatternBeforePlaceholderReplacement instanceof String)
                {
                    final String policyLocationPatternBeforePlaceholderReplacement = (String) policySetOrLocationPatternBeforePlaceholderReplacement;
                    final String policyLocationPattern = environmentProperties.replacePlaceholders(policyLocationPatternBeforePlaceholderReplacement);
                    /*
                    policyLocationPattern is handled like a URL pattern, e.g. file://path/to/policy(ies)
                     Check whether the location is a file path pattern
                     */
                    if (policyLocationPattern.startsWith(ResourceUtils.FILE_URL_PREFIX))
                    {
                        if (policyLocationPattern.endsWith("/"))
                        {
                            throw new IllegalArgumentException("Invalid policy location pattern: " + policyLocationPattern);
                        }

                        // location on the filesystem
                        final int index = policyLocationPattern.indexOf("/*");
                        if (index > 0)
                        {
                            /*
                             * This is a file URL pattern. Separate directory location from glob pattern,
                             * directoryLocation is handled like a URL pattern, e.g. file://path/to/policy_directory
                             */
                            final String directoryLocation = policyLocationPattern.substring(0, index);
                            final String filePathPattern = policyLocationPattern.substring(index + 1);
                            if (LOGGER.isDebugEnabled())
                            {
                                // Beware of autoboxing which causes call to
                                // Integer.valueOf(...) on policyLocationIndex
                                LOGGER.debug("Policy location '{}' is a filepath pattern (found '/*') -> expanding to all files in directory '{}' matching pattern '{}'",
                                        policyLocationPatternBeforePlaceholderReplacement, directoryLocation, filePathPattern);
                            }

                            /*
                             * filePathPattern starts with one or more wildcards (recursive directory listing)
                             */
                            final Matcher filePathPatternMatcher = WILDCARD_SEQ_PREFIX_PATTERN.matcher(filePathPattern);
                            if (!filePathPatternMatcher.matches())
                            {
                                throw new IllegalArgumentException("Invalid policy location: '" + policyLocationPatternBeforePlaceholderReplacement + "'. Pattern part does not match regex: "
                                        + WILDCARD_SEQ_PREFIX_PATTERN.pattern());
                            }

                            /*
                             * First captured group is the sequence of wildcards except the last one, directory levels to search = number of wildcards
                             */
                            final String wildcardSeq = filePathPatternMatcher.group(1);
                            final String filenameSuffix = filePathPatternMatcher.group(2);
                            /*
                             * WilcardSeq should start with '*'
                             */
                            assert wildcardSeq != null;
                            final int maxDepth = wildcardSeq.length();
                            /*
                             * Filename suffix is filenamePattern without starting wildcard
                             */
                            final Path directoryPath = Paths.get(URI.create(directoryLocation));
                            try (Stream<Path> fileStream = Files.find(directoryPath, maxDepth,
                                    (path, attrs) ->
                                    {
                                        if (!attrs.isRegularFile())
                                        {
                                            return false;
                                        }
                                        final Path filename = path.getFileName();
                                        if (filename == null)
                                        {
                                            return false;
                                        }

                                        return filename.toString().endsWith(
                                                filenameSuffix.substring(1));
                                    }))
                            {
                                fileStream.forEach(fp ->
                                {
                                    LOGGER.debug("Adding policy file: {}", fp);
                                    try
                                    {
                                        providerParams.add(new PolicyLocationParam(fp.toUri().toURL()));
                                    } catch (final MalformedURLException e)
                                    {
                                        throw new RuntimeException("Error getting policy files in '" + directoryPath + "' according to policy location pattern '" + policyLocationPattern + "'", e);
                                    }
                                });
                            } catch (final IOException e)
                            {
                                throw new RuntimeException("Error getting policy files in '" + directoryPath + "' according to policy location pattern '" + policyLocationPattern + "'", e);
                            }

                            continue;
                        }
                    }

                    /*
                     * Not an actual file path pattern
                     */
                    final URL policyURL;
                    try
                    {
                        // try to load the policy location as a Spring resource
                        policyURL = ResourceUtils.getURL(policyLocationPattern);
                    } catch (final FileNotFoundException e)
                    {
                        throw new IllegalArgumentException("Error loading policy (as Spring resource) from the following URL: " + policyLocationPattern, e);
                    }

					/*
					If no exception occurred, ResourceUtils#getURL() returns non-null value
					 */
                    providerParams.add(new PolicyLocationParam(policyURL));
                } else
                {
                    throw new UnsupportedOperationException("Unsupported type of input: " + policySetOrLocationPatternBeforePlaceholderReplacement.getClass());
                }
            }

            final Optional<StaticPolicyProvider> otherHelpingStaticPolicyProvider;
            if (otherHelpingPolicyProvider.isEmpty())
            {
                otherHelpingStaticPolicyProvider = Optional.empty();
            } else
            {
                final PolicyProvider<?> provider = otherHelpingPolicyProvider.get();
                if (provider instanceof StaticPolicyProvider)
                {
                    otherHelpingStaticPolicyProvider = Optional.of((StaticPolicyProvider) provider);
                } else
                {
                    LOGGER.warn(
                            "otherHelpingPolicyprovider (composition of previously instantiated policy providers) is not an instance of {} therefore ignored by this new {} instance. This type of provider may use other policy provider(s) (previously declared in PDP configuration) only if they all implement {}.",
                            StaticPolicyProvider.class, this.getClass().getCanonicalName(), StaticPolicyProvider.class);
                    otherHelpingStaticPolicyProvider = Optional.empty();
                }
            }

            return CoreStaticPolicyProvider.getInstance(providerParams, conf.isIgnoreOldVersions(), xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry,
                    otherHelpingStaticPolicyProvider);
        }
    }

    /*
     * Policy Provider used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in PolicySetIdReferences) at initialization time
     */
    private static final class InitOnlyPolicyProvider extends BaseStaticPolicyProvider
    {

        private final ExpressionFactory expressionFactory;
        private final CombiningAlgRegistry combiningAlgRegistry;

        // will be updated by get(...)
        private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap;
        private final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap;
        private final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> policySetMapToUpdate;
        private final Optional<StaticPolicyProvider> otherPolicyProvider;

        private InitOnlyPolicyProvider(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap, final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap,
                                       final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> outPolicySetEvaluatorMap, final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory,
                                       final CombiningAlgRegistry combiningAlgRegistry, final Optional<StaticPolicyProvider> otherPolicyProvider)
        {
            super(maxPolicySetRefDepth);

            this.policyMap = policyMap;
            this.policySetMapToUpdate = outPolicySetEvaluatorMap;
            this.jaxbPolicySetMap = jaxbPolicySetMap;
            this.expressionFactory = expressionFactory;
            this.combiningAlgRegistry = combiningAlgRegistry;
            this.otherPolicyProvider = otherPolicyProvider;
        }

        @Override
        protected StaticTopLevelPolicyElementEvaluator getPolicy(final String policyId, final Optional<PolicyVersionPatterns> versionConstraints) throws IndeterminateEvaluationException
        {
            final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyMap.get(policyId, versionConstraints);
            if (policyEntry == null)
            {
                return this.otherPolicyProvider.isPresent() ? this.otherPolicyProvider.get().get(TopLevelPolicyElementType.POLICY, policyId, versionConstraints, null) : null;
            }

            return policyEntry.getValue();
        }

        @Override
        public StaticTopLevelPolicyElementEvaluator getPolicySet(final String policyId, final Optional<PolicyVersionPatterns> versionConstraints, final Deque<String> policySetRefChain)
                throws IndeterminateEvaluationException
        {
            final Entry<PolicyVersion, PolicyWithNamespaces<PolicySet>> jaxbPolicySetEntry = jaxbPolicySetMap.get(policyId, versionConstraints);
            if (jaxbPolicySetEntry == null)
            {
                /*
                 * No such policy unless provided by other static policy providers
                 */
                return this.otherPolicyProvider.isPresent() ? this.otherPolicyProvider.get().get(TopLevelPolicyElementType.POLICY_SET, policyId, versionConstraints, policySetRefChain) : null;
            }

            final PolicyVersion jaxbPolicySetVersion = jaxbPolicySetEntry.getKey();
            // Check whether already parsed
            final StaticTopLevelPolicyElementEvaluator policySetEvaluator = policySetMapToUpdate.get(policyId, jaxbPolicySetVersion);
            final StaticTopLevelPolicyElementEvaluator resultPolicySetEvaluator;
            if (policySetEvaluator == null)
            {
                /*
                 * No matching version already parsed. Instantiate the policy evaluator
                 */
                final PolicyWithNamespaces<PolicySet> jaxbPolicySetWithNs = jaxbPolicySetEntry.getValue();
                try
                {
                    resultPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, expressionFactory, combiningAlgRegistry, this,
                            policySetRefChain, Optional.empty(), jaxbPolicySetWithNs.nsPrefixUriMap);
                } catch (final IllegalArgumentException e)
                {
                    throw new IllegalArgumentException("Invalid PolicySet with PolicySetId=" + policyId + ", Version=" + jaxbPolicySetVersion, e);
                }

                policySetMapToUpdate.put(policyId, jaxbPolicySetVersion, resultPolicySetEvaluator);
            } else
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
                    policyRefsMetadata.ifPresent(refsMetadata -> joinPolicyRefChains(policySetRefChain, refsMetadata.getLongestPolicyRefChain()));
                }
            }

            return resultPolicySetEvaluator;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.ow2.authzforce.core.pdp.api.policy.CloseablePolicyProvider#close()
         */
        @Override
        public void close()
        {
            // Nothing to close - erase exception from the close() signature
        }
    }

    private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyEvaluatorMap;
    private final PolicyMap<StaticTopLevelPolicyElementEvaluator> policySetEvaluatorMap;

    private CoreStaticPolicyProvider(final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap, final PolicyMap<PolicyWithNamespaces<PolicySet>> jaxbPolicySetMap, final int maxPolicySetRefDepth,
                                     final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final Optional<StaticPolicyProvider> otherPolicyProvider) throws IllegalArgumentException
    {
        super(maxPolicySetRefDepth);
        assert policyMap != null && jaxbPolicySetMap != null && expressionFactory != null && combiningAlgRegistry != null;

        this.policyEvaluatorMap = policyMap;
        final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicySetEvaluatorTable = HashBasedTable.create();
        /*
         * Policy Provider module used only for initialization, more particularly for parsing the PolicySets when they are referred to by others (in PolicySetIdReferences)
         */
        try (InitOnlyPolicyProvider bootstrapPolicyProvider = new InitOnlyPolicyProvider(this.policyEvaluatorMap, jaxbPolicySetMap, updatablePolicySetEvaluatorTable, maxPolicySetRefDepth,
                expressionFactory, combiningAlgRegistry, otherPolicyProvider))
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
                     * Check corresponding PolicySet evaluator in policySetEvaluatorTable to check whether it is not already there, i.e. already instantiated by policyProvider.get(...) because of
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
                            newPolicySetEvaluator = PolicyEvaluators.getInstanceStatic(jaxbPolicySetWithNs.policy, expressionFactory, combiningAlgRegistry,
                                    bootstrapPolicyProvider, null, Optional.empty(), jaxbPolicySetWithNs.nsPrefixUriMap);
                        } catch (final IllegalArgumentException e)
                        {
                            throw new IllegalArgumentException("Invalid PolicySet with PolicySetId='" + policySetId + "', Version=" + policySetVersion, e);
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
     * @param jaxbPolicies         XACML Policies
     * @param jaxbPolicySets       XACML PolicySets
     * @param maxPolicySetRefDepth maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...
     * @param combiningAlgRegistry registry of policy/rule combining algorithms
     * @param expressionFactory    Expression factory for parsing Expressions used in the policy(set)
     * @param otherPolicyProvider  other (supporting) policy provider, used to resolve policy references that match neither {@code jaxbPolicies} nor {@code jaxbPolicySets}
     * @return instance of this module
     * @throws java.lang.IllegalArgumentException if both {@code jaxbPolicies} and {@code jaxbPolicySets} are null/empty, or expressionFactory/combiningAlgRegistry undefined; or one of the Policy(Set)s is not valid or conflicts
     *                                            with another because it has same Policy(Set)Id and Version.
     */
    public static CoreStaticPolicyProvider getInstance(final List<PolicyWithNamespaces<Policy>> jaxbPolicies, final List<PolicyWithNamespaces<PolicySet>> jaxbPolicySets,
                                                       final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final Optional<StaticPolicyProvider> otherPolicyProvider)
            throws IllegalArgumentException
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
            policyMap = new PolicyMap<>(Collections.emptyMap());
        } else
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
                    /*
                    XPath compiler shall be initialized in PolicyEvaluators#getInstance(...) based on PolicyDefaults/XPathVersion if present
                     */
                    policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, expressionFactory, combiningAlgRegistry, Optional.empty(), jaxbPolicyWithNs.nsPrefixUriMap);
                } catch (final IllegalArgumentException e)
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
            jaxbPolicySetMap = new PolicyMap<>(Collections.emptyMap());
        } else
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

        return new CoreStaticPolicyProvider(policyMap, jaxbPolicySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, otherPolicyProvider);
    }

    /**
     * Creates an instance from policy locations
     *
     * @param providerParams          location of Policy(Set) elements (JAXB) to be parsed for future reference by Policy(Set)IdReferences
     * @param ignoreOldPolicyVersions for any given policy ID, ignore all versions except the last one if there are multiple versions of the policy
     * @param xacmlParserFactory      XACML parser factory for parsing any XACML Policy(Set)
     * @param maxPolicySetRefDepth    maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; a strictly negative value means no limit
     * @param combiningAlgRegistry    registry of policy/rule combining algorithms
     * @param expressionFactory       Expression factory for parsing Expressions used in the policy(set)
     * @param otherPolicyProvider     other (supporting) policy provider, used to resolve policy references that do not match any of {@code providerParams}
     * @return instance of this class
     * @throws java.lang.IllegalArgumentException if {@code policyURLs == null || policyURLs.length == 0 || xacmlParserFactory == null || expressionFactory == null || combiningAlgRegistry == null}; or one of {@code policyURLs} is
     *                                            null or is not a valid XACML Policy(Set) or conflicts with another because it has same Policy(Set)Id and Version. Beware that the Policy(Set)Issuer is ignored from this check!
     */
    public static CoreStaticPolicyProvider getInstance(final List<StaticPolicyProviderInParam> providerParams, final boolean ignoreOldPolicyVersions,
                                                       final XmlnsFilteringParserFactory xacmlParserFactory, final int maxPolicySetRefDepth, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry,
                                                       final Optional<StaticPolicyProvider> otherPolicyProvider) throws IllegalArgumentException
    {
        if (providerParams == null || providerParams.isEmpty())
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
        } catch (final JAXBException e)
        {
            throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
        }

        final Table<String, PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicyTable = HashBasedTable.create();
        final Table<String, PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicySetTable = HashBasedTable.create();
        int providerParamIndex = 0;
        for (final StaticPolicyProviderInParam providerParam : providerParams)
        {
            if (providerParam == null)
            {
                throw new IllegalArgumentException("Policy provider parameter #" + providerParamIndex + " undefined");
            }

            final Object jaxbPolicyOrPolicySetObj;
            if (providerParam instanceof XacmlPolicyParam)
            {
                jaxbPolicyOrPolicySetObj = ((XacmlPolicyParam) providerParam).policy;
            } else
            {
                final URL policyURL = ((PolicyLocationParam) providerParam).policyLocation;
                try
                {
                    jaxbPolicyOrPolicySetObj = xacmlParser.parse(policyURL);
                } catch (final JAXBException e)
                {
                    throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + policyURL, e);
                }
            }

            final ImmutableMap<String, String> nsPrefixUriMap = xacmlParser.getNamespacePrefixUriMap();
            if (jaxbPolicyOrPolicySetObj instanceof Policy)
            {
                final Policy jaxbPolicy = (Policy) jaxbPolicyOrPolicySetObj;
                final String policyId = jaxbPolicy.getPolicyId();
                final String policyVersionStr = jaxbPolicy.getVersion();
                final PolicyVersion policyVersion = new PolicyVersion(policyVersionStr);

                if (ignoreOldPolicyVersions)
                {
                    final Map<PolicyVersion, StaticTopLevelPolicyElementEvaluator> updatablePolicyVersions = updatablePolicyTable.row(policyId);
                    // Empty map returned if no mappings
                    final boolean isOld = updatablePolicyVersions.keySet().parallelStream().anyMatch(v -> policyVersion.compareTo(v) <= 0);
                    if (isOld)
                    {
                        // skip
                        continue;
                    }

                    /*
                     * Else replace/overwrite with this new version (make sure it is the only one), so empty the row first
                     */
                    updatablePolicyVersions.clear();
                }

                final StaticTopLevelPolicyElementEvaluator policyEvaluator;
                try
                {
                    /*
                    XPath compiler shall be initialized in PolicyEvaluators#getInstance(...) based on PolicyDefaults/XPathVersion if present
                     */
                    policyEvaluator = PolicyEvaluators.getInstance(jaxbPolicy, expressionFactory, combiningAlgRegistry, Optional.empty(), nsPrefixUriMap);
                } catch (final IllegalArgumentException e)
                {
                    throw new IllegalArgumentException("Invalid Policy with PolicyId=" + policyId + ", Version=" + policyVersionStr, e);
                }

                final StaticTopLevelPolicyElementEvaluator previousValue = updatablePolicyTable.put(policyId, policyVersion, policyEvaluator);
                if (previousValue != null)
                {
                    throw new IllegalArgumentException("Policy conflict: two policies with same PolicyId=" + policyId + ", Version=" + policyVersionStr);
                }

            } else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
            {
                final PolicySet jaxbPolicySet = (PolicySet) jaxbPolicyOrPolicySetObj;
                final String policyId = jaxbPolicySet.getPolicySetId();
                final String policyVersionStr = jaxbPolicySet.getVersion();
                final PolicyVersion policyVersion = new PolicyVersion(policyVersionStr);

                if (ignoreOldPolicyVersions)
                {
                    final Map<PolicyVersion, PolicyWithNamespaces<PolicySet>> updatablePolicyVersions = updatablePolicySetTable.row(policyId);
                    // Empty map returned if no mapping
                    final boolean isOld = updatablePolicyVersions.keySet().parallelStream().anyMatch(v -> policyVersion.compareTo(v) <= 0);
                    if (isOld)
                    {
                        // skip
                        continue;
                    }

                    /*
                     * Else replace/overwrite with this new version (make sure it is the only one), so empty the row first
                     */
                    updatablePolicyVersions.clear();
                }

                final PolicyWithNamespaces<PolicySet> previousValue = updatablePolicySetTable.put(policyId, policyVersion, new PolicyWithNamespaces<>(jaxbPolicySet, nsPrefixUriMap));
                if (previousValue != null)
                {
                    throw new IllegalArgumentException("Policy conflict: two PolicySets with same PolicySetId=" + policyId + ", Version=" + policyVersionStr);
                }

                /*
                 * PolicySets cannot be parsed before we have collected them all, because each PolicySet may refer to others via PolicySetIdReferences
                 */
            } else
            {
                throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
            }

            providerParamIndex++;
        }

        final PolicyMap<StaticTopLevelPolicyElementEvaluator> policyMap = new PolicyMap<>(updatablePolicyTable.rowMap());
        final PolicyMap<PolicyWithNamespaces<PolicySet>> policySetMap = new PolicyMap<>(updatablePolicySetTable.rowMap());
        return new CoreStaticPolicyProvider(policyMap, policySetMap, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, otherPolicyProvider);
    }

    @Override
    protected StaticTopLevelPolicyElementEvaluator getPolicy(final String id, final Optional<PolicyVersionPatterns> constraints)
    {
        final Entry<PolicyVersion, StaticTopLevelPolicyElementEvaluator> policyEntry = policyEvaluatorMap.get(id, constraints);
        if (policyEntry == null)
        {
            return null;
        }

        return policyEntry.getValue();
    }

    @Override
    protected StaticTopLevelPolicyElementEvaluator getPolicySet(final String id, final Optional<PolicyVersionPatterns> constraints, final Deque<String> policySetRefChainIncludingResult)
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
            policyRefsMetadata.ifPresent(refsMetadata -> joinPolicyRefChains(policySetRefChainIncludingResult, refsMetadata.getLongestPolicyRefChain()));
        }

        return policy;
    }

    /**
     * Returns the latest version of the policy if there is only one in #{@code policyMap}; else null.
     */
    private static <P> PrimaryPolicyMetadata getCandidateRootPolicy(final TopLevelPolicyElementType policyElementType, final PolicyMap<P> policyMap)
    {
        final Iterator<Entry<String, PolicyVersions<P>>> policyEvaluatorsIt = policyMap.entrySet().iterator();
        if (!policyEvaluatorsIt.hasNext())
        {
            /*
             * No policy
             */
            return null;
        }

        /*
         * There is at least one policy
         */

        final Entry<String, PolicyVersions<P>> firstPolicyEvaluatorEntry = policyEvaluatorsIt.next();
        /*
         * If there is only one policy, it is the candidate root policy; else we don't know which one so return none.
         */
        if (policyEvaluatorsIt.hasNext())
        {
            return null;
        }

        /*
         * There is only one policy, use the latest version as candidate root policy
         */
        final Entry<PolicyVersion, P> latestPolicyVersion = firstPolicyEvaluatorEntry.getValue().getLatest(Optional.empty());
        assert latestPolicyVersion != null;
        return new BasePrimaryPolicyMetadata(policyElementType, firstPolicyEvaluatorEntry.getKey(), latestPolicyVersion.getKey());
    }

    /**
     * Returns the candidate root policy which is in this case determined as follows: if there is one and only one Policy provided, return the latest version of this Policy; else if there is one and
     * only one PolicySet, return the latest version of this PolicySet; else none.
     */
    @Override
    public Optional<PrimaryPolicyMetadata> getCandidateRootPolicy()
    {
        /*
         * Look for the one and only Policy
         */
        final PrimaryPolicyMetadata candidateRootPolicy = getCandidateRootPolicy(TopLevelPolicyElementType.POLICY, this.policyEvaluatorMap);
        if (candidateRootPolicy != null)
        {
            return Optional.of(candidateRootPolicy);
        }

        /*
         * No single Policy, try with PolicySet
         */
        final PrimaryPolicyMetadata candidateRootPolicySet = getCandidateRootPolicy(TopLevelPolicyElementType.POLICY_SET, this.policySetEvaluatorMap);
        if (candidateRootPolicySet != null)
        {
            return Optional.of(candidateRootPolicySet);
        }

        /*
         * No single policy(set)
         */
        return Optional.empty();
    }

    @Override
    public void close()
    {
        // Nothing to close - erase exception from the close() signature
    }

}
