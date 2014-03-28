/**
 * 
 */
package com.sun.xacml.cond;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * An abstract class to easily test a function evaluation, according to a given
 * function name, a list of arguments, and expected results. In order to perform
 * a function test, simply extend this class and give the test values on
 * construction.
 * 
 * @author Cyrille MARTINS (Thales)
 * 
 */
public abstract class AbstractFunctionTest {

	private final String functionName;
	private final List<Evaluatable> inputs;
	private final String expectedStatus;
	private final AttributeValue expectedValue;

	private static final FunctionFactory FUNCTION_FACTORY = StandardFunctionFactory
			.getGeneralInstance();
	private static final EvaluationCtx CTX = TestUtils
			.createContext(new Request());

	/**
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The
	 *            function must be supported by the StandardFunctionFactory.
	 * @param inputs
	 *            The list of the function arguments, in order.
	 * @param expectedStatus
	 *            The expected evaluation status of the function execution,
	 *            according to the given inputs.
	 * @param expectedValue
	 *            The expected evaluation result value of the function
	 *            execution, according to the given inputs.
	 */
	protected AbstractFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		this.functionName = functionName;
		this.inputs = inputs;
		this.expectedStatus = expectedStatus;
		this.expectedValue = expectedValue;
	}

	@Test
	public void testStatus() throws Exception {
		String actualStatus;

		// Assumptions
		Assume.assumeFalse(expectedStatus.equals(Status.STATUS_OK));

		// Execution
		Function function = FUNCTION_FACTORY.createFunction(functionName);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		EvaluationResult result = function.evaluate((List) inputs, CTX);
		actualStatus = result.getStatus().getCode().get(0);

		// Assertions
		Assert.assertEquals(expectedStatus, actualStatus);
	}

	@Test
	public void testResult() throws Exception {
		AttributeValue actualValue;

		// Assumptions
		Assume.assumeTrue(expectedStatus.equals(Status.STATUS_OK));

		// Execution
		Function function = FUNCTION_FACTORY.createFunction(functionName);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		EvaluationResult result = function.evaluate((List) inputs, CTX);
		actualValue = result.getAttributeValue();

		// Assertions
		Assert.assertEquals(expectedValue, actualValue);
	}
}
