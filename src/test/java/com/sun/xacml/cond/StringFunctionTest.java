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

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
@Deprecated
public class StringFunctionTest extends AbstractFunctionTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:2.0:function:string-concatenate
				new Object[] {
						StringFunction.NAME_STRING_CONCATENATE,
						Arrays.asList(StringAttribute.getInstance("foo"),
								StringAttribute.getInstance("bar")),
						Status.STATUS_OK, StringAttribute.getInstance("foobar") },
						new Object[] {
								StringFunction.NAME_STRING_CONCATENATE,
								Arrays.asList(
										StringAttribute.getInstance("foo"),
										StringAttribute.getInstance(""),
										StringAttribute.getInstance("bar")),
								Status.STATUS_OK,
								StringAttribute.getInstance("foobar") },

						// urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
						new Object[] {
								StringFunction.NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("true")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								StringFunction.NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("false")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								StringFunction.NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("error")),
								Status.STATUS_SYNTAX_ERROR, null });
	}

	public StringFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
