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
package com.sun.xacml.cond;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;

/**
 * An abstract utility superclass for functions. Supplies several useful methods, making it easier
 * to implement a <code>Function</code>. You can extend this class or implement
 * <code>Function</code> directly, depending on your needs.
 * 
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public abstract class FunctionBase extends Function
{

	/**
	 * The standard namespace where all XACML 1.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS = "urn:oasis:names:tc:xacml:1.0:function:";

	/**
	 * The standard namespace where all XACML 2.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";

	/**
	 * The standard namespace where all XACML 3.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";

	// A List used by makeProcessingError() to save some steps.
	private static List<String> processingErrList = null;

	// the name of this function
	private String functionName;

	// the id used by this function
	private int funcId;

	// the return type of this function, and whether it's a bag
	private String returnType;
	private boolean returnsBag;

	// flag that tells us which of the two constructors was used
	private boolean singleType;

	// parameter data if we're only using a single type
	private String paramType;
	private boolean paramIsBag;
	private int numParams;
	private int minParams;

	// paramater data if we're using different types
	private String[] paramTypes;
	private boolean[] paramsAreBags;

	private final String INVALID_ARGUMENT_MESSAGE_FORMAT = "Invalid argument #%s to function '%s': type='%s', isBag='%s'. Required: type='%s', isBag='%s'.";

	/**
	 * Constructor that sets up the function as having some number of parameters all of the same
	 * given type. If <code>numParams</code> is -1, then the length is variable
	 * 
	 * @param functionName
	 *            the name of this function as used by the factory and any XACML policies
	 * @param functionId
	 *            an optional identifier that can be used by your code for convenience
	 * @param paramType
	 *            the type of all parameters to this function, as used by the factory and any XACML
	 *            documents
	 * @param paramIsBag
	 *            whether or not every parameter is actually a bag of values
	 * @param numParams
	 *            the number of parameters required by this function, or -1 if any number are
	 *            allowed
	 * @param returnType
	 *            the type returned by this function, as used by the factory and any XACML documents
	 * @param returnsBag
	 *            whether or not this function returns a bag of values
	 */
	public FunctionBase(String functionName, int functionId, String paramType, boolean paramIsBag, int numParams, String returnType,
			boolean returnsBag)
	{
		this(functionName, functionId, returnType, returnsBag);

		singleType = true;

		this.paramType = paramType;
		this.paramIsBag = paramIsBag;
		this.numParams = numParams;
		this.minParams = 0;
	}

	/**
	 * Constructor that sets up the function as having some number of parameters all of the same
	 * given type. If <code>numParams</code> is -1, then the length is variable, and then
	 * <code>minParams</code> may be used to specify a minimum number of parameters. If
	 * <code>numParams</code> is not -1, then <code>minParams</code> is ignored.
	 * 
	 * @param functionName
	 *            the name of this function as used by the factory and any XACML policies
	 * @param functionId
	 *            an optional identifier that can be used by your code for convenience
	 * @param paramType
	 *            the type of all parameters to this function, as used by the factory and any XACML
	 *            documents
	 * @param paramIsBag
	 *            whether or not every parameter is actually a bag of values
	 * @param numParams
	 *            the number of parameters required by this function, or -1 if any number are
	 *            allowed
	 * @param minParams
	 *            the minimum number of parameters required if <code>numParams</code> is -1
	 * @param returnType
	 *            the type returned by this function, as used by the factory and any XACML documents
	 * @param returnsBag
	 *            whether or not this function returns a bag of values
	 */
	public FunctionBase(String functionName, int functionId, String paramType, boolean paramIsBag, int numParams, int minParams, String returnType,
			boolean returnsBag)
	{
		this(functionName, functionId, returnType, returnsBag);

		singleType = true;

		this.paramType = paramType;
		this.paramIsBag = paramIsBag;
		this.numParams = numParams;
		this.minParams = minParams;
	}

	/**
	 * Constructor that sets up the function as having different types for each given parameter.
	 * 
	 * @param functionName
	 *            the name of this function as used by the factory and any XACML policies
	 * @param functionId
	 *            an optional identifier that can be used by your code for convenience
	 * @param paramTypes
	 *            the type of each parameter, in order, required by this function, as used by the
	 *            factory and any XACML documents
	 * @param paramIsBag
	 *            whether or not each parameter is actually a bag of values
	 * @param returnType
	 *            the type returned by this function, as used by the factory and any XACML documents
	 * @param returnsBag
	 *            whether or not this function returns a bag of values
	 */
	public FunctionBase(String functionName, int functionId, String[] paramTypes, boolean[] paramIsBag, String returnType, boolean returnsBag)
	{
		this(functionName, functionId, returnType, returnsBag);

		singleType = false;

		this.paramTypes = paramTypes;
		this.paramsAreBags = paramIsBag;
	}

	/**
	 * Constructor that sets up some basic values for functions that will take care of parameter
	 * checking on their own. If you use this constructor for your function class, then you must
	 * override the two check methods to make sure that parameters are correct.
	 * 
	 * @param functionName
	 *            the name of this function as used by the factory and any XACML policies
	 * @param functionId
	 *            an optional identifier that can be used by your code for convenience
	 * @param returnType
	 *            the type returned by this function, as used by the factory and any XACML documents
	 * @param returnsBag
	 *            whether or not this function returns a bag of values
	 */
	public FunctionBase(String functionName, int functionId, String returnType, boolean returnsBag)
	{
		this.functionName = functionName;
		// this.functionId = String.valueOf(functionId);
		this.returnType = returnType;
		this.returnsBag = returnsBag;
	}

	/**
	 * Returns the full identifier of this function, as known by the factories.
	 * 
	 * @return the function's identifier
	 * 
	 * @throws IllegalArgumentException
	 *             if the identifier isn't a valid URI
	 */
	@Override
	public String getIdentifier()
	{
		// this is to get around the exception handling problems, but may
		// change if this code changes to include exceptions from the
		// constructors
			return functionName;

	}

	/**
	 * Returns the name of the function to be handled by this particular object.
	 * 
	 * @return the function name
	 */
	public String getFunctionName()
	{
		return functionName;
	}

	/**
	 * Returns the Identifier of the function to be handled by this particular object.
	 * 
	 * @return the function Id
	 */
	@Override
	public String getFunctionId()
	{
		return String.valueOf(funcId);
	}

	/**
	 * Returns the same value as <code>getReturnType</code>. This is here to support the
	 * <code>Expression</code> interface.
	 * 
	 * @return the return type
	 */
	public String getType()
	{
		return getReturnType();
	}

	/**
	 * Get the attribute type returned by this function.
	 * 
	 * @return a <code>URI</code> indicating the attribute type returned by this function
	 */
	@Override
	public String getReturnType()
	{
		return returnType;
	}

	/**
	 * Returns true if this function returns a bag of values.
	 * 
	 * @return true if the function returns a bag, false otherwise
	 */
	@Override
	public boolean returnsBag()
	{
		return returnsBag;
	}

	/**
	 * Returns the return type for this particular object.
	 * 
	 * @return the return type
	 */
	public String getReturnTypeAsString()
	{
		return returnType;
	}

	/**
	 * Create an <code>EvaluationResult</code> that indicates a processing error with the specified
	 * message. This method may be useful to subclasses.
	 * 
	 * @param message
	 *            a description of the error (<code>null</code> if none)
	 * @return the desired <code>EvaluationResult</code>
	 */
	protected static EvaluationResult makeProcessingError(String message)
	{
		// Build up the processing error Status.
		if (processingErrList == null)
		{
			String[] errStrings = { Status.STATUS_PROCESSING_ERROR };
			processingErrList = Arrays.asList(errStrings);
		}
		Status errStatus = new Status(processingErrList, message);
		EvaluationResult processingError = new EvaluationResult(errStatus);

		return processingError;
	}

	/**
	 * Evaluates each of the parameters, in order, filling in the argument array with the resulting
	 * values. If any error occurs, this method returns the error, otherwise null is returned,
	 * signalling that evaluation was successful for all inputs, and the resulting argument list can
	 * be used.
	 * 
	 * @param args
	 *            a <code>List</code> of <code>Evaluatable</code> objects representing the
	 *            parameters to evaluate
	 * @param context
	 *            the representation of the request
	 * @param results
	 *            an array as long as the params <code>List</code> that will, on return, contain the
	 *            <code>AttributeValue</code>s generated from evaluating all parameters
	 * 
	 * @return <code>null</code> if no errors were encountered, otherwise an
	 *         <code>EvaluationResult</code> representing the error
	 */
	protected static EvaluationResult evalArgs(List<? extends ExpressionType> args, EvaluationCtx context, AttributeValueType[] results)
	{

		for (int i = 0; i < args.size(); i++)
		{
			// get and evaluate the next parameter
			// FIXME: We should not have to cast here, maybe we should use Evalutable instead of
			// ExpressionType as argument type
			EvaluationResult result = ((Evaluatable) args.get(i)).evaluate(context);

			// If there was an error, pass it back...
			if (result.indeterminate())
			{
				return result;
			}

			// ...otherwise save it and keep going
			results[i] = result.getAttributeValue();
		}

		// if no error occurred then we got here, so we return no errors
		return null;
	}

	/**
	 * Default handling of input checking. This does some simple checking based on the type of
	 * constructor used. If you need anything more complex, or if you used the simple constructor,
	 * then you must override this method.
	 * 
	 * @param inputs
	 *            a <code>List></code> of <code>Evaluatable</code>s
	 * 
	 * @throws IllegalArgumentException
	 *             if the inputs won't work
	 */
	@Override
	public void checkInputs(List<ExpressionType> inputs) throws IllegalArgumentException
	{
		// first off, see what kind of function we are
		if (singleType)
		{
			// first, check the length of the inputs, if appropriate
			if (numParams != -1)
			{
				if (inputs.size() != numParams) {
					throw new IllegalArgumentException("wrong number of args to " + functionName + ". Required: " + numParams);
				}
			} else
			{
				if (inputs.size() < minParams)
					throw new IllegalArgumentException("not enough args to " + functionName + ". Required: >=" + minParams);
			}

			// now, make sure everything is of the same, correct type
			int argIndex = 0;
			for (final ExpressionType eval : inputs)
			{
				/*
				 * FIXME: Need to be rethink. Too much duplication and introspection
				 */
				if (eval instanceof AttributeDesignator)
				{
					AttributeDesignator evalTmp = (AttributeDesignator) eval;
					if ((!evalTmp.getDataType().toString().equals(paramType)) || (evalTmp.returnsBag() != paramIsBag))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				} else if (eval instanceof AttributeSelector)
				{
					AttributeSelector evalTmp = (AttributeSelector) eval;
					if ((!evalTmp.getDataType().toString().equals(paramType)) || (evalTmp.returnsBag() != paramIsBag))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				} else if (eval instanceof AttributeValue)
				{
					AttributeValue evalTmp = (AttributeValue) eval;
					if ((!evalTmp.getDataType().toString().equals(paramType)) || (evalTmp.returnsBag() != paramIsBag))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				}

				argIndex += 1;
			}
		} else
		{
			// first, check the length of the inputs
			if (paramTypes.length != inputs.size())
				throw new IllegalArgumentException("wrong number of args" + " to " + functionName);

			// now, make sure everything is of the same, correct type
			int i = 0;
			for (final ExpressionType eval : inputs)
			{
				/*
				 * FIXME: Need to be rethink. Too much duplication and introspection
				 */
				if (eval instanceof AttributeDesignator)
				{
					AttributeDesignator evalTmp = (AttributeDesignator) eval;
					if ((!evalTmp.getType().toString().equals(paramTypes[i])) || (evalTmp.returnsBag() != paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}
				} else if (eval instanceof AttributeSelector)
				{
					AttributeSelector evalTmp = (AttributeSelector) eval;
					if ((!evalTmp.getType().toString().equals(paramTypes[i])) || (evalTmp.returnsBag() != paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}
				} else if (eval instanceof AttributeValue)
				{
					AttributeValue evalTmp = (AttributeValue) eval;
					if ((!evalTmp.getDataType().equals(paramTypes[i])) || (evalTmp.returnsBag() != paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}
				}

				i++;
			}
		}
	}

	/**
	 * Default handling of input checking. This does some simple checking based on the type of
	 * constructor used. If you need anything more complex, or if you used the simple constructor,
	 * then you must override this method.
	 * 
	 * @param inputs
	 *            a <code>List></code> of <code>Evaluatable</code>s
	 * 
	 * @throws IllegalArgumentException
	 *             if the inputs won't work
	 */
	@Override
	public void checkInputsNoBag(List<ExpressionType> inputs) throws IllegalArgumentException
	{
		// first off, see what kind of function we are
		if (singleType)
		{
			// first check to see if we need bags
			if (paramIsBag)
				throw new IllegalArgumentException(functionName + " needs bags on input");

			// now check on the length
			if (numParams != -1)
			{
				if (inputs.size() != numParams)
					throw new IllegalArgumentException("wrong number of args to " + functionName + ". Required: " + numParams);
			} else
			{
				if (inputs.size() < minParams)
					throw new IllegalArgumentException("not enough args to " + functionName + ". Required: >=" + minParams);
			}

			// finally check param list
			int argIndex = 0;
			for (final ExpressionType eval : inputs)
			{
				/*
				 * FIXME: Need to be rethink. Too much duplication and introspection
				 */
				if (eval instanceof AttributeDesignator)
				{
					AttributeDesignator evalTmp = (AttributeDesignator) eval;
					if (!evalTmp.getDataType().toString().equals(paramType))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				} else if (eval instanceof AttributeSelector)
				{
					AttributeSelector evalTmp = (AttributeSelector) eval;
					if (!evalTmp.getDataType().toString().equals(paramType))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				} else if (eval instanceof AttributeValue)
				{
					AttributeValue evalTmp = (AttributeValue) eval;
					if (!evalTmp.getDataType().toString().equals(paramType))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, argIndex, functionName,
								evalTmp.getDataType(), evalTmp.returnsBag(), paramType, paramIsBag));
					}
				}

				argIndex += 1;
			}
		} else
		{
			// first, check the length of the inputs
			if (paramTypes.length != inputs.size())
				throw new IllegalArgumentException("wrong number of args" + " to " + functionName);

			// now, make sure everything is of the same, correct type
			int i = 0;

			/*
			 * FIXME: Need to be rethink. Too much duplication and introspection
			 */
			for (final ExpressionType eval : inputs)
			{
				if (eval instanceof AttributeDesignatorType)
				{
					AttributeDesignator evalTmp = (AttributeDesignator) eval;
					if ((!evalTmp.getDataType().toString().equals(paramTypes[i])) || (paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}

				} else if (eval instanceof AttributeSelectorType)
				{
					AttributeSelector evalTmp = (AttributeSelector) eval;
					if ((!evalTmp.getDataType().toString().equals(paramTypes[i])) || (paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}

				} else if (eval instanceof AttributeValueType)
				{
					AttributeValue evalTmp = (AttributeValue) eval;
					if ((!evalTmp.getDataType().toString().equals(paramTypes[i])) || (paramsAreBags[i]))
					{
						throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_MESSAGE_FORMAT, i, functionName, evalTmp.getDataType(),
								evalTmp.returnsBag(), paramTypes[i], paramsAreBags[i]));
					}
				}

				i++;
			}
		}
	}

	/**
	 * Encodes this <code>FunctionBase</code> into its XML representation and writes this encoding
	 * to the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	@Override
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>FunctionBase</code> into its XML representation and writes this encoding
	 * to the given <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	@Override
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		out.println(indenter.makeString() + "<Function FunctionId=\"" + getFunctionName() + "\"/>");
	}

	@Override
	public boolean equals(Object func)
	{
		if (func instanceof Function)
		{
			Function cmp = (Function) func;

			if (cmp.getIdentifier().equals(this.getIdentifier()))
			{
				return true;
			}
		}
		return false;
	}

}
