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
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.audit.annotations.Audit;

public class Policy extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy implements IPolicy
{

	// the meta-data associated with this policy
	private PolicyMetaData metaData;
	private RuleCombiningAlgorithm ruleCombiningAlg;

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Policy.class);

	/**
	 * Low-level Policy constructor
	 * 
	 * @param description
	 * @param issuer
	 * @param policyDefault
	 * @param target
	 * @param policyElements
	 * @param obligations
	 * @param advices
	 * @param policyId
	 * @param version
	 * @param ruleCombiningAlgId
	 * @param maxDelegationDepth
	 * @param metadata
	 * @throws UnknownIdentifierException
	 *             Unknown rule combining algorithm ID
	 */
	public Policy(String description, oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer issuer, DefaultsType policyDefault, Target target,
			List<Rule> policyElements, oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions advices, String policyId, String version, String ruleCombiningAlgId,
			BigInteger maxDelegationDepth, PolicyMetaData metadata) throws UnknownIdentifierException
	{

		this.metaData = metadata;
		this.description = description;
		this.policyIssuer = issuer;
		this.policyDefaults = policyDefault;
		this.target = target;
		if (policyElements == null)
		{
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.EMPTY_LIST;
		} else
		{
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.unmodifiableList(new ArrayList(policyElements));
		}
		if (obligations == null)
		{
			/*
			 * obligationExpressions must be null, if you create new ObligationExpressions() in this
			 * case, the result Obligations will be marshalled to empty <Obligations /> element
			 * which is NOT VALID against the XACML schema.
			 */
			this.obligationExpressions = null;
		} else
		{
			this.obligationExpressions = obligations;
		}
		if (advices == null)
		{
			/*
			 * adviceExpressions must be null, if you create new AdviceExpressions() in this case,
			 * the result AdviceExpressions will be marshalled to empty <AssociateAdvice /> element
			 * which is NOT VALID against the XACML schema.
			 */
			this.adviceExpressions = null;
		} else
		{
			this.adviceExpressions = advices;
		}
		this.policyId = policyId;
		this.version = version;
		this.ruleCombiningAlgId = ruleCombiningAlgId;
		this.maxDelegationDepth = maxDelegationDepth;
		CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
		this.ruleCombiningAlg = factory.createAlgorithm(URI.create(this.ruleCombiningAlgId));

	}

	/**
	 * Creates Policy handler handling Policy document loaded via DOM API FIXME: Support
	 * CombinerParameters, RuleCombinerParameters, VariableDefinition
	 * 
	 * @param root
	 * @return Policy handler
	 * @throws UnknownIdentifierException unknown rule combining algorithm ID
	 */
	public static Policy getInstance(Node root) throws UnknownIdentifierException
	{
		String ruleCombiningAlgId = null;
		final List<Rule> policyElements = new ArrayList<>();
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
		PolicyMetaData metaData = new PolicyMetaData(root.getNamespaceURI(), null);

		/**
		 * Creating the variable manager for connecting VariableReference(s) to their corresponding
		 * VariableDefinition(s) (see VariableReference class). FIXME: VariableManager is useless
		 * here as VariableDefinition is not supported
		 */
		try
		{
			manager = createVariableManager(root, metaData);
		} catch (ParsingException e)
		{
			LOGGER.error("Error creating VariableManager, VariableReferences will not be supported in this policy", e);
		}

		// Setting attributes
		NamedNodeMap attrs = root.getAttributes();
		// FIXME: NPE if policyId is null
		policyId = attrs.getNamedItem("PolicyId").getNodeValue();
		// FIXME: NPE if version is null
		version = attrs.getNamedItem("Version").getNodeValue();
		// FIXME: NPE if RuleCombiningAlg Id is null
		ruleCombiningAlgId = attrs.getNamedItem("RuleCombiningAlgId").getNodeValue();

		if (attrs.getNamedItem("MaxDelegationDepth") != null)
		{
			maxDelegationDepth = BigInteger.valueOf(Long.parseLong(attrs.getNamedItem("MaxDelegationDepth").getNodeValue()));
		} else
		{
			maxDelegationDepth = BigInteger.ZERO;
		}

		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			try
			{
				Node child = children.item(i);
				if ("Description".equals(DOMHelper.getLocalName(child)))
				{
					description = child.getNodeValue();
				} else if ("PolicyIssuer".equals(DOMHelper.getLocalName(child)))
				{
					issuer = PolicyIssuer.getInstance(child);
				} else if ("PolicyDefaults".equals(DOMHelper.getLocalName(child)))
				{
					policyDefault = PolicyDefaults.getInstance(child);
				} else if ("Target".equals(DOMHelper.getLocalName(child)))
				{
					target = Target.getInstance(child, metaData);
				} else if ("CombinerParameters".equals(DOMHelper.getLocalName(child)))
				{
					throw new ParsingException("Combiner Parameters not implemented yet");
				} else if ("RuleCombinerParameters".equals(DOMHelper.getLocalName(child)))
				{
					throw new ParsingException("Rule Combiner Parameters not implemented yet");
				} else if ("VariableDefinition".equals(DOMHelper.getLocalName(child)))
				{
					throw new ParsingException("Variable definition not implemented yet");
				} else if ("Rule".equals(DOMHelper.getLocalName(child)))
				{
					policyElements.add(Rule.getInstance(child, metaData, manager));
				} else if ("ObligationExpressions".equals(DOMHelper.getLocalName(child)))
				{
					obligations = ObligationExpressions.getInstance(child);
				} else if ("AdviceExpressions".equals(DOMHelper.getLocalName(child)))
				{
					advices = AdviceExpressions.getInstance(child);
				}
			} catch (ParsingException e)
			{
				LOGGER.error("Error instantiating Policy", e);
			}
		}

		return new Policy(description, issuer, policyDefault, target, policyElements, obligations, advices, policyId, version, ruleCombiningAlgId,
				maxDelegationDepth, metaData);
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML model FIXME: Support
	 * CombinerParameters, RuleCombinerParameters
	 * 
	 * @param policyElement
	 * @return Policy instance
	 * @throws ParsingException
	 *             if PolicyElement is invalid
	 * @throws UnknownIdentifierException
	 *             if one of the PolicyIssuer AttributeValue datatype is unknown/not supported
	 */
	public static Policy getInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policyElement) throws ParsingException,
			UnknownIdentifierException
	{
		final PolicyMetaData metaData = new PolicyMetaData(PolicyMetaData.XACML_3_0_IDENTIFIER, null);
		final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer issuerElt = policyElement.getPolicyIssuer();
		final PolicyIssuer issuer = issuerElt == null ? null : PolicyIssuer.getInstance(issuerElt);
		final Target target = new Target(policyElement.getTarget(), metaData);
		final DefaultsType policyDefaultsElt = policyElement.getPolicyDefaults();
		final PolicyDefaults defaults = policyDefaultsElt == null ? null : new PolicyDefaults(policyDefaultsElt.getXPathVersion());
		final VariableManager varManager = new VariableManager(Collections.EMPTY_MAP, metaData);
		final List<Rule> rules = new ArrayList<>();
		for (final Object policyChildElt : policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions())
		{
			if (policyChildElt instanceof CombinerParametersType)
			{
				throw new ParsingException("CombinerParameters not supported");
			} else if (policyChildElt instanceof RuleCombinerParameters)
			{
				throw new ParsingException("RuleCombinerParameters not supported");
			} else if (policyChildElt instanceof VariableDefinition)
			{
				varManager.add((VariableDefinition) policyChildElt);
			} else if (policyChildElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule)
			{
				final Rule rule = Rule.getInstance((oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule) policyChildElt, varManager, metaData);
				rules.add(rule);
			}
		}

		return new Policy(policyElement.getDescription(), issuer, defaults, target, rules, policyElement.getObligationExpressions(),
				policyElement.getAdviceExpressions(), policyElement.getPolicyId(), policyElement.getVersion(), policyElement.getRuleCombiningAlgId(),
				policyElement.getMaxDelegationDepth(), metaData);
	}

	private static VariableManager createVariableManager(Node root, PolicyMetaData metaData) throws ParsingException
	{
		Map<String, Node> variableIds = new HashMap<>();
		// first off, go through and look for any definitions to get their
		// identifiers up front, since before we parse any references we'll
		// need to know what definitions we support
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeName().equals("VariableDefinition"))
			{
				String id = child.getAttributes().getNamedItem("VariableId").getNodeValue();

				// it's an error to have more than one definition with the
				// same identifier
				if (variableIds.containsKey(id))
				{
					throw new ParsingException("multiple definitions for " + "variable " + id);
				}

				variableIds.put(id, child);
			}
		}

		// now create a manager with the defined variable identifiers
		VariableManager manager = new VariableManager(variableIds, metaData);

		return manager;
	}

	@Override
	public MatchResult match(EvaluationCtx context)
	{
		if (target == null)
		{
			throw new RuntimeException("No target found in policy with id=" + policyId);
		}

		return ((Target) target).match(context);
	}

	//@Audit(type = Audit.Type.POLICY)
	@Override
	public Result evaluate(EvaluationCtx context)
	{
		Result result = null;
		List<Rule> rules = new ArrayList<>();
		CombinerParametersType combParams = new CombinerParametersType();
		for (Object element : this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions)
		{
			if (element instanceof CombinerParametersType)
			{
				CombinerParametersType jaxbCombinerParams = (CombinerParametersType) element;

				combParams.getCombinerParameters().addAll(jaxbCombinerParams.getCombinerParameters());
			} else if (element instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule)
			{
				rules.add((Rule) element);
			}
		}
		// evaluate
		result = this.ruleCombiningAlg.combine(context, combParams, rules);
		
		try
		{
			if (obligationExpressions != null && !obligationExpressions.getObligationExpressions().isEmpty())
			{
				// now, see if we should add any obligations to the set
				int effect = result.getDecision().ordinal();

				if ((effect == DecisionType.INDETERMINATE.ordinal()) || (effect == DecisionType.NOT_APPLICABLE.ordinal()))
				{
					// we didn't permit/deny, so we never return obligations
					return result;
				}

				for (ObligationExpression myObligation : obligationExpressions.getObligationExpressions())
				{
					if (myObligation.getFulfillOn().ordinal() == effect)
					{
						result.addObligation(myObligation, context);
					}
				}
			}
			/* If we have advice, it's definitely a 3.0 policy */
			if (adviceExpressions != null && !adviceExpressions.getAdviceExpressions().isEmpty())
			{
				int effect = result.getDecision().ordinal();

				if ((effect == DecisionType.INDETERMINATE.ordinal()) || (effect == DecisionType.NOT_APPLICABLE.ordinal()))
				{
					// we didn't permit/deny, so we never return advices
					return result;
				}

				final AssociatedAdvice returnAssociatedAdvice = result.getAssociatedAdvice();
				final AssociatedAdvice newAssociatedAdvice;
				if (returnAssociatedAdvice == null)
				{
					newAssociatedAdvice = new AssociatedAdvice();
					result.setAssociatedAdvice(newAssociatedAdvice);
				} else
				{
					newAssociatedAdvice = returnAssociatedAdvice;
				}

				for (AdviceExpression adviceExpr : adviceExpressions.getAdviceExpressions())
				{
					if (adviceExpr.getAppliesTo().ordinal() == effect)
					{
						Advice advice = new Advice();
						advice.setAdviceId(adviceExpr.getAdviceId());
						for (AttributeAssignmentExpression attrExpr : adviceExpr.getAttributeAssignmentExpressions())
						{
							AttributeAssignment myAttrAssType = new AttributeAssignment();
							myAttrAssType.setAttributeId(attrExpr.getAttributeId());
							myAttrAssType.setCategory(attrExpr.getCategory());
							myAttrAssType.setIssuer(attrExpr.getIssuer());
							if ((attrExpr.getExpression().getDeclaredType() == AttributeValueType.class))
							{
								myAttrAssType.setDataType(((AttributeValueType) attrExpr.getExpression().getValue()).getDataType());
								myAttrAssType.getContent().addAll(((AttributeValueType) attrExpr.getExpression().getValue()).getContent());
							}
							advice.getAttributeAssignments().add(myAttrAssType);
						}

						newAssociatedAdvice.getAdvices().add(advice);
					}
				}
			}

			return result;
		} finally
		{
			LOGGER.debug("{} returned: {}", this, result);
		}
	}

	@Override
	public CombiningAlgorithm getCombiningAlg()
	{
		return ruleCombiningAlg;
	}

	@Override
	public PolicyMetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public URI getId()
	{
		if (policyId != null)
		{
			return URI.create(policyId);
		}

		return null;
	}

	@Override
	public oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions getObligationExpressions()
	{
		return this.obligationExpressions;
	}

	@Override
	public String toString()
	{
		String className = this.getClass().getSimpleName();
		return className + " id: \"" + this.policyId + "\"";
	}

	@Override
	public List getChildren()
	{
		return this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions;
	}

	@Override
	public List getChildElements()
	{
		return this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions;
	}

}
