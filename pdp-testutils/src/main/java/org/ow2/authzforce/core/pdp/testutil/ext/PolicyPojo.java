/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.testutil.ext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Policy POJO for (un)marshalling to (from) JSON
 *
 */
public final class PolicyPojo
{
	private final String id;
	private final String version;
	private final String type;
	private final String content;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Policy(Set) ID
	 * @param version
	 *            Policy(Set) version
	 * @param type
	 *            policy type, e.g. "{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Policy" (resp. {urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySet) for XACML 3.0 Policy (resp. PolicySet)
	 * @param content
	 *            Policy(Set) document as plain text
	 */
	@JsonCreator
	public PolicyPojo(@JsonProperty("id") final String id, @JsonProperty("version") final String version, @JsonProperty("type") final String type, @JsonProperty("content") final String content)
	{
		this.id = id;
		this.version = version;
		this.type = type;
		this.content = content;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @return the content
	 */
	public String getContent()
	{
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[id='" + id + "', version='" + version + "', type='" + type + "', content='" + content + "']";
	}

	/**
	 * Same as {@link #toString()} but the resulting string does not contain the content, only the metadata (type, id, version)
	 * 
	 * @return string similar to {@link #toString()} without content
	 */
	public String toStringWithoutContent()
	{
		return "[id='" + id + "', version='" + version + "', type='" + type + "']";
	}

}
