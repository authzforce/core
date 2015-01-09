/**
 * Copyright (C) 2011-2015 Thales Services SAS - All rights reserved.
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
/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.ctx.Result;

/**
 * Policy Element handler interface, "Policy Element" referring to all Policy* element: Policy,
 * PolicySet, PolicyIdReference (handled by PolicyReference), PolicySetIdReference (handled by
 * PolicySetReference). All these classes have common behavior which is captured by this interface
 * to benefit from polymorphism, therefore more reusable (-> less) code. Actually this class intends
 * to replace AbstractPolicy which can no longer be used as mother class to (xacmlv3.)Policy(Set)
 * and Policy(Set)Reference (because it is an abstract class), as long as those classes already
 * extend another class from JAXB model (e.g. com.sun.xacml.xacmlv3.Policy extends
 * oasis.names...Policy), so we can only make them implement an Interface instead, as Java does not
 * allow extension of multiple abstract classes.
 * 
 * FIXME: is this a good practive to have Policy handler classes (in com.sun.xacml.*) extend JAXB
 * model classes anyway? The alternative would be to have the model class instances as members of
 * these handler classes (ie. use composition instead of extension), and these Policy handler class
 * could then extend AbstractPolicy like before.
 * 
 */
public interface IPolicy extends IDecidable
{
	/**
	 * Get element version
	 * 
	 * @return version identifier
	 */
	String getVersion();
	
	/**
	 * Get metadata
	 * 
	 * @return metadata
	 */
	PolicyMetaData getMetaData();
	
	List getChildren();
	
	List getChildElements();
	
	/**
	 * @return combining algorithm (policy combining for PolicySets, rule combining for Policies)
	 */
	CombiningAlgorithm getCombiningAlg();
}
