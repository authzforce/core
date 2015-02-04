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
package com.thalesgroup.authzforce.audit.aspect;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.BasicEvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.thalesgroup.authzforce.audit.AttributesResolved;
import com.thalesgroup.authzforce.audit.AuditLogs;
import com.thalesgroup.authzforce.audit.annotations.Audit;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditLog;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditedPolicy;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditedResult;
import com.thalesgroup.authzforce.audit.schema.pdp.AuditedRule;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLDatatypes;

@Aspect
public class AuditAspect
{

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditAspect.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSSSSS");
	private static final DateFormat dateFormatWOoNano = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	/**
	 * Evaluation context key indicating that we are in a doAuditLog() call.
	 */
	private final String doAuditLogCallCtxKey = this.toString();

	@Pointcut("execution (@com.thalesgroup.authzforce.audit.annotations * *.*(..))")
	public void searchAnnotation()
	{

	}

	@AfterReturning(value = "@annotation(annotation)", returning = "result")
	public void doAuditLog(Audit annotation, JoinPoint jp, Object result)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("ASPECTJ: entering doAuditLog() for joinpoint kind='{}', target='{}',  signature='{}'",
					new Object[] { jp.getKind(), jp.getTarget(), jp.getSignature() });
		}

		BasicEvaluationCtx evalCtx = null;
		for (Object arg : jp.getArgs())
		{
			if (arg instanceof BasicEvaluationCtx)
			{
				evalCtx = (BasicEvaluationCtx) arg;
				if (evalCtx.containsKey(doAuditLogCallCtxKey))
				{
					/*
					 * So we are in a doAuditLog() call from another doAuditLog() call already, i.e.
					 * we are in a process of calling this method recursively, so we don't do it
					 * again; there we stop here
					 */
					LOGGER.debug("ASPECTJ: this doAuditLog() call is already triggered by another doAuditLog() call -> return (skip this time)");
					return;
				}
			}
		}

		if(evalCtx != null) {
			evalCtx.put(doAuditLogCallCtxKey, null);
		}
		
		try
		{
			switch (annotation.type())
			{
				case RULE: {
					final Rule rule = (Rule) jp.getTarget();
					final Result evalResult = (Result) result;
					LOGGER.debug("{} -> {}", rule, evalResult);			
					AuditLog audit = new AuditLog();
					AuditedRule auditedRule = new AuditedRule();
					auditedRule.setId(rule.getRuleId());
					auditedRule.setResult(AuditedResult.fromValue(evalResult.getDecision().value()));
					if (evalCtx != null)
					{
						audit.setDate(this.formatDate(evalCtx.getCurrentDateTime()));
						audit.setRequest(evalCtx.getRequest());
						audit = this.setAttributes(audit, evalCtx);
					}

					audit.getRules().add(auditedRule);

					synchronized (audit)
					{
						AuditLogs.getInstance().addAudit(audit);
					}
					break;
				}
				
				case POLICY: {
					final Policy policy = (Policy) jp.getTarget();
					final Result evalResult = (Result) result;
					LOGGER.debug("{} -> {}", policy, evalResult);	
					AuditLog audit = new AuditLog();
					AuditedPolicy auditedPolicy = new AuditedPolicy();
					auditedPolicy.setId(policy.getPolicyId());
					auditedPolicy.setRuleCombiningAlgorithm(policy.getRuleCombiningAlgId());
					auditedPolicy.setResult(AuditedResult.fromValue(evalResult.getDecision().value()));
					if (evalCtx != null)
					{
						audit.setDate(this.formatDate(evalCtx.getCurrentDateTime()));
						audit.setRequest(evalCtx.getRequest());
						audit = this.setAttributes(audit, evalCtx);
					}

					audit.getMatchedPolicies().add(auditedPolicy);
					synchronized (audit)
					{
						AuditLogs.getInstance().addAudit(audit);
					}
					break;
				}
				
				// FIXME: case POLICYSET ?
				
				case ATTRIBUTE: {
					AuditLog audit = new AuditLog();
					AttributesResolved attrResolved = new AttributesResolved();
					if (evalCtx != null)
					{
						audit.setDate(this.formatDate(evalCtx.getCurrentDateTime()));
						audit.setRequest(evalCtx.getRequest());
						audit = this.setAttributes(audit, evalCtx);
					}

					attrResolved.setAttributeValue(((EvaluationResult) result).getAttributeValue());
					break;
				}
				
				// Used to display and clean the audit log pool
				case DISPLAY:
					LOGGER.info(AuditLogs.getInstance().toString());
					break;
				default:
					LOGGER.error("Type unknown: " + annotation.type());
			}
		} finally
		{
			/*
			 * We are done with calling this method, so remove the key indicating that we are in a
			 * doAuditLog() call
			 */
			if(evalCtx != null) {
				evalCtx.remove(doAuditLogCallCtxKey);
			}
		}
	}

	private AuditLog setAttributes(AuditLog audit, BasicEvaluationCtx basicEvaluationCtx)
	{
		// FIXME: lots of redundant code here
		if (((BasicEvaluationCtx) basicEvaluationCtx).getSubjectAttribute(URI.create(XACMLDatatypes.XACML_DATATYPE_STRING.value()),
				URI.create(XACMLAttributeId.XACML_SUBJECT_SUBJECT_ID.value()), URI.create(XACMLAttributeId.SUBJECT_CATEGORY.value())) != null)
		{
			if (((BasicEvaluationCtx) basicEvaluationCtx)
					.getSubjectAttribute(URI.create(XACMLDatatypes.XACML_DATATYPE_STRING.value()),
							URI.create(XACMLAttributeId.XACML_SUBJECT_SUBJECT_ID.value()), URI.create(XACMLAttributeId.SUBJECT_CATEGORY.value()))
					.getAttributeValue().getContent().size() > 0)
			{
				String subjectId = (String) ((BasicEvaluationCtx) basicEvaluationCtx)
						.getSubjectAttribute(URI.create(XACMLDatatypes.XACML_DATATYPE_STRING.value()),
								URI.create(XACMLAttributeId.XACML_SUBJECT_SUBJECT_ID.value()), URI.create(XACMLAttributeId.SUBJECT_CATEGORY.value()))
						.getAttributeValue().getContent().get(0);

				audit.setSubjectId(subjectId);
			}
		}

		AttributeValue resourceId = ((BasicEvaluationCtx) basicEvaluationCtx).getResourceId();
		if (resourceId.getContent().size() > 0)
		{
			audit.setResourceId(String.valueOf(resourceId.getContent().get(0)));
		}

		// FIXME: Set the ActionId attribute
		// audit.setActionId();

		return audit;
	}

	private String formatDate(DateTimeAttribute currentDateTime)
	{
		String date = dateFormatWOoNano.format(currentDateTime.getValue()) + "." + String.valueOf(currentDateTime.getNanoseconds());

		return date;
	}
}