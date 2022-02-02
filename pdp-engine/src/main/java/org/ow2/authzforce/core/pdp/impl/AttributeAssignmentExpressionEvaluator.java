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
package org.ow2.authzforce.core.pdp.impl;

import com.google.common.base.Preconditions;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActionAttributeAssignment;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * XACML AttributeAssignmentExpression evaluator
 *
 * @version $Id: $
 */
public final class AttributeAssignmentExpressionEvaluator
{
	private static abstract class AttributeValueExpression<AV extends AttributeValue>
	{
		private final Datatype<AV> datatype;

		private AttributeValueExpression(Datatype<AV> attributeDatatype) {
			this.datatype = attributeDatatype;
		}

		protected abstract Collection<AV> evaluate(final EvaluationContext individualDecisionCtx, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;
	}

	private static final class SingleAttributeValueExpression<AV extends AttributeValue> extends AttributeValueExpression<AV>
	{

		private final Expression<AV> valueExpr;

		private SingleAttributeValueExpression(Expression<AV> valueExpression)
		{
			super(valueExpression.getReturnType());
			this.valueExpr = valueExpression;
		}

		@Override
		protected Collection<AV> evaluate(EvaluationContext individualDecisionCtx, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
		{
			// atomic (see spec §5.30, 5.40) / primitive attribute value
			return Collections.singleton(valueExpr.evaluate(individualDecisionCtx, mdpContext));
		}

	}

	private static final class AttributeBagExpression<AV extends AttributeValue> extends AttributeValueExpression<AV>
	{

		private final Expression<? extends Bag<AV>> valueExpr;

		private <B extends Bag<AV>> AttributeBagExpression(Expression<B> valueExpression)
		{
			super((Datatype<AV>) valueExpression.getReturnType().getTypeParameter().get());
			this.valueExpr = valueExpression;
		}

		@Override
		protected Collection<AV> evaluate(EvaluationContext ctx, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
		{
			return valueExpr.evaluate(ctx, mdpContext).elements();
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeAssignmentExpressionEvaluator.class);

	private final String attributeId;

	private final Optional<String> category;

	private final Optional<String> issuer;

	private final AttributeValueExpression<?> attValExpr;

	private transient final String toString;

	/**
	 * Instantiates evaluable AttributeAssignment expression evaluator from XACML-Schema-derived JAXB {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression}
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
		 * Cannot use AttributeFQN class to handle metadata because AttributeAssignment Category is not required like in AttributeDesignator which is what the AttributeFQN is used for
		 */
		this.attributeId = jaxbAttrAssignExp.getAttributeId();
		Preconditions.checkArgument(attributeId != null, "Undefined AttributeAssignment/AttributeId");
		this.category = Optional.ofNullable(jaxbAttrAssignExp.getCategory());
		this.issuer = Optional.ofNullable(jaxbAttrAssignExp.getIssuer());
		this.toString = "AttributeAssignmentExpression [attributeId=" + attributeId + ", category=" + category.orElse(null) + ", issuer=" + issuer.orElse(null) + "]";

		final JAXBElement<? extends ExpressionType> xacmlExpr = jaxbAttrAssignExp.getExpression();
		Preconditions.checkArgument(xacmlExpr != null, "Undefined AttributeAssignment/Expression");
		final Expression<?> evaluableExpression = expFactory.getInstance(xacmlExpr.getValue(), xPathCompiler, null);

		/*
		 * As stated in section 5.41 of XACML spec, the expression must evaluate to a constant attribute value or a bag of zero or more attribute values.
		 */
		final Datatype<?> expReturnType = evaluableExpression.getReturnType();
		final Optional<? extends Datatype<?>> expReturnTypeParam = expReturnType.getTypeParameter();
		if (expReturnTypeParam.isPresent())
		{
			/*
			 * ExpReturnTypeParam is generic. Make sure it is a bag of AttributeValues
			 */
			final Datatype<?> nonNullTypeParam = expReturnTypeParam.get();
			/*
			 * Make sure typeParam is not itself generic like a bag
			 */
			if (nonNullTypeParam.getTypeParameter().isPresent() || nonNullTypeParam == StandardDatatypes.FUNCTION)
			{
				throw new IllegalArgumentException(
				        "Invalid " + toString + ": invalid Expression's return type (" + expReturnType + ")'. Expected: AttributeValue or bag (of AttributeValues) datatype.");
			}

			/*
			 * So we assume that if type parameter is not Function or generic (bag), it is AttributeValue subtype and expReturnType is Bag<?> datatype. (This is not formally guaranteed :-( but can we
			 * do better?)
			 */
			this.attValExpr = new AttributeBagExpression<>((Expression<Bag>) evaluableExpression);
		} else
		{
			/*
			 * expReturnType assumed primitive (Function or AttributeValue a priori)
			 */
			if (expReturnType == StandardDatatypes.FUNCTION)
			{
				throw new IllegalArgumentException(
				        "Invalid " + toString + ": invalid Expression's return type (" + expReturnType + ")'. Expected: AttributeValue or bag (of AttributeValues) datatype.");
			}

			/*
			 * So we assume that if expReturnType is not Function, it is AttributeValue subtype. (This is not formally guaranteed :-( but can we do better?)
			 */
			this.attValExpr = new SingleAttributeValueExpression<>((Expression<? extends AttributeValue>) evaluableExpression);
		}

	}

	private <AV extends AttributeValue> PepActionAttributeAssignment<AV> newAttributeAssignment(Datatype<AV> datatype, final AV attrVal)
	{
		return new PepActionAttributeAssignment<>(this.attributeId, this.category, this.issuer, datatype, attrVal);
	}

	private <AV extends AttributeValue> List<PepActionAttributeAssignment<?>> newAttributeAssignments(final AttributeValueExpression<AV> expression, final EvaluationContext individualDecisionContext, final Optional<EvaluationContext> mdpContext)
	        throws IndeterminateEvaluationException
	{
		final Collection<AV> vals = expression.evaluate(individualDecisionContext, mdpContext);
		LOGGER.debug("{}/Expression -> {}", this, vals);

		/*
		 * vals may be empty bag, in particular if AttributeDesignator/AttributeSelector with MustBePresent=False evaluates to empty bag. Sections 5.30/5.40 of XACML core spec says:
		 * "If the bag is empty, there shall be no <AttributeAssignment> from this <AttributeAssignmentExpression>."
		 */
		return vals.stream().map(av -> newAttributeAssignment(expression.datatype, av)).collect(Collectors.toList());
	}

	/**
	 * Evaluates to AttributeAssignments Section 5.39 and 5.40 of XACML 3.0 core spec: If an AttributeAssignmentExpression evaluates to an atomic attribute value, then there MUST be one resulting
	 * AttributeAssignment which MUST contain this single attribute value. If the AttributeAssignmentExpression evaluates to a bag, then there MUST be a resulting AttributeAssignment for each of the
	 * values in the bag. If the bag is empty, there shall be no AttributeAssignment from this AttributeAssignmentExpression
	 *
	 * @param context
	 *            Individual Decision evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used.
	 * @return non-null AttributeAssignments; empty if no AttributeValue resulting from evaluation of the Expression
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if evaluation of the Expression in this context fails (Indeterminate)
	 */
	public Collection<PepActionAttributeAssignment<?>> evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		return newAttributeAssignments(this.attValExpr, context, mdpContext);
	}

	@Override
	public String toString()
	{
		return toString;
	}

}
