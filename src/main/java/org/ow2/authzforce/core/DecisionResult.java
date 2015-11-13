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

import java.util.List;
import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

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
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision and nothing else.
	 */
	public static final DecisionResult DENY = new DecisionResult(DecisionType.DENY, null);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit decision and nothing else.
	 */
	public static final DecisionResult PERMIT = new DecisionResult(DecisionType.PERMIT, null);

	/**
	 * Mark the setObligations/Advice methods as unsupported because replaced with {@link #add(List, List)}, to avoid de-synchronization/consistency violation.
	 * WARNING: overriding {@link #getObligations()}/ {@link #getAssociatedAdvice()} is useless as JAXB will not use them for marshalling. JAXB uses the
	 * JAXB-annotated field directly (in superclass {@link Result}).
	 */
	private static final UnsupportedOperationException UNSUPPORTED_SET_ASSOCIATED_ADVICE_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"DecisionResult.setAssociatedAdvice() not allowed");
	private static final UnsupportedOperationException UNSUPPORTED_SET_OBLIGATIONS_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"DecisionResult.setObligations() not allowed");

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#setObligations(oasis.names.tc.xacml._3_0 .core.schema.wd_17.Obligations)
	 */
	@Override
	public void setObligations(Obligations value)
	{
		throw UNSUPPORTED_SET_OBLIGATIONS_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Result#setAssociatedAdvice(oasis.names.tc.xacml ._3_0.core.schema.wd_17.AssociatedAdvice)
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
	 *            PEP actions (obligations/advices)
	 * @param includedAttributes
	 *            attributes to include in the Result
	 * @param policyIdentifierList
	 */
	public DecisionResult(DecisionType decision, Status status, PepActions pepActions, List<Attributes> includedAttributes,
			PolicyIdentifierList policyIdentifierList)
	{
		/*
		 * We do not set the Obligations and AssociatedAdvice here in the constructor (we set to null instead), because they must be derived from pepActions
		 * which may be modified by merge() and add() method afterwards. Instead we override getAssociatedAdvice() and getAssociatedAdvice() and return a result
		 * directly based on pepActions in these methods (see above). JAXB will get the obligations/advice from these method overrides.
		 */
		super(decision, status, null, null, includedAttributes, policyIdentifierList);
		if (pepActions != null)
		{
			this.add(pepActions.getObligations(), pepActions.getAdvices());
		}
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
	 * Instantiates a Permit/Deny decision with optional obligations and advice. See {@link #DecisionResult(StatusHelper)} for Indeterminate, and
	 * {@link #NOT_APPLICABLE} for NotApplicable.
	 * 
	 * @param decision
	 *            decision
	 * @param pepActions
	 *            PEP actions (obligations/advices)
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
	 * Add PEP actions (obligations/advice) to this
	 * 
	 * @param extraObligationList
	 *            obligations to be added
	 * @param extraAdviceList
	 *            advice elements to be added
	 */
	public void add(List<Obligation> extraObligationList, List<Advice> extraAdviceList)
	{
		// merge obligations
		if (extraObligationList != null && !extraObligationList.isEmpty())
		{
			// before we add anything, make sure the result Obligations is not null
			if (this.obligations == null)
			{
				this.obligations = new Obligations();
			}

			this.obligations.getObligations().addAll(extraObligationList);
		}

		// merge advice
		if (extraAdviceList != null && !extraAdviceList.isEmpty())
		{
			if (this.associatedAdvice == null)
			{
				this.associatedAdvice = new AssociatedAdvice();
			}

			this.associatedAdvice.getAdvices().addAll(extraAdviceList);

		}
	}

	/**
	 * Merges obligations/advice from this into other result
	 * 
	 * @param mergeResultBase
	 *            the other result to merge into, may be null, in which case this result is returned
	 * @return new result from merge, this is the actual result as mergeResultBase may be null, in which case this result is returned
	 */
	public DecisionResult merge(DecisionResult mergeResultBase)
	{
		if (mergeResultBase == null)
		{
			return this;
		}

		// mergeResultBase != null
		mergeResultBase.add(this.obligations == null ? null : this.obligations.getObligations(),
				this.associatedAdvice == null ? null : this.associatedAdvice.getAdvices());
		return mergeResultBase;
	}

	private transient volatile int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.decision, this.status, this.obligations == null ? null : this.obligations.getObligations(),
					this.associatedAdvice == null ? null : this.associatedAdvice.getAdvices(), this.attributes, this.policyIdentifierList == null ? null
							: this.policyIdentifierList.getPolicyIdReferencesAndPolicySetIdReferences());
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

		if (!(obj instanceof Result))
		{
			return false;
		}

		final Result other = (Result) obj;
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
		if (this.getObligations() == null)
		{
			if (other.getObligations() != null)
			{
				return false;
			}
		} else if (!this.getObligations().equals(other.getObligations()))
		{
			return false;
		}

		// this.getAssociatedAdvice() derived from this.pepActions
		if (this.getAssociatedAdvice() == null)
		{
			if (other.getAssociatedAdvice() != null)
			{
				return false;
			}
		} else if (!this.getAssociatedAdvice().equals(other.getAssociatedAdvice()))
		{
			return false;
		}

		if (this.attributes == null)
		{
			if (other.getAttributes() != null)
			{
				return false;
			}
		} else if (!this.attributes.equals(other.getAttributes()))
		{
			return false;
		}

		if (this.policyIdentifierList == null)
		{
			if (other.getPolicyIdentifierList() != null)
			{
				return false;
			}
		} else if (!this.policyIdentifierList.equals(other.getPolicyIdentifierList()))
		{
			return false;
		}

		return true;
	}

}
