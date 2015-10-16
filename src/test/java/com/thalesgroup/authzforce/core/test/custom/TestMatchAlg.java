/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.test.custom;

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

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

public class TestMatchAlg
{

	private final static int INDEX_OF_RULE_WITH_NOT_MATCHED_ANYOF = 0;
	private final static int INDEX_OF_RULE_WITH_MATCHED_ANYOF = 1;

	private static EvaluationContext context;
	private static Rule ruleWithNotMatchedAnyOf;
	private static Rule ruleWithMatchedAnyOf;

	private final static int INDEX_OF_ANYOF_WITH_ONE_ALLOF_ONE_MATCH = 0;
	private final static int INDEX_OF_ANYOF_WITH_ONE_ALLOF_MULTI_MATCH = 1;
	private final static int INDEX_OF_ANYOF_WITH_MULTI_ALLOF_ONE_MATCH = 2;

	/*
	 * LOGGER used for all class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMatchAlg.class);

	@BeforeClass
	public static void setUp() throws IndeterminateEvaluationException, JAXBException
	{
		LOGGER.info("Starting match algorithm test of AllOf, AnyOf and Match elements");
		URL requestFile = Thread.currentThread().getContextClassLoader().getResource("custom" + File.separator + "requestAllOfTest.xml");
		URL policyFile = Thread.currentThread().getContextClassLoader().getResource("custom" + File.separator + "PolicySetCustomTest.xml");

		Request request = (Request) JAXBContext.newInstance(Request.class).createUnmarshaller().unmarshal(requestFile);

		PolicySet policySet = (PolicySet) JAXBContext.newInstance(PolicySet.class).createUnmarshaller().unmarshal(policyFile);

		context = TestUtils.createContext(request);
		ruleWithNotMatchedAnyOf = (Rule) ((Policy) policySet.getPolicySetsAndPoliciesAndPolicySetIdReferences().get(0)).getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions().get(INDEX_OF_RULE_WITH_NOT_MATCHED_ANYOF);
		ruleWithMatchedAnyOf = (Rule) ((Policy) policySet.getPolicySetsAndPoliciesAndPolicySetIdReferences().get(0)).getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions().get(INDEX_OF_RULE_WITH_MATCHED_ANYOF);
	}

	/**
	 * Testing the AllOf evaluation algorithm for request expected NOT to match
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testNoMatchAllOfWithOneMatch() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithNotMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_ONE_ALLOF_ONE_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertFalse("AllOf (with 1 <Match>) evaluation algorithm -> MATCH (expected: NO_MATCH)", isMatched);
	}

	/**
	 * Testing the AllOf evaluation algorithm for request expected to match
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testMatchAllOfWithOneMatch() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_ONE_ALLOF_ONE_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertTrue("AllOf (with 1 <Match>) evaluation algorithm -> NO_MATCH (expected: MATCH)", isMatched);
	}

	/**
	 * Testing the Match evaluation algorithm: multiple Match elements in AllOf; expected to return
	 * NO_MATCH
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testNoMatchAllOfWithMultiMatch() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithNotMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_ONE_ALLOF_MULTI_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertFalse("AllOf with multiple <Match>es evaluation -> MATCH (expected: NO_MATCH)", isMatched);
	}

	/**
	 * Testing the Match evaluation algorithm: multiple Match elements in AllOf; expected to return
	 * MATCH
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testMatchAllOfWithMultiMatch() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_ONE_ALLOF_MULTI_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertTrue("AllOf with multiple <Match>es evaluation -> NO_MATCH (expected: MATCH)", isMatched);
	}

	/**
	 * Testing the AnyOf evaluation algorithm: multiple AllOf elements; expected to return NO_MATCH
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testNoMatchAnyOf() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithNotMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_MULTI_ALLOF_ONE_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertFalse("AnyOf with multiple <AllOf>es evaluation -> MATCH (expected: NO_MATCH)", isMatched);
	}

	/**
	 * Testing the AnyOf evaluation algorithm: multiple AllOf elements; expected to return MATCH
	 * 
	 * @throws ParsingException
	 * @throws IndeterminateEvaluationException
	 */
	@Test
	public final void testMatchAnyOf() throws ParsingException, IndeterminateEvaluationException
	{
		AnyOf jaxbAnyOf = ruleWithMatchedAnyOf.getTarget().getAnyOves().get(INDEX_OF_ANYOF_WITH_MULTI_ALLOF_ONE_MATCH);
		com.thalesgroup.authzforce.core.AnyOf anyOf = new com.thalesgroup.authzforce.core.AnyOf(jaxbAnyOf, null, TestUtils.STD_EXPRESSION_FACTORY);
		boolean isMatched = anyOf.match(context);
		Assert.assertTrue("AnyOf with multiple <Allof>es evaluation -> NO_MATCH (expected: MATCH)", isMatched);
	}
}