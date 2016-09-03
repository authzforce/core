/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
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
