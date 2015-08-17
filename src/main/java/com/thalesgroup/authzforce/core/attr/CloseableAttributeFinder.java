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