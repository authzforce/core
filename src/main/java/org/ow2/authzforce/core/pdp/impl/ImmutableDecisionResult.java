package org.ow2.authzforce.core.pdp.impl;

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.PepActions;

/**
 * Immutable {@link DecisionResult}
 *
 */
public final class ImmutableDecisionResult implements DecisionResult
{

	private final DecisionType decision;

	/**
	 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0 core: <i>potential effect value which could have occurred if there would not have been an error causing the
	 * “Indeterminate”</i>. We use the following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 */
	private final DecisionType extIndeterminate;

	private final Status status;

	// initialized non-null
	private final ImmutablePepActions pepActions;

	private transient volatile int hashCode = 0;

	private ImmutableDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate, final Status status, final ImmutablePepActions pepActions)
	{
		assert decision != null && extendedIndeterminate != null;

		this.decision = decision;
		this.extIndeterminate = extendedIndeterminate;
		this.status = status;
		this.pepActions = pepActions == null ? new ImmutablePepActions(null, null) : pepActions;
	}

	/**
	 * Instantiates a Permit/Deny decision with optional PEP actions (obligations and advice). See {@link #ImmutableDecisionResult(Status, DecisionType)} for Indeterminate, and
	 * {@link DecisionResults#SIMPLE_NOT_APPLICABLE} for NotApplicable decision.
	 *
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 */
	public ImmutableDecisionResult(final DecisionType decision, final ImmutablePepActions pepActions)
	{
		this(decision, DecisionType.NOT_APPLICABLE, null, pepActions);
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
	public ImmutableDecisionResult(final Status status, final DecisionType extendedIndeterminate)
	{
		this(DecisionType.INDETERMINATE, extendedIndeterminate, status, null);
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

		final ImmutableDecisionResult other = (ImmutableDecisionResult) obj;
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

	@Override
	public DecisionType getDecision()
	{
		return this.decision;
	}

	@Override
	public DecisionType getExtendedIndeterminate()
	{
		return DecisionType.NOT_APPLICABLE;
	}

	@Override
	public PepActions getPepActions()
	{
		return this.pepActions;
	}

	@Override
	public Status getStatus()
	{
		return this.status;
	}

}