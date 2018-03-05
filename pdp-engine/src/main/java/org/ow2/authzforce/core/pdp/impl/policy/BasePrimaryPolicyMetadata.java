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
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.Objects;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer;

/**
 * Base implementation of {@link PrimaryPolicyMetadata}
 * <p>
 * NB: This class does not support Issuer and Description metadata (returns none). Extend this if you need to support Issuer and Description metadata
 */
public class BasePrimaryPolicyMetadata implements PrimaryPolicyMetadata
{
	private final TopLevelPolicyElementType type;
	private final String id;
	private final PolicyVersion version;

	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/**
	 * Creates instance from policy type, identifier and version
	 * 
	 * @param type
	 *            policy type (Policy or PolicySet)
	 * @param id
	 *            identifier
	 * @param version
	 *            version
	 */
	public BasePrimaryPolicyMetadata(final TopLevelPolicyElementType type, final String id, final PolicyVersion version)
	{
		assert type != null && id != null && version != null;
		this.type = type;
		this.id = id;
		this.version = version;
	}

	@Override
	public TopLevelPolicyElementType getType() {
		return this.type;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public PolicyVersion getVersion() {
		return this.version;
	}

	@Override
	public String toString() {
		if (toString == null)
		{
			this.toString = type + "[" + id + "#v" + version + "]";
		}

		return toString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hashCode == 0)
		{
			/*
			 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well in PolicyIdReferences. So we consider it is useless for identification in the XACML model.
			 */
			this.hashCode = Objects.hash(type, id, version);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof PrimaryPolicyMetadata))
		{
			return false;
		}

		final PrimaryPolicyMetadata other = (PrimaryPolicyMetadata) obj;
		return this.type.equals(other.getType()) && this.id.equals(other.getId()) && this.version.equals(other.getVersion());
	}

	@Override
	public Optional<PolicyIssuer> getIssuer() {
		// TODO: support PolicyIssuer. This field is relevant only to XACML Administrative Profile which is not supported here.
		return Optional.empty();
	}

	@Override
	public Optional<String> getDescription() {
		/*
		 * TODO: support Description. This field has no use in policy evaluation, therefore not a priority.
		 */
		return Optional.empty();
	}

}