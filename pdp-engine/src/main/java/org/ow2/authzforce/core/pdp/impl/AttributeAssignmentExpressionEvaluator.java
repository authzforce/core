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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.PrimitiveValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * XACML AttributeAssignmentExpression evaluator
 *
 * @version $Id: $
 */
public final class AttributeAssignmentExpressionEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeAssignmentExpressionEvaluator.class);

	private final Expression<?> evaluatableExpression;

	private final String attributeId;

	private final String category;

	private final String issuer;

	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * Instantiates evaluatable AttributeAssignment expression evaluator from XACML-Schema-derived JAXB {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression}
	 *
	 * @param jaxbAttrAssignExp
	 *            XACML-schema-derived JAXB AttributeAssignmentExpression
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            expression factory for parsing the AttributeAssignmentExpression's expression
	 * @throws java.lang.IllegalArgumentException
	 *             invalid AttributeAssignmentExpression's Expression
	 */
	public AttributeAssignmentExpressionEvaluator(final AttributeAssignmentExpression jaxbAttrAssignExp, final XPathCompiler xPathCompiler, final ExpressionFactory expFactory)
			throws IllegalArgumentException
	{
		/*
		 * Cannot used AttributeFQN class to handle metadata because AttributeAssignment Category is not required like in AttributeDesignator which is what the AttributeFQN is used for
		 */
		this.attributeId = Preconditions.checkNotNull(jaxbAttrAssignExp.getAttributeId(), "Undefined AttributeAssignment/AttributeId");
		this.category = jaxbAttrAssignExp.getCategory();
		this.issuer = jaxbAttrAssignExp.getIssuer();
		this.evaluatableExpression = expFactory.getInstance(jaxbAttrAssignExp.getExpression().getValue(), xPathCompiler, null);
	}

	private AttributeAssignment newAttributeAssignment(final AttributeValue attrVal)
	{
		return new AttributeAssignment(attrVal.getContent(), attrVal.getDataType(), attrVal.getOtherAttributes(), this.attributeId, this.category, this.issuer);
	}

	/**
	 * Evaluates to AttributeAssignments Section 5.39 and 5.40 of XACML 3.0 core spec: If an AttributeAssignmentExpression evaluates to an atomic attribute value, then there MUST be one resulting
	 * AttributeAssignment which MUST contain this single attribute value. If the AttributeAssignmentExpression evaluates to a bag, then there MUST be a resulting AttributeAssignment for each of the
	 * values in the bag. If the bag is empty, there shall be no AttributeAssignment from this AttributeAssignmentExpression
	 *
	 * @param context
	 *            evaluation context
	 * @return non-null AttributeAssignments; empty if no AttributeValue resulting from evaluation of the Expression
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if evaluation of the Expression in this context fails (Indeterminate)
	 */
	public List<AttributeAssignment> evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		final Value result = this.evaluatableExpression.evaluate(context);
		LOGGER.debug("{}/Expression -> {}", this, result);

		final List<AttributeAssignment> attrAssignList;
		if (result instanceof Bag)
		{
			// result is a bag
			final Bag<?> bag = (Bag<?>) result;
			attrAssignList = new ArrayList<>(bag.size());
			/*
			 * Bag may be empty, in particular if AttributeDesignator/AttributeSelector with MustBePresent=False evaluates to empty bag. Sections 5.30/5.40 of XACML core spec says:
			 * "If the bag is empty, there shall be no <AttributeAssignment> from this <AttributeAssignmentExpression>."
			 */
			for (final PrimitiveValue attrVal : bag)
			{
				if (!(attrVal instanceof AttributeValue))
				{
					LOGGER.error("Error evaluating {}: one of the elements in the result bag is not an AttributeValue");
					throw new IndeterminateEvaluationException("Error evaluating " + this, XacmlStatusCode.PROCESSING_ERROR.value());
				}

				attrAssignList.add(newAttributeAssignment((AttributeValue) attrVal));
			}
		}
		else if (result instanceof AttributeValue)
		{
			// atomic (see spec §5.30, 5.40) / primitive attribute value
			attrAssignList = Collections.singletonList(newAttributeAssignment((AttributeValue) result));
		}
		else
		{
			LOGGER.error("Error evaluating {}: the result value is not an AttributeValue");
			throw new IndeterminateEvaluationException("Error evaluating " + this, XacmlStatusCode.PROCESSING_ERROR.value());
		}

		return attrAssignList;
	}

	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "AttributeAssignmentExpression [attributeId=" + attributeId + ", category=" + category + ", issuer=" + issuer + "]";
		}
		return toString;
	}

	// public static void main(String[] args) throws JAXBException
	// {
	// THIS WILL FAIL: com.sun.istack.internal.SAXException2: unable to marshal type "java.lang.Double" as an element
	// because it is missing an
	// @XmlRootElement annotation; but it succeeds with java.lang.String
	// final AttributeAssignment attrAssignment = new AttributeAssignment(Collections.<Serializable>
	// singletonList("1.0"), "mytype", null, "myattribute1",
	// "mycategory", null);
	//
	// Marshaller marshaller = XACMLBindingUtils.createXacml3Marshaller();
	// marshaller.marshal(attrAssignment, System.out);
	// }

}
