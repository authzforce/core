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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;

/**
 * PEP action (obligation/advice) factory
 *
 * @param <JAXB_T>
 *            JAXB-annotated PEP action type
 * @version $Id: $
 */
public interface PepActionFactory<JAXB_T>
{
	/**
	 * Creates instance of PEP action (obligation/advice)
	 *
	 * @param attributeAssignments
	 *            XML/JAXB AttributeAssignments in the PEP action
	 * @param actionId
	 *            action ID (ObligationId, AdviceId)
	 * @return PEP action
	 */
	JAXB_T getInstance(List<AttributeAssignment> attributeAssignments, String actionId);

	/**
	 * Get name of PEP Action element in XACML model, e.g. 'Obligation'
	 *
	 * @return action element name
	 */
	String getActionXmlElementName();
}
