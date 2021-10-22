[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9c9812d7b09549e59edb99f3948bca4a)](https://www.codacy.com/gh/authzforce/core/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=authzforce/core&amp;utm_campaign=Badge_Grade)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/389/badge)](https://bestpractices.coreinfrastructure.org/projects/389)
[![Build Status](https://travis-ci.org/authzforce/core.svg?branch=develop)](https://travis-ci.org/authzforce/core)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fauthzforce%2Fcore.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fauthzforce%2Fcore?ref=badge_shield)

Javadocs: PDP engine [![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-core-pdp-engine.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-core-pdp-engine), XACML/JSON extension [![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-core-pdp-io-xacml-json.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-core-pdp-io-xacml-json), Test utilities [![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-core-pdp-testutils.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-core-pdp-testutils)

# AuthzForce Core (Community Edition) 
Authorization PDP (Policy Decision Point) engine implementing the [OASIS XACML v3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

AuthzForce Core may be used in the following ways:
- Java API: you may use AuthzForce Core from your Java code to instantiate an embedded Java PDP. 
- CLI (Command-Line Interface): you may call AuthzForce Core PDP engine from the command-line (e.g. in a script) by running the provided executable.

***HTTP/REST server**: if you are interested in using a HTTP/REST API compliant with [REST Profile of XACML 3.0](http://docs.oasis-open.org/xacml/xacml-rest/v1.0/xacml-rest-v1.0.html), check the [AuthzForce RESTful PDP project](http://github.com/authzforce/restful-pdp) and [AuthzForce server project](http://github.com/authzforce/server).*

## Features
* Compliance with the following OASIS XACML 3.0 standards:
  * [XACML v3.0 - Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) 
  * [XACML v3.0 - Core and Hierarchical Role Based Access Control (RBAC) Profile Version 1.0](http://docs.oasis-open.org/xacml/3.0/rbac/v1.0/xacml-3.0-rbac-v1.0.html)
  * [XACML v3.0 - Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories`).
  * [XACML v3.0 - JSON Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-json-http/v1.0/xacml-json-http-v1.0.html), with extra security features:
      * JSON schema [Draft v6](https://tools.ietf.org/html/draft-wright-json-schema-01) validation;
      * DoS mitigation: JSON parser variant checking max JSON string size, max number of JSON keys/array items and max JSON object depth.
  * Experimental support for:
    * [XACML v3.0 - Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html): only `dnsName-value` datatype and `dnsName-value-equal` function are supported;
    * [XACML v3.0 - Additional Combining Algorithms Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html): `on-permit-apply-second` policy combining algorithm;
    * [XACML v3.0 - Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision`). 

  *For further details on what is actually supported with regards to the XACML specifications, please refer to the conformance tests [README](pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/README.md).*
* [GeoXACML](http://portal.opengeospatial.org/files/?artifact_id=42734) (Open Geospatial Consortium) support: see [this AuthzForce extension from SecureDimensions](https://github.com/securedimensions/authzforce-geoxacml-basic).
* Interfaces: 
  * Java API: basically a library for instantiating and using a PDP engine from your Java (or any Java-compatible) code;
  * CLI (Command-Line Interface): basically an executable that you can run from the command-line to test the engine;
  
  *HTTP/REST API compliant with [REST Profile of XACML 3.0](http://docs.oasis-open.org/xacml/xacml-rest/v1.0/xacml-rest-v1.0.html) is provided by [AuthzForce RESTful PDP project](http://github.com/authzforce/restful-pdp) for PDP only, and [AuthzForce server project](http://github.com/authzforce/server) for PDP and PAP with multi-tenancy.*
* Safety & Security:
  * Prevention of circular XACML policy references (PolicyIdReference/PolicySetIdReference) as mandated by [XACML 3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047192);
  * Control of the **maximum XACML PolicyIdReference/PolicySetIdReference depth**;
  * Prevention of circular XACML variable references (VariableReference) as mandated by [XACML 3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047185); 
  * Control of the **maximum XACML VariableReference depth**;
* Performance:
  * Optional **strict multivalued attribute parsing**: if enabled, multivalued attributes must be formed by grouping all `AttributeValue` elements in the same Attribute element (instead of duplicate Attribute elements); this does not fully comply with [XACML 3.0 Core specification of Multivalued attributes (§7.3.3)](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047176), but it usually performs better than the default mode since it simplifies the parsing of attribute values in the request.
  * Optional **strict attribute Issuer matching**: if enabled, `AttributeDesignators` without Issuer only match request Attributes without Issuer (and same AttributeId, Category...); this option is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it is the recommended option for better performance when all AttributeDesignators have an Issuer (the XACML 3.0 specification (5.29) says: *If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone.*);
  * **Optimal integer data-type** implementation: the `maxIntegerValue` configuration parameter (expected maximum absolute value in XACML attributes of type `http://www.w3.org/2001/XMLSchema#integer`) helps the PDP choose the most efficient Java data-type. By default, the XACML/XML type `http://www.w3.org/2001/XMLSchema#integer` is mapped to the larger Java data-type: `BigInteger`. However, this may be overkill for example in the case of integer attributes representing the age of a person; in this case, the `Short` type is more appropriate and especially more efficient. Therefore, decreasing the `maxIntegerValue` value as much as possible, based on the range you expect your integer values to fit in, makes the PDP engine more efficient on integer handling: lower memory consumption, faster computations.
  * **Pluggable Decision Cache**: you can plug-in your own XACML Decision Cache mechanism to speed up evaluation of (repetitive) requests. See down below for more info (Decision Cache extension).
* Extensibility points:
  * **[Attribute Datatypes](https://github.com/authzforce/core/wiki/XACML-Data-Types)**: you may extend the PDP engine with custom XACML attribute datatypes;
  * **[Functions](https://github.com/authzforce/core/wiki/XACML-Functions)**: you may extend the PDP engine with custom XACML functions;
  * **[Combining Algorithms](https://github.com/authzforce/core/wiki/XACML-Combining-Algorithms)**: you may extend the PDP engine with custom XACML policy/rule combining algorithms;
  * **[Attribute Providers a.k.a. PIPs](https://github.com/authzforce/core/wiki/Attribute-Providers)** (Policy Information Points): you may plug custom attribute providers into the PDP engine to allow it to retrieve attributes from other attribute sources (e.g. remote service) than the input XACML Request during evaluation; 
  * **[Request Preprocessor](https://github.com/authzforce/core/wiki/XACML-Request-Preprocessors)**: you may customize the processing of XACML Requests before evaluation by the PDP core engine, e.g. used for supporting new XACML Request formats, and/or implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334);
  * **[Result Postprocessor](https://github.com/authzforce/core/wiki/XACML-Result-Postprocessors)**: you may customize the processing of XACML Results after evaluation by the PDP engine, e.g. used for supporting new XACML Response formats, and/or implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890);
  * **[Policy Provider](https://github.com/authzforce/core/wiki/Policy-Providers)**: you may plug custom policy providers into the PDP engine to allow it to resolve `PolicyIdReference` or `PolicySetIdReference`;
  * **Decision Cache**: you may extend the PDP engine with a custom XACML decision cache, allowing the PDP to skip evaluation and retrieve XACML decisions from cache for recurring XACML Requests;
  * Java [extension mechanism to switch HashMap/HashSet implementations](https://github.com/authzforce/core/wiki/Hashed-Collections) (e.g. to get different performance results).
* PIP (Policy Information Point): AuthzForce provides XACML PIP features in the form of extensions called *Attribute Providers*. More information in the previous list of *Extensibility points*.


## Limitations
The following optional features from [XACML v3.0 Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) are not supported:
* Elements `AttributesReferences`, `MultiRequests` and `RequestReference`;
* Functions `urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal`, `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` and `urn:oasis:names:tc:xacml:3.0:function:access-permitted`;
* [Algorithms planned for future deprecation](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047257).

If you are interested in those, you can ask for [support](#Support).


## Versions
See the [change log](CHANGELOG.md) following the *Keep a CHANGELOG* [conventions](http://keepachangelog.com/).

## License
See the [license file](LICENSE).

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fauthzforce%2Fcore.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fauthzforce%2Fcore?ref=badge_large)

## System requirements
Java (JRE) version: 11 or later. Java 8 is no longer supported.

## Usage
### Getting started
#### CLI
Get the latest executable jar from Maven Central by following the `authzforce-ce-core-pdp-cli` link on [the latest release page](https://github.com/authzforce/core/releases/latest), and make sure you are allowed to run it (it is a fully executable JAR), e.g. with command:

```
$ chmod a+x authzforce-ce-core-pdp-cli-14.0.0.jar
```

To give you an example on how to test a XACML Policy (or PolicySet) and Request, you may copy the content of [that folder](pdp-cli/src/test/resources/conformance/xacml-3.0-core/mandatory) to the same directory as the executable, and run the executable as follows:

```
$ ./authzforce-ce-core-pdp-cli-14.0.0.jar pdp.xml IIA001/Request.xml
```

* `pdp.xml`: PDP configuration file in XML format, that defines the location(s) of XACML policy(ies) and more; for more information about PDP configuration parameters, the configuration format is fully specified and documented in the [XML schema `pdp.xsd`](pdp-engine/src/main/resources/pdp.xsd), also available in a [more user-friendly HTML form](https://authzforce.github.io/pdp.xsd/7.1) (start with the `pdp` element as the root element in a PDP configuration). **Feel free to change the policy location to point to your own for testing.**
* `Request.xml`: XACML request in XACML 3.0/XML (core specification) format. **Feel free to replace with your own for testing.**

If you want to test the JSON Profile of XACML 3.0, run it with extra option `-t XACML_JSON`:
```
$ ./authzforce-ce-core-pdp-cli-14.0.0.jar -t XACML_JSON pdp.xml IIA001/Request.json
```
* `Request.json`: XACML request in XACML 3.0/JSON (Profile) format. **Feel free to replace with your own for testing.**

For more info, run it without parameters and you'll get detailed information on usage.

#### Java API
You can either build AuthzForce PDP library from the source code after cloning this git repository, or use the latest release from Maven Central with this information:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-engine`;
* packaging: `jar`.

Since this is a Maven artifact and it requires dependencies, you should build your application with a build tool that understands Maven dependencies (e.g. Maven or Gradle), and configure this artifact as a Maven dependency, for instance with Maven in the `pom.xml`:

```xml
...
      <dependency>
         <groupId>org.ow2.authzforce</groupId>
         <artifactId>authzforce-ce-core-pdp-engine</artifactId>
         <version>${latest.version}</version>
      </dependency>
...

```

To get started using a PDP to evaluate XACML requests, the first step is to write/get a XACML 3.0 policy. Please refer to [XACML v3.0 - Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) for the syntax. For a basic example, see [this one](pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA001/IIA001Policy.xml). 

Then instantiate a PDP engine configuration with method [PdpEngineConfiguration#getInstance(String)](pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/PdpEngineConfiguration.java#L663). The required parameter *confLocation* must be the location of the PDP configuration file. For more information about PDP configuration parameters, the configuration format is fully specified and documented in the [XML schema `pdp.xsd`](pdp-engine/src/main/resources/pdp.xsd), also available in a [more user-friendly HTML form](https://authzforce.github.io/pdp.xsd/7.1) (start with the `pdp` element as the root element in a PDP configuration). Here is a minimal example of configuration:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <pdp xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://authzforce.github.io/core/xmlns/pdp/7" version="7.1">
	  <policyProvider id="policyProvider" xsi:type="StaticPolicyProvider">
	    <policyLocation>${PARENT_DIR}/policy.xml</policyLocation>
	  </policyProvider>
   </pdp>
   ```
   This is a basic PDP configuration with basic settings and the root policy (XACML 3.0 Policy document) loaded from a file `policy.xml` located in the same directory as this PDP configuration file (see previous paragraph for an example of policy).

As a result of calling method `getInstance(...)`, you get a `PdpEngineConfiguration` object. Basic example of Java code using a PDP configuration file in some folder `/opt/authzforce`:

```java
final PdpEngineConfiguration pdpEngineConf = PdpEngineConfiguration.getInstance("file:///opt/authzforce/pdp.xml"); 
```

Then the next step depends on the kind of decision request you want to evaluate. The various alternatives are detailed in the next sections.

##### Evaluating Requests in AuthzForce native API (most efficient)
If you are creating decision requests internally, i.e. directly from your Java code (not from any data serialization format), you'd better use AuthzForce native interface.
You can pass the `PdpEngineConfiguration` to `BasePdpEngine(PdpEngineConfiguration)` constructor in order to instantiate a PDP engine. With this, you can evaluate a decision request (more precisely an equivalent of a Individual Decision Request as defined by the XACML Multiple Decision Profile) in AuthzForce's native model by calling `evaluate(DecisionRequest)` or (multiple decision requests with `evaluate(List)`). In order to build a `DecisionRequest`, you may use the request builder returned by `BasePdpEngine#newRequestBuilder(...)`. 

Basic example of Java code (based on previous line of code):

```java
...
/*
 * Create the PDP engine. You can reuse the same for all requests, so do it only once for all.
 */
final BasePdpEngine pdp = new BasePdpEngine(pdpEngineConf);
...

// Create the XACML request in native model
final DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(-1, -1);
/*
 * If you care about memory optimization (avoid useless memory allocation), make sure you know the (expected) number of XACML attribute categories and (expected) total number of attributes in the request, and use these as arguments to newRequestBuilder(int,int) method, instead of negative values like above.
 * e.g. 3 attribute categories, 4 total attributes in this case
 */
// final DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(3, 4);

// Add subject ID attribute (access-subject category), no issuer, string value "john"
final AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());
final AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue("john"));
requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

// Add subject role(s) attribute to access-subject category, no issuer, string value "boss"
final AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
final AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue("boss"));
requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);

// Add resource ID attribute (resource category), no issuer, string value "/some/resource/location"
final AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(XACML_3_0_RESOURCE.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());
final AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue("/some/resource/location"));
requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);

// Add action ID attribute (action category), no issuer, string value "GET"
final AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(XACML_3_0_ACTION.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_ACTION_ID.value());
final AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue("GET"));
requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

// No more attribute, let's finalize the request creation
final DecisionRequest request = requestBuilder.build(false);
// Evaluate the request
final DecisionResult result = pdp.evaluate(request);
if(result.getDecision() == DecisionType.PERMIT) {
	// This is a Permit :-)
	...
} else {
	// Not a Permit :-( (maybe Deny, NotApplicable or Indeterminate)
	...
}
```

See [EmbeddedPdpBasedAuthzInterceptor#createRequest(...) method](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptor.java#L158) for a more detailed example. Please look at the Javadoc for the full details.


##### Evaluating Requests in XACML/XML format
You can pass the `PdpEngineConfiguration` to `PdpEngineAdapters#newXacmlJaxbInoutAdapter(PdpEngineConfiguration)` utility method to instantiate a PDP supporting XACML 3.0/XML (core specification) format (`PdpEngineInoutAdapter<Request, Response>`), with which you can evaluate XACML Requests by calling its `evaluate(...)` methods.

##### Evaluating Requests in XACML/JSON format
To instantiate a PDP supporting XACML 3.0/JSON (JSON Profile) format, you may reuse the test code from [PdpEngineXacmlJsonAdapters](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/json/PdpEngineXacmlJsonAdapters.java), esp. the method `PdpEngineXacmlJsonAdapters#newXacmlJsonInoutAdapter(PdpEngineConfiguration)` which returns a `PdpEngineInoutAdapter<JSONObject,JSONObject>` on which you can call the `evaluate(...)` method with the JSON request as input.

You will need an extra dependency as well, available from Maven Central:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-io-xacml-json`;
* packaging: `jar`.

##### Evaluating Requests in other formats
You can support other non-XACML formats of access requests (resp. responses), including your own, by implementing your own [Request Preprocessor](https://github.com/authzforce/core/wiki/XACML-Request-Preprocessors) (resp. [Result Postprocessor](https://github.com/authzforce/core/wiki/XACML-Result-Postprocessors) ).

##### Logging
Our PDP implementation uses SLF4J for logging so you can use any SLF4J implementation to manage logging. The CLI executable includes logback implementation, so you can use logback configuration file, e.g. [logback.xml](pdp-testutils/src/test/resources/logback.xml), for configuring loggers, appenders, etc.


### Example of usage in a web service PEP
For an example of using an AuthzForce PDP engine in a real-life use case, please refer to the JUnit test class [EmbeddedPdpBasedAuthzInterceptorTest](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptorTest.java) and the Apache CXF authorization interceptor [EmbeddedPdpBasedAuthzInterceptor](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptor.java). The test class runs a test similar to @coheigea's [XACML 3.0 Authorization Interceptor test](https://github.com/coheigea/testcases/blob/master/apache/cxf/cxf-sts-xacml/src/test/java/org/apache/coheigea/cxf/sts/xacml/authorization/xacml3/XACML3AuthorizationTest.java) but using AuthzForce as PDP engine instead of OpenAZ. In this test, a web service client requests a Apache-CXF-based web service with a SAML token as credentials (previously issued by a Security Token Service upon successful client authentication) that contains the user ID and roles. Each request is intercepted on the web service side by a [EmbeddedPdpBasedAuthzInterceptor](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptor.java) that plays the role of PEP (Policy Enforcement Point in XACML jargon), i.e. it extracts the various authorization attributes (user ID and roles, web service name, operation...) and requests a decision from a local PDP with these attributes, then enforces the PDP's decision, i.e. forwards the request to the web service implementation if the decision is Permit, else rejects it.
For more information, see the Javadoc of  [EmbeddedPdpBasedAuthzInterceptorTest](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptorTest.java).

## Extensions
Experimental features (see [Features](#Features) section) are provided as extensions. If you want to use them, you need to use this Maven dependency (which depends on the `authzforce-ce-core-pdp-engine` already) instead:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-testutils`;
* packaging: `jar`

If you are still missing features in AuthzForce, you can make your own extensions/plugins (without changing the existing code), as described on the [wiki](../../wiki/Extensions).

If you are using the Java API with extensions configured by XML (Policy Providers, Attribute Providers...), you must use `PdpEngineConfiguration#getInstance(String, String, String)` to instantiate the PDP engine, instead of `PdpEngineConfiguration#getInstance(String)` mentioned previously. The two last extra parameters are mandatory in this case:
1. *catalogLocation*: location of the XML catalog: used to resolve the PDP configuration schema and other imported schemas/DTDs, and schemas of any PDP extension namespace used in the configuration file. You may use the [catalog](pdp-engine/src/main/resources/catalog.xml) in the sources as an example. This is the one used by default if none specified.
1. *extensionXsdLocation*: location of the PDP extensions schema file: contains imports of namespaces corresponding to XML schemas of all XML-schema-defined PDP extensions to be used in the configuration file. Used for validation of PDP extensions configuration. The actual schema locations are resolved by the XML catalog parameter. You may use the [pdp-ext.xsd](pdp-testutils/src/test/resources/pdp-ext.xsd) in the sources as an example.


## Support

You should use [AuthzForce users' mailing list](https://mail.ow2.org/wws/info/authzforce-users) as first contact for any communication about AuthzForce: question, feature request, notification, potential issue (unconfirmed), etc.

If you are experiencing any bug with this project and you indeed confirm this is not an issue with your environment (contact the users mailing list first if you are unsure), please report it on the [OW2 Issue Tracker](https://gitlab.ow2.org/authzforce/core/issues).
Please include as much information as possible; the more we know, the better the chance of a quicker resolution:

* Software version
* Platform (OS and JRE)
* Stack traces generally really help! If in doubt, include the whole thing; often exceptions get wrapped in other exceptions and the exception right near the bottom explains the actual error, not the first few lines at the top. It's very easy for us to skim-read past unnecessary parts of a stack trace.
* Log output can be useful too; sometimes enabling DEBUG logging can help;
* Your code & configuration files are often useful.

## Security - Vulnerability reporting
If you want to report a vulnerability, you must do so on the [OW2 Issue Tracker](https://gitlab.ow2.org/authzforce/core/issues) and **make sure the checkbox** *This issue is confidential and should only be visible to team members with at least Reporter access* **is checked when creating the issue**. Then, if the AuthzForce team can confirm it, they will uncheck it to make the issue public.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).
