/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
/**
 * 
 */
package com.sun.xacml.cond.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.cond.VariableReference;

/**
 * @author Romain Ferrari
 * 
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
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
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
