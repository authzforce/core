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
package org.ow2.authzforce.core.pdp.impl.value;

import net.sf.saxon.lib.StandardURIChecker;

/**
 * Represent the URI value that this class represents
 * <p>
 * WARNING: java.net.URI cannot be used here for this XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * <p>
 * See also:
 * </p>
 * <p>
 * https://java.net/projects/jaxb/lists/users/archive/2011-07/message/16
 * </p>
 * <p>
 * From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the java.net.URI constructor. Using a global JAXB customization
 * described in Section 7.9".
 * </p>
 * <p>
 * Last but not least, we now refer to the definition of anyURI datatype given in XSD 1.1, which has the same value space as the string datatype. More info in the XSD 1.1 datatypes document and SAXON
 * documentation: http://www.saxonica.com/html/documentation9.4/changes/intro93/xsd11-93.html. Also confirmed on the mailing list:
 * https://sourceforge.net/p/saxon/mailman/saxon-help/thread/4F9E683E.8060001@saxonica.com/. Although XACML 3.0 still refers to XSD 1.0 and its stricter definition of anyURI, we prefer to anticipate
 * and use the definition from XSD 1.1 for XACML AttributeValues of datatype anyURI. However, this does not affect XACML schema validation of Policy/PolicySet/Request documents, where the XSD 1.0
 * definition of anyURI still applies.
 * </p>
 * <p>
 * With the new anyURI definition of XSD 1.1, we also avoid using {@link StandardURIChecker} which maintains a thread-local cache of validated URIs (cache size is 50 and eviction policy is LRU) that
 * may be spotted as a possible memory leak by servlet containers such as Tomcat, as confirmed on the mailing list: https://sourceforge.net/p/saxon/mailman/message/27043134/ ,
 * https://sourceforge.net/p/saxon/mailman/saxon-help/thread/4F9E683E.8060001@saxonica.com/ .
 * </p>
 */
public final class AnyURIValue extends SimpleValue<String>
{

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#anyURI";

	/**
	 * Creates a new <code>AnyURIAttributeValue</code> that represents the URI value supplied.
	 * 
	 * @param value
	 *            the URI to be represented
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI. [1]
	 *            http://www.w3.org/TR/xmlschema-2/#anyURI So we use String instead.
	 *            </p>
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation for xs:anyURI
	 */
	public AnyURIValue(String value) throws IllegalArgumentException
	{
		super(TYPE_URI, value);
	}

	@Override
	public String printXML()
	{
		return this.value;
	}

}
