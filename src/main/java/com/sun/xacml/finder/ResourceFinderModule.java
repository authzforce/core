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

import com.sun.xacml.EvaluationCtx;

import com.sun.xacml.attr.xacmlv3.AttributeValue;


/**
 * This is the abstract class that all <code>ResourceFinder</code> modules
 * extend. All methods have default values to represent that the given
 * feature isn't supported by this module, so module writers needs only
 * implement the methods for the features they're supporting.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class ResourceFinderModule
{

    /**
     * Returns this module's identifier. A module does not need to provide
     * a unique identifier, but it is a good idea, especially in support of
     * management software. Common identifiers would be the full package
     * and class name (the default if this method isn't overridden), just the
     * class name, or some other well-known string that identifies this class.
     *
     * @return this module's identifier
     */
    public String getIdentifier() {
        return getClass().getName();
    }

    /**
     * Returns true if this module supports finding resources with the
     * "Children" scope. By default this method returns false.
     *
     * @return true if the module supports the Children scope
     */
    public boolean isChildSupported() {
        return false;
    }

    /**
     * Returns true if this module supports finding resources with the
     * "Descendants" scope. By default this method returns false.
     *
     * @return true if the module supports the Descendants scope
     */
    public boolean isDescendantSupported() {
        return false;
    }

    /**
     * This is an experimental method that asks the module to invalidate any
     * cacheManager values it may contain. This is not used by any of the core
     * processing code, but it may be used by management software that wants
     * to have some control over these modules. Since a module is free to
     * decide how or if it caches values, and whether it is capable of
     * updating values once in a cacheManager, a module is free to intrepret this
     * message in any way it sees fit (including igoring the message). It
     * is preferable, however, for a module to make every effort to clear
     * any dynamically cached values it contains.
     * <p>
     * This method has been introduced to see what people think of this
     * functionality, and how they would like to use it. It may be removed
     * in future versions, or it may be changed to a more general
     * message-passing system (if other useful messages are identified).
     * 
     * @since 1.2
     */
    public void invalidateCache() {

    }

    /**
     * Tries to find the child Resource Ids associated with the parent. If
     * this module cannot handle the given identifier, then an empty result is
     * returned, otherwise the result will always contain at least the
     * parent Resource Id, either as a successfully resolved Resource Id or an
     * error case, but never both.
     *
     * @param parentResourceId the parent resource identifier
     * @param context the representation of the request data
     *
     * @return the result of finding child resources
     */
    public ResourceFinderResult findChildResources(AttributeValue
                                                   parentResourceId,
                                                   EvaluationCtx context) {
        return new ResourceFinderResult();
    }

    /**
     * Tries to find the child Resource Ids associated with the parent. If
     * this module cannot handle the given identifier, then an empty result is
     * returned, otherwise the result will always contain at least the
     * parent Resource Id, either as a successfully resolved Resource Id or an
     * error case, but never both.
     *
     * @deprecated As of version 1.2, replaced by
     *             {@link #findChildResources(AttributeValue,EvaluationCtx)}.
     *             This version does not provide the evaluation context,
     *             and will be removed in a future release. Also, not that
     *             this will never get called when using the default PDP.
     *
     * @param parentResourceId the parent resource identifier
     *
     * @return the result of finding child resources
     */
    public ResourceFinderResult findChildResources(AttributeValue
                                                   parentResourceId) {
        return new ResourceFinderResult();
    }

    /**
     * Tries to find the descendant Resource Ids associated with the parent. If
     * this module cannot handle the given identifier, then an empty result is
     * returned, otherwise the result will always contain at least the
     * parent Resource Id, either as a successfuly resolved Resource Id or an
     * error case, but never both.
     *
     * @param parentResourceId the parent resource identifier
     * @param context the representation of the request data
     *
     * @return the result of finding descendant resources
     */
    public ResourceFinderResult findDescendantResources(AttributeValue
                                                        parentResourceId,
                                                        EvaluationCtx
                                                        context) {
        return new ResourceFinderResult();
    }

    /**
     * Tries to find the descendant Resource Ids associated with the parent. If
     * this module cannot handle the given identifier, then an empty result is
     * returned, otherwise the result will always contain at least the
     * parent Resource Id, either as a successfuly resolved Resource Id or an
     * error case, but never both.
     *
     * @deprecated As of version 1.2, replaced by
     *          {@link #findDescendantResources(AttributeValue,EvaluationCtx)}.
     *             This version does not provide the evaluation context,
     *             and will be removed in a future release. Also, not that
     *             this will never get called when using the default PDP.
     *
     * @param parentResourceId the parent resource identifier
     *
     * @return the result of finding descendant resources
     */
    public ResourceFinderResult findDescendantResources(AttributeValue
                                                        parentResourceId) {
        return new ResourceFinderResult();
    }

}
