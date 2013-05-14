/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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
package com.sun.xacml.finder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This is used to return Resource Ids from the ResourceFinder. Unlike the
 * PolicyFinder, this never returns an empty set, since it will always
 * contain at least the original parent resource. This class will provide
 * two sets of identifiers: those that were successfully resolved and those
 * that had an error.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class ResourceFinderResult
{

    // the set of resource identifiers
    private Set resources;

    // the map of failed identifiers to their failure status data
    private Map failures;

    // a flag specifying whether or not result contains resource listings
    private boolean empty;

    /**
     * Creates an empty result.
     */
    public ResourceFinderResult() {
        resources = Collections.unmodifiableSet(new HashSet());
        failures = Collections.unmodifiableMap(new HashMap());
        empty = true;
    }

    /**
     * Creates a result containing the given <code>Set</code> of resource
     * identifiers. The <code>Set</code>must not be null. The new
     * <code>ResourceFinderResult</code> represents a resource retrieval that
     * encountered no errors.
     *
     * @param resources a non-null <code>Set</code> of
     *                  <code>AttributeValue</code>s
     */
    public ResourceFinderResult(Set resources) {
        this(resources, new HashMap());
    }

    /**
     * Creates a result containing only Resource Ids that caused errors. The
     * <code>Map</code> must not be null. The keys in the <code>Map</code>
     * are <code>AttributeValue</code>s identifying the resources that could
     * not be resolved, and they map to a <code>Status</code> object 
     * explaining the error. The new <code>ResourceFinderResult</code>
     * represents a resource retrieval that did not succeed in finding any
     * resource identifiers.
     *
     * @param failures a non-null <code>Map</code> mapping failed
     *                 <code>AttributeValue</code> identifiers to their
     *                 <code>Status</code>
     */
    public ResourceFinderResult(HashMap failures) {
        this(new HashSet(), failures);
    }

    /**
     * Creates a new result containing both successfully resolved Resource Ids
     * and resources that caused errors.
     *
     * @param resources a non-null <code>Set</code> of
     *                  <code>AttributeValue</code>s
     * @param failures a non-null <code>Map</code> mapping failed
     *                 <code>AttributeValue</code> identifiers to their
     *                 <code>Status</code>
     */
    public ResourceFinderResult(Set resources, Map failures) {
        this.resources = Collections.unmodifiableSet(new HashSet(resources));
        this.failures = Collections.unmodifiableMap(new HashMap(failures));
        empty = false;
    }

    /**
     * Returns whether or not this result contains any Resource Id listings.
     * This will return false if either the set of successfully resolved
     * resource identifiers or the map of failed resources is not empty.
     *
     * @return false if this result names any resources, otherwise true
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Returns the <code>Set</code> of successfully resolved Resource Id
     * <code>AttributeValue</code>s, which will be empty if no resources
     * were successfully resolved.
     *
     * @return a <code>Set</code> of <code>AttributeValue</code>s
     */
    public Set getResources() {
        return resources;
    }

    /**
     * Returns the <code>Map</code> of Resource Ids that caused an error on
     * resolution, which will be empty if no resources caused any error.
     *
     * @return a <code>Map</code> of <code>AttributeValue</code>s to
     *         <code>Status</code>
     */
    public Map getFailures() {
        return failures;
    }

}
