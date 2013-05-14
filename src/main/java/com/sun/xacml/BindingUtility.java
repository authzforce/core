/**
 * Copyright (C) ${year} T0101841 <${email}>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Romain Ferrari
 */
public class BindingUtility {
	
	private static DocumentBuilder db = null;
	
	static DocumentBuilderFactory dbf = null;
	
    static JAXBContext s_jaxbContext = null;
    public static  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory contextFac =
            new  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory();
//    public static oasis.names.tc.xacml._2_0.policy.schema.os.ObjectFactory policyFac =
//            new oasis.names.tc.xacml._2_0.policy.schema.os.ObjectFactory();
    public static  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory policyFac =
            new  oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory();
    

    //public static String jaxbContextPath = "net.opengis.gml.v_3_2_1:org.isotc211.iso19139.d_2007_04_17.gco:org.isotc211.iso19139.d_2007_04_17.gmd:org.isotc211.iso19139.d_2007_04_17.gmx:org.isotc211.iso19139.d_2007_04_17.gsr:org.isotc211.iso19139.d_2007_04_17.gss:org.isotc211.iso19139.d_2007_04_17.gts";
    public static String jaxbContextPath = "oasis.names.tc.xacml._3_0.core.schema.wd_17";

    public static JAXBContext getJAXBContext() throws JAXBException {
        if (s_jaxbContext == null) {
            s_jaxbContext = JAXBContext.newInstance(jaxbContextPath, BindingUtility.class.getClassLoader());            
        }

        return s_jaxbContext;
    }
    
    public static Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext gmlContext = getJAXBContext();
        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();

        //unmarshaller.setValidating(true);
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            public boolean handleEvent(ValidationEvent event) {
                boolean keepOn = false;

                return keepOn;
            }
        });

        return unmarshaller;
    }

    public static Marshaller createMarshaller() {
        try {
            Marshaller marshaller = getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static DocumentBuilder getDocumentBuilder(){    	
    	if (dbf == null) {    		
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);			
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
          return db;
    }
    
}
