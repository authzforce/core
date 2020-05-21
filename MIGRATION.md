## Migration from v14.x to 15.x
- Modify the PDP configuration (XML): replace the XML namespace `http://authzforce.github.io/core/xmlns/pdp/7.0` with `http://authzforce.github.io/core/xmlns/pdp/7`.

## Migration from v13.x to v14.x
- Make sure all your custom PolicyProviders implement the new PolicyProvider interfaces, i.e. BaseStaticPolicyProvider or, as fallback option, CloseableStaticPolicyProvider
- Modify the PDP configuration (XML):
  - Merge 'rootPolicyProvider' and 'refPolicyprovider' into one 'policyProvider' using the new 'StaticPolicyProvider' type if you were using 'StaticRefPolicyprovider' or 'StaticRootPolicyProvider', else your new custom PolicyProvider types if you were using custom ones.
  - Add 'rootPolicyRef' element with policyId of the root policy.
  - If you are migrating from v13.2.0 or lower, and using either `TestAttributeProvider` or `MongoDBBasedPolicyProvider` types in XML namespace `http://authzforce.github.io/core/xmlns/test/3`, you must rename them to `TestAttributeProviderDescriptor` and `MongoDBBasedPolicyProviderDescriptor` respectively.
