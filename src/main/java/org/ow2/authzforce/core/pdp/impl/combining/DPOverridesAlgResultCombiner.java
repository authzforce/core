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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Helper for making Deny/Permit-overrides combining algorithm results,
 * centralizing common behavior
 */
class DPOverridesAlgResultCombiner
{
	private final UpdatableList<JAXBElement<IdReferenceType>> combinedApplicablePolicyIdList;
	/*
	 * Replaces atLeastOneErrorDP from XACML spec. atLeastOneErrorDP == true <=>
	 * firstIndeterminateDPResult != null
	 */
	private ExtendedDecision firstIndeterminateDPResult = null;
	/*
	 * Replaces atLeastOneErrorD from XACML spec. atLeastOneErrorD == true <=>
	 * firstIndeterminateDResult != null
	 */
	private ExtendedDecision firstIndeterminateDResult = null;
	/*
	 * Replaces atLeastOneErrorP from XACML spec. atLeastOneErrorP == true <=>
	 * firstIndeterminatePResult != null
	 */
	private ExtendedDecision firstIndeterminatePResult = null;
	/**
	 * Replaces atLeastOnePermit (resp. atLeastOneDeny) from description of
	 * permit-overrides (resp. deny-overrides) in the XACML spec.
	 * <p>
	 * atLeastOnePermit (resp. atLeastOneDeny) == false <=> combinedPepActions
	 * == null.
	 * <p>
	 * At this point, we don't know yet whether the PEP actions of
	 * combined/children's Permit/Deny decisions will be added to the final
	 * result's PEP actions, since we don't know yet whether the final decision
	 * is Permit/Deny.
	 */
	private UpdatablePepActions combinedPepActions = null;

	DPOverridesAlgResultCombiner(final boolean returnApplicablePolicyIdList)
	{
		/*
		 * Since we may combine multiple elements before returning a final
		 * decision, we have to collect them in a list; and since we don't know
		 * yet whether the final decision is NotApplicable, we cannot add
		 * collected applicable policies straight to outApplicablePolicyIdList.
		 * So we create a temporary list until we know the final decision
		 * applies.
		 */
		combinedApplicablePolicyIdList = returnApplicablePolicyIdList
				? UpdatableCollections.<JAXBElement<IdReferenceType>> newUpdatableList()
				: UpdatableCollections.<JAXBElement<IdReferenceType>> emptyList();
	}

	/**
	 * Return new result's applicable policies combined (added last) with the
	 * ones previously found, or only the ones combined so far if result == null
	 * 
	 */
	List<JAXBElement<IdReferenceType>> getApplicablePolicies(final DecisionResult result)
	{
		if (result != null)
		{
			combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
		}
		return combinedApplicablePolicyIdList.copy();
	}

	/**
	 * Add intermediate (not final a priori) Deny/Permit result (update
	 * applicable policies and PEP actions), i.e. a Permit (resp. Deny) result
	 * for deny-overrides (resp. permit-overrides)
	 */
	void addSubResultDP(final DecisionResult result)
	{
		combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
		if (combinedPepActions == null)
		{
			// first Permit
			combinedPepActions = new UpdatablePepActions();
		}

		combinedPepActions.add(result.getPepActions());
	}

	/**
	 * Add intermediate (not final a priori) Indeterminate result (update
	 * applicable policies, etc.)
	 */
	void addSubResultIndeterminate(final DecisionResult result)
	{
		combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
		switch (result.getExtendedIndeterminate())
		{
			case INDETERMINATE:
				if (firstIndeterminateDPResult == null)
				{
					firstIndeterminateDPResult = result;
				}
				break;
			case DENY:
				if (firstIndeterminateDResult == null)
				{
					firstIndeterminateDResult = result;
				}
				break;
			case PERMIT:
				if (firstIndeterminatePResult == null)
				{
					firstIndeterminatePResult = result;
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Get any occured IndeterminateDP result
	 */
	ExtendedDecision getFirstIndeterminateDP()
	{
		return firstIndeterminateDPResult;
	}

	/**
	 * Get any occured IndeterminateDP result
	 */
	ExtendedDecision getFirstIndeterminateD()
	{
		return firstIndeterminateDResult;
	}

	/**
	 * Get any occured IndeterminateDP result
	 */
	ExtendedDecision getFirstIndeterminateP()
	{
		return firstIndeterminatePResult;
	}

	/**
	 * Get combined PEP actions of intermediate results
	 */
	PepActions getPepActions()
	{
		return combinedPepActions;
	}
}