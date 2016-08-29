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
package org.ow2.authzforce.core.pdp.impl;

import java.util.List;

import org.ow2.authzforce.core.pdp.api.PepActions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * PEP action (obligation/advice) factories
 */
public final class PepActionFactories {
	/**
	 * Obligation factory
	 *
	 */
	public static final PepActions.Factory<Obligation> OBLIGATION_FACTORY = new PepActions.Factory<Obligation>()
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
	public static final PepActions.Factory<Advice> ADVICE_FACTORY = new PepActions.Factory<Advice>()
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
}
