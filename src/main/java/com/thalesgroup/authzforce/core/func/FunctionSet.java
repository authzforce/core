package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

/**
 * Function set. Allows to group functions of the same category, e.g. all standard numeric
 * comparison functions, all standard numeric arithmetic functions, etc. This is particularly
 * important for simplifying the run-time configuration system, which uses this interface to load a
 * group of functions based only on a function group ID specified in the configuration; therefore,
 * it makes configuration files much smaller, easier to read and maintain.
 */
public class FunctionSet implements PdpExtension
{
	/**
	 * Namespace to be used as default prefix for internal function set IDs
	 */
	public static final String DEFAULT_ID_NAMESPACE = "urn:thalesgroup:xacml:function-set:";
	
	private final String id;
	
	private final Set<Function<?>> functions;

	/**
	 * @param id globally unique ID of this function set, to be used as PDP extension ID
	 * @param functions
	 */
	public FunctionSet(String id, Function<?>... functions)
	{
		this(id, new HashSet<>(Arrays.asList(functions)));
	}

	/**
	 * @param id 
	 * @param functions
	 */
	public FunctionSet(String id, Set<Function<?>> functions)
	{
		this.id = id;
		this.functions = Collections.unmodifiableSet(functions);
	}

	/**
	 * Returns a single instance of each of the functions supported by some class. The
	 * <code>Set</code> must contain instances of <code>Function</code>, and it must be both
	 * non-null and non-empty. It may contain only a single <code>Function</code>.
	 * 
	 * @return the functions members of this group
	 */
	public  Set<Function<? extends ExpressionResult<? extends AttributeValue>>> getSupportedFunctions()
	{
		return functions;
	}

	@Override
	public String getId()
	{
		return id;
	}

}
