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
