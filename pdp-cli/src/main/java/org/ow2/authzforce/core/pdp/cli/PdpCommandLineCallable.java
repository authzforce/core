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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.io.xacml.json.BaseXacmlJsonResultPostprocessor;
import org.ow2.authzforce.core.pdp.io.xacml.json.IndividualXacmlJsonRequest;
import org.ow2.authzforce.core.pdp.io.xacml.json.SingleDecisionXacmlJsonRequestPreprocessor;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.json.model.Xacml3JsonUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * {@link Callable} allowing to call the PDP engine (adapters) from the command-line
 * <p>
 * TODO: implement tests: 1) with xacml-xml 2) with xacml-json 3/4) with/without catalog and with/without extension XSD.
 *
 */
@Command(name = "authzforce-ce-core-pdp-cli", description = "Evaluates a XACML Request against a XACML Policy(Set) using AuthzForce PDP engine")
public final class PdpCommandLineCallable implements Callable<Void>
{
	private static enum RequestType
	{
		XACML_XML, XACML_JSON;
	}

	/*
	 * WARNING: do not make picocli-annoated fields final here! Known issue: https://github.com/remkop/picocli/issues/68. Planned to be fixed in release 2.1.0.
	 */
	@Option(names = { "-t", "--type" }, description = "Type of XACML request/response: 'XACML_XML' for XACML 3.0/XML (XACML core specification), 'XACML_JSON' for XACML 3.0/JSON (JSON Profile of XACML 3.0)")
	private RequestType requestType = RequestType.XACML_XML;

	@Parameters(index = "0", description = "Path to PDP configuration file, valid against schema located at https://github.com/authzforce/core/blob/release-X.Y.Z/pdp-engine/src/main/resources/pdp.xsd (X.Y.Z is the version provided by -v option)")
	private File confFile;

	@Option(names = { "-c", "--catalog" }, description = "Path to XML catalog for resolving schemas used in extensions XSD specified by -e option, required only if -e specified")
	private String catalogLocation = null;

	@Option(names = { "-e", "--extensions" }, description = "Path to extensions XSD (contains XSD namespace imports for all extensions used in the PDP configuration), required only if using any extension in the PDP configuration file")
	private String extensionXsdLocation = null;

	@Parameters(index = "1", description = "XACML Request (format determined by -t option)")
	private File reqFile;

	@Option(names = { "-p", "--prettyprint" }, description = "Pretty-print output with line feeds and indentation")
	private boolean formattedOutput = false;

	@Override
	public Void call() throws Exception
	{
		final PdpEngineConfiguration configuration = PdpEngineConfiguration.getInstance(confFile, catalogLocation, extensionXsdLocation);
		System.out.println();
		switch (requestType)
		{

			case XACML_JSON:
				final JSONObject jsonRequest;
				try (InputStream inputStream = new FileInputStream(reqFile))
				{
					jsonRequest = new JSONObject(new JSONTokener(inputStream));
					if (!jsonRequest.has("Request"))
					{
						throw new IllegalArgumentException("Invalid XACML JSON Request file: " + reqFile + ". Expected root key: \"Request\"");
					}

					Xacml3JsonUtils.REQUEST_SCHEMA.validate(jsonRequest);
				}

				final DecisionResultPostprocessor<IndividualXacmlJsonRequest, JSONObject> defaultResultPostproc = new BaseXacmlJsonResultPostprocessor(
						configuration.getClientRequestErrorVerbosityLevel());
				final DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> defaultReqPreproc = SingleDecisionXacmlJsonRequestPreprocessor.LaxVariantFactory.INSTANCE.getInstance(
						configuration.getAttributeValueFactoryRegistry(), configuration.isStrictAttributeIssuerMatchEnabled(), configuration.isXpathEnabled(), XmlUtils.SAXON_PROCESSOR,
						defaultResultPostproc.getFeatures());

				final PdpEngineInoutAdapter<JSONObject, JSONObject> jsonPdpEngineAdapter = PdpEngineAdapters.newInoutAdapter(JSONObject.class, JSONObject.class, configuration, defaultReqPreproc,
						defaultResultPostproc);
				final JSONObject jsonResponse = jsonPdpEngineAdapter.evaluate(jsonRequest);
				System.out.println(jsonResponse.toString(formattedOutput ? 4 : 0));
				break;

			default:
				final XmlnsFilteringParser parser = XacmlJaxbParsingUtils.getXacmlParserFactory(true).getInstance();
				final Object request = parser.parse(reqFile.toURI().toURL());
				if (!(request instanceof Request))
				{
					throw new IllegalArgumentException("Invalid XACML/XML Request file (according to XACML 3.0 schema): " + reqFile);
				}

				final PdpEngineInoutAdapter<Request, Response> xmlPdpEngineAdapter = PdpEngineAdapters.newXacmlJaxbInoutAdapter(configuration);
				final Response xmlResponse = xmlPdpEngineAdapter.evaluate((Request) request, parser.getNamespacePrefixUriMap());
				final Marshaller marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
				marshaller.marshal(xmlResponse, System.out);
				break;
		}

		System.out.println();
		return null;
	}

	/**
	 * Method used for the command-line
	 * 
	 * @param args
	 *            CLI args
	 */
	public static void main(final String[] args)
	{
		CommandLine.call(new PdpCommandLineCallable(), System.out, args);
	}

}
