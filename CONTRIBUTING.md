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

You may build the project and generate the JAR as follows from your local copy of the repository:
<pre><code>
    $ mvn package
</code></pre>

Note that you must use Java 8 to run Maven when building the project.

### Dependency management
No SNAPSHOT dependencies allowed on "develop" and "master" branches.

### Releasing
1. From the develop branch, prepare a release (example using an HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-start
</code></pre>
1. Update the CHANGELOG according to keepachangelog.com.
2. To perform the release (example using an HTTP proxy):
   <pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-finish
   </code></pre>
    If, after deployment, the command does not succeed because of some issue with the branches. Fix the issue, then re-run the same command but with 'noDeploy' option set to true to avoid re-deployment:
   <pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 -DnoDeploy=true jgitflow:release-finish
   </code></pre>
   More info on jgitflow: http://jgitflow.bitbucket.org/
3. Connect and log in to the OSS Nexus Repository Manager: https://oss.sonatype.org/
4. Go to Staging Profiles and select the pending repository authzforce-*... you just uploaded with `jgitflow:release-finish`
5. Click the Release button to release to Maven Central.
6. Create a new Release on GitHub (copy-paste the description from previous releases and update the versions)
7. If the [PDP configuration XSD](pdp-engine/src/main/resources/pdp.xsd) has changed with the new release, publish the new schema document in HTML form on https://authzforce.github.io (example for XSD version 8.1) by following the instructions in the section below.

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
