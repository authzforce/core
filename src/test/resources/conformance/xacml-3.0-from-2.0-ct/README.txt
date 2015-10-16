OASIS XACML Committee's 2.0 version of conformance tests upgraded to conform to the XACML 3.0 standard. Most of them have been submitted to the OASIS XACML Committee in April 2014 by AT&T.
The original files are available on the xacml-comment mailing list:
https://lists.oasis-open.org/archives/xacml-comment/201404/msg00001.html
and on AT&T's Github repository (MIT License):
https://github.com/att/XACML/wiki/XACML-TEST-Project-Information

However, the files in this directory differ inasmuch as we have fixed some issues with some of the AT&T files that are not compliant with the XACML 3.0 XML schema, as of writing (26 September 2015). For instance, IIIA002Response.xml (and others) contains a 'FulfillOn' attribute on the Obligation elements, which used to be valid for XACML 2.0 but no longer for XACML 3.0.