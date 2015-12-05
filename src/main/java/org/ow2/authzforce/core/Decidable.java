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
package org.ow2.authzforce.core;

/**
 * "Decidable" policy element, i.e. policy element that is evaluated to an access control decision:
 * Permit, Deny, etc. As of XACML 3.0, such elements are Rule, Policy and PolicySets, therefore they
 * must implement this interface.
 * 
 */
public interface Decidable
{
	/**
	 * Tries to evaluate the policy by calling the combining algorithm on the given policies or
	 * rules.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	DecisionResult evaluate(EvaluationContext context);

}
