package com.thalesgroup.authzforce.core.attr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * The base type for all attribute value datatypes used in a policy or request/response, this
 * abstract class represents a value for a given attribute type. All the required types defined in
 * the XACML specification are provided as instances of <code>AttributeValue<code>s. If you want to
 * provide a new type, extend this class, implement at least one Constructor with same signature as {@link #AttributeValue(AttributeValueType)} and calling this super constructor, and implement the
 * <code>equals(Object)</code> and <code>hashCode</code> methods from <code>Object</code>, which are
 * used for equality checking and hash-based storage.
 * 
 */
public abstract class AttributeValue extends AttributeValueType implements Serializable
{
	/**
	 * XML datatype factory for parsing XML-Schema-compliant date/time/duration values into Java
	 * types. DatatypeFactory's official javadoc does not say whether it is thread-safe. But bug
	 * report indicates it should be and has been so far:
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177 Reusing the same instance matters
	 * for performance: https://www.java.net/node/666491 The alternative would be to use ThreadLocal
	 * to limit thread-safety issues in the future.
	 */
	protected static final DatatypeFactory XML_TEMPORAL_DATATYPE_FACTORY;
	static
	{
		try
		{
			XML_TEMPORAL_DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e)
		{
			throw new RuntimeException("Error instantiating XML datatype factory for parsing strings corresponding to XML schema date/time/duration values into Java types", e);
		}
	}

	/**
	 * Datatype-specific Attribute Value RefPolicyFinderModuleFactory. Implementations of
	 * {@link #getId()} (PDP extension ID) must return the URI of the value datatype the factory
	 * implementation supports.
	 * 
	 * @param <T>
	 *            type of attribute values
	 */
	public static abstract class Factory<T extends AttributeValue> implements PdpExtension
	{
		private final Class<T> instanceClass;
		private final DatatypeDef datatype;

		protected Factory(Class<T> instanceClass)
		{
			this.instanceClass = instanceClass;
			this.datatype = new DatatypeDef(getId());
		}

		/**
		 * Get the class of all instances created by this factory, i.e. the class of attribute
		 * values
		 * 
		 * @return instance class
		 */
		public Class<T> getInstanceClass()
		{
			return instanceClass;
		}

		/**
		 * Get datatype of values created by this factory
		 * 
		 * @return supported attribute value datatype
		 */
		public DatatypeDef getDatatype()
		{
			return datatype;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return getClass().getName() + "[datatypeURI=" + getId() + ", datatypeClass=" + instanceClass + "]";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return Objects.hash(instanceClass);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			Factory<?> other = (Factory<?>) obj;
			if (instanceClass == null)
			{
				if (other.instanceClass != null)
				{
					return false;
				}
			} else if (!instanceClass.equals(other.instanceClass))
			{
				return false;
			}
			return true;
		}

		/**
		 * Create attribute value from XML input (unmarshalled with JAXB)
		 * 
		 * @param jaxbAttributeValue
		 *            JAXB-unmarshalled XML input value
		 * @return attribute value in internal model compatible with expression evaluator
		 * @throws IllegalArgumentException
		 *             if jaxbAttributeValue is the datatype URI or content is not valid for this
		 *             factory
		 */
		public abstract T getInstance(AttributeValueType jaxbAttributeValue) throws IllegalArgumentException;
	}

	private static IllegalArgumentException UNDEF_ATTR_VAL_TYPE_EXCEPTION = new IllegalArgumentException("Undefined attribute value datatype");

	private static final UnsupportedOperationException UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION = new UnsupportedOperationException("AttributeValue.setDataType() not allowed");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType#setDataType(java.lang.String)
	 */
	@Override
	public final void setDataType(String value)
	{
		// datatype only set with constructor (immutable)
		throw UNSUPPORTED_SET_DATATYPE_OPERATION_EXCEPTION;
	}

	protected AttributeValue(AttributeValueType jaxbVal)
	{
		this(jaxbVal.getContent(), jaxbVal.getDataType(), jaxbVal.getOtherAttributes());
	}

	protected AttributeValue(String datatype, List<Serializable> content)
	{
		this(content, datatype, null);
	}

	protected AttributeValue(List<Serializable> content, String datatype, Map<QName, String> otherAttributes)
	{
		super(content, datatype, otherAttributes);
		if (datatype == null)
		{
			throw UNDEF_ATTR_VAL_TYPE_EXCEPTION;
		}
	}

}
