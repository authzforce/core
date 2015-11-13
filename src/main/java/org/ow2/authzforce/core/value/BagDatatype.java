package org.ow2.authzforce.core.value;

/**
 * Bag datatype for bags of primitive datatypes
 * 
 * @param <AV>
 */
public final class BagDatatype<AV extends AttributeValue> extends Datatype<Bag<AV>>
{

	private static final IllegalArgumentException NULL_ARG_EXCEPTION = new IllegalArgumentException("Undefined element datatype arg");

	/**
	 * Bag datatype ID, for internal identification purposes. This is an invalid URI on purpose, to avoid conflict with any custom XACML datatype URI (datatype
	 * extension).
	 */
	private static final String ID = "#BAG#";

	private Datatype<AV> elementType;

	// cached method results
	private final String toString;
	private final int hashCode;

	private static <V extends AttributeValue> Class<Bag<V>> getBagClass(Datatype<V> elementDatatype) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_ARG_EXCEPTION;
		}

		final Bag<V> bag = new Bag<>(elementDatatype);
		return (Class<Bag<V>>) bag.getClass();
	}

	BagDatatype(Datatype<AV> elementDatatype) throws IllegalArgumentException
	{
		super(getBagClass(elementDatatype), ID);

		this.elementType = elementDatatype;
		toString = ID + "<" + this.elementType + ">";
		hashCode = this.elementType.hashCode();
	}

	/**
	 * Returns the bag element datatype (datatype of every element in a bag of this datatype). Same as {@link #getTypeParameter()}.
	 * 
	 * @return bag element datatype
	 */
	public Datatype<AV> getElementType()
	{
		return this.elementType;
	}

	@Override
	public Datatype<?> getTypeParameter()
	{
		return this.elementType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return toString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BagDatatype))
		{
			return false;
		}

		final BagDatatype<?> other = (BagDatatype<?>) obj;
		// there should be a one-to-one mapping between valueClass and id, so checking
		// only one of these two is necessary
		return this.elementType.equals(other.elementType);

	}

}