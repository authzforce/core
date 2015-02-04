/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.AdviceExpressions;
import com.sun.xacml.xacmlv3.IPolicy;
import com.sun.xacml.xacmlv3.Target;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 * Represents an instance of an XACML policy.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public abstract class AbstractPolicySet extends oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet implements IPolicy
{

	// atributes associated with this policy
	// private URI idAttr;
	// private String version;
	private PolicyCombiningAlgorithm combiningAlg;

	// the value in defaults, or null if there was no default value
	private String defaultVersion;

	// the meta-data associated with this policy
	private PolicyMetaData metaData;

	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicySet.class);

	/**
	 * Constructor used by <code>PolicyReference</code>, which supplies its own values for the
	 * methods in this class.
	 */
	protected AbstractPolicySet()
	{
	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the combining algorithm to use
	 * @param description
	 *            describes the policy or null if there is none
	 * @param target
	 *            the policy's target
	 */
	protected AbstractPolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target)
	{
		this(id, version, combiningAlg, description, target, null);
	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the combining algorithm to use
	 * @param description
	 *            describes the policy or null if there is none
	 * @param target
	 *            the policy's target
	 * @param defaultVersion
	 *            the XPath version to use for selectors
	 */
	protected AbstractPolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, String defaultVersion)
	{
		this(id, version, combiningAlg, description, target, defaultVersion, null, null, null);
	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the combining algorithm to use
	 * @param description
	 *            describes the policy or null if there is none
	 * @param target
	 *            the policy's target
	 * @param defaultVersion
	 *            the XPath version to use for selectors
	 * @param obligations
	 *            the policy's obligations
	 */
	protected AbstractPolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, String defaultVersion,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions advices, List parameters)
	{
		this.policySetId = id.toASCIIString();
		this.combiningAlg = combiningAlg;
		this.description = description;
		this.target = target;
		this.defaultVersion = defaultVersion;

		if (version == null)
		{
			this.version = "1.0";
		} else
		{
			this.version = version;
		}

		metaData = new PolicyMetaData(PolicyMetaData.XACML_3_0_IDENTIFIER, defaultVersion);

		if (obligations == null)
		{
			/*
			 * obligationExpressions must be null by default, if you create new
			 * ObligationExpressions() in this case, the result Obligations will be marshalled to
			 * empty <Obligations /> element which is NOT VALID against the XACML schema.
			 */
			this.obligationExpressions = null;
		} else
		{
			this.obligationExpressions = obligations;
		}

		if (advices == null)
		{
			/*
			 * adviceExpressions must be null by default, if you create new AdviceExpressions() in
			 * this case, the result AssociatedAdvice will be marshalled to empty <AssociatedAdvice
			 * /> element which is NOT VALID against the XACML schema.
			 */
			this.adviceExpressions = null;
		} else
		{
			this.adviceExpressions = advices;
		}

		if (parameters == null)
		{
			this.policySetsAndPoliciesAndPolicySetIdReferences = Collections.EMPTY_LIST;
		} else
		{
			this.policySetsAndPoliciesAndPolicySetIdReferences = Collections.unmodifiableList(new ArrayList<>(parameters));
		}
	}

	/**
	 * Constructor used by child classes to initialize the shared data from a DOM root node.
	 * 
	 * @param root
	 *            the DOM root of the policy
	 * @param policyPrefix
	 *            either "Policy" or "PolicySet"
	 * @param combiningName
	 *            name of the field naming the combining alg
	 * 
	 * @throws ParsingException
	 *             if the policy is invalid
	 */
	protected AbstractPolicySet(Node root, String policyPrefix, String combiningName) throws ParsingException
	{
		// get the attributes, all of which are common to Policies
		NamedNodeMap attrs = root.getAttributes();

		try
		{
			// get the attribute Id
			this.policySetId = attrs.getNamedItem(policyPrefix + "Id").getNodeValue();
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing required attribute " + policyPrefix + "Id", e);
		}

		// see if there's a version
		Node versionNode = attrs.getNamedItem("Version");
		if (versionNode != null)
		{
			version = versionNode.getNodeValue();
		} else
		{
			// assign the default version
			version = "1.0";
		}

		// now get the combining algorithm...
		try
		{
			URI algId = new URI(attrs.getNamedItem(combiningName).getNodeValue());
			CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
			combiningAlg = factory.createAlgorithm(algId);
			this.policyCombiningAlgId = attrs.getNamedItem(combiningName).getNodeValue();
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing policy combining algorithm" + " in " + policyPrefix, e);
		}

		// do an initial pass through the elements to pull out the
		// defaults, if any, so we can setup the meta-data
		NodeList _children = root.getChildNodes();

		for (int i = 0; i < _children.getLength(); i++)
		{
			Node child = _children.item(i);
			if (child.getNodeName().equals(policyPrefix + "Defaults"))
				handleDefaults(child);
		}

		// with the defaults read, create the meta-data
		metaData = new PolicyMetaData(root.getNamespaceURI(), defaultVersion);

		// now read the remaining policy elements
		/*
		 * obligationExpressions must be null by default, if you create new ObligationExpressions()
		 * in this case, the result Obligations will be marshalled to empty <Obligations /> element
		 * which is NOT VALID against the XACML schema.
		 */
		obligationExpressions = null;
		this.policySetsAndPoliciesAndPolicySetIdReferences = new ArrayList<>();
		/*
		 * obligationExpressions must be null by default, if you create new AdviceExpressions() in
		 * this case, the result AssociatedAdvice will be marshalled to empty <AssociatedAdvice />
		 * element which is NOT VALID against the XACML schema.
		 */
		adviceExpressions = null;
		_children = root.getChildNodes();

		for (int i = 0; i < _children.getLength(); i++)
		{
			Node child = _children.item(i);
			String cname = child.getNodeName();

			if (cname.equals("Description"))
			{
				if (child.hasChildNodes())
					description = child.getFirstChild().getNodeValue();
			} else if (cname.equals("Target"))
			{
				target = Target.getInstance(child, metaData);
			} else if (cname.equals("Obligations"))
			{
				parseObligations(child);
			} else if (cname.equals("CombinerParameters"))
			{
				this.policySetsAndPoliciesAndPolicySetIdReferences = handleParameters(child);
			} else if (cname.equals("ObligationExpressions"))
			{
				// parseObligations(child);
				this.obligationExpressions = ObligationExpressions.getInstance(child);
			} else if (cname.equals("AdviceExpressions"))
			{
				// parseAdvicesExpressions(child);
				this.adviceExpressions = AdviceExpressions.getInstance(child);
			} else if (cname.equals("Policy"))
			{
				// myPolicies.add(Policy.getInstance(child));
			}
		}
		// this.policySetOrPolicyOrPolicySetIdReference.addAll((List<JAXBElement<?>>)
		// Collections.unmodifiableList(myPolicies));

		// finally, make sure the obligations and parameters are immutable
		// ObligationExpressionsType oblExpr = new ObligationExpressionsType();
		// oblExpr.getObligationExpression().addAll(obligations);
		// obligationExpressions = oblExpr;
		// this.combinerParametersOrRuleCombinerParametersOrVariableDefinition =
		// Collections.unmodifiableList(parameters);
		// adviceExpressions = Collections.unmodifiableSet(advice);

		// this.adviceExpressions = AdviceExpressions.getInstance(advice);
		// this.obligationExpressions =
		// ObligationExpressions.getInstance(obligations);
	}

	/**
	 * Helper routine to parse the obligation data
	 */
	private void parseObligations(Node root)
	{
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("ObligationExpression"))
			{
				JAXBElement<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions> match = null;
				try
				{
					Unmarshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
					match = u.unmarshal(root, oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions.class);
					obligationExpressions = match.getValue();
				} catch (Exception e)
				{
					LOGGER.error("Error unmarshalling ObligationExpressions", e);
				}

				break;
			}
		}
	}

	/**
	 * There used to be multiple things in the defaults type, but now there's just the one string
	 * that must be a certain value, so it doesn't seem all that useful to have a class for
	 * this...we could always bring it back, however, if it started to do more
	 */
	private void handleDefaults(Node root)
	{
		defaultVersion = null;
		NodeList nodes = root.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("XPathVersion"))
				defaultVersion = node.getFirstChild().getNodeValue();
		}
	}

	/**
	 * Handles all the CombinerParameters in the policy or policy set
	 */
	private static List handleParameters(Node root) throws ParsingException
	{
		NodeList nodes = root.getChildNodes();
		final List<CombinerParameter> parameters = new ArrayList<>();

		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("CombinerParameter"))
				parameters.add(CombinerParameter.getInstance(node));
		}

		return parameters;
	}

	/**
	 * Returns the id of this policy
	 * 
	 * @return the policy id
	 */
	@Override
	public URI getId()
	{
		if (policySetId != null)
		{
			return URI.create(policySetId);
		}

		return null;
	}

	@Override
	public CombiningAlgorithm getCombiningAlg()
	{
		return combiningAlg;
	}

	/**
	 * Returns the list of input parameters for the combining algorithm. If this is an XACML 1.x
	 * policy then the list will always be empty.
	 * 
	 * @return a <code>List</code> of <code>CombinerParameter</code>s
	 */
	public List getCombiningParameters()
	{
		return this.policySetsAndPoliciesAndPolicySetIdReferences;
	}

	/**
	 * Returns the XPath version to use or null if none was specified
	 * 
	 * @return XPath version or null
	 */
	public String getDefaultVersion()
	{
		return defaultVersion;
	}

	/**
	 * Returns the <code>List</code> of children under this node in the policy tree. Depending on
	 * what kind of policy this node represents the children will either be
	 * <code>AbstractPolicy</code> objects or <code>Rule</code>s.
	 * 
	 * @return a <code>List</code> of child nodes
	 */
	@Override
	public List getChildren()
	{
		return policySetsAndPoliciesAndPolicySetIdReferences;
	}

	/**
	 * Returns the <code>List</code> of <code>CombinerElement</code>s that is provided to the
	 * combining algorithm. This returns the same set of children that <code>getChildren</code>
	 * provides along with any associated combiner parameters.
	 * 
	 * @return a <code>List</code> of <code>CombinerElement</code>s
	 */
	@Override
	public List getChildElements()
	{
		return policySetsAndPoliciesAndPolicySetIdReferences;
	}

	/**
	 * Returns the meta-data associated with this policy
	 */
	@Override
	public PolicyMetaData getMetaData()
	{
		return metaData;
	}

	/**
	 * Given the input context sees whether or not the request matches this policy. This must be
	 * called by combining algorithms before they evaluate a policy. This is also used in the
	 * initial policy finding operation to determine which top-level policies might apply to the
	 * request.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the policy and the request
	 */
	@Override
	public MatchResult match(EvaluationCtx context)
	{
		if (target == null)
		{
			throw new RuntimeException("No target found in policySet with id=" + policySetId);
		}

		return ((Target) target).match(context);
	}

	/**
	 * Tries to evaluate the policyset by calling the combining algorithm on the given policies. The
	 * <code>match</code> method must always be called first, and must always return MATCH, before
	 * this method is called.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 * 
	 *         FIXME: the match logic should be done by evaluate, doing two different function calls
	 *         (match + evaluate) every time is useless
	 */
	@Override
	public Result evaluate(EvaluationCtx context)
	{
		Result result = null;
		List<IPolicy> policies = new ArrayList<>();
		CombinerParametersType combParams = new CombinerParametersType();
		for (Object element : this.policySetsAndPoliciesAndPolicySetIdReferences)
		{
			if (element instanceof PolicyCombinerElement)
			{
				final Object combinerElt = ((PolicyCombinerElement) element).getElement();
				if (combinerElt instanceof IPolicy)
				{
					policies.add((IPolicy) combinerElt);
				} else
				{
					continue;
				}

			}
		}
		// evaluate
		result = this.combiningAlg.combine(context, combParams, policies);

		if (obligationExpressions != null && !obligationExpressions.getObligationExpressions().isEmpty())
		{
			// now, see if we should add any obligations to the set
			DecisionType decision = result.getDecision();

			if ((decision == DecisionType.INDETERMINATE) || (decision == DecisionType.NOT_APPLICABLE))
			{
				// we didn't permit/deny, so we never return obligations
				return result;
			}

			for (ObligationExpression myObligation : obligationExpressions.getObligationExpressions())
			{
				final DecisionType obligEffect = myObligation.getFulfillOn() == EffectType.PERMIT? DecisionType.PERMIT: DecisionType.DENY;
				if (obligEffect == decision)
				{
					result.addObligation(myObligation, context);
				}
			}
		}
		/* If we have advice, it's definitely a 3.0 policy */
		if (adviceExpressions != null && !adviceExpressions.getAdviceExpressions().isEmpty())
		{
			DecisionType decision = result.getDecision();

			if ((decision == DecisionType.INDETERMINATE) || (decision == DecisionType.NOT_APPLICABLE))
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
				final DecisionType adviceAppliesTo = adviceExpr.getAppliesTo() == EffectType.PERMIT? DecisionType.PERMIT: DecisionType.DENY;
				if (adviceAppliesTo == decision)
				{
					Advice advice = new Advice();
					advice.setAdviceId(adviceExpr.getAdviceId());
					for (AttributeAssignmentExpression attrExpr : adviceExpr.getAttributeAssignmentExpressions())
					{
						AttributeAssignment myAttrAssType = new AttributeAssignment();
						myAttrAssType.setAttributeId(attrExpr.getAttributeId());
						myAttrAssType.setCategory(attrExpr.getCategory());
						myAttrAssType.getContent().add(attrExpr.getExpression());
						myAttrAssType.setIssuer(attrExpr.getIssuer());
						advice.getAttributeAssignments().add(myAttrAssType);
					}

					newAssociatedAdvice.getAdvices().add(advice);
				}
			}
		}

		return result;
	}

	@Override
	public String toString()
	{
		String className = this.getClass().getSimpleName();
		return className + " id: \"" + policySetId + "\"";
	}

}
