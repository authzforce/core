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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.PdpExtension;

import com.google.common.base.Preconditions;

/**
 * Generic immutable registry of PDP extensions of a given type
 * 
 * @param <T>
 *            extension type
 */
public final class ImmutablePdpExtensionRegistry<T extends PdpExtension> extends BasePdpExtensionRegistry<T>
{

	/**
	 * Creates immutable registry
	 * 
	 * @param extensionClass
	 *            extension class
	 * @param extensions
	 *            extensions
	 */
	public ImmutablePdpExtensionRegistry(Class<? super T> extensionClass, Set<T> extensions)
	{
		super(Preconditions.checkNotNull(extensionClass, "Undefined input extension class (extensionClass == null)"),
				Preconditions.checkNotNull(extensions, "No input extension (extensions == null)"));
	}

}
