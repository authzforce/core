<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2012-2016 Thales Services SAS. This file is part of AuthzForce CE. AuthzForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public 
	License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthzForce CE is distributed in the hope that it will be useful, but WITHOUT 
	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General 
	Public License along with AuthzForce CE. If not, see <http://www.gnu.org/licenses/>. -->
<!-- PDP configuration upgrade XSL Sheet: parent folder name indicates the version from which you can upgrade to the current one. -->
<!-- To be used with Saxon XSLT processor. -->
<!-- Author: Cyril DANGERVILLE. -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:old="http://authzforce.github.io/core/xmlns/pdp/7"
                xmlns="http://authzforce.github.io/core/xmlns/pdp/8" exclude-result-prefixes="old">
    <xsl:output encoding="UTF-8" indent="yes" method="xml"/>

    <!-- Change root element... -->
    <xsl:template match="/old:pdp">
        <xsl:element name="{local-name(.)}">
            <xsl:copy-of select="namespace::*[. != 'http://authzforce.github.io/core/xmlns/pdp/7']"/>
            <!-- Update attributes -->
            <!-- Copy unmodified attributes -->
            <xsl:apply-templates
                    select="@*[name()!='version' and name()!='useStandardDatatypes' and name()!='useStandardFunctions' and name()!='useStandardCombiningAlgorithms' and name()!= 'standardEnvAttributeSource' and name()!='enableXPath']"/>
            <!-- Schema version -->
            <xsl:attribute name="version">8.0</xsl:attribute>
            <!-- Replace pdp/@useStandardDatatypes with pdp/@standardDatatypesEnabled -->
            <xsl:if test="@useStandardDatatypes">
                <xsl:attribute name="standardDatatypesEnabled">
                    <xsl:value-of select="@useStandardDatatypes"/>
                </xsl:attribute>
            </xsl:if>
            <!-- Replace pdp/@useStandardFunctions with pdp/@standardFunctionsEnabled -->
            <xsl:if test="@useStandardFunctions">
                <xsl:attribute name="standardFunctionsEnabled">
                    <xsl:value-of select="@useStandardFunctions"/>
                </xsl:attribute>
            </xsl:if>
            <!-- Replace pdp/@useStandardCombiningAlgorithms with pdp/@standardCombiningAlgorithmsEnabled -->
            <xsl:if test="@useStandardCombiningAlgorithms">
                <xsl:attribute name="standardCombiningAlgorithmsEnabled">
                    <xsl:value-of select="@useStandardCombiningAlgorithms"/>
                </xsl:attribute>
            </xsl:if>
            <!-- Replace pdp/@useStandardCombiningAlgorithms with pdp/@standardCombiningAlgorithmsEnabled -->
            <xsl:if test="@enableXPath">
                <xsl:attribute name="xPathEnabled">
                    <xsl:value-of select="@enableXPath"/>
                </xsl:attribute>
            </xsl:if>
            <!-- Replace pdp/@standardEnvAttributeSource="REQUEST_ELSE_PDP" (resp. REQUEST_ONLY) with pdp/@standardAttributeProvidersEnabled="true" (resp. "false") -->
            <xsl:choose>
                <xsl:when test="@standardEnvAttributeSource = 'REQUEST_ELSE_PDP'">
                    <xsl:attribute name="standardAttributeProvidersEnabled">true</xsl:attribute>
                    <xsl:apply-templates
                            select="node()"/>
                </xsl:when>
                <xsl:when test="@standardEnvAttributeSource = 'REQUEST_ONLY'">
                    <xsl:attribute name="standardAttributeProvidersEnabled">false</xsl:attribute>
                    <xsl:apply-templates
                            select="node()"/>
                </xsl:when>
                <!-- Replace pdp/@standardEnvAttributeSource="PDP_ONLY" with pdp/@standardAttributeProvidersEnabled="false" AND <attributeProvider xsi:type="StdEnvAttributeProviderDescriptor"><override>true</override></attributeProvider> -->
                <xsl:when test="@standardEnvAttributeSource = 'PDP_ONLY'">
                    <xsl:attribute name="standardAttributeProvidersEnabled">false</xsl:attribute>
                    <!-- Copy unmodified elements before attributeProviders -->
                    <xsl:apply-templates
                            select="old:attributeDatatype | old:function | old:combiningAlgorithm"/>
                    <attributeProvider id="_urn_ow2_authzforce_feature_pdp_attribute-provider_std-env" xsi:type="StdEnvAttributeProviderDescriptor">
                        <override>true</override>
                    </attributeProvider>
                    <xsl:apply-templates
                            select="old:policyProvider | old:rootPolicyRef | old:decisionCache | old:ioProcChain"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Copy unmodified elements -->
                    <xsl:apply-templates
                            select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- Change old PDP config namespace to new one on elements by default -->
    <xsl:template match="old:*">
        <xsl:element name="{local-name(.)}">
            <xsl:apply-templates select="node()|attribute()|text()|comment()" />
        </xsl:element>
    </xsl:template>

    <!-- Default rule: copy as is -->
    <xsl:template match="node()|attribute()|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="node()|attribute()|text()|comment()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
