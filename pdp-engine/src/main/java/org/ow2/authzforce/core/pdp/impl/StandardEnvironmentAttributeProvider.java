/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.impl;

import com.google.common.base.Preconditions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.core.xmlns.pdp.StdEnvAttributeProviderDescriptor;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * AttributeProvider that provides the standard environment attributes specified
 * in XACML 3.0 Core specification, §10.2.5: current-time, current-date and current-dateTime.
 * This AttributeProvider is enabled by default with <i>override=false</i> mode, in order to comply with XACML 3.0 standard (§10.2.5): <i>If
 * values for these
 * attributes are not present in
 * the decision request,
 * then their
 * values MUST be supplied
 * by the
 * context
 * handler</i>. Note that it does <b>not</b>
 * say <i>If AND ONLY IF</i>, therefore, the <i>override=true</i> mode (always supplied/overridden by the context handler even if present in the request) may still be considered XACML-compliant in a strict sense.
 */
public final class StandardEnvironmentAttributeProvider extends BaseNamedAttributeProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardEnvironmentAttributeProvider.class);
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    private static final IndeterminateEvaluationException UNEXPECTED_CALL_TO_GET_ATTRIBUTE_EXCEPTION = new IndeterminateEvaluationException("Method StandardEnvironmentAttributeProvider#get(AttributeFqn, Datatype, EvaluationContext, Optional) should not be called in override=true mode because attributes already provided in EvaluationContext when calling method beginIndividualDecisionRequest(EvaluationContext, Optional). (Possibly this method was not called as expected.)", XacmlStatusCode.PROCESSING_ERROR.value());
    private static final Set<AttributeDesignatorType> SUPPORTED_ATT_DESIGNATORS;

    static
    {
        final Collection<AttributeDesignatorType> mutableSet = new HashSet<>();
        for (final StandardEnvironmentAttribute att : StandardEnvironmentAttribute.values())
        {
            mutableSet.add(new AttributeDesignatorType(att.getFQN().getCategory(), att.getFQN().getId(), att.getDatatype().getId(), att.getFQN().getIssuer().orElse(null), false));
        }

        SUPPORTED_ATT_DESIGNATORS = Set.copyOf(mutableSet);
    }

    private static void overrideEvalCtxFromTimestamp(final EvaluationContext evalCtx)
    {
        assert evalCtx != null;
        final Instant evalCtxCreationTimestamp = evalCtx.getCreationTimestamp();
        LOGGER.debug("Setting/overriding standard current-* attributes with values from EvaluationContext#getCreationTimestamp() = {}", evalCtxCreationTimestamp);
        /*
         * Set the standard current date/time attribute according to XACML core spec:
         * "This identifier indicates the current time at the context handler. In practice, it is the time at which the request context was created." (§B.7). XACML standard (§10.2.5) says: "If values
         * for these attributes are not present in the decision request, then their values MUST be supplied by the context handler".
         */
        final ZonedDateTime dateTime = ZonedDateTime.ofInstant(evalCtxCreationTimestamp, UTC_ZONE_ID);
        final DateTimeValue currentDateTimeValue = new DateTimeValue(GregorianCalendar.from(dateTime));
        final DateValue currentDateValue = DateValue.getInstance(currentDateTimeValue.getUnderlyingValue());
        final TimeValue currentTimeValue = TimeValue.getInstance(currentDateTimeValue.getUnderlyingValue());
        evalCtx.putNamedAttributeValue(StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN(), Bags.singletonAttributeBag(StandardDatatypes.DATETIME, currentDateTimeValue, AttributeSources.PDP), true);
        evalCtx.putNamedAttributeValue(StandardEnvironmentAttribute.CURRENT_DATE.getFQN(), Bags.singletonAttributeBag(StandardDatatypes.DATE, currentDateValue, AttributeSources.PDP), true);
        evalCtx.putNamedAttributeValue(StandardEnvironmentAttribute.CURRENT_TIME.getFQN(), Bags.singletonAttributeBag(StandardDatatypes.TIME, currentTimeValue, AttributeSources.PDP), true);
    }

    private static <AV extends AttributeValue> AttributeBag<AV> getCurrentDateOrTime(final AttributeFqn attributeFQN, final Datatype<AV> datatype, Datatype<?> expectedDatatype, final EvaluationContext evalCtx, final Optional<EvaluationContext> mdpCtx) throws IndeterminateEvaluationException
    {
        assert evalCtx != null && attributeFQN != null && (attributeFQN.equals(StandardEnvironmentAttribute.CURRENT_DATE.getFQN()) && expectedDatatype.equals(StandardDatatypes.DATE) || attributeFQN.equals(StandardEnvironmentAttribute.CURRENT_TIME.getFQN()) && expectedDatatype.equals(StandardDatatypes.TIME));

        if (!datatype.equals(expectedDatatype))
        {
            throw new IndeterminateEvaluationException("Invalid Datatype requested for attribute " + attributeFQN + ": " + datatype + ". Expected: " + expectedDatatype + " (mandatory per XACML standard)", XacmlStatusCode.PROCESSING_ERROR.value());
        }

            /*
             Input datatype is valid. Assume the value is not in {@code context}, else the PDP would not have needed to call this method.
             */
            /*
                         Try get it from mdpContext.
             */
        if (mdpCtx.isPresent())
        {
            final AttributeBag<AV> mdpCtxVal = mdpCtx.get().getNamedAttributeValue(attributeFQN, datatype);
            if (mdpCtxVal == null)
            {
                throw new IndeterminateEvaluationException("Attribute " + attributeFQN + " undefined in MDP evaluation context (possibly because the method StandardEnvironmentAttributeProvider#beginMultipleDecisionRequest(EvaluationContext) was not called)", XacmlStatusCode.PROCESSING_ERROR.value());
            }

            return mdpCtxVal;
        }

                 /*
                        No mdpContext. Try recover the value from current-dateTime in request {@code context}.
             */
        final AttributeFqn currentDateTimeAttName = StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN();
        final AttributeBag<DateTimeValue> dateTimeBag = evalCtx.getNamedAttributeValue(currentDateTimeAttName, StandardDatatypes.DATETIME);
        if (dateTimeBag == null)
        {
                /*
                Last option: we set current-datetime from context.getCreationTimestamp(), and for consistency, we set/override current-date and current-time in context as well
                 */
            LOGGER.debug("Requesting attribute {} but {} undefined.", attributeFQN, currentDateTimeAttName);
            overrideEvalCtxFromTimestamp(evalCtx);
            return evalCtx.getNamedAttributeValue(attributeFQN, datatype);
        }

        if (dateTimeBag.isEmpty())
        {
            throw new IndeterminateEvaluationException("Invalid value of attribute " + currentDateTimeAttName + "  in evaluation context: <empty>", XacmlStatusCode.PROCESSING_ERROR.value());
        }

        // current-dateTime non null/empty
        final XMLGregorianCalendar dateTimeCal = dateTimeBag.getSingleElement().getUnderlyingValue();
        final DateValue currentDateValue = DateValue.getInstance(dateTimeCal);
        final TimeValue currentTimeValue = TimeValue.getInstance(dateTimeCal);
        evalCtx.putNamedAttributeValue(StandardEnvironmentAttribute.CURRENT_DATE.getFQN(), Bags.singletonAttributeBag(StandardDatatypes.DATE, currentDateValue, AttributeSources.PDP), true);
        evalCtx.putNamedAttributeValue(StandardEnvironmentAttribute.CURRENT_TIME.getFQN(), Bags.singletonAttributeBag(StandardDatatypes.TIME, currentTimeValue, AttributeSources.PDP), true);
        return evalCtx.getNamedAttributeValue(attributeFQN, datatype);
    }

    private static void overrideEvalCtxFromMdpCtx(final EvaluationContext evalCtx, final EvaluationContext mdpCtx) throws IndeterminateEvaluationException
    {
        assert evalCtx != null && mdpCtx != null;
        for (final StandardEnvironmentAttribute att : StandardEnvironmentAttribute.values())
        {
            final AttributeBag<?> mdpCtxVal = mdpCtx.getNamedAttributeValue(att.getFQN(), att.getDatatype());
            if (mdpCtxVal == null)
            {
                throw new IndeterminateEvaluationException("Attribute " + att.getFQN() + " undefined in MDP evaluation context (possibly because the method StandardEnvironmentAttributeProvider#beginMultipleDecisionRequest(EvaluationContext) was not called)", XacmlStatusCode.PROCESSING_ERROR.value());
            }
            evalCtx.putNamedAttributeValue(att.getFQN(), mdpCtxVal, true);
        }
    }

    private interface AttributeProviderHelper
    {
        void beginIndividualDecisionRequest(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;

        <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFQN, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;
    }

    private static final AttributeProviderHelper ALWAYS_REQ_OVERRIDING_HELPER = new AttributeProviderHelper()
    {
        @Override
        public void beginIndividualDecisionRequest(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
            assert context != null && mdpContext != null;
            if (mdpContext.isEmpty())
            {
                overrideEvalCtxFromTimestamp(context);
            } else
            {
                // Override individual request context from MDP request evaluation context set in beginMultipleDecisionRequest()
                overrideEvalCtxFromMdpCtx(context, mdpContext.get());
            }
        }

        @Override
        public <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFQN, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
             /*
        No reason for the PDP to call this method since all provided attributes already set in {@code context} by beginIndividualDecisionRequest() method
         */
            throw UNEXPECTED_CALL_TO_GET_ATTRIBUTE_EXCEPTION;
        }
    };

    private static final AttributeProviderHelper OPTIONAL_REQ_OVERRIDING_HELPER =  new AttributeProviderHelper()
    {
        @Override
        public void beginIndividualDecisionRequest(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
            assert context != null;
            // validate consistency of dates/times in request context
            final AttributeFqn currentDateTimeAttName = StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN();
            final AttributeBag<DateTimeValue> dateTimeBag = context.getNamedAttributeValue(currentDateTimeAttName, StandardDatatypes.DATETIME);
            if (dateTimeBag != null)
            {
                if (dateTimeBag.isEmpty())
                {
                    // Invalid request
                    throw new IndeterminateEvaluationException("Invalid value of attribute " + currentDateTimeAttName + "  in request context: <empty>", XacmlStatusCode.SYNTAX_ERROR.value());
                }

                // dateTimeBag non null/empty
                final XMLGregorianCalendar dateTimeCal = dateTimeBag.getSingleElement().getUnderlyingValue();
                // Does it match current-date and current-time?
                final AttributeFqn currentDateAttName = StandardEnvironmentAttribute.CURRENT_DATE.getFQN();
                final AttributeBag<DateValue> dateBag = context.getNamedAttributeValue(currentDateAttName, StandardDatatypes.DATE);
                if (dateBag != null)
                {
                    if (dateBag.isEmpty())
                    {
                        // Invalid request
                        throw new IndeterminateEvaluationException("Invalid value of attribute " + currentDateAttName + "  in request context: <empty>", XacmlStatusCode.SYNTAX_ERROR.value());
                    }

                    final XMLGregorianCalendar dateCal = dateBag.getSingleElement().getUnderlyingValue();
                    if (dateTimeCal.getEonAndYear() == null && dateCal.getEonAndYear() != null || dateTimeCal.getEonAndYear() != null && !dateTimeCal.getEonAndYear().equals(dateCal.getEonAndYear()) || dateTimeCal.getMonth() != dateCal.getMonth() || dateTimeCal.getDay() != dateCal.getDay() || dateTimeCal.getTimezone() != dateCal.getTimezone())
                    {
                        // Invalid request
                        throw new IndeterminateEvaluationException("Invalid value of attribute " + currentDateAttName + "  in request context: " + dateCal + ". Not matching the value of attribute " + currentDateTimeAttName + ": " + dateTimeCal, XacmlStatusCode.SYNTAX_ERROR.value());
                    }
                }

                final AttributeFqn currentTimeAttName = StandardEnvironmentAttribute.CURRENT_TIME.getFQN();
                final AttributeBag<TimeValue> timeBag = context.getNamedAttributeValue(currentTimeAttName, StandardDatatypes.TIME);
                if (timeBag != null)
                {
                    if (timeBag.isEmpty())
                    {
                        // Invalid request
                        throw new IndeterminateEvaluationException("Invalid value of attribute " + currentTimeAttName + "  in request context: <empty>", XacmlStatusCode.SYNTAX_ERROR.value());
                    }

                    final XMLGregorianCalendar timeCal = timeBag.getSingleElement().getUnderlyingValue();
                    if (dateTimeCal.getHour() != timeCal.getHour() || dateTimeCal.getMinute() != timeCal.getMinute() || dateTimeCal.getSecond() != timeCal.getSecond() || dateTimeCal.getFractionalSecond() == null && timeCal.getFractionalSecond() != null ||dateTimeCal.getFractionalSecond() != null && !dateTimeCal.getFractionalSecond().equals(timeCal.getFractionalSecond()) || dateTimeCal.getTimezone() != timeCal.getTimezone())
                    {
                        // Invalid request
                        throw new IndeterminateEvaluationException("Invalid value of attribute " + currentTimeAttName + "  in request context: " + timeCal + ". Not matching the value of attribute " + currentDateTimeAttName + ": " + dateTimeCal, XacmlStatusCode.SYNTAX_ERROR.value());
                    }
                }
            }
        }

        @Override
        public <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFQN, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
            assert attributeFQN != null && datatype != null && context != null && mdpContext != null;
            // reqOverride = false
    /*
     We assume that the AttributeProvider is called only if the requested attribute is not already set in {@code context}
     */
            if (attributeFQN.equals(StandardEnvironmentAttribute.CURRENT_DATETIME.getFQN()))
            {
                if (!datatype.equals(StandardDatatypes.DATETIME))
                {
                    throw new IndeterminateEvaluationException("Invalid Datatype requested for attribute " + attributeFQN + ": " + datatype + ". Expected: " + StandardDatatypes.DATETIME + " (mandatory per XACML standard)", XacmlStatusCode.PROCESSING_ERROR.value());
                }

        /*
         Input datatype is valid. Assume the value is not in {@code context}, else the PDP would not have needed to call this method.
         */
        /*
                     Try get it from mdpContext.
         */
                if (mdpContext.isPresent())
                {
                    final AttributeBag<AV> mdpCtxVal = mdpContext.get().getNamedAttributeValue(attributeFQN, datatype);
                    if (mdpCtxVal == null)
                    {
                        throw new IndeterminateEvaluationException("Attribute " + attributeFQN + " undefined in MDP evaluation context (possibly because the method StandardEnvironmentAttributeProvider#beginMultipleDecisionRequest(EvaluationContext) was not called)", XacmlStatusCode.PROCESSING_ERROR.value());
                    }

                    return mdpCtxVal;
                }

        /*
                    No mdpContext. Try recover the value from current-date and current-time from {@code context}.
         */
                final AttributeFqn currentDateAttName = StandardEnvironmentAttribute.CURRENT_DATE.getFQN();
                final AttributeBag<DateValue> dateBag = context.getNamedAttributeValue(currentDateAttName, StandardDatatypes.DATE);
                if (dateBag == null)
                {
            /*
            current-datetime and current-date undefined. Last option: we set current-datetime from context.getCreationTimestamp(), and for consistency, we set/override current-date and current-time in context as well
             */
                    LOGGER.debug("Requesting attribute {} but {} undefined.", attributeFQN, currentDateAttName);
                    overrideEvalCtxFromTimestamp(context);
                    return context.getNamedAttributeValue(attributeFQN, datatype);
                }

                if (dateBag.isEmpty())
                {
                    throw new IndeterminateEvaluationException("Invalid value of attribute " + currentDateAttName + "  in evaluation context: <empty>", XacmlStatusCode.PROCESSING_ERROR.value());
                }

                final AttributeFqn currentTimeAttName = StandardEnvironmentAttribute.CURRENT_TIME.getFQN();
                final AttributeBag<TimeValue> timeBag = context.getNamedAttributeValue(currentTimeAttName, StandardDatatypes.TIME);
                if (timeBag == null)
                {
            /*
            current-datetime and current-date undefined. Last option: we set current-datetime from context.getCreationTimestamp(), and for consistency, we set/override current-date and current-time in context as well
             */
                    LOGGER.debug("Request attribute {} but {} undefined.", attributeFQN, currentTimeAttName);
                    overrideEvalCtxFromTimestamp(context);
                    return context.getNamedAttributeValue(attributeFQN, datatype);
                }

                if (timeBag.isEmpty())
                {
                    throw new IndeterminateEvaluationException("Invalid value of attribute " + currentTimeAttName + "  in evaluation context: <empty>", XacmlStatusCode.PROCESSING_ERROR.value());
                }

        /*
         current-date and current-time non-null/non-empty
         Recover current dateTime from dateBag and timeBag using timezone from timeBag or UTC (zone offset = 0) by default if timeBag's timezone undefined
         */
                final XMLGregorianCalendar dateCal = dateBag.getSingleElement().getUnderlyingValue();
                final XMLGregorianCalendar timeCal = timeBag.getSingleElement().getUnderlyingValue();
                final XMLGregorianCalendar dateTimeCal = XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateCal.getEonAndYear(), dateCal.getMonth(), dateCal.getDay(), timeCal.getHour(), timeCal.getMinute(), timeCal.getSecond(), timeCal.getFractionalSecond(), timeCal.getTimezone());
                final DateTimeValue currentDateTimeValue = new DateTimeValue(dateTimeCal);
                // Set in context to ensure consistency with current-date/time attributes
                context.putNamedAttributeValue(attributeFQN, Bags.singletonAttributeBag(StandardDatatypes.DATETIME, currentDateTimeValue, AttributeSources.PDP), true);
                return context.getNamedAttributeValue(attributeFQN, datatype);
            }

            if (attributeFQN.equals(StandardEnvironmentAttribute.CURRENT_DATE.getFQN()))
            {
                return getCurrentDateOrTime(attributeFQN, datatype, StandardDatatypes.DATE, context, mdpContext);
            }

            if (attributeFQN.equals(StandardEnvironmentAttribute.CURRENT_TIME.getFQN()))
            {
                return getCurrentDateOrTime(attributeFQN, datatype, StandardDatatypes.TIME, context, mdpContext);
            }

            throw new UnsupportedOperationException(this + ": Unsupported attribute: " + attributeFQN);
        }
    };

    private final AttributeProviderHelper helper;

    /**
     * @param reqOverride defines whether the AttributeProvider should override attribute values present in the request:
     *                                     <ul>
     *                    	<li><i>true</i>: always override, i.e. this AttributeProvider sets the current-* attribute always - to the PDP's current dateTime (when the request is received by the PDP) - regardless of any value present in the request.</li>
     *                    	<li><i>false</i>: override or not, depending on the attributes present in the request:
     *                    		<ol>
     *                    			<li>If the standard <i>current-dateTime</i> attribute is present in the request, then:
     *                    				<ul>
     *                    					<li>If either <i>current-date</i> or <i>current-time</i> is present in the request and does not match current-dateTime (inconsistency): return Indeterminate.</li>
     *                    					<li>Else if either current-date or current-time is missing from the request, the AttributeProvider sets the attribute according to current-dateTime.</li>
     *                    					<li>Else (both are present) the request values are used as is.</li>
     *                    				</ul>
     *                    			</li>
     *                    			<li>Else (current-dateTime missing from the request):
     *                    				<ul>
     *                    					<li>If either current-date or current-time is missing from the request, the AttributeProvider sets the attribute to (the PDP's) current date/time.</li>
     *                    					<li>Else (both are present) the request values are used as is, and the <i>current-dateTime</i> is set (if required) according to those request values of current-date and current-time.</li>
     *                    				</ul>
     *                    			</li>
     *                    		</ol>
     *                    	</li>
     *                    </ul>
     */
    private StandardEnvironmentAttributeProvider(String id, boolean reqOverride)
    {
        super(id);
        this.helper = reqOverride ? ALWAYS_REQ_OVERRIDING_HELPER : OPTIONAL_REQ_OVERRIDING_HELPER;
    }

    @Override
    public void close()
    {
        // nothing to close
    }

    @Override
    public Set<AttributeDesignatorType> getProvidedAttributes()
    {
        return SUPPORTED_ATT_DESIGNATORS;
    }

    @Override
    public boolean supportsBeginMultipleDecisionRequest()
    {
        return true;
    }

    @Override
    public void beginMultipleDecisionRequest(final EvaluationContext mdpContext)
    {
        overrideEvalCtxFromTimestamp(mdpContext);
    }

    @Override
    public boolean supportsBeginIndividualDecisionRequest()
    {
        return true;
    }

    @Override
    public void beginIndividualDecisionRequest(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
    {
        Preconditions.checkArgument(context != null && mdpContext != null, "Invalid args");
        /*
         * Set the standard current date/time attribute according to XACML core spec:
         * "This identifier indicates the current time at the context handler. In practice, it is the time at which the request context was created." (§B.7). XACML standard (§10.2.5) says: "If values
         * for these attributes are not present in the decision request, then their values MUST be supplied by the context handler".
         */
        this.helper.beginIndividualDecisionRequest(context, mdpContext);
    }

    @Override
    public <AV extends AttributeValue> AttributeBag<AV> get(final AttributeFqn attributeFQN, final Datatype<AV> datatype, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
    {
        Preconditions.checkArgument(attributeFQN != null && datatype != null && context != null && mdpContext != null, "Invalid args");

        return this.helper.get(attributeFQN, datatype, context, mdpContext);
    }


    private static final class DepAwareFactory implements DependencyAwareFactory
    {
        private final String createdInstanceId;
        private final boolean reqOverride;

        /**
         * @param requestOverride defines whether the AttributeProvider should override attribute values present in the request:
         *                                         <ul>
         *                        	<li><i>true</i>: always override, i.e. this AttributeProvider sets the current-* attribute always - to the PDP's current dateTime (when the request is received by the PDP) - regardless of any value present in the request.</li>
         *                        	<li><i>false</i>: override or not, depending on the attributes present in the request:
         *                        		<ol>
         *                        			<li>If the standard <i>current-dateTime</i> attribute is present in the request, then:
         *                        				<ul>
         *                        					<li>If either <i>current-date</i> or <i>current-time</i> is present in the request and does not match current-dateTime (inconsistency): return Indeterminate.</li>
         *                        					<li>Else if either current-date or current-time is missing from the request, the AttributeProvider sets the attribute according to current-dateTime.</li>
         *                        					<li>Else (both are present) the request values are used as is.</li>
         *                        				</ul>
         *                        			</li>
         *                        			<li>Else (current-dateTime missing from the request):
         *                        				<ul>
         *                        					<li>If either current-date or current-time is missing from the request, the AttributeProvider sets the attribute to (the PDP's) current date/time.</li>
         *                        					<li>Else (both are present) the request values are used as is, and the <i>current-dateTime</i> is set (if required) according to those request values of current-date and current-time.</li>
         *                        				</ul>
         *                        			</li>
         *                        		</ol>
         *                        	</li>
         *                        </ul>
         */
        private DepAwareFactory(String id, boolean requestOverride)
        {
            assert id != null;
            this.createdInstanceId = id;
            this.reqOverride = requestOverride;
        }

        @Override
        public Set<AttributeDesignatorType> getDependencies()
        {
            // no dependency
            return Set.of();
        }

        @Override
        public CloseableNamedAttributeProvider getInstance(AttributeValueFactoryRegistry attributeValueFactoryRegistry, NamedAttributeProvider attributeProvider)
        {
            return new StandardEnvironmentAttributeProvider(this.createdInstanceId, this.reqOverride);
        }
    }

    /**
     * Default provider ID, i.e. ID of AttributeProvider instance(s) created by {@link #DEFAULT_FACTORY}
     */
    public static final String DEFAULT_ID ="_authzforce_pdp_attribute-provider_std-env";
    /**
     * Default factory, XACML-compliant (override='false'), creates attribute provider instances with id={@link #DEFAULT_ID }
     */
    public static final CloseableNamedAttributeProvider.DependencyAwareFactory DEFAULT_FACTORY = new DepAwareFactory(DEFAULT_ID, false);

    /**
     * AttributeProvider factory
     */
    public static final class Factory extends CloseableNamedAttributeProvider.FactoryBuilder<StdEnvAttributeProviderDescriptor>
    {

        @Override
        public Class<StdEnvAttributeProviderDescriptor> getJaxbClass()
        {
            return StdEnvAttributeProviderDescriptor.class;
        }

        @Override
        public DependencyAwareFactory getInstance(final StdEnvAttributeProviderDescriptor conf, final EnvironmentProperties environmentProperties)
        {
            return new DepAwareFactory(conf.getId(), conf.isOverride());
        }

    }

}
