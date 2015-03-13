/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.test.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.support.finder.StaticPolicyFinderModule;

public class SimplePolicyTest
{
	public static void main(String[] args) throws JAXBException
	{
		PolicyFinder policyFinder = new PolicyFinder();
		String policyFileResourceName = "src/test/resources/policy.xml";
		// URL policyFileURL =
		// Thread.currentThread().getContextClassLoader().getResource(policyFileResourceName);
		// Use getPath() to remove the file: prefix, because used later as input to
		// FileInputStream(...) in FilePolicyModule
		String[] policyLocations = { policyFileResourceName /* policyFileURL.getPath() */};
		StaticPolicyFinderModule testPolicyFinderModule = new StaticPolicyFinderModule(policyLocations);
		List<PolicyFinderModule<?>> policyModules = new ArrayList<>();
		policyModules.add(testPolicyFinderModule);
		policyFinder.setModules(policyModules);
		PDPConfig pdpConfig = new PDPConfig(null, policyFinder, null);

		PDP pdp = new PDP(pdpConfig);

		// request
		JAXBContext ctx = JAXBContext.newInstance(PolicySet.class);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		Request request = (Request) unmarshaller.unmarshal(new File("src/test/resources/request.xml"));

		ResponseCtx resp = pdp.evaluate(request);
		System.out.println(resp.getResults().iterator().next().getDecision());
	}
}
