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
package org.ow2.authzforce.core.test.utils;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.expression.ValueExpression;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Bag value expression
 *
 * @param <BV>
 *            bag type
 */
public class BagValueExpression<BV extends Bag<?>> extends ValueExpression<BV>
{

	protected BagValueExpression(Datatype<BV> datatype, BV v) throws IllegalArgumentException
	{
		super(datatype, v, true);
	}

	@Override
	public JAXBElement<? extends ExpressionType> getJAXBElement()
	{
		throw new UnsupportedOperationException();
	}

}
