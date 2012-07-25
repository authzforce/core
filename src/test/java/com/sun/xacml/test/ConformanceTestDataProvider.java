/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author najmi
 */
public class ConformanceTestDataProvider {
    @DataProvider(name = "tests2")
    public static Iterator<Object[]> createData() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

        // load the test file
        Node root = getRootNode();

        JXPathContext jxpathContext =  JXPathContext.newContext(root);
        jxpathContext.setLenient(true);

        List<Node> nodes = jxpathContext.selectNodes(".//test");

        for (Node node : nodes) {
            JXPathContext nodeContext = JXPathContext.newContext(jxpathContext, node);
            String name = (String) nodeContext.getValue("./@name");
            String prefix = getTestPrefix(nodeContext);
            TestDescriptor td = new TestDescriptor();
            td.setTestName(prefix + name);

            String errorExpected = (String) nodeContext.getValue("./@errorExpected");
            if (errorExpected != null) {
                td.errorExpected = Boolean.valueOf(errorExpected).booleanValue();
            }

            String experimental = (String) nodeContext.getValue("./@experimental");
            if (experimental != null) {
                td.experimental = Boolean.valueOf(experimental).booleanValue();
            }

            //Process any test/policy elements
            Set<String> policyFileNames = new HashSet<String>();
            Iterator policyFilesIter = nodeContext.iterate("./policy");
            while (policyFilesIter.hasNext()) {
                String policyFileName = (String) policyFilesIter.next();
                policyFileNames.add(prefix + policyFileName);
            }
            if (policyFileNames.size() > 0) {
                td.setPolicyFileNames(policyFileNames);
            }

            //Process any test/policyReference elements
            List<Node> policyRefsList = nodeContext.selectNodes("./policyReference");
            for (Node policyRef : policyRefsList) {
                JXPathContext policyRefContext = JXPathContext.newContext(nodeContext, policyRef);
                String key = (String) policyRefContext.getValue("./@ref");
                String value = (String) prefix + policyRefContext.getValue(".");
                td.getPolicyRefs().put(key, value);
            }

            //Process any test/policyReference elements
            List<Node> policySetRefsList = nodeContext.selectNodes("./policySetReference");
            for (Node policySetRef : policySetRefsList) {
                JXPathContext policySetRefContext = JXPathContext.newContext(nodeContext, policySetRef);
                String key = (String) policySetRefContext.getValue("./@ref");
                String value = (String) prefix + policySetRefContext.getValue(".");
                td.getPolicySetRefs().put(key, value);
            }

            Object[] datum = new Object[1];
            datum[0] = td;
            data.add(datum);
        }

        return data.iterator();
    }

    private static String getTestPrefix(JXPathContext nodeContext) {
        String prefix = "";

        while (true) {
            Node node = (Node) nodeContext.selectSingleNode(".");
            Node parent = node.getParentNode();

            if ((parent != null) && ( !(parent.getNodeName().equals("group")))) {
                //not group: we are done
                break;
            } else {
                //Group: use group/@name in prefix
                JXPathContext parentContext = JXPathContext.newContext(parent);
                prefix = parentContext.getValue("./@name") + prefix;
                nodeContext = parentContext;
            }
        }

        return prefix;
    }

    /**
     * Private helper that parses the file and sets up the DOM tree.
     */
    private static Node getRootNode() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource configFileResource = resolver.getResource("/tests2.xml");

        DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();

        dbFactory.setIgnoringComments(true);
        dbFactory.setNamespaceAware(false);
        dbFactory.setValidating(false);

        DocumentBuilder db = dbFactory.newDocumentBuilder();
        Document doc = db.parse(configFileResource.getInputStream());
        Element root = doc.getDocumentElement();

        if (!root.getTagName().equals("tests")) {
            throw new Exception("unknown document type: " + root.getTagName());
        }

        return root;
    }

}
