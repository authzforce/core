/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.testutil.ext;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;

import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BaseAttributeValueFactory;

/**
 * Represents a XACML Policy datatype (from XACML schema), to be used as AttributeValue.
 * <p>
 * Used here for testing Authzforce datatype extension mechanism, i.e. plugging a custom complex datatype into the PDP engine.
 * 
 */
public class TestXacmlPolicyAttributeValue extends AttributeValue
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Datatype
	 */
	public static final AttributeDatatype<TestXacmlPolicyAttributeValue> DATATYPE = new AttributeDatatype<>(TestXacmlPolicyAttributeValue.class,
			"urn:ow2:authzforce:feature:pdp:data-type:test-xacml-policy", "urn:ow2:authzforce:feature:pdp:function:test-xacml-policy");

	private static final IllegalArgumentException NO_CONTENT_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': empty");
	private static final IllegalArgumentException NO_ELEMENT_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': no XML element");

	private final Policy policy;

	private TestXacmlPolicyAttributeValue(final List<Serializable> content) throws IllegalArgumentException
	{
		super(DATATYPE.getId(), content, Optional.empty());

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
		}
		else if (content0 instanceof String)
		{
			if (!contentIterator.hasNext())
			{
				throw NO_ELEMENT_EXCEPTION;
			}

			final Object content1 = contentIterator.next();
			if (content1 instanceof Policy)
			{
				policy = (Policy) content1;
			}
			else
			{
				throw new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': second item (after text) is not a XACML <Policy>, but: " + content1.getClass());
			}
		}
		else
		{
			throw new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': first item is neither text nor a XACML <Policy>, but: " + content0.getClass());
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

	public static class Factory extends BaseAttributeValueFactory<TestXacmlPolicyAttributeValue>
	{
		public Factory()
		{
			super(DATATYPE);
		}

		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE
				+ "': extra XML attributes are not supported by this primitive datatype, only one XML element.");
		private static final IllegalArgumentException UNDEFINED_CONTENT_ARG_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': null.");

		@Override
		public TestXacmlPolicyAttributeValue getInstance(final List<Serializable> content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler)
				throws IllegalArgumentException
		{
			if (content == null || content.isEmpty())
			{
				throw UNDEFINED_CONTENT_ARG_EXCEPTION;
			}

			if (!otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return new TestXacmlPolicyAttributeValue(content);
		}

	}

}
