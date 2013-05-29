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

import java.util.ArrayList;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.sun.xacml.BasicEvaluationCtx;
import com.sun.xacml.Rule;
import com.thalesgroup.authzforce.audit.AuditLog;
import com.thalesgroup.authzforce.audit.AuditLogs;
import com.thalesgroup.authzforce.audit.annotations.Audit;

@Aspect
public class AuditAspect {

	@Pointcut("execution (@com.thalesgroup.authzforce.audit.annotations * *.*(..))")
	public void searchAnnotation() {

	}

	@AfterReturning(value = "@annotation(annotation)", returning="result")
	public void doAuditLog(Audit annotation, JoinPoint jp, Object result) {
		StringBuilder sb = new StringBuilder("\n-- Building "+annotation.type()+" audit logs -- \n");
		sb.append(jp.getSourceLocation().toString() + " -- ");
		sb.append(jp.getSignature().getName() + "\n");
		
		switch (annotation.type()) {
		case RULE:
			AuditLog audit = new AuditLog();
			for (Object arg : jp.getArgs()) {
				if(arg instanceof ArrayList<?>) {
					for (Rule ruleElt : (ArrayList<Rule>)arg) {
						audit.addRule(ruleElt);						
					}
				} else if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx)arg).getRequest());
				}
			}		
			AuditLogs.getInstance().addAudit(audit);
			break;
		}
		System.out.println(sb.toString());
		
		
		
	}
}