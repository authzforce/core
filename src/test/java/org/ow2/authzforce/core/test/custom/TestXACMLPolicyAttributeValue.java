/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.test.custom;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.BaseDatatypeFactory;

/**
 * Represents a XACML Policy datatype (from XACML schema), to be used as AttributeValue.
 * <p>
 * Used here for testing Authzforce datatype extension mechanism, i.e. plugging a custom complex datatype into the PDP engine.
 * 
 */
public class TestXACMLPolicyAttributeValue extends AttributeValue
{

	/**
	 * Datatype ID
	 */
	public static final String ID = "urn:ow2:authzforce:feature:pdp:data-type:test-xacml-policy";

	private static final IllegalArgumentException NO_CONTENT_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + ID + "': empty");
	private static final IllegalArgumentException NO_ELEMENT_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + ID + "': no XML element");

	private final Policy policy;

	private TestXACMLPolicyAttributeValue(List<Serializable> content) throws IllegalArgumentException
	{
		super(ID, content, null);

		/*
		 * If content is empty, e.g. <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"/>, assume value is empty string.
		 */
		final Iterator<?> contentIterator = content.iterator();
		if (!contentIterator.hasNext())
		{
			throw NO_CONTENT_EXCEPTION;
		}

		/*
		 * 2 possibilities:
		 * 
		 * 1) first item is Policy element
		 * 
		 * 2) first item is String (if there is some whitespace for instance before the XML tag), then one Policy,...
		 */

		/*
		 * Tricky part: if the JAXB root element is annotated with @XmlRootElement, like Policy, it is not Serializable but still put in the List<Serializable> during unmarshalling. So, to avoid
		 * ClassCastException (from the corresponding JAXB-annotated type to Serializable), we use type Object here and expect it to be Policy.
		 */
		final Object content0 = contentIterator.next();
		if (content0 instanceof Policy)
		{
			policy = (Policy) content0;
		} else if (content0 instanceof String)
		{
			if (!contentIterator.hasNext())
			{
				throw NO_ELEMENT_EXCEPTION;
			}

			final Object content1 = contentIterator.next();
			if (content1 instanceof Policy)
			{
				policy = (Policy) content1;
			} else
			{
				throw new IllegalArgumentException("Invalid content for datatype '" + ID + "': second item (after text) is not a XACML <Policy>, but: " + content1.getClass());
			}
		} else
		{
			throw new IllegalArgumentException("Invalid content for datatype '" + ID + "': first item is neither text nor a XACML <Policy>, but: " + content0.getClass());
		}
	}

	/**
	 * Returns the internal <Policy>.
	 *
	 * @return the value
	 */
	public final Policy getUnderlyingValue()
	{
		return policy;
	}

	public static class Factory extends BaseDatatypeFactory<TestXACMLPolicyAttributeValue>
	{
		public Factory()
		{
			super(TestXACMLPolicyAttributeValue.class, ID);
		}

		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + ID
				+ "': extra XML attributes are not supported by this primitive datatype, only one XML element.");

		@Override
		public TestXACMLPolicyAttributeValue getInstance(List<Serializable> content, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{

			if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return new TestXACMLPolicyAttributeValue(content);
		}

		@Override
		public boolean isExpressionStatic()
		{
			return true;
		}

	}

}
