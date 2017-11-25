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

   <!-- Parameters -->
   <!-- <xsl:param name="param1">val1</xsl:param> -->

   <!-- <xsl:template name="canonicalize-policy" match="child::*"> -->
   <!-- <xsl:copy> -->
   <!-- <xsl:apply-templates select="@* | child::node()" /> -->
   <!-- </xsl:copy> -->
   <!-- </xsl:template> -->
   <xsl:include href="xacml3-common-xml-to-json.xsl" />

   <xsl:template match="xacml3:Request">
      <xsl:text disable-output-escaping="yes">{"Request":{</xsl:text>
      <!-- Transform attributes ReturnPolicyIdList and CombinedDecision -->
      <xsl:apply-templates select="@*" />
      <xsl:text disable-output-escaping="yes">"Category":[</xsl:text>
      <xsl:apply-templates select="child::node()" />
      <xsl:text disable-output-escaping="yes">]}}</xsl:text>
   </xsl:template>

   <!-- Request attributes -->

   <!-- Boolean attributes -->
   <xsl:template match="@ReturnPolicyIdList|@CombinedDecision|@IncludeInResult">
      <xsl:text disable-output-escaping="yes">"</xsl:text>
      <xsl:value-of select="local-name()" />
      <xsl:text disable-output-escaping="yes">":</xsl:text>
      <xsl:value-of select="." />
      <xsl:text disable-output-escaping="yes">,</xsl:text>
   </xsl:template>

   <!-- Request elements -->

   <!-- RequestDefaults -->
   <xsl:template match="xacml3:RequestDefaults">
      <xsl:text disable-output-escaping="yes">"XPathVersion":"</xsl:text>
      <xsl:value-of select="@XPathVersion" />
      <xsl:text disable-output-escaping="yes">",</xsl:text>
   </xsl:template>

   <!-- Attributes -->

   <xsl:template match="Content">
      <xsl:message terminate="yes">Transformation of XACML Content elements is not supported</xsl:message>
   </xsl:template>

   <!-- Special case for first Attributes element, to differentiate from subsequent ones for which an extra preceding comma is necessary in JSON output -->
   <xsl:template match="xacml3:Request/xacml3:Attributes[1]">
      <xsl:call-template name="Attributes" />
   </xsl:template>
</xsl:stylesheet>