This is the directory for all non-regression tests, where each subdirectory corresponds to one test, named after the ID of the issue(s) (underscore-separated list if multiple) of the specific bug(s) in the issue management system (e.g. Gitlab, JIRA...) that this test reproduces before fixing. Therefore, this test validates that there have been no regression after fixing the bug. Tests in this directory are run by the class `com.thalesgroup.authzforce.core.test.nonregression.NonRegression.java` in folder  `src/test/java`. This class expects one subdirectory named `$issueId_$shortTitle` (e.g. 12_SomethingBrokenBecauseOfSomeOtherThing) under `src/test/resources/NonRegression` per test with the following content:

- `pdp.xml` (required) : PDP configuration file
- `pdp-ext.xsd` (optional): XSD for loading PDP extensions such as the TestAttributeFinder, required only if such extensions are used in the PDP configuration file 
- `policy.xml` (required): XACML Policy(Set) file
- `request.xml` (required): XACML Request
- `response.xml` (required): expected response for the test to succeed
- `README.md` (required): description of the test, mostly taken from the issue description and comments in the issue management system where the bug was reported.

If you implement or use a new PDP extension for testing, make sure there have a matching 'system' entry for the resolving the extension XSD location in the XML catalog file `src/test/resources/catalog.xml`, like the one for the TestAttributeFinder XSD.