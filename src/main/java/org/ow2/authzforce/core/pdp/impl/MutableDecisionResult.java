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

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.MutablePepActions;
import org.ow2.authzforce.core.pdp.api.PepActions;

/**
 * Base implementation of DecisionResult
 *
 * @version $Id: $
 */
public final class MutableDecisionResult implements DecisionResult
{
	private final DecisionType decision;

	private final Status status;

	// initialized non-null
	private final MutablePepActions pepActions;

	/**
	 * Extended Indeterminate value, only in case {@link #getDecision()} returns {@value DecisionType#INDETERMINATE}, else it should be ignored, as defined in section 7.10 of XACML 3.0 core:
	 * <i>potential effect value which could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 */
	private final DecisionType extIndeterminate;

	private transient volatile int hashCode = 0;

	private MutableDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate, final Status status, final MutablePepActions pepActions)
	{
		assert decision != null && extendedIndeterminate != null;

		this.decision = decision;
		this.extIndeterminate = extendedIndeterminate;
		this.status = status;
		this.pepActions = pepActions == null ? new MutablePepActions() : pepActions;
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
	public MutableDecisionResult(final Status status, final DecisionType extendedIndeterminate)
	{
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status and extended Indeterminate set to Indeterminate{DP}
	 *
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public MutableDecisionResult(final Status status)
	{
		this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See {@link #MutableDecisionResult(Status, DecisionType)} for Indeterminate, and
	 * {@link DecisionResults#SIMPLE_NOT_APPLICABLE} for NotApplicable decision.
	 *
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public MutableDecisionResult(final DecisionType decision, final MutablePepActions pepActions)
	{
		this(decision, DecisionType.NOT_APPLICABLE, null, pepActions);
	}

	/**
	 * Instantiates a decision result reusing the decision, extended Indeterminate and status from a given result
	 * 
	 * @param algResult
	 *            decision result giving the decision, extended Indeterminate result and status to the new instance
	 * @param pepActions
	 *            PEP actions (obligations/advices) to be added to the result
	 */
	public MutableDecisionResult(final DecisionResult algResult, final MutablePepActions pepActions)
	{
		this(algResult.getDecision(), algResult.getExtendedIndeterminate(), algResult.getStatus(), pepActions);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.decision, this.extIndeterminate, this.status, this.pepActions);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
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
		}
		else if (!this.status.equals(other.getStatus()))
		{
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

	/** {@inheritDoc} */
	@Override
	public DecisionType getExtendedIndeterminate()
	{
		return this.extIndeterminate;
	}

	/**
	 * Add PEP actions to this result
	 * 
	 * @param newPepActions
	 *            PEP actions to be added
	 */
	public void addPepActions(final PepActions newPepActions)
	{
		this.pepActions.add(pepActions);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "Result [decision=" + decision + ", status=" + status + ", pepActions=" + pepActions + "]";
	}

}
