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
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

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
				new Object[] { NAME_STRING_NORMALIZE_SPACE,
						Arrays.asList(StringAttribute.getInstance("test")),
						Status.STATUS_OK, StringAttribute.getInstance("test") },
						new Object[] {
								NAME_STRING_NORMALIZE_SPACE,
								Arrays.asList(StringAttribute
										.getInstance("   test   ")),
								Status.STATUS_OK,
								StringAttribute.getInstance("test") },

						// urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case
						new Object[] {
								NAME_STRING_NORMALIZE_TO_LOWER_CASE,
								Arrays.asList(StringAttribute
										.getInstance("test")),
								Status.STATUS_OK,
								StringAttribute.getInstance("test") },
						new Object[] {
								NAME_STRING_NORMALIZE_TO_LOWER_CASE,
								Arrays.asList(StringAttribute
										.getInstance("TeST")),
								Status.STATUS_OK,
								StringAttribute.getInstance("test") });
	}

	public StringConversionFunctionsTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
