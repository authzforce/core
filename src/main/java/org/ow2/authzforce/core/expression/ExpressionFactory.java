package org.ow2.authzforce.core.expression;

import java.io.Closeable;
import java.util.Deque;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.CloseableAttributeFinder;
import org.ow2.authzforce.core.value.Datatype;

import com.sun.xacml.Function;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * Expression factory for parsing XACML {@link ExpressionType}s: AttributeDesignator, AttributeSelector, Apply, etc.
 * <p>
 * Extends {@link Closeable} because it may use an {@link CloseableAttributeFinder} to resolve AttributeDesignators for attributes not provided in the request;
 * and that attribute finder needs to be closed by calling {@link #close()} (in order to call {@link CloseableAttributeFinder#close()}) when it is no longer
 * needed.
 */
public interface ExpressionFactory extends Closeable
{

	/**
	 * Parses an XACML Expression into internal model of expression (evaluable).
	 * 
	 * @param expr
	 *            the JAXB ExpressionType derived from XACML model
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null
	 *            if none specified
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references in the VariableDefinition's expression that is <code>expr</code> or contains <code>expr</code>,
	 *            or null if <code>expr</code> is not in a VariableDefinition. A VariableReference reference chain is a list of VariableIds, such that V1-> V2
	 *            ->... -> Vn -> <code>expr</code> , where "V1 -> V2" means: the expression in VariableDefinition of V1 has a VariableReference to V2. This is
	 *            used to detect exceeding depth of VariableReference reference in VariableDefinitions' expressions. Again, <code>longestVarRefChain</code> may
	 *            be null, if this expression is not used in a VariableDefinition.
	 * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid Expression
	 * @throws ParsingException
	 *             error parsing instance of ExpressionType
	 */
	Expression<?> getInstance(ExpressionType expr, XPathCompiler xPathCompiler, Deque<String> longestVarRefChain) throws ParsingException;

	/**
	 * Parse/create an attribute value expression from XACML-schema-derived JAXB model
	 * 
	 * @param jaxbAttrVal
	 *            XACML-schema-derived JAXB AttributeValue
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null
	 *            if none specified
	 * @return attribute value Expression
	 * @throws ParsingException
	 *             if value cannot be parsed into the value's defined datatype
	 */
	PrimitiveValueExpression<?> getInstance(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws ParsingException;

	/**
	 * Add VariableDefinition to be managed
	 * 
	 * @param varDef
	 *            VariableDefinition
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element.
	 * @return The previous VariableReference if VariableId already used
	 * @throws ParsingException
	 *             error parsing expression in <code>var</code>
	 */
	VariableReference<?> addVariable(oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition varDef, XPathCompiler xPathCompiler)
			throws ParsingException;

	/**
	 * Removes the VariableReference(Definition) from the manager
	 * 
	 * @param varId
	 * @return the VariableReference previously identified by <code>varId</code>, or null if there was no such variable.
	 */
	VariableReference<?> removeVariable(String varId);

	/**
	 * Gets a non-generic function instance
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @return function instance; or null if no such function with ID {@code functionId}
	 * 
	 */
	Function<?> getFunction(String functionId);

	/**
	 * Gets a function instance (generic or non-generic).
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @param subFunctionReturnType
	 *            optional sub-function's return type required only if a generic higher-order function is expected as the result, of which the sub-function is
	 *            expected to be the first parameter; otherwise null (for first-order function). A generic higher-order function is a function whose return type
	 *            depends on the sub-function ('s return type).
	 * @return function instance; or null if no such function with ID {@code functionId}, or if non-null {@code subFunctionReturnTypeId} specified and no
	 *         higher-order function compatible with sub-function's return type {@code subFunctionReturnTypeId}
	 * @throws UnknownIdentifierException
	 *             if datatype {@code subFunctionReturnType} is not supported
	 * 
	 */
	Function<?> getFunction(String functionId, Datatype<?> subFunctionReturnType) throws UnknownIdentifierException;
}