package com.sun.xacml.xacmlv3.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.cond.MatchFunction;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * 
 * @author romain.ferrari[AT]thalesgroup.com
 *
 */
public class TestMatchFunction {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMatchFunction.class);
	
	private static final String FUNCTION_NS = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";
	private static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";
	
	private static final EvaluationCtx globalContext = TestUtils.createContext(new Request());

	/**
	 * Standard identifier for the regexp-string-match function.
	 */
	public static final String NAME_REGEXP_STRING_MATCH = FUNCTION_NS
			+ "regexp-string-match";

	/**
	 * Standard identifier for the x500Name-match function.
	 */
	public static final String NAME_X500NAME_MATCH = FUNCTION_NS
			+ "x500Name-match";

	/**
	 * Standard identifier for the rfc822Name-match function.
	 */
	public static final String NAME_RFC822NAME_MATCH = FUNCTION_NS
			+ "rfc822Name-match";

	/**
	 * Standard identifier for the string-regexp-match function. NOTE: this in
	 * the 1.0 namespace right now because of a bug in the XACML 2.0
	 * specification, but this will be changed to the 2.0 namespace as soon as
	 * the errata is recognized.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = FUNCTION_NS
			+ "string-regexp-match";

	/**
	 * Standard identifier for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = FUNCTION_NS_2
			+ "anyURI-regexp-match";

	/**
	 * Standard identifier for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = FUNCTION_NS_2
			+ "ipAddress-regexp-match";

	/**
	 * Standard identifier for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "dnsName-regexp-match";

	/**
	 * Standard identifier for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "rfc822Name-regexp-match";

	/**
	 * Standard identifier for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "x500Name-regexp-match";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = FUNCTION_NS_3
			+ "string-starts-with";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = FUNCTION_NS_3
			+ "string-ends-with";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_CONTAINS = FUNCTION_NS_3
			+ "string-contains";

	@Before
	public void setUp() throws Exception {
		LOGGER.info("Begining testing for MatchFunctions");
		
		Set<String> testFunctions = new HashSet<String>();
		testFunctions.add(NAME_REGEXP_STRING_MATCH);
		testFunctions.add(NAME_X500NAME_MATCH);
		testFunctions.add(NAME_RFC822NAME_MATCH);
		testFunctions.add(NAME_STRING_REGEXP_MATCH);
		testFunctions.add(NAME_ANYURI_REGEXP_MATCH);
		testFunctions.add(NAME_IPADDRESS_REGEXP_MATCH);
		testFunctions.add(NAME_DNSNAME_REGEXP_MATCH);
		testFunctions.add(NAME_RFC822NAME_REGEXP_MATCH);
		testFunctions.add(NAME_X500NAME_REGEXP_MATCH);
		testFunctions.add(NAME_STRING_STARTS_WITH);
		testFunctions.add(NAME_STRING_ENDS_WITH);
		testFunctions.add(NAME_STRING_CONTAINS);
		
		if(LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Function to be tested");
	        for (String functionToBeTested : testFunctions) {
	        	LOGGER.debug(functionToBeTested);	
			}
        }
	}

	@Test
	public final void testRegexpStringMatch() {
		LOGGER.info("Testing function: " + NAME_REGEXP_STRING_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_REGEXP_STRING_MATCH);
		List<StringAttribute> goodInputs = new ArrayList<StringAttribute>(Arrays.asList(StringAttribute.getInstance("string$"), StringAttribute.getInstance("This is my test string")));
		List<StringAttribute> wrongInputs = new ArrayList<StringAttribute>(Arrays.asList(StringAttribute.getInstance("hello$"), StringAttribute.getInstance("This is my test string")));
				
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_REGEXP_STRING_MATCH + ": OK");
	}
	
	@Test
	public final void testX500NameMatch() {
						
		X500Principal x500PrincipalArg0Good = new X500Principal("O=Medico Corp,C=US");
		X500NameAttribute x500NameAttributeArg0Good = new X500NameAttribute(x500PrincipalArg0Good);
		
		X500Principal x500PrincipalArg0Wrong = new X500Principal("O=Medico Corp,C=FR");
		X500NameAttribute x500NameAttributeArg0Wrong = new X500NameAttribute(x500PrincipalArg0Wrong);
		
		X500Principal x500PrincipalArg1 = new X500Principal("cn=John Smith,o=Medico Corp, c=US");
		X500NameAttribute x500NameAttributeArg1 = new X500NameAttribute(x500PrincipalArg1);
		
		LOGGER.info("Testing function: " + NAME_X500NAME_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_X500NAME_MATCH);
		
		List<X500NameAttribute> goodInputs = new ArrayList<X500NameAttribute>();
		goodInputs.add(x500NameAttributeArg0Good);
		goodInputs.add(x500NameAttributeArg1);

		List<X500NameAttribute> wrongInputs = new ArrayList<X500NameAttribute>();
		wrongInputs.add(x500NameAttributeArg0Wrong);
		wrongInputs.add(x500NameAttributeArg1);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_X500NAME_MATCH + ": OK");
	}

	@Test
	public final void testCheckInputsList() {
		// fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testCheckInputsNoBagList() {
		// fail("Not yet implemented"); // TODO
	}

}
