package com.thalesgroup.authzforce.core.eval;

import net.sf.saxon.s9api.XdmNode;

import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeSelectorId;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

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
	 * Returns available context evaluation result for given AttributeDesignator.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/id, because not
	 * equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 * java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeGUID
	 *            attribute GUID (global ID = Category,Issuer,AttributeId)
	 * @param datatype
	 *            datatype definition
	 * @param datatypeClass
	 *            class of attribute value(s), should be compatible with <code>designator</code>'s
	 *            Datatype
	 * 
	 * @return attribute value(s), null iff attribute unknown (not set) in this context, empty if
	 *         attribute known in this context but no value
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the attribute value(s) in context. This is
	 *             different from finding without error that the attribute is not in the context
	 *             (and/or no value), e.g. if there is a result but type is different from
	 *             {@code datatypeClass}.
	 */
	public <T extends AttributeValue> BagResult<T> getAttributeDesignatorResult(AttributeGUID attributeGUID, Class<T> datatypeClass, DatatypeDef datatype) throws IndeterminateEvaluationException;

	/**
	 * Put Attribute values in the context, only if the attribute is not already known to this
	 * context. Indeed, an attribute value cannot be overridden once it is set in the context to
	 * comply with 7.3.5 Attribute retrieval: "Regardless of any dynamic modifications of the
	 * request context during policy evaluation, the PDP SHALL behave as if each bag of attribute
	 * values is fully populated in the context before it is first tested, and is thereafter
	 * immutable during evaluation." Therefore,
	 * {@link #getAttributeDesignatorResult(AttributeGUID, Class, DatatypeDef)} should be called
	 * always before calling this, for the same {@code attributeGUID}
	 * 
	 * @param attributeGUID
	 *            attribute's global ID
	 * @param result
	 *            attribute values
	 * @return the current values in this context, or null if it was absent from this context before
	 *         this method call
	 * @throws IndeterminateEvaluationException
	 */
	public <T extends AttributeValue> BagResult<T> putAttributeDesignatorResultIfAbsent(AttributeGUID attributeGUID, BagResult<T> result) throws IndeterminateEvaluationException;

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
	 * @param datatypeClass
	 *            class of attribute value(s), should be compatible with
	 *            <code>attributeDatatype</code>
	 * @param datatypeURI
	 *            datatypeURI datatype URI
	 * @return attribute value(s), null iff AttributeSelector's bag of values unknown (not set) in
	 *         this context because not evaluated yet; empty if it was evaluated in this context but
	 *         not result, i.e. bag is empty
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the result in context. This is different
	 *             from finding without error that the result is not in the context (and/or no
	 *             value), e.g. if there is a result but type is different from
	 *             {@code datatypeClass}.
	 */
	public <T extends AttributeValue> BagResult<T> getAttributeSelectorResult(AttributeSelectorId attributeSelectorId, Class<T> datatypeClass, String datatypeURI) throws IndeterminateEvaluationException;

	/**
	 * Put an Attribute Selector's values in the context, only if the AttributeSelector has not been
	 * already evaluated in this context. Therefore
	 * {@link #getAttributeSelectorResult(AttributeSelectorId, Class, String)} should be called
	 * always before calling this, for the same {@code attributeSelectorId}
	 * 
	 * @param attributeSelectorId
	 *            AttributeSelector ID
	 * @param result
	 *            AttributeSelector value bag
	 * @return the current values in this context, or null if this method is called for the first
	 *         time in this context
	 * @throws IndeterminateEvaluationException
	 */
	public <T extends AttributeValue> BagResult<T> putAttributeSelectorResultIfAbsent(AttributeSelectorId attributeSelectorId, BagResult<T> result) throws IndeterminateEvaluationException;

	/**
	 * Returns the {@literal<Content>} of the {@literal<Attibutes>} identified by a given category,
	 * to be used for AttributeSelector evaluation.
	 * 
	 * @param category
	 *            category of the Attributes element from which to get the Content.
	 * 
	 * @return the resulting Content node, or null if none in the request Attributes category
	 */

	public XdmNode getAttributesContent(String category);

	/**
	 * Get value of a VariableDefinition's expression evaluated in this context and whose value has
	 * been cached with {@link #putVariableIfAbsent(String, ExpressionResult)}. To be used when
	 * evaluating VariableReferences.
	 * 
	 * @param variableId
	 *            VariableId identifying the VariableDefinition
	 * @return value of the evaluated VariableDefinition's expression, or null if not evaluated
	 *         (yet) in this context
	 */
	public ExpressionResult<? extends AttributeValue> getVariableValue(String variableId);

	/**
	 * Caches the value of a VariableDefinition's expression evaluated in this context only if
	 * variable is not already set in this context, for later retrieval by
	 * {@link #getVariableValue(String)} when evaluating ValueReferences to the same VariableId.
	 * <p>
	 * The variable is set only if it was absent from context. In other words, this method does/must
	 * not allow setting the same variable twice. The reason is compliance with XACML spec 7.8
	 * VariableReference evaluation:
	 * "the value of an Expression element remains the same for the entire policy evaluation."
	 * </p>
	 * 
	 * @param variableId
	 *            VariableId identifying the VariableDefinition the expression of which resulted in
	 *            <code>value</code> after evaluation in this context
	 * @param value
	 *            value of the VariableDefinition's expression evaluated in this context
	 * @return current value of the variable in this context if a value was already there, or null
	 *         if there was none (first time it is set)
	 */
	public ExpressionResult<? extends AttributeValue> putVariableIfAbsent(String variableId, ExpressionResult<? extends AttributeValue> value);

	/**
	 * Get custom property
	 * 
	 * @see java.util.Map#get(Object)
	 * @param key
	 * @return property
	 */
	public Object getOther(String key);

	/**
	 * Check whether custom property is in the context
	 * 
	 * @see java.util.Map#containsKey(Object)
	 * @param key
	 * @return true if and only if key exists in updatable property keys
	 */
	public boolean containsKey(String key);

	/**
	 * Puts custom property in the context
	 * 
	 * @see java.util.Map#put(Object, Object)
	 * @param key
	 * @param val
	 */
	public void putOther(String key, Object val);

	/**
	 * Removes custom property from the context
	 * 
	 * @see java.util.Map#remove(Object)
	 * @param key
	 * @return the previous value associated with key, or null if there was no mapping for key.
	 */
	public Object remove(String key);

}
