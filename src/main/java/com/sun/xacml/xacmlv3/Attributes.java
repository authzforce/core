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
package com.sun.xacml.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.ctx.Attribute;
import com.thalesgroup.authzforce.xacml._3_0.identifiers.XACMLAttributeId;

/**
 * Represents the AttributesType XML type found in the context schema.
 * TODO But here, not supporting the xml:id  Identifier. Just use it as String  attributes
 */
public class Attributes {

    /**
     * category of the Attributes element whether it is subject, action and etc
     */
    private URI category;

    /**
     *  content of the Attributes element that can be a XML data
     */
    private Node content;

    /**
     *  a <code>Set</code> of <code>Attribute</code> that contains in <code>Attributes</code> 
     */
    private Set<Attribute> attributes;

    /**
     * id of the Attribute element
     */
    private String id;

    /**
     * Constructor that creates a new <code>Attributes</code> based on
     * the given elements.
     * @param category category of the Attributes element whether it is subject, action and etc
     * @param attributes  a <code>Set</code> of <code>Attribute</code>
     * that contains in <code>Attributes</code>
     */
    public Attributes(URI category,Set<Attribute> attributes) {
        this(category, null, attributes, null);
    }

    /**
     * Constructor that creates a new <code>Attributes</code> based on
     * the given elements.
     * @param category category of the Attributes element whether it is subject, action and etc
     * @param content content of the Attributes element that can be a XML data
     * @param attributes  a <code>Set</code> of <code>Attribute</code>
     * that contains in <code>Attributes</code> 
     * @param id   id of the Attribute element
     */
    public Attributes(URI category, Node content, Set<Attribute> attributes, String id) {
        this.category = category;
        this.content = content;
        this.attributes = attributes;
        this.id = id;
    }

    /**
     *
     * @param root
     * @return
     * @throws ParsingException
     */
    public static Attributes getInstance(Node root) throws ParsingException {
        URI category ;
        Node content = null;
        String id = null;
        Set<Attribute> attributes = new HashSet<Attribute>();

        // First check that we're really parsing an Attribute
        if (!DOMHelper.getLocalName(root).equals(XACMLAttributeId.ATTRIBUTES_ELEMENT.value())) {
            throw new ParsingException("Attributes object cannot be created "
                    + "with root node of type: " + DOMHelper.getLocalName(root));
        }

        NamedNodeMap attrs = root.getAttributes();

        try {
            category = new URI(attrs.getNamedItem(XACMLAttributeId.ATTRIBUTES_CATEGORY.value()).getNodeValue());
        } catch (Exception e) {
            throw new ParsingException("Error parsing required attribute "
                    + "AttributeId in AttributesType", e);
        }

        try {
            Node idNode = attrs.getNamedItem(XACMLAttributeId.ATTRIBUTES_ID.value());
            if(idNode != null){
                id = idNode.getNodeValue();
            }
        } catch (Exception e) {
            throw new ParsingException("Error parsing optional attributes in " +
                            "AttributesType", e);
        }

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (DOMHelper.getLocalName(node).equals(XACMLAttributeId.ATTRIBUTES_CONTENT.value())) {
                // only one value can be in an Attribute
                if (content != null){
                    throw new ParsingException("Too many content elements are defined.");
                }
                // now get the value
                content = node.getFirstChild();
            } else if(DOMHelper.getLocalName(node).equals(XACMLAttributeId.ATTRIBUTE_ELEMENT.value())) {
            	/*
            	 * FIXME: RF
            	 */
                attributes.add(Attribute.getInstance(node, PolicyMetaData.XACML_VERSION_3_0));
            }
        }

        return new Attributes(category, content, attributes , id);
    }

    /**
     * Returns the category of this attributes
     *
     * @return the attribute 's category as <code>URI</code>
     */
    public URI getCategory() {
        return category;
    }

    /**
     * Returns the content of this attributes, or null if no content was included
     *
     * @return the attribute 's content as <code>Node</code> or null
     */
    public Node getContent() {
        return content;
    }

    /**
     * Returns list of attribute that contains in the attributes element
     *
     * @return  list of <code>Attribute</code>
     */
    public Set<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns the id of this attributes, or null if it was not included
     *
     * @return  the attribute 's id as <code>String</code> or null 
     */
    public String getId() {
        return id;
    }

    /**
     * Encodes this attribute into its XML representation and writes this encoding to the given
     * <code>OutputStream</code> with no indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this attribute into its XML representation and writes this encoding to the given
     * <code>OutputStream</code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {

        String indent = indenter.makeString();
        PrintStream out = new PrintStream(output);

        out.println(indent + "<Attributes Category=\"" + category.toString() + "\">");

        indenter.in();

        for(Attribute attribute : attributes){
            if(attribute.isIncludeInResult()){
                attribute.encode(output, indenter);
            }
        }

        indenter.out();

        indenter.in();
        if (content != null) {
        // TODO
        }

        out.println(indent + "</Attributes>");
    }
}