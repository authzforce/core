# AuthZForce Core
PDP engine implementation of the XACML 3.0 Core and part of the Multiple Decision Profile (section 2.3, i.e. repetition of attribute categories) specifications. For further details on what is actually supported with regards to the specifications, please refer to `src/test/resources/conformance/xacml-3.0-from-2.0-ct/README.md`.

## Getting started
To get started using a PDP to evaluate XACML requests, instantiate a new PDP instance with one of the methods: `org.ow2.authzforce.core.PdpConfigurationParser#getPDP(...)`. The parameters are:

1. location of the configuration file (mandatory): this file must be an XML document compliant with schema `src/main/resources/pdp.xsd`. You can read the documentation of every configuration parameter in that file.
1. location of the XML catalog (optional, required only if using one or more XML-schema-defined PDP extensions): used to resolve the PDP configuration schema and other imported schemas/DTDs, and schemas of any PDP extension namespace used in the configuration file. An example of such file is located at `src/main/resources/catalog.xml`. This is the one used by default if none specified.
1. location of the PDP extensions schema file (optional, required only if using one or more PDP extensions): contains imports of namespaces corresponding to XML schemas of all XML-schema-defined PDP extensions to be used in the configuration file. Used for validation of PDP extensions configuration. The actual schema locations are resolved by the XML catalog parameter.

Once you have a PDP instance. You can evaluate a XACML request by calling one of the `PDP#evaluate(...)` methods.
