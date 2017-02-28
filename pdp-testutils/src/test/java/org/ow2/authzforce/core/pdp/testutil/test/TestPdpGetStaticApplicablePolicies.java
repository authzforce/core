/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.testutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.policy.ExtraPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.policy.StaticApplicablePolicyView;
import org.ow2.authzforce.core.pdp.testutil.PdpTest;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Test of {@link BasePdpEngine#getStaticApplicablePolicies()}
 *
 */
public class TestPdpGetStaticApplicablePolicies
{
	/**
	 * Name of directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_DIRECTORY_LOCATION = "classpath:conformance/others/PolicyReference.Valid";

	private final static IdReferenceType ROOT_POLICYSET_IDREF = new IdReferenceType("root:policyset-with-refs", "1.0", null, null);
	private final static Set<IdReferenceType> REF_POLICYSET_IDREFS;
	static
	{
		REF_POLICYSET_IDREFS = new HashSet<>();
		REF_POLICYSET_IDREFS.add(new IdReferenceType("PPS:Employee", "1.0", null, null));
	}

	@Test
	public void test() throws IllegalArgumentException, IOException, URISyntaxException, JAXBException
	{
		final String testResourceLocationPrefix = TEST_RESOURCES_DIRECTORY_LOCATION + "/";
		// Create PDP
		try (BasePdpEngine pdp = TestUtils.getPDPNewInstance(testResourceLocationPrefix + PdpTest.POLICY_FILENAME, testResourceLocationPrefix + PdpTest.REF_POLICIES_DIR_NAME, false, null, null))
		{
			final StaticApplicablePolicyView staticApplicablePolicies = pdp.getStaticApplicablePolicies();
			final ExtraPolicyMetadata rootPolicyExtraMetadata = staticApplicablePolicies.rootPolicyExtraMetadata();
			assertEquals("Invalid root policy returned by PDPImpl#getStaticApplicablePolicies()", ROOT_POLICYSET_IDREF, new IdReferenceType(staticApplicablePolicies.rootPolicyId(), rootPolicyExtraMetadata.getVersion().toString(), null, null));

			assertEquals("Invalid number of referenced policy sets returned by PDPImpl#getStaticApplicablePolicies()", REF_POLICYSET_IDREFS.size(), rootPolicyExtraMetadata.getRefPolicySets().size());

			for (final Entry<String, PolicyVersion> policyEntry : rootPolicyExtraMetadata.getRefPolicySets().entrySet())
			{
				assertTrue("Unexpected policy returned by PDPImpl#getStaticApplicablePolicies()", REF_POLICYSET_IDREFS.contains(new IdReferenceType(policyEntry.getKey(), policyEntry.getValue().toString(), null, null)));
			}

		}
	}
}
