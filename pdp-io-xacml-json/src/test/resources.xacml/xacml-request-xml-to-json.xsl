<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2017 Thales Services SAS. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
	without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public 
	License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
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