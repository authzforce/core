Test for reproducing and fixing issue #19 (Gitlab): request without subject-id results in error described in issue #16: StackOverflowError with Audit log aspect

Infinite loop call during AttributeDesignator evaluation: (read stack from bottom to top)
The cause: the Audit aspect class method setAttributes() tries to get the subject ID attribute whenever doAuditLog() is called on AttributeFinder.findAttribute(). But this getSubjectAttribute() call itself triggers a call to AttributeFinder.findAttribute() on any attribute finder module supporting the subject category, therefore the endless loop.

```
...
AuditAspect.doAuditLog(Audit, JoinPoint, Object) line: 117
AttributeFinder.findAttribute(URI, URI, URI, URI, EvaluationCtx, int) line: 169 // <Repeated Block>
BasicEvaluationCtx.callHelper(URI, URI, URI, URI, int) line: 951
BasicEvaluationCtx.getGenericAttributes(URI, URI, URI, Map<String,Set<Attribute>>, URI, int) line: 923
BasicEvaluationCtx.getSubjectAttribute(URI, URI, URI, URI) line: 706
BasicEvaluationCtx.getSubjectAttribute(URI, URI, URI) line: 661
AuditAspect.setAttributes(AuditLog, BasicEvaluationCtx) line: 135
AuditAspect.doAuditLog(Audit, JoinPoint, Object) line: 117     // </Repeated Block>
AttributeFinder.findAttribute(URI, URI, URI, URI, EvaluationCtx, int) line: 169
BasicEvaluationCtx.callHelper(URI, URI, URI, URI, int) line: 951
BasicEvaluationCtx.getGenericAttributes(URI, URI, URI, Map<String,Set<Attribute>>, URI, int) line: 923
BasicEvaluationCtx.getSubjectAttribute(URI, URI, URI, URI) line: 706
AttributeDesignator.evaluate(EvaluationCtx) line: 375
```

To reproduce the bug, we have to define some policy and request so that the attribute subject-id or other subject attribute is used in the policy but is not in the request, and therefore the PDP tries to use any subject attribute finder and calls AttributeFinderModule.findAttribute() on it. This triggers a called to getSubjectAttribute(), and therefore any attribute finder such that AttributeFinderModule.getSupportedDesignatorTypes() contains the subject category type identifier. This wil call findAttribute() again, resulting in endless loop. So we have to define such subject attribute finder in the PDP configuration. 


In general, such issues comes up when a ASPECTJ annotation is applied on a method  that is called by some method in the aspect class that is already "called" by ASPECTJ because of the same ASPECTJ annotation. In this case, the auditing aspect is calling a method (e.g. AttributeFinderModule#findAttribute()) that has audit annotation itself, therefore callling the auditing aspect again; i.e. auditing auditing itself. To prevent that, we should make sure that aspectj annotation has no effect until done running the aspect, and back to the original code.
