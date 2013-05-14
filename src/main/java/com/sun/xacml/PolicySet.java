/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.combine.CombinerParameter;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.xacmlv3.Target;


/**
 * Represents one of the two top-level constructs in XACML, the PolicySetType.
 * This can contain other policies and policy sets, and can also contain
 * URIs that point to policies and policy sets.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class PolicySet extends AbstractPolicySet
{
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(PolicySet.class);
	
    /**
     * Creates a new <code>PolicySet</code> with only the required elements.
     *
     * @param id the policy set identifier
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     policies in this set
     * @param target the <code>Target</code> for this set
     */
    public PolicySet(URI id, PolicyCombiningAlgorithm combiningAlg,
                     Target target) {
        this(id, null, combiningAlg, null, target, null, null, null, null);
    }

    /**
     * Creates a new <code>PolicySet</code> with only the required elements,
     * plus some policies.
     *
     * @param id the policy set identifier
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     policies in this set
     * @param target the <code>Target</code> for this set
     * @param policies a list of <code>AbstractPolicy</code> objects
     *
     * @throws IllegalArgumentException if the <code>List</code> of policies
     *                                  contains an object that is not an
     *                                  <code>AbstractPolicy</code>
     */
    public PolicySet(URI id, PolicyCombiningAlgorithm combiningAlg,
                     Target target, List policies) {
        this(id, null, combiningAlg, null, target, policies, null, null, null);
    }

    /**
     * Creates a new <code>PolicySet</code> with the required elements plus
     * some policies and a String description.
     *
     * @param id the policy set identifier
     * @param version the policy version or null for the default (this is
     *                always null for pre-2.0 policies)
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     policies in this set
     * @param description a <code>String</code> describing the policy
     * @param target the <code>Target</code> for this set
     * @param policies a list of <code>AbstractPolicy</code> objects
     *
     * @throws IllegalArgumentException if the <code>List</code> of policies
     *                                  contains an object that is not an
     *                                  <code>AbstractPolicy</code>
     */
    public PolicySet(URI id, String version,
                     PolicyCombiningAlgorithm combiningAlg,
                     String description, Target target, List policies) {
        this(id, version, combiningAlg, description, target, policies, null,
             null, null);
    }

    /**
     * Creates a new <code>PolicySet</code> with the required elements plus
     * some policies, a String description, and policy defaults.
     *
     * @param id the policy set identifier
     * @param version the policy version or null for the default (this is
     *                always null for pre-2.0 policies)
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     policies in this set
     * @param description a <code>String</code> describing the policy
     * @param target the <code>Target</code> for this set
     * @param policies a list of <code>AbstractPolicy</code> objects
     * @param defaultVersion the XPath version to use
     *
     * @throws IllegalArgumentException if the <code>List</code> of policies
     *                                  contains an object that is not an
     *                                  <code>AbstractPolicy</code>
     */
    public PolicySet(URI id, String version,
                     PolicyCombiningAlgorithm combiningAlg,
                     String description, TargetType target, List policies,
                     String defaultVersion) {
        this(id, version, combiningAlg, description, target, policies, defaultVersion, null, null);
    }

    /**
     * Creates a new <code>PolicySet</code> with the required elements plus
     * some policies, a String description, policy defaults, and obligations.
     *
     * @param id the policy set identifier
     * @param version the policy version or null for the default (this is
     *                always null for pre-2.0 policies)
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     policies in this set
     * @param description a <code>String</code> describing the policy
     * @param target the <code>Target</code> for this set
     * @param policies a list of <code>AbstractPolicy</code> objects
     * @param defaultVersion the XPath version to use
     * @param obligations a set of <code>Obligation</code> objects
     *
     * @throws IllegalArgumentException if the <code>List</code> of policies
     *                                  contains an object that is not an
     *                                  <code>AbstractPolicy</code>
     */
    public PolicySet(URI id, String version,
                     PolicyCombiningAlgorithm combiningAlg,
                     String description, TargetType target, List policies,
                     String defaultVersion, ObligationExpressionsType obligations, AdviceExpressionsType advices) {
        super(id, version, combiningAlg, description, target, defaultVersion, obligations, advices, null);

        List list = null;        

        // check that the list contains only AbstractPolicy objects
        if (policies != null) {
            list = new ArrayList();
            Iterator it = policies.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (! (o instanceof Policy)) {
                    throw new IllegalArgumentException("non-Policy " +
                                                       "in policies");
                }
                list.add(new PolicyCombinerElement((Policy)o));
            }
        }
        List policyList = new ArrayList();
        policyList = Collections.unmodifiableList(list);
        this.policySetOrPolicyOrPolicySetIdReference = ((List<JAXBElement<?>>) policyList);
//        setChildren(list);
    }
    
    /**
     * Creates a new <code>PolicySet</code> with the required and optional
     * elements. If you need to provide combining algorithm parameters, you
     * need to use this constructor. Note that unlike the other constructors
     * in this class, the policies list is actually a list of
     * <code>CombinerElement</code>s used to match a policy with any
     * combiner parameters it may have.
     *
     * @param id the policy set identifier
     * @param version the policy version or null for the default (this is
     *                always null for pre-2.0 policies)
     * @param combiningAlg the <code>CombiningAlgorithm</code> used on the
     *                     rules in this set
     * @param description a <code>String</code> describing the policy or
     *                    null if there is no description
     * @param target the <code>Target</code> for this policy
     * @param policyElements a list of <code>CombinerElement</code> objects or
     *                       null if there are no policies
     * @param defaultVersion the XPath version to use or null if there is
     *                       no default version
     * @param obligations a set of <code>Obligations</code> objects or null
     *                    if there are no obligations
     * @param parameters the <code>List</code> of
     *                   <code>CombinerParameter</code>s provided for general
     *                   use by the combining algorithm
     *
     * @throws IllegalArgumentException if the <code>List</code> of rules
     *                                  contains an object that is not a
     *                                  <code>Rule</code>
     */
    public PolicySet(URI id, String version,
                     PolicyCombiningAlgorithm combiningAlg,
                     String description, Target target, List policyElements,
                     String defaultVersion, ObligationExpressionsType obligations, AdviceExpressionsType advices, List parameters) {
        super(id, version, combiningAlg, description, target, defaultVersion, obligations, advices, parameters);

        // check that the list contains only CombinerElements
        if (policyElements != null) {
            Iterator it = policyElements.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (! (o instanceof PolicyCombinerElement)) {
                    throw new IllegalArgumentException("non-PolicyCombinerElement " +
                                                       "in policies");
                }
            }
        }

//        setChildren(policyElements);
    }

    /**
     * Creates a new PolicySet based on the given root node. This is 
     * private since every class is supposed to use a getInstance() method
     * to construct from a Node, but since we want some common code in the
     * parent class, we need this functionality in a constructor.
     */
    private PolicySet(Node root, PolicyFinder finder) throws ParsingException {
        super(root, "PolicySet", "PolicyCombiningAlgId");

        List policies = new ArrayList();
        HashMap policyParameters = new HashMap();
        HashMap policySetParameters = new HashMap();
        PolicyMetaData metaData = getMetaData();

        // collect the PolicySet-specific elements
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if (name.equals("Target")) {
                target = Target.getInstance(child, metaData);
            } else if (name.equals("Policy")) {
                policies.add(Policy.getInstance(child));
            } else if (name.equals("PolicySet")) {
                policies.add(PolicySet.getInstance(child));
            } else if (name.equals("PolicySetIdReference")) {
                policies.add(PolicyReference.getInstance(child, finder,
                                                         metaData));
            } else if (name.equals("PolicyIdReference")) {
                policies.add(PolicyReference.getInstance(child, finder,
                                                         metaData));
            } else if (name.equals("PolicyCombinerParameters")) {
                paramaterHelper(policyParameters, child, "Policy");
            } else if (name.equals("PolicySetCombinerParameters")) {
                paramaterHelper(policySetParameters, child, "PolicySet");
            }
        }

        // now make sure that we can match up any parameters we may have
        // found to a cooresponding Policy or PolicySet...
        List elements = new ArrayList();
        Iterator it = policies.iterator();

        // right now we have to go though each policy and based on several
        // possible cases figure out what parameters might apply...but
        // there should be a better way to do this

        while (it.hasNext()) {
            Object policy = (Object)(it.next());
            List list = null;

            if (policy instanceof Policy) {
                list = (List)(policyParameters.remove(((Policy)policy).getPolicyId()));
            } else if (policy instanceof PolicySet) {
            	//TODO: Handle PolicySetIdReference
                list = (List)(policySetParameters.remove(((PolicySet)policy).getPolicySetId()));
            } else {
                PolicyReference ref = (PolicyReference)policy;
                String id = ref.getReference().toString();

                if (ref.getReferenceType() ==
                    PolicyReference.POLICY_REFERENCE) {
                    list = (List)(policyParameters.remove(id));
                } else {
                    list = (List)(policySetParameters.remove(id));
                }
            }
            if(policy instanceof PolicyType) {
            	elements.add(new PolicyCombinerElement((PolicyType)policy, list));
            } else if(policy instanceof PolicySetType) {
            	elements.add(new PolicyCombinerElement((PolicySetType)policy, list));
            }
        }

        // ...and that there aren't extra parameters
        if (! policyParameters.isEmpty()) {
            throw new ParsingException("Unmatched parameters in Policy");
        } if (! policySetParameters.isEmpty()) {
            throw new ParsingException("Unmatched parameters in PolicySet");
        }
        // finally, set the list of Policies
        this.policySetOrPolicyOrPolicySetIdReference.addAll((List<JAXBElement<?>>) Collections.unmodifiableList(elements));        
//        setChildren(elements);
    }

    /**
     * Private helper method that handles parsing a collection of
     * parameters
     */
    private void paramaterHelper(HashMap parameters, Node root,
                                 String prefix) throws ParsingException {
        String ref = root.getAttributes().getNamedItem(prefix + "IdRef").
            getNodeValue();
        
        if (parameters.containsKey(ref)) {
            List list = (List)(parameters.get(ref));
            parseParameters(list, root);
        } else {
            List list = new ArrayList();
            parseParameters(list, root);
            parameters.put(ref, list);
        }
    }

    /**
     * Private helper method that handles parsing a single parameter.
     */
    private void parseParameters(List parameters, Node root)
        throws ParsingException
    {
        NodeList nodes = root.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("CombinerParameter"))
                parameters.add(CombinerParameter.getInstance(node));
        }
    }

    /**
     * Creates an instance of a <code>PolicySet</code> object based on a
     * DOM node. The node must be the root of PolicySetType XML object,
     * otherwise an exception is thrown. This <code>PolicySet</code> will
     * not support references because it has no <code>PolicyFinder</code>.
     *
     * @param root the DOM root of a PolicySetType XML type
     *
     * @throws ParsingException if the PolicySetType is invalid
     */
    public static PolicySet getInstance(Node root) throws ParsingException {
        return getInstance(root, null);
    }

    /**
     * Creates an instance of a <code>PolicySet</code> object based on a
     * DOM node. The node must be the root of PolicySetType XML object,
     * otherwise an exception is thrown. The finder is used to handle
     * policy references.
     *
     * @param root the DOM root of a PolicySetType XML type
     * @param finder the <code>PolicyFinder</code> used to handle references
     *
     * @throws ParsingException if the PolicySetType is invalid
     */
    public static PolicySet getInstance(Node root, PolicyFinder finder)
        throws ParsingException
    {
        // first off, check that it's the right kind of node
        if (! (root.getNodeName().equals("PolicySet") || root.getNodeName().equals("PolicySetType"))) {
            throw new ParsingException("Cannot create PolicySet from root of" +
                                       " type " + root.getNodeName());
        }

        return new PolicySet(root, finder);
    }

    /**
     * Encodes this <code>PolicySet</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>PolicySet</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
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
