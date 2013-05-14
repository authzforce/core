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
package com.sun.xacml.cond;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;


/**
 * This is a basic implementation of <code>FunctionFactory</code>. It
 * implements the insertion and retrieval methods, but it doesn't actually
 * setup the factory with any functions. It also assumes a certain model
 * with regard to the different kinds of functions (Target, Condition, and
 * General). For this reason, you may want to re-use this class, or you 
 * may want to extend FunctionFactory directly, if you're writing a new
 * factory implementation.
 * <p>
 * Note that while this class is thread-safe on all creation methods, it
 * is not safe to add support for a new function while creating an instance
 * of a function. This follows from the assumption that most people will
 * initialize these factories up-front, and then start processing without
 * ever modifying the factories. If you need these mutual operations to
 * be thread-safe, then you should write a wrapper class that implements
 * the right synchronization.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public class BaseFunctionFactory extends FunctionFactory
{

    // the backing maps for the Function objects
    private HashMap functionMap = null;

    // the superset factory chained to this factory
    private FunctionFactory superset = null;
    
    /**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(BaseFunctionFactory.class);

    /**
     * Default constructor. No superset factory is used.
     */
    public BaseFunctionFactory() {
        this(null);
    }

    /**
     * Constructor that sets a "superset factory". This is useful since
     * the different function factories (Target, Condition, and General)
     * have a superset relationship (Condition functions are a superset
     * of Target functions, etc.). Adding a function to this factory will
     * automatically add the same function to the superset factory.
     *
     * @param superset the superset factory or null
     */
    public BaseFunctionFactory(FunctionFactory superset) {
        functionMap = new HashMap();

        this.superset = superset;
    }

    /**
     * Constructor that defines the initial functions supported by this
     * factory but doesn't use a superset factory.
     *
     * @param supportedFunctions a <code>Set</code> of <code>Function</code>s
     * @param supportedAbstractFunctions a mapping from <code>URI</code> to
     *                                   <code>FunctionProxy</code>
     */
    public BaseFunctionFactory(Set supportedFunctions,
                               Map supportedAbstractFunctions) {
        this(null, supportedFunctions, supportedAbstractFunctions);
    }

    /**
     * Constructor that defines the initial functions supported by this
     * factory and uses a superset factory. Note that the functions
     * supplied here are not propagated up to the superset factory, so
     * you must either make sure the superst factory is correctly
     * initialized or use <code>BaseFunctionFactory(FunctionFactory)</code>
     * and then manually add each function.
     *
     * @param superset the superset factory or null
     * @param supportedFunctions a <code>Set</code> of <code>Function</code>s
     * @param supportedAbstractFunctions a mapping from <code>URI</code> to
     *                                   <code>FunctionProxy</code>
     */
    public BaseFunctionFactory(FunctionFactory superset,
                               Set supportedFunctions,
                               Map supportedAbstractFunctions) {
        this(superset);

        Iterator it = supportedFunctions.iterator();
        while (it.hasNext()) {
            Function function = (Function)(it.next());
            LOGGER.debug("Adding supported function to the functionMap: "+function.getIdentifier());
            functionMap.put(function.getIdentifier().toString(), function);
        }

        it = supportedAbstractFunctions.keySet().iterator();
        while (it.hasNext()) {
            URI id = (URI)(it.next());
            FunctionProxy proxy =
                (FunctionProxy)(supportedAbstractFunctions.get(id));
            LOGGER.debug("Adding abstract function to the functionMap: "+id.toString());
            functionMap.put(id.toString(), proxy);
        }
    }

    /**
     * Adds the function to the factory. Most functions have no state, so
     * the singleton model used here is typically desireable. The factory will
     * not enforce the requirement that a Target or Condition matching function
     * must be boolean.
     *
     * @param function the <code>Function</code> to add to the factory
     *
     * @throws IllegalArgumentException if the function's identifier is already
     *                                  used or if the function is non-boolean
     *                                  (when this is a Target or Condition
     *                                  factory)
     */
    public void addFunction(Function function)
        throws IllegalArgumentException
    {
        String id = function.getIdentifier().toString();

        // make sure this doesn't already exist
        if (functionMap.containsKey(id)) {
            throw new IllegalArgumentException("function already exists");
        }

        // add to the superset factory
        if (superset != null) {
            superset.addFunction(function);
        }

        // finally, add to this factory
        functionMap.put(id, function);
    }

    /**
     * Adds the abstract function proxy to the factory. This is used for
     * those functions which have state, or change behavior (for instance
     * the standard map function, which changes its return type based on
     * how it is used). 
     *
     * @param proxy the <code>FunctionProxy</code> to add to the factory
     * @param identity the function's identifier
     *
     * @throws IllegalArgumentException if the function's identifier is already
     *                                  used
     */
    public void addAbstractFunction(FunctionProxy proxy,
                                    URI identity)
        throws IllegalArgumentException
    {
        String id = identity.toString();

        // make sure this doesn't already exist
        if (functionMap.containsKey(id)) {
            throw new IllegalArgumentException("function already exists");
        }

        // add to the superset factory
        if (superset != null) {
            superset.addAbstractFunction(proxy, identity);
        }

        // finally, add to this factory
        functionMap.put(id, proxy);
    }

    /**
     * Returns the function identifiers supported by this factory.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public Set getSupportedFunctions() {
        Set set = new HashSet(functionMap.keySet());

        if (superset != null)
            set.addAll(superset.getSupportedFunctions());

        return set;
    }

    /**
     * Tries to get an instance of the specified function.
     *
     * @param identity the name of the function
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to an
     *                               abstract function, and should therefore
     *                               be created through createAbstractFunction
     */
    public Function createFunction(URI identity)
        throws UnknownIdentifierException, FunctionTypeException
    {
        return createFunction(identity.toString());
    }

    /**
     * Tries to get an instance of the specified function.
     *
     * @param identity the name of the function
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to an
     *                               abstract function, and should therefore
     *                               be created through createAbstractFunction
     */
    public Function createFunction(String identity)
        throws UnknownIdentifierException, FunctionTypeException
    {
        Object entry = functionMap.get(identity);

        if (entry != null) {
            if (entry instanceof Function) {
            	((Function) entry).setFunctionId(((Function) entry).getIdentifier().toASCIIString());
                return (Function)entry;
            } else {
                // this is actually a proxy, which means the other create
                // method should have been called
                throw new FunctionTypeException("function is abstract");
            }
        } else {
            // we couldn't find a match
            throw new UnknownIdentifierException("functions of type " +
                                                 identity + " are not "+
                                                 "supported by this factory");
        }
    }

    /**
     * Tries to get an instance of the specified abstract function.
     *
     * @param identity the name of the function
     * @param root the DOM root containing info used to create the function
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to a
     *                               concrete function, and should therefore
     *                               be created through createFunction
     * @throws ParsingException if the function can't be created with the
     *                          given inputs
     */
    public Function createAbstractFunction(URI identity, Node root)
        throws UnknownIdentifierException, ParsingException,
               FunctionTypeException
    {
        return createAbstractFunction(identity.toString(), root, null);
    }

    /**
     * Tries to get an instance of the specified abstract function.
     *
     * @param identity the name of the function
     * @param root the DOM root containing info used to create the function
     * @param xpathVersion the version specified in the contianing policy, or
     *                     null if no version was specified
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to a
     *                               concrete function, and should therefore
     *                               be created through createFunction
     * @throws ParsingException if the function can't be created with the
     *                          given inputs
     */
    public Function createAbstractFunction(URI identity, Node root,
                                           String xpathVersion)
        throws UnknownIdentifierException, ParsingException,
               FunctionTypeException
    {
        return createAbstractFunction(identity.toString(), root, xpathVersion);
    }

    /**
     * Tries to get an instance of the specified abstract function.
     *
     * @param identity the name of the function
     * @param root the DOM root containing info used to create the function
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to a
     *                               concrete function, and should therefore
     *                               be created through createFunction
     * @throws ParsingException if the function can't be created with the
     *                          given inputs
     */
    public Function createAbstractFunction(String identity, Node root)
        throws UnknownIdentifierException, ParsingException,
               FunctionTypeException
    {
        return createAbstractFunction(identity, root, null);
    }

    /**
     * Tries to get an instance of the specified abstract function.
     *
     * @param identity the name of the function
     * @param root the DOM root containing info used to create the function
     * @param xpathVersion the version specified in the contianing policy, or
     *                     null if no version was specified
     *
     * @throws UnknownIdentifierException if the name isn't known
     * @throws FunctionTypeException if the name is known to map to a
     *                               concrete function, and should therefore
     *                               be created through createFunction
     * @throws ParsingException if the function can't be created with the
     *                          given inputs
     */
    public Function createAbstractFunction(String identity, Node root,
                                           String xpathVersion)
        throws UnknownIdentifierException, ParsingException,
               FunctionTypeException
    {
        Object entry = functionMap.get(identity);
        
        if (entry != null) {
            if (entry instanceof FunctionProxy) {
                try {
                    return ((FunctionProxy)entry).getInstance(root,
                                                              xpathVersion);
                } catch (Exception e) {
                    throw new ParsingException("couldn't create abstract" +
                                               " function " + identity, e);
                }
            } else {
                // this is actually a concrete function, which means that
                // the other create method should have been called
                throw new FunctionTypeException("function is concrete");
            }
        } else {
            // we couldn't find a match
            throw new UnknownIdentifierException("abstract functions of " +
                                                 "type " + identity +
                                                 " are not supported by " +
                                                 "this factory");
        }
    }

}
