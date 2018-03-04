package org.ow2.authzforce.core.pdp.impl.io;

import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor.Factory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

/**
 *
 * Default factory creating instances of {@link BaseXacmlJaxbResultPostprocessor}
 *
 */
public final class DefaultXacmlJaxbResultPostprocessorFactory extends Factory
{
	/**
	 * Result postprocessor ID, as returned by {@link #getId()}
	 */
	public static final String ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-xml:default";

	/**
	 * No-arg constructor
	 */
	public DefaultXacmlJaxbResultPostprocessorFactory()
	{
		super(ID);
	}

	@Override
	public DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response> getInstance(final int clientRequestErrorVerbosityLevel)
	{
		return new BaseXacmlJaxbResultPostprocessor(clientRequestErrorVerbosityLevel);
	}
}