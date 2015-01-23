-------------------------
AuthZForce CORE version @version- Release Notes
-------------------------

-------------------
-- Version @version
-------------------

-------------------
-- Version 3.5.5-SNAPSHOT
-------------------
License changed to GPLV3

-------------------
-- Version 3.5.2-SNAPSHOT
-------------------
Bug fixed when there were more than one AnyOf and AllOf. 
Only the Match element was evaluated with the "match(context)" function
Unitary tests were added to complete and prevent that from happening again

-------------------
-- Version 3.4.2
-------------------
Fixing bug on Rule Algorithm: 
	- DenyUnlessPermitRuleAlg.java
	- PermitUnlessDenyRuleAlg.java. 
=> A cast was misplaced and an error occured on the combination of rules

-------------------
-- Version 3.4.0
-------------------
Implementation working with XACML 3.0 requests and policies. Based on OASIS model (xsd)
Artifact name refactored => authzforce-core-authzforce in order to be more clear in the Nexus repository
Partial implementation of the Multi Decision Request. The Multi Request is not implemented yet
Functionnal tests added for XACML 3.0 model. This is actually the OASIS functionnal tests translated to a v3.O model
	BasicV3 	=> OK
	BasicFunctionV3	=> OK
	ConformanceV3	=> OK
Implementation of the "Include in result" attribute
Full support of obligations
Full support of advices
Apache 2.0 licence headers added to every source file
Audit log based on annotations for Rule and Policies. 
	Use @Audit(type = [RULE || POLICY]) over a method returning a result. You can also use @Audit(type = DISPLAY) to display and clear the logs.
Non exhaustif list of improvement and implementation 
Combining algorithm
OK      urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit
OK      urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit
OK      urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny
OK      urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny

Functions
OK      urn:oasis:names:tc:xacml:3.0:function:string-starts-with
OK      urn:oasis:names:tc:xacml:3.0:function:string-ends-with
OK      urn:oasis:names:tc:xacml:3.0:function:string-contains
OK	urn:oasis:names:tc:xacml:3.0:function:string-substring


-------------------
-- Version 2.1.4
-------------------
Stable version working with XACML 2.0
