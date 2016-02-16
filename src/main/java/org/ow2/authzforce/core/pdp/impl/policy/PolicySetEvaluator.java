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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

import org.ow2.authzforce.core.pdp.api.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.impl.combining.BaseCombiningAlgParameter;
import org.ow2.authzforce.xacml.identifiers.XACMLNodeName;

/**
 * PolicySet Evaluator
 * 
 */
public final class PolicySetEvaluator extends
		GenericPolicyEvaluator<IPolicyEvaluator> {

	// MUST BE IMMUTABLE
	private transient final List<String> longestPolicyRefChain;
	private transient final Map<String, PolicyVersion> staticRefPolicies;

	private PolicySetEvaluator(
			String policyId,
			String version,
			Target target,
			String policyCombiningAlgId,
			List<? extends IPolicyEvaluator> policyEvaluators,
			List<CombiningAlgParameter<? extends IPolicyEvaluator>> policyCombinerParameters,
			ObligationExpressions obligationExpressions,
			AdviceExpressions adviceExpressions, Set<String> localVariableIDs,
			XPathCompiler defaultXPathCompiler,
			ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry,
			List<String> longestPolicyRefChain,
			Map<String, PolicyVersion> staticRefPolicies)
			throws IllegalArgumentException {
		super(IPolicyEvaluator.class, JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY
				.createPolicySetIdReference(new IdReferenceType(policyId,
						version, null, null)), target, policyCombiningAlgId,
				policyEvaluators, policyCombinerParameters,
				obligationExpressions, adviceExpressions, localVariableIDs,
				defaultXPathCompiler, expressionFactory, combiningAlgRegistry);
		this.longestPolicyRefChain = longestPolicyRefChain == null ? null
				: Collections.unmodifiableList(longestPolicyRefChain);
		this.staticRefPolicies = staticRefPolicies == null ? null : Collections
				.unmodifiableMap(staticRefPolicies);
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML
	 * model
	 * 
	 * @param policyElement
	 *            PolicySet (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default
	 *            XPath version, or null if either no parent or no default XPath
	 *            version defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML
	 *            PolicySet (XML) document, to be used for namespace-aware XPath
	 *            evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyProvider
	 *            policy-by-reference (Policy(Set)IdReference) Provider to find
	 *            references used in this policyset
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to this
	 *            PolicySet, if any: PolicySet Ref 1 -> PolicySet Ref 2 -> ...
	 *            -> Ref n -> this. This allows to detect circular references
	 *            and validate the size of the chain against the max depth
	 *            enforced by {@code refPolicyProvider}. This may be null if no
	 *            ancestor, e.g. a PolicySetIdReference in a top-level
	 *            PolicySet. Beware that we only keep the IDs in the chain, and
	 *            not the version, because we consider that a reference loop on
	 *            the same policy ID is not allowed, no matter what the version
	 *            is.
	 * @return instance
	 * @throws IllegalArgumentException
	 *             if PolicyElement is invalid
	 */
	public static PolicySetEvaluator getInstance(PolicySet policyElement,
			XPathCompiler parentDefaultXPathCompiler,
			Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgorithmRegistry,
			RefPolicyProvider refPolicyProvider, Deque<String> policySetRefChain)
			throws IllegalArgumentException {
		final String policyId = policyElement.getPolicySetId();
		final String policyVersion = policyElement.getVersion();
		final String policyFriendlyId = "Policy[" + policyId + "#v"
				+ policyVersion + "]";
		final DefaultsType policyDefaults = policyElement
				.getPolicySetDefaults();
		// Inherited PolicyDefaults is policyDefaults if not null, the
		// parentPolicyDefaults otherwise
		final XPathCompiler defaultXPathCompiler;
		if (policyDefaults == null) {
			defaultXPathCompiler = parentDefaultXPathCompiler;
		} else {
			try {
				defaultXPathCompiler = XMLUtils.newXPathCompiler(
						policyDefaults.getXPathVersion(),
						namespacePrefixesByURI);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						policyFriendlyId
								+ ": Invalid PolicySetDefaults/XPathVersion or XML namespace prefix/URI undefined",
						e);
			}
		}

		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in
		 * Policy? If there were, we would keep a copy of variable IDs defined
		 * in this policy, to remove them from the global manager at the end of
		 * parsing this PolicySet. They should not be visible outside the scope
		 * of this. final Set<String> variableIds = new HashSet<>();
		 */

		/*
		 * Map to get child Policies by their ID so that we can resolve Policies
		 * associated with CombinerParameters
		 */
		final Map<String, PolicyEvaluator> childPoliciesById = new HashMap<>();

		/*
		 * Map to get child PolicySets by their ID so that we can resolve
		 * PolicySets associated with CombinerParameters
		 */
		final Map<String, PolicySetEvaluator> childPolicySetsById = new HashMap<>();

		final List<IPolicyEvaluator> combinedChildElements = new ArrayList<>();
		List<String> longestPolicyRefChain = null;
		Map<String, PolicyVersion> staticallyReferencedPolicies = new HashMap<>();
		final List<CombiningAlgParameter<? extends IPolicyEvaluator>> policyCombinerParameters = new ArrayList<>();
		int childIndex = 0;
		for (final Object policySetChildElt : policyElement
				.getPolicySetsAndPoliciesAndPolicySetIdReferences()) {
			if (policySetChildElt instanceof PolicyCombinerParameters) {
				final String combinedPolicyId = ((PolicyCombinerParameters) policySetChildElt)
						.getPolicyIdRef();
				final PolicyEvaluator combinedPolicy = childPoliciesById
						.get(combinedPolicyId);
				if (combinedPolicy == null) {
					throw new IllegalArgumentException(
							policyFriendlyId
									+ ":  invalid PolicyCombinerParameters: referencing undefined child Policy #"
									+ combinedPolicyId
									+ " (no such policy defined before this element)");
				}

				final BaseCombiningAlgParameter<PolicyEvaluator> combinerElt;
				try {
					combinerElt = new BaseCombiningAlgParameter<>(
							combinedPolicy,
							((CombinerParametersType) policySetChildElt)
									.getCombinerParameters(),
							expressionFactory, defaultXPathCompiler);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(policyFriendlyId
							+ ": invalid child #" + childIndex
							+ " (PolicyCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);

			} else if (policySetChildElt instanceof PolicySetCombinerParameters) {
				final String combinedPolicySetId = ((PolicySetCombinerParameters) policySetChildElt)
						.getPolicySetIdRef();
				final PolicySetEvaluator combinedPolicySet = childPolicySetsById
						.get(combinedPolicySetId);
				if (combinedPolicySet == null) {
					throw new IllegalArgumentException(
							policyFriendlyId
									+ ":  invalid PolicySetCombinerParameters: referencing undefined child PolicySet #"
									+ combinedPolicySetId
									+ " (no such policySet defined before this element)");
				}

				final BaseCombiningAlgParameter<PolicySetEvaluator> combinerElt;
				try {
					combinerElt = new BaseCombiningAlgParameter<>(
							combinedPolicySet,
							((CombinerParametersType) policySetChildElt)
									.getCombinerParameters(),
							expressionFactory, defaultXPathCompiler);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(policyFriendlyId
							+ ": invalid child #" + childIndex
							+ " (PolicySetCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);
			} else if (policySetChildElt instanceof JAXBElement) {
				final JAXBElement<?> jaxbElt = (JAXBElement<?>) policySetChildElt;
				final String eltNameLocalPart = jaxbElt.getName()
						.getLocalPart();
				if (eltNameLocalPart.equals(XACMLNodeName.POLICY_ID_REFERENCE
						.value())) {
					if (refPolicyProvider == null) {
						throw new IllegalArgumentException(
								policyFriendlyId
										+ ": invalid child #"
										+ childIndex
										+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
					}

					final IdReferenceType idRef = (IdReferenceType) jaxbElt
							.getValue();
					final PolicyReferenceEvaluator<PolicyEvaluator> policyRef = GenericPolicyEvaluator
							.getPolicyRefEvaluator(idRef, refPolicyProvider,
									PolicyEvaluator.class, null);
					combinedChildElements.add(policyRef);
				} else if (eltNameLocalPart
						.equals(XACMLNodeName.POLICYSET_ID_REFERENCE.value())) {
					if (refPolicyProvider == null) {
						throw new IllegalArgumentException(
								policyFriendlyId
										+ ": invalid child #"
										+ childIndex
										+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
					}

					final IdReferenceType idRef = (IdReferenceType) jaxbElt
							.getValue();
					final PolicyReferenceEvaluator<PolicySetEvaluator> childElement = GenericPolicyEvaluator
							.getPolicyRefEvaluator(idRef, refPolicyProvider,
									PolicySetEvaluator.class, policySetRefChain);
					// update longest policy ref chain depending on the length
					// of the longest in this child policy element
					final List<String> childLongestPolicyRefChain = childElement
							.getLongestPolicyReferenceChain();
					if (childLongestPolicyRefChain != null
							&& (longestPolicyRefChain == null || childLongestPolicyRefChain
									.size() > longestPolicyRefChain.size())) {
						longestPolicyRefChain = childLongestPolicyRefChain;
					}

					if (staticallyReferencedPolicies != null) {
						final PolicyVersion childVersion = childElement
								.getPolicyVersion();
						if (childVersion == null) {
							// the policy version or some policy reference is
							// not statically resolved, so staticRefPolicies
							// definitely null
							staticallyReferencedPolicies = null;
						} else {
							final Map<String, PolicyVersion> childStaticRefPolicies = childElement
									.getStaticRefPolicies();
							if (childStaticRefPolicies == null) {
								staticallyReferencedPolicies = null;
							} else {
								for (final Entry<String, PolicyVersion> childRefPolicy : childStaticRefPolicies
										.entrySet()) {
									updateStaticRefPolicies(policyFriendlyId,
											staticallyReferencedPolicies,
											childRefPolicy.getKey(),
											childRefPolicy.getValue());
								}

								updateStaticRefPolicies(policyFriendlyId,
										staticallyReferencedPolicies,
										childElement.getPolicyId(),
										childElement.getPolicyVersion());
							}
						}
					}

					combinedChildElements.add(childElement);
				} else if (eltNameLocalPart
						.equals(XACMLNodeName.COMBINER_PARAMETERS.value())) {
					// CombinerParameters that is not
					// Policy(Set)CombinerParameters already tested
					// before
					final BaseCombiningAlgParameter<IPolicyEvaluator> combinerElt;
					try {
						combinerElt = new BaseCombiningAlgParameter<>(null,
								((CombinerParametersType) jaxbElt.getValue())
										.getCombinerParameters(),
								expressionFactory, defaultXPathCompiler);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(policyFriendlyId
								+ ": invalid child #" + childIndex
								+ " (CombinerParameters)", e);
					}

					policyCombinerParameters.add(combinerElt);
				}
			} else if (policySetChildElt instanceof PolicySet) {
				final PolicySetEvaluator childElement;
				try {
					/*
					 * This child PolicySet may have PoliSetIdReferences as well
					 * and therefore update the policySetRefChain. However, if
					 * the current policySetRefChain is updated directly by a
					 * child PolicySet instantiation, then it is no longer valid
					 * for the other child PolicySets of this same PolicySet. So
					 * we need to pass a copy to
					 * PolicySetEvaluator.getInstance(() to avoid that
					 * inconsistency.
					 */
					childElement = PolicySetEvaluator.getInstance(
							(PolicySet) policySetChildElt,
							defaultXPathCompiler, namespacePrefixesByURI,
							expressionFactory, combiningAlgorithmRegistry,
							refPolicyProvider, policySetRefChain == null ? null
									: new ArrayDeque<>(policySetRefChain));
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
							policyFriendlyId + ": Invalid child #" + childIndex
									+ " (PolicySet)", e);
				}

				// update longest policy ref chain depending on the length of
				// the longest in this child policy element
				final List<String> childLongestPolicyRefChain = childElement
						.getLongestPolicyReferenceChain();
				if (childLongestPolicyRefChain != null
						&& (longestPolicyRefChain == null || childLongestPolicyRefChain
								.size() > longestPolicyRefChain.size())) {
					longestPolicyRefChain = childLongestPolicyRefChain;
				}

				childPolicySetsById.put(childElement.getPolicyId(),
						childElement);
				combinedChildElements.add(childElement);
			} else if (policySetChildElt instanceof Policy) {
				final PolicyEvaluator childPolicy;
				try {
					childPolicy = PolicyEvaluator.getInstance(
							(Policy) policySetChildElt, defaultXPathCompiler,
							namespacePrefixesByURI, expressionFactory,
							combiningAlgorithmRegistry);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(policyFriendlyId
							+ ": invalid child #" + childIndex + " (Policy)", e);
				}

				childPoliciesById.put(childPolicy.getPolicyId(), childPolicy);
				combinedChildElements.add(childPolicy);
			}

			/*
			 * Why isn't there any VariableDefinition in XACML PolicySet defined
			 * by OASIS XACML 3.0 spec, like in Policy? If there were, the
			 * following code would be used.
			 */
			// else if (policySetChildElt instanceof VariableDefinition)
			// {
			// final VariableDefinition varDef = (VariableDefinition)
			// policySetChildElt;
			// final VariableReference var;
			// try
			// {
			// var = expFactory.addVariable(varDef);
			// } catch (ParsingException e)
			// {
			// throw new ParsingException(this + ": Error parsing child #" +
			// childIndex +
			// " (VariableDefinition)", e);
			// }
			//
			// if (var != null)
			// {
			// // Conflicts can occur between variables defined in this
			// policySet but also with
			// // others defined in parent/ancestor policySet
			// throw new ParsingException(this +
			// ": Duplicable VariableDefinition for VariableId=" +
			// var.getVariableId());
			// }
			//
			// variableIds.add(varDef.getVariableId());
			// }

			childIndex++;
		}

		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in
		 * Policy? If there were, the final following code would be used: We are
		 * done parsing expressions in this policy, including
		 * VariableReferences, it's time to remove variables scoped to this
		 * policy from the variable manager
		 */
		// for (final String varId : variableIds)
		// {
		// expFactory.remove(varId);
		// }
		final Set<String> localVariableIds = Collections.emptySet();
		return new PolicySetEvaluator(policyId, policyVersion,
				policyElement.getTarget(),
				policyElement.getPolicyCombiningAlgId(), combinedChildElements,
				policyCombinerParameters,
				policyElement.getObligationExpressions(),
				policyElement.getAdviceExpressions(), localVariableIds,
				defaultXPathCompiler, expressionFactory,
				combiningAlgorithmRegistry, longestPolicyRefChain,
				staticallyReferencedPolicies);
	}

	private static void updateStaticRefPolicies(String policyFriendlyId,
			Map<String, PolicyVersion> staticRefPoliciesToUpdate,
			String newChildRefPolicyId, PolicyVersion newChildRefPolicyVersion) {
		final PolicyVersion otherReferencedVersion = staticRefPoliciesToUpdate
				.put(newChildRefPolicyId, newChildRefPolicyVersion);
		if (otherReferencedVersion != null) {
			throw new IllegalArgumentException(policyFriendlyId
					+ ": policy references to same policy ID ("
					+ newChildRefPolicyId + ") but different versions ("
					+ otherReferencedVersion + " and "
					+ newChildRefPolicyVersion
					+ ") - from different places - is not allowed");
		}

	}

	@Override
	public List<String> getLongestPolicyReferenceChain() {
		return this.longestPolicyRefChain;
	}

	@Override
	public Map<String, PolicyVersion> getStaticRefPolicies() {
		return this.staticRefPolicies;
	}

}
