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

import java.util.LinkedList;
import java.util.List;

import sun.misc.BASE64Encoder;

public final class AuditLogs {

	/**
	 * Multiplier to generate unique id. TODO: generate a Hash (base64?) for ID
	 */
	protected static volatile AuditLogs INSTANCE;
	protected static LinkedList<AuditLog> audits;

	private AuditLogs() {
//		INSTANCE = new AuditLogs();
		audits = new LinkedList<AuditLog>();
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

	public synchronized static List<AuditLog> getAudits() {
		return audits;
	}

	public synchronized static void addAudit(AuditLog audit) {
		BASE64Encoder enc = new sun.misc.BASE64Encoder();
		String toEncode = audit.getRequest().toString() + audit.getDate();
		String base64 = enc.encode(toEncode.getBytes());
		if (audits == null) {
			audits = new LinkedList<AuditLog>();
		}
		audit.setId(base64);
		audits.add(audit);
	}
}
