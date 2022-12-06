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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MissingAttributeDetail;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Custom function used for testing resolution of issue: <a href="https://github.com/authzforce/core/issues/69">GitHub issue #69</a>
 */
public class TestExtensibleSimpleValueEqualFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, TestExtensibleSimpleValue>
{
        public static final String ID = TestExtensibleSimpleValue.DATATYPE.getFunctionIdPrefix() +"equal";

        public TestExtensibleSimpleValueEqualFunction() {
            super(ID, StandardDatatypes.BOOLEAN, false, List.of(TestExtensibleSimpleValue.DATATYPE, TestExtensibleSimpleValue.DATATYPE));
        }

    @Override
    public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) {

        return new BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<>(functionSignature, argExpressions, remainingArgTypes)
        {

            @Override
            protected BooleanValue evaluate(final Deque<TestExtensibleSimpleValue> args) throws IndeterminateEvaluationException
            {
                /*
                 Number of arguments is already known and checked based on the list of parameter types passed to the super constructor in #TestExtensibleSimpleValueEqualFunction()                */
                final TestExtensibleSimpleValue arg1 = args.poll();
                final TestExtensibleSimpleValue arg2 = args.poll();
                assert arg1 != null && arg2 != null;
                final String arg1XmlAttVal = arg1.getRequiredXmlAttributeValue();
                if (arg1XmlAttVal == null || !arg1XmlAttVal.equals(arg2.getRequiredXmlAttributeValue()))
                {
                    final String categoryId = arg2.getXmlAttributes().get(CustomTestRequestPreprocessorFactory.XACML_CATEGORY_ID_QNAME);
                    final String attributeId = arg2.getXmlAttributes().get(CustomTestRequestPreprocessorFactory.XACML_ATTRIBUTE_ID_QNAME);
                    /*
                     Do not include the Category/AttributeId XML attributes injected by the custom Request Preprocessor in the MissingAttributeDetail/AttributeValue since they will be included as XML attributes of <MissingAttributeDetail> element.
                     */
                    final AttributeValueType expectedValue = new AttributeValueType(List.of(""), TestExtensibleSimpleValue.DATATYPE.getId(), arg1XmlAttVal == null? Map.of(): Map.of(TestExtensibleSimpleValue.REQUIRED_XML_ATTRIBUTE_QNAME, arg1XmlAttVal));
                    final MissingAttributeDetail detail = new MissingAttributeDetail(List.of(expectedValue), categoryId, attributeId, TestExtensibleSimpleValue.DATATYPE.getId(), null);
                    throw new IndeterminateEvaluationException("Function " + ID + " expects same SRS for both geometry parameters", detail, Optional.of("urn:ogc:def:function:geoxacml:3.0:crs-error"));
                }

                return BooleanValue.valueOf(arg1.equals(arg2));
            }

        };
    }

}
