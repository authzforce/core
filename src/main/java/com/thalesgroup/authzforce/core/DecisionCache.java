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

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import com.thalesgroup.authz.model.ext._3.AbstractDecisionCache;

/**
 * Authorization (XACML) decision result cache. Implements {@link Closeable} because a cache may use
 * resources external to the JVM such as a disk or connection to a remote server for persistence,
 * replication, clustering, etc. Therefore, these resources must be released by calling
 * {@link #close()} when it is no longer needed.
 * 
 */
public interface DecisionCache extends Closeable
{
	/**
	 * Factory for creating instance of DecisionCache extension
	 * 
	 * @param <CONF_T>
	 *            type of extension configuration (initialization parameters)
	 */
	public static abstract class Factory<CONF_T extends AbstractDecisionCache> extends JaxbBoundPdpExtension<CONF_T>
	{

		/**
		 * Instantiates decision cache extension
		 * 
		 * @param conf
		 *            extension parameters
		 * @return instance of extension
		 */
		public abstract DecisionCache getInstance(CONF_T conf);
	}

	/**
	 * Whether this cache is disabled. "Disabled" means:
	 * 
	 * <ol>
	 * <li>puts are discarded</li>
	 * <li>gets return null</li>
	 * </ol>
	 * 
	 * @return true if and only if the cache is disabled
	 */
	boolean isDisabled();

	/**
	 * Gets the decision result(s) from the cache for the given decision request(s). The ability to
	 * get multiple cached results at once allows the Cache implementation to optimize the retrieval
	 * by requesting all in the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param requests
	 *            decision request(s)
	 * @return a map where each entry key is a request from {@code requests}, and the value is the
	 *         corresponding result from cache, or null if no such result found in cache. Each
	 *         request in {@code requests} but be a key in the Map returned, and the Map size must
	 *         be equal to {@code requests.size()}.
	 */
	Map<IndividualDecisionRequest, Result> getAll(Collection<IndividualDecisionRequest> requests);

	/**
	 * Puts a XACML decision requests and corresponding results in cache. The ability to put
	 * multiple cache entries at once allows the Cache implementation to optimize the
	 * creation/update by doing them all in the same request, e.g. if the cache is in a remote
	 * storage/server.
	 * 
	 * @param resultsByRequest
	 *            (request, result) pairs as key-value pairs to be cached
	 */
	void putAll(Map<IndividualDecisionRequest, Result> resultsByRequest);

	/**
	 * Closes the cache when no longer needed, to release all resources managed on behalf of the
	 * cache, as explained by JSR 107 (Java Caching API), § 4.1.8.Closing CacheManagers.
	 */
	@Override
	void close();

}
