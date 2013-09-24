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
package com.sun.xacml.cond.xacmlv3;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

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
