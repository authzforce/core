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
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(true) });
	}

	public NumericComparisonFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}