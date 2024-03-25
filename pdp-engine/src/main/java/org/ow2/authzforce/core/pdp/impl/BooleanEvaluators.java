/*
 * Copyright 2012-2024 THALES.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator returning a boolean result
 */
public final class BooleanEvaluators
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BooleanEvaluators.class);
	private BooleanEvaluators() {
		// prevent instantiation
	}

	/**
	 * Evaluator of expression that always evaluates to True
	 */
	public static final BooleanEvaluator TRUE = (context, mdpContext) ->
	{
		LOGGER.debug("Expression evaluating to constant True -> True");
		return true;
	};

	/**
	 * Evaluator of expression that always evaluates to False
	 */
	public static final BooleanEvaluator FALSE = (context, mdpContext) ->
	{
		LOGGER.debug("Expression evaluating to constant False -> False");
		return false;
	};

}
