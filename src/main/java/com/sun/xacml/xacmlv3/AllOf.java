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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetSection;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;

/**
 * @author Romain Ferrari
 * 
 */
public class AllOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf
{

//	private static final Logger LOGGER = LoggerFactory.getLogger(AllOf.class);

	/**
	 * the version of XACML used by the containing Match element (never used actually!)
	 */
	// private int xacmlVersion;

	/**
	 * Constructor that creates a <code>AllOf</code> from components. In
	 * {@link com.sun.xacml.xacmlv3.Target#match(com.sun.xacml.EvaluationCtx)},
	 * {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.Match} objects are cast to {@link Match},
	 * so we need to make sure this is the {@link Match} type that is passed as constructor argument here to prevent
	 * ClassCastException.
	 * 
	 * @param matches
	 *            a <code>List</code> of <code>Match</code> elements
	 * @param version XACML version
	 */
	public AllOf(List<Match> matches, int version)
	{
		if (matches == null)
		{
			this.matches = new ArrayList<>();
		} else
		{
			this.matches =  new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.Match>(matches);
		}
		// this.xacmlVersion = version;
	}

	/**
	 * Creates AllOf in internal model based on AllOf element as defined in OASIS XACML model (JAXB)
	 * @param allOfElement
	 * @param metadata 
	 * @return AllOf handler
	 * @throws ParsingException if invalid AllOf element
	 */
	public static AllOf getInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf allOfElement, PolicyMetaData metadata) throws ParsingException
	{
		final List<Match> matchList = new ArrayList<>();
		for(final oasis.names.tc.xacml._3_0.core.schema.wd_17.Match matchElement: allOfElement.getMatches()) {
			final Match match = Match.getInstance(matchElement, metadata);
			matchList.add(match);
		}

		if (matchList.isEmpty())
		{
			throw new ParsingException("AllOf must contain at least one Match");
		}

		return new AllOf(matchList, PolicyMetaData.XACML_VERSION_3_0);
	}

	/**
	 * creates a new <code>AllOfSelection</code> by parsing DOM node.
	 * 
	 * @param root
	 *            DOM node
	 * @param metaData
	 *            policy meta data
	 * @return <code>AllOfSelection</code>
	 * @throws ParsingException
	 *             throws, if the DOM node is invalid
	 */
	public static AllOf getInstance(Node root, PolicyMetaData metaData) throws ParsingException
	{

		List<Match> matchType = new ArrayList<>();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if ("Match".equals(DOMHelper.getLocalName(child)))
			{
				matchType.add(Match.getInstance(child, metaData));
			}
		}

		if (matchType.isEmpty())
		{
			throw new ParsingException("AllOf must contain at least one Match");
		}

		return new AllOf(matchType, PolicyMetaData.XACML_VERSION_3_0);
	}

	public static List<TargetSection> getTargetSection(Node root, PolicyMetaData metadata) throws ParsingException
	{
		List<TargetSection> targetSection = new ArrayList<TargetSection>();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if ("Match".equals(DOMHelper.getLocalName(child)))
			{
				NodeList myNodes = child.getChildNodes();
				for (int j = 0; j < myNodes.getLength(); j++)
				{

					if ("AttributeDesignator".equals(myNodes.item(j).getNodeName()))
					{
						String myCategory = myNodes.item(j).getAttributes().getNamedItem("Category").getNodeValue();
						targetSection.add(TargetSection.getInstance(AttributeDesignator.getInstance(child, myCategory, metadata)));
					} else if ("AttributeSelector".equals(myNodes.item(j).getNodeName()))
					{

					} else
					{
						throw new ParsingException("Unknow Element: " + myNodes.item(j).getNodeName() + ". "
								+ "Supported element of Match are AttribtueDesignator and AttributeSelector.");
					}
				}
			}
		}

		return targetSection;
	}
}