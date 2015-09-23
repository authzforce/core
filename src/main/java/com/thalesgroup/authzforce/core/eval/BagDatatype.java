package com.thalesgroup.authzforce.core.eval;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;

/**
 * Bag datatype for bags of primitive datatypes
 * 
 * @param <AV>
 */
public class BagDatatype<AV extends AttributeValue<AV>> extends Expression.Datatype<Bag<AV>>
{

	private static final IllegalArgumentException NULL_BAG_ELEMENT_TYPE_EXCEPTION = new IllegalArgumentException("Undefined bag elementType arg");

	/**
	 * Bag datatype ID, for internal identification purposes. This is an invalid URI on purpose, to
	 * avoid conflict with any custom XACML datatype URI (datatype extension).
	 */
	private static String ID = "#BAG#";

	/**
	 * Bad datatype constructor, same {@link Datatype#Datatype(Class, String, Datatype)}, except the
	 * last parameter is mandatory (non-null value)
	 * 
	 * @param bagClass
	 * @param elementType
	 * @throws IllegalArgumentException
	 *             if {@code elementType == null}
	 */
	public BagDatatype(Class<Bag<AV>> bagClass, Datatype<AV> elementType) throws IllegalArgumentException
	{
		super(bagClass, ID, elementType);
		if (elementType == null)
		{
			throw NULL_BAG_ELEMENT_TYPE_EXCEPTION;
		}
	}

	/**
	 * Returns the bag element datatype (datatype of every element in a bag of this datatype)
	 * 
	 * @return bag element datatype
	 */
	public Datatype<AV> getElementType()
	{
		return (Datatype<AV>) this.subTypeParam;
	}
}