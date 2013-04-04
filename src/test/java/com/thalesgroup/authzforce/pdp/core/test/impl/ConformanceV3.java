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

import org.junit.Test;

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
 *  XACML 3.0 conformance tests published by OASIS
 */
public class ConformanceV3 extends TestCase {


    /**
     * Configuration store
     */
    private static ConfigurationStore store;

    /**
     * directory name that states the test type
     */
    private final static String ROOT_DIRECTORY  = "conformance";

    /**
     * directory name that states XACML version
     */
    private final static String VERSION_DIRECTORY  = "3";

    /**
     * the logger we'll use for all messages
     */
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(ConformanceV3.class);

    @Override
    public void setUp() throws Exception {

        String configFile = (new File(".")).getCanonicalPath() + File.separator + TestConstants.CONF_FILE.value();
        store = new ConfigurationStore(new File(configFile));
    }


    @Test
    public void testConformanceTestA() throws Exception {

        String policyNumber;
        ResponseCtx response = null;
        ResponseType expectedResponse = null;
        RequestType request = null;
        Map<String, String> results = new TreeMap<String, String>();

        for(int i = 1; i < 29 ; i++){
            
            if(i < 10){
                policyNumber = "00" + i;
            } else if(9 < i && i < 100) {
                policyNumber = "0" + i;
            } else {
                policyNumber = Integer.toString(i);
            }

            LOGGER.info("Conformance Test IIIA" + policyNumber + " is started");

            request = TestUtils.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
                                                            "IIIA" + policyNumber + "Request.xacml3.xml");
            if(request != null){
                LOGGER.debug("Request that is sent to the PDP :  " + TestUtils.printRequest(request));
                Set<String> policies = new HashSet<String>();
                policies.add("IIIA" + policyNumber + "Policy.xacml3.xml");
                response = getPDPNewInstance(policies).evaluate(request);
                if(response != null){
                    expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
                                        VERSION_DIRECTORY, "IIIA" + policyNumber + "Response.xacml3.xml");
                    LOGGER.debug("Response that is received from the PDP :  " + response);
                    LOGGER.debug("Going to assert it");
                    if(expectedResponse != null){
                    	boolean assertion = TestUtils.match(response, expectedResponse);
                    	if(assertion) {
                    		LOGGER.debug("Assertion SUCCESS for: IIIA"+policyNumber);
                    		results.put(policyNumber, "SUCCESS");
                    	} else {
                    		LOGGER.debug("Assertion FAILED");
                    	}
                       assertTrue(assertion);
                    } else {
                        assertTrue("Response read from file is Null",false);
                        LOGGER.debug("Assertion FAILED");
                    }
                } else {
                    assertFalse("Response received PDP is Null",false);
                }
            } else {
                assertTrue("Request read from file is Null", false);
            }

            LOGGER.info("Conformance Test IIIA" + policyNumber + " is finished");
        }
        for (String key : results.keySet()) {
			LOGGER.info(key + ":" + results.get(key));
		}
    }


    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @param policies  Set of XACML policy file names
     * @return a  PDP instance
     */
    private static PDP getPDPNewInstance(Set<String> policies){

        PolicyFinder finder= new PolicyFinder();
        List<String> policyLocations = new ArrayList<String>();

        for(String policy : policies){
            try {
                String policyPath = (new File(".")).getCanonicalPath() + File.separator +
                        TestConstants.RESOURCE_PATH.value() + File.separator + ROOT_DIRECTORY + File.separator +
                        VERSION_DIRECTORY + File.separator + TestConstants.POLICY_DIRECTORY.value() +
                        File.separator + policy;
                policyLocations.add(policyPath);
            } catch (IOException e) {
               LOGGER.error(e);
            }
        }

        FilePolicyModule testPolicyFinderModule = new FilePolicyModule(policyLocations);
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