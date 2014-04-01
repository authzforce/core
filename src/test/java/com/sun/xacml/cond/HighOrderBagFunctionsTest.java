/**
 * 
 */
package com.sun.xacml.cond;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class HighOrderBagFunctionsTest extends AbstractFunctionTest {

	// private static final String NAME_ANY_OF =
	// "urn:oasis:names:tc:xacml:3.0:function:any-of";
	private static final String NAME_ANY_OF = "urn:oasis:names:tc:xacml:1.0:function:any-of";
	private static final String NAME_ALL_OF = "urn:oasis:names:tc:xacml:3.0:function:all-of";
	private static final String NAME_ANY_OF_ANY = "urn:oasis:names:tc:xacml:3.0:function:any-of-any";
	private static final String NAME_ALL_OF_ANY = "urn:oasis:names:tc:xacml:1.0:function:all-of-any";
	private static final String NAME_ANY_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:any-of-all";
	private static final String NAME_ALL_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:all-of-all";
	private static final String NAME_MAP = "urn:oasis:names:tc:xacml:3.0:function:map";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:3.0:function:any-of
				new Object[] {
						NAME_ANY_OF,
						Arrays.asList(
								EqualFunction
										.getEqualInstance(
												"urn:oasis:names:tc:xacml:1.0:function:string-equal",
												StringAttribute.identifier),
								StringAttribute.getInstance("Paul"),
								new Apply(
										BagFunction
												.getBagInstance(
														"urn:oasis:names:tc:xacml:1.0:function:string-bag",
														StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("John"),
												(ExpressionType) StringAttribute
														.getInstance("Paul"),
												(ExpressionType) StringAttribute
														.getInstance("George"),
												(ExpressionType) StringAttribute
														.getInstance("Ringo")))),
						Status.STATUS_OK, BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_ALL_OF_ANY,
								Arrays.asList(
										EqualFunction
												.getEqualInstance(
														"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
														StringAttribute.identifier),
										new BagAttribute(
												IntegerAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) IntegerAttribute
																.getInstance("10"),
														IntegerAttribute
																.getInstance("20"))),
										new BagAttribute(
												IntegerAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) IntegerAttribute
																.getInstance("1"),
														IntegerAttribute
																.getInstance("3"),
														IntegerAttribute
																.getInstance("5"),
														IntegerAttribute
																.getInstance("19")))),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) });
	}

	public HighOrderBagFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}


}
