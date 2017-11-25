/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.io;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.io.BasePdpEngineAdapter;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;

import com.google.common.base.Supplier;

/**
 * PDP engine adapter utilities
 *
 */
public final class PdpEngineAdapters
{

	private static final IllegalArgumentException NULL_RESPOSTPROC_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined decision result post-processor");
	private static final IllegalArgumentException NULL_REQPREPROC_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined decision request post-processor");

	private PdpEngineAdapters()
	{
		// prevent instantiation
	}

	private static <ADAPTER_INPUT, ADAPTEE_INPUT_DECISION_REQUEST extends DecisionRequest, ADAPTER_OUTPUT> PdpEngineInoutAdapter<ADAPTER_INPUT, ADAPTER_OUTPUT> newInoutAdapter(
			final CloseablePdpEngine adaptee, final DecisionRequestPreprocessor<ADAPTER_INPUT, ?> rawReqPreproc, final DecisionResultPostprocessor<?, ADAPTER_OUTPUT> rawResultPostproc)
			throws IllegalArgumentException
	{
		assert adaptee != null && rawReqPreproc != null && rawResultPostproc != null;
		return new BasePdpEngineAdapter<>(adaptee, (DecisionRequestPreprocessor<ADAPTER_INPUT, ADAPTEE_INPUT_DECISION_REQUEST>) rawReqPreproc,
				(DecisionResultPostprocessor<ADAPTEE_INPUT_DECISION_REQUEST, ADAPTER_OUTPUT>) rawResultPostproc);
	}

	/**
	 * Constructs a new PDP engine adapter for specific input/output types, using given input/output pre-/post-processors.
	 * 
	 * @param <ADAPTER_INPUT>
	 *            type of original input decision request handled by this class. It may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually
	 *            serializable, e.g. XACML-schema-derived JAXB Request for XML.
	 * @param <ADAPTER_OUTPUT>
	 *            type of output result corresponding to ADAPTER_INPUT_DECISION_REQUEST. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
	 * @param adaptee
	 *            adapted PDP engine
	 * @param adapterInputClass
	 *            class of ADAPTER_INPUT
	 * @param adapterOutputClass
	 *            class of ADAPTER_OUTPUT
	 * @param rawReqPreproc
	 *            decision request preprocessor
	 * @param rawResultPostproc
	 *            decision result postprocessor
	 * @return new instance of {@link PdpEngineInoutAdapter}
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the args is null, or
	 *             {@code rawReqPreproc.getInputRequestType() != adapterInputClass || rawResultPostproc.getResponseType() != adapterOutputClass || rawReqPreproc.getOutputRequestType() != rawResultPostproc.getRequestType()}
	 */
	public static <ADAPTER_INPUT, ADAPTER_OUTPUT> PdpEngineInoutAdapter<ADAPTER_INPUT, ADAPTER_OUTPUT> newInoutAdapter(final Class<ADAPTER_INPUT> adapterInputClass,
			final Class<ADAPTER_OUTPUT> adapterOutputClass, final CloseablePdpEngine adaptee, final DecisionRequestPreprocessor<?, ?> rawReqPreproc,
			final DecisionResultPostprocessor<?, ?> rawResultPostproc) throws IllegalArgumentException
	{
		/*
		 * Decision result processor
		 */
		if (rawResultPostproc == null)
		{
			throw NULL_RESPOSTPROC_ARGUMENT_EXCEPTION;
		}

		if (rawResultPostproc.getResponseType() != adapterOutputClass)
		{
			throw new IllegalArgumentException("Invalid response type for " + DecisionResultPostprocessor.class.getCanonicalName() + " extension: " + rawResultPostproc.getResponseType()
					+ ". Expected: " + adapterOutputClass);
		}

		/*
		 * Decision request processor
		 */
		if (rawReqPreproc == null)
		{
			throw NULL_REQPREPROC_ARGUMENT_EXCEPTION;
		}

		if (rawReqPreproc.getInputRequestType() != adapterInputClass)
		{
			throw new IllegalArgumentException("Invalid request type for " + DecisionRequestPreprocessor.class.getCanonicalName() + " extension: " + rawReqPreproc.getInputRequestType()
					+ ". Expected: " + adapterInputClass);
		}

		if (rawReqPreproc.getOutputRequestType() != rawResultPostproc.getRequestType())
		{
			throw new IllegalArgumentException("Decision request preprocessor is not compatible with decision result postprocessor: output request type of preprocessor ("
					+ rawReqPreproc.getOutputRequestType() + ") != input request type of postprocessor (" + rawResultPostproc.getRequestType() + ")");
		}

		return newInoutAdapter(adaptee, (DecisionRequestPreprocessor<ADAPTER_INPUT, ?>) rawReqPreproc, (DecisionResultPostprocessor<?, ADAPTER_OUTPUT>) rawResultPostproc);
	}

	/**
	 * Constructs a new PDP engine adapter for specific input/output types, using a registry of input/output processors.
	 * 
	 * @param <ADAPTER_INPUT>
	 *            type of original input decision request handled by this class. It may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually
	 *            serializable, e.g. XACML-schema-derived JAXB Request for XML.
	 * @param <ADAPTER_OUTPUT>
	 *            type of output result corresponding to ADAPTER_INPUT_DECISION_REQUEST. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
	 * @param adaptee
	 *            adapted PDP engine
	 * @param adapterInputClass
	 *            class of ADAPTER_INPUT
	 * @param adapterOutputClass
	 *            class of ADAPTER_OUTPUT
	 * @param ioProcChainsByInputType
	 *            input/output processor chains indexed by input type
	 * @param defaultReqPreprocSupplier
	 *            default decision input preprocessor if none suitable found in {@code ioProcChainsByInputType}
	 * @param defaultResultPostprocSupplier
	 *            default decision output postprocessor if none suitable found in {@code ioProcChainsByInputType}
	 * @return new instance of {@link PdpEngineInoutAdapter}
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the args is null, or if no suitable input/output processor found in {@code ioProcChainsByInputType} and none supplied by {@code defaultReqPreprocSupplier} or
	 *             {@code defaultResultPostprocSupplier}
	 */
	public static <ADAPTER_INPUT, ADAPTER_OUTPUT> PdpEngineInoutAdapter<ADAPTER_INPUT, ADAPTER_OUTPUT> newInoutAdapter(final Class<ADAPTER_INPUT> adapterInputClass,
			final Class<ADAPTER_OUTPUT> adapterOutputClass, final CloseablePdpEngine adaptee,
			final Map<Class<?>, Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>>> ioProcChainsByInputType,
			final DecisionRequestPreprocessorSupplier defaultReqPreprocSupplier, final Supplier<DecisionResultPostprocessor<?, ?>> defaultResultPostprocSupplier) throws IllegalArgumentException
	{
		final Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>> ioProcChain = ioProcChainsByInputType.get(adapterInputClass);
		final DecisionResultPostprocessor<?, ?> rawResultPostproc;
		final DecisionRequestPreprocessor<?, ?> rawReqPreproc;
		if (ioProcChain == null)
		{
			rawResultPostproc = null;
			rawReqPreproc = null;
		}
		else
		{
			rawResultPostproc = ioProcChain.getValue();
			rawReqPreproc = ioProcChain.getKey();
		}

		final DecisionResultPostprocessor<?, ?> finalResultProc = rawResultPostproc == null ? defaultResultPostprocSupplier.get() : rawResultPostproc;
		final DecisionRequestPreprocessor<?, ?> finalReqProc = rawReqPreproc == null ? defaultReqPreprocSupplier.get(finalResultProc.getFeatures()) : rawReqPreproc;
		return PdpEngineAdapters.newInoutAdapter(adapterInputClass, adapterOutputClass, adaptee, finalReqProc, finalResultProc);
	}

	/**
	 * Constructs a new PDP engine with the given configuration information.
	 * 
	 * @param <ADAPTER_INPUT>
	 *            type of original input decision request handled by this class. It may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually
	 *            serializable, e.g. XACML-schema-derived JAXB Request for XML.
	 * @param <ADAPTER_OUTPUT>
	 *            type of output result corresponding to ADAPTER_INPUT_DECISION_REQUEST. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
	 * @param <ADAPTEE_INPUT_DECISION_REQUEST>
	 *            type of individual decision request passed to the adaptee, i.e. {@link BasePdpEngine} instance.
	 * @param configuration
	 *            PDP engine configuration
	 * @param adapterInputClass
	 *            class of ADAPTER_INPUT
	 * @param adapterOutputClass
	 *            class of ADAPTER_OUTPUT
	 * @param defaultReqPreproc
	 *            default decision request preprocessor if none defined by {@code configuration}
	 * @param defaultResultPostproc
	 *            default decision result postprocessor if none defined by {@code configuration}
	 * @return new instance of {@link PdpEngineInoutAdapter}
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the arguments is null, or if {@code configuration.getXacmlExpressionFactory() == null || configuration.getRootPolicyProvider() == null}
	 * @throws java.io.IOException
	 *             error closing {@code configuration.getRootPolicyProvider()} when static resolution is to be used
	 */
	public static <ADAPTER_INPUT, ADAPTEE_INPUT_DECISION_REQUEST extends DecisionRequest, ADAPTER_OUTPUT> PdpEngineInoutAdapter<ADAPTER_INPUT, ADAPTER_OUTPUT> newInoutAdapter(
			final Class<ADAPTER_INPUT> adapterInputClass, final Class<ADAPTER_OUTPUT> adapterOutputClass, final PdpEngineConfiguration configuration,
			final DecisionRequestPreprocessor<ADAPTER_INPUT, ADAPTEE_INPUT_DECISION_REQUEST> defaultReqPreproc,
			final DecisionResultPostprocessor<ADAPTEE_INPUT_DECISION_REQUEST, ADAPTER_OUTPUT> defaultResultPostproc) throws IllegalArgumentException, IOException
	{
		// use intermediate Java-friendly PdpEngineConfiguration (higher-level than JAXB) that has #getAttributeValueFactory()
		try (final BasePdpEngine adaptedPdpEngine = new BasePdpEngine(configuration))
		{

			final Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>> ioProcChain = configuration.getInOutProcChains().get(adapterInputClass);
			final DecisionResultPostprocessor<?, ?> rawResultPostProc;
			final DecisionRequestPreprocessor<?, ?> rawReqPreproc;
			if (ioProcChain == null)
			{
				rawResultPostProc = null;
				rawReqPreproc = null;
			}
			else
			{
				rawResultPostProc = ioProcChain.getValue();
				rawReqPreproc = ioProcChain.getKey();
			}

			return newInoutAdapter(adapterInputClass, adapterOutputClass, adaptedPdpEngine, rawReqPreproc == null ? defaultReqPreproc : rawReqPreproc,
					rawResultPostProc == null ? defaultResultPostproc : rawResultPostProc);
		}
	}

	/**
	 * Creates a new PDP engine adapter supporting XACML/XML (JAXB) input/output according to XACML 3.0 core specification.
	 * 
	 * @param configuration
	 *            PDP engine configuration
	 * 
	 * @return new instance of {@link PdpEngineInoutAdapter} supporting standard XACML 3.0 XML input/output
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code configuration == null || configuration.getXacmlExpressionFactory() == null || configuration.getRootPolicyProvider() == null}
	 * @throws java.io.IOException
	 *             error closing {@code configuration.getRootPolicyProvider()} when static resolution is to be used
	 */
	public static PdpEngineInoutAdapter<Request, Response> newXacmlJaxbInoutAdapter(final PdpEngineConfiguration configuration) throws IllegalArgumentException, IOException
	{
		final DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response> defaultResultPostproc = new BaseXacmlJaxbResultPostprocessor(configuration.getClientRequestErrorVerbosityLevel());
		final DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> defaultReqPreproc = SingleDecisionXacmlJaxbRequestPreprocessor.LaxVariantFactory.INSTANCE.getInstance(
				configuration.getAttributeValueFactoryRegistry(), configuration.isStrictAttributeIssuerMatchEnabled(), configuration.isXpathEnabled(), XmlUtils.SAXON_PROCESSOR,
				defaultResultPostproc.getFeatures());

		return newInoutAdapter(Request.class, Response.class, configuration, defaultReqPreproc, defaultResultPostproc);
	}
}