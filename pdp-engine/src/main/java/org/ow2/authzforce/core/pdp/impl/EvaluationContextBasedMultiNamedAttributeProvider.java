/*
 * Copyright 2012-2024 THALES.
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
import com.google.common.collect.ImmutableSet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.NamedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;
import java.util.Set;

/**
 * AttributeProvider that provides one or multiple attributes as follows: tries to find it in the current request context first, else delegates to other Attribute Provider.
 * Used for instance as dependency attribute provider to find attributes required by a given Attribute Provider.
 *
 * @version $Id: $
 */
public class EvaluationContextBasedMultiNamedAttributeProvider extends EvaluationContextBasedNamedAttributeProvider implements NamedAttributeProvider
{
	/**
	 * All implementations of this interface must be immutable
	 */
	@FunctionalInterface
	@Immutable
	interface DelegateSupplier {
		<AV extends AttributeValue> DelegateAttributeProvider<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype) throws IndeterminateEvaluationException;
	}

	private final ImmutableSet<AttributeDesignatorType> providedAttributes;
	private final DelegateSupplier delegateSupplier;

	/**
     * Creates new instance for given provided attribute and delegate attribute provider to be called if not found in evaluation context
	 * @param providedAttributes provided attributes
	 * @param strictAttributeIssuerMatch whether to apply strict match on the attribute Issuer
	 * @param supplierOfDelegateAttributeProvider supplier delegated attribute provider
	 */
	public EvaluationContextBasedMultiNamedAttributeProvider(final ImmutableSet<AttributeDesignatorType> providedAttributes, final boolean strictAttributeIssuerMatch, final DelegateSupplier supplierOfDelegateAttributeProvider)
	{
		super(strictAttributeIssuerMatch);
		Preconditions.checkArgument(providedAttributes != null && !providedAttributes.isEmpty() && supplierOfDelegateAttributeProvider != null, "Invalid arguments");
		this.providedAttributes = providedAttributes;
		this.delegateSupplier = supplierOfDelegateAttributeProvider;
	}

	@Override
	public final Set<AttributeDesignatorType> getProvidedAttributes()
	{
		return this.providedAttributes;
	}

	@Override
	public final <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFqn, final Datatype<AV> datatype, final EvaluationContext evaluationContext, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		return get(attributeFqn, datatype, evaluationContext, mdpContext, delegateSupplier.get(attributeFqn, datatype));
	}
}
