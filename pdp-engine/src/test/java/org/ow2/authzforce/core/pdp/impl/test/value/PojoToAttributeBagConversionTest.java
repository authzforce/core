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
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.value.StandardAttributeValueFactories;

/**
 * 
 * Tests conversion from raw Java types to XACML datatype (attribute value)
 */
@RunWith(value = Parameterized.class)
public class PojoToAttributeBagConversionTest
{
	@Parameters()
	public static Collection<Object[]> data()
	{
		final Object[][] data = new Object[][] {
				/* empty collection */
				{ Collections.emptyList(), StandardDatatypes.STRING.getId() },
				/* string type */
				{ Arrays.asList("string"), StandardDatatypes.STRING.getId() },
				/*
				 * date
				 */
				/*
				 * matching type (LocalDate)
				 */
				{ Arrays.asList(LocalDate.now()), StandardDatatypes.DATE.getId() },
				/* subtype (of Date) */
				{ Arrays.asList(java.sql.Date.valueOf(LocalDate.now())), StandardDatatypes.DATE.getId() },
				/*
				 * TODO: others
				 */
				/* invalid mix of datatypes */
				{ Arrays.asList(new Integer(0), LocalDate.now()), StandardDatatypes.DATE.getId() } };

		return Arrays.asList(data);
	}

	private final Collection<? extends Serializable> rawValues;
	private final String expectedAttributeDatatypeId;

	public PojoToAttributeBagConversionTest(final Collection<? extends Serializable> rawValues,
			final String expectedAttributeDatatypeId)
	{
		this.rawValues = rawValues;
		this.expectedAttributeDatatypeId = expectedAttributeDatatypeId;
	}

	@Test
	public void test()
	{
		if (rawValues.isEmpty())
		{

		}
		final AttributeValue attVal = StandardAttributeValueFactories.newAttributeValue(rawValue);
		Assert.assertEquals(attVal.getDataType(), expectedAttributeDatatypeId,
				"Unexpected datatype for created attribute value");
	}

}
