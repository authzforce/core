/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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
package org.ow2.authzforce.core;

import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.Bag;

/**
 * Individual Decision Request, i.e. native Java equivalent of XACML Request that corresponds to one XACML Result element
 *
 */
public interface IndividualDecisionRequest
{

	/**
	 * Get named attributes by name
	 * 
	 * @return map of attribute name-value pairs
	 */
	Map<AttributeGUID, Bag<?>> getNamedAttributes();

	/**
	 * Get Attributes elements containing only child Attribute elements with IncludeInResult=true
	 * 
	 * @return list of Attributes elements to include in final Result
	 */
	List<Attributes> getReturnedAttributes();

	/**
	 * Get Attributes/Contents (parsed into XDM data model for XPath evaluation) by attribute category
	 * 
	 * @return Contents by category
	 */
	Map<String, XdmNode> getExtraContentsByCategory();

	/**
	 * Get ReturnPolicyIdList flag
	 * 
	 * @return true iff original XACML Request's ReturnPolicyIdList == true
	 */
	boolean isApplicablePolicyIdListReturned();

}