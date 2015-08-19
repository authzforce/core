/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.func;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

@RunWith(Parameterized.class)
public class BagFunctionsTest extends GeneralFunctionTest
{

	private static final String NAME_STRING_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
	private static final String NAME_BOOLEAN_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only";
	private static final String NAME_INTEGER_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
	private static final String NAME_DOUBLE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:double-one-and-only";
	private static final String NAME_TIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:time-one-and-only";
	private static final String NAME_DATE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:date-one-and-only";
	private static final String NAME_DATETIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only";
	private static final String NAME_ANYURI_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only";
	private static final String NAME_HEXBINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only";
	private static final String NAME_BASE64BINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only";
	private static final String NAME_DAYTIMEDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only";
	private static final String NAME_YEARMONTHDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only";
	private static final String NAME_X500NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only";
	private static final String NAME_RFC822NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only";
	private static final String NAME_IPADDRESS_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only";
	private static final String NAME_DNSNAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only";
	private static final String NAME_STRING_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:string-bag-size";
	private static final String NAME_BOOLEAN_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size";
	private static final String NAME_INTEGER_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:integer-bag-size";
	private static final String NAME_DOUBLE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:double-bag-size";
	private static final String NAME_TIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:time-bag-size";
	private static final String NAME_DATE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:date-bag-size";
	private static final String NAME_DATETIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size";
	private static final String NAME_ANYURI_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size";
	private static final String NAME_HEXBINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size";
	private static final String NAME_BASE64BINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size";
	private static final String NAME_DAYTIMEDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size";
	private static final String NAME_YEARMONTHDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size";
	private static final String NAME_X500NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size";
	private static final String NAME_RFC822NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size";
	private static final String NAME_IPADDRESS_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size";
	private static final String NAME_DNSNAME_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size";
	private static final String NAME_STRING_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
	private static final String NAME_BOOLEAN_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:boolean-is-in";
	private static final String NAME_INTEGER_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:integer-is-in";
	private static final String NAME_DOUBLE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:double-is-in";
	private static final String NAME_TIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:time-is-in";
	private static final String NAME_DATE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:date-is-in";
	private static final String NAME_DATETIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in";
	private static final String NAME_ANYURI_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in";
	private static final String NAME_HEXBINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in";
	private static final String NAME_BASE64BINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in";
	private static final String NAME_DAYTIMEDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in";
	private static final String NAME_YEARMONTHDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in";
	private static final String NAME_X500NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in";
	private static final String NAME_RFC822NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in";
	/*
	 * The two following functions are not officially listed in conformance table of XACML 3.0 core
	 * specification. However, we consider they should be there like for the other types.
	 */
	private static final String NAME_IPADDRESS_IS_IN = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in";
	private static final String NAME_DNSNAME_IS_IN = "urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in";

	private static final String NAME_STRING_BAG = "urn:oasis:names:tc:xacml:1.0:function:string-bag";
	private static final String NAME_BOOLEAN_BAG = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag";
	private static final String NAME_INTEGER_BAG = "urn:oasis:names:tc:xacml:1.0:function:integer-bag";
	private static final String NAME_DOUBLE_BAG = "urn:oasis:names:tc:xacml:1.0:function:double-bag";
	private static final String NAME_TIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:time-bag";
	private static final String NAME_DATE_BAG = "urn:oasis:names:tc:xacml:1.0:function:date-bag";
	private static final String NAME_DATETIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag";
	private static final String NAME_ANYURI_BAG = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag";
	private static final String NAME_HEXBINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag";
	private static final String NAME_BASE64BINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag";
	private static final String NAME_DAYTIMEDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag";
	private static final String NAME_YEARMONTHDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag";
	private static final String NAME_X500NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag";
	private static final String NAME_RFC822NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag";
	private static final String NAME_IPADDRESS_BAG = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag";
	private static final String NAME_DNSNAME_BAG = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag";

	/**
	 * *-one-and-only function test parameters. For each, we test with a valid one-value bag
	 * parameter, then with an invalid parameter that is an empty bag, then an invalid parameter
	 * that is a two-value bag.
	 */
	private static <T extends AttributeValue> Collection<Object[]> newOneAndOnlyFunctionTestParams(String oneAndOnlyFunctionId, Class<T[]> typeArrayClass, String typeURI, T primitiveValue)
	{
		DatatypeDef bagType = new DatatypeDef(typeURI, true);
		DatatypeDef primitiveType = new DatatypeDef(typeURI, false);
		Collection<Object[]> params = new ArrayList<>();
		Class<T> typeClass = (Class<T>) typeArrayClass.getComponentType();

		// one-and-only({}) -> Indeterminate
		T[] emptyArray = (T[]) Array.newInstance(typeClass, 0);

		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(new BagResult<>(emptyArray, typeClass, bagType)), null });

		// one-and-only({val}) -> val
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(new BagResult<>(primitiveValue, typeClass, bagType)), primitiveValue });

		// one-and-only({val, val}) -> Indeterminate
		T[] twoValArray = (T[]) Array.newInstance(typeClass, 2);
		Arrays.fill(twoValArray, primitiveValue);
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(new BagResult<>(twoValArray, typeClass, bagType)), null });

		return params;
	}

	private static final IntegerAttributeValue zeroIntValue = new IntegerAttributeValue("0");
	private static final IntegerAttributeValue oneIntValue = new IntegerAttributeValue("1");
	private static final IntegerAttributeValue twoIntValue = new IntegerAttributeValue("2");

	/**
	 * *-bag-size function test parameters. For each, we test with an empty bag parameter, then with
	 * an one-value bag, then a two-value bag.
	 */
	private static <T extends AttributeValue> Collection<Object[]> newBagSizeFunctionTestParams(String bagSizeFunctionId, Class<T[]> typeArrayClass, String typeURI, T primitiveValue)
	{
		DatatypeDef bagType = new DatatypeDef(typeURI, true);
		DatatypeDef primitiveType = new DatatypeDef(typeURI, false);
		Collection<Object[]> params = new ArrayList<>();
		Class<T> typeClass = (Class<T>) typeArrayClass.getComponentType();

		// bag-size({}) -> 0
		T[] emptyArray = (T[]) Array.newInstance(typeClass, 0);
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(new BagResult<>(emptyArray, typeClass, bagType)), zeroIntValue });

		// bag-size({val}) -> 1
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(new BagResult<>(primitiveValue, typeClass, bagType)), oneIntValue });

		// bag-size({val, val}) -> 2
		T[] twoValArray = (T[]) Array.newInstance(typeClass, 2);
		Arrays.fill(twoValArray, primitiveValue);
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(new BagResult<>(twoValArray, typeClass, bagType)), twoIntValue });
		return params;
	}

	/**
	 * *-is-in function test parameters. We want to test the following cases: 1) the first argument
	 * is not in the empty bag as second argument, 2) the first argument is in the non-empty bag as
	 * second argument, 3) the first argument is not in the non-empty bag as second argument.
	 * <p>
	 * Parameters primitiveValue1 and primitiveValue2 MUST be different values.
	 */
	private static <T extends AttributeValue> Collection<Object[]> newIsInFunctionTestParams(String isInFunctionId, Class<T[]> typeArrayClass, String typeURI, T primitiveValue1, T primitiveValue2)
	{
		DatatypeDef bagType = new DatatypeDef(typeURI, true);
		DatatypeDef primitiveType = new DatatypeDef(typeURI, false);
		Collection<Object[]> params = new ArrayList<>();
		Class<T> typeClass = (Class<T>) typeArrayClass.getComponentType();

		// is-in(val, {}) -> false
		T[] emptyArray = (T[]) Array.newInstance(typeClass, 0);
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue1, new BagResult<>(emptyArray, typeClass, bagType)), BooleanAttributeValue.FALSE });

		// is-in(val2, {val1, val2}) -> true
		T[] twoValArray = (T[]) Array.newInstance(typeClass, 2);
		twoValArray[0] = primitiveValue1;
		twoValArray[1] = primitiveValue2;
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, new BagResult<>(twoValArray, typeClass, bagType)), BooleanAttributeValue.TRUE });

		// is-in(val2, {val1, val1}) -> false
		twoValArray[1] = primitiveValue1;
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, new BagResult<>(twoValArray, typeClass, bagType)), BooleanAttributeValue.FALSE });
		return params;
	}

	/**
	 * *-bag function test parameters.
	 */
	private static <T extends AttributeValue> Collection<Object[]> newBagOfFunctionTestParams(String bagOfFunctionId, Class<T[]> typeArrayClass, String typeURI, T primitiveValue1, T primitiveValue2)
	{
		DatatypeDef bagType = new DatatypeDef(typeURI, true);
		Collection<Object[]> params = new ArrayList<>();
		Class<T> typeClass = (Class<T>) typeArrayClass.getComponentType();

		// is-in(val2, {val1, val2}) -> true
		T[] twoValArray = (T[]) Array.newInstance(typeClass, 2);
		twoValArray[0] = primitiveValue1;
		twoValArray[1] = primitiveValue2;
		params.add(new Object[] { bagOfFunctionId, Arrays.asList(primitiveValue1, primitiveValue2), new BagResult<>(twoValArray, typeClass, bagType) });

		return params;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		Collection<Object[]> params = new ArrayList<>();

		// *-one-and-only functions
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_STRING_ONE_AND_ONLY, StringAttributeValue[].class, StringAttributeValue.TYPE_URI, new StringAttributeValue("Test")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BOOLEAN_ONE_AND_ONLY, BooleanAttributeValue[].class, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue.FALSE));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_INTEGER_ONE_AND_ONLY, IntegerAttributeValue[].class, IntegerAttributeValue.TYPE_URI, new IntegerAttributeValue("3")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DOUBLE_ONE_AND_ONLY, DoubleAttributeValue[].class, DoubleAttributeValue.TYPE_URI, new DoubleAttributeValue("3.14")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_ANYURI_ONE_AND_ONLY, AnyURIAttributeValue[].class, AnyURIAttributeValue.TYPE_URI, new AnyURIAttributeValue("http://www.example.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_HEXBINARY_ONE_AND_ONLY, HexBinaryAttributeValue[].class, HexBinaryAttributeValue.TYPE_URI, new HexBinaryAttributeValue("0FB7")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BASE64BINARY_ONE_AND_ONLY, Base64BinaryAttributeValue[].class, Base64BinaryAttributeValue.TYPE_URI, new Base64BinaryAttributeValue("RXhhbXBsZQ==")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_TIME_ONE_AND_ONLY, TimeAttributeValue[].class, TimeAttributeValue.TYPE_URI, new TimeAttributeValue("09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATE_ONE_AND_ONLY, DateAttributeValue[].class, DateAttributeValue.TYPE_URI, new DateAttributeValue("2002-09-24")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATETIME_ONE_AND_ONLY, DateTimeAttributeValue[].class, DateTimeAttributeValue.TYPE_URI, new DateTimeAttributeValue("2002-09-24T09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DAYTIMEDURATION_ONE_AND_ONLY, DayTimeDurationAttributeValue[].class, DayTimeDurationAttributeValue.TYPE_URI, new DayTimeDurationAttributeValue("P1DT2H")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_YEARMONTHDURATION_ONE_AND_ONLY, YearMonthDurationAttributeValue[].class, YearMonthDurationAttributeValue.TYPE_URI, new YearMonthDurationAttributeValue("P1Y2M")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_X500NAME_ONE_AND_ONLY, X500NameAttributeValue[].class, X500NameAttributeValue.TYPE_URI, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_RFC822NAME_ONE_AND_ONLY, RFC822NameAttributeValue[].class, RFC822NameAttributeValue.TYPE_URI, new RFC822NameAttributeValue("Anderson@sun.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_IPADDRESS_ONE_AND_ONLY, IPAddressAttributeValue[].class, IPAddressAttributeValue.identifier, new IPAddressAttributeValue("192.168.1.10")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DNSNAME_ONE_AND_ONLY, DNSNameAttributeValue[].class, DNSNameAttributeValue.identifier, new DNSNameAttributeValue("example.com")));

		// *-bag-size functions
		params.addAll(newBagSizeFunctionTestParams(NAME_STRING_BAG_SIZE, StringAttributeValue[].class, StringAttributeValue.TYPE_URI, new StringAttributeValue("Test")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BOOLEAN_BAG_SIZE, BooleanAttributeValue[].class, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue.FALSE));
		params.addAll(newBagSizeFunctionTestParams(NAME_INTEGER_BAG_SIZE, IntegerAttributeValue[].class, IntegerAttributeValue.TYPE_URI, new IntegerAttributeValue("1")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DOUBLE_BAG_SIZE, DoubleAttributeValue[].class, DoubleAttributeValue.TYPE_URI, new DoubleAttributeValue("3.14")));
		params.addAll(newBagSizeFunctionTestParams(NAME_ANYURI_BAG_SIZE, AnyURIAttributeValue[].class, AnyURIAttributeValue.TYPE_URI, new AnyURIAttributeValue("http://www.example.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_HEXBINARY_BAG_SIZE, HexBinaryAttributeValue[].class, HexBinaryAttributeValue.TYPE_URI, new HexBinaryAttributeValue("0FB7")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BASE64BINARY_BAG_SIZE, Base64BinaryAttributeValue[].class, Base64BinaryAttributeValue.TYPE_URI, new Base64BinaryAttributeValue("RXhhbXBsZQ==")));
		params.addAll(newBagSizeFunctionTestParams(NAME_TIME_BAG_SIZE, TimeAttributeValue[].class, TimeAttributeValue.TYPE_URI, new TimeAttributeValue("09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATE_BAG_SIZE, DateAttributeValue[].class, DateAttributeValue.TYPE_URI, new DateAttributeValue("2002-09-24")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATETIME_BAG_SIZE, DateTimeAttributeValue[].class, DateTimeAttributeValue.TYPE_URI, new DateTimeAttributeValue("2002-09-24T09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DAYTIMEDURATION_BAG_SIZE, DayTimeDurationAttributeValue[].class, DayTimeDurationAttributeValue.TYPE_URI, new DayTimeDurationAttributeValue("P1DT2H")));
		params.addAll(newBagSizeFunctionTestParams(NAME_YEARMONTHDURATION_BAG_SIZE, YearMonthDurationAttributeValue[].class, YearMonthDurationAttributeValue.TYPE_URI, new YearMonthDurationAttributeValue("P1Y2M")));
		params.addAll(newBagSizeFunctionTestParams(NAME_X500NAME_BAG_SIZE, X500NameAttributeValue[].class, X500NameAttributeValue.TYPE_URI, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newBagSizeFunctionTestParams(NAME_RFC822NAME_BAG_SIZE, RFC822NameAttributeValue[].class, RFC822NameAttributeValue.TYPE_URI, new RFC822NameAttributeValue("Anderson@sun.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_IPADDRESS_BAG_SIZE, IPAddressAttributeValue[].class, IPAddressAttributeValue.identifier, new IPAddressAttributeValue("192.168.1.10")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DNSNAME_BAG_SIZE, DNSNameAttributeValue[].class, DNSNameAttributeValue.identifier, new DNSNameAttributeValue("example.com")));

		// *-is-in functions
		params.addAll(newIsInFunctionTestParams(NAME_STRING_IS_IN, StringAttributeValue[].class, StringAttributeValue.TYPE_URI, new StringAttributeValue("Test1"), new StringAttributeValue("Test2")));
		params.addAll(newIsInFunctionTestParams(NAME_BOOLEAN_IS_IN, BooleanAttributeValue[].class, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE));
		params.addAll(newIsInFunctionTestParams(NAME_INTEGER_IS_IN, IntegerAttributeValue[].class, IntegerAttributeValue.TYPE_URI, new IntegerAttributeValue("1"), new IntegerAttributeValue("2")));
		params.addAll(newIsInFunctionTestParams(NAME_DOUBLE_IS_IN, DoubleAttributeValue[].class, DoubleAttributeValue.TYPE_URI, new DoubleAttributeValue("-4.21"), new DoubleAttributeValue("3.14")));
		params.addAll(newIsInFunctionTestParams(NAME_ANYURI_IS_IN, AnyURIAttributeValue[].class, AnyURIAttributeValue.TYPE_URI, new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example1.com")));
		params.addAll(newIsInFunctionTestParams(NAME_HEXBINARY_IS_IN, HexBinaryAttributeValue[].class, HexBinaryAttributeValue.TYPE_URI, new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")));
		params.addAll(newIsInFunctionTestParams(NAME_BASE64BINARY_IS_IN, Base64BinaryAttributeValue[].class, Base64BinaryAttributeValue.TYPE_URI, new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")));
		params.addAll(newIsInFunctionTestParams(NAME_TIME_IS_IN, TimeAttributeValue[].class, TimeAttributeValue.TYPE_URI, new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DATE_IS_IN, DateAttributeValue[].class, DateAttributeValue.TYPE_URI, new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")));
		params.addAll(newIsInFunctionTestParams(NAME_DATETIME_IS_IN, DateTimeAttributeValue[].class, DateTimeAttributeValue.TYPE_URI, new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DAYTIMEDURATION_IS_IN, DayTimeDurationAttributeValue[].class, DayTimeDurationAttributeValue.TYPE_URI, new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P1DT3H")));
		params.addAll(newIsInFunctionTestParams(NAME_YEARMONTHDURATION_IS_IN, YearMonthDurationAttributeValue[].class, YearMonthDurationAttributeValue.TYPE_URI, new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P1Y3M")));
		params.addAll(newIsInFunctionTestParams(NAME_X500NAME_IS_IN, X500NameAttributeValue[].class, X500NameAttributeValue.TYPE_URI, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newIsInFunctionTestParams(NAME_RFC822NAME_IS_IN, RFC822NameAttributeValue[].class, RFC822NameAttributeValue.TYPE_URI, new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")));
		params.addAll(newIsInFunctionTestParams(NAME_IPADDRESS_IS_IN, IPAddressAttributeValue[].class, IPAddressAttributeValue.identifier, new IPAddressAttributeValue("192.168.1.10"), new IPAddressAttributeValue("192.168.1.11")));
		params.addAll(newIsInFunctionTestParams(NAME_DNSNAME_IS_IN, DNSNameAttributeValue[].class, DNSNameAttributeValue.identifier, new DNSNameAttributeValue("example.com"), new DNSNameAttributeValue("example1.com")));

		// *-bag functions
		params.addAll(newBagOfFunctionTestParams(NAME_STRING_BAG, StringAttributeValue[].class, StringAttributeValue.TYPE_URI, new StringAttributeValue("Test1"), new StringAttributeValue("Test2")));
		params.addAll(newBagOfFunctionTestParams(NAME_BOOLEAN_BAG, BooleanAttributeValue[].class, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE));
		params.addAll(newBagOfFunctionTestParams(NAME_INTEGER_BAG, IntegerAttributeValue[].class, IntegerAttributeValue.TYPE_URI, new IntegerAttributeValue("1"), new IntegerAttributeValue("2")));
		params.addAll(newBagOfFunctionTestParams(NAME_DOUBLE_BAG, DoubleAttributeValue[].class, DoubleAttributeValue.TYPE_URI, new DoubleAttributeValue("-4.21"), new DoubleAttributeValue("3.14")));
		params.addAll(newBagOfFunctionTestParams(NAME_ANYURI_BAG, AnyURIAttributeValue[].class, AnyURIAttributeValue.TYPE_URI, new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example1.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_HEXBINARY_BAG, HexBinaryAttributeValue[].class, HexBinaryAttributeValue.TYPE_URI, new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")));
		params.addAll(newBagOfFunctionTestParams(NAME_BASE64BINARY_BAG, Base64BinaryAttributeValue[].class, Base64BinaryAttributeValue.TYPE_URI, new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")));
		params.addAll(newBagOfFunctionTestParams(NAME_TIME_BAG, TimeAttributeValue[].class, TimeAttributeValue.TYPE_URI, new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATE_BAG, DateAttributeValue[].class, DateAttributeValue.TYPE_URI, new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATETIME_BAG, DateTimeAttributeValue[].class, DateTimeAttributeValue.TYPE_URI, new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DAYTIMEDURATION_BAG, DayTimeDurationAttributeValue[].class, DayTimeDurationAttributeValue.TYPE_URI, new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P1DT3H")));
		params.addAll(newBagOfFunctionTestParams(NAME_YEARMONTHDURATION_BAG, YearMonthDurationAttributeValue[].class, YearMonthDurationAttributeValue.TYPE_URI, new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P1Y3M")));
		params.addAll(newBagOfFunctionTestParams(NAME_X500NAME_BAG, X500NameAttributeValue[].class, X500NameAttributeValue.TYPE_URI, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newBagOfFunctionTestParams(NAME_RFC822NAME_BAG, RFC822NameAttributeValue[].class, RFC822NameAttributeValue.TYPE_URI, new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_IPADDRESS_BAG, IPAddressAttributeValue[].class, IPAddressAttributeValue.identifier, new IPAddressAttributeValue("192.168.1.10"), new IPAddressAttributeValue("192.168.1.11")));
		params.addAll(newBagOfFunctionTestParams(NAME_DNSNAME_BAG, DNSNameAttributeValue[].class, DNSNameAttributeValue.identifier, new DNSNameAttributeValue("example.com"), new DNSNameAttributeValue("example1.com")));

		return params;
	}

	public BagFunctionsTest(String functionName, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs, ExpressionResult<? extends AttributeValue> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

}
