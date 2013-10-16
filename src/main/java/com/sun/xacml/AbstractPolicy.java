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

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.xacmlv3.Target;

/**
 * Represents an instance of an XACML policy.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public abstract class AbstractPolicy extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy {

	// atributes associated with this policy
	private CombiningAlgorithm combiningAlg;

	// the value in defaults, or null if there was no default value
	private String defaultVersion;

	// the meta-data associated with this policy
	private PolicyMetaData metaData;

	// the child elements under this policy represented simply as the
	// PolicyTreeElements...
	private List children;
	// ...or the CombinerElements that are passed to combining algorithms
	private List childElements;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicy.class);

	/**
	 * Constructor used by <code>PolicyReference</code>, which supplies its own
	 * values for the methods in this class.
	 */
	protected AbstractPolicy() {

	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always
	 *            null for pre-2.0 policies)
	 * @param combiningAlg
	 *            the combining algorithm to use
	 * @param description
	 *            describes the policy or null if there is none
	 * @param target
	 *            the policy's target
	 */
	protected AbstractPolicy(URI id, String version,
			CombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target) {
		this(id, version, combiningAlg, description, target, null);
	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always
	 *            null for pre-2.0 policies)
	 * @param combiningAlg
	 *            the combining algorithm to use
	 * @param description
	 *            describes the policy or null if there is none
	 * @param target
	 *            the policy's target
	 * @param defaultVersion
	 *            the XPath version to use for selectors
	 */
	protected AbstractPolicy(URI id, String version,
			CombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, String defaultVersion) {
		this(id, version, combiningAlg, description, target, defaultVersion,
				null, null);
	}

	/**
	 * Constructor used to create a policy from concrete components.
	 * 
	 * @param id
	 *            the policy id
	 * @param version
	 *            the policy version or null for the default (this is always
	 *            null for pre-2.0 policies)
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
	protected AbstractPolicy(URI id, String version,
			CombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, String defaultVersion, Set obligations,
			List parameters) {
		this.policyId = id.toASCIIString();
		this.combiningAlg = combiningAlg;
		this.description = description;
		this.target = target;
		this.defaultVersion = defaultVersion;

		if (version == null) {
			this.version = "1.0";
		} else {
			this.version = version;
		}

		// FIXME: this needs to fill in the meta-data correctly
		metaData = null;

		if (obligations == null) {
			this.obligationExpressions = new ObligationExpressions();
		} else {
			ObligationExpressions oblExpr = new ObligationExpressions();
			oblExpr.getObligationExpressions().addAll(obligations);
			this.obligationExpressions = oblExpr;
		}
		
		if (parameters == null) {
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.EMPTY_LIST;
		} else {
			this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.unmodifiableList(new ArrayList(
					parameters));
		}
	}

	/**
	 * Constructor used by child classes to initialize the shared data from a
	 * DOM root node.
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
	protected AbstractPolicy(Node root, String policyPrefix,
			String combiningName) throws ParsingException {
		// get the attributes, all of which are common to Policies
		NamedNodeMap attrs = root.getAttributes();

		try {
			// get the attribute Id
			this.policyId = attrs.getNamedItem(policyPrefix + "Id")
					.getNodeValue();
		} catch (Exception e) {
			throw new ParsingException("Error parsing required attribute "
					+ policyPrefix + "Id", e);
		}

		// see if there's a version
		Node versionNode = attrs.getNamedItem("Version");
		if (versionNode != null) {
			version = versionNode.getNodeValue();
		} else {
			// assign the default version
			version = "1.0";
		}

		// now get the combining algorithm...
		try {
			URI algId = new URI(attrs.getNamedItem(combiningName)
					.getNodeValue());
			CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
			combiningAlg = factory.createAlgorithm(algId);
			this.ruleCombiningAlgId = attrs.getNamedItem(combiningName).getNodeValue();
		} catch (Exception e) {
			throw new ParsingException("Error parsing combining algorithm"
					+ " in " + policyPrefix, e);
		}

		// ...and make sure it's the right kind
		if (policyPrefix.equals("Policy")) {
			if (!(combiningAlg instanceof RuleCombiningAlgorithm))
				throw new ParsingException("Policy must use a Rule "
						+ "Combining Algorithm");
		} else {
			if (!(combiningAlg instanceof PolicyCombiningAlgorithm))
				throw new ParsingException("PolicySet must use a Policy "
						+ "Combining Algorithm");
		}

		// do an initial pass through the elements to pull out the
		// defaults, if any, so we can setup the meta-data
		NodeList _children = root.getChildNodes();

		for (int i = 0; i < _children.getLength(); i++) {
			Node child = _children.item(i);
			if (child.getNodeName().equals(policyPrefix + "Defaults"))
				handleDefaults(child);
		}

		// with the defaults read, create the meta-data
		metaData = new PolicyMetaData(root.getNamespaceURI(), defaultVersion);

		// now read the remaining policy elements
		obligationExpressions = new ObligationExpressions();
		this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = new ArrayList();
		adviceExpressions = new AdviceExpressions();
		_children = root.getChildNodes();

		for (int i = 0; i < _children.getLength(); i++) {
			Node child = _children.item(i);
			String cname = child.getNodeName();

			if (cname.equals("Description")) {
				if (child.hasChildNodes())
					description = child.getFirstChild().getNodeValue();
			} else if (cname.equals("Target")) {
				target = Target.getInstance(child, metaData);
			} else if (cname.equals("Obligations")) {
				parseObligations(child);
			} else if (cname.equals("CombinerParameters")) {
				this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = handleParameters(child);
			} else if (cname.equals("ObligationExpressions")) {
				parseObligations(child);
			} else if (cname.equals("AdviceExpressions")) {
				parseAdvicesExpressions(child);
			}
		}

	}

	/**
	 * Helper routine to parse the obligation data
	 */
	private void parseObligations(Node root) throws ParsingException {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("ObligationExpression")) {
				JAXBElement<ObligationExpressions> match = null;
				try {
					Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
					match = u.unmarshal(root, ObligationExpressions.class);
					obligationExpressions = match.getValue();
				} catch (Exception e) {
					LOGGER.error("Error unmarshalling ObligationExpressions", e);
				}
				
				break;
			}
		}
	}

	/**
	 * Helper routine to parse the obligation data
	 */
	private void parseAdvicesExpressions(Node root) throws ParsingException {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AdviceExpressions")) {
				final JAXBElement<AdviceExpressions> match;
				try {
					Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
					match = u.unmarshal(root, AdviceExpressions.class);
					adviceExpressions = match.getValue();
				} catch (Exception e) {
					LOGGER.error("Error unmarshalling AdviceExpressions", e);
				}

				break;
			}
		}
	}

	/**
	 * There used to be multiple things in the defaults type, but now there's
	 * just the one string that must be a certain value, so it doesn't seem all
	 * that useful to have a class for this...we could always bring it back,
	 * however, if it started to do more
	 */
	private void handleDefaults(Node root) throws ParsingException {
		defaultVersion = null;
		NodeList nodes = root.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("XPathVersion"))
				defaultVersion = node.getFirstChild().getNodeValue();
		}
	}

	/**
	 * Handles all the CombinerParameters in the policy or policy set
	 */
	private List handleParameters(Node root) throws ParsingException {
		NodeList nodes = root.getChildNodes();
		List parameters = new ArrayList();

		for (int i = 0; i < nodes.getLength(); i++) {
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
	public URI getId() {
		if(policyId != null) {
			return URI.create(policyId);
		} else {
			return null;
		}
	}

	/**
	 * Returns the version of this policy. If this is an XACML 1.x policy then
	 * this will always return <code>"1.0"</code>.
	 * 
	 * @return the policy version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the combining algorithm used by this policy
	 * 
	 * @return the combining algorithm
	 */
	public CombiningAlgorithm getCombiningAlg() {
		return combiningAlg;
	}

	/**
	 * Returns the list of input parameters for the combining algorithm. If this
	 * is an XACML 1.x policy then the list will always be empty.
	 * 
	 * @return a <code>List</code> of <code>CombinerParameter</code>s
	 */
	public List getCombiningParameters() {
		return this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions;
	}

	/**
	 * Returns the given description of this policy or null if there is no
	 * description
	 * 
	 * @return the description or null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the target for this policy
	 * 
	 * @return the policy's target
	 */
	public oasis.names.tc.xacml._3_0.core.schema.wd_17.Target getTarget() {
		return target;
	}

	/**
	 * Returns the XPath version to use or null if none was specified
	 * 
	 * @return XPath version or null
	 */
	public String getDefaultVersion() {
		return defaultVersion;
	}

	/**
	 * Returns the <code>List</code> of children under this node in the policy
	 * tree. Depending on what kind of policy this node represents the children
	 * will either be <code>AbstractPolicy</code> objects or <code>Rule</code>s.
	 * 
	 * @return a <code>List</code> of child nodes
	 */
	public List getChildren() {
		return children;
	}

	/**
	 * Returns the <code>List</code> of <code>CombinerElement</code>s that is
	 * provided to the combining algorithm. This returns the same set of
	 * children that <code>getChildren</code> provides along with any associated
	 * combiner parameters.
	 * 
	 * @return a <code>List</code> of <code>CombinerElement</code>s
	 */
	public List getChildElements() {
		return childElements;
	}

	/**
	 * Returns the Set of obligations for this policy, which may be empty
	 * 
	 * @return the policy's obligations
	 */
	public ObligationExpressions getObligations() {
		return obligationExpressions;
	}

	/**
	 * Returns the meta-data associated with this policy
	 */
	public PolicyMetaData getMetaData() {
		return metaData;
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
	 * Sets the child policy tree elements for this node, which are passed to
	 * the combining algorithm on evaluation. The <code>List</code> must contain
	 * <code>CombinerElement</code>s, which in turn will contain
	 * <code>Rule</code>s or <code>AbstractPolicy</code>s, but may not contain
	 * both types of elements.
	 * 
	 * @param children
	 *            a <code>List</code> of <code>CombinerElement</code>s
	 *            representing the child elements used by the combining
	 *            algorithm
	 */
	protected void setChildren(List<Rule> children) {
		// we always want a concrete list, since we're going to pass it to
		// a combiner that expects a non-null input
		if (children == null) {
			this.children = Collections.EMPTY_LIST;
		} else {
			this.children = Collections.unmodifiableList(children);
			childElements = Collections.unmodifiableList(children);
		}
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
//	public Result evaluate(EvaluationCtx context) {
//		// evaluate
//		Result result = null;//combiningAlg.combine(context, this.combinerParametersOrRuleCombinerParametersOrVariableDefinition, childElements);
//
//		if (obligationExpressions.getObligationExpression().size() > 0) {
//			// now, see if we should add any obligations to the set
//			int effect = result.getDecision().ordinal();
//
//			if ((effect == DecisionType.INDETERMINATE.ordinal())
//					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
//				// we didn't permit/deny, so we never return obligations
//				return result;
//			}
//
//			if (metaData.getXACMLVersion() == Integer
//					.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value())) {
//				for (ObligationExpressionType myObligation : obligationExpressions.getObligationExpression()) {
//						if (myObligation.getFulfillOn().ordinal() == effect) {
//							result.addObligation(myObligation, context);
//						}
//				}
//			} else {
//				Iterator it = obligationExpressions.getObligationExpression().iterator();
//				while (it.hasNext()) {
//					Obligation obligation = (Obligation) (it.next());
//					if (obligation.getFulfillOn() == effect) {
//						result.addObligation(obligation);
//					}
//				}
//			}
//		}
//		/* If we have advice, it's definitely a 3.0 policy */
//		if (adviceExpressions.getAdviceExpression().size() > 0) {
//			int effect = result.getDecision().ordinal();
//
//			if ((effect == DecisionType.INDETERMINATE.ordinal())
//					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
//				// we didn't permit/deny, so we never return advices
//				return result;
//			}
//			//TODO: Fix advice parsing
////			for (AdviceExpressionsType myAdvices : (Set<AdviceExpressionsType>) advice) {
////				for (AdviceType myAdvice : myAdvices.getAdviceExpression()) {
////					if (myAdvice.getAppliesTo().ordinal() == effect) {
////						AdviceType adviceType = new AdviceType();
////						result.addAdvice(myAdvice);
////					}
////				}
////			}
//		}
//
//		if (context.getIncludeInResults().size() > 0) {
//			result.getAttributes().addAll(context.getIncludeInResults());
//		}
//
//		return result;
//	}

	/**
	 * Routine used by <code>Policy</code> and <code>PolicySet</code> to encode
	 * some common elements.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	protected void encodeCommonElements(OutputStream output, Indenter indenter) {
		Iterator it = childElements.iterator();
		while (it.hasNext()) {
			((CombinerElement) (it.next())).encode(output, indenter);
		}

		if (obligationExpressions.getObligationExpressions().size() != 0) {
			PrintStream out = new PrintStream(output);
			String indent = indenter.makeString();

			out.println(indent + "<Obligations>");
			indenter.in();

			it = obligationExpressions.getObligationExpressions().iterator();
			while (it.hasNext()) {
				((Obligation) (it.next())).encode(output, indenter);
			}

			out.println(indent + "</Obligations>");
			indenter.out();
		}
	}

	@Override
	public String toString() {
		String className = this.getClass().getSimpleName();
		return className + " id: \"" + policyId + "\"";
	}

}
