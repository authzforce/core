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
package org.ow2.authzforce.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...). This is different from the final Result in the Response by the PDP as it does not have the
 * Attributes to be included in the final Result; and Obligations/Advices are packaged together in a {@link PepActions} field.
 * 
 */
public final class PolicyDecisionResult
{
	private static final IllegalArgumentException ILLEGAL_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Decision");

	/**
	 * NotApplicable decision result
	 */
	public static final PolicyDecisionResult NOT_APPLICABLE = new PolicyDecisionResult(DecisionType.NOT_APPLICABLE, null);

	/**
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision and nothing else.
	 */
	public static final PolicyDecisionResult DENY = new PolicyDecisionResult(DecisionType.DENY, null);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit decision and nothing else.
	 */
	public static final PolicyDecisionResult PERMIT = new PolicyDecisionResult(DecisionType.PERMIT, null);

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
	public PolicyDecisionResult(DecisionType decision, Status status, PepActions pepActions, List<JAXBElement<IdReferenceType>> policyIdentifierList)
	{
		if (decision == null)
		{
			throw ILLEGAL_DECISION_ARGUMENT_EXCEPTION;
		}

		this.decision = decision;
		this.status = status;
		this.pepActions = pepActions == null ? new PepActions(null, null) : pepActions;
		this.applicablePolicyIdList = policyIdentifierList == null ? new ArrayList<JAXBElement<IdReferenceType>>() : policyIdentifierList;

	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 * 
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public PolicyDecisionResult(Status status)
	{
		this(DecisionType.INDETERMINATE, status, null, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See {@link #PolicyDecisionResult(Status)} for Indeterminate, and
	 * {@link #NOT_APPLICABLE} for NotApplicable.
	 * 
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public PolicyDecisionResult(DecisionType decision, PepActions pepActions)
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

		if (!(obj instanceof PolicyDecisionResult))
		{
			return false;
		}

		final PolicyDecisionResult other = (PolicyDecisionResult) obj;
		if (this.decision != other.decision)
		{
			return false;
		}

		// Status is optional in XACML
		if (this.status == null)
		{
			if (other.status != null)
			{
				return false;
			}
		} else if (!this.status.equals(other.status))
		{
			return false;
		}

		// this.getObligations() derived from this.pepActions
		// pepActions never null
		if (!this.pepActions.equals(other.pepActions))
		{
			return false;
		}

		// applicablePolicyIdList never null
		if (!this.applicablePolicyIdList.equals(other.applicablePolicyIdList))
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
	public List<JAXBElement<IdReferenceType>> getApplicablePolicyIdList()
	{
		return this.applicablePolicyIdList;
	}

	/**
	 * Get XACML Decision
	 * 
	 * @return decision
	 */
	public DecisionType getDecision()
	{
		return this.decision;
	}

	/**
	 * Get PEP actions (Obligations/Advices)
	 * 
	 * @return PEP actions
	 */
	public PepActions getPepActions()
	{
		return this.pepActions;
	}

	/**
	 * Status code/message/detail
	 * 
	 * @return status
	 */
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
