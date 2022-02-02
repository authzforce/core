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
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.NamedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.SingleNamedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

import java.util.Optional;

/**
 * AttributeProvider that provides one and only one attribute as follows: tries to find it in the current request context first, else delegates to other {@link NamedAttributeProvider}(s).
 * Used for instance to resolve an AttributeDesignator or AttributeSelector's ContextSelectorId.
 *
 * @version $Id: $
 */
public class EvaluationContextBasedSingleNamedAttributeProvider<AV extends AttributeValue> extends EvaluationContextBasedNamedAttributeProvider implements SingleNamedAttributeProvider<AV>
{
	private final AttributeFqn attName;
	private final Datatype<AV> attType;
	private final DelegateAttributeProvider<AV> delegate;
	private final AttributeDesignatorType jaxbAttDes;

	/**
	 * Creates new instance for given provided attribute and delegate attribute provider to be called if not found in evaluation context
	 * @param attributeName provided attribute name
	 * @param attributeDatatype provided attribute data-type
	 * @param strictAttributeIssuerMatch whether to apply strict match on the attribute Issuer
	 * @param delegate delegated attribute provider
	 */
	public EvaluationContextBasedSingleNamedAttributeProvider(final AttributeFqn attributeName, final Datatype<AV> attributeDatatype, final boolean strictAttributeIssuerMatch, final DelegateAttributeProvider<AV> delegate)
	{
		super(strictAttributeIssuerMatch);
		Preconditions.checkArgument(attributeName != null && attributeDatatype != null && delegate != null, "Invalid arguments");
		this.attName = attributeName;
		this.attType = attributeDatatype;
		this.jaxbAttDes = new AttributeDesignatorType(attName.getCategory(),  attName.getId(), attributeDatatype.getId(), attName.getIssuer().orElse(null), false);
		this.delegate = delegate;
	}

	@Override
	public final AttributeDesignatorType getProvidedAttribute()
	{
		return this.jaxbAttDes;
	}

	/** {@inheritDoc} */
	@Override
	public final AttributeBag<AV> get(final EvaluationContext context,  final Optional<EvaluationContext> mdpContext)
	{
		return get(this.attName, this.attType, context, mdpContext, delegate);
	}

}
