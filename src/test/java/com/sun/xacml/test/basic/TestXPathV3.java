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
package com.sun.xacml.test.basic;

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
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.test.TestConstants;
import com.sun.xacml.test.TestUtil;
import com.sun.xacml.test.advance.AdvanceTestV3;
import com.thalesgroup.authzforce.policyfinder.DirectoryPolicyFinder;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * This would test XPath that is introduced with XACML 3.0.
 */
public class TestXPathV3 extends TestCase {

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
    private static Log log = LogFactory.getLog(TestFunctionV3.class);

    public void testBasicTest0001() throws Exception {

        String reqResNo;
        Set<String> policies = new HashSet<String>();
        policies.add("TestPolicy_0007.xml");
        log.info("Basic Test 0007 is started");

        for (int i = 1; i < 4; i++) {

            if (i < 10) {
                reqResNo = "0" + i;
            } else {
                reqResNo = Integer.toString(i);
            }

            String request = TestUtil.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
                    "request_0007_" + reqResNo + ".xml");
            if (request != null) {
                log.info("Request that is sent to the PDP :  " + request);
                ResponseCtx response = TestUtil.evaluate(getPDPNewInstance(policies), request, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
                if (response != null) {
//                    log.info("Response that is received from the PDP :  " + response.getEncoded());
                    ResponseCtx expectedResponseCtx = TestUtil.createResponse(ROOT_DIRECTORY,
                            VERSION_DIRECTORY, "response_0007_" + reqResNo + ".xml");
                    if (expectedResponseCtx != null) {
                        assertTrue(TestUtil.isMatching(response, expectedResponseCtx));
                    } else {
                        assertTrue("Response read from file is Null", false);
                    }
                } else {
                    assertFalse("Response received PDP is Null", false);
                }
            } else {
                assertTrue("Request read from file is Null", false);
            }

            log.info("Basic Test 0007 is finished");
        }
    }


    public void testBasicTest0002() throws Exception {

        String reqResNo;
        Set<String> policies = new HashSet<String>();
        policies.add("TestPolicy_0008.xml");
        log.info("Basic Test 0008 is started");

        for (int i = 1; i < 4; i++) {

            if (i < 10) {
                reqResNo = "0" + i;
            } else {
                reqResNo = Integer.toString(i);
            }

            String request = TestUtil.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
                    "request_0008_" + reqResNo + ".xml");
            if (request != null) {
                log.info("Request that is sent to the PDP :  " + request);
                ResponseCtx response = TestUtil.evaluate(getPDPNewInstance(policies), request, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
                if (response != null) {
//                    log.info("Response that is received from the PDP :  " + response.getEncoded());
                    ResponseCtx expectedResponseCtx = TestUtil.createResponse(ROOT_DIRECTORY,
                            VERSION_DIRECTORY, "response_0008_" + reqResNo + ".xml");
                    if (expectedResponseCtx != null) {
                        assertTrue(TestUtil.isMatching(response, expectedResponseCtx));
                    } else {
                        assertTrue("Response read from file is Null", false);
                    }
                } else {
                    assertFalse("Response received PDP is Null", false);
                }
            } else {
                assertTrue("Request read from file is Null", false);
            }

            log.info("Basic Test 0008 is finished");
        }
    }

    public void testBasicTest0003() throws Exception {

        String reqResNo;
        Set<String> policies = new HashSet<String>();
        policies.add("TestPolicy_0009.xml");
        log.info("Basic Test 0009 is started");

        for (int i = 1; i < 4; i++) {

            if (i < 10) {
                reqResNo = "0" + i;
            } else {
                reqResNo = Integer.toString(i);
            }

            String request = TestUtil.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
                    "request_0009_" + reqResNo + ".xml");
            if (request != null) {
                log.info("Request that is sent to the PDP :  " + request);
                ResponseCtx response = TestUtil.evaluate(getPDPNewInstance(policies), request, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
                if (response != null) {
//                    log.info("Response that is received from the PDP :  " + response.getEncoded());
                    ResponseCtx expectedResponseCtx = TestUtil.createResponse(ROOT_DIRECTORY,
                            VERSION_DIRECTORY, "response_0009_" + reqResNo + ".xml");
                    if (expectedResponseCtx != null) {
                        assertTrue(TestUtil.isMatching(response, expectedResponseCtx));
                    } else {
                        assertTrue("Response read from file is Null", false);
                    }
                } else {
                    assertFalse("Response received PDP is Null", false);
                }
            } else {
                assertTrue("Request read from file is Null", false);
            }

            log.info("Basic Test 0009 is finished");
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
                        TestConstants.RESOURCE_PATH + File.separator + ROOT_DIRECTORY + File.separator +
                        VERSION_DIRECTORY + File.separator + TestConstants.POLICY_DIRECTORY +
                        File.separator + policy;
                policyLocations.add(policyPath);
            } catch (IOException e) {
               //ignore.
            }
        }

        DirectoryPolicyFinder testPolicyFinderModule = new DirectoryPolicyFinder(policyLocations);
        Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
        policyModules.add(testPolicyFinderModule);
        finder.setModules(policyModules);

        PDP pdp = null;
		try {
			pdp = loadPdp();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pdp.getPolicyFinder().getModules().add(testPolicyFinderModule);
//        PDPConfig pdpConfig = pdp.getPdpConfig();
//        PDPConfig pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder, pdpConfig.getResourceFinder(), false);
        return pdp;

    }
    
    private static PDP loadPdp() throws FileNotFoundException {
		Properties properties = new Properties();
		ConfigurationStore myStore = null;
		PDP myPdp = null;
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
