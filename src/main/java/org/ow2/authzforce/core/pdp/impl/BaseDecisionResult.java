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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.PepActions;

/**
 * Base implementation of DecisionResult
 *
 * @version $Id: $
 */
public final class BaseDecisionResult implements DecisionResult
{
	private static final IllegalArgumentException ILLEGAL_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Decision");

	/**
	 * NotApplicable decision result
	 */
	public static final DecisionResult NOT_APPLICABLE = new BaseDecisionResult(DecisionType.NOT_APPLICABLE, null);

	/**
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision and nothing else.
	 */
	public static final DecisionResult DENY = new BaseDecisionResult(DecisionType.DENY, null);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit decision and nothing else.
	 */
	public static final DecisionResult PERMIT = new BaseDecisionResult(DecisionType.PERMIT, null);

	private final DecisionType decision;

	/**
	 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0 core: <i>potential effect value which could have occurred if there would not have been an error causing the
	 * “Indeterminate”</i>. We use the following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>Null means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 */
	private final DecisionType extIndeterminate;

	private final Status status;

	// initialized non-null
	private final PepActions pepActions;

	// initialized non-null
	private final List<JAXBElement<IdReferenceType>> applicablePolicyIdList;

	private BaseDecisionResult(DecisionType decision, DecisionType extendedIndeterminate, Status status, PepActions pepActions, List<JAXBElement<IdReferenceType>> policyIdentifierList)
	{
		if (decision == null)
		{
			throw ILLEGAL_DECISION_ARGUMENT_EXCEPTION;
		}

		this.decision = decision;
		this.extIndeterminate = extendedIndeterminate;
		this.status = status;
		this.pepActions = pepActions == null ? new BasePepActions(null, null) : pepActions;
		this.applicablePolicyIdList = policyIdentifierList == null ? new ArrayList<JAXBElement<IdReferenceType>>() : policyIdentifierList;

	}

	/**
	 * Instantiates a generic Decision result
	 *
	 * @param extendedIndeterminate
	 *            Extended Indeterminate value (XACML 3.0 Core, section 7.10). We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param status
	 *            status
	 * @param policyIdentifierList
	 *            list of matched policy identifiers
	 */
	public BaseDecisionResult(Status status, DecisionType extendedIndeterminate, List<JAXBElement<IdReferenceType>> policyIdentifierList)
	{
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null, policyIdentifierList);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 *
	 * @param extendedIndeterminate
	 *            Extended Indeterminate value (XACML 3.0 Core, section 7.10). We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public BaseDecisionResult(Status status, DecisionType extendedIndeterminate)
	{
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null, null);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status and extended Indeterminate set to Indeterminate{DP}
	 *
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public BaseDecisionResult(Status status)
	{
		this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See {@link #BaseDecisionResult(Status, DecisionType)} for Indeterminate, and {@link #NOT_APPLICABLE} for NotApplicable.
	 *
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public BaseDecisionResult(DecisionType decision, PepActions pepActions)
	{
		this(decision, DecisionType.NOT_APPLICABLE, null, pepActions, null);
	}

	/**
	 * Instantiates a decision result reusing the decision, extended Indeterminate and status from a given result
	 * 
	 * @param algResult
	 *            decision result giving the decision, extended Indeterminate result and status to the new instance
	 * @param pepActions
	 *            PEP actions (obligations/advices) to be added to the result
	 * @param applicablePolicyIdList
	 *            list of matched policy identifiers to be added to the result
	 */
	public BaseDecisionResult(DecisionResult algResult, PepActions pepActions, List<JAXBElement<IdReferenceType>> applicablePolicyIdList)
	{
		this(algResult.getDecision(), algResult.getExtendedIndeterminate(), algResult.getStatus(), pepActions, applicablePolicyIdList);
	}

	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.decision, this.extIndeterminate, this.status, this.pepActions, this.applicablePolicyIdList);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof DecisionResult))
		{
			return false;
		}

		final DecisionResult other = (DecisionResult) obj;
		if (this.decision != other.getDecision())
		{
			return false;
		}

		if (this.extIndeterminate != other.getExtendedIndeterminate())
		{
			return false;
		}

		// Status is optional in XACML
		if (this.status == null)
		{
			if (other.getStatus() != null)
			{
				return false;
			}
		} else if (!this.status.equals(other.getStatus()))
		{
			return false;
		}

		// this.getObligations() derived from this.pepActions
		// pepActions never null
		// applicablePolicyIdList never null
		return this.pepActions.equals(other.getPepActions()) && this.applicablePolicyIdList.equals(other.getApplicablePolicyIdList());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get identifiers of policies found applicable for the decision request
	 */
	@Override
	public List<JAXBElement<IdReferenceType>> getApplicablePolicyIdList()
	{
		return this.applicablePolicyIdList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get XACML Decision
	 */
	@Override
	public DecisionType getDecision()
	{
		return this.decision;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get PEP actions (Obligations/Advices)
	 */
	@Override
	public PepActions getPepActions()
	{
		return this.pepActions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Status code/message/detail
	 */
	@Override
	public Status getStatus()
	{
		return this.status;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Merge extra PEP actions and/or matched policy identifiers. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 */
	@Override
	public void merge(PepActions newPepActions, List<JAXBElement<IdReferenceType>> newMatchedPolicyIdList)
	{
		if (newPepActions != null)
		{
			this.pepActions.merge(newPepActions);
		}

		if (newMatchedPolicyIdList != null)
		{
			this.applicablePolicyIdList.addAll(newMatchedPolicyIdList);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "Result [decision=" + decision + ", status=" + status + ", pepActions=" + pepActions + ", applicablePolicyIdList=" + applicablePolicyIdList + "]";
	}

	/** {@inheritDoc} */
	@Override
	public DecisionType getExtendedIndeterminate()
	{
		return this.extIndeterminate;
	}

}
