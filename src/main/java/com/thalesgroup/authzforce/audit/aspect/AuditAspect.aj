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
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.BasicEvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;
import com.thalesgroup.authzforce.audit.AttributesResolved;
import com.thalesgroup.authzforce.audit.AuditLog;
import com.thalesgroup.authzforce.audit.AuditLogs;
import com.thalesgroup.authzforce.audit.annotations.Audit;

@Aspect
public class AuditAspect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditAspect.class);

	@Pointcut("execution (@com.thalesgroup.authzforce.audit.annotations * *.*(..))")
	public void searchAnnotation() {

	}

	@AfterReturning(value = "@annotation(annotation)", returning = "result")
	public void doAuditLog(Audit annotation, JoinPoint jp, Object result) {
		switch (annotation.type()) {
		case RULE:
			AuditLog audit = new AuditLog();
			audit.addRule((Rule) jp.getTarget());
			audit.addResultRule((Result) result);
			for (Object arg : jp.getArgs()) {
				if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx) arg).getRequest());
				}
			}
			AuditLogs.getInstance().addAudit(audit);
			break;

		case POLICY:
			audit = new AuditLog();
			audit.addMatchPolicies((Policy) jp.getTarget());
			audit.addResultMatchPolicy((Result) result);
			for (Object arg : jp.getArgs()) {
				if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx) arg).getRequest());
				}
			}
			AuditLogs.getInstance().addAudit(audit);
			break;

		case ATTRIBUTE:
			audit = new AuditLog();
			AttributesResolved attrResolved = new AttributesResolved();
			for (Object arg : jp.getArgs()) {
				if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx) arg).getRequest());
				}
			}
			attrResolved.setAttributeValue(((EvaluationResult) result)
					.getAttributeValue());
			break;

		// Used to display and clean the audit log pool
		case DISPLAY:
			AuditLogs.getInstance().print();
			break;
		default:
			System.err.println("Type unknown: " + annotation.type());
			;
		}
	}
}