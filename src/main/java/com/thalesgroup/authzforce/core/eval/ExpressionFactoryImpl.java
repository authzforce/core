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
package com.thalesgroup.authzforce.core.eval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.Apply;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinderImpl;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.func.FunctionRegistry;
import com.thalesgroup.authzforce.xacml.schema.XPATHVersion;

/**
 * Implementation of ExpressionFactory that supports the Expressions defined in VariableDefinitions
 * in order to resolve VariableReferences. In particular, it makes sure the depth of recursivity of
 * VariableDefinition does not exceed a value (to avoid inconveniences such as stackoverflow or very
 * negative performance impact) defined by parameter to
 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, CloseableAttributeFinder, int, boolean, Map)}
 * parameter. Note that reference loops are avoided by the fact that a VariableReference can
 * reference only a VariableDefinition defined previously to the VariableReference in this
 * implementation.
 * 
 */
public class ExpressionFactoryImpl implements ExpressionFactory
{
	private static final ParsingException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new ParsingException("Unsupported Expression type (optional XACML feature): AttributeSelector");

	private final DatatypeFactoryRegistry attributeFactoryRegistry;
	private final FunctionRegistry functionRegistry;
	private final CloseableAttributeFinder attributeFinder;
	private final int maxVariableReferenceDepth;
	// the map from identifiers to internal data
	private final Map<String, VariableReference<?>> idToVariableMap = new HashMap<>();
	private final boolean allowAttributeSelectors;
	private final Map<String, XPathCompiler> xpathCompilersByVersionURI;

	/**
	 * Maximum VariableReference depth allowed for VariableDefinitions to be managed. Examples:
	 * <ul>
	 * <li>
	 * A VariableDefinition V1 that does not use any VariableReference has a reference depth of 0.</li>
	 * <li>
	 * A VariableDefinition V1 that uses a VariableReference to VariableDefinition V2 with no
	 * further VariableReference, has a reference depth of 1</li>
	 * <li>etc.</li>
	 * </ul>
	 * 
	 * @param attrFactory
	 *            attribute value factory
	 * @param functionRegistry
	 *            function registry
	 * @param attributeFinder
	 *            Attribute Finder for AttributeDesignator expressions that will need it at
	 *            evaluation time
	 * @param xpathCompilersByVersionURI
	 *            XPATH compilers by XPATH version specification URI, as returned by
	 *            {@link XPATHVersion#getURI()}, for XPath evaluation in AttributeSelector
	 *            evaluation; may be null if {@code allowAttributeSelectors == false}
	 * @param maxVarRefDepth
	 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition
	 *            ->... ('->' represents a VariableReference)
	 * @param allowAttributeSelectors
	 *            allow use of AttributeSelectors (experimental, not for production, use with
	 *            caution)
	 */
	public ExpressionFactoryImpl(DatatypeFactoryRegistry attrFactory, FunctionRegistry functionRegistry, CloseableAttributeFinder attributeFinder, int maxVarRefDepth, boolean allowAttributeSelectors, Map<String, XPathCompiler> xpathCompilersByVersionURI)
	{
		this.attributeFactoryRegistry = attrFactory;
		this.functionRegistry = functionRegistry;
		if (maxVarRefDepth < 0)
		{
			throw new IllegalArgumentException("Invalid max VariableReference depth: " + maxVarRefDepth + ". Expected: (integer) > 0");
		}

		this.maxVariableReferenceDepth = maxVarRefDepth;
		this.attributeFinder = attributeFinder;
		this.xpathCompilersByVersionURI = xpathCompilersByVersionURI;
		this.allowAttributeSelectors = allowAttributeSelectors;
	}

	/**
	 * Add VariableDefinition to be managed
	 * 
	 * @param varDef
	 *            VariableDefinition
	 * @param policyDefaultValues
	 *            Policy(Set) default values, such XPath version
	 * @return The previous VariableReference if VariableId already used
	 * @throws ParsingException
	 *             error parsing expression in <code>var</code>, in particular if reference depth
	 *             exceeded (as fixed by max parameter to
	 *             {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, CloseableAttributeFinderImpl, int, boolean, Map)}
	 *             )
	 */
	@Override
	public VariableReference<?> addVariable(oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition varDef, DefaultsType policyDefaultValues) throws ParsingException
	{
		final String varId = varDef.getVariableId();
		/*
		 * Initialize the longest variable reference chain from this VariableDefinition (varDef ->
		 * VarDef2 -> ..., where "v1 -> v2" means: v1's expression contains a VariableReference to
		 * v2) as empty for later update by this#getDefinition() when resolving a VariableReference
		 * within this varDef's expression (being parsed just after). The goal is to detect chains
		 * longer than this.maxVariableReferenceDepth to limit abuse of VariableReferences. There
		 * may be multiple VariableReferences in a VariableDefinition's expression, such as an
		 * Apply, and each may be referencing a different VariableDefinition; but we are interested
		 * only in the one with the longest chain of references.
		 */
		/*
		 * we need to check the longest variableReference chain does not have circular reference and
		 * does not exceed a specific value (need to call contains() method repeatedly and preserve
		 * the order).
		 */
		final List<String> longestVarRefChain = new ArrayList<>();
		final Expression<?> varExpr = getInstance(varDef.getExpression().getValue(), policyDefaultValues, longestVarRefChain);
		final VariableReference<?> var = new VariableReference<>(varId, varExpr, longestVarRefChain);
		return idToVariableMap.put(varId, var);
	}

	/**
	 * Removes the VariableReference(Definition) from the manager
	 * 
	 * @param varId
	 * @return the VariableReference previously identified by <code>varId</code>, or null if there
	 *         was no such variable.
	 */
	@Override
	public VariableReference<?> removeVariable(String varId)
	{
		return idToVariableMap.remove(varId);
	}

	/**
	 * Resolves a VariableReference to the corresponding VariableReference(Definition) and validates
	 * the depth of VariableReference, i.e. the length of VariableReference chain. A chain of
	 * variable references is a list of VariableIds, such that V1 -> V2 ->... -> Vn, where 'V1 ->
	 * V2' means: V1's Expression contains a VariableReference to V2.
	 * 
	 * @param jaxbVarRef
	 *            the JAXB/XACML VariableReference with merely identifying a VariableDefinition by
	 *            its VariableId
	 * 
	 * @return VariableReference containing the resolved VariableDefinition's expression referenced
	 *         by <code>jaxbVarRef</code> as known by this factory, or null if unknown
	 * @param longestVarRefChain
	 *            If we are resolving a VariableReference in a VariableDefinition's expression (may
	 *            be null if not), this is the longest chain of VariableReferences starting from a
	 *            one in this VariableDefinition. If we are not resolving a VariableReference in a
	 *            VariableDefinition's expression, this may be null.This is used to detect exceeding
	 *            reference depth (see {@link #ExpressionFactoryImpl(int)} for the limit. In a
	 *            Expression such as an Apply, we can have multiple VariableReferences referencing
	 *            different VariableDefinitions. So we can have different depths of
	 *            VariableReference references. We compare the length of the current longest chain
	 *            with the one we would get by adding the longest one in the referenced
	 *            VariableDefinition and <code>jaxbVarRef</code>'s VariableId. If the latter is
	 *            longer, its content becomes the content <code>longestVarRefChain</code>.
	 * 
	 * @throws ParsingException
	 *             if the length of VariableReference chain is greater than the defined max for this
	 *             factory
	 * @throws UnknownIdentifierException
	 *             if VariableReference's VariableId is unknown by this factory
	 */
	private VariableReference<?> getVariable(VariableReferenceType jaxbVarRef, List<String> longestVarRefChain) throws UnknownIdentifierException, ParsingException
	{
		final String varId = jaxbVarRef.getVariableId();
		final VariableReference<?> var = idToVariableMap.get(varId);
		if (var == null)
		{
			throw new UnknownIdentifierException("VariableReference's VariableId=" + varId + " does not match any prior VariableDefinition's VariableId");
		}

		if (longestVarRefChain != null)
		{
			final List<String> referencedVarLongestRefChain = var.getLongestVariableReferenceChain();
			if (referencedVarLongestRefChain.size() + 1 > longestVarRefChain.size())
			{
				// current longest is no longer the longest, so replace with new longest's content
				longestVarRefChain.clear();
				longestVarRefChain.addAll(referencedVarLongestRefChain);
				longestVarRefChain.add(varId);
			}

			if (longestVarRefChain.size() > this.maxVariableReferenceDepth)
			{
				throw new ParsingException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length (" + longestVarRefChain.size() + ") of VariableReference Reference chain: " + longestVarRefChain);
			}

		}

		return var;
	}

	/**
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, CloseableAttributeFinderImpl, int, boolean, Map)}
	 * .
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @return function instance; or null if no such function with ID {@code functionId}
	 */
	@Override
	public Function<?> getFunction(String functionId)
	{
		return this.functionRegistry.getFunction(functionId);
	}

	/**
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, CloseableAttributeFinderImpl, int, boolean, Map)}
	 * .
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @param subFunctionReturnType
	 *            optional sub-function's return type required only if a generic higher-order
	 *            function is expected as the result, of which the sub-function is expected to be
	 *            the first parameter; otherwise null (for first-order function). A generic
	 *            higher-order function is a function whose return type depends on the sub-function
	 *            ('s return type).
	 * @return function instance; or null if no such function with ID {@code functionId}, or if
	 *         non-null {@code subFunctionReturnTypeId} specified and no higher-order function
	 *         compatible with sub-function's return type {@code subFunctionReturnTypeId}
	 * @throws UnknownIdentifierException
	 *             if datatype {@code subFunctionReturnType} is not supported/known
	 * 
	 */
	@Override
	public Function<?> getFunction(String functionId, Datatype<?> subFunctionReturnType) throws UnknownIdentifierException
	{
		if (subFunctionReturnType == null)
		{
			return getFunction(functionId);
		}

		final AttributeValue.Factory<?> subFuncReturnTypeFactory = this.attributeFactoryRegistry.getExtension(subFunctionReturnType.getId());
		if (subFuncReturnTypeFactory == null)
		{
			throw new UnknownIdentifierException("Invalid sub-function's return type specified: unknown/unsupported ID: " + subFunctionReturnType.getId());
		}

		return this.functionRegistry.getFunction(functionId, subFuncReturnTypeFactory.getDatatype());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thalesgroup.authzforce.core.eval.ExpressionFactory#getInstance(oasis.names.tc.xacml._3_0
	 * .core.schema.wd_17.ExpressionType, oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType,
	 * java.util.List)
	 */
	@Override
	public Expression<?> getInstance(ExpressionType expr, DefaultsType policyDefaultValues, List<String> longestVarRefChain) throws ParsingException
	{
		final Expression<?> expression;
		/*
		 * We check all types of Expression: <Apply>, <AttributeSelector>, <AttributeValue>,
		 * <Function>, <VariableReference> and <AttributeDesignator>
		 */
		if (expr instanceof ApplyType)
		{
			expression = Apply.getInstance((ApplyType) expr, policyDefaultValues, this, longestVarRefChain);
		} else if (expr instanceof AttributeDesignatorType)
		{
			final AttributeDesignatorType jaxbAttrDes = (AttributeDesignatorType) expr;
			final AttributeValue.Factory<?> attrFactory = attributeFactoryRegistry.getExtension(jaxbAttrDes.getDataType());
			if (attrFactory == null)
			{
				throw new ParsingException("Unsupported Datatype used in AttributeDesignator: " + jaxbAttrDes.getDataType());
			}

			expression = new AttributeDesignator<>(jaxbAttrDes, attrFactory.getBagDatatype(), attributeFinder);
		} else if (expr instanceof AttributeSelectorType)
		{
			if (!allowAttributeSelectors)
			{
				throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
			}

			final AttributeSelectorType jaxbAttrSelector = (AttributeSelectorType) expr;
			final AttributeValue.Factory<?> attrFactory = attributeFactoryRegistry.getExtension(jaxbAttrSelector.getDataType());
			if (attrFactory == null)
			{
				throw new ParsingException("Unsupported Datatype used in AttributeSelector: " + jaxbAttrSelector.getDataType());
			}

			// Default XPath version is 1.0 unless Policy(Set)Defaults specified
			final XPathCompiler xpathCompiler = xpathCompilersByVersionURI.get(policyDefaultValues == null ? XPATHVersion.V1_0.getURI() : policyDefaultValues.getXPathVersion());
			try
			{
				expression = new AttributeSelector<>(jaxbAttrSelector, xpathCompiler, attributeFinder, attrFactory);
			} catch (XPathExpressionException e)
			{
				throw new ParsingException("Error parsing AttributeSelector's Path='" + jaxbAttrSelector.getPath() + "' into a XPath expression", e);
			}
		} else if (expr instanceof AttributeValueType)
		{
			expression = createAttributeValue((AttributeValueType) expr);
		} else if (expr instanceof FunctionType)
		{
			final FunctionType jaxbFunc = (FunctionType) expr;
			final Function<?> func = getFunction(jaxbFunc.getFunctionId());
			if (func != null)
			{
				expression = func;
			} else
			{
				throw new ParsingException("Function " + jaxbFunc.getFunctionId() + " is not supported (at least) as standalone Expression: either a generic higher-order function supported only as Apply FunctionId, or function completely unknown.");
			}
		} else if (expr instanceof VariableReferenceType)
		{
			final VariableReferenceType varRefElt = (VariableReferenceType) expr;
			try
			{
				expression = getVariable(varRefElt, longestVarRefChain);
			} catch (UnknownIdentifierException e)
			{
				throw new ParsingException("Invalid VariableReference/VariableId", e);
			}
		} else
		{
			throw new ParsingException("Expressions of type " + expr.getClass().getSimpleName() + " are not supported. Expected: one of Apply, AttributeDesignator, AttributeSelector, AttributeValue, Function or VariableReference.");
		}

		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thalesgroup.authzforce.core.eval.ExpressionFactory#createAttributeValueExpression(oasis
	 * .names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType)
	 */
	@Override
	public AttributeValue<?> createAttributeValue(AttributeValueType jaxbAttrVal) throws ParsingException
	{
		try
		{
			return this.attributeFactoryRegistry.createValue(jaxbAttrVal);
		} catch (UnknownIdentifierException e)
		{
			throw new ParsingException("Error parsing AttributeValue expression: invalid/unsupported datatype URI", e);
		}
	}

	@Override
	public void close() throws IOException
	{
		attributeFinder.close();
	}

}
