/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.func.ComparisonFunction;
import org.ow2.authzforce.core.pdp.api.func.ComparisonFunction.PostCondition;
import org.ow2.authzforce.core.pdp.api.func.DatatypeConversionFunction;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.EqualIgnoreCaseMatcher;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.EqualMatcher;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderBagFunctions;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction.RegexpMatchCallFactoryBuilder;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DNSNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.api.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticOperators.AbsOperator;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticOperators.AddOperator;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticOperators.DivideOperator;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticOperators.MultiplyOperator;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticOperators.SubtractOperator;
import org.ow2.authzforce.core.pdp.impl.func.StandardDatatypeConverters.FromStringConverter;
import org.ow2.authzforce.core.pdp.impl.func.StandardDatatypeConverters.ToStringConverter;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AllOfAll;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AllOfAny;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AllOfCallFactory;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AnyOfAll;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AnyOfAny;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.AnyOfCallFactory;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.BooleanOneBagOnlyFunction;
import org.ow2.authzforce.core.pdp.impl.func.TemporalArithmeticOperators.TimeAddDurationOperator;
import org.ow2.authzforce.core.pdp.impl.func.TemporalArithmeticOperators.TimeSubtractDurationOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory supports the standard set of functions specified in XACML 1.x and 2.0 and 3.0.
 * <p>
 * Note that because this supports only the standard functions, this factory does not allow the addition of any other functions. If you call {@link #addFunction(Function)} on an instance of this
 * class, an exception will be thrown. If you need a standard factory that is modifiable, you can either create a new <code>BasePdpExtensionRegistry</code> (or some other implementation of
 * <code>PdpExtensionRegistry</code>) populated with the standard functions from {@code this#getSupportedFunctions()}
 *
 * 
 * @version $Id: $
 */
public final class StandardFunctionRegistry extends FunctionRegistry
{

	/**
	 * *-string-normalize-* function cluster
	 */
	public enum StdFunction
	{

		/**
		 * Equal-type match
		 */

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:string-equal
		 */
		STRING_EQUAL(Function.XACML_NS_1_0 + "string-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:boolean-equal
		 */
		BOOLEAN_EQUAL(Function.XACML_NS_1_0 + "boolean-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-equal
		 */
		INTEGER_EQUAL(Function.XACML_NS_1_0 + "integer-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-equal
		 */
		DOUBLE_EQUAL(Function.XACML_NS_1_0 + "double-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:date-equal
		 */
		DATE_EQUAL(Function.XACML_NS_1_0 + "date-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:time-equal
		 */
		TIME_EQUAL(Function.XACML_NS_1_0 + "time-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
		 */
		DATETIME_EQUAL(Function.XACML_NS_1_0 + "dateTime-equal"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
		 */
		DAYTIME_DURATION_EQUAL(Function.XACML_NS_3_0 + "dayTimeDuration-equal"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
		 */
		YEARMONTH_DURATION_EQUAL(Function.XACML_NS_3_0 + "yearMonthDuration-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
		 */
		ANYURI_EQUAL(Function.XACML_NS_1_0 + "anyURI-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:x500Name-equal function.
		 */
		X500NAME_EQUAL(Function.XACML_NS_1_0 + "x500Name-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal function.
		 */
		RFC822NAME_EQUAL(Function.XACML_NS_1_0 + "rfc822Name-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal function.
		 */
		HEXBINARY_EQUAL(Function.XACML_NS_1_0 + "hexBinary-equal"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal function.
		 */
		BASE64BINARY_EQUAL(Function.XACML_NS_1_0 + "base64Binary-equal"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case function.
		 */
		STRING_EQUAL_IGNORE_CASE(Function.XACML_NS_3_0 + "string-equal-ignore-case"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:x500Name-match function (different from x500Name-regexp-match down below).
		 */
		X500NAME_MATCH(Function.XACML_NS_1_0 + "x500Name-match"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-starts-with function.
		 */
		STRING_STARTS_WITH(Function.XACML_NS_3_0 + "string-starts-with"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-ends-with function.
		 */
		STRING_ENDS_WITH(Function.XACML_NS_3_0 + "string-ends-with"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-contains-with function.
		 */
		STRING_CONTAINS(Function.XACML_NS_3_0 + "string-contains"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-regexp-match function.
		 */
		STRING_REGEXP_MATCH(Function.XACML_NS_1_0 + "string-regexp-match"),

		/**
		 * Numeric arithmetic functions
		 */
		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-abs function URI
		 */
		INTEGER_ABS(Function.XACML_NS_1_0 + "integer-abs"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-abs function URI
		 */
		DOUBLE_ABS(Function.XACML_NS_1_0 + "double-abs"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-add
		 */
		INTEGER_ADD(Function.XACML_NS_1_0 + "integer-add"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-add
		 */
		DOUBLE_ADD(Function.XACML_NS_1_0 + "double-add"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-multiply function.
		 */
		INTEGER_MULTIPLY(Function.XACML_NS_1_0 + "integer-multiply"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-multiply function.
		 */
		DOUBLE_MULTIPLY(Function.XACML_NS_1_0 + "double-multiply"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-subtract function.
		 */
		INTEGER_SUBTRACT(Function.XACML_NS_1_0 + "integer-subtract"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-subtract function.
		 */
		DOUBLE_SUBTRACT(Function.XACML_NS_1_0 + "double-subtract"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-divide function.
		 */
		INTEGER_DIVIDE(Function.XACML_NS_1_0 + "integer-divide"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-divide function.
		 */
		DOUBLE_DIVIDE(Function.XACML_NS_1_0 + "double-divide"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-mod function.
		 */
		INTEGER_MOD(Function.XACML_NS_1_0 + "integer-mod"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:round function.
		 */
		ROUND(Function.XACML_NS_1_0 + "round"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:floor function.
		 */
		FLOOR(Function.XACML_NS_1_0 + "floor"),

		/**
		 * String normalization functions
		 */

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
		 */
		STRING_NORMALIZE_SPACE(Function.XACML_NS_1_0 + "string-normalize-space"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case
		 */
		STRING_NORMALIZE_TO_LOWER_CASE(Function.XACML_NS_1_0 + "string-normalize-to-lower-case"),

		/**
		 * Primitive datatype conversion functions
		 */
		/**
		 * urn:oasis:names:tc:xacml:1.0:function:double-to-integer function.
		 */
		DOUBLE_TO_INTEGER(Function.XACML_NS_1_0 + "double-to-integer"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:integer-to-double function.
		 */
		INTEGER_TO_DOUBLE(Function.XACML_NS_1_0 + "integer-to-double"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:boolean-from-string function.
		 */
		BOOLEAN_FROM_STRING(Function.XACML_NS_3_0 + "boolean-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-boolean function.
		 */
		STRING_FROM_BOOLEAN(Function.XACML_NS_3_0 + "string-from-boolean"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:integer-from-string function.
		 */
		INTEGER_FROM_STRING(Function.XACML_NS_3_0 + "integer-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-integer function.
		 */
		STRING_FROM_INTEGER(Function.XACML_NS_3_0 + "string-from-integer"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:double-from-string function.
		 */
		DOUBLE_FROM_STRING(Function.XACML_NS_3_0 + "double-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-double function.
		 */
		STRING_FROM_DOUBLE(Function.XACML_NS_3_0 + "string-from-double"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:time-from-string function.
		 */
		TIME_FROM_STRING(Function.XACML_NS_3_0 + "time-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-time function.
		 */
		STRING_FROM_TIME(Function.XACML_NS_3_0 + "string-from-time"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:date-from-string function.
		 */
		DATE_FROM_STRING(Function.XACML_NS_3_0 + "date-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-date function.
		 */
		STRING_FROM_DATE(Function.XACML_NS_3_0 + "string-from-date"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string function.
		 */
		DATETIME_FROM_STRING(Function.XACML_NS_3_0 + "dateTime-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime function.
		 */
		STRING_FROM_DATETIME(Function.XACML_NS_3_0 + "string-from-dateTime"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string function.
		 */
		ANYURI_FROM_STRING(Function.XACML_NS_3_0 + "anyURI-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI function.
		 */
		STRING_FROM_ANYURI(Function.XACML_NS_3_0 + "string-from-anyURI"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string function.
		 */
		DAYTIMEDURATION_FROM_STRING(Function.XACML_NS_3_0 + "dayTimeDuration-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration function.
		 */
		STRING_FROM_DAYTIMEDURATION(Function.XACML_NS_3_0 + "string-from-dayTimeDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string function.
		 */
		YEARMONTHDURATION_FROM_STRING(Function.XACML_NS_3_0 + "yearMonthDuration-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration function.
		 */
		STRING_FROM_YEARMONTHDURATION(Function.XACML_NS_3_0 + "string-from-yearMonthDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string function.
		 */
		X500NAME_FROM_STRING(Function.XACML_NS_3_0 + "x500Name-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name function.
		 */
		STRING_FROM_X500NAME(Function.XACML_NS_3_0 + "string-from-x500Name"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string function.
		 */
		RFC822NAME_FROM_STRING(Function.XACML_NS_3_0 + "rfc822Name-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name function.
		 */
		STRING_FROM_RFC822NAME(Function.XACML_NS_3_0 + "string-from-rfc822Name"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string function.
		 */
		IPADDRESS_FROM_STRING(Function.XACML_NS_3_0 + "ipAddress-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress function.
		 */
		STRING_FROM_IPADDRESS(Function.XACML_NS_3_0 + "string-from-ipAddress"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string function.
		 */
		DNSNAME_FROM_STRING(Function.XACML_NS_3_0 + "dnsName-from-string"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName function.
		 */
		STRING_FROM_DNSNAME(Function.XACML_NS_3_0 + "string-from-dnsName"),

		/**
		 * Logical functions
		 */

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:or
		 */
		OR(Function.XACML_NS_1_0 + "or"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:and
		 */
		AND(Function.XACML_NS_1_0 + "and"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:n-of
		 */
		N_OF(Function.XACML_NS_1_0 + "n-of"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:not
		 */
		NOT(Function.XACML_NS_1_0 + "not"),

		/**
		 * Temporal arithmetic functions
		 */
		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration function.
		 */
		DATETIME_ADD_DAYTIMEDURATION(Function.XACML_NS_3_0 + "dateTime-add-dayTimeDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration function.
		 */
		DATETIME_SUBTRACT_DAYTIMEDURATION(Function.XACML_NS_3_0 + "dateTime-subtract-dayTimeDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration function.
		 */
		DATETIME_ADD_YEARMONTHDURATION(Function.XACML_NS_3_0 + "dateTime-add-yearMonthDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration function.
		 */
		DATETIME_SUBTRACT_YEARMONTHDURATION(Function.XACML_NS_3_0 + "dateTime-subtract-yearMonthDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration function.
		 */
		DATE_ADD_YEARMONTHDURATION(Function.XACML_NS_3_0 + "date-add-yearMonthDuration"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration function.
		 */
		DATE_SUBTRACT_YEARMONTHDURATION(Function.XACML_NS_3_0 + "date-subtract-yearMonthDuration"),

		/**
		 * Time range comparison function
		 */

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:time-in-range
		 */
		TIME_IN_RANGE(Function.XACML_NS_2_0 + "time-in-range"),

		/**
		 * String concatenation function
		 */

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:string-concatenate function.
		 */
		STRING_CONCATENATE(Function.XACML_NS_2_0 + "string-concatenate"),

		/**
		 * Non-equal type match
		 */

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match function (different from rfc822Name-regexp-match down below).
		 */
		RFC822NAME_MATCH(Function.XACML_NS_1_0 + "rfc822Name-match"),

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match function.
		 */
		ANYURI_REGEXP_MATCH(Function.XACML_NS_2_0 + "anyURI-regexp-match"),

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match function.
		 */
		IPADDRESS_REGEXP_MATCH(Function.XACML_NS_2_0 + "ipAddress-regexp-match"),

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match function.
		 */
		DNSNAME_REGEXP_MATCH(Function.XACML_NS_2_0 + "dnsName-regexp-match"),

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match function.
		 */
		RFC822NAME_REGEXP_MATCH(Function.XACML_NS_2_0 + "rfc822Name-regexp-match"),

		/**
		 * urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match function.
		 */
		X500NAME_REGEXP_MATCH(Function.XACML_NS_2_0 + "x500Name-regexp-match"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with function.
		 */
		ANYURI_STARTS_WITH(Function.XACML_NS_3_0 + "anyURI-starts-with"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with function.
		 */
		ANYURI_ENDS_WITH(Function.XACML_NS_3_0 + "anyURI-ends-with"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:anyURI-contains-with function.
		 */
		ANYURI_CONTAINS(Function.XACML_NS_3_0 + "anyURI-contains"),

		/**
		 * Substring functions
		 */
		/**
		 * urn:oasis:names:tc:xacml:3.0:function:string-substring function.
		 */
		STRING_SUBSTRING(Function.XACML_NS_3_0 + "string-substring"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:anyURI-substring function.
		 */
		ANYURI_SUBSTRING(Function.XACML_NS_3_0 + "anyURI-substring"),

		/**
		 * Higher-order bag functions
		 */

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:any-of function. WARNING: XACML 1.0 any-of planned for deprecation as of XACML 3.0. Only 3.0 version supported henceforth.
		 */
		ANY_OF(Function.XACML_NS_3_0 + "any-of"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:all-of function.
		 */
		ALL_OF(Function.XACML_NS_3_0 + "all-of"),

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:any-of-any function.
		 */
		ANY_OF_ANY(Function.XACML_NS_3_0 + "any-of-any"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:all-of-any function.
		 */
		ALL_OF_ANY(Function.XACML_NS_1_0 + "all-of-any"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:any-of-all function.
		 */
		ANY_OF_ALL(Function.XACML_NS_1_0 + "any-of-all"),

		/**
		 * urn:oasis:names:tc:xacml:1.0:function:all-of-all function.
		 */
		ALL_OF_ALL(Function.XACML_NS_1_0 + "all-of-all"),

		/**
		 * Generic functions
		 */

		/**
		 * urn:oasis:names:tc:xacml:3.0:function:map function.
		 */
		MAP(Function.XACML_NS_3_0 + "map");

		private final String id;

		private StdFunction(final String id)
		{
			this.id = id;
		}

		/**
		 * Get function ID, as defined in the XACML standard
		 * 
		 * @return function ID
		 */
		public String getId()
		{
			return this.id;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardFunctionRegistry.class);

	private static final PdpExtensionComparator<Function<?>> FUNCTION_COMPARATOR = new PdpExtensionComparator<>();
	private static final PdpExtensionComparator<GenericHigherOrderFunctionFactory> FUNCTION_FACTORY_COMPARATOR = new PdpExtensionComparator<>();

	private static final StandardFunctionRegistry NON_XPATH_FUNCTIONS;
	private static final StandardFunctionRegistry ALL_FUNCTIONS;

	static
	{
		final Set<Function<?>> nonGenericFunctions = new HashSet<>();
		/*
		 * Add standard functions in an order as close as possible to the order of declaration in the XACML spec (A.3).
		 */

		/*
		 * Match functions taking only one type of parameter: Equality predicates (A.3.1) + special match function 'x500Name-match' (A.3.14)
		 */
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_EQUAL.id, StandardDatatypes.STRING_FACTORY.getDatatype(), new EqualMatcher<StringValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.BOOLEAN_EQUAL.id, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), new EqualMatcher<BooleanValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.INTEGER_EQUAL.id, StandardDatatypes.INTEGER_FACTORY.getDatatype(), new EqualMatcher<IntegerValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.DOUBLE_EQUAL.id, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), new EqualMatcher<DoubleValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.DATE_EQUAL.id, StandardDatatypes.DATE_FACTORY.getDatatype(), new EqualMatcher<DateValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.TIME_EQUAL.id, StandardDatatypes.TIME_FACTORY.getDatatype(), new EqualMatcher<TimeValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.DATETIME_EQUAL.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(), new EqualMatcher<DateTimeValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.DAYTIME_DURATION_EQUAL.id, StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), new EqualMatcher<DayTimeDurationValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.YEARMONTH_DURATION_EQUAL.id, StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(),
				new EqualMatcher<YearMonthDurationValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.ANYURI_EQUAL.id, StandardDatatypes.ANYURI_FACTORY.getDatatype(), new EqualMatcher<AnyURIValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.X500NAME_EQUAL.id, StandardDatatypes.X500NAME_FACTORY.getDatatype(), new EqualMatcher<X500NameValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.RFC822NAME_EQUAL.id, StandardDatatypes.RFC822NAME_FACTORY.getDatatype(), new EqualMatcher<RFC822NameValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.HEXBINARY_EQUAL.id, StandardDatatypes.HEXBINARY_FACTORY.getDatatype(), new EqualMatcher<HexBinaryValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.BASE64BINARY_EQUAL.id, StandardDatatypes.BASE64BINARY_FACTORY.getDatatype(), new EqualMatcher<Base64BinaryValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_EQUAL_IGNORE_CASE.id, StandardDatatypes.STRING_FACTORY.getDatatype(), new EqualIgnoreCaseMatcher<StringValue>()));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.X500NAME_MATCH.id, StandardDatatypes.X500NAME_FACTORY.getDatatype(), StandardEqualTypeMatchers.X500NAME_MATCHER));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_STARTS_WITH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardEqualTypeMatchers.STRING_STARTS_WITH_MATCHER));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_ENDS_WITH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardEqualTypeMatchers.STRING_ENDS_WITH_MATCHER));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_CONTAINS.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardEqualTypeMatchers.STRING_CONTAINS_MATCHER));
		nonGenericFunctions.add(new EqualTypeMatchFunction<>(StdFunction.STRING_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(),
				StandardEqualTypeMatchers.STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER));

		/*
		 * Numeric Arithmetic functions (A.3.2)
		 */
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_ABS.id, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype()), new AbsOperator<IntegerValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.DOUBLE_ABS.id, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new AbsOperator<DoubleValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_ADD.id, true, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype()), new AddOperator<IntegerValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.DOUBLE_ADD.id, true, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new AddOperator<DoubleValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_MULTIPLY.id, true, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype()), new MultiplyOperator<IntegerValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.DOUBLE_MULTIPLY.id, true, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new MultiplyOperator<DoubleValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_SUBTRACT.id, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype()), new SubtractOperator<IntegerValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.DOUBLE_SUBTRACT.id, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new SubtractOperator<DoubleValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_DIVIDE.id, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype()), new DivideOperator<IntegerValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.DOUBLE_DIVIDE.id, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new DivideOperator<DoubleValue>()));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.INTEGER_MOD.id, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype()), NumericArithmeticOperators.INTEGER_MOD_OPERATOR));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.FLOOR.id, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), NumericArithmeticOperators.FLOOR_OPERATOR));
		nonGenericFunctions.add(new NumericArithmeticFunction<>(StdFunction.ROUND.id, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), NumericArithmeticOperators.ROUND_OPERATOR));

		/*
		 * String-normalize functions (= A.3.3 String conversion functions in the spec)
		 */
		nonGenericFunctions.add(new StringN11nFunction(StdFunction.STRING_NORMALIZE_SPACE.id, StringN11nFunction.STRING_NORMALIZE_SPACE_FUNCTION_CALL_FACTORY));
		nonGenericFunctions.add(new StringN11nFunction(StdFunction.STRING_NORMALIZE_TO_LOWER_CASE.id, StringN11nFunction.STRING_NORMALIZE_TO_LOWER_CASE_FUNCTION_CALL_FACTORY));

		/*
		 * Primitive data-type conversion functions: A.3.4 Numeric data-type conversion functions, and all {type}-from-string / string-from-{type} functions from A.3.9 (other parts of A.3.9 addressed
		 * below by StringConcatenateFunction, NonEqualTypeMatchFunction, SubstringFunction)
		 */
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DOUBLE_TO_INTEGER.id, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypeConverters.DOUBLE_TO_INTEGER_CONVERTER));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.INTEGER_TO_DOUBLE.id, StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				StandardDatatypeConverters.INTEGER_TO_DOUBLE_CONVERTER));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.BOOLEAN_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.BOOLEAN_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.BOOLEAN_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_BOOLEAN.id, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				StandardDatatypeConverters.BOOLEAN_TO_STRING_CONVERTER));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.INTEGER_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.INTEGER_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_INTEGER.id, StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<IntegerValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DOUBLE_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.DOUBLE_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_DOUBLE.id, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<DoubleValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.TIME_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.TIME_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.TIME_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_TIME.id, StandardDatatypes.TIME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<TimeValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DATE_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DATE_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.DATE_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_DATE.id, StandardDatatypes.DATE_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<DateValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DATETIME_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DATETIME_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.DATETIME_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_DATETIME.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<DateTimeValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.ANYURI_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.ANYURI_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_ANYURI.id, StandardDatatypes.ANYURI_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<AnyURIValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DAYTIMEDURATION_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DAYTIMEDURATION_FACTORY
				.getDatatype(), new FromStringConverter<>(StandardDatatypes.DAYTIMEDURATION_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_DAYTIMEDURATION.id, StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY
				.getDatatype(), new ToStringConverter<DayTimeDurationValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.YEARMONTHDURATION_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(),
				StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), new FromStringConverter<>(StandardDatatypes.YEARMONTHDURATION_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_YEARMONTHDURATION.id, StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(),
				StandardDatatypes.STRING_FACTORY.getDatatype(), new ToStringConverter<YearMonthDurationValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.X500NAME_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.X500NAME_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.X500NAME_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_X500NAME.id, StandardDatatypes.X500NAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<X500NameValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.RFC822NAME_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY
				.getDatatype(), new FromStringConverter<>(StandardDatatypes.RFC822NAME_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_RFC822NAME.id, StandardDatatypes.RFC822NAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY
				.getDatatype(), new ToStringConverter<RFC822NameValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.IPADDRESS_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.IPADDRESS_FACTORY
				.getDatatype(), new FromStringConverter<>(StandardDatatypes.IPADDRESS_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_IPADDRESS.id, StandardDatatypes.IPADDRESS_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY
				.getDatatype(), new ToStringConverter<IPAddressValue>()));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.DNSNAME_FROM_STRING.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DNSNAME_FACTORY.getDatatype(),
				new FromStringConverter<>(StandardDatatypes.DNSNAME_FACTORY)));
		nonGenericFunctions.add(new DatatypeConversionFunction<>(StdFunction.STRING_FROM_DNSNAME.id, StandardDatatypes.DNSNAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
				new ToStringConverter<DNSNameWithPortRangeValue>()));

		/*
		 * Logical functions (A.3.5)
		 */
		nonGenericFunctions.add(new LogicalOrFunction(StdFunction.OR.id));
		nonGenericFunctions.add(new LogicalAndFunction(StdFunction.AND.id));
		nonGenericFunctions.add(new LogicalNOfFunction(StdFunction.N_OF.id));
		nonGenericFunctions.add(new LogicalNotFunction(StdFunction.NOT.id));

		/*
		 * Total-ordering comparison functions (all elements of a given type can be compared to each other), i.e. numeric (A.3.6) and string comparison functions (first part of A.3.8)
		 */
		for (final PostCondition condition : PostCondition.values())
		{
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.INTEGER_FACTORY.getDatatype(), condition));
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), condition));
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.STRING_FACTORY.getDatatype(), condition));
		}

		/*
		 * Date and time arithmetic functions (A.3.7)
		 */
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATETIME_ADD_DAYTIMEDURATION.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(),
				StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), new TimeAddDurationOperator<DateTimeValue, DayTimeDurationValue>()));
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATETIME_SUBTRACT_DAYTIMEDURATION.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(),
				StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), new TimeSubtractDurationOperator<DateTimeValue, DayTimeDurationValue>()));
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATETIME_ADD_YEARMONTHDURATION.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(),
				StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), new TimeAddDurationOperator<DateTimeValue, YearMonthDurationValue>()));
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATETIME_SUBTRACT_YEARMONTHDURATION.id, StandardDatatypes.DATETIME_FACTORY.getDatatype(),
				StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), new TimeSubtractDurationOperator<DateTimeValue, YearMonthDurationValue>()));
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATE_ADD_YEARMONTHDURATION.id, StandardDatatypes.DATE_FACTORY.getDatatype(), StandardDatatypes.YEARMONTHDURATION_FACTORY
				.getDatatype(), new TimeAddDurationOperator<DateValue, YearMonthDurationValue>()));
		nonGenericFunctions.add(new TemporalArithmeticFunction<>(StdFunction.DATE_SUBTRACT_YEARMONTHDURATION.id, StandardDatatypes.DATE_FACTORY.getDatatype(),
				StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), new TimeSubtractDurationOperator<DateValue, YearMonthDurationValue>()));

		/*
		 * Date and time comparison functions (second part of A.3.8, first part already addressed previously), i.e. not imposing total ordering of compared objects, as opposed to total-ordering
		 * comparison functions above, since such date/times may have indeterminate relationship to each other
		 */
		for (final PostCondition condition : PostCondition.values())
		{
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.TIME_FACTORY.getDatatype(), condition));
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.DATE_FACTORY.getDatatype(), condition));
			nonGenericFunctions.add(new ComparisonFunction<>(StandardDatatypes.DATETIME_FACTORY.getDatatype(), condition));
		}

		nonGenericFunctions.add(new TimeRangeComparisonFunction(StdFunction.TIME_IN_RANGE.id));

		/*
		 * String-concatenate function (start of A.3.9, other parts addressed above by DatatypeConversionFunction, and below by NonEqualTypeMatchFunction and SubstringFunction)
		 */
		nonGenericFunctions.add(new StringConcatenateFunction(StdFunction.STRING_CONCATENATE.id));

		/*
		 * Match functions taking parameters of possibly different types, i.e. *-contains / *-starts-with / *-ends-with (second before last part of A.3.9, other parts addressed above by
		 * DatatypeConversionFunction, StringConcatenateFunction, and below by SubstringFunction), regexp-match (A.3.13) and special match 'rfc822Name-match' (part of A.3.14, other part addressed
		 * above by EqualTypeMatchFunction)
		 */
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.RFC822NAME_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY.getDatatype(),
				StandardNonEqualTypeMatchers.RFC822NAME_MATCHER));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.ANYURI_STARTS_WITH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
				StandardNonEqualTypeMatchers.ANYURI_STARTS_WITH_MATCHER));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.ANYURI_ENDS_WITH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
				StandardNonEqualTypeMatchers.ANYURI_ENDS_WITH_MATCHER));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.ANYURI_CONTAINS.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
				StandardNonEqualTypeMatchers.ANYURI_CONTAINS_MATCHER));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.ANYURI_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
				new RegexpMatchCallFactoryBuilder<AnyURIValue>()));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.IPADDRESS_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.IPADDRESS_FACTORY
				.getDatatype(), new RegexpMatchCallFactoryBuilder<IPAddressValue>()));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.DNSNAME_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DNSNAME_FACTORY.getDatatype(),
				new RegexpMatchCallFactoryBuilder<DNSNameWithPortRangeValue>()));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.RFC822NAME_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY
				.getDatatype(), new RegexpMatchCallFactoryBuilder<RFC822NameValue>()));
		nonGenericFunctions.add(new NonEqualTypeMatchFunction<>(StdFunction.X500NAME_REGEXP_MATCH.id, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.X500NAME_FACTORY.getDatatype(),
				new RegexpMatchCallFactoryBuilder<X500NameValue>()));

		/*
		 * Substring functions (last part of A.3.9, other parts addressed above by DatatypeConversionFunction, StringConcatenateFunction, NonEqualTypeMatchFunction)
		 */
		nonGenericFunctions.add(new SubstringFunction<>(StdFunction.STRING_SUBSTRING.id, StandardDatatypes.STRING_FACTORY.getDatatype()));
		nonGenericFunctions.add(new SubstringFunction<>(StdFunction.ANYURI_SUBSTRING.id, StandardDatatypes.ANYURI_FACTORY.getDatatype()));

		/*
		 * First-order bag functions (A.3.10, A.3.11)
		 */
		for (final SimpleValue.Factory<? extends SimpleValue<? extends Object>> typeFactory : StandardDatatypes.MANDATORY_DATATYPE_SET)
		{
			nonGenericFunctions.addAll(FirstOrderBagFunctions.getFunctions(typeFactory));
		}

		/*
		 * Higher-order bag functions (A.3.12)
		 */
		nonGenericFunctions.add(new BooleanOneBagOnlyFunction(StdFunction.ANY_OF.id, new AnyOfCallFactory(StdFunction.ANY_OF.id)));
		nonGenericFunctions.add(new BooleanOneBagOnlyFunction(StdFunction.ALL_OF.id, new AllOfCallFactory(StdFunction.ALL_OF.id)));
		nonGenericFunctions.add(new AnyOfAny(StdFunction.ANY_OF_ANY.id));
		nonGenericFunctions.add(new AllOfAny(StdFunction.ALL_OF_ANY.id));
		nonGenericFunctions.add(new AnyOfAll(StdFunction.ANY_OF_ALL.id));
		nonGenericFunctions.add(new AllOfAll(StdFunction.ALL_OF_ALL.id));

		/*
		 * A.3.13 already addressed above by NonEqualTypeMatchFunction
		 */
		/*
		 * A.3.14 already addressed above by EqualTypeMatchFunction and NonEqualTypeMatchFunction
		 */
		final BasePdpExtensionRegistry<Function<?>> nonGenericFuncRegistry = new BasePdpExtensionRegistry<>(Function.class, Collections.unmodifiableSet(nonGenericFunctions));

		// Generic functions, e.g. map function
		final Set<GenericHigherOrderFunctionFactory> genericFuncFactories = Collections.<GenericHigherOrderFunctionFactory> singleton(new MapFunctionFactory(StdFunction.MAP.id));
		final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFuncFactoryRegistry = new BasePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class, genericFuncFactories);

		NON_XPATH_FUNCTIONS = new StandardFunctionRegistry(nonGenericFuncRegistry, genericFuncFactoryRegistry);

		/*
		 * Optional functions
		 */
		/*
		 * A.3.15 functions only xpath-node-count supported
		 */
		nonGenericFuncRegistry.addExtension(XPathNodeCountFunction.INSTANCE);
		ALL_FUNCTIONS = new StandardFunctionRegistry(nonGenericFuncRegistry, genericFuncFactoryRegistry);

		/*
		 * A.3.16 not supported
		 */
		if (LOGGER.isDebugEnabled())
		{
			// TreeSet for sorting functions, easier to read
			final TreeSet<Function<?>> sortedFunctions = new TreeSet<>(FUNCTION_COMPARATOR);
			sortedFunctions.addAll(nonGenericFunctions);
			LOGGER.debug("Loaded XACML standard non-generic functions: {}", sortedFunctions);

			final TreeSet<GenericHigherOrderFunctionFactory> sortedFunctionFactories = new TreeSet<>(FUNCTION_FACTORY_COMPARATOR);
			sortedFunctionFactories.addAll(genericFuncFactories);
			LOGGER.debug("Loaded XACML standard generic functions: {}", sortedFunctionFactories);
		}
	}

	/**
	 * Creates a new StandardFunctionRegistry, making sure that the default maps are initialized correctly. Standard factories can't be modified, so there is no notion of supersetting since that's
	 * only used for correctly propagating new functions.
	 * 
	 * @param functionRegistry
	 * @param genericFunctionFactoryRegistry
	 */
	private StandardFunctionRegistry(final BasePdpExtensionRegistry<Function<?>> functionRegistry, final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFunctionFactoryRegistry)
	{
		super(functionRegistry, genericFunctionFactoryRegistry);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Always throws an exception, since support for new functions may not be added to a standard factory.
	 */
	@Override
	public void addFunction(final Function<?> function) throws IllegalArgumentException
	{
		throw new UnsupportedOperationException("A standard factory cannot be modified");
	}

	/**
	 * Get standard function registry
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @return standard function registry
	 */
	public static FunctionRegistry getInstance(final boolean enableXPath)
	{
		return enableXPath ? ALL_FUNCTIONS : NON_XPATH_FUNCTIONS;
	}

}
