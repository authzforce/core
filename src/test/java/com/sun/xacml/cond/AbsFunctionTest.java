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

import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class AbsFunctionTest extends AbstractFunctionTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-abs
				new Object[] { AbsFunction.NAME_INTEGER_ABS,
						Arrays.asList(IntegerAttribute.getInstance("45")),
						Status.STATUS_OK, IntegerAttribute.getInstance("45") },
						new Object[] {
								AbsFunction.NAME_INTEGER_ABS,
								Arrays.asList(IntegerAttribute
										.getInstance("-56")), Status.STATUS_OK,
								IntegerAttribute.getInstance("56") },

						// urn:oasis:names:tc:xacml:1.0:function:double-abs
						new Object[] {
								AbsFunction.NAME_DOUBLE_ABS,
								Arrays.asList(DoubleAttribute
										.getInstance("45.734")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("45.734") },
						new Object[] {
								AbsFunction.NAME_DOUBLE_ABS,
								Status.STATUS_OK,
								Arrays.asList(DoubleAttribute
										.getInstance("-56.")),
								DoubleAttribute.getInstance("56.") });
	}

	public AbsFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
