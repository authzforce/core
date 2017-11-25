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
package org.ow2.authzforce.core.pdp.impl.expression;

import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.AttributeDesignatorExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * AttributeDesignator evaluator initialized with and using an {@link AttributeProvider} to retrieve the attribute value not only from the request but also possibly from extra Attribute Provider
 * modules (so-called XACML PIPs) (PDP extensions)
 *
 * @param <AV>
 *            AttributeDesignator evaluation result value's primitive datatype
 * 
 * @version $Id: $
 */
public final class GenericAttributeProviderBasedAttributeDesignatorExpression<AV extends AttributeValue> implements AttributeDesignatorExpression<AV>
{
	private static final IllegalArgumentException NULL_ATTRIBUTE_PROVIDER_EXCEPTION = new IllegalArgumentException("Undefined attribute Provider");

	private final AttributeFqn attrGUID;
	private final BagDatatype<AV> returnType;
	private final boolean mustBePresent;
	private final transient Bag.Validator mustBePresentEnforcer;
	private final transient AttributeProvider attrProvider;
	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;

	// lazy initialization
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public Optional<Bag<AV>> getValue()
	{
		/*
		 * context-dependent, therefore not constant
		 */
		return Optional.empty();
	}

	/**
	 * Return an instance of an AttributeDesignator based on an AttributeDesignatorType
	 *
	 * @param attrDesignator
	 *            the AttributeDesignatorType we want to convert
	 * @param resultDatatype
	 *            expected datatype of the result of evaluating this AttributeDesignator ( {@code AV is the expected type of every element in the bag})
	 * @param attrProvider
	 *            Attribute Provider responsible for finding the attribute designated by this in a given evaluation context at runtime
	 * @throws IllegalArgumentException
	 *             if {@code attrDesignator.getCategory() == null || attrDesignator.getAttributeId() == null}
	 */
	public GenericAttributeProviderBasedAttributeDesignatorExpression(final AttributeDesignatorType attrDesignator, final BagDatatype<AV> resultDatatype, final AttributeProvider attrProvider)
	{
		if (attrProvider == null)
		{
			throw NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
		}

		this.attrProvider = attrProvider;
		this.attrGUID = AttributeFqns.newInstance(attrDesignator);
		this.returnType = resultDatatype;

		// error messages/exceptions
		final String missingAttributeMessage = this + " not found in context";
		this.mustBePresent = attrDesignator.isMustBePresent();
		this.mustBePresentEnforcer = mustBePresent ? new Bags.NonEmptinessValidator(missingAttributeMessage) : Bags.DUMB_VALIDATOR;

		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(missingAttributeMessage + " for unknown reason", XacmlStatusCode.MISSING_ATTRIBUTE.value());
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attrGUID
				+ "' because request context undefined", XacmlStatusCode.MISSING_ATTRIBUTE.value());
	}

	@Override
	public AttributeFqn getAttributeFQN()
	{
		return this.attrGUID;
	}

	@Override
	public boolean isNonEmptyBagRequired()
	{
		return this.mustBePresent;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the pre-assigned meta-data against the given context, trying to find some matching values.
	 */
	@Override
	public Bag<AV> evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> bag = attrProvider.get(attrGUID, this.returnType, context);
		if (bag == null)
		{
			throw this.missingAttributeForUnknownReasonException;
		}

		mustBePresentEnforcer.validate(bag);

		/*
		 * if we got here the bag wasn't empty, or mustBePresent was false, so we just return the result
		 */
		return bag;
	}

	/** {@inheritDoc} */
	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "AttributeDesignator [" + this.attrGUID + ", dataType= " + this.returnType.getElementType() + ", mustBePresent= "
					+ (mustBePresentEnforcer == Bags.DUMB_VALIDATOR ? "false" : "true") + "]";
		}

		return toString;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.attrGUID.hashCode();
		}

		return hashCode;
	}

	/** Equal iff the Attribute Category/Issuer/Id are equal */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof GenericAttributeProviderBasedAttributeDesignatorExpression))
		{
			return false;
		}

		final GenericAttributeProviderBasedAttributeDesignatorExpression<?> other = (GenericAttributeProviderBasedAttributeDesignatorExpression<?>) obj;
		return this.attrGUID.equals(other.attrGUID);
	}

}
