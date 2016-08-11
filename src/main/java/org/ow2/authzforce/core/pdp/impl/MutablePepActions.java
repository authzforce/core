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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.PepActions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * Base PEP actions (obligations/advice)
 *
 * @version $Id: $
 */
public final class MutablePepActions implements PepActions
{
	private static final IllegalArgumentException UNDEF_MERGED_PEP_ACTIONS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined PEP actions");

	// always non-null fields
	private final List<Obligation> obligationList;
	private final List<Advice> adviceList;

	private transient volatile int hashCode = 0;

	/**
	 * Instantiates PEP action set from obligations/advice
	 *
	 * @param obligations
	 *            obligation list; null if no obligation
	 * @param advices
	 *            advice list; null if no advice
	 */
	public MutablePepActions(List<Obligation> obligations, List<Advice> advices)
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
	public List<Obligation> getObligatory()
	{
		return Collections.unmodifiableList(obligationList);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal advice list
	 */
	@Override
	public List<Advice> getAdvisory()
	{
		return Collections.unmodifiableList(adviceList);
	}

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

		if (!(obj instanceof MutablePepActions))
		{
			return false;
		}

		final MutablePepActions other = (MutablePepActions) obj;
		return this.obligationList.equals(other.obligationList) && this.adviceList.equals(other.adviceList);
	}

	/**
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * @param newObligations extra obligations
	 * @param newAdviceList extra advice elements
	 */
	public void addAll(List<Obligation> newObligations, List<Advice> newAdviceList)
	{
		if (newObligations != null)
		{
			this.obligationList.addAll(newObligations);
		}

		if (newAdviceList != null)
		{
			this.adviceList.addAll(newAdviceList);
		}
	}

	/**
	 * 
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * @param pepActions extra PEP actions (obligations/advice)
	 */
	public void add(PepActions pepActions)
	{
		if (pepActions == null)
		{
			throw UNDEF_MERGED_PEP_ACTIONS_ARGUMENT_EXCEPTION;
		}

		addAll(pepActions.getObligatory(), pepActions.getAdvisory());
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "[obligations=" + obligationList + ", advices=" + adviceList + "]";
	}

}
