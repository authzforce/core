/**
 * Copyright (C) 2011-2014 Thales Services SAS.
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
 * Interface for all types of PDP extensions (AttributeFinder, PolicyFinder...) (Marker interface
 * pattern.)
 * 
 * @param <T>
 *            datamodel class representing the parameters/configuration of the extension
 * 
 * 
 */
public interface IPdpExtension<T extends AbstractPdpExtension>
{
	/**
	 * Index of the module initialization method in this interface, i.e. in
	 * {@code IPdpExtension.class.getMethods() } The PDP engine will use this index to get
	 * information about the init method below, more precisely the conf parameter type to know the
	 * configuration type supported by the extension, and based on this, load the right extension
	 * implementation for a given configuration item.
	 * 
	 * Note: only one method so far, so this is fairly obvious, but in the future, who knows?
	 */
	public static int INIT_METHOD_INDEX = 0;

	/**
	 * Extension initialization method. The PDP engine will select the right extension implementation for a given
	 * configuration type based on this first parameter type.
	 * 
	 * @param conf
	 *            main configuration
	 */
	public void init(T conf);
}
