/**
 * Copyright (C) 2011-2014 Thales Services - ThereSIS - All rights reserved.
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
package com.sun.xacml.finder.impl;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ProcessingException;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
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

	@Override
	public boolean isChildSupported() {
		return true;
	}
	
	@Override
	public ResourceFinderResult findChildResources(
			AttributeValue parentResourceId, EvaluationCtx context) {
		// todo: check if evalresult not null
		EvaluationResult evalResult = context.getResourceAttribute(
				STRING_DATATYPE_URI, RESOURCE_ID_URI, ANY_ISSUER);
		if(evalResult == null) {
			throw new ProcessingException();
		}
		AttributeValueType attrVal = evalResult.getAttributeValue();
		Collection<AttributeValue> attrSubVals =  ((BagAttribute) attrVal).getValues();
		return new ResourceFinderResult(new HashSet<>(attrSubVals));
	}
}
