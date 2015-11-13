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
package com.thalesgroup.authzforce.core;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

/**
 * Decision result filter, i.e. a PDP extension that processes decision Results from policy
 * evaluation engine before the final XACML Response is created (and returned back to the
 * requester). For example, a typical Result filter may combine multiple individual decisions -
 * produced by the 'requestFilter' - to a single decision Result if and only if the XACML Request's
 * 'CombinedDecision' is set to true, as defined in XACML Multiple Decision Profile specification,
 * section 3.
 * 
 */
public interface DecisionResultFilter extends PdpExtension
{
	/**
	 * Filters results (decision)
	 * 
	 * @param results
	 *            input results
	 * @return final result(s)
	 */
	List<Result> filter(List<Result> results);

	/**
	 * 
	 * Support for CombinedDecision = true as specified in Multiple Decision Profile §3 for
	 * combining decisions.
	 * 
	 * @return true iff requests for combined decisions is supported by this
	 */
	boolean supportsMultipleDecisionCombining();
}
