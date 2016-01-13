/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EnvironmentPropertyName;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Default implementation of PDP configuration parser's environment properties.
 *
 */
public final class DefaultEnvironmentProperties implements EnvironmentProperties
{
	private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
	private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
	private static final String PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR = ":";

	private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper(PROPERTY_PLACEHOLDER_PREFIX,
			PROPERTY_PLACEHOLDER_SUFFIX, PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR, false);

	private final Properties props = new Properties();

	/**
	 * Empty properties
	 */
	public DefaultEnvironmentProperties()
	{
		// empty properties
	}

	/**
	 * Constructs instance from existing properties in a map
	 * 
	 * @param envProps
	 *            environment properties
	 */
	public DefaultEnvironmentProperties(Map<EnvironmentPropertyName, String> envProps)
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

	@Override
	public String replacePlaceholders(String input)
	{
		if (input == null)
		{
			return null;
		}

		return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(input, props);
	}
}
