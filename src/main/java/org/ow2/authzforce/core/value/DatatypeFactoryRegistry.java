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
package org.ow2.authzforce.core.value;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.PdpExtensionRegistry;
import org.ow2.authzforce.core.expression.PrimitiveValueExpression;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * Registry of AttributeValue Factories supporting multiple datatypes. Any implementation of this must guarantee that there is a one-to-one relationship between
 * AttributeValue (sub)classes and datatype URIs (AttributeValueType DataType field)
 * 
 */
public interface DatatypeFactoryRegistry extends PdpExtensionRegistry<DatatypeFactory<?>>
{

	/**
	 * Create internal model's AttributeValue expression
	 * 
	 * @param value
	 *            AttributeValue from OASIS XACML model
	 * @param xPathCompiler
	 *            XPath compiler for evaluating/compiling any XPath expression in {@code value}
	 * @return AttributeValue expression
	 * @throws ParsingException
	 *             if value cannot be parsed into the value's defined datatype
	 * @throws UnknownIdentifierException
	 *             value datatype unknown/not supported
	 */
	PrimitiveValueExpression<?> createValueExpression(AttributeValueType value, XPathCompiler xPathCompiler) throws UnknownIdentifierException,
			ParsingException;

	// /**
	// * Create internal model's AttributeValue expression
	// *
	// * @param value
	// * AttributeValue from OASIS XACML model
	// * @param datatype
	// * datatype of the instance returned
	// * @param xPathCompiler
	// * XPath compiler for evaluating/compiling any XPath expression in {@code value}
	// * @return AttributeValue expression
	// * @throws ParsingException
	// * if value cannot be parsed into the value's defined datatype
	// * @throws UnknownIdentifierException
	// * value datatype unknown/not supported
	// */
	// <AV extends AttributeValue> PrimitiveValueExpression<AV> createValueExpression(AttributeValueType value,
	// Datatype<AV> datatype, XPathCompiler xPathCompiler) throws UnknownIdentifierException, ParsingException;
}
