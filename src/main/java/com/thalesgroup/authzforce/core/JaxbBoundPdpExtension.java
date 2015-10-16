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
package com.thalesgroup.authzforce.core;

import com.thalesgroup.authz.model.ext._3.AbstractPdpExtension;

/**
 * Marker Interface for PDP extensions bound to a specific XML/JAXB class (used for the
 * configuration of the extension)
 * 
 * @param <T>
 *            XML/JAXB type used as configuration class for the extension. There must be a
 *            one-to-one relationship between such types and the JAXB-bound extensions.
 * 
 */
public abstract class JaxbBoundPdpExtension<T extends AbstractPdpExtension> implements PdpExtension
{
	/**
	 * Gets the XML/JAXB class used as configuration class for the extension. There must be a one-to-one
	 * relationship between such types and the JAXB-bound extensions.
	 * 
	 * @return XML/JAXB class bound to this extension
	 */
	public abstract Class<T> getJaxbClass();
	
	@Override
	public final String getId() {
		return getJaxbClass().getCanonicalName();
	}
}
