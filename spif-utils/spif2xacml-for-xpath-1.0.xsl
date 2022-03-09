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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:spif="http://www.xmlspif.org/spif" xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Start with root element... -->
	<xsl:template match="/spif:SPIF">
		<PolicySet xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" PolicySetId="{spif:securityPolicyId/@id}" Version="{@version}" PolicyCombiningAlgId="urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second">
			<Description>
				<xsl:text>Translated from SPIF: </xsl:text><xsl:value-of select="spif:securityPolicyId/@name" /><xsl:text> v</xsl:text><xsl:value-of select="@version" /><xsl:text>.
				Both PolicyIdentifiers from confidentiality clearance (subject) and label (resource) must match the SPIF's.</xsl:text>
			</Description>
			<PolicySetDefaults>
				<XPathVersion>http://www.w3.org/TR/2007/REC-xpath20-20070123</XPathVersion>
			</PolicySetDefaults>
			<Target />

			<PolicySet PolicySetId="SPIF_PolicyIdentifier_and_classifs_PS" Version="1.0" PolicyCombiningAlgId="urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit">
				<Target>
					<!-- PolicyIdentifier match -->
					<AnyOf>
						<AllOf>
							<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
								<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="spif:securityPolicyId/@name" /></AttributeValue>
								<AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource" Path="//*:PolicyIdentifier/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
							</Match>
							<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
								<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="spif:securityPolicyId/@name" /></AttributeValue>
								<AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject" Path="//*:PolicyIdentifier/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
							</Match>
						</AllOf>
					</AnyOf>
				</Target>

				<xsl:for-each select="//spif:securityClassification">
					<xsl:variable name="resource_classif_level" select="@hierarchy" />
					<Policy PolicyId="resource_classif_{@name}_P" Version="1.0" RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit">
						<Target>
							<AnyOf>
								<AllOf>
									<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
										<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="@name" /></AttributeValue>
										<AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource" Path="//*:Classification/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
									</Match>
								</AllOf>
							</AnyOf>
						</Target>

						<Rule RuleId="READ_action_R" Effect="Permit">
							<Target>
								<AnyOf>
									<AllOf>
										<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
											<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">READ</AttributeValue>
											<AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action" AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
										</Match>
									</AllOf>
								</AnyOf>
								<AnyOf>
									<!-- subject must have clearance/clearance greater than or equal to resource classif -->
									<xsl:for-each select="//spif:securityClassification[@hierarchy >= $resource_classif_level]">
										<AllOf>
											<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
												<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="@name" /></AttributeValue>
												<AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject" Path="//*:Classification/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
											</Match>
										</AllOf>
									</xsl:for-each>
								</AnyOf>
							</Target>
						</Rule>

						<Rule RuleId="WRITE_action_R" Effect="Permit">
							<Target>
								<AnyOf>
									<AllOf>
										<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
											<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">WRITE</AttributeValue>
											<AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action" AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
										</Match>
									</AllOf>
								</AnyOf>
								<AnyOf>
									<!-- subject must have clearance/clearance greater than or equal to resource classif -->
									<xsl:for-each select="//spif:securityClassification[@hierarchy &lt;= $resource_classif_level]">
										<AllOf>
											<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
												<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="@name" /></AttributeValue>
												<AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject" Path="//*:Classification/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true" />
											</Match>
										</AllOf>
									</xsl:for-each>
								</AnyOf>
							</Target>
						</Rule>
					</Policy>

				</xsl:for-each>

			</PolicySet>

			<Policy PolicyId="other_categories_match_P" Version="1.0" RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit">
				<Target />
				<Rule RuleId="other_categories_match_R" Effect="Permit">
					<Condition>
						<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:and">
							<!-- For each restrictive category -->
							<xsl:for-each select="//spif:securityCategoryTag">
								<!-- Ignore informative categories -->
								<xsl:if test="@tagType !='tagType7'">
								<!-- Either resource does not have this category (empty bag) or at least one value of this category must match the subject clearance -->
								<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:or">
									<!-- Test if resource has this category -->
									<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:integer-equal">
										<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">
											<AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource" Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false" />
										</Apply>
										<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#integer">0</AttributeValue>
									</Apply>
									<!-- Test if at least one value the resource category matches the subject clearance -->
									<xsl:variable name="comparFuncId"><xsl:choose>
										<xsl:when test="@tagType='permissive' or @enumType='permissive'">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of</xsl:when>
										<!-- otherwise @tagType='restrictive' -->
										<xsl:otherwise>urn:oasis:names:tc:xacml:1.0:function:string-subset</xsl:otherwise>
									</xsl:choose></xsl:variable>
									<Apply FunctionId="{$comparFuncId}">
										<AttributeSelector Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource" Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false" />
										<AttributeSelector Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject" Path="//*:Category[@TagName='{@name}']/*:GenericValue/text()" DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false" />
									</Apply>
								</Apply>
								</xsl:if>
							</xsl:for-each>
						</Apply>
					</Condition>
				</Rule>
			</Policy>

			<Policy PolicyId="default_deny_P" Version="1.0" RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit">
				<Target />
				<Rule RuleId="default_deny_R" Effect="Deny" />
			</Policy>

		</PolicySet>
	</xsl:template>

</xsl:stylesheet>