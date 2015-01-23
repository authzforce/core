/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class SpecialMatchFunctionsTest extends GeneralFunctionTest {

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
			List<ExpressionType> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}
}
