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
package com.sun.xacml.xacmlv3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionFactoryProxy;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.thalesgroup.authzforce.core.PdpModelHandler;

public class TestApply {
	
	/*
	 * LOGGER used for all class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestApply.class);
	
	@BeforeClass
	public static void setUp() {
		LOGGER.info("Testing Applies objects");		
	}
	
	/**
	 * Regarding Issue #24 
	 * https://gitlab.dev.theresis.org/authzforce/core/issues/24
	 * 
	 * @throws ParsingException
	 * @throws  
	 */
	@Test
	public final void testMarshallApply() {
		LOGGER.info("Testing the marshalling of Apply objects");				
		Marshaller marshaller = null;
		Unmarshaller u = null;
		JAXBElement<ApplyType> applyElt = null;
		ApplyType applyType;
		Apply apply = null;
		List<ExpressionType> expressionsType = new ArrayList<ExpressionType>();
		FunctionFactoryProxy funcFactory = FunctionFactory.getInstance();
		try {
			u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
		} catch (JAXBException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		try {
			applyElt = (JAXBElement<ApplyType>) u.unmarshal(new File("src/test/resources/custom/TestApply.xml"));
		} catch (JAXBException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		applyType = applyElt.getValue();		
		for (JAXBElement<? extends ExpressionType> expr : applyType.getExpressions()) {
			expressionsType.add(expr.getValue());
		}		
		try {
			apply = new Apply(funcFactory.getGeneralFactory().createFunction(applyType.getFunctionId()), expressionsType, applyType.getDescription());
		} catch (IllegalArgumentException | UnknownIdentifierException | FunctionTypeException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		try {
			marshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();			
		} catch (JAXBException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		try {
			marshaller.marshal(new ObjectFactory().createApply(apply), System.out);
		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}		
	}	
}
