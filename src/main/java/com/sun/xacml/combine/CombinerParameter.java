/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.combine;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;

/**
 * Represents a single named parameter to a combining algorithm. Parameters are only used by XACML
 * 2.0 and later policies.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
class CombinerParameter extends oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION = new UnsupportedOperationException("CombinerParameter.setAttributeValue() not allowed");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter#setAttributeValue(oasis.names
	 * .tc.xacml._3_0.core.schema.wd_17.AttributeValueType)
	 */
	@Override
	public final void setAttributeValue(AttributeValueType value)
	{
		// Cannot allow this because we have to make sure value is always instance of our internal
		// AttributeValue class
		throw UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION;
	}

	/**
	 * Creates a new CombinerParameter handler.
	 * 
	 * @param param
	 *            CombinerParameter as defined by OASIS XACML model
	 * @param expFactory
	 *            attribute value factory
	 * @throws ParsingException
	 */
	CombinerParameter(oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter param, ExpressionFactory expFactory) throws ParsingException
	{
		super(expFactory.createAttributeValue(param.getAttributeValue()), param.getParameterName());
	}

	/**
	 * Returns the value provided by this parameter.
	 * 
	 * @return the value provided by this parameter
	 */
	public AttributeValue<?> getValue()
	{
		/*
		 * In the constructor, we make sure input is an AttributeValue, and we override
		 * setAttributeValue() to make it unsupported. So this cast should be safe
		 */
		return (AttributeValue<?>) attributeValue;
	}

}
