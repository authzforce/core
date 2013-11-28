/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.thalesgroup.authzforce.audit;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.PolicySet;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.xacmlv3.Policy;
import com.thalesgroup.authzforce.BindingUtility;

public class AuditLog
{

	private static final String SEPARATOR = "\t--\t";

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);

	protected String id;

	protected Timestamp date;

	protected Request request;

	protected List<Policy> matchPolicies;

	protected LinkedList<Result> resultPolicies;

	protected List<PolicySet> matchPolicieSet;

	protected LinkedList<Rule> rules;

	protected LinkedList<Result> resultRule;

	protected List<AttributesResolved> attrResolv;

	public AuditLog()
	{
		date = new Timestamp((new java.util.Date()).getTime());
		rules = new LinkedList<Rule>();
		resultRule = new LinkedList<Result>();
		matchPolicies = new LinkedList<Policy>();
		resultPolicies = new LinkedList<Result>();
		matchPolicieSet = new LinkedList<PolicySet>();
		attrResolv = new LinkedList<AttributesResolved>();
	}

	public String getId()
	{
		return id;
	}

	/**
	 * This should only be called by the AuditsLog class since the ID MUST be unique and fixed by a
	 * higher class
	 * 
	 * @param id
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	public Request getRequest()
	{
		return request;
	}

	public void setRequest(Request request)
	{
		this.request = request;
	}

	public List<Rule> getRules()
	{
		return rules;
	}

	public void addRule(Rule rule)
	{
		this.rules.add(rule);
	}

	/**
	 * @return the resultRule
	 */
	protected LinkedList<Result> getResultRule()
	{
		return resultRule;
	}

	/**
	 * @param resultRule
	 *            the resultRule to set
	 */
	public void addResultRule(Result resultRule)
	{
		this.resultRule.add(resultRule);
	}

	public String getDate()
	{
		return date.toString();
	}

	public List<Policy> getMatchPolicies()
	{
		return matchPolicies;
	}

	public void addMatchPolicies(Policy matchPolicy)
	{
		this.matchPolicies.add(matchPolicy);
	}

	public void addResultMatchPolicy(Result result)
	{
		resultPolicies.add(result);
	}

	/**
	 * @return the resultMatchPolicies
	 */
	protected LinkedList<Result> getResultMatchPolicy()
	{
		return resultPolicies;
	}

	public List<AttributesResolved> getAttrResolv()
	{
		return attrResolv;
	}

	public void setAttrResolv(List<AttributesResolved> attrResolv)
	{
		this.attrResolv = attrResolv;
	}

	public String print()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + this.getId() + SEPARATOR);
		sb.append("Timestamp: " + this.getDate() + "\n");
		sb.append("Request:\n" + request2String(this.getRequest()) + "\n");
		if (this.getMatchPolicies().size() > 0)
		{
			int i = 0;
			sb.append("Matched Policies:\n");
			for (Policy policy : this.getMatchPolicies())
			{
				// We fetch the string after "rule-combining-algorithm:" in order to display only
				// the algorithm
				String ruleCombAlg = policy.getRuleCombiningAlgId().substring(
						policy.getRuleCombiningAlgId().indexOf("rule-combining-algorithm") + "rule-combining-algorithm:".length());
				sb.append("\t");
				sb.append("Policy ID:\t" + policy.getPolicyId() + SEPARATOR + ruleCombAlg + SEPARATOR + "Combinated Evaluation:\t"
						+ ((Result) this.getResultMatchPolicy().get(i)).getDecision());
				sb.append("\n");
				i++;
			}
		}
		if (this.getRules().size() > 0)
		{
			int i = 0;
			for (Rule rule : this.getRules())
			{
				sb.append("\t");
				sb.append("\tRule ID:\t" + rule.getRuleId() + SEPARATOR + "Evaluation:\t" + ((Result) this.getResultRule().get(i)).getDecision());
				sb.append("\n");
				i++;
			}
		}
		if (this.getAttrResolv().size() > 0)
		{
			int i = 0;
			for (AttributesResolved attrResolved : this.getAttrResolv())
			{
				sb.append("\t");				
				sb.append("\tAttribute ID:\t" + attrResolved.getAttributeId() + SEPARATOR + "Value:\t" + attrResolved.getAttributeValue().getContent());
				sb.append("\n");
				i++;
			}
		}

		return sb.toString();
	}

	private String request2String(Request request)
	{
		String result = null;
		final StringWriter sw = new StringWriter();
		try
		{
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(request, sw);
			result = sw.toString();
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Request", e);
		} finally
		{
			try
			{
				sw.close();
			} catch (IOException e)
			{
				LOGGER.error("Error closing StringWriter for marshalling Request", e);
			}
		}

		return result;
	}

	public void addAttrResolved(AttributeFinderModule attributeFinderModule) {
		System.out.println("addAttrResolved");
		
	}
}
