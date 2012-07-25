/*
 * @(#)AttributeValue.java
 *
 * Copyright 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml.attr;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.URI;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The base type for all datatypes used in a policy or request/response, this
 * abstract class represents a value for a given attribute type. All the
 * required types defined in the XACML specification are provided as instances
 * of <code>AttributeValue<code>s. If you want to
 * provide a new type, extend this class and implement the
 * <code>equals(Object)</code> and <code>hashCode</code> methods from
 * <code>Object</code>, which are used for equality checking.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class AttributeValue implements Evaluatable {

	private static DocumentBuilder db1 = null;
	// the type of this attribute
	private URI type;

	/**
	 * Constructor that takes the specific attribute type.
	 * 
	 * @param type
	 *            the attribute's type
	 */
	protected AttributeValue(URI type) {
		this.type = type;
	}

	/**
	 * Returns the type of this attribute value. By default this always returns
	 * the type passed to the constructor.
	 * 
	 * @return the attribute's type
	 */
	public URI getType() {
		return type;
	}

	/**
	 * Returns whether or not this value is actually a bag of values. This is a
	 * required interface from <code>Expression</code>, but the more meaningful
	 * <code>isBag</code> method is used by <code>AttributeValue</code>s, so
	 * this method is declared as final and calls the <code>isBag</code> method
	 * for this value.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	public final boolean returnsBag() {
		return isBag();
	}

	/**
	 * Returns whether or not this value is actually a bag of values. This is a
	 * required interface from <code>Evaluatable</code>, but the more meaningful
	 * <code>isBag</code> method is used by <code>AttributeValue</code>s, so
	 * this method is declared as final and calls the <code>isBag</code> method
	 * for this value.
	 * 
	 * 
	 * @deprecated As of 2.0, you should use the <code>returnsBag</code> method
	 *             from the super-interface <code>Expression</code>.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	public final boolean evaluatesToBag() {
		return isBag();
	}

	/**
	 * Always returns an empty list since values never have children.
	 * 
	 * @return an empty <code>List</code>
	 */
	public List getChildren() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns whether or not this value is actually a bag of values. By default
	 * this returns <code>false</code>. Typically, only the
	 * <code>BagAttribute</code> should ever override this to return
	 * <code>true</code>.
	 * 
	 * @return true if this is a bag of values, false otherwise
	 */
	public boolean isBag() {
		return false;
	}

	/**
	 * Implements the required interface from <code>Evaluatable</code>. Since
	 * there is nothing to evaluate in an attribute value, the default result is
	 * just this instance. Override this method if you want special behavior,
	 * like a dynamic value.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return a successful evaluation containing this value
	 */
	public EvaluationResult evaluate(EvaluationCtx context) {
		return new EvaluationResult(this);
	}

	/**
	 * Encodes the value in a form suitable for including in XML data like a
	 * request or an obligation. This must return a value that could in turn be
	 * used by the factory to create a new instance with the same value.
	 * XML-specific encoding like transforming &lt; to &amp;lt; shall not be
	 * done by this method.
	 * 
	 * @return a <code>String</code> form of the value
	 */
	public abstract String encode();

	/**
	 * Encodes this <code>AttributeValue</code> into its XML representation and
	 * writes this encoding to the given <code>OutputStream</code> with no
	 * indentation. This will always produce the version used in a policy rather
	 * than that used in a request, so this is equivalent to calling
	 * <code>encodeWithTags(true)</code> and then stuffing that into a stream.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>AttributeValue</code> into its XML representation and
	 * writes this encoding to the given <code>OutputStream</code> with
	 * indentation. This will always produce the version used in a policy rather
	 * than that used in a request, so this is equivalent to calling
	 * <code>encodeWithTags(true)</code> and then stuffing that into a stream.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		out.println(indenter.makeString() + encodeWithTags(true));
	}

	/**
	 * Encodes the value and includes the AttributeValue XML tags so that the
	 * resulting string can be included in a valid XACML policy or
	 * Request/Response. The <code>boolean</code> parameter lets you include the
	 * DataType attribute, which is required in a policy but not allowed in a
	 * Request or Response.
	 * 
	 * @param includeType
	 *            include the DataType XML attribute if <code>true</code>,
	 *            exclude if <code>false</code>
	 * 
	 * @return a <code>String</code> encoding including the XML tags
	 */
	public String encodeWithTags(boolean includeType) {
		// FIXME: Properly XML-encode the value
		if (includeType)
			return "<AttributeValue DataType=\"" + type.toString() + "\">"
					+ encode() + "</AttributeValue>";
		else
			return "<AttributeValue>" + encode() + "</AttributeValue>";
	}

	public static AttributeValue convertFromJAXB(AttributeValueType avt,
			URI dataType) throws ParsingException {
		try {
			AttributeValue result = null;
			AttributeFactory attrFactory = AttributeFactory.getInstance();
			result = attrFactory.createValue(getAttributeValueNode(avt),
					dataType);
			return result;
		} catch (UnknownIdentifierException ex) {
			throw new ParsingException(ex);
		}
	}

	public static AttributeValue convertFromJAXB(List<AttributeValueType> avts,
			URI dataType) throws ParsingException {
		if (avts.size() > 0) {
			// throw new
			// UnsupportedOperationException("XACML2 multiple AttributeValues not supported yet.");
		}
		return convertFromJAXB(avts.get(0), dataType);
	}

	
	/**
	 * 
	 * Romain FERRARI (THALES)
	 * @BUG
	 **/
	
	/**
	 * 
	 * @param avt
	 * @return
	 */
	public static Node getAttributeValueNode(AttributeValueType avt) {
		Node node = null;
		
		try {
			Marshaller m = BindingUtility.createMarshaller();
			synchronized (DocumentBuilder.class) {
				db1 = BindingUtility.getDocumentBuilder();	
			}			
			Document doc = db1.newDocument();
			JAXBElement<AttributeValueType> element = BindingUtility.contextFac
					.createAttributeValue(avt);
			m.marshal(element, doc);
			node = doc.getDocumentElement();
		} catch (JAXBException ex) {
			throw new RuntimeException(ex);
		}

		return node;
	}
	
//	public static Node getAttributeValueNode(AttributeValueType avt) {
//        Node node = null;
//        try {
//            Marshaller m = BindingUtility.createMarshaller();
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setNamespaceAware(true);
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document doc = db.newDocument();
//            JAXBElement<AttributeValueType> element = BindingUtility.contextFac.createAttributeValue(avt);
//            m.marshal(element, doc);
//            node = doc.getDocumentElement();
//        } catch (JAXBException ex) {
//            throw new RuntimeException(ex);
//        } catch (ParserConfigurationException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        return node;
//    }
}
