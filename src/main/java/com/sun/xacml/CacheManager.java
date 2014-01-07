/**
 * Copyright (C) 2011-2014 Thales Services - ThereSIS - All rights reserved.
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
package com.sun.xacml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheManager {

	private static net.sf.ehcache.CacheManager cacheManager;
	private static CacheManager INSTANCE;
	private static Object SYNCHRONE = new Object();

	private boolean activate = false;
	private int maxElementsInMemory = 100;
	private boolean overflowToDisk = false;
	private boolean eternal = false;
	private int timeToLiveInSeconds = 10;
	private int timeToIdleInSeconds = 5;
	private Cache cache;
	private Cache memoryOnlyCache;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CacheManager.class); 

	public static CacheManager getInstance() {
		if (null == INSTANCE) {
			synchronized (SYNCHRONE) {
				if (null == INSTANCE) {
					INSTANCE = new CacheManager();
				}
			}
		}
		return INSTANCE;
	}
	
	public CacheManager() {
		activate = false;
	}
	public CacheManager(List args) {
		
		for (Map<String, String> myList : (ArrayList<Map<String, String>>) args) {
			/*
			 * Cache configuration
			 * 
			 * @author romain.ferrari[AT]thalesgroup.com
			 */
			if (myList.containsKey("activate")) {
				activate = Boolean
						.valueOf((Boolean.parseBoolean((String) myList
								.get("activate"))));
			}
			else if (myList.containsKey("maxElementsInMemory")) {
				maxElementsInMemory = (int) Integer
						.parseInt((String) myList
								.get("maxElementsInMemory"));
			}
			else if (myList.containsKey("overflowToDisk")) {
				overflowToDisk = Boolean.valueOf((Boolean
						.parseBoolean((String) myList
								.get("overflowToDisk"))));
			}
			else if (myList.containsKey("eternal")) {
				eternal = Boolean.valueOf((Boolean
						.parseBoolean((String) myList.get("eternal"))));
			}
			else if (myList.containsKey("timeToLiveSeconds")) {
				timeToLiveInSeconds = (int) Integer
						.parseInt((String) myList
								.get("timeToLiveSeconds"));
			}
			else if (myList.containsKey("timeToIdleSeconds")) {
				timeToIdleInSeconds = (int) Integer
						.parseInt((String) myList
								.get("timeToIdleSeconds"));
			}
		}
		/*
		 * Cache stuff
		 * 
		 * @author romain.ferrari[AT]thalesgroup.com
		 */
		LOGGER.debug("Cache activated: {}", activate);
		LOGGER.debug("maxElementsInMemory = {}", maxElementsInMemory);
		LOGGER.debug("overflowToDisk: {}", overflowToDisk);
		LOGGER.debug("eternal: {}", eternal);
		LOGGER.debug("timeToLiveSeconds = {}", timeToLiveInSeconds);
		LOGGER.debug("timeToIdleSeconds = {}", timeToIdleInSeconds);
		
//		cacheManager = net.sf.ehcache.CacheManager.getInstance();
		cacheManager = new net.sf.ehcache.CacheManager();
		
		initCache();
	}
	public CacheManager(Map<String, String> args) {
		
		if (args instanceof HashMap) {
			Iterator it = args.keySet().iterator();
			String val = null;
			while (it.hasNext()) {
				val = (String)it.next();
				/*
				 * Cache configuration
				 * 
				 * @author romain.ferrari[AT]thalesgroup.com
				 */
				if (("activate").equalsIgnoreCase(val)) {
					activate = Boolean.valueOf((Boolean
							.parseBoolean((String) args.get("activate"))));
				} else if (("maxElementsInMemory").equalsIgnoreCase(val)) {
					maxElementsInMemory = (int) Integer.parseInt((String) args
							.get("maxElementsInMemory"));
				} else if (("overflowToDisk").equalsIgnoreCase(val)) {
					overflowToDisk = Boolean.valueOf((Boolean
							.parseBoolean((String) args.get("overflowToDisk"))));
				} else if (("eternal").equalsIgnoreCase(val)) {
					eternal = Boolean.valueOf((Boolean
							.parseBoolean((String) args.get("eternal"))));
				} else if (("timeToLiveSeconds").equalsIgnoreCase(val)) {
					timeToLiveInSeconds = (int) Integer.parseInt((String) args
							.get("timeToLiveSeconds"));
				} else if (("timeToIdleSeconds").equalsIgnoreCase(val)) {
					timeToIdleInSeconds = (int) Integer.parseInt((String) args
							.get("timeToIdleSeconds"));
				}
			}
		}

		/*
		 * Cache stuff
		 * 
		 * @author romain.ferrari[AT]thalesgroup.com
		 */
		LOGGER.debug("Cache activated: {}", activate);
		LOGGER.debug("maxElementsInMemory = {}", maxElementsInMemory);
		LOGGER.debug("overflowToDisk: {}", overflowToDisk);
		LOGGER.debug("eternal: {}", eternal);
		LOGGER.debug("timeToLiveSeconds = {}", timeToLiveInSeconds);
		LOGGER.debug("timeToIdleSeconds = {}", timeToIdleInSeconds);
		
		initCache();
	}
	
	/**
	 * Cache initialization
	 * 
	 * @author romain.ferrari[AT]thalesgroup.com	
     * @param name                the name of the cache. Note that "default" is a reserved name for the defaultCache.
     * @param maxElementsInMemory the maximum number of elements in memory, before they are evicted
     * @param overflowToDisk      whether to use the disk store
     * @param eternal             whether the elements in the cache are eternal, i.e. never expire
     * @param timeToLiveSeconds   the default amount of time to live for an element from its creation date
     * @param timeToIdleSeconds   the default amount of time to live for an element from its last accessed or modified date
     */
	private void initCache() {
	      //NOTE: Create a Cache and add it to the static CacheManager, then use it.	      
			memoryOnlyCache = new Cache("AuthZForce", maxElementsInMemory, overflowToDisk, eternal, timeToLiveInSeconds, timeToIdleInSeconds);
	        cacheManager.addCache(memoryOnlyCache);
	        cache = cacheManager.getCache("AuthZForce");
		}
	
	/**
	 * Used to clear the cache.
	 * 
	 * @return true if ok, false if a problem occured
	 */
	public boolean invalidateCache() {
		if (cache != null && cache.getSize() > 0) {
			LOGGER.info("Invalidating cache");
			this.cache.removeAll();

			return true;
		}

		return false;
	}

	/**
	 * Searching the cache for the <code>hash</code> as key. A Object is returned if the
	 * matching Request is found
	 * 
	 * @param hash symbolising the object as a unique object
	 *  
	 * @return Object matching the hash
	 */
	public Object checkCache(String hash) {
		Element myElt;
		LOGGER.debug("checkCache with {}", hash);
		myElt = cache.get(hash);
		if (myElt != null) {
			return myElt.getObjectValue();
		}

		return null;
	}

	/**
	 * Updating the cache content.
	 * 	 
	 * @param hash
	 * @param storedObject
	 */
	public void updateCache(String hash, Object storedObject) {
		LOGGER.debug("Updating cache with: {}", hash);
		cache.put(new Element(hash, storedObject));
	}

	public static net.sf.ehcache.CacheManager getCacheManager() {
		return cacheManager;
	}

	public static void setCacheManager(net.sf.ehcache.CacheManager cacheManager) {
		CacheManager.cacheManager = cacheManager;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	public void setMaxElementsInMemory(int maxElementsInMemory) {
		this.maxElementsInMemory = maxElementsInMemory;
	}

	public boolean isOverflowToDisk() {
		return overflowToDisk;
	}

	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}

	public boolean isEternal() {
		return eternal;
	}

	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}

	public int getTimeToLiveInSeconds() {
		return timeToLiveInSeconds;
	}

	public void setTimeToLiveInSeconds(int timeToLiveInSeconds) {
		this.timeToLiveInSeconds = timeToLiveInSeconds;
	}

	public int getTimeToIdleInSeconds() {
		return timeToIdleInSeconds;
	}

	public void setTimeToIdleInSeconds(int timeToIdleInSeconds) {
		this.timeToIdleInSeconds = timeToIdleInSeconds;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Cache getMemoryOnlyCache() {
		return memoryOnlyCache;
	}

	public void setMemoryOnlyCache(Cache memoryOnlyCache) {
		this.memoryOnlyCache = memoryOnlyCache;
	}
}
