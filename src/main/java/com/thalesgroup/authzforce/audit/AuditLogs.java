package com.thalesgroup.authzforce.audit;

import java.util.HashMap;
import java.util.Map;

import com.sun.xacml.Rule;
import com.sun.xacml.xacmlv3.Policy;

public final class AuditLogs {
	
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(AuditLogs.class);

	protected static volatile AuditLogs INSTANCE;
	protected static HashMap<String, AuditLog> audits;

	private AuditLogs() {
		audits = new HashMap<String, AuditLog>();
	}

	public synchronized static AuditLogs getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AuditLogs();
		}
		return INSTANCE;
	}

	public synchronized static AuditLogs remove() {
		if (INSTANCE != null) {
			INSTANCE = new AuditLogs();
		}
		return INSTANCE;
	}

	public synchronized static Map<String, AuditLog> getAudits() {
		return audits;
	}

	public synchronized static void addAudit(AuditLog audit) {
//		BASE64Encoder enc = new sun.misc.BASE64Encoder();
//		String toEncode = audit.getRequest().toString() + audit.getDate();
//		String base64 = enc.encode(toEncode.getBytes());
		if (audits == null) {
			audits = new HashMap<String, AuditLog>();
		}
		audit.setId(String.valueOf(audit.getRequest().hashCode()));
		if(audits.containsKey(audit.getId())) {
			audits.put(audit.getId(), updateAudit(audit));
		} else {
			audits.put(audit.getId(), audit);
		}
	}
	
	private static AuditLog updateAudit(AuditLog newAudit) {
		AuditLog updatedAudit = audits.get(newAudit.getId());
		// Updating rules and decision for rules
		if(newAudit.getRules().size() > 0 ) {
			int i = 0;
			for (Rule ruleElt : newAudit.getRules()) {
				// We don't want any doublon. TODO: Maybe use a set ?
				if(!updatedAudit.getRules().contains(ruleElt)) {
					updatedAudit.addRule(ruleElt);
					// If we've got a rule, we've got a decision
					updatedAudit.addResultRule(newAudit.getResultRule().get(i));				
				}
				i++;
			}
		}
		
		if(newAudit.getMatchPolicies().size() > 0) {
			int i = 0;
			for (Policy policyElt : newAudit.getMatchPolicies()) {
				// We don't want any doublon. TODO: Maybe use a set ?
				if(!updatedAudit.getMatchPolicies().contains(policyElt)) {
					updatedAudit.addMatchPolicies(policyElt);
					// If we've got a rule, we've got a decision
					updatedAudit.addResultMatchPolicy(newAudit.getResultMatchPolicy().get(i));
				}
				i++;
			}
		}
		
		return updatedAudit;
	}

	/**
	 * One part of the method needs to be synchronized as it modify the map containing
	 * the audits logs. No one should update this map while it's being displayed
	 * and cleared.
	 */
	public void print() {
		Map<String, AuditLog> auditTmp = null;
		synchronized (audits) {
			auditTmp = new HashMap<String, AuditLog>(audits);
			audits.clear();	
		}		
		for (AuditLog auditElt : auditTmp.values()) {
			StringBuilder sb = new StringBuilder();
			sb.append(auditElt.print());
			sb.append("\n");
			LOGGER.log(AuditLevel.AUDIT, sb);
		}
		
		// Releasing memory 
		auditTmp = null;
//		System.out.println(sb);		
	}
}
