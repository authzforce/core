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
