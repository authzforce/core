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
package com.thalesgroup.authzforce.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.sun.xacml.Rule;
import com.thalesgroup.authzforce.audit.AuditLogs;
import com.thalesgroup.authzforce.audit.annotations.Audit;

@Aspect
public class AuditAspect {
	
	@Before("@annotation(com.thalesgroup.authzforce.audit.annotations.Audit)")
	public void doAuditLog(Audit annotation, JoinPoint jp) {
		StringBuilder sb = new StringBuilder("\nBuilding audit logs -- ");
		sb.append("Entering into the aspect\n");
		sb.append(jp.getSourceLocation().toString() + " -- ");
		sb.append(jp.getSignature().getName() + "\n");
		sb.append("Evaluation from rule: "+jp.getSignature().getName());
		System.out.println(sb.toString());
		
		AuditLogs audit = AuditLogs.getInstance();
		switch (annotation.type()) {
		case RULE:
			System.out.println("This is a rule, adding this to the audit log");
			System.out.println("Rule id: "+ ((Rule)jp.getThis()).getId());
			break;
		default:
			break;
		}
	}
	
	@Before("call(* *..*.combine(..))")
	public void firstApplicableAspect() {
		System.out.println("AuditAspect.firstApplicableAspect()");
	}
	
	@Before("execute(* *..*.combine(..))")
	public void firstApplicableAspectExecute() {
		System.out.println("AuditAspect.firstApplicableAspectExecute()");
	}
}