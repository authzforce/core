/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.combining;

import java.util.Objects;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.core.expression.PrimitiveValueExpression;
import org.ow2.authzforce.core.value.AttributeValue;

import com.sun.xacml.ParsingException;

/**
 * Evaluates a XACML CombinerParameter.
 */
public class CombinerParameterEvaluator extends CombinerParameter
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"CombinerParameterEvaluator.setAttributeValue() not allowed");

	private final AttributeValue attrValue;
	private transient final int hashCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter#setAttributeValue(oasis.names .tc.xacml._3_0.core.schema.wd_17.AttributeValueType)
	 */
	@Override
	public final void setAttributeValue(AttributeValueType value)
	{
		// Cannot allow this because we have to make sure value is always instance of our internal
		// AttributeValue class
		throw UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION;
	}

	@Override
	public AttributeValueType getAttributeValue()
	{
		return attrValue;
	}

	/**
	 * Creates a new CombinerParameter handler.
	 * 
	 * @param param
	 *            CombinerParameter as defined by OASIS XACML model
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null
	 *            if none specified
	 * @param expFactory
	 *            attribute value factory
	 * @throws ParsingException
	 */
	public CombinerParameterEvaluator(CombinerParameter param, ExpressionFactory expFactory, XPathCompiler xPathCompiler) throws ParsingException
	{
		// set JAXB AttributeValueType.attributeValue = null, and overridde getAttributeValue() to return an instance of AttributeValue instead
		super(null, param.getParameterName());
		final PrimitiveValueExpression<?> valExpr = expFactory.getInstance(param.getAttributeValue(), xPathCompiler);
		this.attrValue = valExpr.getValue();
		this.hashCode = Objects.hash(this.parameterName, this.attrValue);
	}

	/**
	 * Returns the value provided by this parameter.
	 * 
	 * @return the value provided by this parameter
	 */
	public AttributeValue getValue()
	{
		/*
		 * In the constructor, we make sure input is an AttributeValue, and we override setAttributeValue() to make it unsupported. So this cast should be safe
		 */
		return attrValue;
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof CombinerParameterEvaluator))
		{
			return false;
		}

		final CombinerParameterEvaluator other = (CombinerParameterEvaluator) obj;
		return this.parameterName.equals(other.parameterName) && this.attrValue.equals(other.attrValue);
	}

}
