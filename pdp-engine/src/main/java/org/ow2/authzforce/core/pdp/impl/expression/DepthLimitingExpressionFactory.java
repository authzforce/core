/*
 * Copyright 2012-2022 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

import com.google.common.base.Preconditions;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.*;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.core.pdp.impl.*;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * Implementation of ExpressionFactory that supports the Expressions defined in VariableDefinitions in order to resolve VariableReferences. In particular, it makes sure the depth of recursion of
 * VariableDefinition does not exceed a value (to avoid inconveniences such as stackoverflow or very negative performance impact) defined by {@code maxVarRefDef} parameter to
 * {@link #DepthLimitingExpressionFactory(AttributeValueFactoryRegistry, FunctionRegistry, int, boolean, boolean, Optional)}. Note that reference loops are avoided by the fact that a VariableReference can
 * reference only a VariableDefinition defined previously to the VariableReference in this implementation.
 *
 * 
 * @version $Id: $
 */
public final class DepthLimitingExpressionFactory implements ExpressionFactory
{

	/**
	 * Base class for evaluating XACML VariableReferences that holds the variable ID and longest VariableReference chain found in the referenced variable's expression, in order to detect abuse of such
	 * chains, which is unsafe.
	 *
	 * @param <V>
	 *            evaluation's return type
	 * 
	 * @version $Id: $
	 */
	private static abstract class BaseVariableReference<V extends Value> implements VariableReference<V>
	{

		protected final String variableId;
		private final transient Deque<String> longestVariableReferenceChain;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            variable ID
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		private BaseVariableReference(final String varId, final Deque<String> longestVarRefChain)
		{
			assert varId != null;
			this.variableId = varId;
			this.longestVariableReferenceChain = longestVarRefChain;
		}

		/** {@inheritDoc} */
		@Override
		public final String getVariableId()
		{
			return this.variableId;
		}

	}

	private static final class ConstantVariableReference<V extends Value> extends BaseVariableReference<V>
	{
		private final transient Optional<V> alwaysPresentVarValue;
		private final transient Datatype<V> varDatatype;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            variable ID
		 * @param varValue
		 *            constant value (for constant variable)
		 * @param varDatatype
		 *            variable datatype
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		private ConstantVariableReference(final String varId, final V varValue, final Datatype<V> varDatatype, final Deque<String> longestVarRefChain)
		{
			super(varId, longestVarRefChain);
			assert varValue != null && varDatatype != null;
			this.alwaysPresentVarValue = Optional.of(varValue);
			this.varDatatype = varDatatype;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Returns the type of the referenced expression.
		 */
		@Override
		public Datatype<V> getReturnType()
		{
			return this.varDatatype;
		}

		@Override
		public Optional<V> getValue()
		{
			return this.alwaysPresentVarValue;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Evaluates the referenced expression using the given context, and either returns an error or a resulting value. If this doesn't reference an evaluable expression (eg, a single Function)
		 * then this will throw an exception.
		 * <p>
		 * The policy evaluator should call this when starting the evaluation of the policy where the VariableDefinition occurs, then cache the value in the evaluation context with
		 * {@link EvaluationContext#putVariableIfAbsent(String, Value)}.
		 */
		@Override
		public V evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
		{
			assert this.alwaysPresentVarValue.isPresent();
			return this.alwaysPresentVarValue.get();
		}
	}

	private static final class DynamicVariableReference<V extends Value> extends BaseVariableReference<V>
	{
		private final transient Expression<V> expression;
		private final transient IndeterminateEvaluationException nullContextException;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            input VariableReference from XACML model
		 * @param varExpr
		 *            Expression of referenced VariableDefinition
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		private DynamicVariableReference(final String varId, final Expression<V> varExpr, final Deque<String> longestVarRefChain)
		{
			super(varId, longestVarRefChain);
			assert varExpr != null;
			this.expression = varExpr;
			this.nullContextException = new IndeterminateEvaluationException(
			        "VariableReference[VariableId='" + this.variableId + "']: evaluate(context = null) not allowed because the variable requires context for evaluation (not constant)",
			        XacmlStatusCode.PROCESSING_ERROR.value());
		}

		/**
		 * {@inheritDoc}
		 *
		 * Returns the type of the referenced expression.
		 */
		@Override
		public Datatype<V> getReturnType()
		{
			return expression.getReturnType();
		}

		@Override
		public Optional<V> getValue()
		{
			return Optional.empty();
		}

		/**
		 * {@inheritDoc}
		 *
		 * Evaluates the referenced expression using the given context, and either returns an error or a resulting value. If this doesn't reference an evaluable expression (eg, a single Function)
		 * then this will throw an exception.
		 * <p>
		 * The policy evaluator should call this when starting the evaluation of the policy where the VariableDefinition occurs, then cache the value in the evaluation context with
		 * {@link EvaluationContext#putVariableIfAbsent(String, Value)}.
		 */
		@Override
		public V evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
		{
			if (context == null)
			{
				throw nullContextException;
			}

			final V ctxVal = context.getVariableValue(this.variableId, expression.getReturnType());
			if (ctxVal != null)
			{
				return ctxVal;
			}

			// ctxVal == null: not evaluated yet in this context -> evaluate now
			final V result = expression.evaluate(context, mdpContext);
			context.putVariableIfAbsent(this.variableId, result);
			return result;
		}
	}

	/**
	 * Named Attribute Provider based only on the evaluation context, i.e. it does not use any extra attribute provider module to get attribute values if not found in the context
	 */
	private static final class EvaluationContextOnlyScopedSingleNamedAttributeProvider<AV extends AttributeValue> extends EvaluationContextBasedSingleNamedAttributeProvider<AV>
	{

		/**
		 * Creates new instance for given provided attribute and delegate attribute provider to be called if not found in evaluation context
		 *
		 * @param attributeName              provided attribute name
		 * @param attributeDatatype          provided attribute data-type
		 * @param strictAttributeIssuerMatch whether to apply strict match on the attribute Issuer
		 */
		private EvaluationContextOnlyScopedSingleNamedAttributeProvider(final AttributeFqn attributeName, final Datatype<AV> attributeDatatype, final boolean strictAttributeIssuerMatch)
		{
			super(attributeName, attributeDatatype, strictAttributeIssuerMatch, (attName, attType, ctx, mdpCtx) -> Bags.emptyAttributeBag(attributeDatatype, null));
		}
	}

	private static final class CompositeSingleNamedAttributeProvider<AV extends AttributeValue> extends EvaluationContextBasedSingleNamedAttributeProvider<AV>
	{
		private CompositeSingleNamedAttributeProvider(final AttributeFqn attributeName, final Datatype<AV> attributeDatatype, final boolean strictAttributeIssuerMatch, final List<NamedAttributeProvider> composedProviders)
		{
			super(attributeName, attributeDatatype, strictAttributeIssuerMatch, newDelegate(composedProviders));
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DepthLimitingExpressionFactory.class);

	private static final IllegalArgumentException MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION = new IllegalArgumentException(
	        "Missing Issuer that is required on AttributeDesignators by PDP configuration");

	private static final IllegalArgumentException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException("Unsupported Expression type (optional XACML feature): AttributeSelector");

	private static final IllegalArgumentException NULL_FUNCTION_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined function registry");

	private static final IllegalArgumentException NULL_ATTRIBUTE_DATATYPE_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype registry");

	private static final int UNLIMITED_MAX_VARIABLE_REF_DEPTH = -1;

	private static final Map<AttributeFqn, AttributeDatatype<?>> STANDARD_ATTRIBUTES_WITH_FIXED_DATATYPE;
	static {
		final Map<AttributeFqn, AttributeDatatype<?>> mutableMap = new HashMap<>();
		Stream.of(StandardSubjectAttribute.values()).forEach(a -> mutableMap.put(a.getFQN(), a.getDatatype()));
		Stream.of(StandardResourceAttribute.values()).forEach(a -> mutableMap.put(a.getFQN(), a.getDatatype()));
		Stream.of(StandardEnvironmentAttribute.values()).forEach(a -> mutableMap.put(a.getFQN(), a.getDatatype()));
		STANDARD_ATTRIBUTES_WITH_FIXED_DATATYPE = Map.copyOf(mutableMap);
	}

	private final AttributeValueFactoryRegistry datatypeFactoryRegistry;
	private final FunctionRegistry functionRegistry;
	// not null
	private final Optional<CloseableNamedAttributeProviderRegistry> attributeProviderRegistry;
	private final int maxVariableReferenceDepth;
	// the map from identifiers to internal data
	private final Map<String, BaseVariableReference<?>> idToVariableMap = HashCollections.newMutableMap();
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
	 * @param attributeProviderRegistry
	 *            Registry of Attribute Provider (Attribute Providers resolve values of attributes absent from the request context). Empty if none. <b>Calling {@link CloseableNamedAttributeProviderRegistry#close()}} on this is the responsibility of the caller.</b>
	 * @param maxVariableRefDepth
	 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference); strictly negative value means unlimited
	 * @param allowAttributeSelectors
	 *            allow use of AttributeSelectors (experimental, not for production, use with caution)
	 * @param strictAttributeIssuerMatch
	 *            true iff we want strict Attribute Issuer matching, and we require that all AttributeDesignators set the Issuer field.
	 *            <p>
	 *            "Strict Attribute Issuer matching" means that an AttributeDesignator without Issuer only matches request Attributes without Issuer. This mode is not fully compliant with XACML 3.0,
	 *            §5.29, in the case that the Issuer is not present in the Attribute Designator, but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice).
	 *            Indeed, the XACML 3.0 Attribute Evaluation section §5.29 says: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute
	 *            SHALL be governed by AttributeId and DataType attributes alone." Therefore, if {@code strictAttributeIssuerMatch} is false, since policies may use AttributeDesignators without
	 *            Issuer, if the requests are using matching Attributes but with none, one or more different Issuers, this PDP engine has to gather all the values from all the attributes with matching
	 *            Category/AttributeId but with any Issuer or no Issuer. Therefore, in order to stay compliant with §5.29 and still enforce best practice, when {@code strictAttributeIssuerMatch} is
	 *            true, we also require that all AttributeDesignators set the Issuer field.
	 * @throws java.lang.IllegalArgumentException
	 *             If {@code attributeFactory == null || functionRegistry == null} OR any Attribute Provider created from {@code attributeProviderFactories} does not provide any attribute.
	 */
	public DepthLimitingExpressionFactory(final AttributeValueFactoryRegistry attributeFactory, final FunctionRegistry functionRegistry,
	        final int maxVariableRefDepth, final boolean allowAttributeSelectors,
	        final boolean strictAttributeIssuerMatch, final Optional<CloseableNamedAttributeProviderRegistry> attributeProviderRegistry) throws IllegalArgumentException
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
		this.maxVariableReferenceDepth = maxVariableRefDepth < 0 ? UNLIMITED_MAX_VARIABLE_REF_DEPTH : maxVariableRefDepth;
		/*
		 * finally, create the global attribute Provider used to resolve AttributeDesignators
		 */
		this.attributeProviderRegistry = attributeProviderRegistry;
		this.allowAttributeSelectors = allowAttributeSelectors;
		this.issuerRequiredOnAttributeDesignators = strictAttributeIssuerMatch;
	}

	private static <V extends Value> BaseVariableReference<?> newVariableReference(final String variableId, final Expression<V> variableExpression, final Deque<String> longestVarRefChainInExpression)
	{
		assert variableId != null && variableExpression != null;

		final Optional<V> constant = variableExpression.getValue();
		if (constant.isPresent())
		{
			/*
			 * Variable expression is constant
			 */
			if (LOGGER.isWarnEnabled() && !(variableExpression instanceof ConstantExpression))
			{
				LOGGER.warn("Expression of Variable {} is constant '{}', therefore should be replaced with a equivalent AttributeValue.", variableId, constant);
			}

			return new ConstantVariableReference<>(variableId, constant.get(), variableExpression.getReturnType(), longestVarRefChainInExpression);
		}

		return new DynamicVariableReference<>(variableId, variableExpression, longestVarRefChainInExpression);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> addVariable(final VariableDefinition varDef, final XPathCompiler xPathCompiler, final Deque<String> inoutLongestVarRefChain) throws IllegalArgumentException
	{
		assert varDef != null;

		final String varId = varDef.getVariableId();
		/*
		 * Initialize the longest variable reference chain from this VariableDefinition (varDef -> VarDef2 -> ..., where "v1 -> v2" means: v1's expression contains a VariableReference to v2) as empty
		 * for later update by this#getVariable() when resolving a VariableReference within this {@code varDef}'s expression (being parsed just after). The goal is to detect chains longer than
		 * this.maxVariableReferenceDepth to limit abuse of VariableReferences. There may be multiple VariableReferences in a VariableDefinition's expression, such as an 'Apply', and each may be
		 * referencing a different VariableDefinition; but we are interested only in the one with the longest chain of references. Note that circular references are prevented by the fact that, in our
		 * implementation, a VariableReference can only reference a Variable previously declared.
		 */
		/*
		 * inoutLongestVarRefChain == null means that the longest VarRef chain will not be computed nor checked
		 */
		final Deque<String> longestVarRefChainInCurrentVarExpression = inoutLongestVarRefChain == null ? null : new ArrayDeque<>();
		final Expression<?> varExpr = getInstance(varDef.getExpression().getValue(), xPathCompiler, longestVarRefChainInCurrentVarExpression);
		/*
		 * if not null, longestVarRefChainInCurrentVarExpression has now been updated to longest VariableReference chain in varExpr
		 */
		if (inoutLongestVarRefChain != null)
		{
			// => longestVarRefChainInCurrentVarExpression != null (see previous line where longestVarRefChainInCurrentVarExpression is assigned
			/*
			 * Check size only if there is a limit
			 */
			if (maxVariableReferenceDepth != UNLIMITED_MAX_VARIABLE_REF_DEPTH && longestVarRefChainInCurrentVarExpression.size() > this.maxVariableReferenceDepth)
			{
				throw new IllegalArgumentException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length (" + longestVarRefChainInCurrentVarExpression.size()
				        + ") of longest VariableReference Reference chain found in Expression of Variable '" + varId + "': " + longestVarRefChainInCurrentVarExpression);
			}

			/*
			 * Update inoutLongestVarRefChain if this variable's expression contains bigger chain
			 */
			if (longestVarRefChainInCurrentVarExpression.size() > inoutLongestVarRefChain.size())
			{
				/* current longest is no longer the longest, so replace with new
				 longest content
				 */
				inoutLongestVarRefChain.clear();
				/*
				 * This is a VariableDefinition we are looking at here, but there may not be any VariableReference to it; if there is, it will be taken into account in #getVariable(). So at this point,
				 * we do not include it in the chain.
				 */
				inoutLongestVarRefChain.addAll(longestVarRefChainInCurrentVarExpression);
			}

		}

		final BaseVariableReference<?> var = newVariableReference(varId, varExpr, longestVarRefChainInCurrentVarExpression);
		return idToVariableMap.putIfAbsent(varId, var);
	}

	@Override
	public VariableReference<?> getVariableExpression(final String varId)
	{
		return idToVariableMap.get(varId);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> removeVariable(final String varId)
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
	 * @param inoutLongestVarRefChain
	 *            If we are resolving a VariableReference in a VariableDefinition's expression (may be null if not), this is the longest chain of VariableReferences starting from a one in this
	 *            VariableDefinition. If we are not resolving a VariableReference in a VariableDefinition's expression, this may be null. This is used to detect exceeding reference depth (see
	 *            the argument to {@link #DepthLimitingExpressionFactory(AttributeValueFactoryRegistry, FunctionRegistry, int, boolean, boolean, Optional)} for the limit). In an Expression such as an 'Apply', we can have multiple VariableReferences referencing different VariableDefinitions. So we can have
	 *            different depths of VariableReference references. We compare the length of the current longest chain with the one we would get by adding the longest one in the referenced
	 *            VariableDefinition and <code>jaxbVarRef</code>'s VariableId. If the latter is longer, its content becomes the content <code>longestVarRefChain</code>.
	 * @throws IllegalArgumentException
	 *             if VariableReference's VariableId is unknown by this factory
	 */
	private BaseVariableReference<?> getVariable(final VariableReferenceType jaxbVarRef, final Deque<String> inoutLongestVarRefChain) throws IllegalArgumentException
	{
		assert jaxbVarRef != null;

		final String varId = jaxbVarRef.getVariableId();
		final BaseVariableReference<?> var = idToVariableMap.get(varId);
		if (var == null)
		{
			throw new IllegalArgumentException("VariableReference's VariableId=" + varId + " unknown in the current context, i.e. does not match any prior VariableDefinition's VariableId");
		}

		/*
		 * inoutLongestVarRefChain == null means that the longest VarRef chain will not be computed nor checked
		 */
		if (inoutLongestVarRefChain != null)
		{
			/*
			 * Check whether the chain formed by [the Variable's Expression's longest VariableReference chain, and the VariableReference that we are currently resolving in this method] is not too big
			 */
			final Deque<String> referencedVarLongestRefChain = var.longestVariableReferenceChain;
			/*
			 * Make sure longestVarRefChain is set to the biggest one
			 */
			if (referencedVarLongestRefChain != null && referencedVarLongestRefChain.size() + 1 > inoutLongestVarRefChain.size())
			{
				/* current longest is no longer the longest, so replace with new
				 longest content
				 */
				inoutLongestVarRefChain.clear();
				inoutLongestVarRefChain.add(varId);
				inoutLongestVarRefChain.addAll(referencedVarLongestRefChain);
			}

			if (maxVariableReferenceDepth != UNLIMITED_MAX_VARIABLE_REF_DEPTH && inoutLongestVarRefChain.size() > this.maxVariableReferenceDepth)
			{
				throw new IllegalArgumentException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length (" + inoutLongestVarRefChain.size()
				        + ") of VariableReference Reference chain: " + inoutLongestVarRefChain);
			}
		}

		return var;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #DepthLimitingExpressionFactory(AttributeValueFactoryRegistry, FunctionRegistry, int, boolean, boolean, Optional)} .
	 */
	@Override
	public FunctionExpression getFunction(final String functionId)
	{
		final Function<?> f = this.functionRegistry.getFunction(functionId);
		if (f == null)
		{
			return null;
		}

		return new FunctionExpression(f);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #DepthLimitingExpressionFactory(AttributeValueFactoryRegistry, FunctionRegistry, int, boolean, boolean, Optional)} .
	 */
	@Override
	public FunctionExpression getFunction(final String functionId, final Datatype<? extends AttributeValue> subFunctionReturnType) throws IllegalArgumentException
	{
		if (subFunctionReturnType == null)
		{
			return getFunction(functionId);
		}

		final Function<?> f = this.functionRegistry.getFunction(functionId, subFunctionReturnType);
		if (f == null)
		{
			return null;
		}

		return new FunctionExpression(f);
	}

	private <AV extends AttributeValue> GenericAttributeProviderBasedAttributeDesignatorExpression<AV> newAttributeDesignatorExpr(final AttributeFqn attName, final AttributeDatatype<AV> attType, final boolean mustBePresent)
	{
		assert attName != null && attType != null;
		final SingleNamedAttributeProvider<AV> combiningProvider;
		if(attributeProviderRegistry.isEmpty()) {
			combiningProvider = new EvaluationContextOnlyScopedSingleNamedAttributeProvider<>(attName, attType, this.issuerRequiredOnAttributeDesignators);
		} else
		{
			final List<NamedAttributeProvider> registeredAttProviders = this.attributeProviderRegistry.get().getProviders(attName);
			// Empty list returned if no provider of attName
			combiningProvider = registeredAttProviders.isEmpty() ?  new EvaluationContextOnlyScopedSingleNamedAttributeProvider<>(attName, attType, this.issuerRequiredOnAttributeDesignators):
			new CompositeSingleNamedAttributeProvider<>(attName, attType, this.issuerRequiredOnAttributeDesignators, registeredAttProviders);
		}
		return new GenericAttributeProviderBasedAttributeDesignatorExpression<>(attName, mustBePresent, attType.getBagDatatype(), combiningProvider);
	}

	/** {@inheritDoc} */
	@Override
	public Expression<?> getInstance(final ExpressionType expr, final XPathCompiler xPathCompiler, final Deque<String> longestVarRefChain) throws IllegalArgumentException
	{
		final Expression<?> expression;
		/*
		 * We check all types of Expression: <Apply>, <AttributeSelector>, <AttributeValue>, <Function>, <VariableReference> and <AttributeDesignator>
		 */
		if (expr instanceof ApplyType)
		{
			expression = ApplyExpressions.newInstance((ApplyType) expr, xPathCompiler, this, longestVarRefChain);
		}
		else if (expr instanceof AttributeDesignatorType)
		{
			final AttributeDesignatorType jaxbAttrDes = (AttributeDesignatorType) expr;
			if (this.issuerRequiredOnAttributeDesignators && jaxbAttrDes.getIssuer() == null)
			{
				throw MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION;
			}

			final AttributeValueFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrDes.getDataType());
			if (attrFactory == null)
			{
				throw new IllegalArgumentException("Unsupported Datatype used in AttributeDesignator: " + jaxbAttrDes.getDataType());
			}

			final AttributeFqn attName = AttributeFqns.newInstance(jaxbAttrDes);
			// validate data-type if the attribute is standardized with fixed data type
			final AttributeDatatype<?> requiredDatatype = STANDARD_ATTRIBUTES_WITH_FIXED_DATATYPE.get(attName);
			Preconditions.checkArgument(requiredDatatype == null || requiredDatatype.equals(attrFactory.getDatatype()), "Invalid AttributeDesignator: invalid Datatype ("+attrFactory.getDatatype()+") for attribute " +attName+ ". Expected: " + requiredDatatype + " (mandatory per XACML standard)");
			expression = newAttributeDesignatorExpr(attName, attrFactory.getDatatype(), jaxbAttrDes.isMustBePresent());
		}
		else if (expr instanceof AttributeSelectorType)
		{
			if (!allowAttributeSelectors)
			{
				throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
			}

			final AttributeSelectorType jaxbAttrSelector = (AttributeSelectorType) expr;
			final AttributeValueFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrSelector.getDataType());
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

			final String contextSelectorId = jaxbAttrSelector.getContextSelectorId();
			if(contextSelectorId == null) {
				expression = AttributeSelectorExpressions.newInstance(jaxbAttrSelector.getCategory(), jaxbAttrSelector.getPath(), jaxbAttrSelector.isMustBePresent(), xPathCompiler, attrFactory);
			} else {
				final AttributeFqn contextSelectorAttName = AttributeFqns.newInstance(jaxbAttrSelector.getCategory(), Optional.empty(), contextSelectorId);
				final SingleNamedAttributeProvider<XPathValue> ctxSelectorAttProvider;
				if(attributeProviderRegistry.isEmpty()) {
					ctxSelectorAttProvider = new EvaluationContextOnlyScopedSingleNamedAttributeProvider<>(contextSelectorAttName, StandardDatatypes.XPATH, this.issuerRequiredOnAttributeDesignators);
				} else
				{
					final List<NamedAttributeProvider> registeredAttProviders = this.attributeProviderRegistry.get().getProviders(contextSelectorAttName);
					// Empty list returned if no provider of contextSelectorAttName
					ctxSelectorAttProvider = registeredAttProviders.isEmpty() ? new EvaluationContextOnlyScopedSingleNamedAttributeProvider<>(contextSelectorAttName, StandardDatatypes.XPATH, issuerRequiredOnAttributeDesignators) : new CompositeSingleNamedAttributeProvider<>(contextSelectorAttName, StandardDatatypes.XPATH, issuerRequiredOnAttributeDesignators, registeredAttProviders);
				}
				expression = AttributeSelectorExpressions.newInstance(jaxbAttrSelector, xPathCompiler, attrFactory, ctxSelectorAttProvider);
			}
		}
		else if (expr instanceof AttributeValueType)
		{
			expression = getInstance((AttributeValueType) expr, xPathCompiler);
		}
		else if (expr instanceof FunctionType)
		{
			final FunctionType jaxbFunc = (FunctionType) expr;
			final FunctionExpression funcExp = getFunction(jaxbFunc.getFunctionId());
			if (funcExp != null)
			{
				expression = funcExp;
			}
			else
			{
				throw new IllegalArgumentException("Function " + jaxbFunc.getFunctionId()
				        + " is not supported (at least) as standalone Expression: either a generic higher-order function supported only as Apply FunctionId, or function completely unknown.");
			}
		}
		else if (expr instanceof VariableReferenceType)
		{
			final VariableReferenceType varRefElt = (VariableReferenceType) expr;
			expression = getVariable(varRefElt, longestVarRefChain);
		}
		else
		{
			throw new IllegalArgumentException("Expressions of type " + expr.getClass().getSimpleName()
			        + " are not supported. Expected: one of Apply, AttributeDesignator, AttributeSelector, AttributeValue, Function or VariableReference.");
		}

		return expression;
	}

	/** {@inheritDoc} */
	@Override
	public ConstantExpression<? extends AttributeValue> getInstance(final AttributeValueType jaxbAttrVal, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		return this.datatypeFactoryRegistry.newExpression(jaxbAttrVal.getDataType(), jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), xPathCompiler);
	}

}
