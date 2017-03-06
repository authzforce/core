/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Evaluator returning a boolean result
 */
public interface BooleanEvaluator
{

	/**
	 * Evaluates the condition
	 *
	 * @param context
	 *            the representation of the request
	 * @return true if and only if the condition holds
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if error evaluating the condition
	 */
	boolean evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

}
