package org.ow2.authzforce.core.expression;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.XACMLBindingUtils;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Datatype;

/**
 * 
 * Expression wrapper for primitive static values to be used as Expressions, e.g. as function arguments; 'static' here means the actual value does not depend on
 * the evaluation context; it evaluates to itself.
 * 
 * @param <V>
 *            concrete value type
 *
 */
public final class PrimitiveValueExpression<V extends AttributeValue> extends ValueExpression<V>
{

	/**
	 * Creates instance
	 * 
	 * @param type
	 *            value datatype
	 * @param v
	 *            static value
	 */
	public PrimitiveValueExpression(Datatype<V> type, V v)
	{
		super(type, v);
	}

	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		// create new JAXB AttributeValue as defensive copy (JAXB AttributeValue is not immutable)
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(this.value);
	}
}
