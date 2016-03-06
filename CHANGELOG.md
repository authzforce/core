# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. 

## Unreleased
### Added
- PDP XML schema: 'requestFilter' attribute (RequestFilter extension): 
	- Added documentation about natively supported values, with '-lax' suffix meaning that duplicate <Attribute> with same meta-data in the same <Attributes> element of a Request is allowed (in compliance with XACML 3.0 core spec, §7.3.3), and '-strict' suffix meaning that it is not allowed (not strictly compliant with XACML 3.0 Core, section 7.3.3):
		- 'urn:thalesgroup:xacml:request-filter:default-lax' and 'urn:thalesgroup:xacml:request-filter:default-strict': default requestFilter limited to what is specified in XACML 3.0 Core specification
		- 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories-lax' and 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories-strict': implement Multiple Decision Profile, section 2.3 (repeated attribute categories)
categories)
	- Added XSD-defined default value: 'urn:thalesgroup:xacml:request-filter:default-lax'
- Support for Extended Indeterminate values (XACML 3.0 Core specification, section 7.10-7.14, appendix C: combining algorithms)
- PdpImpl#getStaticRootAndRefPolicies() that provides all the PDP's root policy and policies referenced - directly or indirectly - from the root policy, if all are statically resolved. This allows PDP clients to know all the policies (if statically resolved) possibly used by the PDP during the evaluation.

## 3.7.0
### Added
- Root policy provider module based on any policy-by-reference provider (parameter is the root policy reference to be resolved by the policy-by-reference provider)

### Changed
- PDP configuration XSD version -> 3.6.1 (supporting new configuration type for the new ref-based Root policy provider module mentioned in previous section)

### Removed
- Moved/Refactored API classes sufficient for implementing PDP extensions (Datatypes, Functions, Policy/Attribute providers, etc.) to a separate project: authzforce-ce-core-pdp-api

### Fixed
- Broken validation of max policy reference depth


## 3.6.0
### Added
- Support all [XACML 3.0 conformance tests](https://lists.oasis-open.org/archives/xacml-comment/201404/msg00001.html) published by AT&T on XACML mailing list in March 2014, except IIA010, IIA012, IIA024, IID029, IID030, III.C.2, III.C.3, IIIE301, IIIE303, II.G.2-6 (see also [README](src\test\resources\conformance\xacml-3.0-from-2.0-ct\README.md) ); with specific adaptations and anhancements:
  1. XACML 3.0 Schema validation in all conformance tests (original files are not all compliant with XACML 3.0). 
  1. The original conformance test folder contains hundreds of files; for better readability and management, the folder is split in *mandatory* folder for tests on supported mandatory features (XACMl 3.0 core), *optional* folder for supported optional features (XACML 3.0 core and profiles), and *unsupported* for unsupported features.
  1. For tests requiring a custom attribute finder, added a file with suffix `AttributeProvider.xml` that configures the `TestAttributeProviderModule`. This configuration file must contain a list of `Attributes` elements defining the attributes that this attribute provider is able to provide, with their constant values.
  1. For tests requiring policies to be referenced via Policy(Set)IdReferences, added a directory named `refPolicies` containing a XACML Policy(Set) file per referenced Policy(Set).
  1. For tests of Request syntax validation (syntax error expected to be detected by Authzforce PDP at initialization-time, i.e. before any Request evaluation), added suffix `.ignore` to the original test Policy(Set) and Response files.
  1. For tests of Policy(Set) syntax validation (syntax error expected to be detected by Authzforce PDP at initialization-time, i.e. before any Request evaluation), added suffix `.ignore` to the original test Request and Response files.
- [HTML description](\src\test\resources\conformance\xacml-3.0-from-2.0-ct\ConformanceTests.html) of XACML 3.0 conformance tests
- Support of Policy(Set)Version in Policy(Set)IdReference handled by the native policy finder
- Support for Variable evaluation in Policy with scope management (variable is local to Policy where defined and inherited by Rules)
- Added support of xpathExpressions (optional XACML feature) in Request with support of namespace-prefix mappings extracted from XML document (XACML Request/Policy(Set)/Rule) (typically via `xmlns` declarations) where the xpathExpression is defined, e.g. XACML Request or Policy(Set).
- PDP configuration option to enable/disable XPath support (evaluation of xpathExpression datatype in Request/Policy(Set)/Rule, AttributeSelector and xpath functions)
- Added support of RequestDefaults/XPathVersion (optional XACML features) for evaluation of xpathExpressions in Request, and PolicyDefaults/XPathVersion (optional XACML feature) for evaluation of xpathExpressions and AttributeSelectors in Policy(Set) documents.
- Added support of ReturnPolicyIdList (optional XACML feature) to return identifiers of policies found applicable for the Request
- Added support of xpath-node-count function (optional XACML feature)
- New modes of request parsing/filtering and attribute matching to enforce best practices and optimize Request processing:
  1. *Strict Attribute Issuer match*: in this mode, an AttributeDesignator without Issuer only matches XACML Request Attributes without Issuer (faster if all Attributes have an Issuer which is recommended, but not fully XACML (§5.29) compliant)
  2. *Allow Attribute duplicates*: allows defining multi-valued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML Attributes element (same Category). Indeed, not allowing this enables the PDP to parse and evaluate Requests more efficiently, especially if you know the Requests to be well-formed, i.e. all AttributeValues of a given Attribute are grouped together in the same `<Attribute>` element. However, it may not be fully compliant with the XACML spec according to a [discussion](https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html) on the xacml-dev mailing list, referring to the XACML 3.0 core spec, §7.3.3, that indicates that multiple occurrences of the same `<Attribute>` with same meta-data but different values should be considered equivalent to a single `<Attribute>` element with same meta-data and merged values (multi-valued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in a multi-value bag during evaluation of the `<AttributeDesignator>`.
- Features to prevent circular references in Policy(Set)IdReferences or VariableReference
- Features to limit depth of PolicySetIdReference or VariableReference chain (otherwise no theoretical limit)

### Changed
- TestMatchAlg class replaced with official conformance test on Target matching: group II.B.
- Improved `TestUtils` class to allow configuring a directory of referenced policies for Policy(Set)IdReferences, to enable/disable XPath support, and to configure a specific RequestFilter ID, e.g. to use the MultipleDecisionProfile for conformance tests of 'optional' features.
- Renamed RELEASE-NOTES.md to CHANGELOG.md to adopt conventions from [keepachangelog.com](http://keepachangelog.com).
- Logback dependency scope (maven) from `compile` to `test` (not required for compiling, only for tests, any SLF4J-compatible library may be used at runtime).
- Moved old README.md content to the server project since it does not apply anymore to this project but to the AuthzForce server project.

### Fixed 
- Issues reported by PMD and findbugs
- Fixed issues in [XACML 3.0 conformance tests](https://lists.oasis-open.org/archives/xacml-comment/201404/msg00001.html) published by AT&T on XACML mailing list in March 2014, see [README](src\test\resources\conformance\xacml-3.0-from-2.0-ct\README.md).
- In logical OR, AND and N-OF functions, an Indeterminate argument results in Indeterminate result. 
  1. FIX for OR function: If at least one True argument, return True regardless of Indeterminate arguments; else (no True) if there is at least one Indeterminate, return Indeterminate, return Indeterminate; else (no True/Indeterminate -> all false) return false
  1. FIX for AND function: If at least one False argument, return False regardless of Indeterminate arguments; else (no False) if there is at least one Indeterminate, return Indeterminate, return Indeterminate; else (no False/Indeterminate -> all true) return true
  1. FIX for N-OF function: similar to OR but checking if there are at least N Trues instead of 1, in the remaining arguments; else there is/are n True(s) with `n < N`; if there are at least `(N-n)` Indeterminate, return Indeterminate; else return false.
- Misleading IllegalArgumentException error for XML-schema-valid anyURI but not valid for `java.net.URI` class. Fixed by using `java.lang.String` instead and validating strings according to anyURI definition with Saxon library
- RuntimeException when no subject and no resource and no action attributes in the XACML request

## 3.5.8 - 2015-04-01
### Added
- New XACML 3.0 versions of (ordered-)deny-overrides and (ordered-)permit-overrides combining algorithms (ALGORITHM IS NOT THE SAME as in XACML 2.0)

### Changed
- Renamed classes of XACML 1.0/2.0 combining algorithms (Ordered)DenyOverrides and (Ordered)PermitOverrides to Legacy*, and replaced with new XACML 3.0 versions

### Fixed
- Empty StatusDetail tag in Response when no StatusDetail (which is always the case as of now). Fix: remove the tag completely.

## 3.5.7 - 2015-03-13
### Changed
- Upraded version of maven-jaxb2-plugin to 0.12.3 for JAXB-annotated java class generation from OASIS XACML model

## 3.5.6 - 2015-02-27
### Added
- Generic test class for non-regression tests
- TestsAttributeFinder class for tests with a mock attribute finder (e.g. in non-regression tests)
- Functional unit test for Multiple Decision Profile with repeated categories (section 2.3 of XACML MDP)

### Changed
- Changed PDP *evaluate* method return type to standard XACML Response (JAXB-annotated)
- More explicit error messages for illegal parameters to functions: function ID, expected argument type, number of arguments, etc.

### Fixed
- NullPointerException with Indeterminate result of evaluating XACML AllOf or if no AllOf matches in a AnyOf
- NullPointerException when no resource-id attribute in XACML Request: 
- XACML Apply element marshalling (some elements were lost)

## 3.5.5 - 2015-01-26
### Added
- PDP configuration XML schema for configuration loading with JAXB and schema validation
- Framework for plugging PDP extensions (attribute/policy finders) by configuration, without re-compiling
- PDP Bean class usable as JNDI resource

### Changed
- License changed to GPLV3
- Upgrade code to use new Java 7 features
- Policy finder change: FilePolicyModule replaced with StaticPolicyFinderModule that supports loading policy files from any Spring-compatible resource URL

### Fixed
- Thread-local memory leak
- Empty Obligations/Associated Advice with permit|deny-unless-deny|permit combining algorithms

## 3.5.4 - 2014-12-23
### Added
- Unit tests for various match functions introduced in XACML 2.0 on strings, x509Names, rfc822Names, date/time, IP address
- Unit tests on date/time arithmetic functions, number arithmetic functions
- Unit tests for Set functions, higher-order bag functions
- Unit tests for logical functions
- Implementations of date/time artithemtic functions, number arithmetic, string-equal-*, higher-order bag functions
- Unit tests for "abstract" functions, e.g. 'map'
- Logback dependency for logging

### Changed
- Log formats

## 3.5.3 - 2013-12-16
### Added
- Support of Policy(Set)IdReference with StaticRefPolicyFinder class
- Support of dynamic obligations/advices containing AttributeDesignators or other expressions evaluated in the request context
- Enhanced debug logs in evaluation of Target, Policy(Set), Rule

## 3.5.2 - 2013-11-29
### Fixed
- Fixed bug when there were more than one AnyOf and AllOf: only the Match element was evaluated with the "match(context)" function

## 3.4.2 - 2013-07-03
### Fixed
- Fixing bugs on deny-unless-permit and permit-unless-deny rule combining algorithms (misplaced cast)

## 3.4.0 - 2013-05-30
### Added
- Implementation working with XACML 3.0 requests and policies compliant with OASIS XACML model (xsd)
- Partial implementation of the Multiple Decision Profile. The MultiRequests scheme is not implemented yet
- Functionnal tests added for XACML 3.0 model. This is actually the OASIS functional tests translated to a v3.O model.
- Implementation of the "IncludeInResult" attribute
- Support of XACML Obligations
- Support of XACML Advices
- Apache 2.0 licence headers added to every source file
- First implementation of XACML 3.0 Combining algorithms: deny-unless-permit, deny-unless-permit, permit-unless-deny, permit-unless-deny
- First implementation of XACML 3.0 Functions: string-starts-with, string-ends-with, string-contains, string-substring

## 3.3.1 - 2013-05-14
### Added
- New license headers and file for Apache 2 license

## 3.2.0 - 2013-05-13
### Added
- Support of XACML 3.0 Obligations/Advices in Rules
- Compliance with new conformance tests for 3.0 (converted from XACML 2.0 official category III.A)

## 3.1.0 - 2013-05-13
### Added
- Beta support of Multiple Decision profile, on repeated attribute categories only
- Beta support of XACML 3.0 Policy(Set)s and Obligations/Advices in Policy(Set)s

## 3.0.0 - 2013-04-05
### Added
- Preliminary support of XACML 3.0

