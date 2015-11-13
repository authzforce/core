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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * PEP actions (obligations/advice)
 * 
 */
public final class PepActions
{
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
		return obligationList;
	}

	/**
	 * Get the internal advice list
	 * 
	 * @return advice; null if no advice, else an immutable list
	 */
	public List<Advice> getAdvices()
	{
		return adviceList;
	}
}
