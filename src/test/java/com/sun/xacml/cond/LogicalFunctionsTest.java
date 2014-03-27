package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class LogicalFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_OR = "urn:oasis:names:tc:xacml:1.0:function:or";
	private static final String NAME_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
	private static final String NAME_N_OF = "urn:oasis:names:tc:xacml:1.0:function:n-of";
	private static final String NAME_NOT = "urn:oasis:names:tc:xacml:1.0:function:not";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:or
				new Object[] { NAME_OR, Arrays.asList(), Status.STATUS_OK,
						BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_OR,
								Arrays.asList(
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_OR,
								Arrays.asList(
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(false)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:and
						new Object[] { NAME_AND, Arrays.asList(),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_AND,
								Arrays.asList(
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(true)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_AND,
								Arrays.asList(
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(true)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:n-of
						// TODO: Indeterminate case
						new Object[] {
								NAME_N_OF,
								Arrays.asList(
										IntegerAttribute.getInstance("0"),
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_N_OF,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_N_OF,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(false)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_N_OF,
								Arrays.asList(
										IntegerAttribute.getInstance("2"),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(true)),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:not
						new Object[] {
								NAME_NOT,
								Arrays.asList(BooleanAttribute
										.getInstance(true)), Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_NOT,
								Arrays.asList(BooleanAttribute
										.getInstance(false)), Status.STATUS_OK,
								BooleanAttribute.getInstance(true) });
	}

	public LogicalFunctionsTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
