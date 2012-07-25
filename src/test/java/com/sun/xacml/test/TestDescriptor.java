/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author najmi
 */
public class TestDescriptor {
    private String testName;
    private Set<String> policyFileNames = null;
    private Map<String, String> policyRefs = new HashMap<String, String>();
    private Map<String, String> policySetRefs = new HashMap<String, String>();;

    boolean errorExpected = false;
    boolean experimental = false;

    @Override
    public String toString() {
        return getTestName();
    }

    /**
     * @return the testName
     */
    public String getTestName() {
        return testName;
    }

    /**
     * @return the requestFileName
     */
    public String getRequestFileName() {
        return getTestName() + "Request.xml";
    }

    /**
     * @return the responseFileName
     */
    public String getResponseFileName() {
        return getTestName() + "Response.xml";
    }

    /**
     * @return the policyFileName
     */
    public Set<String> getPolicyFileNames() {
        if (policyFileNames == null) {
            policyFileNames = new HashSet<String>();
            policyFileNames.add(getTestName() + "Policy.xml");
        }
        return policyFileNames;
    }

    /**
     * @param testName the testName to set
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * @param policyFileNames the policyFileNames to set
     */
    public void setPolicyFileNames(Set<String> policyFileNames) {
        this.policyFileNames = policyFileNames;
    }

    /**
     * @return the policyRefs
     */
    public Map<String, String> getPolicyRefs() {
        return policyRefs;
    }

    /**
     * @return the policySetRefs
     */
    public Map<String, String> getPolicySetRefs() {
        return policySetRefs;
    }
}
