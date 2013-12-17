package com.sun.xacml.xacmlv3;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.thalesgroup.authzforce.pdp.core.test.impl.MainTest;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * @author Romain Ferrari
 *
 */
public class TestMatchAlg {
	
	private static EvaluationCtx context;
	private static Request request = null;
	private static PolicySet policySet = null;
	private static Rule ruleNoMatch;
	private static Rule ruleMatch;
	
	private final static int ONE_ALL_OF = 0;
	private final static int MULTI_MATCH = 1;
	private final static int MULTI_ALL_OF = 2;
	
	private final static int RULE_NO_MATCH = 0;
	private final static int RULE_MATCH = 1;
	
	/*
	 * LOGGER used for all class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestMatchAlg.class);
	
	@BeforeClass
	public static void setUp() {
		LOGGER.info("Starting match algorithm test of AllOf, AnyOf and Match elements");
		try {
			URL requestFile = Thread.currentThread().getContextClassLoader().getResource("custom"+ File.separator +"requestAllOfTest.xml");
			URL policyFile = Thread.currentThread().getContextClassLoader().getResource("custom"+ File.separator +"PolicySetCustomTest.xml");
			
			request = (Request)JAXBContext.newInstance(Request.class).createUnmarshaller().unmarshal(requestFile);
			policySet = (PolicySet)JAXBContext.newInstance(PolicySet.class).createUnmarshaller().unmarshal(policyFile);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}
		context = TestUtils.createContext(request);
		ruleNoMatch = (Rule) ((Policy)policySet.getPolicySetsAndPoliciesAndPolicySetIdReferences().get(0)).getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions().get(RULE_NO_MATCH);		
		ruleMatch = (Rule) ((Policy)policySet.getPolicySetsAndPoliciesAndPolicySetIdReferences().get(0)).getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions().get(RULE_MATCH);
	}
	
	/**
	 * Testing the AllOf algorithm
	 * @throws ParsingException
	 */
	@Test
	public final void testMatchAllOf() throws ParsingException {
		LOGGER.info("Testing AllOf algorithm");
		AnyOf anyOf = ruleNoMatch.getTarget().getAnyOves().get(ONE_ALL_OF);
		MatchResult result = null;
		com.sun.xacml.xacmlv3.AnyOf myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("AllOf algorithm failed when looking for MATCH", MatchResult.NO_MATCH, result.getResult());
		
		anyOf = ruleMatch.getTarget().getAnyOves().get(ONE_ALL_OF);
		myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("AllOf algorithm failed when looking for MATCH", MatchResult.MATCH, result.getResult());
		LOGGER.info("AllOf algorithm: OK");
	}
	
	/**
	 * Testing the Match algorithm
	 * @throws ParsingException
	 */
	@Test
	public final void testMatchMatch() throws ParsingException {
		LOGGER.info("Testing Match algorithm");
		AnyOf anyOf = ruleNoMatch.getTarget().getAnyOves().get(MULTI_MATCH);
		MatchResult result = null;
		com.sun.xacml.xacmlv3.AnyOf myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("Match algorithm failed when looking for NO_MATCH", MatchResult.NO_MATCH, result.getResult());
		
		anyOf = ruleMatch.getTarget().getAnyOves().get(MULTI_MATCH);
		myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("Match algorithm failed when looking for MATCH", MatchResult.MATCH, result.getResult());
		LOGGER.info("Match algorithm: OK");
	}

	/**
	 * Testing the AnyOf algorithm
	 * @throws ParsingException
	 */
	@Test
	public final void testMatchAnyOf() throws ParsingException {
		LOGGER.info("Testing AnyOf algorithm");
		AnyOf anyOf = ruleNoMatch.getTarget().getAnyOves().get(MULTI_ALL_OF);
		MatchResult result = null;
		com.sun.xacml.xacmlv3.AnyOf myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("AnyOf algorithm failed when looking for MATCH", MatchResult.NO_MATCH, result.getResult());
		
		anyOf = ruleMatch.getTarget().getAnyOves().get(MULTI_ALL_OF);
		myAnyOf = com.sun.xacml.xacmlv3.AnyOf.getInstance(anyOf, new PolicyMetaData());
		result = myAnyOf.match(context);
		Assert.assertEquals("AnyOf algorithm failed when looking for MATCH", MatchResult.MATCH, result.getResult());
		LOGGER.info("AnyOf algorithm: OK");
	}
}