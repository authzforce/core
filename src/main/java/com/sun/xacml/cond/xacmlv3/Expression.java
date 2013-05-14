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
/**
 * 
 */
package com.sun.xacml.cond.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Evaluatable;

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
 */

public abstract class Expression extends ExpressionType implements Evaluatable {
	
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Expression.class);
	
	public static ExpressionType getInstance(ExpressionType expression) throws UnknownIdentifierException {
		ExpressionType expr = null;		
		// We check what type of Expression is contained within the
		// AttributeAssignmentExpressionType
		// it can be: <Apply>, <AttributeSelector>, <AttributeValue>,
		// <Function>, <VariableReference> and <AttributeDesignator>.
		
		if (expression instanceof AttributeDesignatorType) {
			expr = AttributeDesignator.getInstance((AttributeDesignatorType)expression);
		} else if ((expression.getClass() == ApplyType.class)) {

		} else if ((expression.getClass() == AttributeSelectorType.class)) {
			expr = AttributeSelector.getInstance((AttributeSelectorType)expression);
		} else if ((expression.getClass() == FunctionType.class)) {

		} else if ((expression.getClass() == VariableReferenceType.class)) {

		} else if ((expression.getClass() == AttributeValueType.class)) {
			expr = AttributeValue.getInstance((AttributeValueType)expression);
		} else {
			throw new UnknownIdentifierException(
					"Attributes of type "
							+ expression.getClass()
							+ " aren't supported. "
							+ "You must use Apply, AttributeSelector, AttributeValue (Not Implemented Yet), "
							+ "Function, VariableReference or AttributeDesignator");
		}

		return expr;
	}

	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	public abstract EvaluationResult evaluate(EvaluationCtx context);
	public abstract EvaluationResult evaluate(List xprs, EvaluationCtx context);
	public abstract void checkInputs(List xprs);
	public abstract URI getType();
	public abstract boolean returnsBag();
	
}
