/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.policy;

import java.io.Closeable;
import java.util.Deque;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;
import com.thalesgroup.authzforce.core.JaxbBoundPdpExtension;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * This is the interface for all modules responsible for finding Policy(Set)s by their
 * Policy(Set)IdReference.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a
 * cache, a disk, a connection to a remote server, etc. for retrieving the policies. Therefore,
 * these resources must be release by calling {@link #close()} when it is no longer needed.
 * 
 */
public interface RefPolicyFinderModule extends Closeable
{
	/**
	 * RefPolicyFinderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization
	 *            parameter)
	 * 
	 * 
	 */
	public abstract class Factory<CONF_T extends AbstractPolicyFinder> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RefPolicyFinderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param maxPolicySetRefDepth
		 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference):
		 *            PolicySet1 -> PolicySet2 -> ...; to be enforced by any instance created by
		 *            this factory
		 * @param expressionFactory
		 *            Expression factory for parsing XACML Expressions in the policies
		 * @param combiningAlgRegistry
		 *            Combining algorithm registry for getting implementations of algorithms used in
		 *            the policies
		 * 
		 * @return the module instance
		 */
		public abstract RefPolicyFinderModule getInstance(CONF_T conf, int maxPolicySetRefDepth, Expression.Factory expressionFactory, CombiningAlgRegistry combiningAlgRegistry);
	}

	/**
	 * Whether resolution of the policy is static, i.e. policy is resolved at initialization time,
	 * once and for all:
	 * 
	 * @return true iff static
	 */
	boolean isStatic();

	/**
	 * Tries to find one and only one matching policy given the idReference and version boundaries.
	 * 
	 * @param id
	 *            Policy(Set)Id used in the policy reference
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for this XACML datatype, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            See also:
	 *            </p>
	 *            <p>
	 *            https://java.net/projects/jaxb/lists/users/archive/2011-07/message/16
	 *            </p>
	 *            <p>
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not
	 *            all possible values of xs:anyURI can be passed to the java.net.URI constructor.
	 * @param policyType
	 *            type of policy element to be found for this reference: Policy, PolicySet
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy
	 * @param policySetRefChain
	 *            chain of PolicySetIdReference leading to the policy to be found here. This chain
	 *            is used to control all PolicySetIdReferences found within the result policy (to
	 *            detect loops, i.e. circular references, and validate reference depth); therefore
	 *            it is the responsibility of the implementation to pass this parameter as the last
	 *            one to
	 *            {@link PolicySet#PolicySet(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet, DefaultsType, Expression.Factory, CombiningAlgRegistry, RefPolicyFinder, Deque)}
	 *            henever it instantiates a {@link PolicySet}.
	 * 
	 * @return the result of looking for a matching policy, or null if no policy found with PolicyId
	 *         matching {@code idReference} and Version meeting the {@code constraints}
	 * @throws ParsingException
	 *             Error parsing found policy. The policy finder module may parse policies lazily or
	 *             on the fly, i.e. only when the policy is requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the one matching policy of type {@code policyType}, e.g. if
	 *             more than one policy is found
	 */
	<POLICY_T extends IPolicy> POLICY_T findPolicy(String id, Class<POLICY_T> policyType, VersionConstraints constraints, Deque<String> policySetRefChain) throws IndeterminateEvaluationException, ParsingException;
}
