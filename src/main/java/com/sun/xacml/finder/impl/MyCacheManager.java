//package com.sun.xacml.finder.impl;
//
//import net.sf.ehcache.CacheManager;
//
///**
// * 
// * @version 0.1
// * @author romain.ferrari[AT]thalesgroup.com
// * 
// * 	0.1	-	First start of a cache manager implementation
// */
//public class MyCacheManager extends CacheManager {
//	
//	
//	static CacheManager cacheManager = null;
//
//	public synchronized static CacheManager getinstance(){
//		if (cacheManager == null) { 
//			cacheManager = new CacheManager();
//		}
//			return cacheManager;		
//	}
//	
//	public CacheManager getCache() {
//		return cacheManager;
//	}
//
//	public void setCache(CacheManager cache) {
//		this.cacheManager = cache;
//	}
//}
