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

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.cond.VariableManager;
import com.sun.xacml.cond.VariableReference;
import com.thalesgroup.authzforce.core.PdpModelHandler;


/**
 * Represents the XACML ApplyType and ConditionType XML types.
 * <p>
 * Note well: as of 2.0, there is no longer a notion of a separate higher-
 * order bag function. Instead, if needed, it is supplied as one of the
 * <code>Expression</code>s in the parameter list. As such, when this
 * <code>Apply</code> is evaluated, it no longer pre-evaluates all the
 * parameters if a bag function is used. It is now up to the implementor
 * of a higher-order function to do this.
 * <p>
 * Also, as of 2.0, the <code>Apply</code> is no longer used to represent
 * a Condition, since the XACML 2.0 specification changed how Condition
 * works. Instead, there is now a <code>Condition</code> class that
 * represents both 1.x and 2.0 style Conditions.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class Apply extends ApplyType implements Evaluatable
{

    // the function used to evaluate the contents of the apply
    private Function function;

    // the paramaters to the function...ie, the contents of the apply
//    private List<ExpressionType> xprs;
    
    /**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Apply.class);

    /**
     * Constructs an <code>Apply</code> instance.
     * 
     * @param function the <code>Function</code> to use in evaluating the
     *                 elements in the apply
     * @param xprs the contents of the apply which will be the parameters
     *              to the function, each of which is an
     *              <code>Expression</code>
     *
     * @throws IllegalArgumentException if the input expressions don't
     *                                  match the signature of the function
     */
    public Apply(Function function, List<ExpressionType> xprs) throws IllegalArgumentException
    {
        // check that the given inputs work for the function
//        ((Expression)function).checkInputs(xprs);

        // if everything checks out, then store the inputs
    	this.functionId = function.getFunctionId();
        this.expressions = Collections.unmodifiableList(new ArrayList(xprs));
    	this.function = function;
    	
//        this.xprs = Collections.unmodifiableList(new ArrayList(xprs));
    }

    /**
     * Constructs an <code>Apply</code> instance.
     * 
     * @deprecated As of 2.0 <code>Apply</code> is no longer used for
     *             Conditions, so the <code>isCondition</code> parameter
     *             is no longer needed. You should now use the 2 parameter
     *             constructor. This constructor will be removed in a
     *             future release.
     *
     * @param function the <code>Function</code> to use in evaluating the
     *                 elements in the apply
     * @param xprs the contents of the apply which will be the parameters
     *              to the function, each of which is an
     *              <code>Expression</code>
     * @param isCondition as of 2.0, this must always be false
     *
     * @throws IllegalArgumentException if the input expressions don't
     *                                  match the signature of the function or
     *                                  if <code>isCondition</code> is true
     */
    public Apply(Function function, List xprs, boolean isCondition) throws IllegalArgumentException
    {
        // make sure that no is using this constructor to create a Condition
        if (isCondition) {
            throw new IllegalArgumentException("As of version 2.0 an Apply" +
                                               " may not represent a" +
                                               " Condition");
        }

        // check that the given inputs work for the function
        function.checkInputs(xprs);

        // if everything checks out, then store the inputs
        this.function = function;
//        this.xprs = Collections.unmodifiableList(new ArrayList(xprs));
        this.expressions = Collections.unmodifiableList(new ArrayList(xprs));
    }

    /**
     * Returns an instance of an <code>Apply</code> based on the given DOM
     * root node. This will actually return a special kind of
     * <code>Apply</code>, namely an XML ConditionType, which is the root
     * of the condition logic in a RuleType. A ConditionType is the same
     * as an ApplyType except that it must use a FunctionId that returns
     * a boolean value.
     * <p>
     * Note that as of 2.0 there is a separate <code>Condition</code> class
     * used to support the different kinds of Conditions in XACML 1.x and
     * 2.0. As such, the system no longer treats a ConditionType as a
     * special kind of ApplyType. You may still use this method to get a
     * 1.x style ConditionType, but you will need to convert it into a
     * <code>Condition</code> to use it in evaluation. The preferred way
     * to create a Condition is now through the <code>getInstance</code>
     * method on <code>Condition</code>.
     * 
     * @param root the DOM root of a ConditionType XML type
     * @param xpathVersion the XPath version to use in any selectors or XPath
     *                     functions, or null if this is unspecified (ie, not
     *                     supplied in the defaults section of the policy)
     * @param manager <code>VariableManager</code> used to connect references
     *                and definitions while parsing
     *
     * @throws ParsingException if this is not a valid ConditionType
     */
    public static Apply getConditionInstance(Node root, String xpathVersion,
                                             VariableManager manager)
        throws ParsingException
    {
        return getInstance(root, FunctionFactory.getConditionInstance(),
                           new PolicyMetaData(
                                   PolicyMetaData.XACML_1_0_IDENTIFIER,
                                   xpathVersion),
                           manager);
    }

    /**
     * Returns an instance of an <code>Apply</code> based on the given DOM
     * root node. This will actually return a special kind of
     * <code>Apply</code>, namely an XML ConditionType, which is the root
     * of the condition logic in a RuleType. A ConditionType is the same
     * as an ApplyType except that it must use a FunctionId that returns
     * a boolean value.
     * 
     * @deprecated As of 2.0 you should avoid using this method, since it
     *             does not provide a <code>Condition</code> instance and
     *             does not handle XACML 2.0 policies correctly. If you need
     *             a similar method you can use the new version that
     *             accepts a <code>VariableManager</code>. This will return
     *             an <code>Apply</code> instance for XACML 1.x policies.
     *
     * @param root the DOM root of a ConditionType XML type
     * @param xpathVersion the XPath version to use in any selectors or XPath
     *                     functions, or null if this is unspecified (ie, not
     *                     supplied in the defaults section of the policy)
     *
     * @throws ParsingException if this is not a valid ConditionType
     */
    public static Apply getConditionInstance(Node root, String xpathVersion)
        throws ParsingException
    {
        return getInstance(root, FunctionFactory.getConditionInstance(),
                           new PolicyMetaData(
                                   PolicyMetaData.XACML_1_0_IDENTIFIER,
                                   xpathVersion),
                           null);
    }

    /**
     * Returns an instance of <code>Apply</code> based on the given DOM root.
     * 
     * @param root the DOM root of an ApplyType XML type
     * @param metaData the meta-data associated with the containing policy
     * @param manager <code>VariableManager</code> used to connect references
     *                and definitions while parsing
     *
     * @throws ParsingException if this is not a valid ApplyType
     */
    public static Apply getInstance(Node root, PolicyMetaData metaData,
                                    VariableManager manager)
        throws ParsingException
    {
        return getInstance(root, FunctionFactory.getGeneralInstance(),
                           metaData, manager);
    }

    /**
     * Returns an instance of <code>Apply</code> based on the given DOM root.
     * 
     * @deprecated As of 2.0 you should avoid using this method, since it
     *             does not handle XACML 2.0 policies correctly. If you need
     *             a similar method you can use the new version that
     *             accepts a <code>VariableManager</code>. This will return
     *             an <code>Apply</code> instance for XACML 1.x policies.
     *
     * @param root the DOM root of an ApplyType XML type
     * @param xpathVersion the XPath version to use in any selectors or XPath
     *                     functions, or null if this is unspecified (ie, not
     *                     supplied in the defaults section of the policy)
     *
     * @throws ParsingException if this is not a valid ApplyType
     */
    public static Apply getInstance(Node root, String xpathVersion)
        throws ParsingException
    {
        return getInstance(root, FunctionFactory.getGeneralInstance(),
                           new PolicyMetaData(
                                   PolicyMetaData.XACML_1_0_IDENTIFIER,
                                   xpathVersion),
                           null);
    }

    /**
     * This is a helper method that is called by the two getInstance
     * methods. It takes a factory so we know that we're getting the right
     * kind of function.
     */
    private static Apply getInstance(Node root, FunctionFactory factory,
                                     PolicyMetaData metaData,
                                     VariableManager manager)
        throws ParsingException
    {
    	Function function = null;
//    	ExpressionType xpr = null;
		AttributeValue attrValue = null;
		List xprs = new ArrayList();
//        Function function = ExpressionTools.getFunction(root, metaData, factory);        
//        List xprs = new ArrayList();
//
//        NodeList nodes = root.getChildNodes();
//        for (int i = 0; i < nodes.getLength(); i++) {
//            ExpressionType xpr = ExpressionTools.getExpression(nodes.item(i), metaData, manager);
//
//            if (xpr != null) {
//                xprs.add(xpr);
//            }
//        }
  

		// get the function type, making sure that it's really a correct
		// Target function
		String funcName = root.getAttributes().getNamedItem("FunctionId").getNodeValue();
		try {
			URI funcId = new URI(funcName);
			function = factory.createFunction(funcId);
		} catch (URISyntaxException use) {
			throw new ParsingException("Error parsing Apply", use);
		} catch (UnknownIdentifierException uie) {
			throw new ParsingException("Unknown FunctionId", uie);
		} catch (FunctionTypeException fte) {
			// try to create an abstract function
			try {
				URI funcId = new URI(funcName);
				function = factory.createAbstractFunction(funcId, root);
			} catch (Exception e) {
				// any exception here is an error
				throw new ParsingException("invalid abstract function", e);
			}
		}

		// next, get the function, the designator or selector being used, and the attribute
		// value paired with it
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getNodeName();
			ExpressionType xpr = null;
			if (name.equals("Apply")) {
				xpr = Apply.getInstance(node, metaData, manager);
			} else if (name.equals("AttributeDesignator")) {
				xpr = AttributeDesignator.getInstance(node);
			} else if (name.equals("AttributeSelector")) {
				xpr = AttributeSelector.getInstance(node, metaData);
			} else if (name.equals("AttributeValue")) {
				xpr = AttributeValue.getInstance(node, metaData);
			} else if (name.equals("Function")) {
				funcName = node.getAttributes().getNamedItem("FunctionId").getNodeValue();
				try {
					URI funcId = new URI(funcName);
					xpr = factory.createFunction(funcId);
				} catch (URISyntaxException use) {
					throw new ParsingException("Error parsing Apply", use);
				} catch (UnknownIdentifierException uie) {
					throw new ParsingException("Unknown FunctionId", uie);
				} catch (FunctionTypeException fte) {
					// try to create an abstract function
					try {
						URI funcId = new URI(funcName);
						xpr = factory.createAbstractFunction(funcId, root);
					} catch (Exception e) {
						// any exception here is an error
						throw new ParsingException("invalid abstract function", e);
					}
				}
			} else if (name.equals("VariableReference")) {
				xpr = VariableReference.getInstance(root, metaData, manager);
			} 
			
			if (xpr != null) {
              xprs.add(xpr);
          }
		}
    	
    	

        return new Apply(function, xprs);
    }
    
    /**
     * Get instance of Apply handler based on Apply element from OASIS XACML model
     * @param applyElt
     * @param funcFactory function factory for instantiating functions in Apply
     * @param varManager
     * @return Apply handler
     * @throws ParsingException
     */
    public static Apply getInstance(ApplyType applyElt, FunctionFactory funcFactory, VariableManager varManager) throws ParsingException {
    	final Function function;
    	final URI funcId;
    	try {
			funcId = new URI(applyElt.getFunctionId());
			function = funcFactory.createFunction(funcId);
		} catch (URISyntaxException use) {
			throw new ParsingException("Error parsing Apply", use);
		} catch (UnknownIdentifierException uie) {
			throw new ParsingException("Unknown FunctionId", uie);
		} catch (FunctionTypeException fte) {
			// try to create an abstract function
			throw new ParsingException("Unsupported function: " + applyElt.getFunctionId(), fte);
		}
    	
    	final List<ExpressionType> exprHandlers = new ArrayList<>();
    	for(final JAXBElement<? extends ExpressionType> exprElt: applyElt.getExpressions()) {
    		final ExpressionType exprHandler = ExpressionTools.getInstance(exprElt.getValue(), funcFactory, varManager);
    		exprHandlers.add(exprHandler);
    	}  	
    	
    	return new Apply(function, exprHandlers);
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
    
    /**
     * Returns the <code>Function</code> used by this <code>Apply</code>.
     *
     * @return the <code>Function</code>
     */
    public ExpressionType getFunction() {
        return function;
    }

    /**
     * Returns the <code>List</code> of children for this <code>Apply</code>.
     * The <code>List</code> contains <code>Expression</code>s. The list is
     * unmodifiable, and may be empty.
     *
     * @return a <code>List</code> of <code>Expression</code>s
     */
    @Override
    public List getChildren() {
        return expressions;
    }

    /**
     * Returns whether or not this ApplyType is actually a ConditionType. As
     * of 2.0 this always returns false;
     *
     * @deprecated As of 2.0 this method should not be used, since an
     *             <code>Apply</code> is never a Condition.
     *
     * @return false
     */
    public boolean isCondition() {
        return false;
    }

    /**
     * Evaluates the apply object using the given function. This will in
     * turn call evaluate on all the given parameters, some of which may be
     * other <code>Apply</code> objects.
     *
     * @param context the representation of the request
     *
     * @return the result of trying to evaluate this apply object
     */
    @Override
    public EvaluationResult evaluate(EvaluationCtx context) {
        return function.evaluate(expressions, context);
    }

    /**
     * Returns the type of attribute that this object will return on a call
     * to <code>evaluate</code>. In practice, this will always be the same as
     * the result of calling <code>getReturnType</code> on the function used
     * by this object.
     *
     * @return the type returned by <code>evaluate</code>
     */
    public URI getType() {
//        return function.getType();
    	return null;
    }

    /**
     * Returns whether or not the <code>Function</code> will return a bag
     * of values on evaluation.
     *
     * @return true if evaluation will return a bag of values, false otherwise
     */    
    public boolean returnsBag() {
//        return function.returnsBag();
    	return false;
    }

    /**
     * Returns whether or not the <code>Function</code> will return a bag
     * of values on evaluation.
     *
     *
     * @deprecated As of 2.0, you should use the <code>returnsBag</code>
     *             method from the super-interface <code>Expression</code>.
     *
     * @return true if evaluation will return a bag of values, false otherwise
     */
    @Override
    public boolean evaluatesToBag() {
        return false;
    }

    /**
     * Encodes this <code>Apply</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>Apply</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
    	PrintStream out = new PrintStream(output);
		try {
			Marshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			/*
			 *  ApplyType does not have XmlRoot annotation so we need to wrap it in JAXBElement in order to marshall it
			 */
	        final JAXBElement<ApplyType> root = PdpModelHandler.XACML_OBJECT_FACTORY.createApply(this);
			u.marshal(root, out);
		} catch (Exception e) {
			LOGGER.error("Error marshalling Apply",e);
		}  
    }

}
