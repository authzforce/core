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
package com.sun.xacml.support.finder;

import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.VersionConstraints;

import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.logging.Logger;


/**
 * This module supports references made with resolvable URLs (eg, http or
 * file pointers). No policies are cached. Instead, all policy references are
 * resolved in real-time. To make this module as generally applicable as
 * possible, no errors are ever returned when attempting to resolve a
 * policy. This means that if a resolved policy is invalid, a server cannot
 * be contacted, etc., this module simply reports that it cannot provide a
 * policy. If you need to report errors, or support any caching, you have to
 * write your own implementation.
 * <p>
 * This module is provided as an example, but is still fully functional, and
 * should be useful for many simple applications. This is provided in the
 * <code>support</code> package rather than the core codebase because it
 * implements non-standard behavior.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class URLPolicyFinderModule extends PolicyFinderModule
{

    // the optional schema file for validating policies
    private File schemaFile;

    // the reader used to load all policies
    private PolicyReader reader;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(URLPolicyFinderModule.class.getName());

    /**
     * Creates a <code>URLPolicyFinderModule</code>. The schema file used
     * to validate policies is specified by the property
     * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved
     * property is null, then no schema validation will occur.
     */
    public URLPolicyFinderModule() {
        String schemaName =
            System.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);

        if (schemaName != null)
            schemaFile = new File(schemaName);
    }

    /**
     * Creates a <code>URLPolicyFinderModule</code> that may do schema
     * validation of policies.
     *
     * @param schemaFile the schema file to use for validation, or null if
     *                   validation isn't desired
     */
    public URLPolicyFinderModule(String schemaFile) {
        this.schemaFile = new File(schemaFile);
    }

    /**
     * Always returns <code>true</code> since this module does support
     * finding policies based on reference.
     *
     * @return true
     */
    public boolean isIdReferenceSupported() {
        return true;
    }

    /**
     * Initialize this module. Typically this is called by
     * <code>PolicyFinder</code> when a PDP is created.
     *
     * @param finder the <code>PolicyFinder</code> using this module
     */
    public void init(PolicyFinder finder) {
        reader = new PolicyReader(finder, logger, schemaFile);
    }

    /**
     * Attempts to find a policy by reference, based on the provided
     * parameters. Specifically, this module will try to treat the reference
     * as a URL, and resolve that URL directly. If the reference is not
     * a valid URL, cannot be resolved, or does not resolve to an XACML
     * policy, then no matching policy is returned. This method never
     * returns an error.
     *
     * @param idReference an identifier specifying some policy
     * @param type type of reference (policy or policySet) as identified by
     *             the fields in <code>PolicyReference</code>
     * @param constraints any optional constraints on the version of the
     *                    referenced policy (this will never be null, but
     *                    it may impose no constraints, and in fact will
     *                    never impose constraints when used from a pre-2.0
     *                    XACML policy)
     * @param parentMetaData the meta-data from the parent policy, which
     *                       provides XACML version, factories, etc.
     *
     * @return the result of looking for a matching policy
     */
    public PolicyFinderResult findPolicy(URI idReference, int type,
                                         VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {
        // see if the URI is in fact a URL
        URL url = null;
        try {
            url = new URL(idReference.toString());
        } catch (MalformedURLException murle) {
            // it's not a URL, so we can't handle this reference
            return new PolicyFinderResult();
        }

        // try resolving the URL
        Policy policy = null;
        try {
            policy = reader.readPolicy(url);
        } catch (ParsingException pe) {
            // An error loading the policy could be many things (the URL
            // doesn't actually resolve a policy, the server is down, the
            // policy is invalid, etc.). This could be interpreted as an
            // error case, or simply as a case where no applicable policy
            // is available (as is done when we pre-load policies). This
            // module chooses the latter interpretation.
            return new PolicyFinderResult();
        }

        // check that we got the right kind of policy...if we didn't, then
        // we can't handle the reference
        if (type == PolicyReference.POLICY_REFERENCE) {
            if (! (policy instanceof Policy))
                return new PolicyFinderResult();
        } 
//        else {
//            if (! (policy instanceof PolicySet))
//                return new PolicyFinderResult();
//        }

        // finally, check that the constraints match ... note that in a more
        // powerful module, you could actually have used the constraints to
        // construct a more specific URL, passed the constraints to the
        // server, etc., but this example module is staying simple
        if (! constraints.meetsConstraint(policy.getVersion()))
            return new PolicyFinderResult();

        // if we got here, then we successfully resolved a policy that is
        // the correct type, so return it
        return new PolicyFinderResult(policy);
    }

}
