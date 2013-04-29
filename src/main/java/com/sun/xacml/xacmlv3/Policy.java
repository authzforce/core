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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameterType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuerType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;

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
import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.ctx.Result;

/**
 * @author Romain Ferrari
 * 
 */
public class Policy extends PolicyType {

	// the meta-data associated with this policy
	private static PolicyMetaData metaData;
	private CombiningAlgorithm ruleCombiningAlg;

	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Policy.class);

	public Policy(String description, PolicyIssuerType issuer,
			DefaultsType policyDefault, Target target, List policyElements,
			ObligationExpressionsType obligations, AdviceExpressionsType advices,
			String policyId, String version, String ruleCombiningAlgId,
			BigInteger maxDelegationDepth) {

		this.description = description;
		this.policyIssuer = issuer;
		this.policyDefaults = policyDefault;
		this.target = target;
		if (policyElements == null) {
			this.combinerParametersOrRuleCombinerParametersOrVariableDefinition = Collections.EMPTY_LIST;
		} else {
			this.combinerParametersOrRuleCombinerParametersOrVariableDefinition = Collections
					.unmodifiableList(new ArrayList(policyElements));
		}
		if (obligations == null) {
			this.obligationExpressions = new ObligationExpressionsType();
		} else {
			this.obligationExpressions = obligations;
		}
		if (advices == null) {
			this.adviceExpressions = new AdviceExpressionsType();
		} else {
			this.adviceExpressions = advices;
		}
		this.policyId = policyId;
		this.version = version;
		this.ruleCombiningAlgId = ruleCombiningAlgId;
		this.maxDelegationDepth = maxDelegationDepth;
		CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
		try {
			this.ruleCombiningAlg = factory.createAlgorithm(URI.create(this.ruleCombiningAlgId));
		} catch (DOMException e) {
			LOGGER.error(e);
		} catch (UnknownIdentifierException e) {
			LOGGER.error(e);
		}		
	}

	public static Policy getInstance(Node root) {
		String ruleCombiningAlgId = null;
		List policyElements = new ArrayList();
		AdviceExpressionsType advices = null;
		ObligationExpressionsType obligations = null;
		PolicyIssuerType issuer = null;
		DefaultsType policyDefault = null;
		Target target = null;
		String description = null;
		String policyId = null;
		String version = null;
		BigInteger maxDelegationDepth = null;
		VariableManager manager = null;

		// Creating the variable manager
		// FIXME: Understand the point of this thing
		 try {
			manager = createVariableManager(root);
		} catch (ParsingException e) {
			LOGGER.error(e);
		}
		 metaData = new PolicyMetaData(root.getNamespaceURI(), null);		 
		// Setting attributes 
		NamedNodeMap attrs = root.getAttributes();
		policyId = attrs.getNamedItem("PolicyId").getNodeValue();
		version = attrs.getNamedItem("Version").getNodeValue();
		ruleCombiningAlgId = attrs.getNamedItem("RuleCombiningAlgId").getNodeValue();
		
		if(attrs.getNamedItem("MaxDelegationDepth") != null) {
			maxDelegationDepth = BigInteger.valueOf(Long.parseLong(attrs.getNamedItem("MaxDelegationDepth").getNodeValue()));
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
				} else if ("PolicyDefaults".equals(DOMHelper.getLocalName(child))) {
					policyDefault = PolicyDefaults.getInstance(child);
				} else if ("Target".equals(DOMHelper.getLocalName(child))) {
					target = Target.getInstance(child, metaData);
				} else if ("CombinerParameters".equals(DOMHelper.getLocalName(child))) {
					throw new ParsingException("Combiner Parameters not implemented yet");
				} else if ("RuleCombinerParameters".equals(DOMHelper.getLocalName(child))) {
					throw new ParsingException("Rule Combiner Parameters not implemented yet");
				} else if ("VariableDefinition".equals(DOMHelper.getLocalName(child))) {
					throw new ParsingException("Variable definition not implemented yet");
				} else if ("Rule".equals(DOMHelper.getLocalName(child))) {
					policyElements.add(Rule.getInstance(child, metaData, manager));
				} else if ("ObligationExpressions".equals(DOMHelper.getLocalName(child))) {
					obligations = ObligationExpressions.getInstance(child);
				} else if ("AdviceExpressions".equals(DOMHelper.getLocalName(child))) {
					advices = AdviceExpressions.getInstance(child);
				}
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		

		return new Policy(description, issuer, policyDefault, target,
				policyElements, obligations, advices, policyId, version,
				ruleCombiningAlgId, maxDelegationDepth);
	}

	private static VariableManager createVariableManager(Node root) throws ParsingException {
		HashMap variableIds = new HashMap();
		// first off, go through and look for any definitions to get their
        // identifiers up front, since before we parse any references we'll
        // need to know what definitions we support
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("VariableDefinition")) {
                String id = child.getAttributes().
                    getNamedItem("VariableId").getNodeValue();

                // it's an error to have more than one definition with the
                // same identifier
                if (variableIds.containsKey(id)) {
                    throw new ParsingException("multiple definitions for " +
                                               "variable " + id);
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
	public Result evaluate(EvaluationCtx context) {
		Result result = null;
		List<Rule> rules = new ArrayList<Rule>();
		CombinerParametersType combParams = new CombinerParametersType();
		CombinerParameter combParam = null;
		for (Object element : this.combinerParametersOrRuleCombinerParametersOrVariableDefinition) {
			if(element instanceof CombinerParametersType) {
				combParams.getCombinerParameter().add((CombinerParameterType) element);
			} else if(element instanceof RuleType) {
				rules.add((Rule) element);
			}
		}
		// evaluate
		result = this.ruleCombiningAlg.combine(context, combParams, rules);

		if (obligationExpressions.getObligationExpression().size() > 0) {
			// now, see if we should add any obligations to the set
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal())
					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return obligations
				return result;
			}

//			if (metaData.getXACMLVersion() == Integer
//					.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value())) {
				for (ObligationExpressionType myObligation : obligationExpressions.getObligationExpression()) {
						if (myObligation.getFulfillOn().ordinal() == effect) {
							result.addObligation(myObligation, context);
						}
				}
//			} else {
//				Iterator it = obligationExpressions.getObligationExpression().iterator();
//				while (it.hasNext()) {
//					Obligation obligation = (Obligation) (it.next());
//					if (obligation.getFulfillOn() == effect) {
//						result.addObligation(obligation);
//					}
//				}
//			}
		}
		/* If we have advice, it's definitely a 3.0 policy */
		if (adviceExpressions.getAdviceExpression().size() > 0) {
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal())
					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return advices
				return result;
			}
			for (AdviceExpressionType adviceExpr : adviceExpressions.getAdviceExpression()) {
				if(adviceExpr.getAppliesTo().ordinal() == effect) {
					AdviceType advice = new AdviceType();
					advice.setAdviceId(adviceExpr.getAdviceId());
					for (AttributeAssignmentExpressionType attrExpr : adviceExpr.getAttributeAssignmentExpression()) {
						AttributeAssignmentType myAttrAssType = new AttributeAssignmentType();
						myAttrAssType.setAttributeId(attrExpr.getAttributeId());
						myAttrAssType.setCategory(attrExpr.getCategory());
						myAttrAssType.getContent().add(attrExpr.getExpression());
						myAttrAssType.setIssuer(attrExpr.getIssuer());
						advice.getAttributeAssignment().add(myAttrAssType);
					}					
				}
			}
			//TODO: Fix advice parsing
//			for (AdviceExpressionsType myAdvices : (Set<AdviceExpressionsType>) advice) {
//				for (AdviceType myAdvice : myAdvices.getAdviceExpression()) {
//					if (myAdvice.getAppliesTo().ordinal() == effect) {
//						AdviceType adviceType = new AdviceType();
//						result.addAdvice(myAdvice);
//					}
//				}
//			}
		}

//		if (context.getIncludeInResults().size() > 0) {
//			result.getAttributes().addAll(context.getIncludeInResults());
//		}

		return result;
	}
	
	public CombiningAlgorithm getRuleCombiningAlg() {
		return ruleCombiningAlg;
	}
	
	public PolicyMetaData getMetaData() {
		return metaData;
	}

}
