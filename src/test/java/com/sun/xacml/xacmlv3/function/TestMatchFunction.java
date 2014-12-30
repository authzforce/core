package com.sun.xacml.xacmlv3.function;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.IPv4AddressAttribute;
import com.sun.xacml.attr.PortRange;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.cond.MatchFunction;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

public class TestMatchFunction {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMatchFunction.class);
	
	private static final String FUNCTION_NS = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";
	private static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";
	
	private static final String IP_REGEX_PATTERN = 
			"^10\\.10\\.10\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])" +
			"/255\\.255\\.255\\.0" +
			":80$";
	
	private static final EvaluationCtx globalContext = TestUtils.createContext(new Request());

	/**
	 * Standard identifier for the regexp-string-match function.
	 */
	public static final String NAME_REGEXP_STRING_MATCH = FUNCTION_NS
			+ "regexp-string-match";

	/**
	 * Standard identifier for the x500Name-match function.
	 */
	public static final String NAME_X500NAME_MATCH = FUNCTION_NS
			+ "x500Name-match";

	/**
	 * Standard identifier for the rfc822Name-match function.
	 */
	public static final String NAME_RFC822NAME_MATCH = FUNCTION_NS
			+ "rfc822Name-match";

	/**
	 * Standard identifier for the string-regexp-match function. NOTE: this in
	 * the 1.0 namespace right now because of a bug in the XACML 2.0
	 * specification, but this will be changed to the 2.0 namespace as soon as
	 * the errata is recognized.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = FUNCTION_NS
			+ "string-regexp-match";

	/**
	 * Standard identifier for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = FUNCTION_NS_2
			+ "anyURI-regexp-match";

	/**
	 * Standard identifier for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = FUNCTION_NS_2
			+ "ipAddress-regexp-match";

	/**
	 * Standard identifier for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "dnsName-regexp-match";

	/**
	 * Standard identifier for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "rfc822Name-regexp-match";

	/**
	 * Standard identifier for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = FUNCTION_NS_2
			+ "x500Name-regexp-match";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = FUNCTION_NS_3
			+ "string-starts-with";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = FUNCTION_NS_3
			+ "string-ends-with";

	/**
	 * Standard identifier for the string-start-with function.
	 */
	public static final String NAME_STRING_CONTAINS = FUNCTION_NS_3
			+ "string-contains";

	@BeforeClass
	public static void setUp() {
		LOGGER.info("Begining testing for MatchFunctions");
		
		Set<String> testFunctions = new HashSet<String>();
		testFunctions.add(NAME_REGEXP_STRING_MATCH);
		testFunctions.add(NAME_X500NAME_MATCH);
		testFunctions.add(NAME_RFC822NAME_MATCH);
		testFunctions.add(NAME_STRING_REGEXP_MATCH);
		testFunctions.add(NAME_ANYURI_REGEXP_MATCH);
		testFunctions.add(NAME_IPADDRESS_REGEXP_MATCH);
		testFunctions.add(NAME_DNSNAME_REGEXP_MATCH);
		testFunctions.add(NAME_RFC822NAME_REGEXP_MATCH);
		testFunctions.add(NAME_X500NAME_REGEXP_MATCH);
		testFunctions.add(NAME_STRING_STARTS_WITH);
		testFunctions.add(NAME_STRING_ENDS_WITH);
		testFunctions.add(NAME_STRING_CONTAINS);
		
		if(LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Function to be tested");
	        for (String functionToBeTested : testFunctions) {
	        	LOGGER.debug(functionToBeTested);	
			}
        }
	}

	@Test
	public final void testRegexpStringMatch() {
		LOGGER.info("Testing function: " + NAME_REGEXP_STRING_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_REGEXP_STRING_MATCH);
		List<StringAttribute> goodInputs = new ArrayList<StringAttribute>(Arrays.asList(StringAttribute.getInstance("string$"), StringAttribute.getInstance("This is my test string")));
		List<StringAttribute> wrongInputs = new ArrayList<StringAttribute>(Arrays.asList(StringAttribute.getInstance("hello$"), StringAttribute.getInstance("This is my test string")));
				
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_REGEXP_STRING_MATCH + ": OK");
	}
	
	@Test
	public final void testX500NameMatch() {
						
		X500Principal x500PrincipalArg0Good = new X500Principal("O=Medico Corp,C=US");
		X500NameAttribute x500NameAttributeArg0Good = new X500NameAttribute(x500PrincipalArg0Good);
		
		X500Principal x500PrincipalArg0Wrong = new X500Principal("O=Medico Corp,C=FR");
		X500NameAttribute x500NameAttributeArg0Wrong = new X500NameAttribute(x500PrincipalArg0Wrong);
		
		X500Principal x500PrincipalArg1 = new X500Principal("cn=John Smith,o=Medico Corp, c=US");
		X500NameAttribute x500NameAttributeArg1 = new X500NameAttribute(x500PrincipalArg1);
		
		LOGGER.info("Testing function: " + NAME_X500NAME_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_X500NAME_MATCH);
		
		List<X500NameAttribute> goodInputs = new ArrayList<X500NameAttribute>();
		goodInputs.add(x500NameAttributeArg0Good);
		goodInputs.add(x500NameAttributeArg1);

		List<X500NameAttribute> wrongInputs = new ArrayList<X500NameAttribute>();
		wrongInputs.add(x500NameAttributeArg0Wrong);
		wrongInputs.add(x500NameAttributeArg1);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_X500NAME_MATCH + ": OK");
	}
	
	@Test
	public final void testNameRFC822NameMatch() {
						
		LOGGER.info("Testing function: " + NAME_RFC822NAME_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_RFC822NAME_MATCH);
		
		//In order to match a particular address in the second argument, the first argument must specify the 
		// complete mail address to be matched. For example, if the first argument is 
		//“Anderson@sun.com”, this matches a value in the second argument of “Anderson@sun.com” 
		//and “Anderson@SUN.COM”, but not “Anne.Anderson@sun.com”, “anderson@sun.com” or “Anderson@east.sun.com”. 
		
		LOGGER.info(NAME_RFC822NAME_MATCH +"function: Testing to match a particular address in the second argument, the first argument specify the complete mail address to be matched");
		
		StringAttribute stringArg0 = new StringAttribute("Anderson@sun.com");
		RFC822NameAttribute rfc822Arg1Good1 = new RFC822NameAttribute("Anderson@sun.com");
		RFC822NameAttribute rfc822Arg1Good2 = new RFC822NameAttribute("Anderson@SUN.COM");
		RFC822NameAttribute rfc822Arg1Wrong1 = new RFC822NameAttribute("Anne.Anderson@sun.com");
		RFC822NameAttribute rfc822Arg1Wrong2 = new RFC822NameAttribute("anderson@sun.com");
		RFC822NameAttribute rfc822Arg1Wrong3 = new RFC822NameAttribute("Anderson@east.sun.com");

		List<Object> goodInputs1 = new ArrayList<Object>();
		goodInputs1.add(stringArg0);
		goodInputs1.add(rfc822Arg1Good1);
		
		List<Object> goodInputs2 = new ArrayList<Object>();
		goodInputs2.add(stringArg0);
		goodInputs2.add(rfc822Arg1Good2);
		
		List<Object> wrongInputs1 = new ArrayList<Object>();
		wrongInputs1.add(stringArg0);
		wrongInputs1.add(rfc822Arg1Wrong1);
		
		List<Object> wrongInputs2 = new ArrayList<Object>();
		wrongInputs2.add(stringArg0);
		wrongInputs2.add(rfc822Arg1Wrong2);
		
		List<Object> wrongInputs3 = new ArrayList<Object>();
		wrongInputs3.add(stringArg0);
		wrongInputs3.add(rfc822Arg1Wrong3);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs1, globalContext).getAttributeValue()).encode()));
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs2, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs1, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs2, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs3, globalContext).getAttributeValue()).encode()));

		// In order to match any address at a particular domain in the second argument, the first argument 
		//must specify only a domain name (usually a DNS name). For example, if the first argument is 
		//“sun.com”, this matches a value in the second argument of “Anderson@sun.com” or 
		//“Baxter@SUN.COM”, but not “Anderson@east.sun.com”. 
		
		LOGGER.info(NAME_RFC822NAME_MATCH +"function: Testing to match any address at a particular domain in the second argument, the first argument specify only a domain name (usually a DNS name)");

		stringArg0 = new StringAttribute("sun.com");
		rfc822Arg1Good1 = new RFC822NameAttribute("Anderson@sun.com");
		rfc822Arg1Good2 = new RFC822NameAttribute("Baxter@SUN.COM");
		rfc822Arg1Wrong1 = new RFC822NameAttribute("Anderson@east.sun.com");

		goodInputs1 = new ArrayList<Object>();
		goodInputs1.add(stringArg0);
		goodInputs1.add(rfc822Arg1Good1);
		
		goodInputs2 = new ArrayList<Object>();
		goodInputs2.add(stringArg0);
		goodInputs2.add(rfc822Arg1Good2);
		
		wrongInputs1 = new ArrayList<Object>();
		wrongInputs1.add(stringArg0);
		wrongInputs1.add(rfc822Arg1Wrong1);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs1, globalContext).getAttributeValue()).encode()));
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs2, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs1, globalContext).getAttributeValue()).encode()));	
		
		
		//In order to match any address in a particular domain in the second argument, the first argument 
		//must specify the desired domain-part with a leading ".". For example, if the first argument is 
		//“.east.sun.com”, this matches a value in the second argument of "Anderson@east.sun.com" and 
		//"anne.anderson@ISRG.EAST.SUN.COM" but not "Anderson@sun.com".
	
		LOGGER.info(NAME_RFC822NAME_MATCH +"function: Testing to match any address in a particular domain in the second argument,the first argument specify the desired domain-part with a leading . ");

		stringArg0 = new StringAttribute(".east.sun.com");
		rfc822Arg1Good1 = new RFC822NameAttribute("Anderson@east.sun.com");
		rfc822Arg1Good2 = new RFC822NameAttribute("anne.anderson@ISRG.EAST.SUN.COM");
		rfc822Arg1Wrong1 = new RFC822NameAttribute("Anderson@sun.com");

		goodInputs1 = new ArrayList<Object>();
		goodInputs1.add(stringArg0);
		goodInputs1.add(rfc822Arg1Good1);
		
		goodInputs2 = new ArrayList<Object>();
		goodInputs2.add(stringArg0);
		goodInputs2.add(rfc822Arg1Good2);
		
		wrongInputs1 = new ArrayList<Object>();
		wrongInputs1.add(stringArg0);
		wrongInputs1.add(rfc822Arg1Wrong1);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs1, globalContext).getAttributeValue()).encode()));
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs2, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs1, globalContext).getAttributeValue()).encode()));	
		
		LOGGER.info("Function: " + NAME_RFC822NAME_MATCH + ": OK");
	}	
	
	@Test
	public final void testNameStringRegexpMatch() {
						
		LOGGER.info("Testing function: " + NAME_STRING_REGEXP_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_STRING_REGEXP_MATCH); 
				
		StringAttribute stringArg0 = new StringAttribute("^[0-9a-zA-Z]+@acme.com");
		StringAttribute stringGood = new StringAttribute("john.doe@acme.com");
		StringAttribute stringWrong = new StringAttribute("john.doe@acme.com");

		List<StringAttribute> goodInputs = new ArrayList<StringAttribute>();
		goodInputs.add(stringArg0);
		goodInputs.add(stringGood);
		
		List<StringAttribute> wrongInputs = new ArrayList<StringAttribute>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(stringWrong);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
	
		LOGGER.info("Function: " + NAME_STRING_REGEXP_MATCH + ": OK");
	}	
	
	@Test
	public final void testNameAnyURIRegexpMatch() {
						
		LOGGER.info("Testing function: " + NAME_ANYURI_REGEXP_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_ANYURI_REGEXP_MATCH); 
				
		StringAttribute stringArg0 = new StringAttribute("^http://.+");
		AnyURIAttribute stringGood = new AnyURIAttribute(URI.create("http://www.acme.com"));
		AnyURIAttribute stringWrong = new AnyURIAttribute(URI.create("https://www.acme.com"));

		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(stringGood);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(stringWrong);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
	
		LOGGER.info("Function: " + NAME_ANYURI_REGEXP_MATCH + ": OK");
	}	
	
	@Test
	public final void testNameIPAddressRegexpMatch() {
						
		LOGGER.info("Testing function: " + NAME_IPADDRESS_REGEXP_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_IPADDRESS_REGEXP_MATCH); 
			
		//TEST with IPV4
		LOGGER.info("Testing function: " + NAME_IPADDRESS_REGEXP_MATCH+", with IP V4 Address");

		StringAttribute stringArg0 = new StringAttribute(IP_REGEX_PATTERN);
		
		IPAddressAttribute ipv4AddressGood  = null;
		try {
			byte[] ipGoodAddrByte = new byte[]{10, 10 , 10 , 10 };
			InetAddress ipGood = InetAddress.getByAddress(ipGoodAddrByte);
			byte[] ipGoodMaskByte = new byte[]{(byte)255, (byte)255 , (byte)255 , 0 };
			InetAddress ipmaskGood = InetAddress.getByAddress(ipGoodMaskByte);
			PortRange portGood = new PortRange(80);
			ipv4AddressGood = new IPv4AddressAttribute(ipGood,ipmaskGood,portGood);
		} catch (UnknownHostException e1) {
			LOGGER.error("Exception: "+e1);
			e1.printStackTrace();
		}
		
		IPAddressAttribute ipv4AddressWrong  = null;
		try {
			byte[] ipWrongAddrByte = new byte[]{(byte)192, (byte)168 , 1 , 10 };
			InetAddress ipWrong = InetAddress.getByAddress(ipWrongAddrByte);			
			byte[] ipWrongMaskByte = new byte[]{(byte)255, (byte)255 , (byte)255 , 0 };
			InetAddress ipmaskWrong = InetAddress.getByAddress(ipWrongMaskByte);
			PortRange portWrong = new PortRange(8080);
			ipv4AddressWrong = new IPv4AddressAttribute(ipWrong,ipmaskWrong,portWrong);
		} catch (UnknownHostException e) {
			LOGGER.error("Exception: "+e);
			e.printStackTrace();
		}
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(ipv4AddressGood);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(ipv4AddressWrong);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_IPADDRESS_REGEXP_MATCH +" with IP V4 Address"+ ": OK");

		//TODO: Test with IPV6 Address		
	}	
	
	@Test
	public final void testNameStringContainsMatch() {
		
		LOGGER.info("Testing function: " + NAME_STRING_CONTAINS);
		MatchFunction testMatchFunction = new MatchFunction(NAME_STRING_CONTAINS); 			
		
		StringAttribute stringArg0 = new StringAttribute("test");
		StringAttribute goodStringArg1 = new StringAttribute("testing");
		StringAttribute wrongStringArg1 = new StringAttribute("tasting");
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(goodStringArg1);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(wrongStringArg1);
		
		//FIXME:  SHALL return a "http://www.w3.org/2001/XMLSchema#boolean". The result SHALL be true if the second string 
		//contains the first string, and false otherwise
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_STRING_CONTAINS +": OK");				
		
	}	
	
	@Test
	public final void testNameStringEndsWithMatch() {
		
		LOGGER.info("Testing function: " + NAME_STRING_ENDS_WITH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_STRING_ENDS_WITH); 			
		
		StringAttribute stringArg0 = new StringAttribute("ing");
		StringAttribute goodStringArg1 = new StringAttribute("testing");
		StringAttribute wrongStringArg1 = new StringAttribute("testang");
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(goodStringArg1);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(wrongStringArg1);

		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_STRING_ENDS_WITH +": OK");				
	}	
	
	@Test
	public final void testNameStringStartsWithMatch() {
		
		LOGGER.info("Testing function: " + NAME_STRING_STARTS_WITH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_STRING_STARTS_WITH); 			
		
		StringAttribute stringArg0 = new StringAttribute("test");
		StringAttribute goodStringArg1 = new StringAttribute("testing");
		StringAttribute wrongStringArg1 = new StringAttribute("tasting");
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(goodStringArg1);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(wrongStringArg1);

		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_STRING_STARTS_WITH +": OK");				
	}		
		
	@Test
	public final void testNameDnsNameRegexpMatch() {
	}
	
	@Test
	public final void testNameX500NameRegexpMatch() {
		
		LOGGER.info("Testing function: " + NAME_X500NAME_REGEXP_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_X500NAME_REGEXP_MATCH);
		
		StringAttribute stringArg0 = new StringAttribute(".*dc=example,dc=com");
		
		X500Principal x500PrincipalArg0Good = new X500Principal("ou=test,dc=example,dc=com");
		X500NameAttribute x500NameAttributeArg0Good = new X500NameAttribute(x500PrincipalArg0Good);
		
		X500Principal x500PrincipalArg0Wrong = new X500Principal("ou=test,dc=example,dc=fr");
		X500NameAttribute x500NameAttributeArg0Wrong = new X500NameAttribute(x500PrincipalArg0Wrong);	
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(x500NameAttributeArg0Good);

		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(x500NameAttributeArg0Wrong);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_X500NAME_REGEXP_MATCH + ": OK");
	}

	@Test
	public final void testNameRFC822NameRegexpMatch() {
						
		LOGGER.info("Testing function: " + NAME_RFC822NAME_REGEXP_MATCH);
		MatchFunction testMatchFunction = new MatchFunction(NAME_RFC822NAME_REGEXP_MATCH);
			
		StringAttribute stringArg0 = new StringAttribute("^[0-9a-zA-Z]+@acme.com");
		RFC822NameAttribute rfc822Arg1Good = new RFC822NameAttribute("john.doe@acme.com");
		RFC822NameAttribute rfc822Arg1Wrong = new RFC822NameAttribute("john.doe@acme.com");
		
		List<Object> goodInputs = new ArrayList<Object>();
		goodInputs.add(stringArg0);
		goodInputs.add(rfc822Arg1Good);
		
		List<Object> wrongInputs = new ArrayList<Object>();
		wrongInputs.add(stringArg0);
		wrongInputs.add(rfc822Arg1Wrong);
		
		Assert.assertTrue(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(goodInputs, globalContext).getAttributeValue()).encode()));
		Assert.assertFalse(Boolean.parseBoolean(((BooleanAttribute)testMatchFunction.evaluate(wrongInputs, globalContext).getAttributeValue()).encode()));
		
		LOGGER.info("Function: " + NAME_RFC822NAME_REGEXP_MATCH + ": OK");

	}
}