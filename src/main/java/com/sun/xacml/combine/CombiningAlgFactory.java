/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
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

import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;

import java.net.URI;

import java.util.HashMap;
import java.util.Set;


/**
 * Provides a factory mechanism for installing and retrieving combining
 * algorithms.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class CombiningAlgFactory
{

    // the proxy used to get the default factory
    private static CombiningAlgFactoryProxy defaultFactoryProxy;

    // the map of registered factories
    private static HashMap registeredFactories;

    /**
     * static intialiazer that sets up the default factory proxy and
     * registers the standard namespaces
     */
    static {
        CombiningAlgFactoryProxy proxy = new CombiningAlgFactoryProxy() {
                public CombiningAlgFactory getFactory() {
                    return StandardCombiningAlgFactory.getFactory();
                }
            };

        registeredFactories = new HashMap();
        registeredFactories.put(PolicyMetaData.XACML_1_0_IDENTIFIER, proxy);
        registeredFactories.put(PolicyMetaData.XACML_2_0_IDENTIFIER, proxy);

        defaultFactoryProxy = proxy;
    };

    /**
     * Default constructor. Used only by subclasses.
     */
    protected CombiningAlgFactory() {
        
    }

    /**
     * Returns the default factory. Depending on the default factory's
     * implementation, this may return a singleton instance or new instances
     * with each invokation.
     *
     * @return the default <code>CombiningAlgFactory</code>
     */
    public static final CombiningAlgFactory getInstance() {
        return defaultFactoryProxy.getFactory();
    }

    /**
     * Returns a factory based on the given identifier. You may register
     * as many factories as you like, and then retrieve them through this
     * interface, but a factory may only be registered once using a given
     * identifier. By default, the standard XACML 1.0 and 2.0 identifiers
     * are regsietered to provide the standard factory.
     *
     * @param identifier the identifier for a factory
     *
     * @return a <code>CombiningAlgFactory</code>
     *
     * @throws UnknownIdentifierException if the given identifier isn't
     *                                    registered
     */
    public static final CombiningAlgFactory getInstance(String identifier)
        throws UnknownIdentifierException
    {
        CombiningAlgFactoryProxy proxy =
            (CombiningAlgFactoryProxy)(registeredFactories.get(identifier));

        if (proxy == null)
            throw new UnknownIdentifierException("Uknown CombiningAlgFactory "
                                                 + "identifier: " +
                                                 identifier);

        return proxy.getFactory();
    }

    /**
     * Sets the default factory. This does not register the factory proxy as
     * an identifiable factory.
     *
     * @param proxy the <code>CombiningAlgFactoryProxy</code> to set as the
     *              new default factory proxy
     */
    public static final void setDefaultFactory(CombiningAlgFactoryProxy proxy)
    {
        defaultFactoryProxy = proxy;
    }

    /**
     * Registers the given factory proxy with the given identifier. If the
     * identifier is already used, then this throws an exception. If the
     * identifier is not already used, then it will always be bound to the
     * given proxy.
     *
     * @param identifier the identifier for the proxy
     * @param proxy the <code>CombiningAlgFactoryProxy</code> to register with
     *              the given identifier
     *
     * @throws IllegalArgumentException if the identifier is already used
     */
    public static final void registerFactory(String identifier,
                                             CombiningAlgFactoryProxy proxy)
        throws IllegalArgumentException
    {
        synchronized (registeredFactories) {
            if (registeredFactories.containsKey(identifier))
                throw new IllegalArgumentException("Identifier is already " +
                                                   "registered as " +
                                                   "CombiningAlgFactory: " +
                                                   identifier);
            
            registeredFactories.put(identifier, proxy);
        }
    }

    /**
     * Adds a combining algorithm to the factory. This single instance will
     * be returned to anyone who asks the factory for an algorithm with the
     * id given here.
     *
     * @param alg the combining algorithm to add
     *
     * @throws IllegalArgumentException if the algorithm is already registered
     */
    public abstract void addAlgorithm(CombiningAlgorithm alg);

    /**
     * Adds a combining algorithm to the factory. This single instance will
     * be returned to anyone who asks the factory for an algorithm with the
     * id given here.
     *
     * @deprecated As of version 1.2, replaced by
     *        {@link #addAlgorithm(CombiningAlgorithm)}.
     *             The new factory system requires you to get a factory
     *             instance and then call the non-static methods on that
     *             factory. The static versions of these methods have been
     *             left in for now, but are slower and will be removed in
     *             a future version.
     *
     * @param alg the combining algorithm to add
     *
     * @throws IllegalArgumentException if the algorithm is already registered
     */
    public static void addCombiningAlg(CombiningAlgorithm alg) {
        getInstance().addAlgorithm(alg);
    }

    /**
     * Returns the algorithm identifiers supported by this factory.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public abstract Set getSupportedAlgorithms();

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
    public abstract CombiningAlgorithm createAlgorithm(URI algId)
        throws UnknownIdentifierException;

    /**
     * Tries to return the correct combinging algorithm based on the
     * given algorithm ID.
     *
     * @deprecated As of version 1.2, replaced by
     *        {@link #createAlgorithm(URI)}.
     *             The new factory system requires you to get a factory
     *             instance and then call the non-static methods on that
     *             factory. The static versions of these methods have been
     *             left in for now, but are slower and will be removed in
     *             a future version.
     *
     * @param algId the identifier by which the algorithm is known
     *
     * @return a combining algorithm
     *
     * @throws UnknownIdentifierException algId is unknown
     */
    public static CombiningAlgorithm createCombiningAlg(URI algId)
        throws UnknownIdentifierException
    {
        return getInstance().createAlgorithm(algId);
    }


}
