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
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class ComparisonFunctionTest extends AbstractFunctionTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() {
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(44)), Status.STATUS_OK,
						BooleanAttribute.getInstance(true) },
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(46)), Status.STATUS_OK,
						BooleanAttribute.getInstance(false) },
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(45)), Status.STATUS_OK,
						BooleanAttribute.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(44)), Status.STATUS_OK,
						BooleanAttribute.getInstance(true) },
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(46)), Status.STATUS_OK,
						BooleanAttribute.getInstance(false) },
				new Object[] {
						ComparisonFunction.NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new IntegerAttribute(45),
								new IntegerAttribute(45)), Status.STATUS_OK,
						BooleanAttribute.getInstance(true) });
	}

	public ComparisonFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
