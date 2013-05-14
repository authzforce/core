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
package com.sun.xacml.cond.cluster;

import java.util.Set;


/**
 * Interface used by classes that support more than one function. It's a
 * common design model to have a single class support more than one XACML
 * function. In those cases, you should provide a proxy that implements
 * <code>FunctionCluster</code> in addition to the <code>Function</code>.
 * This is particularly important for the run-time configuration system,
 * which uses this interface to create "clusters" of functions and therefore
 * can use a smaller configuration file.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public interface FunctionCluster
{

    /**
     * Returns a single instance of each of the functions supported by
     * some class. The <code>Set</code> must contain instances of
     * <code>Function</code>, and it must be both non-null and non-empty.
     * It may contain only a single <code>Function</code>.
     * <p>
     * Note that this is only used to return concrete <code>Function</code>s.
     * It may not be used to report abstract functions.
     *
     * @return the functions supported by this class
     */
    public Set getSupportedFunctions();

}
