/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.test.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DNSNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.api.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class BagFunctionsTest extends FunctionTest
{

	public BagFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

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
	 * The two following functions are not officially listed in conformance table of XACML 3.0 core specification. However, we consider they should be there like for the other types.
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
	 * *-one-and-only function test parameters. For each, we test with a valid one-value bag parameter, then with an invalid parameter that is an empty bag, then an invalid parameter that is a
	 * two-value bag.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newOneAndOnlyFunctionTestParams(String oneAndOnlyFunctionId, DatatypeFactory<AV> typeFactory, AV primitiveValue)
	{
		Collection<Object[]> params = new ArrayList<>();

		// case 1: empty bag {}
		// one-and-only({}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(typeFactory.getEmptyBag()), null });

		// one-and-only({primitiveValue}) -> primitiveValue
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(Bags.singleton(typeFactory.getDatatype(), primitiveValue)), primitiveValue });

		// one-and-only({primitiveValue, primitiveValue}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(Bags.getInstance(typeFactory.getDatatype(), Collections.nCopies(2, primitiveValue))), null });

		return params;
	}

	private static final IntegerValue ZERO_AS_INT = new IntegerValue("0");
	private static final IntegerValue ONE_AS_INT = new IntegerValue("1");
	private static final IntegerValue TWO_AS_INT = new IntegerValue("2");

	/**
	 * *-bag-size function test parameters. For each, we test with an empty bag parameter, then with an one-value bag, then a two-value bag.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newBagSizeFunctionTestParams(String bagSizeFunctionId, DatatypeFactory<AV> typeFactory, AV primitiveValue)
	{
		Collection<Object[]> params = new ArrayList<>();

		// bag-size({}) -> 0
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(typeFactory.getEmptyBag()), ZERO_AS_INT });

		// bag-size({primitiveValue}) -> 1
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(Bags.singleton(typeFactory.getDatatype(), primitiveValue)), ONE_AS_INT });

		// bag-size({primitiveValue, primitiveValue}) -> 2
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(Bags.getInstance(typeFactory.getDatatype(), Collections.nCopies(2, primitiveValue))), TWO_AS_INT });
		return params;
	}

	/**
	 * *-is-in function test parameters. We want to test the following cases: 1) the first argument is not in the empty bag as second argument, 2) the first argument is in the non-empty bag as second
	 * argument, 3) the first argument is not in the non-empty bag as second argument.
	 * <p>
	 * Parameters primitiveValue1 and primitiveValue2 MUST be different values.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newIsInFunctionTestParams(String isInFunctionId, DatatypeFactory<AV> typeFactory, AV primitiveValue1, AV primitiveValue2)
	{
		Collection<Object[]> params = new ArrayList<>();

		// is-in(val, {}) -> false
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue1, typeFactory.getEmptyBag()), BooleanValue.FALSE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue2}) -> true
		Bag<AV> twoValBag = Bags.getInstance(typeFactory.getDatatype(), Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag), BooleanValue.TRUE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue1}) -> false
		Bag<AV> twoValBag2 = Bags.getInstance(typeFactory.getDatatype(), Collections.nCopies(2, primitiveValue1));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag2), BooleanValue.FALSE });
		return params;
	}

	/**
	 * *-bag function test parameters.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newBagOfFunctionTestParams(String bagOfFunctionId, DatatypeFactory<AV> typeFactory, AV primitiveValue1, AV primitiveValue2)
	{
		Collection<Object[]> params = new ArrayList<>();

		// bag(primitiveValue1, primitiveValue2) -> {primitiveValue1, primitiveValue2}
		Bag<AV> twoValBag = Bags.getInstance(typeFactory.getDatatype(), Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { bagOfFunctionId, Arrays.asList(primitiveValue1, primitiveValue2), twoValBag });
		return params;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		Collection<Object[]> params = new ArrayList<>();

		// *-one-and-only functions
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_STRING_ONE_AND_ONLY, StandardDatatypes.STRING_FACTORY, new StringValue("Test")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BOOLEAN_ONE_AND_ONLY, StandardDatatypes.BOOLEAN_FACTORY, BooleanValue.FALSE));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_INTEGER_ONE_AND_ONLY, StandardDatatypes.INTEGER_FACTORY, new IntegerValue("3")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DOUBLE_ONE_AND_ONLY, StandardDatatypes.DOUBLE_FACTORY, new DoubleValue("3.14")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_ANYURI_ONE_AND_ONLY, StandardDatatypes.ANYURI_FACTORY, new AnyURIValue("http://www.example.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_HEXBINARY_ONE_AND_ONLY, StandardDatatypes.HEXBINARY_FACTORY, new HexBinaryValue("0FB7")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BASE64BINARY_ONE_AND_ONLY, StandardDatatypes.BASE64BINARY_FACTORY, new Base64BinaryValue("RXhhbXBsZQ==")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_TIME_ONE_AND_ONLY, StandardDatatypes.TIME_FACTORY, new TimeValue("09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATE_ONE_AND_ONLY, StandardDatatypes.DATE_FACTORY, new DateValue("2002-09-24")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATETIME_ONE_AND_ONLY, StandardDatatypes.DATETIME_FACTORY, new DateTimeValue("2002-09-24T09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DAYTIMEDURATION_ONE_AND_ONLY, StandardDatatypes.DAYTIMEDURATION_FACTORY, new DayTimeDurationValue("P1DT2H")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_YEARMONTHDURATION_ONE_AND_ONLY, StandardDatatypes.YEARMONTHDURATION_FACTORY, new YearMonthDurationValue("P1Y2M")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_X500NAME_ONE_AND_ONLY, StandardDatatypes.X500NAME_FACTORY, new X500NameValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_RFC822NAME_ONE_AND_ONLY, StandardDatatypes.RFC822NAME_FACTORY, new RFC822NameValue("Anderson@sun.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_IPADDRESS_ONE_AND_ONLY, StandardDatatypes.IPADDRESS_FACTORY, new IPAddressValue("192.168.1.10")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DNSNAME_ONE_AND_ONLY, StandardDatatypes.DNSNAME_FACTORY, new DNSNameWithPortRangeValue("example.com")));

		// *-bag-size functions
		params.addAll(newBagSizeFunctionTestParams(NAME_STRING_BAG_SIZE, StandardDatatypes.STRING_FACTORY, new StringValue("Test")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BOOLEAN_BAG_SIZE, StandardDatatypes.BOOLEAN_FACTORY, BooleanValue.FALSE));
		params.addAll(newBagSizeFunctionTestParams(NAME_INTEGER_BAG_SIZE, StandardDatatypes.INTEGER_FACTORY, new IntegerValue("1")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DOUBLE_BAG_SIZE, StandardDatatypes.DOUBLE_FACTORY, new DoubleValue("3.14")));
		params.addAll(newBagSizeFunctionTestParams(NAME_ANYURI_BAG_SIZE, StandardDatatypes.ANYURI_FACTORY, new AnyURIValue("http://www.example.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_HEXBINARY_BAG_SIZE, StandardDatatypes.HEXBINARY_FACTORY, new HexBinaryValue("0FB7")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BASE64BINARY_BAG_SIZE, StandardDatatypes.BASE64BINARY_FACTORY, new Base64BinaryValue("RXhhbXBsZQ==")));
		params.addAll(newBagSizeFunctionTestParams(NAME_TIME_BAG_SIZE, StandardDatatypes.TIME_FACTORY, new TimeValue("09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATE_BAG_SIZE, StandardDatatypes.DATE_FACTORY, new DateValue("2002-09-24")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATETIME_BAG_SIZE, StandardDatatypes.DATETIME_FACTORY, new DateTimeValue("2002-09-24T09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DAYTIMEDURATION_BAG_SIZE, StandardDatatypes.DAYTIMEDURATION_FACTORY, new DayTimeDurationValue("P1DT2H")));
		params.addAll(newBagSizeFunctionTestParams(NAME_YEARMONTHDURATION_BAG_SIZE, StandardDatatypes.YEARMONTHDURATION_FACTORY, new YearMonthDurationValue("P1Y2M")));
		params.addAll(newBagSizeFunctionTestParams(NAME_X500NAME_BAG_SIZE, StandardDatatypes.X500NAME_FACTORY, new X500NameValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newBagSizeFunctionTestParams(NAME_RFC822NAME_BAG_SIZE, StandardDatatypes.RFC822NAME_FACTORY, new RFC822NameValue("Anderson@sun.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_IPADDRESS_BAG_SIZE, StandardDatatypes.IPADDRESS_FACTORY, new IPAddressValue("192.168.1.10")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DNSNAME_BAG_SIZE, StandardDatatypes.DNSNAME_FACTORY, new DNSNameWithPortRangeValue("example.com")));

		// *-is-in functions
		params.addAll(newIsInFunctionTestParams(NAME_STRING_IS_IN, StandardDatatypes.STRING_FACTORY, new StringValue("Test1"), new StringValue("Test2")));
		params.addAll(newIsInFunctionTestParams(NAME_BOOLEAN_IS_IN, StandardDatatypes.BOOLEAN_FACTORY, BooleanValue.FALSE, BooleanValue.TRUE));
		params.addAll(newIsInFunctionTestParams(NAME_INTEGER_IS_IN, StandardDatatypes.INTEGER_FACTORY, new IntegerValue("1"), new IntegerValue("2")));
		params.addAll(newIsInFunctionTestParams(NAME_DOUBLE_IS_IN, StandardDatatypes.DOUBLE_FACTORY, new DoubleValue("-4.21"), new DoubleValue("3.14")));
		params.addAll(newIsInFunctionTestParams(NAME_ANYURI_IS_IN, StandardDatatypes.ANYURI_FACTORY, new AnyURIValue("http://www.example.com"), new AnyURIValue("http://www.example1.com")));
		params.addAll(newIsInFunctionTestParams(NAME_HEXBINARY_IS_IN, StandardDatatypes.HEXBINARY_FACTORY, new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")));
		params.addAll(newIsInFunctionTestParams(NAME_BASE64BINARY_IS_IN, StandardDatatypes.BASE64BINARY_FACTORY, new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")));
		params.addAll(newIsInFunctionTestParams(NAME_TIME_IS_IN, StandardDatatypes.TIME_FACTORY, new TimeValue("09:30:15"), new TimeValue("17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DATE_IS_IN, StandardDatatypes.DATE_FACTORY, new DateValue("2002-09-24"), new DateValue("2003-10-25")));
		params.addAll(newIsInFunctionTestParams(NAME_DATETIME_IS_IN, StandardDatatypes.DATETIME_FACTORY, new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DAYTIMEDURATION_IS_IN, StandardDatatypes.DAYTIMEDURATION_FACTORY, new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P1DT3H")));
		params.addAll(newIsInFunctionTestParams(NAME_YEARMONTHDURATION_IS_IN, StandardDatatypes.YEARMONTHDURATION_FACTORY, new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P1Y3M")));
		params.addAll(newIsInFunctionTestParams(NAME_X500NAME_IS_IN, StandardDatatypes.X500NAME_FACTORY, new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue(
				"cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newIsInFunctionTestParams(NAME_RFC822NAME_IS_IN, StandardDatatypes.RFC822NAME_FACTORY, new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")));
		params.addAll(newIsInFunctionTestParams(NAME_IPADDRESS_IS_IN, StandardDatatypes.IPADDRESS_FACTORY, new IPAddressValue("192.168.1.10"), new IPAddressValue("192.168.1.11")));
		params.addAll(newIsInFunctionTestParams(NAME_DNSNAME_IS_IN, StandardDatatypes.DNSNAME_FACTORY, new DNSNameWithPortRangeValue("example.com"), new DNSNameWithPortRangeValue("example1.com")));

		// *-bag functions
		params.addAll(newBagOfFunctionTestParams(NAME_STRING_BAG, StandardDatatypes.STRING_FACTORY, new StringValue("Test1"), new StringValue("Test2")));
		params.addAll(newBagOfFunctionTestParams(NAME_BOOLEAN_BAG, StandardDatatypes.BOOLEAN_FACTORY, BooleanValue.FALSE, BooleanValue.TRUE));
		params.addAll(newBagOfFunctionTestParams(NAME_INTEGER_BAG, StandardDatatypes.INTEGER_FACTORY, new IntegerValue("1"), new IntegerValue("2")));
		params.addAll(newBagOfFunctionTestParams(NAME_DOUBLE_BAG, StandardDatatypes.DOUBLE_FACTORY, new DoubleValue("-4.21"), new DoubleValue("3.14")));
		params.addAll(newBagOfFunctionTestParams(NAME_ANYURI_BAG, StandardDatatypes.ANYURI_FACTORY, new AnyURIValue("http://www.example.com"), new AnyURIValue("http://www.example1.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_HEXBINARY_BAG, StandardDatatypes.HEXBINARY_FACTORY, new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")));
		params.addAll(newBagOfFunctionTestParams(NAME_BASE64BINARY_BAG, StandardDatatypes.BASE64BINARY_FACTORY, new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")));
		params.addAll(newBagOfFunctionTestParams(NAME_TIME_BAG, StandardDatatypes.TIME_FACTORY, new TimeValue("09:30:15"), new TimeValue("17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATE_BAG, StandardDatatypes.DATE_FACTORY, new DateValue("2002-09-24"), new DateValue("2003-10-25")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATETIME_BAG, StandardDatatypes.DATETIME_FACTORY, new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DAYTIMEDURATION_BAG, StandardDatatypes.DAYTIMEDURATION_FACTORY, new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P1DT3H")));
		params.addAll(newBagOfFunctionTestParams(NAME_YEARMONTHDURATION_BAG, StandardDatatypes.YEARMONTHDURATION_FACTORY, new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P1Y3M")));
		params.addAll(newBagOfFunctionTestParams(NAME_X500NAME_BAG, StandardDatatypes.X500NAME_FACTORY, new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue(
				"cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newBagOfFunctionTestParams(NAME_RFC822NAME_BAG, StandardDatatypes.RFC822NAME_FACTORY, new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_IPADDRESS_BAG, StandardDatatypes.IPADDRESS_FACTORY, new IPAddressValue("192.168.1.10"), new IPAddressValue("192.168.1.11")));
		params.addAll(newBagOfFunctionTestParams(NAME_DNSNAME_BAG, StandardDatatypes.DNSNAME_FACTORY, new DNSNameWithPortRangeValue("example.com"), new DNSNameWithPortRangeValue("example1.com")));

		return params;
	}

}
