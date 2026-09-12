## Contributing
### Coding Rules
Follow these Java coding guidelines:
* [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), except braces must follow the Allman style instead of K & R style;
* *Effective Java, Second Edition*, by Joshua Bloch;
* [Oracle Secure Coding Guidelines for Java SE](http://www.oracle.com/technetwork/java/seccodeguide-139067.html).

### Testing
For every new major functionality, there must be unit tests added to some unit test class that is part of the automated test suite of [pdp-engine's MainTest.java](pdp-engine/src/test/java/org/ow2/authzforce/core/pdp/impl/test/MainTest.java). If the functionality has any impact on XACML - any Request/Response/Policy(Set) element - processing and/or change XACML standard conformance in any way, make sure you add relevant integration and/or conformance tests to the test suite run by [pdp-testutils's MainTest.java](pdp-testutils/src/test/java/org/ow2/authzforce/core/pdp/testutil/test/MainTest.java).

You may run the tests as follows from your local copy of the repository:
<pre><code>
    $ mvn test
</code></pre>

### Building the project

#### Prerequisites

Building AuthzForce Core requires:

* JDK 17 or later;
* [Apache Maven](https://maven.apache.org/);
* an [NVD API key](https://nvd.nist.gov/developers/request-an-api-key) for the OWASP Dependency-Check vulnerability scan;
* a [Sonatype Guide personal access token](https://help.sonatype.com/en/manage-guide-user-tokens.html) for the OSS Index vulnerability scan.

The API key and personal access token are required only for the complete verification lifecycle. They are not required for a regular `mvn package` build.

#### Environment setup

Configure the NVD API key and Sonatype Guide token in your Maven `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>nvd-api</id>
      <password>YOUR_NVD_API_KEY</password>
    </server>
    <server>
      <id>sonatype-guide</id>
      <password>YOUR_SONATYPE_GUIDE_TOKEN</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>dependency-check-credentials</id>
      <properties>
        <nvdApiServerId>nvd-api</nvdApiServerId>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>dependency-check-credentials</activeProfile>
  </activeProfiles>
</settings>
```

Both credentials are stored as `password`; no `username` is required. The `sonatype-guide` token is used by the OSS Index analyzer configured in the parent POM. Do not commit either credential to this repository or pass it directly on the Maven command line, where it may be exposed in build logs.

#### Build commands

From the root of the cloned repository, run a regular build with:

```shell
mvn package
```

This compiles all modules, runs the tests, and creates the artifacts in each module's `target` directory.

To run the complete verification lifecycle, including the dependency vulnerability checks, run:

```shell
mvn verify
```

### Dependency management
No SNAPSHOT dependencies allowed on "develop" and "master" branches.

### Releasing
1. From the develop branch, prepare a release (example using an HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-start
</code></pre>
1. Update the CHANGELOG according to keepachangelog.com.
1To perform the release (example using an HTTP proxy):
   <pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-finish
   </code></pre>
    If, after deployment, the command does not succeed because of some issue with the branches. Fix the issue, then re-run the same command but with 'noDeploy' option set to true to avoid re-deployment:
   <pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 -DnoDeploy=true jgitflow:release-finish
   </code></pre>
   More info on jgitflow: http://jgitflow.bitbucket.org/
1. Connect and log in to the Maven Central Repository: https://central.sonatype.com/publishing/deployments
1. Go to Deployments and select the pending Deployment
1. Click the Publish button to release to Maven Central.
1. Create a new Release on GitHub (copy-paste the description from previous releases and update the versions)
1. If the [PDP configuration XSD](pdp-engine/src/main/resources/pdp.xsd) has changed with the new release, publish the new schema document in HTML form on https://authzforce.github.io (example for XSD version 8.1) by following the instructions in the section below.

### Updating the HTML documentation for the PDP configuration after updating the XSD 
Install FlexDoc/XML (tested with v1.12.2). 

Install openjfx (e.g. on Ubuntu/Debian):
```
$ sudo apt install openjfx
```

On Linux, modify the JAVA_HOME and CLASS_PATH variables in `.../flexdoc-xml-XXX/bin/linux/generator.sh`:

```
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
...
# Add JavaFX libraries to the classpath
CLASS_PATH="${FDH}/lib/xml-apis.jar:${FDH}/lib/xercesImpl.jar:${FDH}/lib/resolver.jar:${FDH}/lib/flexdoc-xml.jar:/usr/share/openjfx/lib/*"
```

Run FlexDoc generator from the XSD documentation directory `pdp.xsd/XXX` where `XXX` is the schema version:
```
$ git clone https://github.com/authzforce/authzforce.github.io.git
$ cd authzforce.github.io
$ mkdir -p pdp.xsd/7.1
$ /path/to/flexdoc-xml-XXX/bin/linux/generator.sh
```

In the Generator dialog, and specify:
- Template: `.../flexdoc-xml-XXX/templates/XSDDoc/FramedDoc.tpl`
  - Params: set *Generate Details / For Schemas / Exclude* parameter to `xacml-core-v3-schema-wd-17.xsd;xml.xsd`. OK.
- XML file: `https://raw.githubusercontent.com/authzforce/core/master/pdp-engine/src/main/resources/pdp.xsd`
  - Catalog: add the `catalog.xml`from the [repository](https://github.com/authzforce/authzforce.github.io.git) you just git cloned.
- Output format: HTML

Then hit Run.
