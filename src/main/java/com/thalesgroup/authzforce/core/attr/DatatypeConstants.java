package com.thalesgroup.authzforce.core.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.eval.BagDatatype;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Bag;

/**
 * XACML standard datatype constants, i.e. constants related to XACML standard datatypes
 * 
 * @param <AV>
 *            Concrete Java type actually implementing the datatype
 * 
 */
public final class DatatypeConstants<AV extends AttributeValue<AV>>
{
	/**
	 * string
	 */
	public static final DatatypeConstants<StringAttributeValue> STRING = new DatatypeConstants<>(StringAttributeValue.FACTORY, StringAttributeValue[].class, Function.FUNCTION_NS_1 + "string");

	/**
	 * boolean
	 */
	public static final DatatypeConstants<BooleanAttributeValue> BOOLEAN = new DatatypeConstants<>(BooleanAttributeValue.FACTORY, BooleanAttributeValue[].class, Function.FUNCTION_NS_1 + "boolean");

	/**
	 * integer
	 */
	public static final DatatypeConstants<IntegerAttributeValue> INTEGER = new DatatypeConstants<>(IntegerAttributeValue.FACTORY, IntegerAttributeValue[].class, Function.FUNCTION_NS_1 + "integer");

	/**
	 * double
	 */
	public static final DatatypeConstants<DoubleAttributeValue> DOUBLE = new DatatypeConstants<>(DoubleAttributeValue.FACTORY, DoubleAttributeValue[].class, Function.FUNCTION_NS_1 + "double");

	/**
	 * time
	 */
	public static final DatatypeConstants<TimeAttributeValue> TIME = new DatatypeConstants<>(TimeAttributeValue.FACTORY, TimeAttributeValue[].class, Function.FUNCTION_NS_1 + "time");

	/**
	 * date
	 */
	public static final DatatypeConstants<DateAttributeValue> DATE = new DatatypeConstants<>(DateAttributeValue.FACTORY, DateAttributeValue[].class, Function.FUNCTION_NS_1 + "date");

	/**
	 * dateTime
	 */
	public static final DatatypeConstants<DateTimeAttributeValue> DATETIME = new DatatypeConstants<>(DateTimeAttributeValue.FACTORY, DateTimeAttributeValue[].class, Function.FUNCTION_NS_1 + "dateTime");

	/**
	 * anyURI
	 */
	public static final DatatypeConstants<AnyURIAttributeValue> ANYURI = new DatatypeConstants<>(AnyURIAttributeValue.FACTORY, AnyURIAttributeValue[].class, Function.FUNCTION_NS_1 + "anyURI");
	/**
	 * hexBinary
	 */

	public static final DatatypeConstants<HexBinaryAttributeValue> HEXBINARY = new DatatypeConstants<>(HexBinaryAttributeValue.FACTORY, HexBinaryAttributeValue[].class, Function.FUNCTION_NS_1 + "hexBinary");

	/**
	 * base64Binary
	 */
	public static final DatatypeConstants<Base64BinaryAttributeValue> BASE64BINARY = new DatatypeConstants<>(Base64BinaryAttributeValue.FACTORY, Base64BinaryAttributeValue[].class, Function.FUNCTION_NS_1 + "base64Binary");

	/**
	 * x500Name
	 */
	public static final DatatypeConstants<X500NameAttributeValue> X500NAME = new DatatypeConstants<>(X500NameAttributeValue.FACTORY, X500NameAttributeValue[].class, Function.FUNCTION_NS_1 + "x500Name");

	/**
	 * rfc822Name
	 */
	public static final DatatypeConstants<RFC822NameAttributeValue> RFC822NAME = new DatatypeConstants<>(RFC822NameAttributeValue.FACTORY, RFC822NameAttributeValue[].class, Function.FUNCTION_NS_1 + "rfc822Name");

	/**
	 * ipAddress
	 */
	public static final DatatypeConstants<IPAddressAttributeValue> IPADDRESS = new DatatypeConstants<>(IPAddressAttributeValue.FACTORY, IPAddressAttributeValue[].class, Function.FUNCTION_NS_2 + "ipAddress");

	/**
	 * dnsName
	 */
	public static final DatatypeConstants<DNSNameAttributeValue> DNSNAME = new DatatypeConstants<>(DNSNameAttributeValue.FACTORY, DNSNameAttributeValue[].class, Function.FUNCTION_NS_2 + "dnsName");

	/**
	 * dayTimeDuration
	 */
	public static final DatatypeConstants<DayTimeDurationAttributeValue> DAYTIMEDURATION = new DatatypeConstants<>(DayTimeDurationAttributeValue.FACTORY, DayTimeDurationAttributeValue[].class, Function.FUNCTION_NS_3 + "dayTimeDuration");

	/**
	 * yearMonthDuration
	 */
	public static final DatatypeConstants<YearMonthDurationAttributeValue> YEARMONTHDURATION = new DatatypeConstants<>(YearMonthDurationAttributeValue.FACTORY, YearMonthDurationAttributeValue[].class, Function.FUNCTION_NS_3 + "yearMonthDuration");

	/**
	 * xpathExpression
	 */
	public static final DatatypeConstants<XPathAttributeValue> XPATH = new DatatypeConstants<>(XPathAttributeValue.FACTORY, XPathAttributeValue[].class, Function.FUNCTION_NS_3 + "xpath");

	/**
	 * Standard datatype constants
	 */
	public static Set<DatatypeConstants<? extends SimpleAttributeValue<?, ?>>> SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(STRING, BOOLEAN, INTEGER, DOUBLE, TIME, DATE, DATETIME, ANYURI, HEXBINARY, BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME, DAYTIMEDURATION,
			YEARMONTHDURATION, XPATH)));

	/**
	 * (Primitive) Datatype
	 */
	public final Datatype<AV> TYPE;

	/**
	 * Empty bag
	 */
	public final Bag<AV> EMPTY_BAG;

	/**
	 * Bag datatype based on the primitive datatype {@link #TYPE}
	 */
	public final BagDatatype<AV> BAG_TYPE;

	/**
	 * class of array of instances of this datatype
	 */
	public final Class<AV[]> ARRAY_CLASS;

	/**
	 * Prefix of URI of standard functions taking parameters of this datatype (and this datatype
	 * only)
	 */
	public final String FUNCTION_ID_PREFIX;

	private DatatypeConstants(AttributeValue.Factory<AV> valueFactory, Class<AV[]> valueArrayClass, String functionIdPrefix)
	{
		this.TYPE = valueFactory.getDatatype();
		this.BAG_TYPE = valueFactory.getBagDatatype();
		this.EMPTY_BAG = valueFactory.getEmptyBag();
		this.ARRAY_CLASS = valueArrayClass;
		this.FUNCTION_ID_PREFIX = functionIdPrefix;
	}
}
