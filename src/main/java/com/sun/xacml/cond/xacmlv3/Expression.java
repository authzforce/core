/**
 * Copyright (C) 2011-2014 Thales Services SAS - All rights reserved.
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
 * 
 */
package com.sun.xacml.cond.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.cond.Evaluatable;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 *         Implementation of utils to back ExpressionType up from the OASIS
 *         schema. 
 *                  
 *         SuperClass for:
 *         -	AttributeDesignatorType.class
 *         -	VariableReferenceType.class,
 *         -	ApplyType.class
 *         -	FunctionType.class
 *         -	AttributeSelectorType.class 
 *         - 	FunctionType
 *         -	AttributeValueType
 *         
 *         FIXME: Having Expression as a subtype of Evaluatable does not make sense. Should be the opposite (as it was in original Sunxacml version): Evaluatable is a special type of Expression (which is Evaluatable).
 *  E.g. Other types of Expression such as VariableReferenceType are not evaluatable (e.g. Evaluatable#getChildren method do not apply to this class).
 *  Maybe this class should be an Interface for all, and move the "utils" in ExpressionTools (it seems to be the case already
 */

public abstract class Expression extends ExpressionType implements Evaluatable {
	
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Expression.class);
	
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		try {
			Marshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error("Error marshalling Expression", e);
		}
	}

	public abstract EvaluationResult evaluate(EvaluationCtx context);
	public abstract EvaluationResult evaluate(List xprs, EvaluationCtx context);
	public abstract void checkInputs(List xprs);
	public abstract URI getType();
	public abstract boolean returnsBag();
	
}
