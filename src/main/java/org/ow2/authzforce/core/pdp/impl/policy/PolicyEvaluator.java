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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import org.ow2.authzforce.core.pdp.api.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.VariableReference;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.impl.combining.BaseCombiningAlgParameter;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;

/**
 * Evaluates a XACML Policy to a Decision.
 * 
 */
public final class PolicyEvaluator extends GenericPolicyEvaluator<RuleEvaluator>
{

	private PolicyEvaluator(String policyId, String version, Target target, String ruleCombiningAlgId, List<RuleEvaluator> ruleEvaluators,
			List<CombiningAlgParameter<? extends RuleEvaluator>> ruleCombinerParameters, ObligationExpressions obligationExpressions,
			AdviceExpressions adviceExpressions, Set<String> localVariableIDs, XPathCompiler defaultXPathCompiler, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		super(RuleEvaluator.class, JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createPolicyIdReference(new IdReferenceType(policyId, version, null, null)), target,
				ruleCombiningAlgId, ruleEvaluators, ruleCombinerParameters, obligationExpressions, adviceExpressions, localVariableIDs, defaultXPathCompiler,
				expressionFactory, combiningAlgRegistry);
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML model
	 * 
	 * @param policyElement
	 *            Policy (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicyDefaults/XPathVersion; null if this Policy has no parent Policy (root), or none defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML Policy (XML) document, to be used for namespace-aware XPath evaluation; null or empty
	 *            iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgRegistry
	 *            rule/policy combining algorithm registry
	 * @return instance
	 * @throws IllegalArgumentException
	 *             if Policy element is invalid
	 */
	public static PolicyEvaluator getInstance(Policy policyElement, XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		final String policyId = policyElement.getPolicyId();
		final String policyVersion = policyElement.getVersion();
		final String policyFriendlyId = "Policy[" + policyId + "#v" + policyVersion + "]";
		final DefaultsType policyDefaults = policyElement.getPolicyDefaults();

		// Inherited PolicyDefaults is this.policyDefaults if not null, the
		// parentPolicyDefaults otherwise
		final XPathCompiler defaultXPathCompiler;
		if (policyDefaults == null)
		{
			defaultXPathCompiler = parentDefaultXPathCompiler;
		} else
		{
			try
			{
				defaultXPathCompiler = XMLUtils.newXPathCompiler(policyDefaults.getXPathVersion(), namespacePrefixesByURI);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(policyFriendlyId + ": Invalid PolicyDefaults/XPathVersion or XML namespace prefix/URI undefined", e);
			}

		}

		final List<RuleEvaluator> ruleEvaluators = new ArrayList<>();
		final List<CombiningAlgParameter<? extends RuleEvaluator>> ruleCombinerParameters = new ArrayList<>();

		/*
		 * Keep a copy of locally-defined variable IDs defined in this policy, to remove them from the global manager at the end of parsing this policy. They
		 * should not be visible outside the scope of this policy.
		 */
		final Set<String> localVariableIds = new HashSet<>();
		/*
		 * Map to get rules by their ID so that we can resolve rules associated with CombinerParameters
		 */
		final Map<String, RuleEvaluator> rulesById = new HashMap<>();
		int childIndex = 0;
		for (final Object policyChildElt : policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions())
		{
			if (policyChildElt instanceof RuleCombinerParameters)
			{
				final String combinedRuleId = ((RuleCombinerParameters) policyChildElt).getRuleIdRef();
				final RuleEvaluator combinedRule = rulesById.get(combinedRuleId);
				if (combinedRule == null)
				{
					throw new IllegalArgumentException(policyFriendlyId + ":  invalid RuleCombinerParameters: referencing undefined child Rule #"
							+ combinedRuleId + " (no such rule defined before this element)");
				}

				final BaseCombiningAlgParameter<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(combinedRule, ((CombinerParametersType) policyChildElt).getCombinerParameters(),
							expressionFactory, defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (RuleCombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof CombinerParametersType)
			{
				// CombinerParameters that is not RuleCombinerParameters already tested before
				final BaseCombiningAlgParameter<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(null, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory,
							defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (CombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof VariableDefinition)
			{
				final VariableDefinition varDef = (VariableDefinition) policyChildElt;
				final VariableReference<?> var;
				try
				{
					var = expressionFactory.addVariable(varDef, defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (VariableDefinition)", e);
				}

				if (var != null)
				{
					/*
					 * Conflicts can occur between variables defined in this policy but also with others already in a wider scope, i.e. defined in
					 * parent/ancestor policy
					 */
					throw new IllegalArgumentException(policyFriendlyId + ": Duplicable VariableDefinition for VariableId=" + var.getVariableId());
				}

				localVariableIds.add(varDef.getVariableId());
			} else if (policyChildElt instanceof Rule)
			{
				final RuleEvaluator ruleEvaluator;
				try
				{
					ruleEvaluator = new RuleEvaluator((Rule) policyChildElt, defaultXPathCompiler, expressionFactory);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": Error parsing child #" + childIndex + " (Rule)", e);
				}

				rulesById.put(ruleEvaluator.getRuleId(), ruleEvaluator);
				ruleEvaluators.add(ruleEvaluator);
			}

			childIndex++;
		}

		final PolicyEvaluator policyEvaluator = new PolicyEvaluator(policyId, policyVersion, policyElement.getTarget(), policyElement.getRuleCombiningAlgId(),
				ruleEvaluators, ruleCombinerParameters, policyElement.getObligationExpressions(), policyElement.getAdviceExpressions(),
				Collections.unmodifiableSet(localVariableIds), defaultXPathCompiler, expressionFactory, combiningAlgRegistry);

		/*
		 * We are done parsing expressions in this policy, including VariableReferences, it's time to remove variables scoped to this policy from the variable
		 * manager
		 */
		for (final String varId : localVariableIds)
		{
			expressionFactory.removeVariable(varId);
		}

		return policyEvaluator;
	}

	@Override
	public List<String> getLongestPolicyReferenceChain()
	{
		// a Policy does not have any policy reference
		return null;
	}

	@Override
	public Map<String, PolicyVersion> getStaticRefPolicies() {
		// a Policy does not have any policy reference
		return Collections.emptyMap();
	}

}
