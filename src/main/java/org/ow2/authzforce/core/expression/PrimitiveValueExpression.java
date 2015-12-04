/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
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
	 * @param isStatic
	 *            true iff the expression based on this value always evaluates to the same constant in any context (not the case for xpathExpressions for
	 *            instance)
	 */
	public PrimitiveValueExpression(Datatype<V> type, V v, boolean isStatic)
	{
		super(type, v, isStatic);
	}

	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		// create new JAXB AttributeValue as defensive copy (JAXB AttributeValue is not immutable)
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(this.value);
	}
}
