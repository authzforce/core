package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;

@RunWith(Parameterized.class)
public class LogicalFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_OR = "urn:oasis:names:tc:xacml:1.0:function:or";
	private static final String NAME_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
	private static final String NAME_N_OF = "urn:oasis:names:tc:xacml:1.0:function:n-of";
	private static final String NAME_NOT = "urn:oasis:names:tc:xacml:1.0:function:not";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:or
				new Object[] { NAME_OR, Arrays.asList(),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_OR,
						Arrays.asList(BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_OR,
						Arrays.asList(BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:and
				new Object[] { NAME_AND, Arrays.asList(),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_AND,
						Arrays.asList(BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true)),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_AND,
						Arrays.asList(BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(true)),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:n-of
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("0"),
								BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("2"),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(false),
								BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("2"),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("2"),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true)),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("4"),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true)),
						new EvaluationResult(new Status(Arrays
								.asList(Status.STATUS_PROCESSING_ERROR))) },
				new Object[] {
						NAME_N_OF,
						Arrays.asList(IntegerAttribute.getInstance("-1"),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true),
								BooleanAttribute.getInstance(true)),
						new EvaluationResult(new Status(Arrays
								.asList(Status.STATUS_PROCESSING_ERROR))) },

				// urn:oasis:names:tc:xacml:1.0:function:not
				new Object[] { NAME_NOT,
						Arrays.asList(BooleanAttribute.getInstance(true)),
						EvaluationResult.getInstance(false) },
				new Object[] { NAME_NOT,
						Arrays.asList(BooleanAttribute.getInstance(false)),
						EvaluationResult.getInstance(true) });
	}

	public LogicalFunctionsTest(String functionName, List<Evaluatable> inputs,
			EvaluationResult expectedResult) throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
