package com.thalesgroup.authzforce.pdp.core.test.utils;

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
	public static void main(String[] args) throws JAXBException {
		PolicyFinder policyFinder = new PolicyFinder();
		List<String> policyLocations = new ArrayList<>();
			String policyFileResourceName = "src/test/resources/policy.xml";
			//URL policyFileURL = Thread.currentThread().getContextClassLoader().getResource(policyFileResourceName);
			// Use getPath() to remove the file: prefix, because used later as input to FileInputStream(...) in FilePolicyModule
			policyLocations.add(policyFileResourceName /*policyFileURL.getPath()*/);

		StaticPolicyFinderModule testPolicyFinderModule = new StaticPolicyFinderModule(
				policyLocations);
		List<PolicyFinderModule<?>> policyModules = new ArrayList<>();
		policyModules.add(testPolicyFinderModule);
		policyFinder.setModules(policyModules);
		PDPConfig pdpConfig =  new PDPConfig(null, policyFinder, null);
			
		PDP pdp = new PDP(pdpConfig);
		
		// request
		JAXBContext ctx = JAXBContext.newInstance(PolicySet.class);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		Request request = (Request) unmarshaller.unmarshal(new File("src/test/resources/request.xml"));
		
		ResponseCtx resp = pdp.evaluate(request);
		System.out.println(resp.getResults().iterator().next().getDecision());
	}
}
