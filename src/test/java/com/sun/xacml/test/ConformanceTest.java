
/*
 * @(#)ConformanceTest.java
 *
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.test;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testng.annotations.BeforeTest;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.ConfigurationStore;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;


/**
 * TestNG driver class to run conformance tests
 *
 * @author Seth Proctor
 * @author Farrukh Najmi
 */
@org.testng.annotations.Test
public class ConformanceTest { //extends XMLTestCase {

    private static Log log = LogFactory.getLog(ConformanceTest.class);
    
    // the pdp we use to do all evaluations
    private PDP pdp;

    // the module we use to manage all policy management
    private TestPolicyFinderModule testPolicyFinderModule;

    @BeforeTest
    public void setUp() throws Exception {
        System.out.println("Starting XACML tests at " + new Date());
        
        testPolicyFinderModule = new TestPolicyFinderModule();
        
        configurePDP();
    }

    @org.testng.annotations.Test(dataProvider = "tests2", dataProviderClass = ConformanceTestDataProvider.class)
    public void testXACML(TestDescriptor td) throws Exception {
        log.info("Running xacml test: " + td.getTestName());
        
        // the policies and references used by this test
        Map policyRefs = null;
        Map policySetRefs = null;

        int errorCount = 0;
        boolean failurePointReached = false;

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource requestResource = resolver.getResource("/conformance-tests/" + td.getRequestFileName());
        Resource responseResource = resolver.getResource("/conformance-tests/" + td.getResponseFileName());        

        try {
            // load the request for this test
            Unmarshaller u = BindingUtility.getUnmarshaller();
            RequestType request = ((JAXBElement<RequestType>) u.unmarshal(requestResource.getInputStream())).getValue();

            Set<String> policyFileNames = td.getPolicyFileNames();
            testPolicyFinderModule.setPolicies(policyFileNames);

            // re-set any references we're using
            testPolicyFinderModule.setPolicyRefs(td.getPolicyRefs(), "");
            testPolicyFinderModule.setPolicySetRefs(td.getPolicySetRefs(), "");

            if (log.isTraceEnabled()) {
                File tempFile;
                tempFile = File.createTempFile("Request-" + td.getTestName() + "-", ".xml");
                JAXBElement<RequestType> elem = com.sun.xacml.BindingUtility.contextFac.createRequest(request);
                com.sun.xacml.BindingUtility.createMarshaller().marshal(elem, tempFile);
            }

            // actually do the evaluation
            ResponseCtx response = pdp.evaluate(request);

            // load the reponse that we expectd to get
            ResponseCtx expectedResponse =
                ResponseCtx.
                getInstance(responseResource.getInputStream());

            //XMLAssert.assertXMLEqual(getReader(expectedResponse, td.toString()+"-expected-"), getReader(response, td.toString()+"-result-"));

            // see if the actual result matches the expected result
            //TODO: Migrate TestUtil to XMLUnit
            boolean equiv = TestUtil.areEquivalent(response,
                                                   expectedResponse);            
            assertTrue("Assertion failed for test " + td, equiv);            

        } catch (Exception e) {
            if (!td.errorExpected) {
                throw new RuntimeException("Unexpected exception in test " + td, e);
            } else {
                //TODO: Only swallow expected exception not any exceptions
                log.debug("Expected failure of test " + td + ": ", e);
            }
        }

    }

    private Reader getReader(ResponseCtx response, String suffix) throws IOException {
        Reader reader = null;

        File tempFile = File.createTempFile("ResponseCtx-" + suffix, ".xml");
        FileOutputStream os = new FileOutputStream(tempFile);
        response.encode(os);
        os.close();

        reader = new FileReader(tempFile);        

        return reader;
    }

    /**
     * Private helper that configures the pdp and the factories based on the
     * settings in the run-time configuration file.
     */
    private void configurePDP() throws Exception {
    	
        String pdpConfigFilePath = System.getProperty(ConfigurationStore.PDP_CONFIG_PROPERTY);
        if (pdpConfigFilePath == null) {
            System.setProperty(ConfigurationStore.PDP_CONFIG_PROPERTY, "src/test/resources/config.xml");
        }                
        // load the configuration
        ConfigurationStore cs = new ConfigurationStore();

        // use the default factories from the configuration
        cs.useDefaultFactories();

        // get the PDP configuration's policy finder modules...
        PDPConfig config = cs.getDefaultPDPConfig();
        PolicyFinder finder = config.getPolicyFinder();
        Set policyModules = finder.getModules();

        // ...and add the testPolicyFinderModule used by the tests
        policyModules.add(testPolicyFinderModule);
        finder.setModules(policyModules);

        // finally, setup the PDP
        pdp = new PDP(config);        
    }


}
