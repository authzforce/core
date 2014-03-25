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
public class ArithmeticFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_INTEGER_ADD = "urn:oasis:names:tc:xacml:1.0:function:integer-add";
	private static final String NAME_DOUBLE_ADD = "urn:oasis:names:tc:xacml:1.0:function:double-add";
	private static final String NAME_INTEGER_SUBTRACT = "urn:oasis:names:tc:xacml:1.0:function:integer-subtract";
	private static final String NAME_DOUBLE_SUBTRACT = "urn:oasis:names:tc:xacml:1.0:function:double-subtract";
	private static final String NAME_INTEGER_MULTIPLY = "urn:oasis:names:tc:xacml:1.0:function:integer-multiply";
	private static final String NAME_DOUBLE_MULTIPLY = "urn:oasis:names:tc:xacml:1.0:function:double-multiply";
	private static final String NAME_INTEGER_DIVIDE = "urn:oasis:names:tc:xacml:1.0:function:integer-divide";
	private static final String NAME_DOUBLE_DIVIDE = "urn:oasis:names:tc:xacml:1.0:function:double-divide";
	private static final String NAME_INTEGER_MOD = "urn:oasis:names:tc:xacml:1.0:function:integer-mod";
	private static final String NAME_INTEGER_ABS = "urn:oasis:names:tc:xacml:1.0:function:integer-abs";
	private static final String NAME_DOUBLE_ABS = "urn:oasis:names:tc:xacml:1.0:function:double-abs";
	private static final String NAME_ROUND = "urn:oasis:names:tc:xacml:1.0:function:round";
	private static final String NAME_FLOOR = "urn:oasis:names:tc:xacml:1.0:function:floor";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		// TODO: Indeterminate cases
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-add
				new Object[] {
						NAME_INTEGER_ADD,
						Arrays.asList(IntegerAttribute.getInstance("2"),
								IntegerAttribute.getInstance("1")),
						Status.STATUS_OK, IntegerAttribute.getInstance("3") },
						new Object[] {
								NAME_INTEGER_ADD,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("-1")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("1") },
						new Object[] {
								NAME_INTEGER_ADD,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("-1"),
										IntegerAttribute.getInstance("0"),
										IntegerAttribute.getInstance("1")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("2") },

						// urn:oasis:names:tc:xacml:1.0:function:double-add
						new Object[] {
								NAME_DOUBLE_ADD,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("2.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("4.") },
						new Object[] {
								NAME_DOUBLE_ADD,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("-2.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("-1.") },
						new Object[] {
								NAME_DOUBLE_ADD,
								Arrays.asList(
										DoubleAttribute.getInstance("1.25"),
										DoubleAttribute.getInstance("-2.75"),
										DoubleAttribute.getInstance("0."),
										DoubleAttribute.getInstance("2.")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("0.5") },

						// urn:oasis:names:tc:xacml:1.0:function:integer-subtract
						new Object[] {
								NAME_INTEGER_SUBTRACT,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("1")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("1") },
						new Object[] {
								NAME_INTEGER_SUBTRACT,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("-1")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("3") },

						// urn:oasis:names:tc:xacml:1.0:function:double-subtract
						new Object[] {
								NAME_DOUBLE_SUBTRACT,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("2.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("-1.") },
						new Object[] {
								NAME_DOUBLE_SUBTRACT,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("-2.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("4.") },
						new Object[] {
								NAME_DOUBLE_SUBTRACT,
								Arrays.asList(
										DoubleAttribute.getInstance("1.25"),
										DoubleAttribute.getInstance("-2.75"),
										DoubleAttribute.getInstance("0."),
										DoubleAttribute.getInstance("1.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("2.5") },

						// urn:oasis:names:tc:xacml:1.0:function:integer-multiply
						new Object[] {
								NAME_INTEGER_MULTIPLY,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("3")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("6") },
						new Object[] {
								NAME_INTEGER_MULTIPLY,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("0")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("0") },
						new Object[] {
								NAME_INTEGER_MULTIPLY,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										IntegerAttribute.getInstance("-1"),
										IntegerAttribute.getInstance("1")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("-2") },

						// urn:oasis:names:tc:xacml:1.0:function:double-multiply
						new Object[] {
								NAME_DOUBLE_MULTIPLY,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("2.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("3.75") },
						new Object[] {
								NAME_DOUBLE_MULTIPLY,
								Arrays.asList(
										DoubleAttribute.getInstance("1.5"),
										DoubleAttribute.getInstance("0")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("0") },
						new Object[] {
								NAME_DOUBLE_MULTIPLY,
								Arrays.asList(
										DoubleAttribute.getInstance("1.25"),
										DoubleAttribute.getInstance("-2.75"),
										DoubleAttribute.getInstance("1.5")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("-5.15625") },

						// urn:oasis:names:tc:xacml:1.0:function:integer-divide
						new Object[] {
								NAME_INTEGER_DIVIDE,
								Arrays.asList(
										IntegerAttribute.getInstance("6"),
										IntegerAttribute.getInstance("3")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("2") },
						new Object[] {
								NAME_INTEGER_DIVIDE,
								Arrays.asList(
										IntegerAttribute.getInstance("7"),
										IntegerAttribute.getInstance("-3")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("-2") },
						new Object[] {
								NAME_INTEGER_DIVIDE,
								Arrays.asList(
										IntegerAttribute.getInstance("0"),
										IntegerAttribute.getInstance("-3")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("0") },
						// urn:oasis:names:tc:xacml:1.0:function:double-divide
						// urn:oasis:names:tc:xacml:1.0:function:integer-mod
						// urn:oasis:names:tc:xacml:1.0:function:integer-abs
						new Object[] {
								NAME_INTEGER_ABS,
								Arrays.asList(IntegerAttribute
										.getInstance("45")), Status.STATUS_OK,
								IntegerAttribute.getInstance("45") },
						new Object[] {
								NAME_INTEGER_ABS,
								Arrays.asList(IntegerAttribute
										.getInstance("-56")), Status.STATUS_OK,
								IntegerAttribute.getInstance("56") },

						// urn:oasis:names:tc:xacml:1.0:function:double-abs
						new Object[] {
								NAME_DOUBLE_ABS,
								Arrays.asList(DoubleAttribute
										.getInstance("45.734")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("45.734") },
						new Object[] {
								NAME_DOUBLE_ABS,
								Status.STATUS_OK,
								Arrays.asList(DoubleAttribute
										.getInstance("-56.")),
								DoubleAttribute.getInstance("56.") }

				// urn:oasis:names:tc:xacml:1.0:function:round
				// urn:oasis:names:tc:xacml:1.0:function:floor
				);
	}

	public ArithmeticFunctionsTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
