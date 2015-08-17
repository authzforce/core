/**
 * 
 */
package com.thalesgroup.authzforce.core.eval;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

/**
 * Generic data type definition that represents a primitive XACML datatype or a bag of primitive
 * XACML datatypes
 */
public class DatatypeDef
{
	/**
	 * Generic type representing any primitive datatype: {@link #datatypeURI()} returns null and
	 * {@link #isBag()} returns false
	 */
	public static final DatatypeDef ANY_PRIMITIVE = new DatatypeDef(null, false, false);

	/**
	 * Generic type representing any bag type: {@link #datatypeURI()} returns null and
	 * {@link #isBag()} returns true
	 */
	public static final DatatypeDef ANY_BAG = new DatatypeDef(null, false, true);

	private static final String XML_FRAGMENT_START = "<xml-fragment>";
	private static final String XML_FRAGMENT_END = "</xml-fragment>";

	// private static final String XML_FRAGMENT_FORMAT = XML_FRAGMENT_START+"%s"+XML_FRAGMENT_END;

	/**
	 * Validates string representation of XML schema type anyURI
	 * 
	 * @param xsAnyURI
	 * @throws IllegalArgumentException
	 *             if not valid
	 */
	public static void validateURI(String xsAnyURI)
	{
		final XmlOptions validationOps = new XmlOptions();
		validationOps.setValidateStrict();
		final Queue<XmlError> errors = new ArrayDeque<>();
		validationOps.setErrorListener(errors);
		final XmlAnyURI xmlAnyURI;
		try
		{
			// Using String + operator is faster than String.format() here (see main() for tests)
			xmlAnyURI = XmlAnyURI.Factory.parse(XML_FRAGMENT_START + xsAnyURI + XML_FRAGMENT_END);
			xmlAnyURI.validate(validationOps);
		} catch (XmlException e)
		{
			throw new IllegalArgumentException("Invalid XML anyURI '" + xsAnyURI + "'", e);
		}

		if (!errors.isEmpty())
		{
			throw new IllegalArgumentException("Invalid XML anyURI '" + xsAnyURI + "': " + errors);
		}
	}

	private final String datatype;
	private final boolean isBag;

	private DatatypeDef(String datatypeURI, boolean checkDatatypeURI, boolean isBag)
	{
		if (checkDatatypeURI)
		{
			if (datatypeURI == null || datatypeURI.isEmpty())
			{
				throw new IllegalArgumentException("Undefined or empty parameter datatype URI");
			}

			validateURI(datatypeURI);
		}

		this.datatype = datatypeURI;
		this.isBag = isBag;
	}

	/**
	 * Instantiates this class with isBag=false
	 * 
	 * @param datatypeURI
	 *            paramater type URI (e.g. XACML datatype URI)
	 */
	public DatatypeDef(String datatypeURI)
	{
		this(datatypeURI, false);
	}

	/**
	 * Instantiates this class
	 * 
	 * @param datatypeURI
	 *            paramater type URI (e.g. XACML datatype URI); may be null to indicate any
	 *            datatype, or in other words, the super datatype of all (like Java Object)
	 * @param isBag
	 *            whether this parameter is single-valued or multi-valued, i.e. a bag of
	 *            <code>datatypeURI</code> values
	 */
	public DatatypeDef(String datatypeURI, boolean isBag)
	{
		this(datatypeURI, true, isBag);
	}

	/**
	 * @return the datatype, null if any type
	 */
	public String datatypeURI()
	{
		return datatype;
	}

	/**
	 * @return the isBag
	 */
	public boolean isBag()
	{
		return isBag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return isBag ? "Bag<" + datatype + ">" : datatype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(datatype, isBag);
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
		final DatatypeDef other = (DatatypeDef) obj;

		if (datatype == null)
		{
			if (other.datatype != null)
			{
				return false;
			}
		} else

		if (!datatype.equals(other.datatype))
		{
			return false;
		}
		if (isBag != other.isBag)
		{
			return false;
		}
		return true;
	}

}
