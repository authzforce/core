AuthZForce - Authorization Server
=================================
# INSTALLATION GUIDE
## Version
Version | Date | Comment           |
---------|:------:|:--------------:|
1.0 | 23/02/2012 | Initialisation | 
2.0 | 20/09/2013 | Update for the 3.0 version of the PDP |

# Summary
* Version
*  [Prerequisites](#prerequisites)
 *  [Sun Java JDK](#sun-java-jdk)
 *  [Tomcat Installation](#tomcat-installation)
*  [Installing the Authorization Server](#installing-the-authorisation-server)
 *  [Unitary Tests](#unitary-tests)
 *  [Conformance Tests](#conformance-tests)
 *  [Installation](#installation)
 *  [Installation Checking](#installation-checking)
*  [Authorization Server Configuration](#authorization-server-configuration)
 * [Policy Finder Configuration](#policy-finder-configuration)
 * [Attribute Finder Configuration File](#attribute-finder-configuration-file)
    * [JDBC](#jdbc)
    * [LDAP](#ldap)
	* [Fortress](#fortress)
	* [JWT](#jwt)
	* [String Map](#string-map)
	* [JSON Path](#json-path)
	* [RestFul](#restful)	
* [Calling the PDP](#calling-the-pdp)
 * [Test the PDP from a REST client](#test-the-pdp-from-a-rest-client)

# Prerequisites
## Sun Java JDK
The authorization server run on Java, so it is prerequisite to have java running on the server. For compatibility reasons, it is highly recommended to use Sun java instead of the Open Java that is now default for some Linux distributions. 
## Tomcat Installation 
To run the Policy decision Point, you also need a Tomcat Server to deploy the AuthZForce-REST-[VERSION].war (Tomcat 6/0 was our testing version but tomcat 7 can be used too).
# Installing the Authorization Server
## Unitary Tests
TODO
## Conformance Tests
TODO
## Installation
* /etc/AuthZForce/conf	Configuration files
*	log4j.properties: PDP log4j configuration file
*	config.xml: PDP configuration file
     * /etc/AuthZForce/policies	Contains an example policy
     * /etc/AuthZForce/logs	Logs files
*	pdp.log: PDP system logs
*	pdp-audit.log: Authorization decision audit logs

### Copy the default configuration file in this directory: 
## Installation Checking

Start the server by running this command: 
```bash
[root@ authzforce ~]# /opt/apache-tomcat-6.0.35/bin/catalina.sh start
````

Test that the PDP and the REST interface is running by doing a GET request to the
```bash
[root@ authzforce ~]# curl -X GET http://@IP:8080/AuthZForce-REST-[VERSION]/pdp/service
```
The Server should respond by a 200 status code and display "It Works !"

# Authorization Server Configuration
The authorization server configuration file is located in 
> /etc/AuthZForce/conf/config.xml

You can find below and example of configuration and a description of the different element. You can modify this configuration according to your situation:
```xml
<config defaultPDP="PDPDemo" defaultAttributeFactory="attr"
          defaultCombiningAlgFactory="comb" defaultFunctionFactory="func">
    
<pdp name="PDPDemo">

<!--******************Declaration of the Policy Finder******************************** -->
 <policyFinderModule class="com.sun.xacml.finder.impl.FilePolicyModule">
      <list>
         <string>/etc/AuthZForce/policies/policy-example.xml</string>
      </list>
 </policyFinderModule>

<!--******************Declaration of the Attribute Finder****************************** -->
   <attributeFinderModule class="com.sun.xacml.finder.impl.LdapAttributeFinder">
     <map>
          <url>ldap://11.6.207.31</url>
          <username>CN=Administrator,DC=TEST,DC=COM</username>
           <password>secret</password>
           <baseDN>OU=Users, DC=TEST,DC=COM </baseDN>
           <attributeSupportedId>subject-job</attributeSupportedId>
           <ldapAttribute>jobtitle</ldapAttribute>
           <substituteValue>urn:oasis:names:tc:xacml:1.0:subject:subject-id</substituteValue>
                  <cache class="com.sun.xacml.CacheManager">
                                <activate>false</activate>
                                <maxElementsInMemory>10000</maxElementsInMemory>
                                <overflowToDisk>false</overflowToDisk>
                                <eternal>true</eternal>
</cache>
     </map>
   </attributeFinderModule>
<!--******************Cache Configuration**************************************** -->
            <cache class="com.sun.xacml.CacheManager">
                    <map>
                        <activate>true</activate>
                        <maxElementsInMemory>10000</maxElementsInMemory>
                        <overflowToDisk>false</overflowToDisk>
                        <eternal>false</eternal>
                    </map>
            </cache>
 </pdp>
 <attributeFactory name="attr" useStandardDatatypes="true"/>
 <combiningAlgFactory name="comb" useStandardAlgorithms="true"/>
 <functionFactory name="func" useStandardFunctions="true">
 </functionFactory>
 </config>
 ````
 
## Policy Finder Configuration
The Policy Decision Point has the ability to load XACML policies from different locations and types of stores. In this version, policies correspond to a single file located on the server. 
```xml
<!--******************Declaration of the Policy Finder******************************** -->
 <policyFinderModule class="com.sun.xacml.finder.impl.FilePolicyModule">
      <list>
         <string>/etc/AuthZForce/policies/policy-example.xml</string>
         <string>/etc/AuthZForce/policies/policy-example-2.xml</string>
      </list>
 </policyFinderModule>
 ````

Once you have created your own policy, you will need to change this path to point to your policy file.

## Attribute Finder Configuration File
During an evaluation, the PDP may require other attributes that are not provided as part of the XACML request. To get those the PDP will ask the attribute finder(s) (configured below) to provide missing information. In this version, we provided two generic attribute finders that allow you to retrieve information from a LDAP directory and from a database.
### JDBC
```xml
<attributeFinderModule class="com.sun.xacml.finder.impl.AttributeDBFinder">
	<map>
		<url>jdbc:mysql://mysqlServer.opencloudware.org:3306/</url>
		<username>mysql</username>
		<password>password</password>
		<dbName>Customer </dbName>
		<driver>com.mysql.jdbc.Driver</driver>
		<attributeSupportedId>customer-id</attributeSupportedId>
		<sqlRequest>SELECT customer-id as $alias where sales-manager=$filter</sqlRequest>
		<substituteValue>urn:oasis:names:tc:xacml:1.0:subject-id:subject-id</substituteValue>
                                   <cache class="com.sun.xacml.CacheManager">
                                              <activate>false</activate>
                                              <maxElementsInMemory>10000</maxElementsInMemory>
                                              <overflowToDisk>false</overflowToDisk>
                                              <eternal>true</eternal>
                 </cache>
	</map>
</attributeFinderModule>
````

The possible configuration elements defined for this configuration type are:

Name | description |
---------|:------:|
url |  Address of the Database server|
username | Database username|
password | Database password |
dbName | Database name|
driver | Driver used to access the Database|
attributeSupportedId | Attribute that is supported by this attribute finder for retrieval. |
sqlRequest | Request used to fetch the attribute. $filter  is the variable part used to make a filter in the SQL query mapped to the AttributeValue in the XACML request defined with the substituteValue option. $alias is used to map easily the request’s result with the attributeSupportedId in order to have a more logical output |
substituteValue|Value extracted from the XACML request (Mandatory in the XACML request)|
Cache|cache configuration for this attribute finder, (Optional, false by default)|
activate|cache activation, true or false|
maxElementInMemory|max element that are stored in the cache memory, integer|
overflowToDisk|if cache can write on the disk if the memory is full, true or false|
eternal|do we store eternally the elements, true or false|
timeToLiveSeconds|time to live of the stored elements, integer, (Optional)|
timeToIdleSeconds|time to idle for the stored elements, integer, (Optional) |
### LDAP
```xml
<attributeFinderModule class="com.sun.xacml.finder.impl.LdapAttributeFinder">
       	<map>
       		<url>ldap://10.222.148.102</url>
      		<username>cn=Manager,c=gb</username>
      		<password>secret</password>
      		<baseDn>ou=people,dc=authzforce,dc=com</baseDn>
      		<ldapAttribute>title</ldapAttribute>   
      		<attributeSupportedId>jobtitle</attributeSupportedId>      		   	
      		<substituteValue>urn:oasis:names:tc:xacml:1.0:subject:subject-id</substituteValue>
                                   <cache class="com.sun.xacml.CacheManager">
                                              <activate>false</activate>
                                              <maxElementsInMemory>10000</maxElementsInMemory>
                                              <overflowToDisk>false</overflowToDisk>
                                              <eternal>true</eternal>
                 </cache>
      	</map>
</attributeFinderModule>
````
The possible configuration elements defined for this configuration type are:

Name | description |
---------|:------:|
url|Address of the LDAP directory|
username|Username to access the directory|
password|Password to access the directory|
baseDN|Specifies the DN of the node where the search would start. (For performance reasons, this DN should be as specific as possible.) The default value is the root of the directory tree. |
ldapAttribute|The name of the entry's attribute that we are going to get the value from. |
attributeSupportedId|Attribute that is supported by this attribute finder for retrieval. |
substituteValue|Value extracted from the XACML request (Mandatory in the XACML request)|
Cache|cache configuration for this attribute finder, (Optional, false by default)|
activate|cache activation, true or false|
maxElementInMemory|max element that are stored in the cache memory, integer|
overflowToDisk|if cache can write on the disk if the memory is full, true or false|
eternal|do we store eternally the elements, true or false|
timeToLiveSeconds|time to live of the stored elements, integer, (Optional)|
timeToIdleSeconds|time to idle for the stored elements, integer, (Optional) |

### Fortress
TODO

### JWT
TODO

### String Map
TODO

### JSON Path
TODO

### RESTFul
TODO

# Calling the PDP
## Test the PDP from a REST client

1. To test the REST API with AuthZForce you need to get an REST Client like this one for Firefox
> [Rest Client](https://addons.mozilla.org/en-US/firefox/addon/restclient/)

2. Prepare an XACML request fitting your Policy and paste it into your body's request, for us it looks like this:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<RequestType xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" ReturnPolicyIdList="false" CombinedDecision="false">
   <Attributes Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject">
      <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id" IncludeInResult="true">
         <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">bob</AttributeValue>
      </Attribute>
   </Attributes>
   <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
      <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
         <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#81325</AttributeValue>
      </Attribute>
   </Attributes>
<Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
      <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
         <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#75903</AttributeValue>
      </Attribute>
   </Attributes>
   <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action">
      <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" IncludeInResult="true">
         <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">Read</AttributeValue>
      </Attribute>
   </Attributes>
   <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action">
      <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" IncludeInResult="true">
         <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">Write</AttributeValue>
      </Attribute>
   </Attributes>
</RequestType>
````


3. Set the Headers' request with:
	> Content-type : application/xml

4. Send it using a POST request to your PDP's interface 
    > http://@IP:8080/AuthZForce-REST-[VERSION]/pdp/service).
The response should look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ResponseType xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
   <Result>
      <Decision>Deny</Decision>
      <Status>
         <StatusCode Value="urn:oasis:names:tc:xacml:1.0:status:ok" />
         <StatusDetail />
      </Status>      
      <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
         <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#81325</AttributeValue>
         </Attribute>
      </Attributes>
   </Result>
   <Result>
      <Decision>Deny</Decision>
      <Status>
         <StatusCode Value="urn:oasis:names:tc:xacml:1.0:status:ok" />
         <StatusDetail />
      </Status>      
      <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
         <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#75903</AttributeValue>
         </Attribute>
      </Attributes>
   </Result>
   <Result>
      <Decision>Deny</Decision>
      <Status>
         <StatusCode Value="urn:oasis:names:tc:xacml:1.0:status:ok" />
         <StatusDetail />
      </Status>
      <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
         <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#81325</AttributeValue>
         </Attribute>
      </Attributes>
   </Result>
   <Result>
      <Decision>Deny</Decision>
      <Status>
         <StatusCode Value="urn:oasis:names:tc:xacml:1.0:status:ok" />
         <StatusDetail />
      </Status>
      <Obligations>
         <Obligation ObligationId="urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA014:policyset:obligation-3">
            <AttributeAssignment AttributeId="urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA014:policyset:assignment2" DataType="http://www.w3.org/2001/XMLSchema#string">assignment2</AttributeAssignment>
         </Obligation>
      </Obligations>
      <AssociatedAdvice />
      <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
         <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="true">
            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">medicalReccord#75903</AttributeValue>
         </Attribute>
      </Attributes>
   </Result>
</ResponseType>
```

It’s of course a snippet of the response. That can be much more complex if you have Obligations or Advices in your policies. Check the Oasis specification for more explanation on these objects.
