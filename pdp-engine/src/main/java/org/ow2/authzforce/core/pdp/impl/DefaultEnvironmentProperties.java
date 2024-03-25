/*
 * Copyright 2012-2024 THALES.
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
package org.ow2.authzforce.core.pdp.impl;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EnvironmentPropertyName;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Default implementation of PDP configuration parser's environment properties, that supports user-defined properties, Java system properties and system-dependent environment variables.
 *
 * @version $Id: $
 */
public final class DefaultEnvironmentProperties implements EnvironmentProperties
{

	private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
	private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
	/*
	 * We cannot use ':' as default value separator because not valid in XML anyURI
	 */
	private static final String PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR = "!";

	private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper(PROPERTY_PLACEHOLDER_PREFIX, PROPERTY_PLACEHOLDER_SUFFIX,
	        PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR, false);

	private final Properties props = new Properties();

	/**
	 * Empty properties. Placeholders are resolved as system properties and environment variables only.
	 */
	public DefaultEnvironmentProperties()
	{
		// empty properties
	}

	/**
	 * Constructs instance from existing properties in a map. Placeholders are resolved from {@code envProps} if the property name matches any, else as Java system property if the name matches any,
	 * else as system environment variables.
	 *
	 * @param envProps
	 *            environment properties taking precedence over system properties or environment variables.
	 */
	public DefaultEnvironmentProperties(final Map<EnvironmentPropertyName, String> envProps)
	{
		if (envProps == null)
		{
			return;
		}

		for (final Entry<EnvironmentPropertyName, String> envProp : envProps.entrySet())
		{
			/*
			 * Property value must be a String! Using props.put(Object,Object) is misleading here as it makes falsely believe other datatypes would work
			 */
			props.setProperty(envProp.getKey().name(), envProp.getValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String replacePlaceholders(final String input)
	{
		if (input == null)
		{
			return null;
		}

		return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(input, placeholderName -> {

			final String userDefinedPropVal = props.getProperty(placeholderName);
			if (userDefinedPropVal != null)
			{
				return userDefinedPropVal;
			}

			final String sysPropVal = System.getProperty(placeholderName);
			if (sysPropVal != null)
			{
				return sysPropVal;
			}
			// Fall back to searching the system environment.
			return System.getenv(placeholderName);
		});
	}
}
