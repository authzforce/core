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
package com.sun.xacml;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.AdviceExpressions;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.xacmlv3.Target;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * Represents an instance of an XACML policy.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public abstract class AbstractPolicySet extends PolicySetType {

	// atributes associated with this policy
	// private URI idAttr;
	// private String version;
	private CombiningAlgorithm combiningAlg;

	// the elements in the policy
	// private String description;
	// private TargetType target;

	// the value in defaults, or null if there was no default value
	private String defaultVersion;

	// the meta-data associated with this policy
	private PolicyMetaData metaData;

	// the child elements under this policy represented simply as the
	// PolicyTreeElements...
	private List children;
	// ...or the CombinerElements that are passed to combining algorithms
	private List childElements;

	// any obligations held by this policy
	// private Set obligations;

	// any obligations held by this policy
	// private Set advice;

	// the list of combiner parameters
	// private List parameters;

	// the logger we'll use for all messages
	private static final Logger logger = Logger
			.getLogger(AbstractPolicySet.class.getName());

	/**
	 * Constructor used by <code>PolicyReference</code>, which supplies its own
	 * values for the methods in this class.
	 */
	protected AbstractPolicySet() {

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
	protected AbstractPolicySet(URI id, String version,
			PolicyCombiningAlgorithm combiningAlg, String description,
			TargetType target) {
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
	protected AbstractPolicySet(URI id, String version,
			PolicyCombiningAlgorithm combiningAlg, String description,
			TargetType target, String defaultVersion) {
		this(id, version, combiningAlg, description, target, defaultVersion,
				null, null, null);
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
	protected AbstractPolicySet(URI id, String version,
			PolicyCombiningAlgorithm combiningAlg, String description,
			TargetType target, String defaultVersion,
			ObligationExpressionsType obligations,
			AdviceExpressionsType advices, List parameters) {
		this.policySetId = id.toASCIIString();
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
			this.obligationExpressions = new ObligationExpressionsType();
		} else {
			this.obligationExpressions = obligations;
		}

		if (advices == null) {
			this.adviceExpressions = new AdviceExpressionsType();
		} else {
			this.adviceExpressions = advices;
		}

		if (parameters == null) {
			this.policySetOrPolicyOrPolicySetIdReference = Collections.EMPTY_LIST;
		} else {
			this.policySetOrPolicyOrPolicySetIdReference = Collections
					.unmodifiableList(new ArrayList(parameters));
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
	protected AbstractPolicySet(Node root, String policyPrefix,
			String combiningName) throws ParsingException {
		// get the attributes, all of which are common to Policies
		NamedNodeMap attrs = root.getAttributes();

		try {
			// get the attribute Id
			this.policySetId = attrs.getNamedItem(policyPrefix + "Id")
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
			this.policyCombiningAlgId = attrs.getNamedItem(combiningName)
					.getNodeValue();
		} catch (Exception e) {
			throw new ParsingException(
					"Error parsing policy combining algorithm" + " in "
							+ policyPrefix, e);
		}

		// ...and make sure it's the right kind
		if (policyPrefix.equals("PolicySet")) {
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
		obligationExpressions = new ObligationExpressionsType();
		this.policySetOrPolicyOrPolicySetIdReference = new ArrayList();
		adviceExpressions = new AdviceExpressionsType();
		_children = root.getChildNodes();
		List myPolicies = new ArrayList();

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
				this.policySetOrPolicyOrPolicySetIdReference = handleParameters(child);
			} else if (cname.equals("ObligationExpressions")) {
//				parseObligations(child);
				this.obligationExpressions = ObligationExpressions.getInstance(child);
			} else if (cname.equals("AdviceExpressions")) {
//				parseAdvicesExpressions(child);
				this.adviceExpressions = AdviceExpressions.getInstance(child);
			} else if (cname.equals("Policy")) {
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
	private void parseObligations(Node root) throws ParsingException {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("ObligationExpression")) {
				JAXBElement<ObligationExpressionsType> match = null;
				try {
					JAXBContext jc = JAXBContext
							.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					match = (JAXBElement<ObligationExpressionsType>) u
							.unmarshal(root);
				} catch (Exception e) {
					System.err.println(e);
				}

				obligationExpressions = match.getValue();
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
				JAXBElement<AdviceExpressionsType> match = null;
				try {
					JAXBContext jc = JAXBContext
							.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					match = (JAXBElement<AdviceExpressionsType>) u
							.unmarshal(root);
				} catch (Exception e) {
					System.err.println(e);
				}

				adviceExpressions = match.getValue();
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
		if (policySetId != null) {
			return URI.create(policySetId);
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
		return this.policySetOrPolicyOrPolicySetIdReference;
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
	public TargetType getTarget() {
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
	public ObligationExpressionsType getObligations() {
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
			throw new RuntimeException("No target found in policySet with id="
					+ policySetId);
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
			// NOTE: since this is only getting called by known child
			// classes we don't check that the types are all the same
			// List list = new ArrayList();
			// Iterator it = children.iterator();
			//
			// while (it.hasNext()) {
			// CombinerElement element = (CombinerElement) (it.next());
			// list.add(element.getElement());
			// }

			this.children = Collections.unmodifiableList(children);
			childElements = Collections.unmodifiableList(children);
		}
	}

	/**
	 * Tries to evaluate the policyset by calling the combining algorithm on the
	 * given policies. The <code>match</code> method must always be called
	 * first, and must always return MATCH, before this method is called.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	public Result evaluate(EvaluationCtx context) {
		Result result = null;
		List policies = new ArrayList();
		CombinerParametersType combParams = new CombinerParametersType();
		PolicyCombinerParametersType combParam = null;
		for (Object element : this.policySetOrPolicyOrPolicySetIdReference) {
			if (element instanceof PolicyCombinerElement) {
				if (((PolicyCombinerElement) element).getElement() instanceof Policy) {
					policies.add((Policy) ((PolicyCombinerElement) element)
							.getElement());
				} else if (((PolicyCombinerElement) element).getElement() instanceof PolicySet) {
					policies.add((PolicySet) ((PolicyCombinerElement) element)
							.getElement());
				}
			}
		}
		// evaluate
		result = this.combiningAlg.combine(context, combParams, policies);

		if (obligationExpressions.getObligationExpression().size() > 0) {
			// now, see if we should add any obligations to the set
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal())
					|| (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return obligations
				return result;
			}

			for (ObligationExpressionType myObligation : obligationExpressions
					.getObligationExpression()) {
				if (myObligation.getFulfillOn().ordinal() == effect) {
					result.addObligation(myObligation, context);
				}
			}
		}
		/* If we have advice, it's definitely a 3.0 policy */
		if (adviceExpressions.getAdviceExpression().size() > 0) {
			int effect = result.getDecision().ordinal();

			if ((effect == DecisionType.INDETERMINATE.ordinal()) || (effect == DecisionType.NOT_APPLICABLE.ordinal())) {
				// we didn't permit/deny, so we never return advices
				return result;
			}
			for (AdviceExpressionType adviceExpr : adviceExpressions.getAdviceExpression()) {
				if (adviceExpr.getAppliesTo().ordinal() == effect) {
					AdviceType advice = new AdviceType();
					advice.setAdviceId(adviceExpr.getAdviceId());
					for (AttributeAssignmentExpressionType attrExpr : adviceExpr
							.getAttributeAssignmentExpression()) {
						AttributeAssignmentType myAttrAssType = new AttributeAssignmentType();
						myAttrAssType.setAttributeId(attrExpr.getAttributeId());
						myAttrAssType.setCategory(attrExpr.getCategory());
						myAttrAssType.getContent()
								.add(attrExpr.getExpression());
						myAttrAssType.setIssuer(attrExpr.getIssuer());
						advice.getAttributeAssignment().add(myAttrAssType);
					}
				}
			}
		}
		if (context.getIncludeInResults().size() > 0) {
			result.getAttributes().addAll(context.getIncludeInResults());
		}

		return result;
	}

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

		if (obligationExpressions.getObligationExpression().size() != 0) {
			PrintStream out = new PrintStream(output);
			String indent = indenter.makeString();

			out.println(indent + "<Obligations>");
			indenter.in();

			it = obligationExpressions.getObligationExpression().iterator();
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
		return className + " id: \"" + policySetId + "\"";
	}

}
