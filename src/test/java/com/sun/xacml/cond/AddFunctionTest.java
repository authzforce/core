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
public class AddFunctionTest extends AbstractFunctionTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-add
				// TODO: Indeterminate case
				new Object[] { AddFunction.NAME_INTEGER_ADD,
						Arrays.asList(IntegerAttribute.getInstance("45")),
						Status.STATUS_OK, IntegerAttribute.getInstance("45") },
						new Object[] {
								AddFunction.NAME_INTEGER_ADD,
								Arrays.asList(
										IntegerAttribute.getInstance("45"),
										IntegerAttribute.getInstance("-56"),
										IntegerAttribute.getInstance("0"),
										IntegerAttribute.getInstance("3")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("-8") },

						// urn:oasis:names:tc:xacml:1.0:function:double-add
						// TODO: Indeterminate case
						new Object[] {
								AddFunction.NAME_DOUBLE_ADD,
								Arrays.asList(DoubleAttribute
										.getInstance("45.734")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("45.734") },
						new Object[] {
								AddFunction.NAME_DOUBLE_ADD,
								Arrays.asList(
										DoubleAttribute.getInstance("45.734"),
										DoubleAttribute.getInstance("-56."),
										DoubleAttribute.getInstance("0."),
										DoubleAttribute.getInstance("3.33")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("-7.296") });
	}

	public AddFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}
}
