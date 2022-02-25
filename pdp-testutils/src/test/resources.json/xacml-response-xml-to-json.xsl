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
<!-- Transformation of XACML 3.0 Response from core specification's XML format to JSON Profile's format. -->
<!-- Author: Cyril DANGERVILLE -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xacml3="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" method="text" omit-xml-declaration="yes" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Parameters (used in xacml-common-xml-to-json.xsl) -->
	<xsl:param name="useJsonProfile" select="'yes'" />

	<!-- <xsl:template name="canonicalize-policy" match="child::*"> -->
	<!-- <xsl:copy> -->
	<!-- <xsl:apply-templates select="@* | child::node()" /> -->
	<!-- </xsl:copy> -->
	<!-- </xsl:template> -->
	<xsl:include href="xacml-common-xml-to-json.xsl" />

	<xsl:template match="xacml3:Response">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Response'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Response/Result -->
	<xsl:template match="xacml3:Result">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:apply-templates select="xacml3:Decision" />
		<xsl:for-each select="xacml3:Status|xacml3:Obligations|xacml3:AssociatedAdvice|xacml3:PolicyIdentifierList">
			<xsl:text disable-output-escaping="yes">,</xsl:text>
			<xsl:apply-templates select="." />
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Category'" />
			<xsl:with-param name="elementsPath" select="xacml3:Attributes" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Decision -->
	<xsl:template match="xacml3:Decision|xacml3:StatusMessage|@Category">
		<xsl:call-template name="simple-key-string" />
	</xsl:template>

	<xsl:template match="xacml3:StatusCode|xacml3:Status">
		<xsl:call-template name="elementToJsonWithKey" />
	</xsl:template>

	<xsl:template match="xacml3:StatusDetail">
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">[</xsl:text>
		<xsl:value-of select="." />
		<xsl:text disable-output-escaping="yes">]</xsl:text>
	</xsl:template>

	<xsl:template match="xacml3:AttributeAssignment">
		<xsl:call-template name="elementToJson">
			<xsl:with-param name="textNodeKey" select="'Value'" />
		</xsl:call-template>
	</xsl:template>

	<!-- Obligation or Advice -->
	<xsl:template match="xacml3:Obligation|xacml3:Advice">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:apply-templates select="@*" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<!-- WARNING: Property name for the array of AttributeAssignments is 'AttributeAssignment' in JSON Profile, not 'AttributeAssignments' although it is an array of them. -->
			<xsl:with-param name="outputName" select="'AttributeAssignment'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml3:Obligations">
		<xsl:call-template name="elementsToJsonArrayWithKey" />
	</xsl:template>

	<xsl:template match="xacml3:AssociatedAdvice">
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'AssociatedAdvice'" />
		</xsl:call-template>
	</xsl:template>

	<!-- PolicyIdentifierList -->
	<xsl:template match="xacml3:PolicyIdReference|xacml3:PolicySetIdReference">
		<xsl:call-template name="elementToJson">
			<xsl:with-param name="textNodeKey" select="'Id'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml3:PolicyIdentifierList">
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'PolicyIdReference'" />
			<xsl:with-param name="elementsPath" select="xacml3:PolicyIdReference" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'PolicySetIdReference'" />
			<xsl:with-param name="elementsPath" select="xacml3:PolicySetIdReference" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

</xsl:stylesheet>