<?xml version="1.1" encoding="UTF-8"?>
<!-- Copyright (C) 2019 Thales. This file is part of AuthZForce CE. Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. -->
<!-- Common part of transformation of XACML 3.0/XML Request, Response or Policy to JSON; this version outputs all JSON property names in lower camel case -->
<!-- Author: Cyril DANGERVILLE -->
<!-- XSLT v3.0+ required for 'xsl:map' function support -->
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xacml="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
	xpath-default-namespace="http://www.w3.org/2005/xpath-functions" xmlns:map="http://www.w3.org/2005/xpath-functions/map" xmlns:azf="http://authzforce.github.io/xmlns/xsl">
	<xsl:output encoding="UTF-8" method="text" omit-xml-declaration="yes" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Parameters to be specified in the enclosing stylesheet -->
	<!-- <xsl:param name="useJsonProfile" select="'yes'" /> -->

	<!-- Global variables -->
	<!-- Present XACML/JSON Profile compatibility -->
	<xsl:variable name="jsonProfileCompat" select="if ($useJsonProfile = 'yes') then true() else false()" />

	<xsl:variable name="json-string-replacements" as="map(xs:string, xs:string)">
		<!-- Characters that must be escaped according to RFC 8259 (IETF), section 7 -->
		<xsl:map>
			<!-- Quotation mark -->
			<xsl:map-entry key="'&quot;'" select="'\&quot;'" />
			<!-- Reverse solidus (aka backslash) -->
			<xsl:map-entry key="'\'" select="'\\'" />
			<!-- Control characters -->
			<!-- Linefeed -->
			<xsl:map-entry key="'&#xA;'" select="'\n'" />
			<!-- Backspace -->
			<xsl:map-entry key="'&#x8;'" select="'\b'" />
			<!-- Formfeed -->
			<xsl:map-entry key="'&#xC;'" select="'\f'" />
			<!-- Carriage return -->
			<xsl:map-entry key="'&#xD;'" select="'\r'" />
			<!-- Horizontal tab -->
			<xsl:map-entry key="'&#x9;'" select="'\t'" />
		</xsl:map>
	</xsl:variable>

	<xsl:variable name="azf-to-json-profile-keys" as="map(xs:string, xs:string)">
		<xsl:map>
			<xsl:map-entry key="'Attrs'" select="'Attribute'" />
			<xsl:map-entry key="'AttrId'" select="'AttributeId'" />
			<xsl:map-entry key="'Values'" select="'Value'" />
			<xsl:map-entry key="'AttrDesignator'" select="'AttributeDesignator'" />
			<xsl:map-entry key="'AttrSelector'" select="'AttributeSelector'" />
			<xsl:map-entry key="'AttrAssignmentExprs'" select="'AttributeAssignmentExprs'" />
		</xsl:map>
	</xsl:variable>

	<!-- Functions -->
	<xsl:function name="azf:json-encode-string" as="xs:string">
		<xsl:param name="input" as="xs:string" />
		<xsl:value-of>
			<!-- See https://www.oxygenxml.com/archives/xsl-list/201311/msg00053.html -->
			<xsl:analyze-string select="$input" regex="&quot;|\\|\n|&#xA;|&#x8;|&#xC;|&#xD;|&#x9;">
				<xsl:matching-substring>
					<xsl:value-of select="map:get($json-string-replacements, .)" />
				</xsl:matching-substring>
				<xsl:non-matching-substring>
					<xsl:value-of select="." />
				</xsl:non-matching-substring>
			</xsl:analyze-string>
		</xsl:value-of>
	</xsl:function>

	<xsl:function name="azf:sanitize-prop-name" as="xs:string">
		<xsl:param name="name" as="xs:string" />
		<xsl:param name="json_profile_compat" as="xs:boolean" />
		<xsl:choose>
			<xsl:when test="$json_profile_compat">
				<xsl:value-of select="if (map:contains($azf-to-json-profile-keys, $name)) then map:get($azf-to-json-profile-keys, $name) else $name" />
			</xsl:when>
			<xsl:otherwise>
				<!-- lower-case first character (camel case) -->
				<xsl:value-of select="concat(lower-case(substring($name, 1, 1)),substring($name, 2))" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- Boolean/integer attributes -->
	<xsl:template name="simple-key-literal">
		<xsl:param name="outputName" select="local-name()" />
		<xsl:param name="valueExpr" select="." />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
		<xsl:value-of select="azf:sanitize-prop-name($outputName, $jsonProfileCompat)" />
		<xsl:text disable-output-escaping="yes">":</xsl:text>
		<xsl:value-of select="$valueExpr" />
	</xsl:template>

	<xsl:template match="@IncludeInResult">
		<xsl:call-template name="simple-key-literal" />
	</xsl:template>

	<!-- String attributes and simple string elements -->
	<xsl:template name="simple-key-string">
		<xsl:param name="outputName" select="local-name()" />
		<xsl:param name="valueExpr" select="." />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
		<xsl:value-of select="azf:sanitize-prop-name($outputName, $jsonProfileCompat)" />
		<xsl:text disable-output-escaping="yes">":"</xsl:text>
		<xsl:value-of select="azf:json-encode-string($valueExpr)" />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
	</xsl:template>

	<!-- @Version used in Policies (references) and Responses' PolicyIdentifierList -->
	<!-- XML Content is used in Attributes and PolicyIssuer elements. Escape the XML content (default behavior) as defined in JSON Profile of XACML -->
	<!-- XPathVersion used in RequestDefaults and Policy(Set)Defaults -->
	<xsl:template match="@Issuer|@DataType|@Version|xacml:Content|xacml:XPathVersion">
		<xsl:call-template name="simple-key-string" />
	</xsl:template>

	<xsl:template match="@AttributeId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'AttrId'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml:Attributes/@Category">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'CategoryId'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@xml:id">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'Id'" />
		</xsl:call-template>
	</xsl:template>

	<!-- ObligationId/AdviceId used in Policies and Responses -->
	<xsl:template match="@ObligationId|@AdviceId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'Id'" />
		</xsl:call-template>
	</xsl:template>

	<!-- Simple elements (no multi-occurring child element and no mixed content, i.e. minOccurs and maxOccurs = 0 or 1) -->
	<xsl:template name="elementToJson">
		<xsl:param name="skipBraces" select="false()" />
		<!-- Optional parameter, may be null -->
		<xsl:param name="textNodeKey" />
		<xsl:if test="not($skipBraces)">
			<xsl:text disable-output-escaping="yes">{</xsl:text>
		</xsl:if>
		<!-- If an extra JSON property should be added, the element's text node value -->
		<xsl:if test="$textNodeKey">
			<xsl:call-template name="simple-key-string">
				<xsl:with-param name="outputName" select="$textNodeKey" />
			</xsl:call-template>
			<xsl:if test="count(@*|child::*) > 0">
				<xsl:text disable-output-escaping="yes">,</xsl:text>
			</xsl:if>
		</xsl:if>
		<xsl:for-each select="@*|child::*">
			<xsl:if test="position() > 1">
				<xsl:text disable-output-escaping="yes">,</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." />
		</xsl:for-each>
		<xsl:if test="not($skipBraces)">
			<xsl:text disable-output-escaping="yes">}</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="elementToJsonWithKey">
		<xsl:param name="outputName" select="local-name()" />
		<xsl:param name="skipBraces" select="false()" />
		<!-- output JSON property name for the element's text node value if any (may be null) -->
		<xsl:param name="textNodeKey" />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
		<xsl:value-of select="azf:sanitize-prop-name($outputName, $jsonProfileCompat)" />
		<xsl:text disable-output-escaping="yes">":</xsl:text>
		<xsl:call-template name="elementToJson">
			<xsl:with-param name="skipBraces" select="$skipBraces" />
			<xsl:with-param name="textNodeKey" select="$textNodeKey" />
		</xsl:call-template>
	</xsl:template>

	<!-- List of elements of common type -->
	<xsl:template name="elementsToJsonArray">
		<xsl:param name="elementsPath" select="child::*" />
		<xsl:text disable-output-escaping="yes">[</xsl:text>
		<xsl:for-each select="$elementsPath">
			<xsl:if test="position() > 1">
				<xsl:text disable-output-escaping="yes">,</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." />
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">]</xsl:text>
	</xsl:template>

	<xsl:template name="elementsToJsonArrayWithKey">
		<xsl:param name="outputName" select="concat(child::*[1]/local-name(),'s')" />
		<xsl:param name="elementsPath" select="child::*" />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
		<xsl:value-of select="azf:sanitize-prop-name($outputName, $jsonProfileCompat)" />
		<xsl:text disable-output-escaping="yes">": </xsl:text>
		<xsl:call-template name="elementsToJsonArray">
			<xsl:with-param name="elementsPath" select="$elementsPath" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml:Attribute/xacml:AttributeValue">
		<xsl:text disable-output-escaping="yes">"</xsl:text>
		<xsl:value-of select="azf:json-encode-string(.)" />
		<xsl:text disable-output-escaping="yes">"</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:Attribute">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<!-- Transform @IncludeInResult, @AttributeId (required), @Issuer -->
		<xsl:for-each select="@*">
			<xsl:if test="position() > 1">
				<xsl:text disable-output-escaping="yes">,</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." />
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:apply-templates select="xacml:AttributeValue[1]/@DataType" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<!-- AttributeValues (child nodes) -->
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<!-- Note that it is "Value" instead of "Values" in the standard JSON Profile of XACML. We consider the plural form makes more sense. -->
			<xsl:with-param name="outputName" select="'Values'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Category of Attributes -->
	<xsl:template match="xacml:Attributes">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:apply-templates select="@Category" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:if test="xacml:Content">
			<xsl:apply-templates select="xacml:Content" />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:if>
		<!-- Note that it is "Attribute" instead of "Attributes" in the standard JSON Profile of XACML. We consider the plural form makes more sense. -->
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Attrs'" />
			<xsl:with-param name="elementsPath" select="xacml:Attribute" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Convert XML attributes to string key-value pairs by default -->
	<xsl:template match="@*">
		<xsl:call-template name="simple-key-string" />
	</xsl:template>

	<!-- JSON does not support comments -->
	<!-- <xsl:template match="comment()"> -->
	<!-- <xsl:copy /> -->
	<!-- </xsl:template> -->
</xsl:stylesheet> 