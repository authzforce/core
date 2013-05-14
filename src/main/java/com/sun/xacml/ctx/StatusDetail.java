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
package com.sun.xacml.ctx;

import com.sun.xacml.ParsingException;

import java.io.ByteArrayInputStream;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * This class represents the StatusDetailType in the context schema. Because
 * status detail is defined as a sequence of xs:any XML type, the data in
 * this class must be generic, and it is up to the application developer to
 * interpret the data appropriately.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class StatusDetail
{

    // the root node
    private Node detailRoot;

    // the text version, if it's avilable already
    private String detailText = null;

    /**
     * Constructor that uses a <code>List</code> of <code>Attribute</code>s
     * to define the status detail. This is a common form of detail data,
     * and can be used for things like providing the information included
     * with the missing-attribute status code.
     *
     * @param attributes a <code>List</code> of <code>Attribute</code>s
     *
     * @throws IllegalArgumentException if there is a problem encoding the
     *                                  <code>Attribute</code>s
     */
    public StatusDetail(List attributes) throws IllegalArgumentException {
        detailText = "<StatusDetail>\n";
        Iterator it = attributes.iterator();

        while (it.hasNext()) {
            Attribute attr = (Attribute)(it.next());
            detailText += attr.encode() + "\n";
        }

        detailText += "</StatusDetail>";

        try {
            detailRoot = textToNode(detailText);
        } catch (ParsingException pe) {
            // really, this should never happen, since we just made sure that
            // we're working with valid text, but it's possible that encoding
            // the attribute could have caused problems...
            throw new IllegalArgumentException("invalid Attribute data");
        }
    }

    /**
     * Constructor that takes the text-encoded form of the XML to use as
     * the status data. The encoded text will be wrapped with the
     * <code>StatusDetail</code> XML tag, and the resulting text must
     * be valid XML or a <code>ParsingException</code> will be thrown.
     *
     * @param encoded a non-null <code>String</code> that encodes the
     *                status detail
     *
     * @throws ParsingException if the encoded text is invalid XML
     */
    public StatusDetail(String encoded) throws ParsingException {
        detailText = "<StatusDetail>\n" + encoded + "\n</StatusDetail>";
        detailRoot = textToNode(detailText);
    }
    
    /**
     * Private constructor that just sets the root node. This interface
     * is provided publically through the getInstance method.
     */
    private StatusDetail(Node root) {
        detailRoot = root;
    }

    /**
     * Private helper routine that converts text into a node
     */
    private Node textToNode(String encoded) throws ParsingException {
        try {
            String text = "<?xml version=\"1.0\"?>\n";
            byte [] bytes = (text + encoded).getBytes();

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(bytes));

            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new ParsingException("invalid XML for status detail");
        }
    }
    
    /**
     * Creates an instance of a <code>StatusDetail</code> object based on
     * the given DOM root node. The node must be a valid StatusDetailType
     * root, or else a <code>ParsingException</code> is thrown.
     *
     * @param root the DOM root of the StatusDetailType XML type
     *
     * @return a new <code>StatusDetail</code> object
     *
     * @throws ParsingException if the root node is invalid
     */
    public static StatusDetail getInstance(Node root) throws ParsingException {
        // check that it's really a StatusDetailType root
        if (! root.getNodeName().equals("StatusDetail"))
            throw new ParsingException("not a StatusDetail node");

        return new StatusDetail(root);
    }

    /**
     * Returns the StatusDetailType DOM root node. This may contain within
     * it any type of valid XML data, and it is up to the application writer
     * to handle the data accordingly. One common use of status data is to
     * include <code>Attribute</code>s, which can be created from their
     * root DOM nodes using their <code>getInstance</code> method.
     *
     * @return the DOM root for the StatusDetailType XML type
     */
    public Node getDetail() {
        return detailRoot;
    }

    /**
     * Returns the text-encoded version of this data, if possible. If the
     * <code>String</code> form constructor was used, this will just be the
     * original text wrapped with the StatusData tag. If the <code>List</code>
     * form constructor was used, it will be the encoded attribute data.
     * If this was created using the <code>getInstance</code> method, then
     * <code>getEncoded</code> will throw an exception.
     *
     * @return the encoded form of this data
     *
     * @throws IllegalStateException if this object was created using the
     *                               <code>getInstance</code> method
     */
    public String getEncoded() throws IllegalStateException {
        if (detailText == null)
            throw new IllegalStateException("no encoded form available");
        
        return detailText;
    }

    @Override
    public String toString() {
        String msg = "";
        if (detailText != null) {
            msg += "StatusDetail: " + detailText;
        }

        return msg;
    }
}
