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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.xacmlv3.AnyOf;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * Represents the TargetType XML type in XACML. This also stores several other
 * XML types: Subjects, Resources, Actions, and Environments (in XACML 2.0 and
 * later). The target is used to quickly identify whether the parent element (a
 * policy set, policy, or rule) is applicable to a given request.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Target extends TargetType {

	// the four sections of a Target
	private TargetSection subjectsSection;
	private TargetSection resourcesSection;
	private TargetSection actionsSection;
	private TargetSection environmentsSection;

	// the version of XACML of the policy containing this target
	private int xacmlVersion;

	List<AnyOf> anyOfType;

	// the logger we'll use for all messages
	private static final Logger logger = Logger.getLogger(Target.class
			.getName());

	/**
	 * Constructor that creates an XACML 1.x <code>Target</code> from
	 * components. Each of the sections must be non-null, but they may match any
	 * request. Because this is only used for 1.x Targets, there is no
	 * Environments section.
	 * 
	 * @param subjectsSection
	 *            a <code>TargetSection</code> representing the Subjects section
	 *            of this target
	 * @param resourcesSection
	 *            a <code>TargetSection</code> representing the Resources
	 *            section of this target
	 * @param actionsSection
	 *            a <code>TargetSection</code> representing the Actions section
	 *            of this target
	 */
	public Target(TargetSection subjectsSection,
			TargetSection resourcesSection, TargetSection actionsSection) {
		if ((subjectsSection == null) || (resourcesSection == null)
				|| (actionsSection == null))
			throw new ProcessingException("All sections of a Target must "
					+ "be non-null");

		this.subjectsSection = subjectsSection;
		this.resourcesSection = resourcesSection;
		this.actionsSection = actionsSection;
		this.environmentsSection = new TargetSection(null,
				TargetMatch.ENVIRONMENT, PolicyMetaData.XACML_VERSION_1_0);
		this.xacmlVersion = PolicyMetaData.XACML_VERSION_1_0;
	}

	/**
	 * Constructor that creates an XACML 2.0 <code>Target</code> from
	 * components. Each of the sections must be non-null, but they may match any
	 * request.
	 * 
	 * @param subjectsSection
	 *            a <code>TargetSection</code> representing the Subjects section
	 *            of this target
	 * @param resourcesSection
	 *            a <code>TargetSection</code> representing the Resources
	 *            section of this target
	 * @param actionsSection
	 *            a <code>TargetSection</code> representing the Actions section
	 *            of this target
	 * @param environmentsSection
	 *            a <code>TargetSection</code> representing the Environments
	 *            section of this target
	 */
	public Target(TargetSection subjectsSection,
			TargetSection resourcesSection, TargetSection actionsSection,
			TargetSection environmentsSection) {
		if ((subjectsSection == null) || (resourcesSection == null)
				|| (actionsSection == null) || (environmentsSection == null))
			throw new ProcessingException("All sections of a Target must "
					+ "be non-null");

		this.subjectsSection = subjectsSection;
		this.resourcesSection = resourcesSection;
		this.actionsSection = actionsSection;
		this.environmentsSection = environmentsSection;
		this.xacmlVersion = PolicyMetaData.XACML_VERSION_2_0;
		this.anyOfType = new ArrayList<AnyOf>();
	}

	/**
	 * Constructor that creates an XACML 3.0 <code>Target</code> from
	 * components.
	 * 
	 * @param anyOfType
	 *            List of <code>AnyOf</code> objects that representing the AnyOf
	 *            sections of this target
	 */
	public Target(TargetSection subjectsSection,
			TargetSection resourcesSection, TargetSection actionsSection,
			TargetSection environmentsSection, List<AnyOf> anyOfType) {
		if ((subjectsSection == null) || (resourcesSection == null)
				|| (actionsSection == null) || (environmentsSection == null))
			throw new ProcessingException("All sections of a Target must "
					+ "be non-null");

		this.subjectsSection = subjectsSection;
		this.resourcesSection = resourcesSection;
		this.actionsSection = actionsSection;
		this.environmentsSection = environmentsSection;
		this.xacmlVersion = PolicyMetaData.XACML_VERSION_3_0;
		this.anyOfType = anyOfType;
	}

	/**
	 * Creates a <code>Target</code> by parsing a node.
	 * 
	 * @deprecated As of 2.0 you should avoid using this method and should
	 *             instead use the version that takes a
	 *             <code>PolicyMetaData</code> instance. This method will only
	 *             work for XACML 1.x policies.
	 * 
	 * @param root
	 *            the node to parse for the <code>Target</code>
	 * @param xpathVersion
	 *            the XPath version to use in any selectors, or null if this is
	 *            unspecified (ie, not supplied in the defaults section of the
	 *            policy)
	 * 
	 * @return a new <code>Target</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if the DOM node is invalid
	 */
	public static TargetType getInstance(Node root, String xpathVersion)
			throws ParsingException {
		return getInstance(root, new PolicyMetaData(
				PolicyMetaData.XACML_1_0_IDENTIFIER, xpathVersion));
	}

	/**
	 * Creates a <code>Target</code> by parsing a node.
	 * 
	 * @param root
	 *            the node to parse for the <code>Target</code>
	 * @return a new <code>Target</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if the DOM node is invalid
	 */
	public static TargetType getInstance(Node root, PolicyMetaData metaData)
			throws ParsingException {
		TargetType returnTarget = new TargetType();
		List<TargetSection> subjects = new ArrayList<TargetSection>();
		List<TargetSection> resources = new ArrayList<TargetSection>();
		List<TargetSection> actions = new ArrayList<TargetSection>();
		List<TargetSection> environments = new ArrayList<TargetSection>();
		List<TargetSection> targetSectionTmp = new ArrayList<TargetSection>();
		List<AnyOf> anyOf = new ArrayList<AnyOf>();

		int version = metaData.getXACMLVersion();

		if (version == Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0
				.value())) {
			NodeList myChildren = root.getChildNodes();

			for (int i = 0; i < myChildren.getLength(); i++) {
				Node child = myChildren.item(i);
				if ("AnyOf".equals(DOMHelper.getLocalName(child))) {
					anyOf.add(AnyOf.getInstance(child, metaData));
				}
			}
			if (subjects.isEmpty()) {
				subjects.add(new TargetSection(null, TargetMatch.SUBJECT,
						version));
			}
			if (resources.isEmpty()) {
				resources.add(new TargetSection(null, TargetMatch.RESOURCE,
						version));
			}
			if (actions.isEmpty()) {
				actions.add(new TargetSection(null, TargetMatch.ACTION, version));
			}
			if (environments.isEmpty()) {
				environments.add(new TargetSection(null,
						TargetMatch.ENVIRONMENT, version));
			}
			returnTarget.getAnyOf().addAll(anyOf);

//			return new Target(subjects.get(0), resources.get(0), actions.get(0), environments.get(0), anyOf);
		}

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String name = child.getNodeName();

			if (name.equals("Subjects")) {
				subjects.add(TargetSection.getInstance(child,
						TargetMatch.SUBJECT, metaData));
			} else if (name.equals("Resources")) {
				resources.add(TargetSection.getInstance(child,
						TargetMatch.RESOURCE, metaData));
			} else if (name.equals("Actions")) {
				actions.add(TargetSection.getInstance(child,
						TargetMatch.ACTION, metaData));
			} else if (name.equals("Environments")) {
				environments.add(TargetSection.getInstance(child,
						TargetMatch.ENVIRONMENT, metaData));
			}
		}

		if (subjects.isEmpty()) {
			subjects.add(new TargetSection(null, TargetMatch.SUBJECT, version));
		}
		if (resources.isEmpty()) {
			resources
					.add(new TargetSection(null, TargetMatch.RESOURCE, version));
		}
		if (actions.isEmpty()) {
			actions.add(new TargetSection(null, TargetMatch.ACTION, version));
		}

		// starting in 2.0 an any-matching section is represented by a
		// missing element, and in 1.x there were no Environments elements,
		// so these need to get turned into non-null arguments
		//
		// FIXME: RF
		// Workaround in order to handle XACML 3.0 Policy
		if (version == PolicyMetaData.XACML_VERSION_2_0) {
			if (environments.isEmpty()) {
				environments.add(new TargetSection(null,
						TargetMatch.ENVIRONMENT, version));
			}
//			return new Target(subjects.get(0), resources.get(0),
//					actions.get(0), environments.get(0));
//		} else {
//			return new Target(subjects.get(0), resources.get(0), actions.get(0));
		}
		
		return returnTarget;
	}

	/**
	 * Returns the Subjects section of this Target.
	 * 
	 * @return a <code>TargetSection</code> representing the Subjects
	 */
	public TargetSection getSubjectsSection() {
		return subjectsSection;
	}

	/**
	 * Returns the Resources section of this Target.
	 * 
	 * @return a <code>TargetSection</code> representing the Resources
	 */
	public TargetSection getResourcesSection() {
		return resourcesSection;
	}

	/**
	 * Returns the Actions section of this Target.
	 * 
	 * @return a <code>TargetSection</code> representing the Actions
	 */
	public TargetSection getActionsSection() {
		return actionsSection;
	}

	/**
	 * Returns the Environments section of this Target. Note that if this is an
	 * XACML 1.x policy, then the section will always match anything, since
	 * XACML 1.x doesn't support matching on the Environment.
	 * 
	 * @return a <code>TargetSection</code> representing the Environments
	 */
	public TargetSection getEnvironmentsSection() {
		return environmentsSection;
	}

	/**
	 * Returns whether or not this <code>Target</code> matches any request.
	 * 
	 * @return true if this Target matches any request, false otherwise
	 */
	public boolean matchesAny() {
		boolean matchAny = false;
		matchAny = subjectsSection.matchesAny()
				&& resourcesSection.matchesAny() && actionsSection.matchesAny()
				&& environmentsSection.matchesAny();

		return matchAny;
	}

	/**
	 * Returns whether or not this <code>Target</code> matches any request.
	 * 
	 * @param version
	 *            the version of the context
	 * 
	 * @return true if this Target matches any request, false otherwise
	 */
	public boolean matchesAny(int version) {
		boolean matchAny = false;
		if (version == Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0
				.value())) {
			for (AnyOf anyOf : anyOfType) {
				for (AllOfType allOf : anyOf.getAllOf()) {
					matchAny = allOf.getMatch().isEmpty();
				}
			}
		} else {
			return matchesAny();
		}

		return matchAny;
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request
	 * (whether it is applicable).
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the target and the request
	 */
	public MatchResult match(EvaluationCtx context) {
		MatchResult result = null;

		// before matching, see if this target matches any request

		if (matchesAny(context.getVersion())) {
			return new MatchResult(MatchResult.MATCH);
		}

		// first, try matching the Subjects section
		result = subjectsSection.match(context);
		if (result.getResult() != MatchResult.MATCH) {
			logger.finer("failed to match Subjects section of Target");
			return result;
		}

		// now try matching the Resources section
		result = resourcesSection.match(context);
		if (result.getResult() != MatchResult.MATCH) {
			logger.finer("failed to match Resources section of Target");
			return result;
		}

		// next, look at the Actions section
		result = actionsSection.match(context);
		if (result.getResult() != MatchResult.MATCH) {
			logger.finer("failed to match Actions section of Target");
			return result;
		}

		// finally, match the Environments section
		result = environmentsSection.match(context);
		if (result.getResult() != MatchResult.MATCH) {
			logger.finer("failed to match Environments section of Target");
			return result;
		}

		// if we got here, then everything matched
		return result;
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		String indent = indenter.makeString();

		// see if this Target matches anything
		boolean matchesAny = (subjectsSection.matchesAny()
				&& resourcesSection.matchesAny() && actionsSection.matchesAny() && environmentsSection
				.matchesAny());

		if (matchesAny && (xacmlVersion == PolicyMetaData.XACML_VERSION_2_0)) {
			// in 2.0, if all the sections match any request, then the Target
			// element is empty and should be encoded simply as an empty tag
			out.println("<Target/>");
		} else {
			out.println(indent + "<Target>");
			indenter.in();

			subjectsSection.encode(output, indenter);
			resourcesSection.encode(output, indenter);
			actionsSection.encode(output, indenter);

			// we should only do this if we're a 2.0 policy
			if (xacmlVersion == PolicyMetaData.XACML_VERSION_2_0)
				environmentsSection.encode(output, indenter);

			indenter.out();
			out.println(indent + "</Target>");
		}
	}
}
