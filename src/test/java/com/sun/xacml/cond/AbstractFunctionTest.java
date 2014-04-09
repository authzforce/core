/**
 * 
 */
package com.sun.xacml.cond;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.Test;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * An abstract class to easily test a function evaluation, according to a given
 * function name, a list of arguments, and expected result. In order to perform
 * a function test, simply extend this class and give the test values on
 * construction.
 * 
 * @author Cyrille MARTINS (Thales)
 * 
 */
public abstract class AbstractFunctionTest {

	private final String functionName;
	private final List<Evaluatable> inputs;
	private final EvaluationResult expectedResult;

	private static final EvaluationCtx CTX = TestUtils
			.createContext(new Request());

	/**
	 * A factory to create functions
	 */
	protected static final FunctionFactory FUNCTION_FACTORY = FunctionFactory
			.getGeneralInstance();

	/**
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The
	 *            function must be supported by the StandardFunctionFactory.
	 * @param inputs
	 *            The list of the function arguments, in order.
	 * @param expectedResult
	 *            The expected function evaluation result, according to the
	 *            given inputs
	 */
	protected AbstractFunctionTest(final String functionName,
			final List<Evaluatable> inputs,
			final EvaluationResult expectedResult) throws Exception {
		this.functionName = functionName;
		this.inputs = inputs;
		this.expectedResult = expectedResult;
	}

	@Test
	public void testEvaluate() throws Exception {
		// Execution
		Function function = FUNCTION_FACTORY.createFunction(functionName);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		EvaluationResult actualResult = function.evaluate((List) inputs, CTX);

		// Assertions
		Assert.assertEquals("Indeterminate", expectedResult.indeterminate(),
				actualResult.indeterminate());

		if (expectedResult.getStatus() != null) {
			Assert.assertNotNull("Status", actualResult.getStatus());
			// Compare status codes
			Status expectedStatus = expectedResult.getStatus();
			Status actualStatus = actualResult.getStatus();
			Assert.assertTrue("Status codes", actualStatus.getCode()
					.containsAll(expectedStatus.getCode()));
		} else {
			Assert.assertNull("Status", actualResult.getStatus());
		}

		if (expectedResult.getAttributeValue() != null) {
			Assert.assertNotNull("Attribute value",
					actualResult.getAttributeValue());
			if (expectedResult.getAttributeValue() instanceof BagAttribute) {
				// Compare bag content as sets (regardless of order)
				BagAttribute expectedBag = (BagAttribute) expectedResult
						.getAttributeValue();
				BagAttribute actualBag = (BagAttribute) actualResult
						.getAttributeValue();
				Set<AttributeValue> expectedValues = new HashSet<>(
						expectedBag.getValues());
				Set<AttributeValue> actualValues = new HashSet<>(
						actualBag.getValues());
				Assert.assertEquals("Bag attribute content", expectedValues,
						actualValues);
			} else {
				// Compare attribute values
				Assert.assertEquals("Attribute value",
						expectedResult.getAttributeValue(),
						actualResult.getAttributeValue());
			}
		} else {
			Assert.assertNull("Attribute value",
					actualResult.getAttributeValue());
		}
	}
}
