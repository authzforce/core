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
package org.ow2.authzforce.core.pdp.impl.io;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;

/**
 * Supplier of {@link DecisionRequestPreprocessor}s *
 */
public interface DecisionRequestPreprocessorSupplier
{
	/**
	 * Gets a DecisionRequestPreprocessor applying specific request processing according to the features of the PDP it processes requests for
	 * 
	 * @param extraPdpFeatures
	 *            extra - not mandatory per XACML 3.0 core specification - features supported by the PDP. If a decision request requests any such non-mandatory feature (e.g. CombinedDecision=true in
	 *            XACML), the request preprocessor should use this argument to check whether it is supported by the PDP before processing the request further. See
	 *            {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor.Features} for example.
	 * @return request preprocessor
	 */
	DecisionRequestPreprocessor<?, ?> get(Set<String> extraPdpFeatures);
}
