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
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-add
				// TODO: Indeterminate case
				new Object[] { AddFunction.NAME_INTEGER_ADD,
						Arrays.asList(new IntegerAttribute(45)),
						Status.STATUS_OK, new IntegerAttribute(45) },
				new Object[] {
						AddFunction.NAME_INTEGER_ADD,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(-56),
								new IntegerAttribute(0),
								new IntegerAttribute(3)), Status.STATUS_OK,
						new IntegerAttribute(-8) },

				// urn:oasis:names:tc:xacml:1.0:function:double-add
				// TODO: Indeterminate case
				new Object[] { AddFunction.NAME_DOUBLE_ADD,
						Arrays.asList(new DoubleAttribute(45.734)),
						Status.STATUS_OK, new DoubleAttribute(45.734) },
				new Object[] {
						AddFunction.NAME_DOUBLE_ADD,
						Arrays.asList(new DoubleAttribute(45.734),
								new DoubleAttribute(-56.), new DoubleAttribute(
										0.), new DoubleAttribute(3.33)),
						Status.STATUS_OK, new DoubleAttribute(-7.296) });
	}

	public AddFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}
}
