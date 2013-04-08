package com.sun.xacml.finder.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ProcessingException;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.ResourceFinderModule;
import com.sun.xacml.finder.ResourceFinderResult;

/**
 * @author Cyril DANGERVILLE
 * 
 */
public class MultipleResourceFinder extends ResourceFinderModule {
	
	private final static URI STRING_DATATYPE_URI = URI
			.create(StringAttribute.identifier);
	private final static URI RESOURCE_ID_URI = URI
			.create(EvaluationCtx.RESOURCE_ID);

	// Null issuer allows to retrieve attributes by any issuer, see
	// BasicEvaluationContext#getGenericAttributes()
	private final static URI ANY_ISSUER = null;

	public MultipleResourceFinder() {
		// TODO Auto-generated constructor stub
	}

	public boolean isChildSupported() {
		return true;
	}
	
	public ResourceFinderResult findChildResources(
			AttributeValue parentResourceId, EvaluationCtx context) {
		// todo: check if evalresult not null
		EvaluationResult evalResult = context.getResourceAttribute(
				STRING_DATATYPE_URI, RESOURCE_ID_URI, ANY_ISSUER);
		if(evalResult == null) {
			throw new ProcessingException();
		}
		AttributeValue attrVal = evalResult.getAttributeValue();
		List<AttributeValue> attrSubVals = (List<AttributeValue>) ((BagAttribute) attrVal).getValue();
		return new ResourceFinderResult(new HashSet<AttributeValue>(attrSubVals));
	}
}
