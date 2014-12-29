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
package com.sun.xacml.attr.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 * The base type for all datatypes used in a policy or request/response, this abstract class
 * represents a value for a given attribute type. All the required types defined in the XACML
 * specification are provided as instances of <code>AttributeValue<code>s. If you want to
 * provide a new type, extend this class and implement the
 * <code>equals(Object)</code> and <code>hashCode</code> methods from <code>Object</code>, which are
 * used for equality checking.
 * 
 */
public class AttributeValue extends AttributeValueType implements Evaluatable
{

	// the type of this attribute
	private URI type;

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeValue.class);

	/**
	 * Gets AttributeValue handler based on AttributeValueType element as defined in OASIS XACML
	 * 
	 * @param attrValue
	 * @return AttributeValue
	 * @throws UnknownIdentifierException
	 *             if value data type isn't known to the default attribute factory
	 * @throws ParsingException
	 *             if the value string is invalid or can't be parsed according to specified value
	 *             datatype
	 */
	public static AttributeValue getInstance(AttributeValueType attrValue) throws UnknownIdentifierException, ParsingException
	{

		AttributeFactory attrFactory = AttributeFactory.getInstance();

		AttributeValue returnData = null;
		/*
		 * FIXME: what if attrValue.getContent() is empty list (no content): <AttributeValue
		 * ....DataType="..." />
		 */
		try
		{			
			Serializable attrValueContent = attrValue.getContent().get(0);
			returnData = attrFactory.createValue(URI.create(attrValue.getDataType()), String.valueOf(attrValueContent));
			// FIXME: comment on why SecurityException/IllegalArgumentException may occur
		} catch (SecurityException e)
		{
			throw new ParsingException("Error parsing AttributeValue", e);
		} catch (IllegalArgumentException e)
		{
			throw new ParsingException(e);
		}

		return returnData;
	}

	public static AttributeValue getInstance(Node root, PolicyMetaData metadata)
	{
		final JAXBElement<AttributeValueType> attrValue;
		try
		{
			Unmarshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			attrValue = u.unmarshal(root, AttributeValueType.class);
			return new AttributeValue(URI.create(attrValue.getValue().getDataType()), attrValue.getValue().getContent());
		} catch (Exception e)
		{
			LOGGER.error("Error unmarshalling AttributeValue", e);
		}

		return null;
	}

	/**
	 * Constructor that takes the specific attribute type.
	 * 
	 * @param type
	 *            the attribute's type
	 */
	protected AttributeValue(URI type, List<Serializable> content)
	{
		this.content = content;
		this.dataType = type.toASCIIString();
		this.type = type;
	}

	protected AttributeValue(URI type)
	{
		this.content = new ArrayList<>();
		this.dataType = type.toASCIIString();
		this.type = type;
	}

	/**
	 * Returns the type of this attribute value. By default this always returns the type passed to
	 * the constructor.
	 * 
	 * @return the attribute's type
	 */
	public URI getType()
	{
		return type;
	}

	/**
	 * Returns whether or not this value is actually a bag of values. This is a required interface
	 * from <code>Expression</code>, but the more meaningful <code>isBag</code> method is used by
	 * <code>AttributeValue</code>s, so this method is declared as final and calls the
	 * <code>isBag</code> method for this value.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	public final boolean returnsBag()
	{
		return isBag();
	}

	/**
	 * Returns whether or not this value is actually a bag of values. This is a required interface
	 * from <code>Evaluatable</code>, but the more meaningful <code>isBag</code> method is used by
	 * <code>AttributeValue</code>s, so this method is declared as final and calls the
	 * <code>isBag</code> method for this value.
	 * 
	 * 
	 * @deprecated As of 2.0, you should use the <code>returnsBag</code> method from the
	 *             super-interface <code>Expression</code>.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	@Override
	public final boolean evaluatesToBag()
	{
		return isBag();
	}

	/**
	 * Always returns an empty list since values never have children.
	 * 
	 * @return an empty <code>List</code>
	 */
	@Override
	public List getChildren()
	{
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns whether or not this value is actually a bag of values. By default this returns
	 * <code>false</code>. Typically, only the <code>BagAttribute</code> should ever override this
	 * to return <code>true</code>.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	public boolean isBag()
	{
		return false;
	}

	/**
	 * Implements the required interface from <code>Evaluatable</code>. Since there is nothing to
	 * evaluate in an attribute value, the default result is just this instance. Override this
	 * method if you want special behavior, like a dynamic value.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a successful evaluation containing this value
	 */
	@Override
	public EvaluationResult evaluate(EvaluationCtx context)
	{
		final AttributeValue attrVal;
		try
		{
			attrVal = AttributeValue.getInstance(this);		
		} catch (UnknownIdentifierException|ParsingException e)
		{
			Status status = new Status(Arrays.asList(Status.STATUS_PROCESSING_ERROR), e.getLocalizedMessage());
			return new EvaluationResult(status);
		} 
		
		return new EvaluationResult(attrVal);
	}

	/**
	 * Encodes the value in a form suitable for including in XML data like a request or an
	 * obligation. This must return a value that could in turn be used by the factory to create a
	 * new instance with the same value. XML-specific encoding like transforming &lt; to &amp;lt;
	 * shall not be done by this method.
	 * 
	 * @return a <code>String</code> form of the value
	 */
	public String encode()
	{
		StringWriter out = new StringWriter();
		try
		{
			Marshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling AttributeValue to String", e);
		}

		return out.toString();
	}

	@Override
	public String toString()
	{
		return encode();
	}

	/**
	 * Encodes this <code>AttributeValue</code> into its XML representation and writes this encoding
	 * to the given <code>OutputStream</code> with no indentation. This will always produce the
	 * version used in a policy rather than that used in a request, so this is equivalent to calling
	 * <code>encodeWithTags(true)</code> and then stuffing that into a stream.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>AttributeValue</code> into its XML representation and writes this encoding
	 * to the given <code>OutputStream</code> with indentation. This will always produce the version
	 * used in a policy rather than that used in a request, so this is equivalent to calling
	 * <code>encodeWithTags(true)</code> and then stuffing that into a stream.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		out.println(indenter.makeString() + encodeWithTags(true));
	}

	/**
	 * Encodes the value and includes the AttributeValue XML tags so that the resulting string can
	 * be included in a valid XACML policy or Request/Response. The <code>boolean</code> parameter
	 * lets you include the DataType attribute, which is required in a policy but not allowed in a
	 * Request or Response.
	 * 
	 * @param includeType
	 *            include the DataType XML attribute if <code>true</code>, exclude if
	 *            <code>false</code>
	 * 
	 * @return a <code>String</code> encoding including the XML tags
	 */
	public String encodeWithTags(boolean includeType)
	{
		// FIXME: Properly XML-encode the value
		if (includeType)
			return "<AttributeValue DataType=\"" + type.toString() + "\">" + encode() + "</AttributeValue>";
		else
			return "<AttributeValue>" + encode() + "</AttributeValue>";
	}

	public static List<AttributeValue> convertFromJAXB(List<AttributeValueType> avts) throws ParsingException, UnknownIdentifierException
	{
		if (avts == null)
		{
			return null;
		}

		final List<AttributeValue> resultAvts = new ArrayList<>();
		for (final AttributeValueType avt : avts)
		{
			final AttributeValue newAvt = AttributeValue.getInstance(avt);

			// final AttributeValue newAvt = convertFromJAXB(avt,
			// URI.create(avt.getDataType()));
			if (newAvt != null)
			{
				resultAvts.add(newAvt);
			}
		}

		return resultAvts;
	}
}
