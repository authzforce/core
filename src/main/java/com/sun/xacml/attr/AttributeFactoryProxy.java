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
package com.sun.xacml.attr;


/**
 * A simple proxy interface used to install new
 * <code>AttributeFactory</code>s.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public interface AttributeFactoryProxy
{

    /**
     * Returns an instance of the <code>AttributeFactory</code> for which
     * this is a proxy.
     *
     * @return an <code>AttributeFactory</code> instance
     */
    public AttributeFactory getFactory();

}
