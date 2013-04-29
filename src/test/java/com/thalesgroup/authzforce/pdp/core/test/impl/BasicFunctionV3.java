package com.thalesgroup.authzforce.pdp.core.test.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

import com.sun.xacml.ConfigurationStore;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.support.finder.FilePolicyModule;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestConstants;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * This XACML 3.0 basic policy test. This would test a basic policy, basic
 * policy with obligations and basic policy with advices.
 */
public class BasicFunctionV3 extends TestCase {

	/**
	 * Configuration store
	 */
	private static ConfigurationStore store;

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "basic";

	/**
	 * directory name that states XACML version
	 */
	private final static String VERSION_DIRECTORY = "3";

	/**
	 * the logger we'll use for all messages
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(BasicFunctionV3.class);
	/**
	 * The map of results
	 */
	private Map<String, String> results = new TreeMap<String, String>();

	@Override
	public void setUp() throws Exception {

		String configFile = (new File(".")).getCanonicalPath() + File.separator
				+ TestConstants.CONF_FILE.value();
		store = new ConfigurationStore(new File(configFile));
	}

	public void tearDown() throws Exception {
		this.showResults();
	}

	public void testBasicTest0006() throws Exception {

		String reqResNo;
		Set<String> policies = new HashSet<String>();
		policies.add("TestPolicy_0006.xml");
		PDP pdp = getPDPNewInstance(policies);
		LOGGER.info("Basic Test 0006 is started");
		ResponseCtx response = null;
		ResponseType expectedResponse = null;
		RequestType request = null;

		for (int i = 1; i < 4; i++) {

			if (i < 10) {
				reqResNo = "0" + i;
			} else {
				reqResNo = Integer.toString(i);
			}

			request = TestUtils.createRequest(ROOT_DIRECTORY,
					VERSION_DIRECTORY, "request_0006_" + reqResNo + ".xml");
			if (request != null) {
				LOGGER.debug("Request that is sent to the PDP :  "
						+ TestUtils.printRequest(request));
				response = getPDPNewInstance(policies).evaluate(request);
				if (response != null) {
					LOGGER.debug("Response that is received from the PDP :  "
							+ response.getEncoded());
					expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
							VERSION_DIRECTORY, "response_0006_" + reqResNo
									+ ".xml");
					if (expectedResponse != null) {
						boolean assertion = TestUtils.match(response,
								expectedResponse);
						if (assertion) {
							LOGGER.debug("Assertion SUCCESS for: IIIA"
									+ "response_0006_" + reqResNo);
							results.put("response_0006_" + reqResNo, "SUCCESS");
						} else {
							LOGGER.error("Assertion FAILED for: TestPolicy_0006 and response_0006_"
									+ reqResNo);
							results.put("response_0006_" + reqResNo, "FAILED");
						}
						assertTrue(assertion);
					} else {
						assertTrue("Response read from file is Null", false);
					}
				} else {
					assertFalse("Response received PDP is Null", false);
				}
			} else {
				assertTrue("Request read from file is Null", false);
			}

			LOGGER.info("Basic Test 0006 is finished");
		}
	}
	
	private void showResults() throws Exception {
		for (String key : results.keySet()) {
			LOGGER.info(key + ":" + results.get(key));
		}
	}

	/**
	 * Returns a new PDP instance with new XACML policies
	 * 
	 * @param policies
	 *            Set of XACML policy file names
	 * @return a PDP instance
	 */
	private static PDP getPDPNewInstance(Set<String> policies) {

		PolicyFinder finder = new PolicyFinder();
		List<String> policyLocations = new ArrayList<String>();

		for (String policy : policies) {
			try {
				String policyPath = (new File(".")).getCanonicalPath()
						+ File.separator + TestConstants.RESOURCE_PATH.value()
						+ File.separator + ROOT_DIRECTORY + File.separator
						+ VERSION_DIRECTORY + File.separator
						+ TestConstants.POLICY_DIRECTORY.value()
						+ File.separator + policy;
				policyLocations.add(policyPath);
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}

		FilePolicyModule testPolicyFinderModule = new FilePolicyModule(
				policyLocations);
		Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
		policyModules.add(testPolicyFinderModule);
		finder.setModules(policyModules);

		PDP authzforce = PDP.getInstance();
		PDPConfig pdpConfig = authzforce.getPDPConfig();
		pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder,
				pdpConfig.getResourceFinder(), null);
		return new PDP(pdpConfig);

	}
}