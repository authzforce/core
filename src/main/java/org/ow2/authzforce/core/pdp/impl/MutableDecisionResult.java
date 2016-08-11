/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.PepActions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Base implementation of DecisionResult
 *
 * @version $Id: $
 */
public final class MutableDecisionResult implements DecisionResult {
	private static final IllegalArgumentException ILLEGAL_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined Decision");

	private final DecisionType decision;

	private final Status status;

	// initialized non-null
	private final MutablePepActions pepActions;

	/**
	 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0
	 * core: <i>potential effect value which could have occurred if there would
	 * not have been an error causing the “Indeterminate”</i>. We use the
	 * following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>Null means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means
	 * the decision is not Indeterminate, and therefore any extended
	 * Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 */
	private final DecisionType extIndeterminate;

	private transient volatile int hashCode = 0;

	private MutableDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate,
			final Status status, final MutablePepActions pepActions, final List<JAXBElement<IdReferenceType>> policyIdList,
			final Set<AttributeGUID> usedNamedAttributes, final Set<AttributeSelectorId> usedExtraAttributeContents) {
		if (decision == null) {
			throw ILLEGAL_DECISION_ARGUMENT_EXCEPTION;
		}

		this.decision = decision;
		this.extIndeterminate = extendedIndeterminate;
		this.status = status;
		this.pepActions = pepActions == null ? new MutablePepActions(null, null) : pepActions;
	}

	/**
	 * Instantiates a generic Decision result
	 *
	 * @param extendedIndeterminate
	 *            Extended Indeterminate value (XACML 3.0 Core, section 7.10).
	 *            We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} means
	 *            "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value
	 *            and means the decision is not Indeterminate, and therefore any
	 *            extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param status
	 *            status
	 * @param policyIdentifierList
	 *            list of matched policy identifiers
	 */
	public MutableDecisionResult(final Status status, final DecisionType extendedIndeterminate,
			final List<JAXBElement<IdReferenceType>> policyIdentifierList) {
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null, policyIdentifierList, null, null);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 *
	 * @param extendedIndeterminate
	 *            Extended Indeterminate value (XACML 3.0 Core, section 7.10).
	 *            We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} means
	 *            "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value
	 *            and means the decision is not Indeterminate, and therefore any
	 *            extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public MutableDecisionResult(final Status status, final DecisionType extendedIndeterminate) {
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null, null, null, null);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 * and extended Indeterminate set to Indeterminate{DP}
	 *
	 * @param status
	 *            reason/code for Indeterminate
	 * @param usedNamedAttributes
	 *            list of identifiers of the named attributes actually used for
	 *            evaluating this decision
	 * @param usedExtraAttributeContents
	 *            extra Attributes/Content(s) actually used for evaluating this
	 *            decision
	 */
	public MutableDecisionResult(final Status status, final Set<AttributeGUID> usedNamedAttributes,
			final Set<AttributeSelectorId> usedExtraAttributeContents) {
		this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null, null, usedNamedAttributes,
				usedExtraAttributeContents);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 * and extended Indeterminate set to Indeterminate{DP}
	 *
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public MutableDecisionResult(final Status status) {
		this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null, null, null, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice.
	 * See {@link #MutableDecisionResult(Status, DecisionType)} for
	 * Indeterminate, and {@link DecisionResults#NOT_APPLICABLE} for
	 * NotApplicable.
	 *
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public MutableDecisionResult(final DecisionType decision, final MutablePepActions pepActions) {
		this(decision, DecisionType.NOT_APPLICABLE, null, pepActions, null, null, null);
	}

	/**
	 * Instantiates a decision result reusing the decision, extended
	 * Indeterminate and status from a given result
	 * 
	 * @param algResult
	 *            decision result giving the decision, extended Indeterminate
	 *            result and status to the new instance
	 * @param pepActions
	 *            PEP actions (obligations/advices) to be added to the result
	 * @param applicablePolicyIdList
	 *            list of matched policy identifiers to be added to the result
	 * @param usedNamedAttributes
	 *            list of identifiers of the named attributes actually used for
	 *            evaluating this decision
	 * @param usedExtraAttributeContents
	 *            extra Attributes/Content(s) actually used for evaluating this
	 *            decision
	 */
	public MutableDecisionResult(final DecisionResult algResult, final MutablePepActions pepActions,
			final List<JAXBElement<IdReferenceType>> applicablePolicyIdList,
			final Set<AttributeGUID> usedNamedAttributes, final Set<AttributeSelectorId> usedExtraAttributeContents) {
		this(algResult.getDecision(), algResult.getExtendedIndeterminate(), algResult.getStatus(), pepActions,
				applicablePolicyIdList, usedNamedAttributes, usedExtraAttributeContents);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = Objects.hash(this.decision, this.extIndeterminate, this.status, this.pepActions);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DecisionResult)) {
			return false;
		}

		final DecisionResult other = (DecisionResult) obj;
		if (this.decision != other.getDecision()) {
			return false;
		}

		if (this.extIndeterminate != other.getExtendedIndeterminate()) {
			return false;
		}

		// Status is optional in XACML
		if (this.status == null) {
			if (other.getStatus() != null) {
				return false;
			}
		} else if (!this.status.equals(other.getStatus())) {
			return false;
		}

		// this.getObligations() derived from this.pepActions
		// pepActions never null
		// applicablePolicyIdList never null
		return this.pepActions.equals(other.getPepActions());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get XACML Decision
	 */
	@Override
	public DecisionType getDecision() {
		return this.decision;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get PEP actions (Obligations/Advices)
	 */
	@Override
	public PepActions getPepActions() {
		return this.pepActions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Status code/message/detail
	 */
	@Override
	public Status getStatus() {
		return this.status;
	}

	/** {@inheritDoc} */
	@Override
	public DecisionType getExtendedIndeterminate() {
		return this.extIndeterminate;
	}
	
	/**
	 * Add PEP actions to this result
	 * @param newPepActions PEP actions to be added
	 */
	public void addPepActions(PepActions newPepActions) {
		this.pepActions.add(pepActions);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Result [decision=" + decision + ", status=" + status + ", pepActions=" + pepActions + "]";
	}

}
