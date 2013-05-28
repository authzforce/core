package com.thalesgroup.authzforce.audit;

import java.util.ArrayList;
import java.util.List;

public class AuditLogs { 
	
	protected static AuditLogs audit;
	
	protected Request request;
	
	protected String effect;
	
	protected List<MatchPolicies> matchPolicies;
	
	protected String ruleId; 
	
	protected String date;
	
	protected List<AttributesResolved> attrResolv = new ArrayList<AttributesResolved>();
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}	

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
