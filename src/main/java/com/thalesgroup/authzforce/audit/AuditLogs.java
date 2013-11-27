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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.Rule;
import com.sun.xacml.xacmlv3.Policy;

/**
 * Audit logging class
 * 
 * Note: to disable audit logging, just change log level for logger named
 * 'com.thalesgroup.authzforce.audit.AuditLogs' in the SLF4J logger configuration file. Use level <=
 * INFO to enable, level > INFO to disable. Other options on this logger may be configured in the
 * logging configuration file to customize audit logging.
 * 
 */
public final class AuditLogs
{

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogs.class);

	protected static volatile AuditLogs INSTANCE;
	protected static HashMap<String, AuditLog> audits;

	private AuditLogs()
	{
		audits = new HashMap<String, AuditLog>();
	}

	public synchronized static AuditLogs getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new AuditLogs();
		}
		return INSTANCE;
	}

	public synchronized static AuditLogs remove()
	{
		if (INSTANCE != null)
		{
			INSTANCE = new AuditLogs();
		}
		return INSTANCE;
	}

	public synchronized static Map<String, AuditLog> getAudits()
	{
		return audits;
	}

	public synchronized static void addAudit(AuditLog audit)
	{
		// BASE64Encoder enc = new sun.misc.BASE64Encoder();
		// String toEncode = audit.getRequest().toString() + audit.getDate();
		// String base64 = enc.encode(toEncode.getBytes());
		if (audits == null)
		{
			audits = new HashMap<String, AuditLog>();
		}
		audit.setId(String.valueOf(audit.getRequest().hashCode()));
		if (audits.containsKey(audit.getId()))
		{
			audits.put(audit.getId(), updateAudit(audit));
		} else
		{
			audits.put(audit.getId(), audit);
		}
	}

	private static AuditLog updateAudit(AuditLog newAudit)
	{
		AuditLog updatedAudit = audits.get(newAudit.getId());
		// Updating rules and decision for rules
		if (newAudit.getRules().size() > 0)
		{
			int i = 0;
			for (Rule ruleElt : newAudit.getRules())
			{
				// We don't want any doublon. TODO: Maybe use a set ?
				if (!updatedAudit.getRules().contains(ruleElt))
				{
					updatedAudit.addRule(ruleElt);
					// If we've got a rule, we've got a decision
					updatedAudit.addResultRule(newAudit.getResultRule().get(i));
				}
				i++;
			}
		}

		if (newAudit.getMatchPolicies().size() > 0)
		{
			int i = 0;
			for (Policy policyElt : newAudit.getMatchPolicies())
			{
				// We don't want any doublon. TODO: Maybe use a set ?
				if (!updatedAudit.getMatchPolicies().contains(policyElt))
				{
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
	 * One part of the method needs to be synchronized as it modify the map containing the audits
	 * logs. No one should update this map while it's being displayed and cleared.
	 */
	public void print()
	{
		Map<String, AuditLog> auditTmp = null;
		synchronized (audits)
		{
			auditTmp = new HashMap<String, AuditLog>(audits);
			audits.clear();
		}
		for (AuditLog auditElt : auditTmp.values())
		{
			StringBuilder sb = new StringBuilder();
			sb.append(auditElt.print());
			sb.append("\n");

			/**
			 * To disable audit logging at this point, just change log level for logger named
			 * 'com.thalesgroup.authzforce.audit.AuditLogs' in the SLF4J logger configuration Use
			 * level <= INFO to enable, level > INFO to disable.
			 */
			LOGGER.info(sb.toString());
		}
		
		// Releasing memory 
		auditTmp = null;		
//		System.out.println(sb);		
	}
}
