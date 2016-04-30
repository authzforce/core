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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

import org.ow2.authzforce.core.pdp.api.PepActions;

/**
 * Base PEP actions (obligations/advice)
 *
 * @author cdangerv
 * @version $Id: $
 */
public final class BasePepActions implements PepActions
{
	private static final IllegalArgumentException UNDEF_MERGED_PEP_ACTIONS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined PEP actions");

	/**
	 * Obligation factory
	 *
	 */
	public static final PepActionFactory<Obligation> OBLIGATION_FACTORY = new PepActionFactory<Obligation>()
	{

		@Override
		public Obligation getInstance(List<AttributeAssignment> attributeAssignments, String actionId)
		{
			return new Obligation(attributeAssignments, actionId);
		}

		@Override
		public String getActionXmlElementName()
		{
			return "Obligation";
		}

	};

	/**
	 * Advice factory
	 *
	 */
	public static final PepActionFactory<Advice> ADVICE_FACTORY = new PepActionFactory<Advice>()
	{

		@Override
		public Advice getInstance(List<AttributeAssignment> attributeAssignments, String actionId)
		{
			return new Advice(attributeAssignments, actionId);
		}

		@Override
		public String getActionXmlElementName()
		{
			return "Advice";
		}

	};

	// always non-null fields
	private final List<Obligation> obligationList;
	private final List<Advice> adviceList;

	/**
	 * Instantiates PEP action set from obligations/advice
	 *
	 * @param obligations
	 *            obligation list; null if no obligation
	 * @param advices
	 *            advice list; null if no advice
	 */
	public BasePepActions(List<Obligation> obligations, List<Advice> advices)
	{
		this.obligationList = obligations == null ? new ArrayList<Obligation>() : obligations;
		this.adviceList = advices == null ? new ArrayList<Advice>() : advices;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal obligation list
	 */
	@Override
	public List<Obligation> getObligations()
	{
		return Collections.unmodifiableList(obligationList);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal advice list
	 */
	@Override
	public List<Advice> getAdvices()
	{
		return Collections.unmodifiableList(adviceList);
	}

	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.obligationList, this.adviceList);
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

		if (!(obj instanceof BasePepActions))
		{
			return false;
		}

		final BasePepActions other = (BasePepActions) obj;
		return this.obligationList.equals(other.obligationList) && this.adviceList.equals(other.adviceList);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 */
	@Override
	public void merge(List<Obligation> newObligations, List<Advice> newAdvices)
	{
		if (newObligations != null)
		{
			this.obligationList.addAll(newObligations);
		}

		if (newAdvices != null)
		{
			this.adviceList.addAll(newAdvices);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 */
	@Override
	public void merge(PepActions pepActions)
	{
		if (pepActions == null)
		{
			throw UNDEF_MERGED_PEP_ACTIONS_ARGUMENT_EXCEPTION;
		}

		merge(pepActions.getObligations(), pepActions.getAdvices());
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "[obligations=" + obligationList + ", advices=" + adviceList + "]";
	}

}
