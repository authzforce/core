/**
 * Copyright 2012-2018 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EnvironmentPropertyName;
import org.ow2.authzforce.core.pdp.impl.DefaultEnvironmentProperties;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * Tests the placeholders replacement with {@link DefaultEnvironmentProperties} class.
 * <p>
 * WARNING: An environment variable AUTHZFORCE_DATA_DIR must be set to "/tmp/authzforce" for the test to succeed (e.g. with 'environmentVariables' configuration element of Maven Surefire plugin)
 */
@RunWith(value = Parameterized.class)
public class DefaultEnvironmentPropertiesTest
{

	@Parameters(name = "{index}: {1}; user={2}; sys={3} -> {5}")
	public static Collection<Object[]> data()
	{
		final Object[][] data = new Object[][] {
		        /**
		         * each test input is: input string, user-defined properties Map<EnvironmentPropertyName,String>, system properties as Map<String, String>, and expected output string
		         */
		        /* empty collection */
		        { "${PARENT_DIR}/a", ImmutableMap.of(EnvironmentPropertyName.PARENT_DIR, "/tmp"), null, "/tmp/a" },

		        /*
		         * System property replacement
		         */
		        { "${org.ow2.authzforce.data.dir}/b", null, ImmutableMap.of("org.ow2.authzforce.data.dir", "/tmp/authzforce"), "/tmp/authzforce/b" },

		        /*
		         * Environment variable PATH replacement. Environment variable AUTHZFORCE_DATA_DIR must be set to "/tmp/authzforce" (e.g. with 'environmentVariables' configuration element of Maven
		         * Surefire plugin)
		         */
		        { "${AUTHZFORCE_DATA_DIR}/c", null, null, "/tmp/authzforce/c" },

		        /*
		         * All kinds of placeholder replacements
		         */
		        { "${AUTHZFORCE_DATA_DIR}/b/${PARENT_DIR}/c/${sys.prop}/d", ImmutableMap.of(EnvironmentPropertyName.PARENT_DIR, "path/to"), ImmutableMap.of("sys.prop", "zx"),
		                "/tmp/authzforce/b/path/to/c/zx/d" },

		        /*
		         * Default value
		         */
		        { "${PARENT_DIR!/opt/driver-testbed-sec-authz-service/data}/a", null, null, "/opt/driver-testbed-sec-authz-service/data/a" },

				/*
				 * 
				 */
		};

		return Arrays.asList(data);
	}

	private final String input;
	private final String expectedOutput;
	private final EnvironmentProperties envProps;
	private final Map<String, String> sysProps;

	public DefaultEnvironmentPropertiesTest(final String input, final Map<EnvironmentPropertyName, String> userDefinedProperties, final Map<String, String> systemProperties,
	        final String expectedOutput)
	{
		this.input = input;
		this.expectedOutput = expectedOutput;
		this.envProps = new DefaultEnvironmentProperties(userDefinedProperties);
		this.sysProps = systemProperties;
		if (sysProps != null)
		{
			sysProps.forEach((k, v) -> System.setProperty(k, v));
		}

	}

	@Test
	public void test()
	{
		final String output = envProps.replacePlaceholders(input);
		Assert.assertEquals(expectedOutput, output);
	}

	@After
	public void afterTest()
	{
		if (sysProps != null)
		{
			sysProps.forEach((k, v) -> System.clearProperty(k));
		}
	}

	public static void main(final String[] args)
	{
	}

}
