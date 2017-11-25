<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2017 Thales Services SAS. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
   as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public 
   License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
<!-- Transformation of XACML 3.0 Response from core specification's XML format to JSON Profile's format. -->
<!-- Author: Cyril DANGERVILLE -->
<!-- WARNING: StatusDetail and nested StatusCode elements are not supported -->
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
   <xsl:include href="xacml3-common-xml-to-json.xsl" />

   <xsl:template match="xacml3:Response">
      <xsl:text disable-output-escaping="yes">{"Response":[</xsl:text>
      <xsl:apply-templates select="child::node()" />
      <xsl:text disable-output-escaping="yes">]}</xsl:text>
   </xsl:template>

   <!-- Response/Result -->
   <xsl:template name="Result">
      <xsl:text disable-output-escaping="yes">{</xsl:text>
      <xsl:apply-templates select="xacml3:Decision" />
      <xsl:apply-templates select="xacml3:Status" />
      <xsl:apply-templates select="xacml3:Obligations" />
      <xsl:apply-templates select="xacml3:AssociatedAdvice" />
      <xsl:text disable-output-escaping="yes">,"Category":[</xsl:text>
      <xsl:apply-templates select="xacml3:Attributes" />
      <xsl:text disable-output-escaping="yes">]</xsl:text>
      <xsl:apply-templates select="xacml3:PolicyIdentifierList" />
      <xsl:text disable-output-escaping="yes">}</xsl:text>
   </xsl:template>

   <!-- First Result of Response -->
   <xsl:template match="xacml3:Response/xacml3:Result[1]">
      <xsl:call-template name="Result" />
   </xsl:template>

   <!-- Subsequent Results -->
   <xsl:template match="xacml3:Result">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="Result" />
   </xsl:template>

   <!-- Decision -->
   <xsl:template match="xacml3:Decision">
      <xsl:call-template name="simple-key-string" />
   </xsl:template>

   <xsl:template name="simple-key-object">
      <xsl:text disable-output-escaping="yes">"</xsl:text>
      <xsl:value-of select="local-name()" />
      <xsl:text disable-output-escaping="yes">":{</xsl:text>
      <xsl:apply-templates select="@*|child::node()" />
      <xsl:text disable-output-escaping="yes">}</xsl:text>
   </xsl:template>

   <xsl:template name="simple-key-array">
      <xsl:text disable-output-escaping="yes">"</xsl:text>
      <xsl:value-of select="local-name()" />
      <xsl:text disable-output-escaping="yes">":[</xsl:text>
      <xsl:apply-templates select="@*|child::node()" />
      <xsl:text disable-output-escaping="yes">]</xsl:text>
   </xsl:template>

   <!-- Status -->
   <!-- StatusCode/@Value (and Policy reference @Id) -->
   <xsl:template match="@Value|@Id">
      <xsl:call-template name="simple-key-string" />
   </xsl:template>

   <xsl:template match="xacml3:StatusCode">
      <xsl:call-template name="simple-key-object" />
   </xsl:template>

   <xsl:template match="xacml3:StatusMessage">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="simple-key-object" />
   </xsl:template>

   <xsl:template match="xacml3:Status">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="simple-key-object" />
   </xsl:template>

   <!-- Obligations/Advice -->
   <xsl:template match="@ObligationId|@AdviceId">
      <xsl:text disable-output-escaping="yes">"Id":"</xsl:text>
      <xsl:value-of select="." />
      <xsl:text disable-output-escaping="yes">"</xsl:text>
   </xsl:template>

   <!-- AttributeAssignment -->
   <xsl:template match="@Category|@DataType">
      <xsl:call-template name="simple-key-string-and-comma" />
   </xsl:template>

   <xsl:template name="AttributeAssignment">
      <xsl:text disable-output-escaping="yes">{</xsl:text>
      <xsl:apply-templates select="@*" />
      <xsl:text disable-output-escaping="yes">"Value":"</xsl:text>
      <xsl:apply-templates select="child::node()" />
      <xsl:text disable-output-escaping="yes">"}</xsl:text>
   </xsl:template>

   <!-- First AttributeAssignment of an Obligation or Advice -->
   <xsl:template match="xacml3:Obligation/xacml3:AttributeAssignment[1]|xacml3:Advice/xacml3:AttributeAssignment[1]">
      <xsl:call-template name="AttributeAssignment" />
   </xsl:template>

   <!-- Subsequent AttributeAssignments of an Obligation or Advice -->
   <xsl:template match="xacml3:AttributeAssignment">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="AttributeAssignment" />
   </xsl:template>

   <!-- Obligation or Advice -->
   <xsl:template name="ObligationOrAdvice">
      <xsl:text disable-output-escaping="yes">{</xsl:text>
      <xsl:apply-templates select="@*" />
      <xsl:text disable-output-escaping="yes">,"AttributeAssignment":[</xsl:text>
      <xsl:apply-templates select="xacml3:AttributeAssignment" />
      <xsl:text disable-output-escaping="yes">]}</xsl:text>
   </xsl:template>

   <!-- First Obligation (resp. Advice) in Obligations (resp. AssociatedAdvice) -->
   <xsl:template match="xacml3:Obligations/xacml3:Obligation[1]|xacml3:AssociatedAdvice/xacml3:Advice[1]">
      <xsl:call-template name="ObligationOrAdvice" />
   </xsl:template>

   <!-- Subsequent Obligation (resp. Advice) in Obligations (resp. AssociatedAdvice) -->
   <xsl:template match="xacml3:Obligation|xacml3:Advice">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="ObligationOrAdvice" />
   </xsl:template>

   <!-- Obligations or AssociatedAdvice -->
   <xsl:template match="xacml3:Obligations|xacml3:AssociatedAdvice">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="simple-key-array" />
   </xsl:template>
   
   <!-- Special case for first Attributes element in Result (no preceding comma)-->
   <!-- Ignore IncludeInResult because useless in Result, and optional in XACML JSON -->
    <xsl:template match="@IncludeInResult" />
   <xsl:template match="xacml3:Result/xacml3:Attributes[1]">
      <xsl:call-template name="Attributes" />
   </xsl:template>

   <!-- PolicyIdentifierList -->
   <xsl:template match="@Version">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="simple-key-string" />
   </xsl:template>

   <xsl:template name="PolicyReference">
      <xsl:text disable-output-escaping="yes">{</xsl:text>
      <xsl:apply-templates select="@*|child::node()" />
      <xsl:text disable-output-escaping="yes">}</xsl:text>
   </xsl:template>

   <xsl:template match="xacml3:PolicyIdentifierList/xacml3:PolicyIdReference[1]|xacml3:PolicyIdentifierList/xacml3:PolicySetIdReference[1]">
      <xsl:call-template name="PolicyReference" />
   </xsl:template>

   <xsl:template match="xacml3:PolicyIdentifierList/xacml3:PolicyIdReference|xacml3:PolicyIdentifierList/xacml3:PolicySetIdReference">
      <xsl:text disable-output-escaping="yes">,</xsl:text>
      <xsl:call-template name="PolicyReference" />
   </xsl:template>

   <xsl:template match="xacml3:PolicyIdentifierList">
      <xsl:text disable-output-escaping="yes">,"PolicyIdReference":[</xsl:text>
      <xsl:apply-templates select="xacml3:PolicyIdReference" />
      <xsl:text disable-output-escaping="yes">],"PolicySetIdReference":[</xsl:text>
      <xsl:apply-templates select="xacml3:PolicySetIdReference" />
      <xsl:text disable-output-escaping="yes">]</xsl:text>
   </xsl:template>

   <!-- JSON does not support comments -->
   <!-- <xsl:template match="@* | comment()"> -->
   <!-- <xsl:copy /> -->
   <!-- </xsl:template> -->
</xsl:stylesheet>