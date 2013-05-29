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

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;

import com.sun.xacml.PolicySet;
import com.sun.xacml.Rule;
import com.sun.xacml.xacmlv3.Policy;

public class AuditLog {

	protected String id;

	protected Timestamp date;

	protected RequestType request;

	protected List<Policy> matchPolicies;

	protected List<PolicySet> matchPolicieSet;

	protected List<Rule> rules;

	protected List<AttributesResolved> attrResolv;

	public AuditLog() {
		date = new Timestamp((new java.util.Date()).getTime());
		rules = new LinkedList<Rule>();
		matchPolicies = new LinkedList<Policy>();
		matchPolicieSet = new LinkedList<PolicySet>();
		attrResolv = new LinkedList<AttributesResolved>();
	}

	public String getId() {
		return id;
	}

	/**
	 * This should only be called by the AuditsLog class since the ID MUST be
	 * unique and fixed by a higher class
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	public RequestType getRequest() {
		return request;
	}

	public void setRequest(RequestType request) {
		this.request = request;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void addRule(Rule rule) {
		this.rules.add(rule);
	}

	public String getDate() {
		return date.toString();
	}

	public List<Policy> getMatchPolicies() {
		return matchPolicies;
	}

	public void addMatchPolicies(Policy matchPolicy) {
		this.matchPolicies.add(matchPolicy);
	}

	public List<AttributesResolved> getAttrResolv() {
		return attrResolv;
	}

	public void setAttrResolv(List<AttributesResolved> attrResolv) {
		this.attrResolv = attrResolv;
	}

}
