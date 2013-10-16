package com.thalesgroup.authzforce.pdp.core.test.utils;

public enum TestConstants {
	
	CONF_FILE("src\\test\\resources\\config.xml"),
	LOG_FILE("src\\test\\resources\\log4j.properties"),
	RESOURCE_PATH("src\\test\\resources"),
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
