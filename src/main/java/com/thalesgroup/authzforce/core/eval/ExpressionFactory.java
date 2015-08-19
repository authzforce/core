package com.thalesgroup.authzforce.core.eval;

import java.io.Closeable;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder;
import com.thalesgroup.authzforce.core.func.FirstOrderFunction;

/**
 * Expression factory for parsing XACML {@link ExpressionType}s: AttributeDesignator,
 * AttributeSelector, Apply, etc.
 * <p>
 * Extends {@link Closeable} because it may use an {@link CloseableAttributeFinder} to resolve
 * AttributeDesignators for attributes not provided in the request; and that attribute finder needs
 * to be closed by calling {@link #close()} (in order to call
 * {@link CloseableAttributeFinder#close()}) when it is no longer needed.
 */
public interface ExpressionFactory extends Closeable
{

	/**
	 * Parses an XACML Expression into internal model of expression (evaluable).
	 * 
	 * @param expr
	 *            the JAXB ExpressionType derived from XACML model
	 * @param policyDefaultValues
	 *            Policy(Set) default values, e.g. XPath version
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references in the VariableDefinition's
	 *            expression that is <code>expr</code> or contains <code>expr</code>, or null if
	 *            <code>expr</code> is not in a VariableDefinition. A VariableReference reference
	 *            chain is a list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>expr</code>
	 *            , where "V1 -> V2" means: the expression in VariableDefinition of V1 has a
	 *            VariableReference to V2. This is used to detect exceeding depth of
	 *            VariableReference reference in VariableDefinitions' expressions. Again,
	 *            <code>longestVarRefChain</code> may be null, if this expression is not used in a
	 *            VariableDefinition.
	 * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid
	 *         Expression
	 * @throws ParsingException
	 *             error parsing instance of ExpressionType
	 */
	Expression<? extends ExpressionResult<? extends AttributeValue>> getInstance(ExpressionType expr, DefaultsType policyDefaultValues, List<String> longestVarRefChain) throws ParsingException;

	/**
	 * Parse/create an attribute value from XACML-schema-derived JAXB model
	 * 
	 * @param jaxbAttrVal
	 *            XACML-schema-derived JAXB AttributeValue
	 * @return attribute value
	 * @throws ParsingException
	 *             if value cannot be parsed into the value's defined datatype
	 */
	AttributeValue createAttributeValue(AttributeValueType jaxbAttrVal) throws ParsingException;

	/**
	 * Create a function instance
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @return function instance, or null if no function with ID {@code functionId} supported
	 * 
	 */
	Function<? extends ExpressionResult<? extends AttributeValue>> getFunction(String functionId);

	/**
	 * Create a higher-order function instance with a first-order sub-function as argument
	 * 
	 * @param functionId
	 *            higher-order function ID (XACML URI)
	 * @param subFunction
	 *            sub-function used as first parameter to the result higher-order function
	 * @return higher-order function instance, or null if no function with ID {@code functionId}
	 *         supported {@code subFunction}'s return type is unknown datatype
	 * @throws UnknownIdentifierException
	 *             if sub-function's return datatype is not valid/supported
	 * 
	 */
	Function<? extends ExpressionResult<? extends AttributeValue>> getHigherOrderFunction(String functionId, FirstOrderFunction<? extends ExpressionResult<? extends AttributeValue>> subFunction) throws UnknownIdentifierException;

	/**
	 * Add VariableDefinition to be managed
	 * 
	 * @param varDef
	 *            VariableDefinition
	 * @param policyDefaultValues
	 *            Policy(Set) default values, such XPath version
	 * @return The previous VariableReference if VariableId already used
	 * @throws ParsingException
	 *             error parsing expression in <code>var</code>
	 */
	VariableReference addVariable(oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition varDef, DefaultsType policyDefaultValues) throws ParsingException;

	/**
	 * Removes the VariableReference(Definition) from the manager
	 * 
	 * @param varId
	 * @return the VariableReference previously identified by <code>varId</code>, or null if there
	 *         was no such variable.
	 */
	VariableReference removeVariable(String varId);

}