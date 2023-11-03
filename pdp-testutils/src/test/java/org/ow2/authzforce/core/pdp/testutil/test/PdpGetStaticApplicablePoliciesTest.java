/*
 * Copyright 2012-2023 THALES.
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
package org.ow2.authzforce.core.pdp.testutil.test;

import jakarta.xml.bind.JAXBException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.core.pdp.testutil.XacmlXmlPdpTestHelper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Test of {@link BasePdpEngine#getApplicablePolicies()}
 *
 */
public class PdpGetStaticApplicablePoliciesTest
{
	/**
	 * Name of directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_DIRECTORY_LOCATION = "target/test-classes/conformance/others/PolicyReference.Valid";

	private static final class MinimalPolicySetMetadata implements PrimaryPolicyMetadata
	{

		private final String id;
		private final PolicyVersion version;

		private MinimalPolicySetMetadata(final String id, final String version)
		{
			assert id != null && version != null;
			this.id = id;
			this.version = new PolicyVersion(version);
		}

		@Override
		public TopLevelPolicyElementType getType()
		{
			return TopLevelPolicyElementType.POLICY_SET;
		}

		@Override
		public String getId()
		{
			return this.id;
		}

		@Override
		public PolicyVersion getVersion()
		{
			return this.version;
		}

		@Override
		public Optional<PolicyIssuer> getIssuer()
		{
			return Optional.empty();
		}

		@Override
		public Optional<String> getDescription()
		{
			return Optional.empty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "PolicySet['" + id + "' v" + version + "]";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			// id != null (see constructor)
			result = prime * result + id.hashCode();
			result = prime * result + version.hashCode();
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof PrimaryPolicyMetadata other))
			{
				return false;
			}

			// Description metadata is ignored
			if (this.getIssuer().isPresent())
			{
				if (other.getIssuer().isEmpty())
				{
					return false;
				}

				if (!this.getIssuer().get().equals(other.getIssuer().get()))
				{
					return false;
				}
			}
			else if (other.getIssuer().isPresent())
			{
				return false;
			}

			return other.getType().equals(TopLevelPolicyElementType.POLICY_SET) && this.id.equals(other.getId()) && this.version.equals(other.getVersion());
		}

	}

	private final static PrimaryPolicyMetadata ROOT_POLICYSET_METADATA = new MinimalPolicySetMetadata("root", "1.0");

	private final static List<PrimaryPolicyMetadata> REF_POLICYSET_METADATA_SET = Collections.singletonList(new MinimalPolicySetMetadata("PPS:Employee", "1.0"));

	@Test
	public void test() throws IllegalArgumentException, IOException, URISyntaxException, JAXBException
	{
		final String testResourceLocationPrefix = TEST_RESOURCES_DIRECTORY_LOCATION + "/";

		/*
		 * Policies
		 * 
		 * If there is a "$TEST_DIR/$POLICIES_DIR_NAME" directory, then load all policies from there, including root policy from "$TEST_DIR/$POLICIES_DIR_NAME/$ROOT_POLICY_FILENAME" Else load only the
		 * root policy from "$TEST_DIR/$ROOT_POLICY_FILENAME"
		 */
		final Path policiesDir = Paths.get(testResourceLocationPrefix + XacmlXmlPdpTestHelper.POLICIES_DIR_NAME);
		final Optional<Path> optPoliciesDir;
		final Path rootPolicyFile;
		if (Files.isDirectory(policiesDir))
		{
			optPoliciesDir = Optional.of(policiesDir);
			rootPolicyFile = policiesDir.resolve(XacmlXmlPdpTestHelper.ROOT_POLICY_FILENAME);
		}
		else
		{
			optPoliciesDir = Optional.empty();
			rootPolicyFile = Paths.get(testResourceLocationPrefix + XacmlXmlPdpTestHelper.ROOT_POLICY_FILENAME);
		}

		/*
		 * Create PDP
		 */
		final PdpEngineConfiguration pdpEngineConf = optPoliciesDir.isPresent()
		        ? TestUtils.newPdpEngineConfiguration(TestUtils.getPolicyRef(rootPolicyFile), optPoliciesDir.get(), false, Optional.empty(), null, null)
		        : TestUtils.newPdpEngineConfiguration(rootPolicyFile, false, Optional.empty(), null, null);
		try (final PdpEngineInoutAdapter<Request, Response> pdp = PdpEngineAdapters.newXacmlJaxbInoutAdapter(pdpEngineConf))
		{
			final Iterable<PrimaryPolicyMetadata> staticApplicablePolicies = pdp.getApplicablePolicies();
			assertNotNull("One of the policies may not be statically resolved", staticApplicablePolicies);
			final Iterator<PrimaryPolicyMetadata> staticApplicablePoliciesIterator = pdp.getApplicablePolicies().iterator();
			assertTrue("No root policy in PDP's applicable policies (statically resolved)", staticApplicablePoliciesIterator.hasNext());
			assertEquals("Invalid root policy in PDP's applicable policies (statically resolved)", ROOT_POLICYSET_METADATA, staticApplicablePoliciesIterator.next());

			for (final PrimaryPolicyMetadata expectedRefPolicyMeta : REF_POLICYSET_METADATA_SET)
			{
				assertTrue("No (more) referenced policy in PDP's applicable policies (statically resolved) although expected", staticApplicablePoliciesIterator.hasNext());
				assertEquals("Invalid referenced policy in PDP's applicable policies (statically resolved)", expectedRefPolicyMeta, staticApplicablePoliciesIterator.next());
			}

		}
	}
}
