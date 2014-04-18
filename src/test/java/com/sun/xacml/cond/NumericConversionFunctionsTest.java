package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class NumericConversionFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_DOUBLE_TO_INTEGER = "urn:oasis:names:tc:xacml:1.0:function:double-to-integer";
	private static final String NAME_INTEGER_TO_DOUBLE = "urn:oasis:names:tc:xacml:1.0:function:integer-to-double";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:double-to-integer
				new Object[] { NAME_DOUBLE_TO_INTEGER,
						Arrays.asList(DoubleAttribute.getInstance("5.25")),
						new EvaluationResult(IntegerAttribute.getInstance("5")) },
						new Object[] {
								NAME_DOUBLE_TO_INTEGER,
								Arrays.asList(DoubleAttribute
										.getInstance("5.75")),
								new EvaluationResult(IntegerAttribute
										.getInstance("5")) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-to-double
						new Object[] {
								NAME_INTEGER_TO_DOUBLE,
								Arrays.asList(IntegerAttribute.getInstance("5")),
								new EvaluationResult(DoubleAttribute
										.getInstance("5.")) });
	}

	public NumericConversionFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
