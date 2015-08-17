package com.thalesgroup.authzforce.core.combining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;

/**
 * This is the standard Permit-Overrides policy/rule combining algorithm. It allows a single
 * evaluation of Permit to take precedence over any number of deny, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports
 * the Ordered-Permit-Overrides algorithm.
 */
public class PermitOverridesAlg extends CombiningAlgorithm<Decidable>
{
	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET;
	static
	{
		final Set<CombiningAlgorithm<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new PermitOverridesAlg(algId));
		}

		SET = new CombiningAlgorithmSet(algSet);
	}

	private PermitOverridesAlg(String algId)
	{
		super(algId, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		/*
		 * Replaces and enhances atLeastOneError from XACML spec. atLeastOneError == true <=>
		 * firstIndeterminateResult != null
		 */
		DecisionResult firstIndeterminateResult = null;

		/*
		 * Replaces and enhances atLeastOneDeny from XACML spec. atLeastOneDeny == true <=>
		 * combinedDenyResult != null
		 */
		DecisionResult combinedDenyResult = null;

		for (final Decidable combinedElement : combinedElements)
		{
			// evaluate the policy
			final DecisionResult result = combinedElement.evaluate(context);
			switch (result.getDecision())
			{
				case PERMIT:
					return result;
				case DENY:
					// merge the obligations/advice in case the final result is Deny
					combinedDenyResult = result.merge(combinedDenyResult);
					break;
				case INDETERMINATE:
					/*
					 * FIXME: implement extended Indeterminate decisions (result differs if
					 * Indeterminate{P} or Indeterminate{D})
					 */
					firstIndeterminateResult = result;
					break;
				default:
			}
		}

		/*
		 * FIXME: implement extended Indeterminate decisions as the algorithm distinguishes them.
		 */
		if (firstIndeterminateResult != null)
		{
			return firstIndeterminateResult;
		}

		/*
		 * atLeastOneDeny == true <=> combinedDenyResult != null
		 */
		if (combinedDenyResult != null)
		{
			return combinedDenyResult;
		}

		return DecisionResult.NOT_APPLICABLE;
	}
}
