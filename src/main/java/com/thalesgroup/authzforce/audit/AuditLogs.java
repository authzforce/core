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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thalesgroup.authzforce.audit.schema.pdp.AuditLog;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditedPolicy;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditedRule;

/**
 * Audit logging class
 * 
 * Note: to disable audit logging, just change log level for logger named
 * 'com.thalesgroup.authzforce.audit.AuditLogs' in the SLF4J logger
 * configuration file. Use level <= INFO to enable, level > INFO to disable.
 * Other options on this logger may be configured in the logging configuration
 * file to customize audit logging.
 * 
 */
public final class AuditLogs {

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AuditLogs.class);

	private static final String HASH_ALG = "MD5";

	protected static volatile AuditLogs INSTANCE;

	protected static HashMap<String, com.thalesgroup.authzforce.audit.schema.pdp.AuditLog> audits;

	private AuditLogs() {
		audits = new HashMap<String, com.thalesgroup.authzforce.audit.schema.pdp.AuditLog>();
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

	public synchronized static Map<String, com.thalesgroup.authzforce.audit.schema.pdp.AuditLog> getAudits() {
		return audits;
	}
	
	public synchronized static void clearAudits() {
		audits.clear();
	}

	public synchronized static void addAudit(
			com.thalesgroup.authzforce.audit.schema.pdp.AuditLog audit) {
		if (audits == null) {
			audits = new HashMap<String, com.thalesgroup.authzforce.audit.schema.pdp.AuditLog>();
		}
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALG);
			byte[] hash = digest
					.digest(String.valueOf(
							audit.getDate() + audit.getRequest().hashCode())
							.getBytes());
			String id = byte2String(hash);
			audit.setId(id);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (audits.containsKey(audit.getId())) {
			audits.put(audit.getId(), updateAudit(audit));
		} else {
			audits.put(audit.getId(), audit);
		}
	}

	private static com.thalesgroup.authzforce.audit.schema.pdp.AuditLog updateAudit(
			com.thalesgroup.authzforce.audit.schema.pdp.AuditLog newAudit) {
		com.thalesgroup.authzforce.audit.schema.pdp.AuditLog updatedAudit = audits
				.get(newAudit.getId());
		// Updating rules and decision for rules
		if (newAudit.getRules().size() > 0) {
			int i = 0;
			for (AuditedRule ruleElt : newAudit.getRules()) {
				// We don't want any doublon. TODO: Maybe use a set ?
				if (!updatedAudit.getRules().contains(ruleElt)) {
					updatedAudit.getRules().add(ruleElt);
					// If we've got a rule, we've got a decision
//					 updatedAudit.getResultRules().add(newAudit.getResultRules().get(i));
				}
				i++;
			}
		}

		if (newAudit.getMatchedPolicies().size() > 0) {
			int i = 0;
			for (AuditedPolicy policyElt : newAudit.getMatchedPolicies()) {
				// We don't want any doublon. TODO: Maybe use a set ?
				if (!updatedAudit.getMatchedPolicies().contains(policyElt)) {
					updatedAudit.getMatchedPolicies().add(policyElt);
					// If we've got a rule, we've got a decision
					// updatedAudit.getResultPolicies().add(newAudit.getResultPolicies().get(i));
				}
				i++;
			}
		}

		return updatedAudit;
	}

	/**
	 * One part of the method needs to be synchronized as it modify the map
	 * containing the audits logs. No one should update this map while it's
	 * being displayed and cleared.
	 */
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		Map<String, AuditLog> tmpMap;
		synchronized (INSTANCE) {
			tmpMap = getAudits();	
		}		
		try {
			for (String hash : tmpMap.keySet()) {
				// JAXBContext.newInstance(AuditLog.class).createMarshaller().marshal(getAudits().get(hash),
				// sw);

				ObjectMapper mapper = new ObjectMapper();
				AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
				// make serializer use JAXB annotations (only)
				mapper.getSerializationConfig().withAnnotationIntrospector(
						introspector);
				synchronized (INSTANCE) {
					mapper.writeValue(sw, getAudits().get(hash));	
				}				
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (INSTANCE) {
			clearAudits();	
		}		
		return sw.toString();
	}

	private static String byte2String(byte[] hash) {
		StringBuffer id = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				id.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				id.append(Integer.toHexString(0xFF & hash[i]));
			}
		}

		return id.toString();
	}
}
