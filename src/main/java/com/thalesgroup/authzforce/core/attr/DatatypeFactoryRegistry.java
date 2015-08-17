package com.thalesgroup.authzforce.core.attr;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.thalesgroup.authzforce.core.PdpExtensionRegistry;

/**
 * Registry of AttributeValue Factories supporting multiple datatypes. Any implementation of this
 * must guarantee that there is a one-to-one relationship between AttributeValue (sub)classes and
 * datatype URIs (AttributeValueType DataType field)
 * 
 */
public interface DatatypeFactoryRegistry extends PdpExtensionRegistry<AttributeValue.Factory<? extends AttributeValue>>
{

	/**
	 * Create internal model's AttributeValue
	 * 
	 * @param value
	 *            AttributeValue from OASIS XACML model
	 * @return AttributeValue
	 * @throws ParsingException
	 *             if value cannot be parsed into the value's defined datatype
	 * @throws UnknownIdentifierException
	 *             value datatype unknown/not supported
	 */
	AttributeValue createValue(oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType value) throws UnknownIdentifierException, ParsingException;

	/**
	 * Create internal model's AttributeValue
	 * 
	 * @param value
	 *            AttributeValue from OASIS XACML model
	 * @param valueClass
	 *            concrete class of the instance returned
	 * @return SunXACML AttributeValue
	 * @throws ParsingException
	 *             if value cannot be parsed into the value's defined datatype
	 * @throws UnknownIdentifierException
	 *             value datatype unknown/not supported
	 */
	<T extends AttributeValue> T createValue(oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType value, Class<T> valueClass) throws UnknownIdentifierException, ParsingException;
}
