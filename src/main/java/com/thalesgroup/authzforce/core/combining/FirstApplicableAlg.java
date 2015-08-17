package com.thalesgroup.authzforce.core.combining;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;

/**
 * This is the standard First-Applicable policy/rule combining algorithm. It looks through the set
 * of policies/rules, finds the first one that applies, and returns that evaluation result.
 * 
 */
public class FirstApplicableAlg extends CombiningAlgorithm<Decidable>
{

	/**
	 * The standard URIs used to identify this algorithm
	 */
	public static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET = new CombiningAlgorithmSet(new FirstApplicableAlg(SUPPORTED_IDENTIFIERS[0]), new FirstApplicableAlg(SUPPORTED_IDENTIFIERS[1]));

	private FirstApplicableAlg(String algId)
	{
		super(algId, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		for (final Decidable combinedElement : combinedElements)
		{
			// evaluate the policy
			final DecisionResult result = combinedElement.evaluate(context);
			final DecisionType decision = result.getDecision();

			// in the case of PERMIT, DENY, or INDETERMINATE, we always
			// just return that result, so only on a rule that doesn't
			// apply do we keep going...
			if (decision != DecisionType.NOT_APPLICABLE)
			{
				return result;
			}
		}

		// if we got here, then none of the rules applied
		return DecisionResult.NOT_APPLICABLE;
	}

}
