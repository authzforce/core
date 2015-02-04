/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.audit;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	protected static ConcurrentHashMap<String, com.thalesgroup.authzforce.audit.schema.pdp.AuditLog> audits;

	private AuditLogs() {
		audits = new ConcurrentHashMap<>();
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
			audits = new ConcurrentHashMap<>();
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
