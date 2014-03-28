package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class NumericComparisonFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_INTEGER_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than";
	private static final String NAME_INTEGER_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal";
	private static final String NAME_INTEGER_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than";
	private static final String NAME_INTEGER_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal";
	private static final String NAME_DOUBLE_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than";
	private static final String NAME_DOUBLE_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal";
	private static final String NAME_DOUBLE_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-less-than";
	private static final String NAME_DOUBLE_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						Status.STATUS_OK, BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_INTEGER_GREATER_THAN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_INTEGER_GREATER_THAN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
						new Object[] {
								NAME_INTEGER_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_INTEGER_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_INTEGER_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-less-than
						new Object[] {
								NAME_INTEGER_LESS_THAN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_INTEGER_LESS_THAN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_INTEGER_LESS_THAN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
						new Object[] {
								NAME_INTEGER_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_INTEGER_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_INTEGER_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:double-greater-than
						new Object[] {
								NAME_DOUBLE_GREATER_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_GREATER_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_DOUBLE_GREATER_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
						new Object[] {
								NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:double-less-than
						new Object[] {
								NAME_DOUBLE_LESS_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_DOUBLE_LESS_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_LESS_THAN,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
						new Object[] {
								NAME_DOUBLE_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.4")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_DOUBLE_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.6")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_LESS_THAN_OR_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("5.5"),
										DoubleAttribute.getInstance("5.5")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) });
	}

	public NumericComparisonFunctionsTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
