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
package com.sun.xacml.cond.xacmlv3;

import java.net.URI;
import java.net.URISyntaxException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.w3c.dom.Node;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.cond.VariableReference;

public class ExpressionTools {
	/**
	 * Parses an expression, recursively handling any sub-elements. This is
	 * provided as a utility class, but in practice is used only by
	 * <code>Apply</code>, <code>Condition</code>, and
	 * <code>VariableDefinition</code>.
	 * 
	 * @param root
	 *            the DOM root of an ExpressionType XML type
	 * @param metaData
	 *            the meta-data associated with the containing policy
	 * @param manager
	 *            <code>VariableManager</code> used to connect references and
	 *            definitions while parsing
	 * 
	 * @return an <code>Expression</code> or null if the root node cannot be
	 *         parsed as a valid Expression
	 * @throws ParsingException 
	 */
	// public static ExpressionType getExpression(Node root,
	// PolicyMetaData metaData, VariableManager manager)
	// throws ParsingException {
	// String name = root.getNodeName();
	//
	// if (name.equals("Apply")) {
	// return Apply.getInstance(root, metaData, manager);
	// } else if (name.equals("AttributeValue")) {
	// try {
	// return AttributeFactory.getInstance().createValue(root);
	// } catch (UnknownIdentifierException uie) {
	// throw new ParsingException("Unknown DataType", uie);
	// }
	// } else if (name.equals("AttributeSelector")) {
	// return AttributeSelector.getInstance(root, metaData);
	// } else if (name.equals("Function")) {
	// return getFunction(root, metaData, FunctionFactory.getGeneralInstance());
	// } else if (name.equals("VariableReference")) {
	// return VariableReference.getInstance(root, metaData, manager);
	// } else if (name.equals("AttributeDesignator")) {
	// String category = root.getAttributes().getNamedItem("Category")
	// .getNodeValue();
	// if (XACMLAttributeId.XACML_1_0_SUBJECT_CATEGORY_SUBJECT.value()
	// .equalsIgnoreCase(category)) {
	// return AttributeDesignator.getInstance(root,
	// String.valueOf(AttributeDesignator.SUBJECT_TARGET),
	// metaData);
	// } else if (XACMLAttributeId.XACML_3_0_RESOURCE_CATEGORY_RESOURCE
	// .value().equalsIgnoreCase(category)) {
	// return AttributeDesignator.getInstance(root,
	// String.valueOf(AttributeDesignator.RESOURCE_TARGET),
	// metaData);
	// } else if (XACMLAttributeId.XACML_3_0_ACTION_CATEGORY_ACTION
	// .value().equalsIgnoreCase(category)) {
	// return AttributeDesignator.getInstance(root,
	// String.valueOf(AttributeDesignator.ACTION_TARGET),
	// metaData);
	// } else if (XACMLAttributeId.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT
	// .value().equalsIgnoreCase(category)) {
	// return AttributeDesignator.getInstance(root,
	// String.valueOf(AttributeDesignator.ENVIRONMENT_TARGET),
	// metaData);
	// }
	//
	// }
	//
	// // return null if it was none of these
	// return null;
	// }

	public static ExpressionType getExpression(Node root,
			PolicyMetaData metaData, VariableManager manager)
			throws ParsingException {
		String name = root.getNodeName();
		ExpressionType myExpr = null;
		if (name.equals("Apply")) {
			myExpr = Apply.getInstance(root, metaData, manager);
		} 
		//FIXME: AttributeValue.class causes VerifyError
//		else if (name.equals("AttributeValue")) {
//			try {
//				myExpr = AttributeFactory.getInstance().createValue(root);
//			} catch (UnknownIdentifierException uie) {
//				throw new ParsingException("Unknown DataType", uie);
//			}
//		} 
		else if (name.equals("AttributeValue")) {
			myExpr = AttributeValue.getInstance(root, metaData);
		}
		else if (name.equals("AttributeSelector")) {
			myExpr = AttributeSelector.getInstance(root, metaData);
		} 
		else if (name.equals("Function")) {
			myExpr = getFunction(root, metaData,
					FunctionFactory.getGeneralInstance());
		} 
		else if (name.equals("VariableReference")) {
			myExpr = VariableReference.getInstance(root, metaData, manager);
		} 
		else if (name.equals("AttributeDesignator")) {
			myExpr = AttributeDesignator.getInstance(root);
		}

		return myExpr;
	}
	
	/**
	 * @param expr
	 * @param manager
	 * @param funcFactory
	 * @return d
	 * @throws ParsingException error parsing instance of ExpressionType
	 */
	public static ExpressionType getInstance(ExpressionType expr, FunctionFactory funcFactory, VariableManager manager) throws ParsingException {
		final ExpressionType exprHandler;		
		// We check all types of Expression: <Apply>, <AttributeSelector>, <AttributeValue>,
		// <Function>, <VariableReference> and <AttributeDesignator>
		if (expr instanceof ApplyType) {
			exprHandler = Apply.getInstance((ApplyType) expr, funcFactory, manager);
		} else if (expr instanceof AttributeDesignatorType) {
			exprHandler = AttributeDesignator.getInstance( (AttributeDesignatorType) expr);
		} else if (expr instanceof AttributeSelectorType) {
			exprHandler = AttributeSelector.getInstance((AttributeSelectorType) expr);
		} else if (expr instanceof AttributeValueType) {
			try
			{
				exprHandler = AttributeValue.getInstance((AttributeValueType) expr);
			} catch (UnknownIdentifierException e)
			{
				throw new ParsingException("Unknown AttributeValue datatype", e);
			}
		} else if (expr instanceof FunctionType) {
			final FunctionType func = (FunctionType) expr;
			try {
				final URI funcId = new URI(func.getFunctionId());
				exprHandler = funcFactory.createFunction(funcId);
			} catch (URISyntaxException use) {
				throw new ParsingException("Error parsing Apply", use);
			} catch (UnknownIdentifierException uie) {
				throw new ParsingException("Unknown FunctionId", uie);
			} catch (FunctionTypeException fte) {
				// try to create an abstract function
//				try {
//					URI funcId = new URI(funcName);
//					xpr = factory.createAbstractFunction(funcId, root);
//				} catch (Exception e) {
					// any exception here is an error
					throw new ParsingException("Unsupported function ID: " + func.getFunctionId(), fte);
//				}
			}
		} else if (expr instanceof VariableReferenceType) {
			final VariableReferenceType varRefElt = (VariableReferenceType) expr;
			exprHandler = new VariableReference(varRefElt.getVariableId(), manager);
		} else {
			throw new ParsingException(
					"Expression of type "
							+ expr.getClass().getSimpleName()
							+ " aren't supported. "
							+ "You must use Apply, AttributeSelector, AttributeValue, "
							+ "Function, VariableReference or AttributeDesignator");
		}

		return exprHandler;
	}


	/**
	 * Helper method that tries to get a function instance
	 */
	public static Function getFunction(Node root, PolicyMetaData metaData,
			FunctionFactory factory) throws ParsingException {
		Node functionNode = root.getAttributes().getNamedItem("FunctionId");
		String functionName = functionNode.getNodeValue();

		try {
			// try to get an instance of the given function
			return factory.createFunction(functionName);
		} catch (UnknownIdentifierException uie) {
			throw new ParsingException("Unknown FunctionId", uie);
		} catch (FunctionTypeException fte) {
			// try creating as an abstract function
			try {
				FunctionFactory ff = FunctionFactory.getGeneralInstance();
				return ff.createAbstractFunction(functionName, root,
						metaData.getXPathIdentifier());
			} catch (Exception e) {
				// any exception at this point is a failure
				throw new ParsingException("failed to create abstract function"
						+ " " + functionName, e);
			}
		}
	}
}
