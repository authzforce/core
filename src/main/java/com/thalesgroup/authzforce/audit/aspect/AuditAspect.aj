package com.thalesgroup.authzforce.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.sun.xacml.BasicEvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;
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
		switch (annotation.type()) {
		case RULE:
			AuditLog audit = new AuditLog();
			audit.addRule((Rule)jp.getTarget());
			audit.addResultRule((Result)result);
			for (Object arg : jp.getArgs()) {
				if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx)arg).getRequest());
				}
			}		
			AuditLogs.getInstance().addAudit(audit);
			break;
			
		case POLICY:
			audit = new AuditLog();
			audit.addMatchPolicies((Policy)jp.getTarget());
			audit.addResultMatchPolicy((Result)result);
			for (Object arg : jp.getArgs()) {
				if (arg instanceof BasicEvaluationCtx) {
					audit.setRequest(((BasicEvaluationCtx)arg).getRequest());
				}
			}
			AuditLogs.getInstance().addAudit(audit);
			break;
			
			//Used to display and clean the audit log pool
		case DISPLAY:
			AuditLogs.getInstance().print();
			break;
		default:
			System.err.println("Type unknown: "+annotation.type());;
		}
	}
}