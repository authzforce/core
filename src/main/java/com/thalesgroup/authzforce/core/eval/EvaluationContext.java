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

import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;

import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeSelectorId;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

/**
 * Manages context for the policy evaluation of a given authorization decision request. Typically,
 * an instance of this is instantiated whenever the PDP gets a request and needs to perform an
 * evaluation to a authorization decision. Such a context is used and possibly updated all along the
 * evaluation of the request.
 * 
 */
public interface EvaluationContext
{

	/**
	 * Get default XPath compiler for compiling/evaluating all XPath expressions in the request
	 * context (derived from XACML {@link RequestDefaults#getXPathVersion()}); null if none
	 * specified in the XACML Request.
	 * 
	 * @return default XPath compiler
	 */
	XPathCompiler getDefaultXPathCompiler();

	/**
	 * Returns available context evaluation result for given AttributeDesignator.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/id, because not
	 * equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 * java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeGUID
	 *            attribute GUID (global ID = Category,Issuer,AttributeId)
	 * @param bagDatatype
	 *            expected result (bag) datatype ({@code AV} is the type of each element in the bag)
	 * 
	 * @return attribute value(s), null iff attribute unknown (not set) in this context, empty if
	 *         attribute known in this context but no value
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the attribute value(s) in context. This is
	 *             different from finding without error that the attribute is not in the context
	 *             (and/or no value), e.g. if there is a result but type is different from
	 *             {@code datatypeClass}.
	 */
	<AV extends AttributeValue<AV>> Bag<AV> getAttributeDesignatorResult(AttributeGUID attributeGUID, Datatype<Bag<AV>> bagDatatype) throws IndeterminateEvaluationException;

	/**
	 * Put Attribute values in the context, only if the attribute is not already known to this
	 * context. Indeed, an attribute value cannot be overridden once it is set in the context to
	 * comply with 7.3.5 Attribute retrieval: "Regardless of any dynamic modifications of the
	 * request context during policy evaluation, the PDP SHALL behave as if each bag of attribute
	 * values is fully populated in the context before it is first tested, and is thereafter
	 * immutable during evaluation." Therefore,
	 * {@link #getAttributeDesignatorResult(AttributeGUID, Datatype)} should be called always before
	 * calling this, for the same {@code attributeGUID}
	 * 
	 * @param attributeGUID
	 *            attribute's global ID
	 * @param result
	 *            attribute values
	 * @return false iff there is already a matching value in this context (this operation did NOT
	 *         succeed)
	 */
	boolean putAttributeDesignatorResultIfAbsent(AttributeGUID attributeGUID, Bag<?> result);

	/**
	 * Returns available context evaluation result for a given AttributeSelector. This feature is
	 * optional. Any implementation that does not implement this method may throw
	 * {@link UnsupportedOperationException}.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/contextSelectorId,
	 * because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not
	 * in java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeSelectorId
	 *            AttributeSelector ID
	 * @param bagDatatype
	 *            expected result (bag) datatype ({@code AV} is the type of each element in the bag)
	 * @return attribute value(s), null iff AttributeSelector's bag of values unknown (not set) in
	 *         this context because not evaluated yet; empty if it was evaluated in this context but
	 *         not result, i.e. bag is empty
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the result in context. This is different
	 *             from finding without error that the result is not in the context (and/or no
	 *             value), e.g. if there is a result but type is different from
	 *             {@code datatypeClass}.
	 */
	<AV extends AttributeValue<AV>> Bag<AV> getAttributeSelectorResult(AttributeSelectorId attributeSelectorId, Datatype<Bag<AV>> bagDatatype) throws IndeterminateEvaluationException;

	/**
	 * Put an Attribute Selector's values in the context, only if the AttributeSelector has not been
	 * already evaluated in this context. Therefore
	 * {@link #getAttributeSelectorResult(AttributeSelectorId, Datatype)} should be called always
	 * before calling this, for the same {@code attributeSelectorId}
	 * 
	 * @param attributeSelectorId
	 *            AttributeSelector ID
	 * @param result
	 *            AttributeSelector value bag
	 * @return false iff there is already a matching value in this context (this operation could NOT
	 *         succeed)
	 * @throws IndeterminateEvaluationException
	 *             if AttributeSelector evaluation is not supported (this is an optional feature of
	 *             XACML specification)
	 */
	boolean putAttributeSelectorResultIfAbsent(AttributeSelectorId attributeSelectorId, Bag<?> result) throws IndeterminateEvaluationException;

	/**
	 * Returns the {@literal<Content>} of the {@literal<Attibutes>} identified by a given category,
	 * to be used for AttributeSelector evaluation.
	 * 
	 * @param category
	 *            category of the Attributes element from which to get the Content.
	 * 
	 * @return the resulting Content node, or null if none in the request Attributes category
	 */

	XdmNode getAttributesContent(String category);

	/**
	 * Get value of a VariableDefinition's expression evaluated in this context and whose value has
	 * been cached with {@link #putVariableIfAbsent(String, Value)}. To be used when evaluating
	 * VariableReferences.
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param datatype
	 *            datatype
	 * @return value of the evaluated VariableDefinition's expression, or null if not evaluated
	 *         (yet) in this context
	 * @throws IndeterminateEvaluationException
	 *             if actual datatype of variable value in context does not match expected
	 *             {@code datatype}
	 */
	<V extends Value<?>> V getVariableValue(String variableId, Datatype<V> datatype) throws IndeterminateEvaluationException;

	/**
	 * Caches the value of a VariableDefinition's expression evaluated in this context only if
	 * variable is not already set in this context, for later retrieval by
	 * {@link #getVariableValue(String, Datatype)} when evaluating ValueReferences to the same
	 * VariableId.
	 * <p>
	 * The variable is set only if it was absent from context. In other words, this method does/must
	 * not allow setting the same variable twice. The reason is compliance with XACML spec 7.8
	 * VariableReference evaluation:
	 * "the value of an Expression element remains the same for the entire policy evaluation."
	 * </p>
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param value
	 *            value of the VariableDefinition's expression evaluated in this context
	 * @return false iff there is already a value for this variable in context (this operation could
	 *         NOT succeed).
	 */
	boolean putVariableIfAbsent(String variableId, Value<?> value);

	/**
	 * Removes a variable (defined by VariableDefinition) from this context.
	 * 
	 * @param variableId
	 *            identifies the Variable to remove
	 * @return the value of the variable before removal, or null if there was no such variable set
	 *         in this context.
	 */
	Value<?> removeVariable(String variableId);

	/**
	 * Get custom property
	 * 
	 * @see java.util.Map#get(Object)
	 * @param key
	 * @return property
	 */
	Object getOther(String key);

	/**
	 * Check whether custom property is in the context
	 * 
	 * @see java.util.Map#containsKey(Object)
	 * @param key
	 * @return true if and only if key exists in updatable property keys
	 */
	boolean containsKey(String key);

	/**
	 * Puts custom property in the context
	 * 
	 * @see java.util.Map#put(Object, Object)
	 * @param key
	 * @param val
	 */
	void putOther(String key, Object val);

	/**
	 * Removes custom property from the context
	 * 
	 * @see java.util.Map#remove(Object)
	 * @param key
	 * @return the previous value associated with key, or null if there was no mapping for key.
	 */
	Object remove(String key);

}
