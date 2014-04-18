/**
 * 
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.DNSNameAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class RegExpBasedFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_STRING_REGEXP_MATCH = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";
	private static final String NAME_ANYURI_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match";
	private static final String NAME_IPADDRESS_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match";
	private static final String NAME_DNSNAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match";
	private static final String NAME_RFC822NAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match";
	private static final String NAME_X500NAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
				new Object[] {
						NAME_STRING_REGEXP_MATCH,
						Arrays.asList(StringAttribute.getInstance("John.*"),
								StringAttribute.getInstance("John Doe")),
						EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute.getInstance("John.*"),
										StringAttribute.getInstance("Jane Doe")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
						new Object[] {
								NAME_ANYURI_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^http://.+"),
										AnyURIAttribute
												.getInstance("http://www.thalesgroup.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^http://.+"),
										AnyURIAttribute
												.getInstance("https://www.thalesgroup.com")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"),
										IPAddressAttribute
												.getInstance("10.10.10.190")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"),
										IPAddressAttribute
												.getInstance("10.144.10.190")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"),
										IPAddressAttribute
												.getInstance("10.10.10.10/255.255.255.0:80")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"),
										IPAddressAttribute
												.getInstance("192.168.1.10/255.255.255.0:8080")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"),
										IPAddressAttribute
												.getInstance("[1fff:0:a88:85a5::ac1f]:8001")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"),
										IPAddressAttribute
												.getInstance("[1eee:0:a88:85a5::ac1f]:8001")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
						new Object[] {
								NAME_DNSNAME_REGEXP_MATCH,
								Arrays.asList(StringAttribute
										.getInstance("\\.com$"),
										DNSNameAttribute
												.getInstance("thalesgroup.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DNSNAME_REGEXP_MATCH,
								Arrays.asList(StringAttribute
										.getInstance("\\.org$"),
										DNSNameAttribute
												.getInstance("thalesgroup.com")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
						new Object[] {
								NAME_RFC822NAME_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"),
										RFC822NameAttribute
												.getInstance("anne.anderson@sun.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"),
										RFC822NameAttribute
												.getInstance("anderson@sun.com")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
						new Object[] {
								NAME_X500NAME_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance(".*dc=example,dc=com"),
										X500NameAttribute
												.getInstance("ou=test,dc=example,dc=com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_REGEXP_MATCH,
								Arrays.asList(
										StringAttribute
												.getInstance(".*dc=example,dc=com"),
										X500NameAttribute
												.getInstance("ou=test,dc=sun,dc=com")),
								EvaluationResult.getInstance(false) });
	}

	public RegExpBasedFunctionsTest(String functionName,
			List<Evaluatable> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
