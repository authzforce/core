/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
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

	private final Status status;

	// initialized non-null
	private final PepActions pepActions;

	// initialized non-null
	private final List<JAXBElement<IdReferenceType>> applicablePolicyIdList;

	/**
	 * Instantiates a generic Decision result
	 * 
	 * @param decision
	 *            decision
	 * @param status
	 *            status
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 * @param policyIdentifierList
	 *            list of matched policy identifiers
	 */
	public BaseDecisionResult(DecisionType decision, Status status, PepActions pepActions, List<JAXBElement<IdReferenceType>> policyIdentifierList)
	{
		if (decision == null)
		{
			throw ILLEGAL_DECISION_ARGUMENT_EXCEPTION;
		}

		this.decision = decision;
		this.status = status;
		this.pepActions = pepActions == null ? new BasePepActions(null, null) : pepActions;
		this.applicablePolicyIdList = policyIdentifierList == null ? new ArrayList<JAXBElement<IdReferenceType>>() : policyIdentifierList;

	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 * 
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public BaseDecisionResult(Status status)
	{
		this(DecisionType.INDETERMINATE, status, null, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See {@link #BaseDecisionResult(Status)} for Indeterminate, and
	 * {@link #NOT_APPLICABLE} for NotApplicable.
	 * 
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public BaseDecisionResult(DecisionType decision, PepActions pepActions)
	{
		this(decision, null, pepActions, null);
	}

	private transient volatile int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.decision, this.status, this.pepActions, this.applicablePolicyIdList);
		}

		return hashCode;
	}

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
		if (!this.pepActions.equals(other.getPepActions()))
		{
			return false;
		}

		// applicablePolicyIdList never null
		if (!this.applicablePolicyIdList.equals(other.getApplicablePolicyIdList()))
		{
			return false;
		}

		return true;
	}

	/**
	 * Get identifiers of policies found applicable for the decision request
	 * 
	 * @return identifiers of policies found applicable for the decision request
	 */
	@Override
	public List<JAXBElement<IdReferenceType>> getApplicablePolicyIdList()
	{
		return this.applicablePolicyIdList;
	}

	/**
	 * Get XACML Decision
	 * 
	 * @return decision
	 */
	@Override
	public DecisionType getDecision()
	{
		return this.decision;
	}

	/**
	 * Get PEP actions (Obligations/Advices)
	 * 
	 * @return PEP actions
	 */
	@Override
	public PepActions getPepActions()
	{
		return this.pepActions;
	}

	/**
	 * Status code/message/detail
	 * 
	 * @return status
	 */
	@Override
	public Status getStatus()
	{
		return this.status;
	}

	/**
	 * Merge extra PEP actions and/or matched policy identifiers. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param newPepActions
	 *            new PEP actions
	 * @param newMatchedPolicyIdList
	 *            new matched policy identifiers
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

	@Override
	public String toString()
	{
		return "Result [decision=" + decision + ", status=" + status + ", pepActions=" + pepActions + ", applicablePolicyIdList=" + applicablePolicyIdList
				+ "]";
	}

}
