/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.ValueExpression;
import org.ow2.authzforce.core.pdp.api.VariableReference;
import org.ow2.authzforce.core.pdp.impl.CloseableAttributeProvider;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;

import com.sun.xacml.UnknownIdentifierException;

/**
 * Implementation of ExpressionFactory that supports the Expressions defined in VariableDefinitions in order to resolve VariableReferences. In particular, it makes sure the depth of recursivity of
 * VariableDefinition does not exceed a value (to avoid inconveniences such as stackoverflow or very negative performance impact) defined by parameter to
 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean)} parameter. Note that reference loops are avoided by the fact that a VariableReference can
 * reference only a VariableDefinition defined previously to the VariableReference in this implementation.
 *
 * @author cdangerv
 * @version $Id: $
 */
public class ExpressionFactoryImpl implements ExpressionFactory
{
	private static final IllegalArgumentException MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION = new IllegalArgumentException(
			"Missing Issuer that is required on AttributeDesignators by PDP configuration");

	private static final IllegalArgumentException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException("Unsupported Expression type (optional XACML feature): AttributeSelector");

	private static final IllegalArgumentException NULL_FUNCTION_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined function registry");

	private static final IllegalArgumentException NULL_ATTRIBUTE_DATATYPE_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype registry");

	private static final IllegalArgumentException UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION = new IllegalArgumentException(
			"Unsupported Expression type 'AttributeDesignator' and 'AttributeSelector' because no attribute Provider defined");

	private static final int UNLIMITED_MAX_VARIABLE_REF_DEPTH = -1;

	private final DatatypeFactoryRegistry datatypeFactoryRegistry;
	private final FunctionRegistry functionRegistry;
	private final CloseableAttributeProvider attributeProvider;
	private final int maxVariableReferenceDepth;
	// the map from identifiers to internal data
	private final Map<String, BaseVariableReference<?>> idToVariableMap = new HashMap<>();
	private final boolean allowAttributeSelectors;

	private final boolean issuerRequiredOnAttributeDesignators;

	/**
	 * Maximum VariableReference depth allowed for VariableDefinitions to be managed. Examples:
	 * <ul>
	 * <li>A VariableDefinition V1 that does not use any VariableReference has a reference depth of 0.</li>
	 * <li>A VariableDefinition V1 that uses a VariableReference to VariableDefinition V2 with no further VariableReference, has a reference depth of 1</li>
	 * <li>etc.</li>
	 * </ul>
	 *
	 * @param attributeFactory
	 *            attribute value factory (not null)
	 * @param functionRegistry
	 *            function registry (not null)
	 * @param jaxbAttributeProviderConfs
	 *            XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static expression evaluation (out of context), in which case
	 *            AttributeSelectors/AttributeDesignators are not supported
	 * @param maxVarRefDepth
	 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference); strictly negative value means unlimited
	 * @param allowAttributeSelectors
	 *            allow use of AttributeSelectors (experimental, not for production, use with caution)
	 * @param issuerRequiredInAttributeDesignators
	 *            true iff it is required that all AttributeDesignator set the Issuer field, as a best practice. If the issuer is not set, remember what XACML 3.0 AttributeDesignator Evaluation says:
	 *            "If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone."
	 *            As a result, be aware that if you use AttributeDesignators without Issuer ( {@code issuerRequiredInAttributeDesignators == false}) and the requests are using matching Attributes but
	 *            with one or more different Issuers, this PDP engine has to gather all the values from all the attributes with matching Category/AttributeId but with any Issuer or no Issuer,
	 *            resulting in lower performance.
	 * @throws java.lang.IllegalArgumentException
	 *             If any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict with another one already registered to
	 *             provide the same or part of the same attributes.
	 * @throws java.io.IOException
	 *             error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when and before an {@link IllegalArgumentException} is raised
	 */
	public ExpressionFactoryImpl(DatatypeFactoryRegistry attributeFactory, FunctionRegistry functionRegistry, List<AbstractAttributeProvider> jaxbAttributeProviderConfs, int maxVarRefDepth,
			boolean allowAttributeSelectors, boolean issuerRequiredInAttributeDesignators) throws IllegalArgumentException, IOException
	{
		if (attributeFactory == null)
		{
			throw NULL_ATTRIBUTE_DATATYPE_REGISTRY_EXCEPTION;
		}

		if (functionRegistry == null)
		{
			throw NULL_FUNCTION_REGISTRY_EXCEPTION;
		}

		this.datatypeFactoryRegistry = attributeFactory;
		this.functionRegistry = functionRegistry;
		this.maxVariableReferenceDepth = maxVarRefDepth < 0 ? UNLIMITED_MAX_VARIABLE_REF_DEPTH : maxVarRefDepth;
		// finally create the global attribute Provider used to resolve
		// AttributeDesignators
		this.attributeProvider = CloseableAttributeProvider.getInstance(jaxbAttributeProviderConfs, attributeFactory);
		this.allowAttributeSelectors = allowAttributeSelectors;
		this.issuerRequiredOnAttributeDesignators = issuerRequiredInAttributeDesignators;
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> addVariable(VariableDefinition varDef, XPathCompiler xPathCompiler, Deque<String> inoutLongestVarRefChain) throws IllegalArgumentException
	{
		final String varId = varDef.getVariableId();
		/*
		 * Initialize the longest variable reference chain from this VariableDefinition (varDef -> VarDef2 -> ..., where "v1 -> v2" means: v1's expression contains a VariableReference to v2) as empty
		 * for later update by this#getDefinition() when resolving a VariableReference within this varDef's expression (being parsed just after). The goal is to detect chains longer than
		 * this.maxVariableReferenceDepth to limit abuse of VariableReferences. There may be multiple VariableReferences in a VariableDefinition's expression, such as an Apply, and each may be
		 * referencing a different VariableDefinition; but we are interested only in the one with the longest chain of references.
		 */
		/*
		 * we need to check the longest variableReference chain does not have circular reference and does not exceed a specific value (need to call contains() method repeatedly and preserve the
		 * order).
		 */
		final Deque<String> longestVarRefChain = inoutLongestVarRefChain == null ? new ArrayDeque<String>() : inoutLongestVarRefChain;
		final Expression<?> varExpr = getInstance(varDef.getExpression().getValue(), xPathCompiler, longestVarRefChain);
		final BaseVariableReference<?> var = new BaseVariableReference<>(varId, varExpr, longestVarRefChain);
		return idToVariableMap.put(varId, var);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> removeVariable(String varId)
	{
		return idToVariableMap.remove(varId);
	}

	/**
	 * Resolves a VariableReference to the corresponding VariableReference(Definition) and validates the depth of VariableReference, i.e. the length of VariableReference chain. A chain of variable
	 * references is a list of VariableIds, such that V1 -> V2 ->... -> Vn, where 'V1 -> V2' means: V1's Expression contains a VariableReference to V2.
	 * 
	 * @param jaxbVarRef
	 *            the JAXB/XACML VariableReference with merely identifying a VariableDefinition by its VariableId
	 * 
	 * @return VariableReference containing the resolved VariableDefinition's expression referenced by <code>jaxbVarRef</code> as known by this factory, or null if unknown
	 * @param longestVarRefChain
	 *            If we are resolving a VariableReference in a VariableDefinition's expression (may be null if not), this is the longest chain of VariableReferences starting from a one in this
	 *            VariableDefinition. If we are not resolving a VariableReference in a VariableDefinition's expression, this may be null.This is used to detect exceeding reference depth (see
	 *            {@link #ExpressionFactoryImpl(int)} for the limit. In a Expression such as an Apply, we can have multiple VariableReferences referencing different VariableDefinitions. So we can have
	 *            different depths of VariableReference references. We compare the length of the current longest chain with the one we would get by adding the longest one in the referenced
	 *            VariableDefinition and <code>jaxbVarRef</code>'s VariableId. If the latter is longer, its content becomes the content <code>longestVarRefChain</code>.
	 * @throws UnknownIdentifierException
	 *             if VariableReference's VariableId is unknown by this factory
	 */
	private BaseVariableReference<?> getVariable(VariableReferenceType jaxbVarRef, Deque<String> longestVarRefChain) throws IllegalArgumentException
	{
		final String varId = jaxbVarRef.getVariableId();
		final BaseVariableReference<?> var = idToVariableMap.get(varId);
		if (var == null)
		{
			throw new IllegalArgumentException("VariableReference's VariableId=" + varId + " unknown in the current context, i.e. does not match any prior VariableDefinition's VariableId");
		}

		if (longestVarRefChain != null)
		{
			final Deque<String> referencedVarLongestRefChain = var.getLongestVariableReferenceChain();
			if (referencedVarLongestRefChain.size() + 1 > longestVarRefChain.size())
			{
				// current longest is no longer the longest, so replace with new
				// longest's content
				longestVarRefChain.clear();
				longestVarRefChain.add(varId);
				longestVarRefChain.addAll(referencedVarLongestRefChain);
			}

			if (maxVariableReferenceDepth != UNLIMITED_MAX_VARIABLE_REF_DEPTH && longestVarRefChain.size() > this.maxVariableReferenceDepth)
			{
				throw new IllegalArgumentException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length (" + longestVarRefChain.size()
						+ ") of VariableReference Reference chain: " + longestVarRefChain);
			}

		}

		return var;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean)} .
	 */
	@Override
	public Function<?> getFunction(String functionId)
	{
		return this.functionRegistry.getFunction(functionId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean)} .
	 */
	@Override
	public Function<?> getFunction(String functionId, Datatype<?> subFunctionReturnType) throws IllegalArgumentException
	{
		if (subFunctionReturnType == null)
		{
			return getFunction(functionId);
		}

		final DatatypeFactory<?> subFuncReturnTypeFactory = this.datatypeFactoryRegistry.getExtension(subFunctionReturnType.getId());
		if (subFuncReturnTypeFactory == null)
		{
			throw new IllegalArgumentException("Invalid sub-function's return type specified: unknown/unsupported ID: " + subFunctionReturnType.getId());
		}

		return this.functionRegistry.getFunction(functionId, subFuncReturnTypeFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.ExpressionFactory#getInstance(oasis. names.tc.xacml._3_0 .core.schema.wd_17.ExpressionType, oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType,
	 * java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public Expression<?> getInstance(ExpressionType expr, XPathCompiler xPathCompiler, Deque<String> longestVarRefChain) throws IllegalArgumentException
	{
		final Expression<?> expression;
		/*
		 * We check all types of Expression: <Apply>, <AttributeSelector>, <AttributeValue>, <Function>, <VariableReference> and <AttributeDesignator>
		 */
		if (expr instanceof ApplyType)
		{
			expression = Apply.getInstance((ApplyType) expr, xPathCompiler, this, longestVarRefChain);
		} else if (expr instanceof AttributeDesignatorType)
		{
			if (this.attributeProvider == null)
			{
				throw UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
			}

			final AttributeDesignatorType jaxbAttrDes = (AttributeDesignatorType) expr;
			if (this.issuerRequiredOnAttributeDesignators && jaxbAttrDes.getIssuer() == null)
			{
				throw MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION;
			}

			final DatatypeFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrDes.getDataType());
			if (attrFactory == null)
			{
				throw new IllegalArgumentException("Unsupported Datatype used in AttributeDesignator: " + jaxbAttrDes.getDataType());
			}

			expression = new AttributeDesignator<>(jaxbAttrDes, attrFactory.getBagDatatype(), attributeProvider);
		} else if (expr instanceof AttributeSelectorType)
		{
			if (!allowAttributeSelectors)
			{
				throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
			}

			if (this.attributeProvider == null)
			{
				throw UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
			}

			final AttributeSelectorType jaxbAttrSelector = (AttributeSelectorType) expr;
			final DatatypeFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrSelector.getDataType());
			if (attrFactory == null)
			{
				throw new IllegalArgumentException("Unsupported Datatype used in AttributeSelector: " + jaxbAttrSelector.getDataType());
			}

			// Check whether default XPath compiler/version specified for the
			// XPath evaluator
			if (xPathCompiler == null)
			{
				throw new IllegalArgumentException("AttributeSelector found but missing Policy(Set)Defaults/XPathVersion required for XPath evaluation in AttributeSelector");
			}

			try
			{
				expression = new AttributeSelectorExpression<>(jaxbAttrSelector, xPathCompiler, attributeProvider, attrFactory);
			} catch (XPathExpressionException e)
			{
				throw new IllegalArgumentException("Invalid AttributeSelector's Path='" + jaxbAttrSelector.getPath() + "' into a XPath expression", e);
			}
		} else if (expr instanceof AttributeValueType)
		{
			expression = getInstance((AttributeValueType) expr, xPathCompiler);
		} else if (expr instanceof FunctionType)
		{
			final FunctionType jaxbFunc = (FunctionType) expr;
			final Function<?> func = getFunction(jaxbFunc.getFunctionId());
			if (func != null)
			{
				expression = func;
			} else
			{
				throw new IllegalArgumentException("Function " + jaxbFunc.getFunctionId()
						+ " is not supported (at least) as standalone Expression: either a generic higher-order function supported only as Apply FunctionId, or function completely unknown.");
			}
		} else if (expr instanceof VariableReferenceType)
		{
			final VariableReferenceType varRefElt = (VariableReferenceType) expr;
			expression = getVariable(varRefElt, longestVarRefChain);
		} else
		{
			throw new IllegalArgumentException("Expressions of type " + expr.getClass().getSimpleName()
					+ " are not supported. Expected: one of Apply, AttributeDesignator, AttributeSelector, AttributeValue, Function or VariableReference.");
		}

		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.ExpressionFactory# createAttributeValueExpression(oasis .names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType)
	 */
	/** {@inheritDoc} */
	@Override
	public ValueExpression<? extends AttributeValue> getInstance(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		return this.datatypeFactoryRegistry.createValueExpression(jaxbAttrVal, xPathCompiler);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		if (attributeProvider != null)
		{
			attributeProvider.close();
		}
	}

}
