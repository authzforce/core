/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core;

import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * PEP actions (obligations/advice)
 * 
 */
public class PepActions
{

	private List<Obligation> obligationList = null;
	private List<Advice> adviceList = null;

	/**
	 * Instantiates PEP action set from obligations/advice
	 * 
	 * @param obligations
	 *            obligation list; null if no obligation
	 * @param advices
	 *            advice list; null if no advice
	 */
	public PepActions(List<Obligation> obligations, List<Advice> advices)
	{
		this.obligationList = obligations;
		this.adviceList = advices;
	}

	/**
	 * Get the internal obligation list
	 * 
	 * @return obligations; null if no obligation, else an immutable list
	 */
	public List<Obligation> getObligations()
	{
		return obligationList == null ? null : Collections.unmodifiableList(this.obligationList);
	}

	/**
	 * Get the internal advice list
	 * 
	 * @return advice; null if no advice, else an immutable list
	 */
	public List<Advice> getAdvices()
	{
		return adviceList == null ? null : Collections.unmodifiableList(this.adviceList);
	}

	/**
	 * Add/merge other PEP actions into this (obligations added to obligations, advice added to
	 * advice)
	 * 
	 * @param otherPepActions
	 *            other PEP actions
	 */
	public void add(PepActions otherPepActions)
	{
		if (otherPepActions == null)
		{
			return;
		}

		// merge obligations
		final List<Obligation> otherObligations = otherPepActions.obligationList;
		if (otherObligations != null && !otherObligations.isEmpty())
		{
			if (this.obligationList == null)
			{
				this.obligationList = otherObligations;
			} else
			{

				this.obligationList.addAll(otherObligations);
			}
		}

		// merge advice
		final List<Advice> otherAdvices = otherPepActions.adviceList;
		if (otherAdvices != null && !otherAdvices.isEmpty())
		{
			if (this.adviceList == null)
			{
				this.adviceList = otherAdvices;
			} else
			{

				this.adviceList.addAll(otherAdvices);
			}
		}
	}
}
