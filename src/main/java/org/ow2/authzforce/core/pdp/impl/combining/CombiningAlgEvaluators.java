/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;

/**
 * Common Combining Algorithm evaluators
 */
final class CombiningAlgEvaluators
{
	private CombiningAlgEvaluators()
	{
	}

	final static CombiningAlg.Evaluator NOT_APPLICABLE_CONSTANT_EVALUATOR = new CombiningAlg.Evaluator()
	{

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> updatableApplicablePolicyIdList)
		{
			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	};

}
