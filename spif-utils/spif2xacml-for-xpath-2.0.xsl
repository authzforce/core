<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2022 THALES.

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
<!-- SPIF (xmlspif.org) to XACML 3.0 Policy Conversion XSL Sheet. Author: Cyril DANGERVILLE -->
<!-- Tested with Saxon XSLT processor. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:spif="http://www.xmlspif.org/spif"
                xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="spif xs">
    <xsl:output encoding="UTF-8" indent="yes" method="xml"/>

    <!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
    <!-- <xsl:strip-space elements="*" /> -->

    <!-- Parameters -->
    <!-- Flag to enable/disable optimizations for AuthzForce only. Disable for maximum compatibility with other XACML implementations. -->
    <xsl:param name="authzforce_optimized" as="xs:boolean">true</xsl:param>

    <!-- Start with root element... -->
    <xsl:template match="/spif:SPIF">
        <PolicySet xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" PolicySetId="{spif:securityPolicyId/@id}"
                   Version="{@version}"
                   PolicyCombiningAlgId="urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit">
            <Description>
                <xsl:text>Generated from SPIF: </xsl:text><xsl:value-of select="spif:securityPolicyId/@name"/><xsl:text> v</xsl:text><xsl:value-of
                    select="@version"/><xsl:text>. See also NATO ADatP-4774.1 Implementation Guidance.</xsl:text>
            </Description>
            <PolicySetDefaults>
                <XPathVersion>http://www.w3.org/TR/2007/REC-xpath20-20070123</XPathVersion>
            </PolicySetDefaults>
            <Target/>

            <Policy PolicyId="Permissions_P" Version="1.0"
                    RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides">
                <Description>
				Both PolicyIdentifiers from confidentiality clearance (subject) and label (resource) must match the SPIF's.
                    Then match the categories against each other (hierarchical ones aka classifications, and non-hierarchical ones).
                </Description>
                <Target>
                    <!-- PolicyIdentifier match -->
                    <AnyOf>
                        <AllOf>
                            <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                                <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">
                                    <xsl:value-of select="spif:securityPolicyId/@name"/>
                                </AttributeValue>
                                <AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                                   Path="//*:PolicyIdentifier/text()"
                                                   DataType="http://www.w3.org/2001/XMLSchema#string"
                                                   MustBePresent="true"/>
                            </Match>
                            <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                                <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">
                                    <xsl:value-of select="spif:securityPolicyId/@name"/>
                                </AttributeValue>
                                <AttributeSelector
                                        Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                                        Path="//*:PolicyIdentifier/text()"
                                        DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true"/>
                            </Match>
                        </AllOf>
                    </AnyOf>
                </Target>
                <xsl:if test="$authzforce_optimized">
                    <VariableDefinition VariableId="resource_classif_name">
                        <AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                           Path="//*:Classification/text()"
                                           DataType="http://www.w3.org/2001/XMLSchema#string"
                                           MustBePresent="true"/>
                    </VariableDefinition>
                    <VariableDefinition VariableId="subject_classif_name">
                        <AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                                           Path="//*:Classification/text()"
                                           DataType="http://www.w3.org/2001/XMLSchema#string"
                                           MustBePresent="true"/>
                    </VariableDefinition>
                </xsl:if>

                <!--With AuthzForce, we can use XPath variables based on XACML VariableDefinitions. -->
                <xsl:variable name="xpath_resource_classif_expr" as="xs:string">
                    <xsl:choose>
                        <xsl:when test="$authzforce_optimized">
                            <xsl:text>$resource_classif_name</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>//*:Classification</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <VariableDefinition VariableId="resource_classif_level">
                    <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">
                        <AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                           DataType="http://www.w3.org/2001/XMLSchema#integer"
                                           MustBePresent="true">
                            <xsl:attribute name="Path">
                                <xsl:for-each select="//spif:securityClassification">
                                    <xsl:sort select="@hierarchy" order="descending" />
                                    <xsl:text>if (</xsl:text>
                                    <xsl:value-of select="$xpath_resource_classif_expr"/>
                                    <xsl:text> = '</xsl:text><xsl:value-of select="@name" />
                                    <xsl:text>') then </xsl:text>
                                    <xsl:value-of select="@hierarchy"/>
                                    <xsl:text> else </xsl:text>
                                </xsl:for-each>
                                <xsl:text>0</xsl:text>
                            </xsl:attribute>
                        </AttributeSelector>
                    </Apply>
                </VariableDefinition>

                <xsl:variable name="xpath_subject_classif_expr" as="xs:string">
                    <xsl:choose>
                        <xsl:when test="$authzforce_optimized">
                            <xsl:text>$subject_classif_name</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>//*:Classification</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <VariableDefinition VariableId="subject_classif_level">
                    <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">
                        <AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                                           DataType="http://www.w3.org/2001/XMLSchema#integer"
                                           MustBePresent="true">
                            <xsl:attribute name="Path">
                                <xsl:for-each select="//spif:securityClassification">
                                    <xsl:sort select="@hierarchy" order="descending" />
                                    <xsl:text>if (</xsl:text>
                                    <xsl:value-of select="$xpath_subject_classif_expr"/>
                                    <xsl:text> = '</xsl:text><xsl:value-of select="@name" />
                                    <xsl:text>') then </xsl:text>
                                    <xsl:value-of select="@hierarchy"/>
                                    <xsl:text> else </xsl:text>
                                </xsl:for-each>
                                <xsl:text>0</xsl:text>
                            </xsl:attribute>
                        </AttributeSelector>
                    </Apply>
                </VariableDefinition>

                <Rule Effect="Permit" RuleId="READ_if_subject_classif_greater_than_or_equal_to_resource_classif_R">
                    <Description>Bell-Lapadula: allow READ if and only if subject level ≥ object (resource) level </Description>
                    <Target>
                        <AnyOf>
                            <AllOf>
                                <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                                    <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">READ</AttributeValue>
                                    <AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action"
                                                         AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
                                                         DataType="http://www.w3.org/2001/XMLSchema#string"
                                                         MustBePresent="true"/>
                                </Match>
                            </AllOf>
                        </AnyOf>
                    </Target>
                    <Condition>
                        <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal">
                            <VariableReference VariableId="subject_classif_level"/>
                            <VariableReference VariableId="resource_classif_level"/>
                        </Apply>
                    </Condition>
                </Rule>

                <Rule Effect="Permit" RuleId="WRITE_if_subject_classif_less_than_or_equal_to_resource_classif_R">
                    <Description>Bell-Lapadula: allow WRITE if and only if subject level ≤ object (resource) level </Description>
                    <Target>
                        <AnyOf>
                            <AllOf>
                                <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                                    <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">WRITE</AttributeValue>
                                    <AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action"
                                                         AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
                                                         DataType="http://www.w3.org/2001/XMLSchema#string"
                                                         MustBePresent="true"/>
                                </Match>
                            </AllOf>
                        </AnyOf>
                    </Target>
                    <Condition>
                        <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">
                            <VariableReference VariableId="subject_classif_level"/>
                            <VariableReference VariableId="resource_classif_level"/>
                        </Apply>
                    </Condition>
                </Rule>

                <Rule RuleId="other_categories_match_R" Effect="Permit">
                    <Description>Match other (non-hierarchical) categories:
                        for each category Cn,
                        (resource has no C1 value OR subject/resource C1 values match) AND (resource has no C2 value OR subject/resource C2 values match) AND ...

                        The 'has no value' is translated to: the AttributeSelector returns an empty bag (size 0).
                        The 'match' function depends on whether the category is permissive (at least one value must match, i.e. 'at-least-one-member-of' function) or restrictive (all must match, i.e. 'subset' function).
                    </Description>
                    <Condition>
                        <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:and">
                            <!-- For each restrictive category -->
                            <xsl:for-each
                                    select="//spif:securityCategoryTag">
                                <!-- Ignore informative categories -->
                                <xsl:if test="@tagType !='tagType7'">
                                    <!-- Either resource does not have this category (empty bag) or at least one value of this category must match the subject clearance -->
                                    <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:or">
                                        <!-- Test if resource has this category -->
                                        <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-equal">
                                            <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">
                                                <AttributeSelector
                                                        Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                                        Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()"
                                                        DataType="http://www.w3.org/2001/XMLSchema#string"
                                                        MustBePresent="false"/>
                                            </Apply>
                                            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#integer">0</AttributeValue>
                                        </Apply>
                                        <!-- Test if at least one value the resource category matches the subject clearance -->
                                        <xsl:variable name="comparFuncId">
                                            <xsl:choose>
                                                <xsl:when test="@tagType='permissive' or @enumType='permissive'">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of</xsl:when>
                                                <!-- otherwise category is restrictive -->
                                                <xsl:otherwise>urn:oasis:names:tc:xacml:1.0:function:string-subset</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        <Apply FunctionId="{$comparFuncId}">
                                            <AttributeSelector
                                                    Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                                    Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()"
                                                    DataType="http://www.w3.org/2001/XMLSchema#string"
                                                    MustBePresent="false"/>
                                            <AttributeSelector
                                                    Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                                                    Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()"
                                                    DataType="http://www.w3.org/2001/XMLSchema#string"
                                                    MustBePresent="false"/>
                                        </Apply>
                                    </Apply>
                                </xsl:if>
                            </xsl:for-each>
                        </Apply>
                    </Condition>
                </Rule>
            </Policy>

        </PolicySet>
    </xsl:template>

</xsl:stylesheet>