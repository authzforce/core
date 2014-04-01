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

import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class SpecialMatchFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_X500NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:x500Name-match";
	private static final String NAME_RFC822NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:x500Name-match
				new Object[] {
						NAME_X500NAME_MATCH,
						Arrays.asList(
								X500NameAttribute
										.getInstance("O=Medico Corp,C=US"),
								X500NameAttribute
										.getInstance("cn=John Smith,o=Medico Corp, c=US")),
						EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_MATCH,
								Arrays.asList(
										X500NameAttribute
												.getInstance("O=Another Corp,C=US"),
										X500NameAttribute
												.getInstance("cn=John Smith,o=Medico Corp, c=US")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@sun.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@SUN.COM")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anne.Anderson@sun.com")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("anderson@sun.com")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@east.sun.com")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute.getInstance("sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@sun.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(StringAttribute
										.getInstance("sun.com"),
										RFC822NameAttribute
												.getInstance("Baxter@SUN.COM")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute.getInstance("sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@east.sun.com")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance(".east.sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@east.sun.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance(".east.sun.com"),
										RFC822NameAttribute
												.getInstance("anne.anderson@ISRG.EAST.SUN.COM")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance(".east.sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@sun.com")),
								EvaluationResult.getInstance(false) });
	}

	public SpecialMatchFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}
}