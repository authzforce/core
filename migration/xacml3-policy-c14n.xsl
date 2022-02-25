<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2012-2022 THALES.

    This file is part of AuthZForce CE. Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!-- XACML 3.0 policy canonicalization, basically replacing deprecated identifiers (XACML 3.0 Core Specification, §A.4) with new ones. Author: Cyril DANGERVILLE. -->
<!-- Tested with Saxon XSLT processor. -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" indent="yes" method="xml" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<xsl:template name="canonicalize-policy" match="child::*">
		<xsl:copy>
			<xsl:apply-templates select="@* | child::node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@MatchId|@FunctionId">
		<xsl:attribute name="{local-name()}">		
			<xsl:choose>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:xpath-node-count'">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal'">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:xpath-node-match'">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal'">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal'">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration'">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration'">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration'">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration'">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration'">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration'">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration</xsl:when>
				<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="@DataType">
		<xsl:attribute name="{local-name()}">		
			<xsl:choose>
				<xsl:when test=". = 'http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration'">http://www.w3.org/2001/XMLSchema#dayTimeDuration</xsl:when>
				<xsl:when test=". = 'http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration'">http://www.w3.org/2001/XMLSchema#yearMonthDuration</xsl:when>
				<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="@RuleCombiningAlgId">
		<xsl:attribute name="{local-name()}">		
			<xsl:choose>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides'">urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides'">urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides'">urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides'">urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides</xsl:when>
				<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="@PolicyCombiningAlgId">
		<xsl:attribute name="{local-name()}">		
			<xsl:choose>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides'">urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides'">urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides'">urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides'">urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides</xsl:when>
				<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="@AttributeId|@ContextSelectorId">
		<xsl:attribute name="{local-name()}">		
			<xsl:choose>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:subject:authn-locality:ip-address'">urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address</xsl:when>
				<xsl:when test=". = 'urn:oasis:names:tc:xacml:1.0:subject:authn-locality:dns-name'">urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name</xsl:when>
				<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="@* | comment()">
		<xsl:copy />
	</xsl:template>
</xsl:stylesheet>