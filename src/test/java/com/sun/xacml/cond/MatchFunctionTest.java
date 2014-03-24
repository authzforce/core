/**
 * 
 */
package com.sun.xacml.cond;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class MatchFunctionTest extends AbstractFunctionTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:x500Name-match
				new Object[] {
						MatchFunction.NAME_X500NAME_MATCH,
						Arrays.asList(new X500NameAttribute(new X500Principal(
								"O=Medico Corp,C=US")), new X500NameAttribute(
								new X500Principal(
										"cn=John Smith,o=Medico Corp, c=US"))),
						Status.STATUS_OK, BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_X500NAME_MATCH,
								Arrays.asList(
										new X500NameAttribute(
												new X500Principal(
														"O=Another Corp,C=US")),
										new X500NameAttribute(
												new X500Principal(
														"cn=John Smith,o=Medico Corp, c=US"))),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										"Anderson@sun.com"),
										new RFC822NameAttribute(
												"Anderson@sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										"Anderson@sun.com"),
										new RFC822NameAttribute(
												"Anderson@SUN.COM")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										"Anderson@sun.com"),
										new RFC822NameAttribute(
												"Anne.Anderson@sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										"Anderson@sun.com"),
										new RFC822NameAttribute(
												"anderson@sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										"Anderson@sun.com"),
										new RFC822NameAttribute(
												"Anderson@east.sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute("sun.com"),
										new RFC822NameAttribute(
												"Anderson@sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute("sun.com"),
										new RFC822NameAttribute(
												"Baxter@SUN.COM")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute("sun.com"),
										new RFC822NameAttribute(
												"Anderson@east.sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										".east.sun.com"),
										new RFC822NameAttribute(
												"Anderson@east.sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(
										new StringAttribute(".east.sun.com"),
										new RFC822NameAttribute(
												"anne.anderson@ISRG.EAST.SUN.COM")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_RFC822NAME_MATCH,
								Arrays.asList(new StringAttribute(
										".east.sun.com"),
										new RFC822NameAttribute(
												"Anderson@sun.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
						new Object[] {
								MatchFunction.NAME_STRING_REGEXP_MATCH,
								Arrays.asList(new StringAttribute("John.*"),
										new StringAttribute("John Doe")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_STRING_REGEXP_MATCH,
								Arrays.asList(new StringAttribute("John.*"),
										new StringAttribute("Jane Doe")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
						new Object[] {
								MatchFunction.NAME_ANYURI_REGEXP_MATCH,
								Arrays.asList(
										new StringAttribute("^http://.+"),
										new AnyURIAttribute(new URI(
												"http://www.thalesgroup.com"))),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_ANYURI_REGEXP_MATCH,
								Arrays.asList(
										new StringAttribute("^http://.+"),
										new AnyURIAttribute(new URI(
												"https://www.thalesgroup.com"))),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
						new Object[] {
								MatchFunction.NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										new StringAttribute(
												"^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"),
										IPAddressAttribute
												.getInstance("10.10.10.190")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								MatchFunction.NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(
										new StringAttribute(
												"^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"),
										IPAddressAttribute
												.getInstance("10.144.10.190")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) }
				// urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
				// urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
				// urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
				);
	}

	public MatchFunctionTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
