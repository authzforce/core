When handling the same XACML Request twice in the same JVM with the root PolicySet using deny-unless-permit algorithm over a Policy returning simple Deny (no status/obligation/advice) and a Policy returning Permit/Deny with obligations/advice, the obligation is duplicated in the final result at the second time this situation occurs. 
Cause: the obligation/advice of the second policy is merged into the static variable systematically used for the simple Deny result. 

This folder is the first part of test which consists to evaluate the first request.