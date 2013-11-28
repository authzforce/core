package com.sun.xacml.xacmlv3;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * @author Romain Ferrari
 *
 */
public class AllOfTest {
	
	private static EvaluationCtx context;
	private static Request request = null;
	private static PolicySet policySet = null;
	private List<AllOf> allOf = null;
	private static Rule ruleNoMatch;
	private static Rule ruleMatch;
	
	private final static int ONE_MATCH = 0;
	private final static int MULTI_MATCH = 1;
	
	private final static int RULE_NO_MATCH = 0;
	private final static int RULE_MATCH = 1;
	
	@BeforeClass
	public static void setUp() {
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
	
	@Test
	public final void testMatch_noMatch() throws ParsingException {
		allOf = ruleNoMatch.getTarget().getAnyOves().get(ONE_MATCH).getAllOves();
		MatchResult result = null;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : allOf) {
			com.sun.xacml.xacmlv3.AllOf myAllOf = com.sun.xacml.xacmlv3.AllOf.getInstance(jaxbAllOf, new PolicyMetaData());
			result = myAllOf.match(context);
		}
		Assert.assertEquals(MatchResult.NO_MATCH, result.getResult());
	}
	
	@Test
	public final void testMatch_match() throws ParsingException {
		allOf = ruleMatch.getTarget().getAnyOves().get(ONE_MATCH).getAllOves();
		MatchResult result = null;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : allOf) {
			com.sun.xacml.xacmlv3.AllOf myAllOf = com.sun.xacml.xacmlv3.AllOf.getInstance(jaxbAllOf, new PolicyMetaData());
			result = myAllOf.match(context);
		}
		Assert.assertEquals(MatchResult.MATCH, result.getResult());
	}
	
	@Test
	public final void testMatchMultiple_NoMatch() throws ParsingException {
		allOf = ruleNoMatch.getTarget().getAnyOves().get(MULTI_MATCH).getAllOves();
		MatchResult result = null;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : allOf) {
			com.sun.xacml.xacmlv3.AllOf myAllOf = com.sun.xacml.xacmlv3.AllOf.getInstance(jaxbAllOf, new PolicyMetaData());
			result = myAllOf.match(context);
		}
		Assert.assertEquals(MatchResult.NO_MATCH, result.getResult());
	}
	
	@Test
	public final void testMatchMultiple_Match() throws ParsingException {
		allOf = ruleMatch.getTarget().getAnyOves().get(MULTI_MATCH).getAllOves();
		MatchResult result = null;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : allOf) {
			com.sun.xacml.xacmlv3.AllOf myAllOf = com.sun.xacml.xacmlv3.AllOf.getInstance(jaxbAllOf, new PolicyMetaData());
			result = myAllOf.match(context);
			if(result.getResult() == MatchResult.MATCH) break;
		}
		Assert.assertEquals(MatchResult.MATCH, result.getResult());
	}
}