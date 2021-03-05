<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2017 Thales Services SAS. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
	without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public 
	License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
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
			<xsl:with-param name="textNodeKey" select="'Id'"></xsl:with-param>
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