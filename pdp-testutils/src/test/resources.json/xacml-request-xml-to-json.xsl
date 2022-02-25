<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2017-2022 THALES.
This file is part of AuthZForce CE. Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. -->
<!-- Transformation of XACML 3.0 Request from core specification's XML format to JSON Profile's format. -->
<!-- Author: Cyril DANGERVILLE -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xacml3="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" indent="yes" method="text" omit-xml-declaration="yes" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Parameters (used in xacml-common-xml-to-json.xsl) -->
	<xsl:param name="useJsonProfile" select="'yes'" />

	<xsl:include href="xacml-common-xml-to-json.xsl" />

	<xsl:template match="xacml3:Request">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:for-each select="@*|xacml3:RequestDefaults|xacml3:MultiRequests">
			<xsl:apply-templates select="." />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:for-each>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Category'" />
			<xsl:with-param name="elementsPath" select="xacml3:Attributes" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}}</xsl:text>
	</xsl:template>

	<!-- Boolean attributes -->
	<xsl:template match="@ReturnPolicyIdList|@CombinedDecision">
		<xsl:call-template name="simple-key-literal" />
	</xsl:template>

	<xsl:template match="xacml3:RequestDefaults">
		<xsl:apply-templates select="@XPathVersion" />
	</xsl:template>

	<xsl:template match="xacml3:RequestReference">
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'ReferenceId'" />
			<xsl:with-param name="elementsPath" select="xacml3:AttributesReference/@ReferenceId" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml3:MultiRequests">
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'RequestReference'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>