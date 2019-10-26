## Migration from v13.x to v14.x
- Make sure all your custom PolicyProviders implement the new PolicyProvider interfaces, i.e. BaseStaticPolicyProvider or, as fallback option, CloseableStaticPolicyProvider
- Modify the PDP configuration (XML):
  - Merge 'rootPolicyProvider' and 'refPolicyprovider' into one 'policyProvider' using the new 'StaticPolicyProvider' type if you were using 'StaticRefPolicyprovider' or 'StaticRootPolicyProvider', else your new custom PolicyProvider types if you were using custom ones.
  - Add 'rootPolicyRef' element with policyId of the root policy.