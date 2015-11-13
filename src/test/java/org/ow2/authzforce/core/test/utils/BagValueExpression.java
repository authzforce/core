package org.ow2.authzforce.core.test.utils;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.expression.ValueExpression;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.Datatype;

public class BagValueExpression<BV extends Bag<?>> extends ValueExpression<BV>
{

	protected BagValueExpression(Datatype<BV> datatype, BV v) throws IllegalArgumentException
	{
		super(datatype, v);
	}

	@Override
	public JAXBElement<? extends ExpressionType> getJAXBElement()
	{
		throw new UnsupportedOperationException();
	}

}
