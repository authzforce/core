package org.ow2.authzforce.core;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;

/**
 * PEP action (obligation/advice) factory
 *
 * @param <JAXB_T>
 *            JAXB-annotated PEP action type
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
