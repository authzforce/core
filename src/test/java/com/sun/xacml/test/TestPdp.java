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
/**
 * 
 */
package com.sun.xacml.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.PDP;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLCategory;
import com.thalesgroup.authzforce.xacml.schema.XACMLDatatypes;

public class TestPdp {
	
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestPdp.class);
	
	private static PDP pdp;
	
	@BeforeClass
	public static void setUp() {
		LOGGER.info("Launching tests for PDP class");
		pdp = PDP.getInstance();
	}	

	/**
	 * Test method for {@link com.sun.xacml.PDP#getHashCode(oasis.names.tc.xacml._3_0.core.schema.wd_17.Request)}.
	 */
	@Test
	public final void testGetHashCode() {
		LOGGER.info("Testing: getHashCode");
		Request request = new Request();
		String hashCode1, hashCode2;
		
		/* Subject Category */
		Attribute subjId = new Attribute();
		AttributeValueType subjAttrValue = new AttributeValueType();
		Set<Attribute> subjAttributes = new HashSet<Attribute>();
		Attributes subjCategory = new Attributes();
		
		/* Resource Category */
		Attribute rscId = new Attribute();
		AttributeValueType rscAttrValue = new AttributeValueType();
		Set<Attribute> rscAttributes = new HashSet<Attribute>();
		Attributes rscCategory = new Attributes();
		
		/* Action Category */
		Attribute actId = new Attribute();
		AttributeValueType actAttrValue = new AttributeValueType();
		Set<Attribute> actAttributes = new HashSet<Attribute>();
		Attributes actCategory = new Attributes();
		
		/* Subject category configuration */
		subjAttrValue.setDataType(XACMLDatatypes.XACML_DATATYPE_STRING.value());
		subjAttrValue.getContent().add("T0101841");
		
		subjId.setAttributeId(XACMLAttributeId.XACML_SUBJECT_SUBJECT_ID.value());
		subjId.setIncludeInResult(true);
		subjId.setIssuer("com.thalesgroup.theresis.testCase");
		subjId.getAttributeValues().add(subjAttrValue);
		
		subjAttributes.add(subjId);
		subjCategory.setCategory(XACMLCategory.XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT.value());
		subjCategory.getAttributes().addAll(subjAttributes);
		
		/* Resource category configuration */
		rscAttrValue.setDataType(XACMLDatatypes.XACML_DATATYPE_STRING.value());
		rscAttrValue.getContent().add("testunit");
		
		rscId.setAttributeId(XACMLAttributeId.XACML_RESOURCE_RESOURCE_ID.value());
		rscId.setIncludeInResult(true);
		rscId.setIssuer("com.thalesgroup.theresis.testCase");
		rscId.getAttributeValues().add(rscAttrValue);
		
		rscAttributes.add(rscId);
		
		rscCategory.setCategory(XACMLCategory.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value());
		rscCategory.getAttributes().addAll(rscAttributes);
		
		/* Action category configuration */
		actAttrValue.setDataType(XACMLDatatypes.XACML_DATATYPE_STRING.value());
		actAttrValue.getContent().add("test");
		
		actId.setAttributeId(XACMLAttributeId.XACML_ACTION_ACTION_ID.value());
		actId.setIncludeInResult(true);
		actId.setIssuer("com.thalesgroup.theresis.testCase");
		actId.getAttributeValues().add(actAttrValue);
		
		actAttributes.add(actId);
		
		actCategory.setCategory(XACMLCategory.XACML_3_0_ACTION_CATEGORY_ACTION.value());
		actCategory.getAttributes().addAll(actAttributes);
		
		
		/* Request Construction */
		request.setCombinedDecision(true);
		request.getAttributes().add(subjCategory);
		request.getAttributes().add(rscCategory);
		request.getAttributes().add(actCategory);
		
		
		/**
		 * FIXME: why not use Request#hashCode()? It is generated from XSD with JAXB annotations.
		 */
		hashCode1 = pdp.getHashCode(request);
		hashCode2 = pdp.getHashCode(request);
		
		LOGGER.debug("Test for the same request");
		LOGGER.debug("HashCode 1: "+hashCode1);
		LOGGER.debug("HashCode 2: "+hashCode2);
		assertEquals("Two hashCode from the same request are not equals.", hashCode1, hashCode2);
		
		/* Second resource to test hashcode from two differents requests */
		Attribute rscId2 = new Attribute();
		AttributeValueType rscAttrValue2 = new AttributeValueType();
		rscId2.setAttributeId(XACMLAttributeId.XACML_RESOURCE_RESOURCE_ID.value());
		rscId2.setIncludeInResult(true);
		rscId2.setIssuer("com.thalesgroup.theresis.testCaseNumBerTwo");
		rscId2.getAttributeValues().add(rscAttrValue2);
		
		rscAttributes.add(rscId2);
		
		rscCategory.getAttributes().clear();
		rscCategory.getAttributes().addAll(rscAttributes);
		
		hashCode2 = pdp.getHashCode(request);
		
		LOGGER.debug("Test for differents requests");
		LOGGER.debug("HashCode 1: "+hashCode1);
		LOGGER.debug("HashCode 2: "+hashCode2);
		assertNotEquals("Two hashCode from different requests should not be equals.", hashCode1, hashCode2);
	}

}
