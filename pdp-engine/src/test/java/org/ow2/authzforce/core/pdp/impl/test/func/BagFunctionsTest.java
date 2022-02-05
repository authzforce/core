/*
 * Copyright 2012-2022 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.test.func;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.*;

@RunWith(Parameterized.class)
public class BagFunctionsTest extends StandardFunctionTest
{

	public BagFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
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
	private static <AV extends AttributeValue> Collection<Object[]> newOneAndOnlyFunctionTestParams(final String oneAndOnlyFunctionId, final AttributeDatatype<AV> bagElementType,
			final AV primitiveValue)
	{
		final Collection<Object[]> params = new ArrayList<>();

		// case 1: empty bag {}
		// one-and-only({}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Collections.singletonList(bagElementType.getEmptyBag()), null });

		// one-and-only({primitiveValue}) -> primitiveValue
		params.add(new Object[] { oneAndOnlyFunctionId, Collections.singletonList(Bags.singleton(bagElementType, primitiveValue)), primitiveValue });

		// one-and-only({primitiveValue, primitiveValue}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Collections.singletonList(Bags.newBag(bagElementType, Collections.nCopies(2, primitiveValue))), null });

		return params;
	}

	private static final IntegerValue ZERO_AS_INT = IntegerValue.valueOf(0);
	private static final IntegerValue ONE_AS_INT = IntegerValue.valueOf(1);
	private static final IntegerValue TWO_AS_INT = IntegerValue.valueOf(2);

	/**
	 * *-bag-size function test parameters. For each, we test with an empty bag parameter, then with an one-value bag, then a two-value bag.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newBagSizeFunctionTestParams(final String bagSizeFunctionId, final AttributeDatatype<AV> bagElementType, final AV primitiveValue)
	{
		final Collection<Object[]> params = new ArrayList<>();

		// bag-size({}) -> 0
		params.add(new Object[] { bagSizeFunctionId, Collections.singletonList(bagElementType.getEmptyBag()), ZERO_AS_INT });

		// bag-size({primitiveValue}) -> 1
		params.add(new Object[] { bagSizeFunctionId, Collections.singletonList(Bags.singleton(bagElementType, primitiveValue)), ONE_AS_INT });

		// bag-size({primitiveValue, primitiveValue}) -> 2
		params.add(new Object[] { bagSizeFunctionId, Collections.singletonList(Bags.newBag(bagElementType, Collections.nCopies(2, primitiveValue))), TWO_AS_INT });
		return params;
	}

	/**
	 * *-is-in function test parameters. We want to test the following cases: 1) the first argument is not in the empty bag as second argument, 2) the first argument is in the non-empty bag as second
	 * argument, 3) the first argument is not in the non-empty bag as second argument.
	 * <p>
	 * Parameters primitiveValue1 and primitiveValue2 MUST be different values.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newIsInFunctionTestParams(final String isInFunctionId, final AttributeDatatype<AV> bagElementType, final AV primitiveValue1,
			final AV primitiveValue2)
	{
		final Collection<Object[]> params = new ArrayList<>();

		// is-in(val, {}) -> false
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue1, bagElementType.getEmptyBag()), BooleanValue.FALSE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue2}) -> true
		final Bag<AV> twoValBag = Bags.newBag(bagElementType, Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag), BooleanValue.TRUE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue1}) -> false
		final Bag<AV> twoValBag2 = Bags.newBag(bagElementType, Collections.nCopies(2, primitiveValue1));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag2), BooleanValue.FALSE });
		return params;
	}

	/**
	 * *-bag function test parameters.
	 */
	private static <AV extends AttributeValue> Collection<Object[]> newBagOfFunctionTestParams(final String bagOfFunctionId, final PrimitiveDatatype<AV> bagElementType, final AV primitiveValue1,
			final AV primitiveValue2)
	{
		final Collection<Object[]> params = new ArrayList<>();

		// bag(primitiveValue1, primitiveValue2) -> {primitiveValue1, primitiveValue2}
		final Bag<AV> twoValBag = Bags.newBag(bagElementType, Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { bagOfFunctionId, Arrays.asList(primitiveValue1, primitiveValue2), twoValBag });
		return params;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params()
	{
		final Collection<Object[]> params = new ArrayList<>();

		// *-one-and-only functions
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_STRING_ONE_AND_ONLY, StandardDatatypes.STRING, new StringValue("Test")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BOOLEAN_ONE_AND_ONLY, StandardDatatypes.BOOLEAN, BooleanValue.FALSE));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_INTEGER_ONE_AND_ONLY, StandardDatatypes.INTEGER, IntegerValue.valueOf(3)));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DOUBLE_ONE_AND_ONLY, StandardDatatypes.DOUBLE, new DoubleValue("3.14")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_ANYURI_ONE_AND_ONLY, StandardDatatypes.ANYURI, new AnyUriValue("http://www.example.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_HEXBINARY_ONE_AND_ONLY, StandardDatatypes.HEXBINARY, new HexBinaryValue("0FB7")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BASE64BINARY_ONE_AND_ONLY, StandardDatatypes.BASE64BINARY, new Base64BinaryValue("RXhhbXBsZQ==")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_TIME_ONE_AND_ONLY, StandardDatatypes.TIME, new TimeValue("09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATE_ONE_AND_ONLY, StandardDatatypes.DATE, new DateValue("2002-09-24")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATETIME_ONE_AND_ONLY, StandardDatatypes.DATETIME, new DateTimeValue("2002-09-24T09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DAYTIMEDURATION_ONE_AND_ONLY, StandardDatatypes.DAYTIMEDURATION, new DayTimeDurationValue("P1DT2H")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_YEARMONTHDURATION_ONE_AND_ONLY, StandardDatatypes.YEARMONTHDURATION, new YearMonthDurationValue("P1Y2M")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_X500NAME_ONE_AND_ONLY, StandardDatatypes.X500NAME, new X500NameValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_RFC822NAME_ONE_AND_ONLY, StandardDatatypes.RFC822NAME, new Rfc822NameValue("Anderson@sun.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_IPADDRESS_ONE_AND_ONLY, StandardDatatypes.IPADDRESS, IpAddressValue.valueOf("192.168.1.10")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DNSNAME_ONE_AND_ONLY, StandardDatatypes.DNSNAME, new DnsNameWithPortRangeValue("example.com")));

		// *-bag-size functions
		params.addAll(newBagSizeFunctionTestParams(NAME_STRING_BAG_SIZE, StandardDatatypes.STRING, new StringValue("Test")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BOOLEAN_BAG_SIZE, StandardDatatypes.BOOLEAN, BooleanValue.FALSE));
		params.addAll(newBagSizeFunctionTestParams(NAME_INTEGER_BAG_SIZE, StandardDatatypes.INTEGER, IntegerValue.valueOf(1)));
		params.addAll(newBagSizeFunctionTestParams(NAME_DOUBLE_BAG_SIZE, StandardDatatypes.DOUBLE, new DoubleValue("3.14")));
		params.addAll(newBagSizeFunctionTestParams(NAME_ANYURI_BAG_SIZE, StandardDatatypes.ANYURI, new AnyUriValue("http://www.example.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_HEXBINARY_BAG_SIZE, StandardDatatypes.HEXBINARY, new HexBinaryValue("0FB7")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BASE64BINARY_BAG_SIZE, StandardDatatypes.BASE64BINARY, new Base64BinaryValue("RXhhbXBsZQ==")));
		params.addAll(newBagSizeFunctionTestParams(NAME_TIME_BAG_SIZE, StandardDatatypes.TIME, new TimeValue("09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATE_BAG_SIZE, StandardDatatypes.DATE, new DateValue("2002-09-24")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATETIME_BAG_SIZE, StandardDatatypes.DATETIME, new DateTimeValue("2002-09-24T09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DAYTIMEDURATION_BAG_SIZE, StandardDatatypes.DAYTIMEDURATION, new DayTimeDurationValue("P1DT2H")));
		params.addAll(newBagSizeFunctionTestParams(NAME_YEARMONTHDURATION_BAG_SIZE, StandardDatatypes.YEARMONTHDURATION, new YearMonthDurationValue("P1Y2M")));
		params.addAll(newBagSizeFunctionTestParams(NAME_X500NAME_BAG_SIZE, StandardDatatypes.X500NAME, new X500NameValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newBagSizeFunctionTestParams(NAME_RFC822NAME_BAG_SIZE, StandardDatatypes.RFC822NAME, new Rfc822NameValue("Anderson@sun.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_IPADDRESS_BAG_SIZE, StandardDatatypes.IPADDRESS, IpAddressValue.valueOf("192.168.1.10")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DNSNAME_BAG_SIZE, StandardDatatypes.DNSNAME, new DnsNameWithPortRangeValue("example.com")));

		// *-is-in functions
		params.addAll(newIsInFunctionTestParams(NAME_STRING_IS_IN, StandardDatatypes.STRING, new StringValue("Test1"), new StringValue("Test2")));
		params.addAll(newIsInFunctionTestParams(NAME_BOOLEAN_IS_IN, StandardDatatypes.BOOLEAN, BooleanValue.FALSE, BooleanValue.TRUE));
		params.addAll(newIsInFunctionTestParams(NAME_INTEGER_IS_IN, StandardDatatypes.INTEGER, IntegerValue.valueOf(1), IntegerValue.valueOf(2)));
		params.addAll(newIsInFunctionTestParams(NAME_DOUBLE_IS_IN, StandardDatatypes.DOUBLE, new DoubleValue("-4.21"), new DoubleValue("3.14")));
		params.addAll(newIsInFunctionTestParams(NAME_ANYURI_IS_IN, StandardDatatypes.ANYURI, new AnyUriValue("http://www.example.com"), new AnyUriValue("http://www.example1.com")));
		params.addAll(newIsInFunctionTestParams(NAME_HEXBINARY_IS_IN, StandardDatatypes.HEXBINARY, new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")));
		params.addAll(newIsInFunctionTestParams(NAME_BASE64BINARY_IS_IN, StandardDatatypes.BASE64BINARY, new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")));
		params.addAll(newIsInFunctionTestParams(NAME_TIME_IS_IN, StandardDatatypes.TIME, new TimeValue("09:30:15"), new TimeValue("17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DATE_IS_IN, StandardDatatypes.DATE, new DateValue("2002-09-24"), new DateValue("2003-10-25")));
		params.addAll(newIsInFunctionTestParams(NAME_DATETIME_IS_IN, StandardDatatypes.DATETIME, new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DAYTIMEDURATION_IS_IN, StandardDatatypes.DAYTIMEDURATION, new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P1DT3H")));
		params.addAll(newIsInFunctionTestParams(NAME_YEARMONTHDURATION_IS_IN, StandardDatatypes.YEARMONTHDURATION, new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P1Y3M")));
		params.addAll(newIsInFunctionTestParams(NAME_X500NAME_IS_IN, StandardDatatypes.X500NAME, new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue(
				"cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newIsInFunctionTestParams(NAME_RFC822NAME_IS_IN, StandardDatatypes.RFC822NAME, new Rfc822NameValue("Anderson@sun.com"), new Rfc822NameValue("Smith@sun.com")));
		params.addAll(newIsInFunctionTestParams(NAME_IPADDRESS_IS_IN, StandardDatatypes.IPADDRESS, IpAddressValue.valueOf("192.168.1.10"), IpAddressValue.valueOf("192.168.1.11")));
		params.addAll(newIsInFunctionTestParams(NAME_DNSNAME_IS_IN, StandardDatatypes.DNSNAME, new DnsNameWithPortRangeValue("example.com"), new DnsNameWithPortRangeValue("example1.com")));

		// *-bag functions
		params.addAll(newBagOfFunctionTestParams(NAME_STRING_BAG, StandardDatatypes.STRING, new StringValue("Test1"), new StringValue("Test2")));
		params.addAll(newBagOfFunctionTestParams(NAME_BOOLEAN_BAG, StandardDatatypes.BOOLEAN, BooleanValue.FALSE, BooleanValue.TRUE));
		params.addAll(newBagOfFunctionTestParams(NAME_INTEGER_BAG, StandardDatatypes.INTEGER, IntegerValue.valueOf(1), IntegerValue.valueOf(2)));
		params.addAll(newBagOfFunctionTestParams(NAME_DOUBLE_BAG, StandardDatatypes.DOUBLE, new DoubleValue("-4.21"), new DoubleValue("3.14")));
		params.addAll(newBagOfFunctionTestParams(NAME_ANYURI_BAG, StandardDatatypes.ANYURI, new AnyUriValue("http://www.example.com"), new AnyUriValue("http://www.example1.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_HEXBINARY_BAG, StandardDatatypes.HEXBINARY, new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")));
		params.addAll(newBagOfFunctionTestParams(NAME_BASE64BINARY_BAG, StandardDatatypes.BASE64BINARY, new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")));
		params.addAll(newBagOfFunctionTestParams(NAME_TIME_BAG, StandardDatatypes.TIME, new TimeValue("09:30:15"), new TimeValue("17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATE_BAG, StandardDatatypes.DATE, new DateValue("2002-09-24"), new DateValue("2003-10-25")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATETIME_BAG, StandardDatatypes.DATETIME, new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DAYTIMEDURATION_BAG, StandardDatatypes.DAYTIMEDURATION, new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P1DT3H")));
		params.addAll(newBagOfFunctionTestParams(NAME_YEARMONTHDURATION_BAG, StandardDatatypes.YEARMONTHDURATION, new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P1Y3M")));
		params.addAll(newBagOfFunctionTestParams(NAME_X500NAME_BAG, StandardDatatypes.X500NAME, new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue(
				"cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newBagOfFunctionTestParams(NAME_RFC822NAME_BAG, StandardDatatypes.RFC822NAME, new Rfc822NameValue("Anderson@sun.com"), new Rfc822NameValue("Smith@sun.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_IPADDRESS_BAG, StandardDatatypes.IPADDRESS, IpAddressValue.valueOf("192.168.1.10"), IpAddressValue.valueOf("192.168.1.11")));
		params.addAll(newBagOfFunctionTestParams(NAME_DNSNAME_BAG, StandardDatatypes.DNSNAME, new DnsNameWithPortRangeValue("example.com"), new DnsNameWithPortRangeValue("example1.com")));

		return params;
	}

}
