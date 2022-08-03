## Migration from version 17.x to 18.x and later
- You have to upgrade your PDP configuration(s), e.g. `pdp.xml` files, using XSLT stylesheet [pdp-xsd-v7.xsl](migration/pdp-xsd-v7.xsl) and any XSLT engine supporting XSLT 2.0, e.g. [SAXON-HE 9.x or later](https://www.saxonica.com/download/java.xml), e.g. with this command:
```shell
$ PDP_XML_FILE="pdp.xml"
$ mv $PDP_XML_FILE{,.old}
$  java -jar Saxon-HE-10.3.jar -xsl:migration/pdp-xsd-v7.xsl -s:$PDP_XML_FILE.old -o:$PDP_XML_FILE
```

## Migration from version 16.x to 17.x
- If you are still using Java 8, you have to upgrade to Java 11 (Java 8 is no longer supported).

## Migration from version 15.x to 16.x
- If you have any custom PolicyProvider extension, you need to update the implementation of the method `CloseablePolicyProvider.Factory#getInstance(...)` to the new PDP API (`authzforce-ce-core-pdp-api`: 17.0.0).

## Migration from version  14.x to 15.x
- Modify the PDP configuration (XML): replace the XML namespace `http://authzforce.github.io/core/xmlns/pdp/7.0` with `http://authzforce.github.io/core/xmlns/pdp/7`.

## Migration from version 13.x to 14.x
- Make sure all your custom PolicyProviders implement the new PolicyProvider interfaces, i.e. BaseStaticPolicyProvider or, as fallback option, CloseableStaticPolicyProvider
- Modify the PDP configuration (XML):
  - Merge 'rootPolicyProvider' and 'refPolicyprovider' into one 'policyProvider' using the new 'StaticPolicyProvider' type if you were using 'StaticRefPolicyprovider' or 'StaticRootPolicyProvider', else your new custom PolicyProvider types if you were using custom ones.
  - Add 'rootPolicyRef' element with policyId of the root policy.
  - If you are migrating from v13.2.0 or lower, and using either `TestAttributeProvider` or `MongoDBBasedPolicyProvider` types in XML namespace `http://authzforce.github.io/core/xmlns/test/3`, you must rename them to `TestAttributeProviderDescriptor` and `MongoDBBasedPolicyProviderDescriptor` respectively.
