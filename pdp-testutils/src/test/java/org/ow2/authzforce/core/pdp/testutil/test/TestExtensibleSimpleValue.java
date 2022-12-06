/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.testutil.test;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * For testing purposes, SimpleValue that can have any extra XML attributes
 */
public final class TestExtensibleSimpleValue extends SimpleValue<String>
{
    public static final AttributeDatatype<TestExtensibleSimpleValue> DATATYPE = new AttributeDatatype<>(TestExtensibleSimpleValue.class,
            "urn:ow2:authzforce:feature:pdp:data-type:test-extensible-value", "urn:ow2:authzforce:feature:pdp:function:test-extensible-value:", ItemType.ELEMENT_NODE);

    public static final QName REQUIRED_XML_ATTRIBUTE_QNAME = new QName("http://authzforce.github.io/core/xmlns/test/3", "some-required-xml-attribute");

    private transient volatile int hashCode = 0; // Effective Java - Item 9
    private final transient ImmutableMap<QName, String> extraXmlAtts;

    private transient volatile XdmNode xdmValue;

    private TestExtensibleSimpleValue(final String value, final Map<QName, String> otherXmlAttributes) throws IllegalArgumentException
    {
        super(value);
        this.extraXmlAtts = ImmutableMap.copyOf(otherXmlAttributes);
    }

    public String getRequiredXmlAttributeValue()
    {
        return this.extraXmlAtts.get(REQUIRED_XML_ATTRIBUTE_QNAME);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        if (hashCode == 0)
        {
            // hash regardless of letter case
            hashCode = Objects.hash(value, extraXmlAtts);
        }

        return hashCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        // Effective Java - Item 8
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof TestExtensibleSimpleValue))
        {
            return false;
        }

        final TestExtensibleSimpleValue other = (TestExtensibleSimpleValue) obj;
        return this.value.equals(other.value) && this.extraXmlAtts.equals(other.extraXmlAtts);
    }

    /** {@inheritDoc} */
    @Override
    public String printXML()
    {
        return this.value;
    }

    @Override
    public Map<QName, String> getXmlAttributes()
    {
        return this.extraXmlAtts;
    }

    @SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
    @Override
    public XdmItem getXdmItem()
    {
        if(xdmValue == null ) {
            final ElementImpl node = new ElementImpl();
            this.extraXmlAtts.forEach((qName, s) -> node.addAttribute(new FingerprintedQName(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart()), BuiltInAtomicType.STRING, s, 0, true));
            node.replaceStringValue(value);
            xdmValue = new XdmNode(node);
        }
        return xdmValue;
    }

    public static final class Factory extends SimpleValue.BaseFactory<TestExtensibleSimpleValue>
    {
        private static final Set<Class<? extends Serializable>> SUPPORTED_INPUT_CONTENT_TYPES = HashCollections.newImmutableSet(String.class);

        public Factory()
        {
            super(DATATYPE);
        }

        @Override
        public Set<Class<? extends Serializable>> getSupportedInputTypes()
        {
            return SUPPORTED_INPUT_CONTENT_TYPES;
        }

        @Override
        public TestExtensibleSimpleValue getInstance(final Serializable value, final Map<QName, String> otherXmlAttributes, final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
        {
            if (!(value instanceof String))
            {
                throw new IllegalArgumentException("Invalid AttributeValueType: content contains instance of " + value.getClass().getName() + ". Expected: " + String.class);
            }

            if(otherXmlAttributes == null || !otherXmlAttributes.containsKey(REQUIRED_XML_ATTRIBUTE_QNAME)) {
                throw new IllegalArgumentException("Invalid AttributeValueType: missing required XML attribute: " + REQUIRED_XML_ATTRIBUTE_QNAME);
            }
            return new TestExtensibleSimpleValue((String) value, otherXmlAttributes);
        }
    }
}
