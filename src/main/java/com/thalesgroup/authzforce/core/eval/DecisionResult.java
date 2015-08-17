/**
 * 
 */
package com.thalesgroup.authzforce.core.eval;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.PepActions;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...)
 * 
 */
public class DecisionResult extends Result
{
	/**
	 * NotApplicable decision result
	 */
	public static final DecisionResult NOT_APPLICABLE = new DecisionResult(DecisionType.NOT_APPLICABLE, null);

	/**
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision
	 * and nothing else.
	 */
	public static final DecisionResult DENY = new DecisionResult(DecisionType.DENY, null);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit
	 * decision and nothing else.
	 */
	public static final DecisionResult PERMIT = new DecisionResult(DecisionType.PERMIT, null);

	/**
	 * Mark the get/setObligations/Advice methods as unsupported because replaced with
	 * {@link #getPepActions()} and {@link #add(PepActions)}, to avoid
	 * de-synchronization/consistency violation
	 */
	private static final UnsupportedOperationException UNSUPPORTED_SET_ASSOCIATED_ADVICE_OPERATION_EXCEPTION = new UnsupportedOperationException("DecisionResult.setAssociatedAdvice() not allowed");
	private static final UnsupportedOperationException UNSUPPORTED_SET_OBLIGATIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("DecisionResult.setObligations() not allowed");

	private PepActions pepActions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#getObligations()
	 */
	@Override
	public Obligations getObligations()
	{
		if (pepActions == null)
		{
			return null;
		}

		final List<Obligation> obligationList = pepActions.getObligations();
		// obligationList expected to be immutable
		return obligationList == null ? null : new Obligations(obligationList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#setObligations(oasis.names.tc.xacml._3_0
	 * .core.schema.wd_17.Obligations)
	 */
	@Override
	public void setObligations(Obligations value)
	{
		throw UNSUPPORTED_SET_OBLIGATIONS_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#getAssociatedAdvice()
	 */
	@Override
	public AssociatedAdvice getAssociatedAdvice()
	{
		if (pepActions == null)
		{
			return null;
		}

		final List<Advice> adviceList = pepActions.getAdvices();
		// adviceList expected to be immutable
		return adviceList == null ? null : new AssociatedAdvice(adviceList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#setAssociatedAdvice(oasis.names.tc.xacml
	 * ._3_0.core.schema.wd_17.AssociatedAdvice)
	 */
	@Override
	public void setAssociatedAdvice(AssociatedAdvice value)
	{
		throw UNSUPPORTED_SET_ASSOCIATED_ADVICE_OPERATION_EXCEPTION;
	}

	/**
	 * Instantiates a generic Decision result
	 * 
	 * @param decision
	 *            decision
	 * @param status
	 *            status
	 * @param pepActions
	 *            obligations and advice
	 * @param attributes
	 * @param policyIdentifierList
	 */
	public DecisionResult(DecisionType decision, Status status, PepActions pepActions, List<Attributes> attributes, PolicyIdentifierList policyIdentifierList)
	{
		/*
		 * We do not set the Obligations and AssociatedAdvice here in the constructor (we set to
		 * null instead), because they must be derived from pepActions which may be modified by
		 * merge() and add() method afterwards. Instead we override getAssociatedAdvice() and
		 * getAssociatedAdvice() and return a result directly based on pepActions in these methods
		 * (see above). JAXB will get the obligations/advice from these method overrides.
		 */
		super(decision, status, null, null, attributes, policyIdentifierList);
		this.pepActions = pepActions;
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 * 
	 * @param status
	 *            reason/code for Indeterminate
	 */
	public DecisionResult(Status status)
	{
		this(DecisionType.INDETERMINATE, status, null, null, null);
	}

	/**
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See
	 * {@link #DecisionResult(Status)} for Indeterminate, and {@link #NOT_APPLICABLE} for
	 * NotApplicable.
	 * 
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            obligations/advice
	 */
	public DecisionResult(DecisionType decision, PepActions pepActions)
	{
		this(decision, null, pepActions, null, null);
	}

	/**
	 * Set attributes to be included in Result
	 * 
	 * @param attributes
	 *            attributes to be included
	 */
	public void setAttributes(List<Attributes> attributes)
	{
		this.attributes = attributes;
	}

	/**
	 * Get obligations/advice in this result
	 * 
	 * @return PEP actions (obligations/advice)
	 */
	public PepActions getPepActions()
	{
		return this.pepActions;
	}

	/**
	 * Add PEP actions (obligations/advice) to this
	 * 
	 * @param actions
	 */
	public void add(PepActions actions)
	{
		if (actions == null)
		{
			return;
		}

		// actions != null
		if (this.pepActions == null)
		{
			this.pepActions = actions;
			return;
		}

		// pepActions != null
		this.pepActions.add(actions);
	}

	/**
	 * Merges obligations/advice from this into other result
	 * 
	 * @param mergeResultBase
	 *            the other result to merge into, may be null, in which case this result is returned
	 * @return new result from merge, this is the actual result as mergeResultBase may be null, in
	 *         which case this result is returned
	 */
	public DecisionResult merge(DecisionResult mergeResultBase)
	{
		if (mergeResultBase == null)
		{
			return this;
		}

		// mergeResultBase != null
		mergeResultBase.add(pepActions);
		return mergeResultBase;
	}

}
