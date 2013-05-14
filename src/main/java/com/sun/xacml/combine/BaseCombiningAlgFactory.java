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
package com.sun.xacml.combine;

import com.sun.xacml.UnknownIdentifierException;

import java.net.URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * This is a basic implementation of <code>CombiningAlgFactory</code>. It
 * implements the insertion and retrieval methods, but doesn't actually
 * setup the factory with any algorithms.
 * <p>
 * Note that while this class is thread-safe on all creation methods, it
 * is not safe to add support for a new algorithm while creating an instance
 * of an algorithm. This follows from the assumption that most people will
 * initialize these factories up-front, and then start processing without
 * ever modifying the factories. If you need these mutual operations to
 * be thread-safe, then you should write a wrapper class that implements
 * the right synchronization.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public class BaseCombiningAlgFactory extends CombiningAlgFactory
{

    // the map of available combining algorithms
    private HashMap algMap;

    /**
     * Default constructor.
     */
    public BaseCombiningAlgFactory() {
        algMap = new HashMap();
    }

    /**
     * Constructor that configures this factory with an initial set of
     * supported algorithms.
     *
     * @param algorithms a <code>Set</code> of
     *                   </code>CombiningAlgorithm</code>s
     *
     * @throws IllegalArgumentException if any elements of the set are not
     *                                  </code>CombiningAlgorithm</code>s
     */
    public BaseCombiningAlgFactory(Set algorithms) {
        algMap = new HashMap();

        Iterator it = algorithms.iterator();
        while (it.hasNext()) {
            try {
                CombiningAlgorithm alg = (CombiningAlgorithm)(it.next());
                algMap.put(alg.getIdentifier().toString(), alg);
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("an element of the set " +
                                                   "was not an instance of " +
                                                   "CombiningAlgorithm");
            }
        }
    }

    /**
     * Adds a combining algorithm to the factory. This single instance will
     * be returned to anyone who asks the factory for an algorithm with the
     * id given here.
     *
     * @param alg the combining algorithm to add
     *
     * @throws IllegalArgumentException if the algId is already registered
     */
    public void addAlgorithm(CombiningAlgorithm alg) {
        String algId = alg.getIdentifier().toString();

        // check that the id doesn't already exist in the factory
        if (algMap.containsKey(algId))
            throw new IllegalArgumentException("algorithm already registered: "
                                               + algId);

        // add the algorithm
        algMap.put(algId, alg);
    }

    /**
     * Returns the algorithm identifiers supported by this factory.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public Set getSupportedAlgorithms() {
        return Collections.unmodifiableSet(algMap.keySet());
    }

    /**
     * Tries to return the correct combinging algorithm based on the
     * given algorithm ID.
     *
     * @param algId the identifier by which the algorithm is known
     *
     * @return a combining algorithm
     *
     * @throws UnknownIdentifierException algId is unknown
     */
    public CombiningAlgorithm createAlgorithm(URI algId)
        throws UnknownIdentifierException
    {
        String id = algId.toString();

        if (algMap.containsKey(id)) {
            return (CombiningAlgorithm)(algMap.get(algId.toString()));
        } else {
            throw new UnknownIdentifierException("unknown combining algId: "
                                                 + id);
        }
    }

}
