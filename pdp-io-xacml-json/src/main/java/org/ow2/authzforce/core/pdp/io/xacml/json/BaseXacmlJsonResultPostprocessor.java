/*
 * Copyright 2012-2023 THALES.
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
package org.ow2.authzforce.core.pdp.io.xacml.json;

import com.google.common.collect.ImmutableList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Convenient base class for {@link DecisionResultPostprocessor} implementations producing XACML/JSON (XACML-JSON-Profile-standard-compliant) output
 * 
 */
public class BaseXacmlJsonResultPostprocessor implements DecisionResultPostprocessor<IndividualXacmlJsonRequest, JSONObject>
{

	private static final RuntimeException ILLEGAL_ATTRIBUTE_VALUE_RUNTIME_EXCEPTION = new RuntimeException(
	        "Unsupported AttributeValue for JSON output: no content or mixed content (more than one JAXB Serializable node)");

	private static Object toJson(Serializable contentItem) {
		if(contentItem instanceof SerializableJSONObject) {
			return ((SerializableJSONObject) contentItem).get();
		}

		return contentItem.toString();
	}

	private static Object toJson(AttributeValue attributeValue) {
		if (!attributeValue.getXmlAttributes().isEmpty())
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_RUNTIME_EXCEPTION;
		}

		final List<Serializable> contentItems = attributeValue.getContent();
		if (contentItems.size() != 1)
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_RUNTIME_EXCEPTION;
		}

		return toJson(contentItems.get(0));
	}

	private static Object toJson(AttributeValueType attributeValue) {
		if (!attributeValue.getOtherAttributes().isEmpty())
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_RUNTIME_EXCEPTION;
		}

		final List<Serializable> contentItems = attributeValue.getContent();
		if (contentItems.size() != 1)
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_RUNTIME_EXCEPTION;
		}

		return toJson(contentItems.get(0));
	}

	/*
	Used for both MissingAttributeDetail (attributeValues may be empty) and AttributeAssignment (category is optional)
	 */
	private static JSONObject attributeToJson(final String attributeId, final Optional<String> category, final Optional<String> issuer, final String datatypeId, final List<Object> jsonAttributeValues)
	{
		final Map<String, Object> jsonPropertiesMap = HashCollections.newUpdatableMap(5);
		jsonPropertiesMap.put("AttributeId", attributeId);
		category.ifPresent(c -> jsonPropertiesMap.put("Category", c));
		jsonPropertiesMap.put("DataType", datatypeId);
		issuer.ifPresent(i -> jsonPropertiesMap.put("Issuer", i));
		if(!jsonAttributeValues.isEmpty()) {
			jsonPropertiesMap.put("Value", jsonAttributeValues.size() == 1? jsonAttributeValues.get(0) : new JSONArray(jsonAttributeValues));
		}
		return new JSONObject(jsonPropertiesMap);
	}

	private static JSONObject toJson(final StatusCode statusCode)
	{
		assert statusCode != null;
		final Map<String, Object> resultJsonObject = HashCollections.newUpdatableMap(2);
		resultJsonObject.put("Value", statusCode.getValue());
		/*
		 * TODO: support nested statusCode. Is it safe?
		 */
		// resultJsonObject.put("StatusCode", toJson(statusCode.getStatusCode()));
		assert statusCode.getStatusCode() == null;
		return new JSONObject(resultJsonObject);
	}

	private static JSONObject toJson(final Status status)
	{
		/*
		 * Weirdness: StatusCode is optional in XACML/JSON Status although mandatory in XACML/XML Status
		 */
		final Map<String, Object> statusJsonObject = HashCollections.newUpdatableMap(3);
		statusJsonObject.put("StatusCode", toJson(status.getStatusCode()));
		final String statusMsg = status.getStatusMessage();
		if (statusMsg != null)
		{
			statusJsonObject.put("StatusMessage", statusMsg);
		}

		final StatusDetail statusDetail = status.getStatusDetail();
		if(statusDetail != null) {
			final List<Element> statusDetailContent = statusDetail.getAnies();
			/*
			AuthzForce only allows/supports StatusDetail containing one and only one MissingAttributeDetail
			 */
			assert statusDetailContent.size() == 1;
			final Element statusDetailElement = statusDetailContent.get(0);
			/*
			This is probably not optimal performance-wise (unmarshalling from DOM Element a JAXB-annotated MissingAttributeDetail that was initially marshalled to the DOM Element), but keeps the code simple.
			 */
			final MissingAttributeDetail missingAttDetail;
			final Unmarshaller unmarshaller;
			try
			{
				unmarshaller = Xacml3JaxbHelper.createXacml3Unmarshaller();
				missingAttDetail = unmarshaller.unmarshal(statusDetailElement, MissingAttributeDetail.class).getValue();
			} catch (JAXBException e)
			{
				throw new RuntimeException("Error instantiating XACML3.0 JAXB unmarshaller or DOM document builder or or unmarshalling MissingAttributeDetail from DOM Element in StatusDetail", e);
			}

			final List<Object> jsonAttributeValues = missingAttDetail.getAttributeValues().stream().map(BaseXacmlJsonResultPostprocessor::toJson).collect(Collectors.toList());
			final JSONObject missingAttDetailJson = attributeToJson(missingAttDetail.getAttributeId(), Optional.ofNullable(missingAttDetail.getCategory()), Optional.ofNullable(missingAttDetail.getIssuer()), missingAttDetail.getDataType(), jsonAttributeValues);
			statusJsonObject.put("StatusDetail", new JSONObject(Map.of("MissingAttributeDetail", missingAttDetailJson)));
		}

		return new JSONObject(statusJsonObject);
	}

	private static JSONObject toJson(final PepActionAttributeAssignment<?> aa)
	{
		return attributeToJson(aa.getAttributeId(), aa.getCategory(), aa.getIssuer(), aa.getDatatype().getId(), List.of(toJson(aa.getValue())));
	}

	private static JSONObject toJson(final String obligationOrAdviceId, final List<PepActionAttributeAssignment<?>> aaList)
	{
		assert obligationOrAdviceId != null && aaList != null;
		final Map<String, Object> obligationOrAdviceJsonPropMap = HashCollections.newUpdatableMap(2);
		obligationOrAdviceJsonPropMap.put("Id", obligationOrAdviceId);
		if (!aaList.isEmpty())
		{
			final List<JSONObject> jsonAttAssignments = aaList.stream().map(BaseXacmlJsonResultPostprocessor::toJson).collect(Collectors.toList());
			obligationOrAdviceJsonPropMap.put("AttributeAssignment", new JSONArray(jsonAttAssignments));
		}

		return new JSONObject(obligationOrAdviceJsonPropMap);
	}

	private static JSONObject convert(final IndividualXacmlJsonRequest request, final DecisionResult result)
	{
		assert request != null && result != null;

		final Map<String, Object> jsonPropertyMap = HashCollections.newUpdatableMap(6);
		// Decision
		jsonPropertyMap.put("Decision", result.getDecision().value());

		// Status
		final Optional<ImmutableXacmlStatus> optStatus = result.getStatus();
		optStatus.ifPresent(immutableXacmlStatus -> jsonPropertyMap.put("Status", toJson(immutableXacmlStatus)));

		// Obligations/Advice
		final ImmutableList<PepAction> pepActions = result.getPepActions();
		assert pepActions != null;
		if (!pepActions.isEmpty())
		{
			final int numOfPepActions = pepActions.size();
			final List<JSONObject> jsonObligations = new ArrayList<>(numOfPepActions);
			final List<JSONObject> jsonAdvices = new ArrayList<>(numOfPepActions);
			pepActions.forEach(pepAction -> {
				final JSONObject pepActionJsonObject = toJson(pepAction.getId(), pepAction.getAttributeAssignments());
				final List<JSONObject> pepActionJsonObjects = pepAction.isMandatory() ? jsonObligations : jsonAdvices;
				pepActionJsonObjects.add(pepActionJsonObject);
			});

			if (!jsonObligations.isEmpty())
			{
				jsonPropertyMap.put("Obligations", new JSONArray(jsonObligations));
			}

			if (!jsonAdvices.isEmpty())
			{
				jsonPropertyMap.put("AssociatedAdvice", new JSONArray(jsonAdvices));
			}
		}

		// IncludeInResult categories
		final List<JSONObject> attributesByCategoryToBeReturned = request.getAttributesByCategoryToBeReturned();
		if (!attributesByCategoryToBeReturned.isEmpty())
		{
			jsonPropertyMap.put("Category", new JSONArray(attributesByCategoryToBeReturned));
		}

		// PolicyIdentifierList
		final ImmutableList<PrimaryPolicyMetadata> applicablePolicies = result.getApplicablePolicies();
		if (applicablePolicies != null && !applicablePolicies.isEmpty())
		{
			final List<JSONObject> policyRefs = new ArrayList<>(applicablePolicies.size());
			final List<JSONObject> policySetRefs = new ArrayList<>(applicablePolicies.size());
			for (final PrimaryPolicyMetadata applicablePolicy : applicablePolicies)
			{
				final JSONObject ref = new JSONObject(HashCollections.newImmutableMap("Id", applicablePolicy.getId(), "Version", applicablePolicy.getVersion().toString()));
				final List<JSONObject> refs = applicablePolicy.getType() == TopLevelPolicyElementType.POLICY ? policyRefs : policySetRefs;
				refs.add(ref);
			}

			final Map<String, Object> policyListJsonObjMap = HashCollections.newUpdatableMap(2);
			if (!policyRefs.isEmpty())
			{
				policyListJsonObjMap.put("PolicyIdReference", new JSONArray(policyRefs));
			}

			if (!policySetRefs.isEmpty())
			{
				policyListJsonObjMap.put("PolicySetIdReference", new JSONArray(policySetRefs));
			}

			jsonPropertyMap.put("PolicyIdentifierList", new JSONObject(policyListJsonObjMap));
		}

		// final Result
		return new JSONObject(jsonPropertyMap);
	}

	private final int maxDepthOfErrorCauseIncludedInResult;

	/**
	 * Constructor
	 * 
	 * @param clientRequestErrorVerbosityLevel
	 *            Level of verbosity of the error message trace returned in case of client request errors, e.g. invalid requests. Increasing this value usually helps the clients better pinpoint the
	 *            issue with their Requests. This result postprocessor returns all error messages in the Java stacktrace up to the same level as this parameter's value if the stacktrace is bigger,
	 *            else the full stacktrace.
	 * @throws IllegalArgumentException
	 *             if {@code clientRequestErrorVerbosityLevel < 0}
	 */
	public BaseXacmlJsonResultPostprocessor(final int clientRequestErrorVerbosityLevel) throws IllegalArgumentException
	{
		if (clientRequestErrorVerbosityLevel < 0)
		{
			throw new IllegalArgumentException("Invalid clientRequestErrorVerbosityLevel: " + clientRequestErrorVerbosityLevel + ". Expected: non-negative.");
		}

		if (clientRequestErrorVerbosityLevel > 0)
		{
			throw new IllegalArgumentException("Unsupported clientRequestErrorVerbosityLevel: " + clientRequestErrorVerbosityLevel + ". Expected: 0.");
		}

		this.maxDepthOfErrorCauseIncludedInResult = clientRequestErrorVerbosityLevel;
	}

	@Override
	public final Class<IndividualXacmlJsonRequest> getRequestType()
	{
		return IndividualXacmlJsonRequest.class;
	}

	@Override
	public final Class<JSONObject> getResponseType()
	{
		return JSONObject.class;
	}

	@Override
	public JSONObject process(final Collection<Entry<IndividualXacmlJsonRequest, ? extends DecisionResult>> resultsByRequest)
	{
		final List<JSONObject> results = resultsByRequest.stream().map(entry -> convert(entry.getKey(), entry.getValue())).collect(Collectors.toList());
		return new JSONObject(HashCollections.newImmutableMap("Response", new JSONArray(results)));
	}

	@Override
	public JSONObject processInternalError(final IndeterminateEvaluationException error)
	{
		final JSONObject result = new JSONObject(HashCollections.newImmutableMap("Decision", DecisionType.INDETERMINATE.value(), "Status", toJson(error.getTopLevelStatus())));
		return new JSONObject(HashCollections.newImmutableMap("Response", new JSONArray(Collections.singleton(result))));
	}

	@Override
	public JSONObject processClientError(final IndeterminateEvaluationException error)
	{
		assert maxDepthOfErrorCauseIncludedInResult == 0;
		final Status finalStatus = error.getTopLevelStatus();
		// FIXME: maxDepthOfErrorCauseIncludedInResult > 0 not supported so far

		final JSONObject result = new JSONObject(HashCollections.newImmutableMap("Decision", DecisionType.INDETERMINATE.value(), "Status", toJson(finalStatus)));
		return new JSONObject(HashCollections.newImmutableMap("Response", new JSONArray(Collections.singleton(result))));
	}

	/**
	 * Convenient base class for {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor.Factory} implementations supporting XACML/JSON output (JSON Profile of XACML)
	 * 
	 */
	public static abstract class Factory implements DecisionResultPostprocessor.Factory<IndividualXacmlJsonRequest, JSONObject>
	{
		private final String id;

		protected Factory(final String id)
		{
			this.id = id;
		}

		@Override
		public final String getId()
		{
			return id;
		}

		@Override
		public final Class<IndividualXacmlJsonRequest> getRequestType()
		{
			return IndividualXacmlJsonRequest.class;
		}

		@Override
		public final Class<JSONObject> getResponseType()
		{
			return JSONObject.class;
		}
	}

	/**
	 *
	 * Default factory creating instances of {@link BaseXacmlJsonResultPostprocessor}
	 *
	 */
	public static final class DefaultFactory extends Factory
	{
		/**
		 * Result postprocessor ID, as returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-json:default";

		/**
		 * No-arg constructor
		 */
		public DefaultFactory()
		{
			super(ID);
		}

		@Override
		public DecisionResultPostprocessor<IndividualXacmlJsonRequest, JSONObject> getInstance(final int clientRequestErrorVerbosityLevel)
		{
			return new BaseXacmlJsonResultPostprocessor(clientRequestErrorVerbosityLevel);
		}
	}

}
