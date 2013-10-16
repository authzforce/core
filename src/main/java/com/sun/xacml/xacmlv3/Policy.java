/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ObligationExpressions;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.Rule;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.cond.xacmlv3.Expression;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.audit.annotations.Audit;

/**
 * @author Romain Ferrari
 * 
 */
public class Policy extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy {

	// the meta-data associated with this policy
	private static PolicyMetaData metaData;
	private CombiningAlgorithm ruleCombiningAlg;

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Policy.class);

	public Policy(String description, oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer issuer,
			DefaultsType policyDefault, Target target, List policyElements,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions advices, String policyId, String version,
			String ruleCombiningAlgId, BigInteger maxDelegationDepth) {

		this.description = description;
		this.policyIssuer = issuer;
		this.policyDefaults = policyDefault;
		this.target = target;
		if (policyElements == null) {
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.EMPTY_LIST;
		} else {
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections
					.unmodifiableList(new ArrayList(policyElements));
		}
		if (obligations == null) {
			this.obligationExpressions = new oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions();
		} else {
			this.obligationExpressions = obligations;
		}
		if (advices == null) {
			this.adviceExpressions = new oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions();
		} else {
			this.adviceExpressions = advices;
		}
		this.policyId = policyId;
		this.version = version;
		this.ruleCombiningAlgId = ruleCombiningAlgId;
		this.maxDelegationDepth = maxDelegationDepth;
		CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
		try {
			this.ruleCombiningAlg = factory.createAlgorithm(URI
					.create(this.ruleCombiningAlgId));
		} catch (DOMException e) {
			LOGGER.error("Error instantiating algorithm '{}'", this.ruleCombiningAlgId, e);
		} catch (UnknownIdentifierException e) {
			LOGGER.error("Error instantiating algorithm '{}'", this.ruleCombiningAlgId, e);
		}
	}

	public static Policy getInstance(Node root) {
		String ruleCombiningAlgId = null;
		List policyElements = new ArrayList();
		oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions advices = null;
		oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations = null;
		oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer issuer = null;
		DefaultsType policyDefault = null;
		Target target = null;
		String description = null;
		String policyId = null;
		String version = null;
		BigInteger maxDelegationDepth = null;
		VariableManager manager = null;

		/**
		 *  Creating the variable manager for connecting VariableReference(s) to their corresponding VariableDefinition(s) (see VariableReference class).
		 */
		try {
			manager = createVariableManager(root);
		} catch (ParsingException e) {
			LOGGER.error("Error creating VariableManager, VariableReferences will not be supported in this policy", e);
		}
		metaData = new PolicyMetaData(root.getNamespaceURI(), null);
		// Setting attributes
		NamedNodeMap attrs = root.getAttributes();
		//FIXME: NPE if policyId is null
		policyId = attrs.getNamedItem("PolicyId").getNodeValue();
		//FIXME: NPE if version is null
		version = attrs.getNamedItem("Version").getNodeValue();
		//FIXME: NPE if RuleCombiningAlg Id is null
		ruleCombiningAlgId = attrs.getNamedItem("RuleCombiningAlgId")
				.getNodeValue();

		if (attrs.getNamedItem("MaxDelegationDepth") != null) {
			maxDelegationDepth = BigInteger.valueOf(Long.parseLong(attrs
					.getNamedItem("MaxDelegationDepth").getNodeValue()));
		} else {
			maxDelegationDepth = BigInteger.ZERO;
		}

		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			try {
				Node child = children.item(i);
				if ("Description".equals(DOMHelper.getLocalName(child))) {
					description = child.getNodeValue();
				} else if ("PolicyIssuer".equals(DOMHelper.getLocalName(child))) {
					issuer = PolicyIssuer.getInstance(child);
				} else if ("PolicyDefaults".equals(DOMHelper
						.getLocalName(child))) {
					policyDefault = PolicyDefaults.getInstance(child);
				} else if ("Target".equals(DOMHelper.getLocalName(child))) {
					target = Target.getInstance(child, metaData);
				} else if ("CombinerParameters".equals(DOMHelper
						.getLocalName(child))) {
					throw new ParsingException(
							"Combiner Parameters not implemented yet");
				} else if ("RuleCombinerParameters".equals(DOMHelper
						.getLocalName(child))) {
					throw new ParsingException(
							"Rule Combiner Parameters not implemented yet");
				} else if ("VariableDefinition".equals(DOMHelper
						.getLocalName(child))) {
					throw new ParsingException(
							"Variable definition not implemented yet");
				} else if ("Rule".equals(DOMHelper.getLocalName(child))) {
					policyElements.add(Rule.getInstance(child, metaData,
							manager));
				} else if ("ObligationExpressions".equals(DOMHelper
						.getLocalName(child))) {
					obligations = ObligationExpressions.getInstance(child);
				} else if ("AdviceExpressions".equals(DOMHelper
						.getLocalName(child))) {
					advices = AdviceExpressions.getInstance(child);
				}
			} catch (ParsingException e) {
				LOGGER.error("Error instantiating Policy", e);
			}
		}

		return new Policy(description, issuer, policyDefault, target,
				policyElements, obligations, advices, policyId, version,
				ruleCombiningAlgId, maxDelegationDepth);
	}

	private static VariableManager createVariableManager(Node root)
			throws ParsingException {
		HashMap variableIds = new HashMap();
		// first off, go through and look for any definitions to get their
		// identifiers up front, since before we parse any references we'll
		// need to know what definitions we support
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("VariableDefinition")) {
				String id = child.getAttributes().getNamedItem("VariableId")
						.getNodeValue();

				// it's an error to have more than one definition with the
				// same identifier
				if (variableIds.containsKey(id)) {
					throw new ParsingException("multiple definitions for "
							+ "variable " + id);
				}

				variableIds.put(id, child);
			}
		}

		// now create a manager with the defined variable identifiers
		VariableManager manager = new VariableManager(variableIds, metaData);

		return manager;
	}

	/**
	 * Given the input context sees whether or not the request matches this
	 * policy. This must be called by combining algorithms before they evaluate
	 * a policy. This is also used in the initial policy finding operation to
	 * determine which top-level policies might apply to the request.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the policy and the request
	 */	
	public MatchResult match(EvaluationCtx context) {
		/**
		 * Romain Ferrari (Thales)
		 * 
		 * @BUG: NPE
		 */
		if (target == null) {
			throw new RuntimeException("No target found in policy with id="
					+ policyId);
		}

		return ((Target) target).match(context);
	}

	/**
	 * Tries to evaluate the policy by calling the combining algorithm on the
	 * given policies or rules. The <code>match</code> method must always be
	 * called first, and must always return MATCH, before this method is called.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	@Audit(type = Audit.Type.POLICY)
	public Result evaluate(EvaluationCtx context) {
		Result result = null;
		List<Rule> rules = new ArrayList<Rule>();
		CombinerParametersType combParams = new CombinerParametersType();
		CombinerParameter combParam = null;
		for (Object element : this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions) {
			if (element instanceof CombinerParametersType) {
				combParams.getCombinerParameters().add(
						(oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter) element);
			} else if (element instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule) {
				rules.add((Rule) element);
			}
		}
		// evaluate
		result = this.ruleCombiningAlg.combine(context, combParams, rules);

		if (obligationExpressions.getObligationExpressions().size() > 0) {
			// now, see if we should add any obligations to the set
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal())
					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return obligations
				return result;
			}

			for (ObligationExpression myObligation : obligationExpressions
					.getObligationExpressions()) {
				if (myObligation.getFulfillOn().ordinal() == effect) {
					result.addObligation(myObligation, context);
				}
			}
		}
		/* If we have advice, it's definitely a 3.0 policy */
		if (adviceExpressions.getAdviceExpressions().size() > 0) {
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal())
					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return advices
				return result;
			}
			for (AdviceExpression adviceExpr : adviceExpressions
					.getAdviceExpressions()) {
				if (adviceExpr.getAppliesTo().ordinal() == effect) {
					Advice advice = new Advice();
					advice.setAdviceId(adviceExpr.getAdviceId());
					for (AttributeAssignmentExpression attrExpr : adviceExpr
							.getAttributeAssignmentExpressions()) {
						AttributeAssignment myAttrAssType = new AttributeAssignment();
						myAttrAssType.setAttributeId(attrExpr.getAttributeId());
						myAttrAssType.setCategory(attrExpr.getCategory());
						myAttrAssType.setIssuer(attrExpr.getIssuer());		
						if ((attrExpr.getExpression().getDeclaredType()  == AttributeValueType.class)) {
							myAttrAssType.setDataType(((AttributeValueType)attrExpr.getExpression().getValue()).getDataType());
							myAttrAssType.getContent().addAll(((AttributeValueType)attrExpr.getExpression().getValue()).getContent());
						}
						advice.getAttributeAssignments().add(myAttrAssType);
					}
					result.addAdvice(advice);
				}
			}
		}

		return result;
	}

	public CombiningAlgorithm getRuleCombiningAlg() {
		return ruleCombiningAlg;
	}

	public PolicyMetaData getMetaData() {
		return metaData;
	}

}
