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
package com.thalesgroup.authzforce.core.attr;

import java.io.Closeable;

import com.sun.xacml.finder.AttributeFinder;

/**
 * {@link Closeable} AttributeFinder
 * <p>
 * {@link Closeable} because this kind of AttributeFinder may hold resources such as network
 * resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore,
 * you are required to call {@link #close()} when you no longer need an instance - especially before
 * replacing with a new instance (with different modules) - in order to make sure these resources
 * are released properly by each underlying module (e.g. close the attribute caches).
 * 
 */
public interface CloseableAttributeFinder extends AttributeFinder, Closeable
{

}