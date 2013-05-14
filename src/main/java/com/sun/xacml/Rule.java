/*
 * @(#)Rule.java
 *
 * Copyright 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ConditionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.cond.Condition;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.xacmlv3.AdviceExpressions;
import com.sun.xacml.xacmlv3.Target;

/**
 * Represents the RuleType XACML type. This has a target for matching, and
 * encapsulates the condition and all sub-operations that make up the heart of
 * most policies.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Rule extends RuleType {

	// the attributes associated with this Rule
	// private URI idAttr;
	// private EffectType effectAttr;

	// the elements in the rule, each of which is optional
	// private String description = null;
	// private TargetType target = null;

	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Rule.class);

	/**
	 * Creates a new <code>Rule</code> object for XACML 1.x and 2.0.
	 * 
	 * @param id
	 *            the rule's identifier
	 * @param effect
	 *            the effect to return if the rule applies (either Pemit or
	 *            Deny) as specified in <code>Result</code>
	 * @param description
	 *            a textual description, or null
	 * @param target
	 *            the rule's target, or null if the target is to be inherited
	 *            from the encompassing policy
	 * @param condition
	 *            the rule's condition, or null if there is none
	 * @param obligation
	 *            the rule's obligations, or null if there is none
	 */
	public Rule(String id, EffectType effect, String description,
			TargetType target, ConditionType condition,
			ObligationExpressionsType obligations, AdviceExpressionsType advices) {
		this.ruleId = id;
		this.effect = effect;
		this.description = description;
		this.target = target;
		this.condition = condition;
		this.obligationExpressions = obligations;
		this.adviceExpressions = advices;
	}

	/**
	 * Creates a new <code>Rule</code> object for XACML 1.x and 2.0.
	 * 
	 * @param id
	 *            the rule's identifier
	 * @param effect
	 *            the effect to return if the rule applies (either Pemit or
	 *            Deny) as specified in <code>Result</code>
	 * @param description
	 *            a textual description, or null
	 * @param target
	 *            the rule's target, or null if the target is to be inherited
	 *            from the encompassing policy
	 * @param condition
	 *            the rule's condition, or null if there is none
	 * @param obligation
	 *            the rule's obligations, or null if there is none
	 */
	public Rule(String id, EffectType effect, String description,
			TargetType target, ConditionType condition,
			ObligationExpressionsType obligations) {
		this.ruleId = id;
		this.effect = effect;
		this.description = description;
		this.target = target;
		this.condition = condition;
		this.obligationExpressions = obligations;
	}

	/**
	 * Creates a new <code>Rule</code> object for XACML 1.x and 2.0.
	 * 
	 * @param id
	 *            the rule's identifier
	 * @param effect
	 *            the effect to return if the rule applies (either Pemit or
	 *            Deny) as specified in <code>Result</code>
	 * @param description
	 *            a textual description, or null
	 * @param target
	 *            the rule's target, or null if the target is to be inherited
	 *            from the encompassing policy
	 * @param condition
	 *            the rule's condition, or null if there is none
	 */
	public Rule(String id, EffectType effect, String description,
			TargetType target, ConditionType condition) {
		this.ruleId = id;
		this.effect = effect;
		this.description = description;
		this.target = target;
		this.condition = condition;
	}

	/**
	 * Creates a new <code>Rule</code> object for XACML 1.x only.
	 * 
	 * @deprecated As of 2.0 you should use the Constructor that accepts the new
	 *             <code>Condition</code> class.
	 * 
	 * @param id
	 *            the rule's identifier
	 * @param effect
	 *            the effect to return if the rule applies (either Pemit or
	 *            Deny) as specified in <code>Result</code>
	 * @param description
	 *            a textual description, or null
	 * @param target
	 *            the rule's target, or null if the target is to be inherited
	 *            from the encompassing policy
	 * @param condition
	 *            the rule's condition, or null if there is none
	 */
	public Rule(String id, EffectType effect, String description,
			TargetType target, Apply condition) {
		this.ruleId = id;
		this.effect = effect;
		this.description = description;
		this.target = target;
		// this.condition = new Condition(condition.getFunction(),
		// condition.getChildren());
	}

	/**
	 * Returns a new instance of the <code>Rule</code> class based on a DOM
	 * node. The node must be the root of an XML RuleType.
	 * 
	 * @deprecated As of 2.0 you should avoid using this method and should
	 *             instead use the version that takes a
	 *             <code>PolicyMetaData</code> instance. This method will only
	 *             work for XACML 1.x policies.
	 * 
	 * @param root
	 *            the DOM root of a RuleType XML type
	 * @param xpathVersion
	 *            the XPath version to use in any selectors or XPath functions,
	 *            or null if this is unspecified (ie, not supplied in the
	 *            defaults section of the policy)
	 * 
	 * @throws ParsingException
	 *             if the RuleType is invalid
	 */
	public static Rule getInstance(Node root, String xpathVersion)
			throws ParsingException {
		return getInstance(root, new PolicyMetaData(
				PolicyMetaData.XACML_1_0_IDENTIFIER, xpathVersion), null);
	}

	/**
	 * Returns a new instance of the <code>Rule</code> class based on a DOM
	 * node. The node must be the root of an XML RuleType.
	 * 
	 * @param root
	 *            the DOM root of a RuleType XML type
	 * @param metaData
	 *            the meta-data associated with this Rule's policy
	 * @param manager
	 *            the <code>VariableManager</code> used to connect
	 *            <code>VariableReference</code>s to their cooresponding
	 *            <code>VariableDefinition<code>s
	 * 
	 * @throws ParsingException
	 *             if the RuleType is invalid
	 */
	public static Rule getInstance(Node root, PolicyMetaData metaData,
			VariableManager manager) throws ParsingException {
		String id = null;
		String name = null;
		EffectType effect = null;
		String description = null;
		TargetType target = null;
		ConditionType condition = null;
		ObligationExpressionsType obligations = null;
		AdviceExpressionsType advices = null;

		// first, get the attributes
		NamedNodeMap attrs = root.getAttributes();

		// get the two required attrs...
		id = attrs.getNamedItem("RuleId").getNodeValue();

		String str = attrs.getNamedItem("Effect").getNodeValue();
		if (str.equals("Permit")) {
			effect = EffectType.PERMIT;
		} else if (str.equals("Deny")) {
			effect = EffectType.DENY;
		} else {
			throw new ParsingException("Invalid Effect: " + effect);
		}

		// next, get the elements
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String cname = child.getNodeName();

			if (cname.equals("Description")) {
				description = child.getFirstChild().getNodeValue();
			} else if (cname.equals("Target")) {
				target = Target.getInstance(child, metaData);
			} else if (cname.equals("Condition")) {
				condition = Condition.getInstance(child, metaData, manager);
			} else if (cname.equals("ObligationExpressions")) {
				obligations = ObligationExpressions.getInstance(child);
			} else if (cname.equals("AdviceExpressions")) {
				advices = AdviceExpressions.getInstance(child);
			}
		}

		return new Rule(id, effect, description, target, condition,
				obligations, advices);
	}

	/**
	 * Returns the effect that this <code>Rule</code> will return from the
	 * evaluate method (Permit or Deny) if the request applies.
	 * 
	 * @return a decision effect, as defined in <code>Result</code>
	 */
	public EffectType getEffect() {
		return effect;
	}

	/**
	 * Returns the id of this <code>Rule</code>
	 * 
	 * @return the rule id
	 */
	public String getId() {
		return ruleId;
	}

	/**
	 * Returns the given description of this <code>Rule</code> or null if there
	 * is no description
	 * 
	 * @return the description or null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the target for this <code>Rule</code> or null if there is no
	 * target
	 * 
	 * @return the rule's target
	 */
	public TargetType getTarget() {
		return this.target;
	}

	/**
	 * Since a rule is always a leaf in a policy tree because it can have no
	 * children, this always returns an empty <code>List</code>.
	 * 
	 * @return a <code>List</code> with no elements
	 */
	public List getChildren() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the condition for this <code>Rule</code> or null if there is no
	 * condition
	 * 
	 * @return the rule's condition
	 */
	public ConditionType getCondition() {
		return condition;
	}

	/**
	 * Given the input context sees whether or not the request matches this
	 * <code>Rule</code>'s <code>Target</code>. Note that unlike the matching
	 * done by the <code>evaluate</code> method, if the <code>Target</code> is
	 * missing than this will return Indeterminate. This lets you write your own
	 * custom matching routines for rules but lets evaluation proceed normally.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match this rule and the request
	 */
	public MatchResult match(EvaluationCtx context) {
		if (target == null) {
			ArrayList code = new ArrayList();
			code.add(Status.STATUS_PROCESSING_ERROR);
			Status status = new Status(code, "no target available for "
					+ "matching a rule");

			return new MatchResult(MatchResult.INDETERMINATE, status);
		}

		return ((Target) target).match(context);
	}

	/**
	 * Evaluates the rule against the supplied context. This will check that the
	 * target matches, and then try to evaluate the condition. If the target and
	 * condition apply, then the rule's effect is returned in the result.
	 * <p>
	 * Note that rules are not required to have targets. If no target is
	 * specified, then the rule inherits its parent's target. In the event that
	 * this <code>Rule</code> has no <code>Target</code> then the match is
	 * assumed to be true, since evaluating a policy tree to this level required
	 * the parent's target to match.
	 * 
	 * @param context
	 *            the representation of the request we're evaluating
	 * 
	 * @return the result of the evaluation
	 */
	public Result evaluate(EvaluationCtx context) {
		// Do the list of Attribute who needs to be included in result
		List<AttributesType> includeInResult = context.getIncludeInResults();
		MatchResult match = null;
		Result returnResult = null;
		// If the Target is null then it's supposed to inherit from the
		// parent policy, so we skip the matching step assuming we wouldn't
		// be here unless the parent matched
		if (target != null) {
			match = ((Target) target).match(context);
			int result = match.getResult();

			// if the target didn't match, then this Rule doesn't apply
			if (result == MatchResult.NO_MATCH) {
				return new Result(DecisionType.NOT_APPLICABLE, null, context
						.getResourceId().encode(), null, includeInResult);
			}
			// if the target was indeterminate, we can't go on
			if (result == MatchResult.INDETERMINATE) {
				return new Result(DecisionType.INDETERMINATE,
						match.getStatus(), context.getResourceId().encode(),
						null, includeInResult);
			}
		}

		// log4jLogger.debug("Found a rule that match the request");
		// log4jLogger.debug("RuleId: "+idAttr);
		// AuditLogs audit = AuditLogs.getInstance();
		// audit.setRuleId(idAttr.toString());

		// if there's no condition, then we just return the effect...
		if (condition == null) {
			LOGGER.info("Rule doesn't contain condition, so result is: "
					+ this.effect.value() + " for rule: " + this.ruleId);
			returnResult = new Result(DecisionType.fromValue(this.effect
					.value()), null, context.getResourceId().encode(), null,
					includeInResult);
		} else {
			// ...otherwise we evaluate the condition
			EvaluationResult result = ((Condition) condition).evaluate(context);

			if (result.indeterminate()) {
				// if it was INDETERMINATE, then that's what we return
				return new Result(DecisionType.INDETERMINATE,
						result.getStatus(), context.getResourceId().encode(),
						null, includeInResult);
			} else {
				// otherwise we return the effect on tue, and NA on false
				BooleanAttribute bool = (BooleanAttribute) (result
						.getAttributeValue());

				if (bool.getValue()) {
					returnResult = new Result(DecisionType.valueOf(effect
							.name()), null, context.getResourceId().encode(),
							null, includeInResult);

				} else {
					return new Result(DecisionType.NOT_APPLICABLE, null,
							context.getResourceId().encode(), null,
							includeInResult);
				}
			}
		}
		
		// Adding Obligations and Advice to the result
		if (obligationExpressions != null) {
			if (obligationExpressions.getObligationExpression().size() > 0) {
				// now, see if we should add any obligations to the set
				int effect = returnResult.getDecision().ordinal();

				if ((effect == DecisionType.INDETERMINATE.ordinal())
						|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
					// we didn't permit/deny, so we never return
					// obligations
					return returnResult;
				}

				for (ObligationExpressionType myObligation : obligationExpressions
						.getObligationExpression()) {
					if (myObligation.getFulfillOn().ordinal() == effect) {
						returnResult.addObligation(myObligation, context);
					}
				}
			}
		}
		if (adviceExpressions != null) {
			if (adviceExpressions.getAdviceExpression().size() > 0) {
				int effect = returnResult.getDecision().ordinal();

				if ((effect == DecisionType.INDETERMINATE.ordinal())
						|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
					// we didn't permit/deny, so we never return advices
					return returnResult;
				}
				for (AdviceExpressionType adviceExpr : adviceExpressions
						.getAdviceExpression()) {
					if (adviceExpr.getAppliesTo().ordinal() == effect) {
						AdviceType advice = new AdviceType();
						advice.setAdviceId(adviceExpr.getAdviceId());
						for (AttributeAssignmentExpressionType attrExpr : adviceExpr
								.getAttributeAssignmentExpression()) {
							AttributeAssignmentType myAttrAssType = new AttributeAssignmentType();
							myAttrAssType.setAttributeId(attrExpr.getAttributeId());
							myAttrAssType.setCategory(attrExpr.getCategory());
							myAttrAssType.setIssuer(attrExpr.getIssuer());		
							if ((attrExpr.getExpression().getDeclaredType()  == AttributeValueType.class)) {
								myAttrAssType.setDataType(((AttributeValueType)attrExpr.getExpression().getValue()).getDataType());
								myAttrAssType.getContent().addAll(((AttributeValueType)attrExpr.getExpression().getValue()).getContent());
							}
							advice.getAttributeAssignment().add(myAttrAssType);
						}
						returnResult.addAdvice(advice);
					}
				}				
			}
		}

		return returnResult;
	}

	/**
	 * Encodes this <code>Rule</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Rule</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

}
