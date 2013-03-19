/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.sun.xacml.test.conformance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.sun.xacml.ConfigurationStore;
import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.test.TestConstants;
import com.sun.xacml.test.TestUtil;
import com.sun.xacml.test.advance.AdvanceTestV3;
import com.thalesgroup.authzforce.policyfinder.FilePolicyFinder;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;


/**
 *  This XACML 3.0 conformation test. But this is the correct tests published by OASIS
 */
public class ConformanceTestV3 extends TestCase {


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
	private static Log log = LogFactory.getLog(ConformanceTestV2.class);

    @Override
    public void setUp() throws Exception {

        String configFile = (new File(".")).getCanonicalPath() + File.separator + TestConstants.CONFIG_FILE;
        store = new ConfigurationStore(new File(configFile));
    }


    public void testConformanceTestA() throws Exception {

        String policyNumber;

        for(int i = 1; i < 29 ; i++){
            
            if(i < 10){
                policyNumber = "00" + i;
            } else if(9 < i && i < 100) {
                policyNumber = "0" + i;
            } else {
                policyNumber = Integer.toString(i);
            }

            log.info("Conformance Test IIIA" + policyNumber + " is started");

            String request = TestUtil.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
                                                            "IIIA" + policyNumber + "Request.xacml3.xml");
            if(request != null){
                log.info("Request that is sent to the PDP :  " + request);
                Set<String> policies = new HashSet<String>();
                policies.add("IIIA" + policyNumber + "Policy.xacml3.xml");
                ResponseCtx response = TestUtil.evaluate(getPDPNewInstance(policies), request, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
                if(response != null){
                    ResponseCtx expectedResponseCtx = TestUtil.createResponse(ROOT_DIRECTORY,
                                        VERSION_DIRECTORY, "IIIA" + policyNumber + "Response.xacml3.xml");
                    log.info("Response that is received from the PDP :  " + response.getEncoded());
                    if(expectedResponseCtx != null){
                       assertTrue(TestUtil.isMatching(response, expectedResponseCtx));
                    } else {
                        assertTrue("Response read from file is Null",false);
                    }
                } else {
                    assertFalse("Response received PDP is Null",false);
                }
            } else {
                assertTrue("Request read from file is Null", false);
            }

            log.info("Conformance Test IIIA" + policyNumber + " is finished");
        }
    }


    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @param policies  Set of XACML policy file names
     * @return a  PDP instance
     */
    @SuppressWarnings("unchecked")
	private static PDP getPDPNewInstance(Set<String> policies){

        PolicyFinder finder= new PolicyFinder();
        List<String> policyLocations = new ArrayList<String>();

        for(String policy : policies){
            try {
                String policyPath = (new File(".")).getCanonicalPath() + File.separator +
                        TestConstants.RESOURCE_PATH + File.separator + ROOT_DIRECTORY + File.separator +
                        VERSION_DIRECTORY + File.separator + TestConstants.POLICY_DIRECTORY +
                        File.separator + policy;
                policyLocations.add(policyPath);
            } catch (IOException e) {
               //ignore.
            }
        }

        PolicyFinderModule testPolicyFinderModule = new FilePolicyFinder(policyLocations);
        Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
        policyModules.add(testPolicyFinderModule);
        finder.setModules(policyModules);

        PDP pdp = null;
		try {
			pdp = loadPdp(pdp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pdp.setPolicyFinder(finder);
//        PDPConfig pdpConfig = pdp.getPdpConfig();
//        PDPConfig pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder, pdpConfig.getResourceFinder(), false);
        return pdp;

    }
    
    private static PDP loadPdp(PDP myPdp) throws FileNotFoundException {
		Properties properties = new Properties();
		ConfigurationStore myStore = null;
		try {
			properties.load(AdvanceTestV3.class.getClassLoader().getResourceAsStream("authzforce.test.properties"));
		} catch (IOException e) {
			log.fatal("An exception occurred : " + e);
		}
		PropertyConfigurator.configure(properties.getProperty("logProperties"));
		File configFile = new File(properties.getProperty("configFile"));
		log.info("PDP Configuration Initialization");
		log.debug("Configuration file: " + configFile.getAbsolutePath());

		try {
			myStore = new ConfigurationStore(configFile);
		} catch (ParsingException e) {
			log.fatal("Error in Configuration Initialization, stacktrace:");
			log.fatal(e.getMessage());
		}
		log.info("PDP Initialization");
		try {
			myPdp = new PDP(myStore.getDefaultPDPConfig());
		} catch (UnknownIdentifierException e) {
			log.fatal("An exception occurred(UnknownIdentifierException) : ");
			log.fatal(e.getMessage());
		}
		
		return myPdp;
	}
}
