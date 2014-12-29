/**
 * 
 */
package com.sun.xacml.xacmlv3.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.StringFunction;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

public class TestStringFunction {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestStringFunction.class);

	private static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";
	private static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";
	
	/**
     * Standard identifier for the string-concatenate function.
     */
    public static final String NAME_STRING_CONCATENATE =
        FUNCTION_NS_2 + "string-concatenate";
    
    /**
     * Standard identifier for the string-concatenate function.
     */
    public static final String NAME_BOOLEAN_FROM_STRING = 
        FUNCTION_NS_3 + "boolean-from-string";

	private static final EvaluationCtx globalContext = TestUtils
			.createContext(new Request());

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() {
		LOGGER.info("Begining testing for String Functions");
		Set<String> testFunctions = new HashSet<String>();

        testFunctions.add(NAME_STRING_CONCATENATE);
        testFunctions.add(NAME_BOOLEAN_FROM_STRING);
        
        if(LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Function to be tested");
	        for (String functionToBeTested : testFunctions) {
	        	LOGGER.debug(functionToBeTested);	
			}
        }
	}

	@Test
	public final void testNameStringConcatenate() {
		LOGGER.info("Testing function: " + NAME_STRING_CONCATENATE);
		StringFunction testNameStringConcatenate = new StringFunction(NAME_STRING_CONCATENATE);
		List<AttributeValue> inputs = new ArrayList<AttributeValue>(Arrays.asList(
																	StringAttribute.getInstance("This "), 
																	StringAttribute.getInstance("is "), 
																	StringAttribute.getInstance("my "),
																	StringAttribute.getInstance("test !")));
		StringAttribute expected = new StringAttribute("This is my test !");
		Assert.assertEquals(expected, testNameStringConcatenate.evaluate(inputs, globalContext).getAttributeValue());
		
		LOGGER.info("Function: " + NAME_STRING_CONCATENATE + ": OK");
	}
	
	@Test
	public final void testNameBooleanFromString() {
		LOGGER.info("Testing function: " + NAME_BOOLEAN_FROM_STRING);
		StringFunction testNameBooleanFromString = new StringFunction(NAME_BOOLEAN_FROM_STRING);
		List<AttributeValue> inputTrue = new ArrayList<AttributeValue>(Arrays.asList(StringAttribute.getInstance("true")));
		List<AttributeValue> inputFalse = new ArrayList<AttributeValue>(Arrays.asList(StringAttribute.getInstance("false")));
		List<AttributeValue> inputError = new ArrayList<AttributeValue>(Arrays.asList(StringAttribute.getInstance("error")));
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testNameBooleanFromString.evaluate(inputTrue, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testNameBooleanFromString.evaluate(inputFalse, globalContext).getAttributeValue()).encode()));
		
		EvaluationResult errorResult = testNameBooleanFromString.evaluate(inputError, globalContext);
		Assert.assertEquals(Status.STATUS_SYNTAX_ERROR, errorResult.getStatus().getCode().get(0));
		
		LOGGER.info("Function: " + NAME_BOOLEAN_FROM_STRING + ": OK");
	}

}
