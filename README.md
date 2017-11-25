[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dee3e6f5cdd240fc80dfdcc1ee419ac8)](https://www.codacy.com/app/coder103/authzforce-ce-core?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=authzforce/core&amp;utm_campaign=Badge_Grade)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/389/badge)](https://bestpractices.coreinfrastructure.org/projects/389)

# AuthzForce Core (Community Edition) 
Authorization PDP (Policy Decision Point) engine implementing the [OASIS XACML v3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

AuthzForce Core may be used in the following ways:
- Java API: you may use AuthzForce Core from your Java code to instantiate an embedded Java PDP. 
- CLI (Command-Line Interface): you may call AuthzForce Core PDP engine from the command-line (e.g. in a script) by running the provided executable.

*HTTP/REST API: if you are interested in using a HTTP/REST API compliant with [REST Profile of XACML 3.0](http://docs.oasis-open.org/xacml/xacml-rest/v1.0/xacml-rest-v1.0.html), check the [AuthZForce RESTful PDP project](http://github.com/authzforce/restful-pdp) and [AuthZForce server project](http://github.com/authzforce/server).*

## Features
* Compliance with the following OASIS XACML 3.0 standards:
  * [XACML v3.0 - Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) 
  * [XACML v3.0 - Core and Hierarchical Role Based Access Control (RBAC) Profile Version 1.0](http://docs.oasis-open.org/xacml/3.0/rbac/v1.0/xacml-3.0-rbac-v1.0.html)
  * [XACML v3.0 - Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories`).
  * [XACML v3.0 - JSON Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-json-http/v1.0/xacml-json-http-v1.0.html)
  * Experimental support for:
    * [XACML v3.0 - Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html): only `dnsName-value` datatype and `dnsName-value-equal` function are supported;
    * [XACML v3.0 - Additional Combining Algorithms Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html): `on-permit-apply-second` policy combining algorithm;
    * [XACML v3.0 - Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision`). 

  *For further details on what is actually supported with regards to the XACML specifications, please refer to the conformance tests [README](pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/README.md).*
* Interfaces: 
  * Java API: basically a library for instantiating and using a PDP engine from your Java (or any Java-compatible) code;
  * CLI (Command-Line Interface): basically an executable that you can run on from the command-line to test the engine;
  
  *The HTTP/REST API compliant with [REST Profile of XACML 3.0](http://docs.oasis-open.org/xacml/xacml-rest/v1.0/xacml-rest-v1.0.html) provided by the [AuthZForce RESTful PDP project](http://github.com/authzforce/restful-pdp) for PDP only, and [AuthZForce server project](http://github.com/authzforce/server) for PDP and PAP with multi-tenancy).*
* Safety/Security:
  * Prevention circular XACML policy references (PolicyIdReference/PolicySetIdReference) as mandated by [XACML 3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047192);
  * Control of the **maximum XACML PolicyIdReference/PolicySetIdReference depth**;
  * Prevention circular XACML variable references (VariableReference) as mandated by [XACML 3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047185); 
  * Control of the **maximum XACML VariableReference depth**;
* Optional **strict multivalued attribute parsing**: if enabled, multivalued attributes must be formed by grouping all `AttributeValue` elements in the same Attribute element (instead of duplicate Attribute elements); this does not fully comply with [XACML 3.0 Core specification of Multivalued attributes (§7.3.3)](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047176), but it usually performs better than the default mode since it simplifies the parsing of attribute values in the request.
* Optional **strict attribute Issuer matching**: if enabled, `AttributeDesignators` without Issuer only match request Attributes without Issuer (and same AttributeId, Category...); this option is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it is the recommended option when all AttributeDesignators have an Issuer (the XACML 3.0 specification (5.29) says: *If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone.*);
* Extensibility points:
  * **Attribute Datatypes**: you may extend the PDP engine with custom XACML attribute datatypes;
  * **Functions**: you may extend the PDP engine with custom XACML functions;
  * **Combining Algorithms**: you may extend the PDP engine with custom XACML policy/rule combining algorithms;
  * **Attribute Providers a.k.a. PIPs** (Policy Information Points): you may plug custom attribute providers into the PDP engine to allow it to retrieve attributes from other attribute sources (e.g. remote service) than the input XACML Request during evaluation; 
  * **Request Preprocessor**: you may customize the processing of XACML Requests before evaluation by the PDP core engine, e.g. used for supporting new XACML Request formats, and/or implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334);
  * **Result Postprocessor**: you may customize the processing of XACML Results after evaluation by the PDP engine, e.g. used for supporting new XACML Response formats, and/or implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890);
  * **Root Policy Provider**: you may plug custom policy providers into the PDP engine to allow it to retrieve the root policy from specific sources (e.g. remote service);
  * **Policy-by-reference Providers**: you may plug custom policy providers into the PDP engine to allow it to resolve `PolicyIdReference` or `PolicySetIdReference`;
  * **Decision Cache**: you may extend the PDP engine with a custom XACML decision cache, allowing the PDP to skip evaluation and retrieve XACML decisions from cache for recurring XACML Requests;
  * Java extension mechanism to switch HashMap/HashSet implementations (e.g. to get different performance results).
* PIP (Policy Information Point): AuthzForce provides XACML PIP features in the form of extensions called *Attribute Providers*. More information in the previous list on *Extensibility points*.


## Limitations
The following optional features from [XACML v3.0 Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) are not supported:
* Elements `AttributesReferences`, `MultiRequests` and `RequestReference`;
* Functions `urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal`, `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` and `urn:oasis:names:tc:xacml:3.0:function:access-permitted`;
* [Algorithms planned for future deprecation](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047257).
If you are interested in those, you can ask for [support](#Support).


## Versions
See the [change log file](CHANGELOG.md) following the *Keep a CHANGELOG* [conventions](http://keepachangelog.com/).

## License
See the [license file](LICENSE).

## Usage
### Getting started
#### CLI
Get the latest executable jar from Maven Central: groupId/artifactId = `org.ow2.authzforce`/`authzforce-ce-core-pdp-cli`.

Copy the content of [that folder](pdp-cli/src/test/resources/conformance/xacml-3.0-core/mandatory) to the same directory, and run the executable as follows:
```
$ ./authzforce-ce-core-pdp-cli-10.0.0.jar pdp.xml request.xml
```
`pdp.xml`: PDP configuration file
`request.xml`: XACML request in XACML 3.0/XML (core specification) format

If you want to test the JSON Profile of XACML 3.0, run it with extra option `-t XACML_JSON`:
```
$ ./authzforce-ce-core-pdp-cli-10.0.0.jar -t XACML_JSON pdp.xml request.json
```
`request.json`: XACML request in XACML 3.0/JSON (Profile) format

For more info, run it without parameters and you'll get detailed information on usage.

#### Java API
You can either build Authzforce PDP library from the source code after cloning this git repository, or use the latest release from Maven Central with this information:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-engine`;
* packaging: `jar`.

If you want to use the experimental features (see previous section) as well, you need to use an extra Maven dependency:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-testutils`;
* packaging: `jar`.

To get started using a PDP to evaluate XACML requests, instantiate a new PDP engine configuration with one of the methods: `org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration#getInstance(...)`. The parameters are:

1. *confLocation*: location of the configuration file (mandatory): this file must be an XML document compliant with the PDP configuration [XML schema](pdp-engine/src/main/resources/pdp.xsd). You can read the documentation of every configuration parameter in that file. If you don't use any XML-schema-defined PDP extension (AttributeProviders, PolicyProviders...), this is the only parameter you need, and you can use the simplest method `BasePdpEngine#getInstance(String confLocation)` to load your PDP. Here is an example of configuration:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <pdp xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://authzforce.github.io/core/xmlns/pdp/6.0" version="6.0.0">
	   <rootPolicyProvider id="rootPolicyProvider" xsi:type="StaticRootPolicyProvider" policyLocation="${PARENT_DIR}/policy.xml" />
   </pdp>
   ```
   This is a basic PDP configuration with basic settings and the root policy (XACML Policy document) loaded from a file `policy.xml` (see [this one](pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA001/IIA001Policy.xml) for an example) located in the same directory as this PDP configuration file. 
1. *catalogLocation*: location of the XML catalog (optional, required only if using one or more XML-schema-defined PDP extensions): used to resolve the PDP configuration schema and other imported schemas/DTDs, and schemas of any PDP extension namespace used in the configuration file. You may use the [catalog](pdp-engine/src/main/resources/catalog.xml) in the sources as an example. This is the one used by default if none specified.
1. *extensionXsdLocation*: location of the PDP extensions schema file (optional, required only if using one or more XML-schema-defined PDP extensions): contains imports of namespaces corresponding to XML schemas of all XML-schema-defined PDP extensions to be used in the configuration file. Used for validation of PDP extensions configuration. The actual schema locations are resolved by the XML catalog parameter. You may use the [pdp-ext.xsd](pdp-testutils/src/test/resources/pdp-ext.xsd) in the sources as an example.

As a result of `getInstance(...)`, you get an instance of `PdpEngineConfiguration`.

##### Evaluating Requests in AuthzForce native API (most efficient)
You can pass the `PdpEngineConfiguration` to `BasePdpEngine(PdpEngineConfiguration)` constructor in order to instantiate a PDP engine. With this, you can evaluate a decision request (more precisely an equivalent of a Individual Decision Request as defined by the XACML Multiple Decision Profile) in AuthzForce's native model by calling `evaluate(DecisionRequest)` or (multiple decision requests with `evaluate(List)`). In order to build a `DecisionRequest`, you may use the request builder returned by `BasePdpEngine#newRequestBuilder(...)`.  Please look at the Javadoc for more information.

##### Evaluating Requests in XACML/XML format
You can pass the `PdpEngineConfiguration` to `PdpEngineAdapters#newXacmlJaxbInoutAdapter(PdpEngineConfiguration)` utility method to instantiate a PDP supporting XACML 3.0/XML (core specification) format. You can evaluate such XACML Request by calling the `evaluate(...)` methods.

##### Evaluating Requests in XACML/JSON format
To instantiate a PDP supporting XACML 3.0/JSON (JSON Profile) format, you may reuse the test code from [PdpEngineXacmlJsonAdapters](pdp-io-xacml-json/src/test/java/org/ow2/authzforce/core/pdp/io/xacml/json/test/PdpEngineXacmlJsonAdapters.java).
You will need an extra dependency as well, available from Maven Central:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core-pdp-io-xacml-json`;
* packaging: `jar`.

##### Logging
Our PDP implementation uses SLF4J for logging so you can use any SLF4J implementation to manage logging. The CLI executable includes logback implementation, so you can use logback configuration file, e.g. [logback.xml](pdp-testutils/src/test/resources/logback.xml), for configuring loggers, appenders, etc.

##### Java 8+ external schema access restriction (workaround)
If you are using **Java 8**, make sure the following JVM argument is set before execution:
`-Djavax.xml.accessExternalSchema=http`

### Example of usage in a web service PEP
For an example of using an AuthzForce PDP engine in a real-life use case, please refer to the JUnit test class [EmbeddedPdpBasedAuthzInterceptorTest](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptorTest.java) and the Apache CXF authorization interceptor [EmbeddedPdpBasedAuthzInterceptor](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptor.java). The test class runs a test similar to @coheigea's [XACML 3.0 Authorization Interceptor test](https://github.com/coheigea/testcases/blob/master/apache/cxf/cxf-sts-xacml/src/test/java/org/apache/coheigea/cxf/sts/xacml/authorization/xacml3/XACML3AuthorizationTest.java) but using AuthzForce as PDP engine instead of OpenAZ. In this test, a web service client requests a Apache-CXF-based web service with a SAML token as credentials (previously issued by a Security Token Service upon successful client authentication) that contains the user ID and roles. Each request is intercepted on the web service side by a [EmbeddedPdpBasedAuthzInterceptor](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptor.java) that plays the role of PEP (Policy Enforcement Point in XACML jargon), i.e. it extracts the various authorization attributes (user ID and roles, web service name, operation...) and requests a decision from a local PDP with these attributes, then enforces the PDP's decision, i.e. forwards the request to the web service implementation if the decision is Permit, else rejects it.
For more information, see the Javadoc of  [EmbeddedPdpBasedAuthzInterceptorTest](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/pep/cxf/EmbeddedPdpBasedAuthzInterceptorTest.java).

## Extensions
If you are missing features in AuthzForce, you can extend it with various types of plugins (without changing the existing code), as described on the [wiki](../../wiki/Extensions).

## Support

If you are experiencing any issue with this project, please report it on the [OW2 Issue Tracker](https://jira.ow2.org/browse/AUTHZFORCE/).
Please include as much information as possible; the more we know, the better the chance of a quicker resolution:

* Software version
* Platform (OS and JDK)
* Stack traces generally really help! If in doubt include the whole thing; often exceptions get wrapped in other exceptions and the exception right near the bottom explains the actual error, not the first few lines at the top. It's very easy for us to skim-read past unnecessary parts of a stack trace.
* Log output can be useful too; sometimes enabling DEBUG logging can help;
* Your code & configuration files are often useful.

If you wish to contact the developers for other reasons, use [AuthzForce contact mailing list](http://scr.im/azteam).

## Vulnerability reporting
If you want to report a vulnerability, you must do so on the [OW2 Issue Tracker](https://jira.ow2.org/browse/AUTHZFORCE/) with *Security Level* set to **Private**. Then, if the AuthzForce team can confirm it, they will change it to **Public** and set a fix version.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).
