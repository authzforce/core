/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.thalesgroup.authzforce.pdp.core.test.utils;

public enum TestConstants {
	
	CONF_FILE("src/test/resources/config.xml"),
	LOG_FILE("src/test/resources/log4j.properties"),
	RESOURCE_PATH("src/test/resources"),
	POLICY_DIRECTORY("policies"),
	REQUEST_DIRECTORY("requests"),
	RESPONSE_DIRECTORY("responses");
	
	private final String value;

	TestConstants(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static TestConstants fromValue(String v) {
		for (TestConstants c : TestConstants.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
