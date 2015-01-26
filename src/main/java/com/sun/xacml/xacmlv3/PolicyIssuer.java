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

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.ctx.Attribute;

public class PolicyIssuer extends oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer
{

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyIssuer.class);

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer getInstance(Node root)
	{
		Content content = new Content();
		List<Attribute> attribute = new ArrayList<>();

		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if ("Attribute".equals(DOMHelper.getLocalName(child)))
			{
				try
				{
					attribute.add(Attribute.getInstance(child, PolicyMetaData.XACML_VERSION_3_0));
				} catch (ParsingException e)
				{
					/**
					 * TODO: should we not let the exception be thrown? Is this not critical?
					 */
					LOGGER.error("Error parsing Attribute from DOM node", e);
				}
			} else if ("Content".equals(DOMHelper.getLocalName(child)))
			{
				content.getContent().add(child.getTextContent());
			}
		}

		return new PolicyIssuer(content, attribute);
	}

	/**
	 * @param content
	 * @param attributes
	 */
	public PolicyIssuer(Content content, List<Attribute> attributes)
	{
		this.content = content;
		this.attributes = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute>(attributes);
	}

	/**
	 * Creates PolicyIssuer handler from PolicyIssuer element as defined in OASIS XACML model
	 * @param issuer
	 * @return PolicyIssuer handler
	 * @throws UnknownIdentifierException if one of the PolicyIssuer AttributeValue datatype is unknown/not supported
	 * @throws ParsingException if one of the PolicyIssuer AttributeValue content cannot be parsed according to specified value datatype

	 */
	public static PolicyIssuer getInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer issuer) throws ParsingException, UnknownIdentifierException
	{
		final List<Attribute> attrList = new ArrayList<>();
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute attrElt : issuer.getAttributes())
		{
			final Attribute attribute = Attribute.getInstance(attrElt,  PolicyMetaData.XACML_VERSION_3_0);
			attrList.add(attribute);
		}
		
		return new PolicyIssuer(issuer.getContent(), attrList);
	}

}