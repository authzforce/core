# Change log
All notable changes to this project are documented in this file following the conventions at keepachangelog.com.
This project adheres to [Semantic Versioning](http://semver.org).

## Unreleased


## 3.5.5
- License changed to GPLV3

## 3.5.2
- Bug fixed when there were more than one AnyOf and AllOf. 
- Only the Match element was evaluated with the "match(context)" function
- Unitary tests were added to complete and prevent that from happening again

## 3.4.2
- Fixing bugs on deny-unless-permit and permit-unless-deny rule combining algorithms (misplaced cast)

## 3.4.0
- Implementation working with XACML 3.0 requests and policies compliant with OASIS XACML model (xsd)
- Artifact name refactored => authzforce-core-authzforce
- Partial implementation of the Multiple Decision Profile. The MultiRequests scheme is not implemented yet
- Functionnal tests added for XACML 3.0 model. This is actually the OASIS functional tests translated to a v3.O model.
- Implementation of the "IncludeInResult" attribute
- Full support of obligations
- Full support of advices
- Apache 2.0 licence headers added to every source file
- XACML 3.0 Combining algorithms implemented: deny-unless-permit, deny-unless-permit, permit-unless-deny, permit-unless-deny
- XACML 3.0 Functions implemented: string-starts-with, string-ends-with, string-contains, string-substring, 

## 2.1.4
- Stable version working with XACML 2.0
