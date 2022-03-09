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
<!-- XACML 2.0-to-3.0 Policy Conversion XSL Sheet. Author: Cyril DANGERVILLE -->
<!-- For replacing deprecated identifiers (XACML 3.0 Core Specification, §A.4) with new ones, see file 'xacml3-policy-c14n.xsl'. -->
<!-- WARNING: This XSLT does not convert XACML 2.0 AttributeSelectors to their strict equivalent in XACML 3.0: 1) it converts XACML 2.0 RequestContextPath to XACML 3.0 Path, although they have different
	meaning as they do not apply to the same XML node, so please be aware. 2) It cannot determine the required Category in XACML 3.0 from XACML 2.0 input in some cases, so it has to use some default value 
	that you can set with XSLT parameter 'AttributeSelector.SubjectCategory.default' for AttrbuteSelectors coming from SubjectMatches, and 'AttributeSelector.Category.default' for the ones coming from Conditions. -->
<!-- Tested with Saxon XSLT processor. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xacml2="urn:oasis:names:tc:xacml:2.0:policy:schema:os" xmlns:xacml3="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Parameters -->
	<!-- Default value of <AttributeSelector>'s Category to be used in XACML 3.0 output when converting from <AttributeSelector> in XACML 2.0 <SubjectMatch>. Author's note: there does not seem to be any automatic 
		way to guess this. -->
	<xsl:param name="AttributeSelector.SubjectCategory.default">urn:oasis:names:tc:xacml:1.0:subject-category:access-subject</xsl:param>
	<!-- Default value of <AttributeSelector>'s Category to be used in XACML 3.0 output when converting from <AttributeSelector> in XACML 2.0 <Condition>. Author's note: there does not seem to be any automatic 
		way to guess this. -->
	<xsl:param name="AttributeSelector.Category.default">urn:oasis:names:tc:xacml:3.0:attribute-category:resource</xsl:param>

	<xsl:template match="xacml2:Subjects | xacml2:Actions | xacml2:Resources | xacml2:Environments">
		<xsl:element name="xacml3:AnyOf">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:Subject | xacml2:Action | xacml2:Resource | xacml2:Environment">
		<xsl:element name="xacml3:AllOf">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:SubjectMatch | xacml2:ActionMatch | xacml2:ResourceMatch | xacml2:EnvironmentMatch">
		<xsl:element name="xacml3:Match">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:SubjectAttributeDesignator | xacml2:ActionAttributeDesignator | xacml2:ResourceAttributeDesignator | xacml2:EnvironmentAttributeDesignator">
		<xsl:element name="xacml3:AttributeDesignator">
			<xsl:attribute name="Category">
				<xsl:choose>
					<xsl:when test="local-name() = 'SubjectAttributeDesignator'">
						<xsl:choose>
							<xsl:when test="@SubjectCategory"><xsl:value-of select="@SubjectCategory"/></xsl:when>
							<xsl:otherwise>urn:oasis:names:tc:xacml:1.0:subject-category:access-subject</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="local-name() = 'ActionAttributeDesignator'">urn:oasis:names:tc:xacml:3.0:attribute-category:action</xsl:when>
					<xsl:when test="local-name() = 'ResourceAttributeDesignator'">urn:oasis:names:tc:xacml:3.0:attribute-category:resource</xsl:when>
					<xsl:when test="local-name() = 'EnvironmentAttributeDesignator'">urn:oasis:names:tc:xacml:3.0:attribute-category:environment</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:if test="not(@MustBePresent)">
				<xsl:attribute name="MustBePresent">false</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*[not(local-name() = 'SubjectCategory')] | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:AttributeSelector">
		<xsl:element name="xacml3:{local-name()}">
			<xsl:attribute name="Category">
				<xsl:choose>
					<xsl:when test="local-name(parent::*) = 'SubjectMatch'"><xsl:value-of select="$AttributeSelector.SubjectCategory.default"/></xsl:when>
					<xsl:when test="local-name(parent::*) = 'ActionMatch'">urn:oasis:names:tc:xacml:3.0:attribute-category:action</xsl:when>
					<xsl:when test="local-name(parent::*) = 'ResourceMatch'">urn:oasis:names:tc:xacml:3.0:attribute-category:resource</xsl:when>
					<xsl:when test="local-name(parent::*) = 'EnvironmentMatch'">urn:oasis:names:tc:xacml:3.0:attribute-category:environment</xsl:when>
					<xsl:otherwise><xsl:value-of select="$AttributeSelector.Category.default"/></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:attribute name="Path"><xsl:value-of select="@RequestContextPath"/></xsl:attribute>
			<xsl:if test="not(@MustBePresent)">
				<xsl:attribute name="MustBePresent">false</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*[not(local-name() = 'RequestContextPath')] | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:Obligations">
		<xsl:element name="xacml3:ObligationExpressions">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:Obligation">
		<xsl:element name="xacml3:ObligationExpression">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:AttributeAssignment">
		<xsl:element name="xacml3:AttributeAssignmentExpression">
			<xsl:apply-templates select="@AttributeId"/>
			<xsl:element name="xacml3:AttributeValue">
				<xsl:apply-templates select="@*[not(local-name() = 'AttributeId')] | child::node()"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="xacml2:PolicySet | xacml2:Policy">
		<xsl:element name="xacml3:{local-name()}">
			<xsl:if test="not(@Version)">
				<xsl:attribute name="Version">1.0</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="child::*">
		<xsl:element name="xacml3:{local-name()}">
			<xsl:apply-templates select="@* | child::node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="@* | comment()">
		<xsl:copy/>
	</xsl:template>
</xsl:stylesheet>