<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2017 Thales Services SAS. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
   as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public 
   License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
<!-- Common part of transformation of XACML 3.0 Request from core specification's XML format to JSON Profile's format. -->
<!-- Author: Cyril DANGERVILLE -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xacml3="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
   <xsl:output encoding="UTF-8" method="text" omit-xml-declaration="yes" />

   <!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
   <!-- <xsl:strip-space elements="*" /> -->

   <!-- Parameters -->
   <!-- <xsl:param name="param1">val1</xsl:param> -->

   <!-- <xsl:template name="canonicalize-policy" match="child::*"> -->
   <!-- <xsl:copy> -->
   <!-- <xsl:apply-templates select="@* | child::node()" /> -->
   <!-- </xsl:copy> -->
   <!-- </xsl:template> -->
   <xsl:template name="simple-key-string">
      <xsl:text disable-output-escaping="yes">"</xsl:text>
      <xsl:value-of select="local-name()" />
      <xsl:text disable-output-escaping="yes">":"</xsl:text>
      <xsl:value-of select="." />
      <xsl:text disable-output-escaping="yes">"</xsl:text>
   </xsl:template>

   <xsl:template name="simple-key-string-and-comma">
      <xsl:call-template name="simple-key-string" />
      <xsl:text disable-output-escaping="yes">,</xsl:text>
   </xsl:template>

   <!-- AttributeValue -->
   <xsl:template name="AttributeValue">
      <xsl:text disable-output-escaping="yes">"</xsl:text>
      <xsl:value-of select="." />
      <xsl:text disable-output-escaping="yes">"</xsl:text>
   </xsl:template>

   <!-- First AttributeValue of the Attribute -->
   <xsl:template match="xacml3:Attribute/xacml3:AttributeValue[1]">
      <xsl:call-template name="AttributeValue" />
   </xsl:template>

   <!-- Subsequent AttributeValues of the Attribute (precede with a comma) -->
   <xsl:template match="xacml3:AttributeValue">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="AttributeValue" />
   </xsl:template>

   <!-- Attribute -->
   <xsl:template match="@Issuer|@AttributeId">
      <xsl:call-template name="simple-key-string-and-comma" />
   </xsl:template>

   <xsl:template name="Attribute">
      <xsl:text disable-output-escaping="yes">{</xsl:text>
      <!-- Transform attributes IncludeInResult, AttributeId, Issuer -->
      <xsl:apply-templates select="@*" />
      <xsl:text disable-output-escaping="yes">"DataType":"</xsl:text>
      <xsl:value-of select="xacml3:AttributeValue[1]/@DataType" />
      <xsl:text disable-output-escaping="yes">","Value":[</xsl:text>
      <xsl:apply-templates select="child::node()" />
      <xsl:text disable-output-escaping="yes">]}</xsl:text>
   </xsl:template>

    <!-- First Attribute of the category (Attributes element) -->
   <xsl:template match="xacml3:Attributes/xacml3:Attribute[1]">
      <xsl:call-template name="Attribute" />
   </xsl:template>

   <!-- Subsequent Attributes of the category (precede with a comma) -->
   <xsl:template match="xacml3:Attribute">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="Attribute" />
   </xsl:template>

   <!-- Attributes -->
   <xsl:template name="Attributes">
      <xsl:text disable-output-escaping="yes">{"CategoryId":"</xsl:text>
      <xsl:value-of select="@Category" />
      <xsl:text disable-output-escaping="yes">",</xsl:text>
      <xsl:apply-templates select="xacml3:Content" />
      <xsl:text disable-output-escaping="yes">"Attribute":[</xsl:text>
      <xsl:apply-templates select="xacml3:Attribute" />
      <xsl:text disable-output-escaping="yes">]}</xsl:text>
   </xsl:template>

   <xsl:template match="xacml3:Attributes">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="Attributes" />
   </xsl:template>


   <!-- JSON does not support comments -->
   <!-- <xsl:template match="@* | comment()"> -->
   <!-- <xsl:copy /> -->
   <!-- </xsl:template> -->
</xsl:stylesheet>