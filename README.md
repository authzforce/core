# AuthZForce Core
Authorization PDP (Policy Decision Point) engine implementing the [OASIS XACML v3.0](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

Java projects may use AuthZForce Core to instantiate an embedded Java PDP. 

*If you are interested in using a XACML PDP/PAP as a server with a RESTful API, go to the [AuthZForce server project](http://github.com/authzforce/server).*

## Features
* Compliance with the following OASIS XACML 3.0 standards:
  * [XACML v3.0 Core standard](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html): all mandatory and optional features are supported, **except**: 
    * Elements `AttributesReferences`, `MultiRequests` and `RequestReference`;
    * Functions `urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal`, `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` and `urn:oasis:names:tc:xacml:3.0:function:access-permitted`;
    * [Algorithms planned for future deprecation](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047257).
  * [XACML v3.0 Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories`). 
  * Experimental support for:
    * [XACML Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html): only `dnsName-value` datatype and `dnsName-value-equal` function are supported;
    * [XACML 3.0 Additional Combining Algorithms Profile Version 1.0](http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html): `on-permit-apply-second` policy combining algorithm;
    * [XACML v3.0 Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890)  (`urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision`). 

  *For further details on what is actually supported with regards to the XACML specifications, please refer to the conformance tests [README](src/test/resources/conformance/xacml-3.0-from-2.0-ct/README.md).*
* Control of the **maximum XACML VariableReference depth**;
* Control of the **maximum XACML PolicyIdReference/PolicySetIdReference depth**;
* Optional **strict multivalued attribute parsing**: if enabled, multivalued attributes must be formed by grouping all `AttributeValue` elements in the same Attribute element (instead of duplicate Attribute elements); this does not fully comply with [XACML 3.0 Core specification of Multivalued attributes (§7.3.3)](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047176), but it usually performs better than the default mode since it simplifies the parsing of attribute values in the request.
* Optional **strict attribute Issuer matching**: if enabled, `AttributeDesignators` without Issuer only match request Attributes without Issuer (and same AttributeId, Category...); this option is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it is the recommended option when all AttributeDesignators have an Issuer (the XACML 3.0 specification (5.29) says: *If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone.*);
* Extensibility points:
  * **Attribute Datatypes**: you may extend the PDP engine with custom XACML attribute datatypes;
  * **Functions**: you may extend the PDP engine with custom XACML functions;
  * **Combining Algorithms**: you may extend the PDP engine with custom XACML policy/rule combining algorithms;
  * **Attribute Providers**: you may plug custom attribute providers into the PDP engine to allow it to retrieve attributes from other attribute sources (e.g. remote service) than the input XACML Request during evaluation; 
  * **Request Filter**: you may customize the processing of XACML Requests before evaluation by the PDP core engine (e.g. used for implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Repeated attribute categories](http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html#_Toc388943334));
  * **Result Filter**: you may customize the processing of XACML Results after evaluation by the PDP engine (e.g. used for implementing [XACML v3.0 Multiple Decision Profile Version 1.0 - Requests for a combined decision](http://docs.oasis-open.org/xacml/3.0/xacml-3.0-multiple-v1-spec-cd-03-en.html#_Toc260837890));
  * **Root Policy Provider**: you may plug custom policy providers into the PDP engine to allow it to retrieve the root policy from specific sources (e.g. remote service);
  * **Ref Policy Providers**: you may plug custom policy providers into the PDP engine to allow it to resolve `PolicyIdReference` or `PolicySetIdReference`;
  * **Decision Cache**: you may extend the PDP engine with a custom XACML decision cache, allowing the PDP to skip evaluation and retrieve XACML decisions from cache for recurring XACML Requests;

## Versions
See the [change log file](CHANGELOG.md) according to the *Keep a CHANGELOG* [conventions](http://keepachangelog.com/).

## License
See the [license file](LICENSE.txt).

## Getting started
You can either build Authzforce PDP library from the source code after cloning this git repository, or use the latest release from Maven Central with this information:
* groupId: `org.ow2.authzforce`;
* artifactId: `authzforce-ce-core`;
* packaging: `jar`.
If you want to use the experimental features (see previous section) as well, you need to use an extra Maven dependency that has the same groupId/artifactId/packaging but a specific classifier: `tests`.

To get started using a PDP to evaluate XACML requests, instantiate a new PDP instance with one of the methods: `org.ow2.authzforce.core.pdp.impl.PdpConfigurationParser#getPDP(...)`. The parameters are:

1. Location of the configuration file (mandatory): this file must be an XML document compliant with the PDP configuration [XML schema](src/main/resources/pdp.xsd). You can read the documentation of every configuration parameter in that file.
1. Location of the XML catalog (optional, required only if using one or more XML-schema-defined PDP extensions): used to resolve the PDP configuration schema and other imported schemas/DTDs, and schemas of any PDP extension namespace used in the configuration file. You may use the [catalog](src/main/resources/catalog.xml) in the sources as an example. This is the one used by default if none specified.
1. Location of the PDP extensions schema file (optional, required only if using one or more PDP extensions): contains imports of namespaces corresponding to XML schemas of all XML-schema-defined PDP extensions to be used in the configuration file. Used for validation of PDP extensions configuration. The actual schema locations are resolved by the XML catalog parameter. You may use the [pdp-ext.xsd](src/test/resources/pdp-ext.xsd) in the sources as an example.

Once you have a PDP instance. You can evaluate a XACML request by calling one of the `PDP#evaluate(...)` methods.

Our PDP implementation uses SLF4J for logging so you can use any SLF4J implementation to manage logging. As an example, we use logback for testing, so you can use [logback.xml](src/test/resources/logback.xml) as an example for configuring loggers, appenders, etc.
