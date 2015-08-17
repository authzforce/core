package com.thalesgroup.authzforce.core.attr;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.attr.AttributeValue.Factory;

/**
 * Basic implementation of <code>DatatypeFactoryRegistry</code>.
 */
public class BaseDatatypeFactoryRegistry extends BasePdpExtensionRegistry<AttributeValue.Factory<? extends AttributeValue>> implements DatatypeFactoryRegistry
{

	private final Set<Factory<?>> datatypeFactoryClasses = new HashSet<>();

	protected BaseDatatypeFactoryRegistry(Map<String, AttributeValue.Factory<?>> attributeValueFactoriesByDatatypeURI)
	{
		super(attributeValueFactoriesByDatatypeURI);
	}

	/**
	 * Constructor that configures this factory with an initial set of supported datatypes.
	 * 
	 * @param baseFactory
	 *            (base) factory that this factory extends, i.e. this factory inherits all the
	 *            datatype factories of {@code baseFactory}. If null, this is equivalent to
	 *            {@link #BaseDatatypeFactoryRegistry()}.
	 */
	public BaseDatatypeFactoryRegistry(BaseDatatypeFactoryRegistry baseFactory)
	{
		super(baseFactory);
	}

	/**
	 * Creates instance without any registered datatype
	 */
	public BaseDatatypeFactoryRegistry()
	{
		super();
	}

	@Override
	public AttributeValue createValue(AttributeValueType attrVal) throws UnknownIdentifierException, ParsingException
	{
		return createValue(attrVal, AttributeValue.class);
	}

	@Override
	public <T extends AttributeValue> T createValue(AttributeValueType jaxbAttrVal, Class<T> valClass) throws UnknownIdentifierException, ParsingException
	{
		final String type = jaxbAttrVal.getDataType();
		final AttributeValue.Factory<? extends AttributeValue> attrFactory = getExtension(type);
		if (attrFactory == null)
		{
			throw new UnknownIdentifierException("Attribute datatype '" + type + "' is not supported.");
		}

		final AttributeValue attrVal;
		try
		{
			attrVal = attrFactory.getInstance(jaxbAttrVal);
		} catch (IllegalArgumentException e)
		{
			throw new ParsingException("Error creating Attribute value of type '" + type + "'", e);
		}

		try
		{
			return valClass.cast(attrVal);
		} catch (ClassCastException e)
		{
			throw new IllegalArgumentException("Invalid attribute value class argument (" + valClass + ") for the datatype argument '" + type + "'. Expected: " + attrVal.getClass() + " (as defined in attribute datatype factory) or any superclass/superinterface");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thalesgroup.authzforce.core.BasePdpExtensionRegistry#addExtension(com.thalesgroup.authzforce
	 * .core.PdpExtension)
	 */
	@Override
	public void addExtension(Factory<? extends AttributeValue> datatypeFactory) throws IllegalArgumentException
	{
		if (!datatypeFactoryClasses.add(datatypeFactory))
		{
			throw new IllegalArgumentException("Attribute value factory (" + datatypeFactory + ") already registered for another datatype, or conflicting with another factory registered for the same datatype class in this list of registered factories: " + datatypeFactoryClasses);
		}

		super.addExtension(datatypeFactory);
	}

}
