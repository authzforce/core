/**
 * 
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class StringConversionFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_STRING_NORMALIZE_SPACE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-space";
	private static final String NAME_STRING_NORMALIZE_TO_LOWER_CASE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
				new Object[] {
						NAME_STRING_NORMALIZE_SPACE,
						Arrays.asList(StringAttribute.getInstance("test")),
						new EvaluationResult(StringAttribute
								.getInstance("test")) },
						new Object[] {
								NAME_STRING_NORMALIZE_SPACE,
								Arrays.asList(StringAttribute
										.getInstance("   test   ")),
								new EvaluationResult(StringAttribute
										.getInstance("test")) },

						// urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case
						new Object[] {
								NAME_STRING_NORMALIZE_TO_LOWER_CASE,
								Arrays.asList(StringAttribute
										.getInstance("test")),
								new EvaluationResult(StringAttribute
										.getInstance("test")) },
						new Object[] {
								NAME_STRING_NORMALIZE_TO_LOWER_CASE,
								Arrays.asList(StringAttribute
										.getInstance("TeST")),
								new EvaluationResult(StringAttribute
										.getInstance("test")) });
	}

	public StringConversionFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
