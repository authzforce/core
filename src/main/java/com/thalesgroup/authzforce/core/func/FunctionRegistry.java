/**
 * 
 */
package com.thalesgroup.authzforce.core.func;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

/**
 * 
 *
 */
public class FunctionRegistry
{

	private final BasePdpExtensionRegistry<Function<? extends ExpressionResult<? extends AttributeValue>>> nonGenericFunctionRegistry;
	private final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericHigherOrderFunctionFactoryRegistry;

	protected FunctionRegistry(BasePdpExtensionRegistry<Function<? extends ExpressionResult<? extends AttributeValue>>> nonGenericFunctionRegistry,
			BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFunctionFactoryRegistry)
	{
		this.nonGenericFunctionRegistry = new BasePdpExtensionRegistry<>(nonGenericFunctionRegistry);
		this.genericHigherOrderFunctionFactoryRegistry = new BasePdpExtensionRegistry<>(genericFunctionFactoryRegistry);
	}

	/**
	 * Constructor that sets a "base registry" from which this inherits all the extensions. Used for
	 * instance to build a new registry based on a standard one (e.g.
	 * {@link StandardFunctionRegistry} for standard functions).
	 * 
	 * @param baseRegistry
	 *            the base/parent registry on which this one is based or null
	 */
	public FunctionRegistry(FunctionRegistry baseRegistry)
	{
		this(baseRegistry == null ? new BasePdpExtensionRegistry<Function<? extends ExpressionResult<? extends AttributeValue>>>()
				: new BasePdpExtensionRegistry<>(baseRegistry.nonGenericFunctionRegistry),
				baseRegistry == null ? new BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory>() : new BasePdpExtensionRegistry<>(
						baseRegistry.genericHigherOrderFunctionFactoryRegistry));
	}

	/**
	 * Adds (non-generic) function
	 * 
	 * @param function
	 */
	public void addFunction(Function<? extends ExpressionResult<? extends AttributeValue>> function)
	{
		nonGenericFunctionRegistry.addExtension(function);

	}

	/**
	 * Get a (non-generic) function by ID. 'Non-generic' here means the function is either
	 * first-order, or higher-order but does not need the specific sub-function parameter to be
	 * instantiated.
	 * 
	 * @param functionId
	 *            ID of function to loop up
	 * 
	 * @return function instance, null if none with such ID in the registry of non-generic
	 *         functions, in which case it may be a generic function and you should try
	 *         {@link #getFunction(String, FirstOrderFunction, Class)} instead.
	 */
	public Function<? extends ExpressionResult<? extends AttributeValue>> getFunction(String functionId)
	{
		return nonGenericFunctionRegistry.getExtension(functionId);
	}

	/**
	 * Get any function including generic ones. 'Generic' here means the function is a higher-order
	 * function that is instantiated for a specific sub-function. For instance, the
	 * {@link MapFunction} function class takes the sub-function's return type as type parameter and
	 * therefore it needs this sub-function's return type to be instantiated (this is done via the
	 * {@link MapFunction}).
	 * 
	 * @param functionId
	 *            function ID
	 * @param subFunction
	 *            sub-function
	 * @param subFunctionReturnType
	 *            sub-function return type
	 * @return function instance
	 */
	public <SUB_RETURN_T extends AttributeValue> Function<? extends ExpressionResult<? extends AttributeValue>> getFunction(String functionId,
			FirstOrderFunction<? extends ExpressionResult<? extends AttributeValue>> subFunction, Class<? extends SUB_RETURN_T> subFunctionReturnType)
	{
		final Function<? extends ExpressionResult<? extends AttributeValue>> nonGenericFunc = nonGenericFunctionRegistry.getExtension(functionId);
		if (nonGenericFunc != null)
		{
			return nonGenericFunc;
		}

		final GenericHigherOrderFunctionFactory funcFactory = genericHigherOrderFunctionFactoryRegistry.getExtension(functionId);
		return funcFactory.getInstance(subFunction, subFunctionReturnType);
	}

}
