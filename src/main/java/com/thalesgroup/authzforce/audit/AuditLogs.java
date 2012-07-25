package com.thalesgroup.authzforce.audit;

import java.util.ArrayList;
import java.util.List;

public class AuditLogs {
	
	private static AuditLogs audit;
	
	Request request;
	
	String effect;
	
	List<MatchPolicies> matchPolicies;
	
	String ruleId; 
	
	String date; 
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	List<AttributesResolved> attrResolv = new ArrayList<AttributesResolved>();

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}

	public List<MatchPolicies> getMatchPolicies() {
		return matchPolicies;
	}

	public void setMatchPolicies(List<MatchPolicies> matchPolicies) {
		this.matchPolicies = matchPolicies;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public List<AttributesResolved> getAttrResolv() {
		return attrResolv;
	}

	public void setAttrResolv(List<AttributesResolved> attrResolv) {
		this.attrResolv = attrResolv;
	} 
	
	public synchronized static AuditLogs getInstance(){
		if (audit == null){
			audit = new AuditLogs();
		}
		return audit;
	}
	
	public synchronized static AuditLogs remove(){
		if (audit != null){
			audit = new AuditLogs();
		}
		return audit;
	}
	
}
