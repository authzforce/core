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


/**
 * A simple proxy interface used to install new <code>FunctionFactory</code>s.
 * The three kinds of factory (Target, Condition, and General) are tied
 * together in this interface because implementors writing new factories
 * should always implement all three types and provide them together.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public interface FunctionFactoryProxy
{

    /**
     * Returns the Target version of an instance of the
     * <code>FunctionFactory</code> for which this is a proxy.
     *
     * @return a <code>FunctionFactory</code> instance
     */
    public FunctionFactory getTargetFactory();

    /**
     * Returns the Condition version of an instance of the
     * <code>FunctionFactory</code> for which this is a proxy.
     *
     * @return a <code>FunctionFactory</code> instance
     */
    public FunctionFactory getConditionFactory();

    /**
     * Returns the General version of an instance of the
     * <code>FunctionFactory</code> for which this is a proxy.
     *
     * @return a <code>FunctionFactory</code> instance
     */
    public FunctionFactory getGeneralFactory();

}
