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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetCombinerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.xacmlv3.IPolicy;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.xacmlv3.Target;

/**
 * Represents one of the two top-level constructs in XACML, the PolicySetType. This can contain
 * other policies and policy sets, and can also contain URIs that point to policies and policy sets.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PolicySet extends AbstractPolicySet implements IPolicy
{
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PolicySet.class);

	private static final CombiningAlgFactory COMBINING_ALG_FACTORY = CombiningAlgFactory.getInstance();

	/**
	 * Creates a new <code>PolicySet</code> with only the required elements.
	 * 
	 * @param id
	 *            the policy set identifier
	 * @param combiningAlg
	 *            the <code>CombiningAlgorithm</code> used on the policies in this set
	 * @param target
	 *            the <code>Target</code> for this set
	 * @throws ParsingException
	 */
	public PolicySet(URI id, PolicyCombiningAlgorithm combiningAlg, Target target) throws ParsingException
	{
		this(id, combiningAlg, target, null);
	}

	/**
	 * Creates a new <code>PolicySet</code> with only the required elements, plus some policies.
	 * 
	 * @param id
	 *            the policy set identifier
	 * @param combiningAlg
	 *            the <code>CombiningAlgorithm</code> used on the policies in this set
	 * @param target
	 *            the <code>Target</code> for this set
	 * @param policies
	 *            a list of <code>IPolicy</code> objects
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>List</code> of policies contains an object that is not an
	 *             <code>IPolicy</code>
	 */
	public PolicySet(URI id, PolicyCombiningAlgorithm combiningAlg, Target target, List<IPolicy> policies)
	{
		this(id, null, combiningAlg, null, target, policies, null);
	}

	/**
	 * Creates a new <code>PolicySet</code> with the required elements plus some policies and a
	 * String description.
	 * 
	 * @param id
	 *            the policy set identifier
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the <code>CombiningAlgorithm</code> used on the policies in this set
	 * @param description
	 *            a <code>String</code> describing the policy
	 * @param target
	 *            the <code>Target</code> for this set
	 * @param policies
	 *            a list of <code>IPolicy</code> objects
	 * @param defaultVersion
	 *            default XPath version
	 * @param obligations
	 * @param advices
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>List</code> of policies contains an object that is not an
	 *             <code>IPolicy</code>
	 */
	public PolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description, oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, List<IPolicy> policies,
			String defaultVersion, oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations, AdviceExpressions advices)
	{
		super(id, version, combiningAlg, description, target, defaultVersion);

		final List<PolicyCombinerElement> elements = new ArrayList<>();
		for (final IPolicy policyElt : policies)
		{
			elements.add(new PolicyCombinerElement(policyElt));
		}

		// finally, set the list of Policies
		/*
		 * FIXME: this is using a JAXB class member of type List<Object> for non-JAXB classes (not
		 * serializable/marshallable), therefore not what it is intended for, why? Would be more
		 * readable and simple to have another member of type List<PolicyCombinerElement>
		 */
		this.policySetsAndPoliciesAndPolicySetIdReferences = new ArrayList<Object>(Collections.unmodifiableList(elements));
	}

	/**
	 * Creates a new <code>PolicySet</code> with the required elements plus some policies, a String
	 * description, and policy defaults.
	 * 
	 * @param id
	 *            the policy set identifier
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the <code>CombiningAlgorithm</code> used on the policies in this set
	 * @param description
	 *            a <code>String</code> describing the policy
	 * @param target
	 *            the <code>Target</code> for this set
	 * @param policies
	 *            a list of <code>IPolicy</code> objects
	 * @param defaultVersion
	 *            the XPath version to use
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>List</code> of policies contains an object that is not an
	 *             <code>IPolicy</code>
	 */
	public PolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description, Target target, List<IPolicy> policies,
			String defaultVersion)
	{
		this(id, version, combiningAlg, description, target, policies, defaultVersion, null, null);
	}

	/**
	 * Creates a new <code>PolicySet</code> with the required and optional elements. If you need to
	 * provide combining algorithm parameters, you need to use this constructor. Note that unlike
	 * the other constructors in this class, the policies list is actually a list of
	 * <code>CombinerElement</code>s used to match a policy with any combiner parameters it may
	 * have.
	 * 
	 * @param id
	 *            the policy set identifier
	 * @param version
	 *            the policy version or null for the default (this is always null for pre-2.0
	 *            policies)
	 * @param combiningAlg
	 *            the <code>CombiningAlgorithm</code> used on the rules in this set
	 * @param description
	 *            a <code>String</code> describing the policy or null if there is no description
	 * @param target
	 *            the <code>Target</code> for this policy
	 * @param policySetChoiceElements
	 *            a list of objects in PolicySet choice elements (or null if there are none):
	 *            Policy(IdReference)s, PolicySet(IdReference)s, CombinerParameters,
	 *            Policy(Set)CombinerParameters
	 * @param defaultVersion
	 *            the XPath version to use or null if there is no default version
	 * @param obligations
	 *            a set of <code>Obligations</code> objects or null if there are no obligations
	 * @param advices
	 * @param finder
	 * @throws ParsingException
	 * 
	 */
	public PolicySet(URI id, String version, PolicyCombiningAlgorithm combiningAlg, String description,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Target target, List<Object> policySetChoiceElements, String defaultVersion,
			oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligations, AdviceExpressions advices, PolicyFinder finder)
			throws ParsingException
	{
		super(id, version, combiningAlg, description, target, defaultVersion, obligations, advices, null);

		final List<IPolicy> policies = new ArrayList<>();
		final List<CombinerParameter> combinerParamHandlers = new ArrayList<>();
		final Map<String, List<CombinerParameter>> policyCombinerParamHandlers = new HashMap<>();
		final Map<String, List<CombinerParameter>> policySetCombinerParamHandlers = new HashMap<>();
		final PolicyMetaData metaData = getMetaData();

		// collect instances of Policy(Set)(IdReference) and (Policy(Set))CombinerParameters from
		// XACML model
		this.target = new Target(target, metaData);
		for (final Object policyElt : policySetChoiceElements)
		{
			if (policyElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policy = (oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) policyElt;
				try
				{
					policies.add(Policy.getInstance((oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) policyElt));
				} catch (UnknownIdentifierException e)
				{
					throw new ParsingException("Invalid child Policy '" + policy.getPolicyId() + "' in  PolicySet '" + id + "'", e);
				}
			} else if (policyElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySet = (oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) policyElt;
				try
				{
					policies.add(PolicySet.getInstance(policySet, finder));
				} catch (UnknownIdentifierException e)
				{
					throw new ParsingException("Unknown combining algorithm for policy: " + policySet.getPolicySetId(), e);
				}
			} else if (policyElt instanceof JAXBElement)
			{
				final JAXBElement<?> jaxbElt = (JAXBElement<?>) policyElt;
				final String eltName = jaxbElt.getName().getLocalPart();
				if (eltName.equals("PolicyIdReference"))
				{
					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					policies.add(new PolicyReference(idRef, PolicyReference.POLICY_REFERENCE, finder, metaData));
				} else if (eltName.equals("PolicySetIdReference"))
				{
					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					policies.add(new PolicyReference(idRef, PolicyReference.POLICYSET_REFERENCE, finder, metaData));
				} else if (eltName.equals("CombinerParameters"))
				{
					final CombinerParametersType combinerParams = (CombinerParametersType) jaxbElt.getValue();
					parseCombinerParameters(combinerParams.getCombinerParameters(), combinerParamHandlers);
				}
			} else if (policyElt instanceof PolicyCombinerParameters)
			{
				final PolicyCombinerParameters policyCombinerParams = (PolicyCombinerParameters) policyElt;
				parsePolicyCombinerParameters(policyCombinerParams, policyCombinerParamHandlers);
			} else if (policyElt instanceof PolicySetCombinerParameters)
			{
				final PolicySetCombinerParameters policySetCombinerParams = (PolicySetCombinerParameters) policyElt;
				parsePolicySetCombinerParameters(policySetCombinerParams, policySetCombinerParamHandlers);
			}
		}

		/*
		 * Checking PolicyCombinerParameters IdRefs match Policy(IdReference)s, and
		 * PolicySetCombinerParameters IdRefs match PolicySet(IdReference)s
		 */
		// right now we have to go though each policy and based on several
		// possible cases figure out what parameters might apply...but
		// there should be a better way to do this
		final List<PolicyCombinerElement> elements = new ArrayList<>();
		for (final IPolicy policyElt : policies)
		{
			final List<CombinerParameter> paramList;
			if (policyElt instanceof Policy)
			{
				paramList = policyCombinerParamHandlers.remove(policyElt.getId());
			} else if (policyElt instanceof PolicySet)
			{
				paramList = policySetCombinerParamHandlers.remove(policyElt.getId());
			} else if (policyElt instanceof PolicyReference)
			{
				final PolicyReference ref = (PolicyReference) policyElt;
				if (ref.getReferenceType() == PolicyReference.POLICY_REFERENCE)
				{
					paramList = policyCombinerParamHandlers.remove(policyElt.getId());
				} else
				{
					paramList = policySetCombinerParamHandlers.remove(policyElt.getId());
				}
			} else
			{
				paramList = null;
			}

			elements.add(new PolicyCombinerElement(policyElt, paramList));
		}

		// ...and that there aren't extra parameters
		if (!policyCombinerParamHandlers.isEmpty())
		{
			throw new ParsingException("PolicyCombinerParameters PolicyIdRefs not matched by any PolicyId/PolicyIdReference: "
					+ policyCombinerParamHandlers.keySet());
		}
		if (!policySetCombinerParamHandlers.isEmpty())
		{
			throw new ParsingException("PolicySetCombinerParameters PolicySetIdRefs not matched by any PolicySetId/PolicySetIdReference: "
					+ policySetCombinerParamHandlers.keySet());
		}
		// finally, set the list of Policies
		/*
		 * FIXME: this is using a JAXB class member of type List<Object> for non-JAXB classes (not
		 * serializable/marshallable), therefore not what it is intended for, why? Would be more
		 * readable and simple to have another member of type List<PolicyCombinerElement>, because
		 * here we lose the type of element stored in the list, therefore code is more prone to
		 * bugs.
		 */
		this.policySetsAndPoliciesAndPolicySetIdReferences = new ArrayList<Object>(Collections.unmodifiableList(elements));
		// setChildren(elements);

	}

	/**
	 * @param policySetElement
	 *            PolicySet Element from XACML model
	 * @param finder
	 * @return PolicySet handler
	 * @throws UnknownIdentifierException
	 *             if policyset-combining-algorithm ID is unknown
	 * @throws ParsingException
	 */
	public static PolicySet getInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySetElement, PolicyFinder finder)
			throws UnknownIdentifierException, ParsingException
	{
		return new PolicySet(URI.create(policySetElement.getPolicySetId()), policySetElement.getVersion(),
				(PolicyCombiningAlgorithm) COMBINING_ALG_FACTORY.createAlgorithm(URI.create(policySetElement.getPolicyCombiningAlgId())),
				policySetElement.getDescription(), policySetElement.getTarget(), policySetElement.getPolicySetsAndPoliciesAndPolicySetIdReferences(),
				policySetElement.getPolicySetDefaults() == null ? null : policySetElement.getPolicySetDefaults().getXPathVersion(),
				policySetElement.getObligationExpressions(), policySetElement.getAdviceExpressions(), finder);
	}

	/**
	 * Creates a new PolicySet based on the given root node. This is private since every class is
	 * supposed to use a getInstance() method to construct from a Node, but since we want some
	 * common code in the parent class, we need this functionality in a constructor.
	 */
	private PolicySet(Node root, PolicyFinder finder) throws ParsingException
	{
		super(root, "PolicySet", "PolicyCombiningAlgId");

		List<IPolicy> policies = new ArrayList<>();
		HashMap policyParameters = new HashMap();
		HashMap policySetParameters = new HashMap();
		PolicyMetaData metaData = getMetaData();

		// collect the PolicySet-specific elements
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			String name = child.getNodeName();

			if (name.equals("Target"))
			{
				target = Target.getInstance(child, metaData);
			} else if (name.equals("Policy"))
			{
				policies.add(Policy.getInstance(child));
			} else if (name.equals("PolicySet"))
			{
				policies.add(PolicySet.getInstance(child));
			} else if (name.equals("PolicySetIdReference"))
			{
				policies.add(PolicyReference.getInstance(child, finder, metaData));
			} else if (name.equals("PolicyIdReference"))
			{
				policies.add(PolicyReference.getInstance(child, finder, metaData));
			} else if (name.equals("PolicyCombinerParameters"))
			{
				paramaterHelper(policyParameters, child, "Policy");
			} else if (name.equals("PolicySetCombinerParameters"))
			{
				paramaterHelper(policySetParameters, child, "PolicySet");
			}

			/*
			 * FIXME: CombinerParameters element not supported
			 */
		}

		// now make sure that we can match up any parameters we may have
		// found to a cooresponding Policy or PolicySet...
		List elements = new ArrayList();
		Iterator<IPolicy> it = policies.iterator();

		// right now we have to go though each policy and based on several
		// possible cases figure out what parameters might apply...but
		// there should be a better way to do this

		while (it.hasNext())
		{
			IPolicy policy = it.next();
			List list = null;

			if (policy instanceof Policy)
			{
				list = (List) (policyParameters.remove(((Policy) policy).getPolicyId()));
			} else if (policy instanceof PolicySet)
			{
				// TODO: Handle PolicySetIdReference
				list = (List) (policySetParameters.remove(((PolicySet) policy).getPolicySetId()));
			} else
			{
				PolicyReference ref = (PolicyReference) policy;
				String id = ref.getReference().toString();

				if (ref.getReferenceType() == PolicyReference.POLICY_REFERENCE)
				{
					list = (List) (policyParameters.remove(id));
				} else
				{
					list = (List) (policySetParameters.remove(id));
				}
			}
			
			elements.add(new PolicyCombinerElement(policy, list));
			
		}

		// ...and that there aren't extra parameters
		if (!policyParameters.isEmpty())
		{
			throw new ParsingException("Unmatched parameters in Policy");
		}
		if (!policySetParameters.isEmpty())
		{
			throw new ParsingException("Unmatched parameters in PolicySet");
		}
		// finally, set the list of Policies
		/*
		 * FIXME: this is using a JAXB class member of type List<Object> for non-JAXB classes (not
		 * serializable/marshallable), therefore not what it is intended for, why? Would be more
		 * readable and simple to have another member of type List<PolicyCombinerElement>
		 */
		this.policySetsAndPoliciesAndPolicySetIdReferences.addAll((List<JAXBElement<?>>) Collections.unmodifiableList(elements));
		// setChildren(elements);
	}

	/**
	 * Private helper method that handles parsing a collection of parameters
	 */
	private void paramaterHelper(HashMap parameters, Node root, String prefix) throws ParsingException
	{
		String ref = root.getAttributes().getNamedItem(prefix + "IdRef").getNodeValue();

		if (parameters.containsKey(ref))
		{
			List list = (List) (parameters.get(ref));
			parseParameters(list, root);
		} else
		{
			List list = new ArrayList();
			parseParameters(list, root);
			parameters.put(ref, list);
		}
	}

	private static void parsePolicyCombinerParameters(PolicyCombinerParameters policyCombinerParams, Map<String, List<CombinerParameter>> parameters)
			throws ParsingException
	{
		final String policyIdRef = policyCombinerParams.getPolicyIdRef();
		final List<CombinerParameter> paramHandlerList;
		if (parameters.containsKey(policyIdRef))
		{
			paramHandlerList = parameters.get(policyIdRef);
		} else
		{
			paramHandlerList = new ArrayList<>();
			parameters.put(policyIdRef, paramHandlerList);
		}

		parseCombinerParameters(policyCombinerParams.getCombinerParameters(), paramHandlerList);
	}

	private static void parsePolicySetCombinerParameters(PolicySetCombinerParameters policySetCombinerParams,
			Map<String, List<CombinerParameter>> parameters) throws ParsingException
	{
		final String policySetIdRef = policySetCombinerParams.getPolicySetIdRef();
		final List<CombinerParameter> paramHandlerList;
		if (parameters.containsKey(policySetIdRef))
		{
			paramHandlerList = parameters.get(policySetIdRef);
		} else
		{
			paramHandlerList = new ArrayList<CombinerParameter>();
			parameters.put(policySetIdRef, paramHandlerList);
		}

		parseCombinerParameters(policySetCombinerParams.getCombinerParameters(), paramHandlerList);
	}

	/**
	 * Private helper method that handles parsing a single parameter.
	 */
	private static void parseParameters(List parameters, Node root) throws ParsingException
	{
		NodeList nodes = root.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("CombinerParameter"))
				parameters.add(CombinerParameter.getInstance(node));
		}
	}

	private static void parseCombinerParameters(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter> parameters,
			List<CombinerParameter> parameterHandlerList) throws ParsingException
	{
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter param : parameters)
		{
			final CombinerParameter parameterHandler = new CombinerParameter(param);
			parameterHandlerList.add(parameterHandler);
		}
	}

	/**
	 * Creates an instance of a <code>PolicySet</code> object based on a DOM node. The node must be
	 * the root of PolicySetType XML object, otherwise an exception is thrown. This
	 * <code>PolicySet</code> will not support references because it has no
	 * <code>PolicyFinder</code>.
	 * 
	 * @param root
	 *            the DOM root of a PolicySetType XML type
	 * @return PolicySet
	 * 
	 * @throws ParsingException
	 *             if the PolicySetType is invalid
	 */
	public static PolicySet getInstance(Node root) throws ParsingException
	{
		return getInstance(root, null);
	}

	/**
	 * Creates an instance of a <code>PolicySet</code> object based on a DOM node. The node must be
	 * the root of PolicySetType XML object, otherwise an exception is thrown. The finder is used to
	 * handle policy references.
	 * 
	 * @param root
	 *            the DOM root of a PolicySetType XML type
	 * @param finder
	 *            the <code>PolicyFinder</code> used to handle references
	 * @return policySet
	 * 
	 * @throws ParsingException
	 *             if the PolicySetType is invalid
	 */
	public static PolicySet getInstance(Node root, PolicyFinder finder) throws ParsingException
	{
		// first off, check that it's the right kind of node
		if (!(root.getNodeName().equals("PolicySet") || root.getNodeName().equals("PolicySetType")))
		{
			throw new ParsingException("Cannot create PolicySet from root of" + " type " + root.getNodeName());
		}

		return new PolicySet(root, finder);
	}

	/**
	 * Encodes this <code>PolicySet</code> into its XML representation and writes this encoding to
	 * the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>PolicySet</code> into its XML representation and writes this encoding to
	 * the given <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		try
		{
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling PolicySet", e);
		}
	}

}
