package com.thalesgroup.authzforce.core.attr;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Represent the URI value that this class represents
 * <p>
 * WARNING: java.net.URI cannot be used here for this XACML datatype, because not equivalent to XML
 * schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
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
 * From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible
 * values of xs:anyURI can be passed to the java.net.URI constructor. Using a global JAXB
 * customization described in Section 7.9".
 * </p>
 */
public class AnyURIAttributeValue extends PrimitiveAttributeValue<String>
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
	 *            WARNING: java.net.URI cannot be used here for XACML datatype, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI So we use String
	 *            instead.
	 *            </p>
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation for xs:anyURI
	 */
	public AnyURIAttributeValue(String value) throws IllegalArgumentException
	{
		super(TYPE, value);
	}

	/**
	 * Creates instance from XML/JAXB value
	 * 
	 * @param jaxbAttrVal
	 *            JAXB AttributeValue
	 * @throws IllegalArgumentException
	 *             if not valid value for datatype {@value #TYPE_URI}
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(DatatypeDef, AttributeValueType)
	 */
	public AnyURIAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(TYPE, jaxbAttrVal);
	}

	/**
	 * Generic type info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(TYPE_URI);

	/**
	 * Bag datatype definition of this attribute value
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(TYPE_URI, true);

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<AnyURIAttributeValue> FACTORY = new AttributeValue.Factory<AnyURIAttributeValue>(AnyURIAttributeValue.class)
	{
		@Override
		public final String getId()
		{
			return TYPE_URI;
		}

		@Override
		public final AnyURIAttributeValue getInstance(AttributeValueType jaxbAttributeValue) throws IllegalArgumentException
		{
			return new AnyURIAttributeValue(jaxbAttributeValue);
		}
	};

	@Override
	protected String parse(String stringForm)
	{
		// validate as anyURI
		DatatypeDef.validateURI(stringForm);
		return stringForm;
	}

	// /**
	// * For testing only
	// * @param args
	// */
	// public static void main(String... args) {
	// String values[] = {"http://localhost.example.com:9090/path/to/something/somewhere/close",
	// "http://com.example.localhost:7171/close/to/somewhere/something/path"};
	// long best = -1;
	// for(int i=0; i< 10000; i++) {
	// long start = System.nanoTime();
	// String result = String.format(XML_FRAGMENT_FORMAT, values[i%2]);
	// long elapsed = System.nanoTime() - start;
	// if(best == -1 || elapsed < best) {
	// best = elapsed;
	// }
	// }
	//
	// System.out.println("Best time with String.format(): " + best + " ns");
	//
	// best = -1;
	// for(int i=0; i< 10000; i++) {
	// long start = System.nanoTime();
	// //String result = String.format("<xml-fragment>%s</xml-fragment>", value);
	// String result =XML_FRAGMENT_START+values[i%2]+XML_FRAGMENT_END;
	// long elapsed = System.nanoTime() - start;
	// if(best == -1 || elapsed < best) {
	// best = elapsed;
	// }
	// }
	//
	// System.out.println("Best time with String +: " + best + " ns");
	// }

}
