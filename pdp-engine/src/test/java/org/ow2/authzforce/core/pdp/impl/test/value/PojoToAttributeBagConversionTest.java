/**
 * Copyright 2012-2018 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.test.value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * 
 * Tests conversion from raw Java types to XACML datatype (attribute value)
 */
@RunWith(value = Parameterized.class)
public class PojoToAttributeBagConversionTest
{
	@Parameters
	public static Collection<Object[]> data()
	{
		final Object[][] data = new Object[][] {
		/* string type */
		{ "string", StandardDatatypes.STRING },
		/*
		 * date
		 */
		/*
		 * matching type (LocalDate)
		 */
		{ LocalDate.now(), StandardDatatypes.DATE },
		/* subtype (of Date) */
		{ java.sql.Date.valueOf(LocalDate.now()), StandardDatatypes.DATE } };
		/*
		 * TODO: others
		 */
		return Arrays.asList(data);
	}

	private final Serializable value;
	private final AttributeDatatype<?> expectedXacmlDatatype;

	public PojoToAttributeBagConversionTest(final Serializable rawValue, final AttributeDatatype<?> expectedReturnedXacmlDataType)
	{
		this.value = rawValue;
		this.expectedXacmlDatatype = expectedReturnedXacmlDataType;
	}

	@Test
	public void testSingleValue()
	{
		// TODO
	}

	@Test
	public void testCollection()
	{
		// TODO
	}

}
