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
package org.ow2.authzforce.core.pdp.testutil.ext;

import net.sf.saxon.s9api.XPathCompiler;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BaseAttributeValueFactory;
import org.ow2.authzforce.core.pdp.io.xacml.json.SerializableJSONObject;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CustomJsonObjectBasedAttributeValue implements AttributeValue
{

    /**
     * Datatype
     */
    public static final AttributeDatatype<CustomJsonObjectBasedAttributeValue> DATATYPE = new AttributeDatatype<>(CustomJsonObjectBasedAttributeValue.class,
            "urn:ow2:authzforce:feature:pdp:data-type:test-custom-from-json-object", "urn:ow2:authzforce:feature:pdp:function:test-custom-from-json-object");

    private final JSONObject json;
    private transient volatile List<Serializable> content = null;

    public CustomJsonObjectBasedAttributeValue(JSONObject json)
    {
        this.json = json;
    }

    @Override
    public List<Serializable> getContent()
    {
        if(content == null) {
            content = Collections.singletonList(new SerializableJSONObject(json));
        }

        return content;
    }

    @Override
    public Map<QName, String> getXmlAttributes()
    {
        return Collections.emptyMap();
    }

    public static final class Factory extends BaseAttributeValueFactory<CustomJsonObjectBasedAttributeValue> {

        private static final IllegalArgumentException UNDEFINED_CONTENT_ARG_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': null.");
        private static final IllegalArgumentException INVALID_CONTENT_ARG_EXCEPTION = new IllegalArgumentException("Invalid content for datatype '" + DATATYPE + "': not a JSON object");
        private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException(
                "Invalid content for datatype '" + DATATYPE + "': extra XML attributes are not supported by this datatype, only JSON object.");

        /**
         * Attribute value factory constructor
         *
         */
        public Factory()
        {
            super(DATATYPE);
        }

        @Override
        public CustomJsonObjectBasedAttributeValue getInstance(List<Serializable> content, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
        {
            if (content == null || content.isEmpty())
            {
                throw UNDEFINED_CONTENT_ARG_EXCEPTION;
            }

            if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
            {
                throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
            }

            final Serializable content0 = content.get(0);
            if(!(content0 instanceof SerializableJSONObject)) {
                throw INVALID_CONTENT_ARG_EXCEPTION;
            }

            final JSONObject json = ((SerializableJSONObject) content0).get();
            return new CustomJsonObjectBasedAttributeValue(json);
        }
    }


}
