<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2022 THALES. This file is part of AuthZForce CE. Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. -->
<!-- PDP configuration upgrade XSL Sheet, from schema v7 to current version. -->
<!-- Tested with Saxon XSLT processor. -->
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
