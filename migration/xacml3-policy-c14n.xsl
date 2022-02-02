<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2012-2016 Thales Services SAS.

    This file is part of AuthzForce CE.

    AuthzForce CE is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuthzForce CE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuthzForce CE.  If not, see <http://www.gnu.org/licenses/>.

-->
<!-- XACML 3.0 policy canonicalization, basically replacing deprecated identifiers (XACML 3.0 Core Specification, §A.4) with new ones. Author: Cyril DANGERVILLE. -->
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