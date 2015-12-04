/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.policy;

import java.io.Closeable;
import java.util.Deque;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.XACMLParsers.XACMLParserFactory;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

/**
 * This is the interface for all modules responsible for finding Policy(Set)s by their Policy(Set)IdReference.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving the policies. Therefore, these resources must be release by calling {@link #close()} when it is no longer needed.
 * 
 */
public interface RefPolicyProviderModule extends Closeable
{
	/**
	 * RefPolicyProviderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization parameter)
	 * 
	 * 
	 */
	abstract class Factory<CONF_T extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RefPolicyProviderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param maxPolicySetRefDepth
		 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; to be enforced by any
		 *            instance created by this factory
		 * @param expressionFactory
		 *            Expression factory for parsing XACML Expressions in the policies
		 * @param combiningAlgRegistry
		 *            Combining algorithm registry for getting implementations of algorithms used in the policies
		 * 
		 * @return the module instance
		 */
		public abstract RefPolicyProviderModule getInstance(CONF_T conf, XACMLParserFactory xacmlParserFactory, int maxPolicySetRefDepth,
				ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry);
	}

	/**
	 * Whether resolution of the policy is static, i.e. policy is resolved at initialization time, once and for all:
	 * 
	 * @return true iff static
	 */
	boolean isStatic();

	/**
	 * Gets one and only one policy matching a given idReference and version boundaries.
	 * 
	 * @param id
	 *            Policy(Set)Id used in the policy reference
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for this XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD
	 *            anyURI [1], not in java.net.URI.
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
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the
	 *            java.net.URI constructor.
	 * @param policyType
	 *            type of policy element to be found for this reference: Policy, PolicySet
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy
	 * @param policySetRefChain
	 *            chain of PolicySetIdReference leading to the policy to be found here. This chain is used to control all PolicySetIdReferences found within the
	 *            result policy (to detect loops, i.e. circular references, and validate reference depth); therefore it is the responsibility of the
	 *            implementation to pass this parameter as the last one to
	 *            {@link PolicySetEvaluator#getInstance(PolicySet, net.sf.saxon.s9api.XPathCompiler, Map, ExpressionFactory, CombiningAlgRegistry, RefPolicyProvider, Deque)}
	 *            whenever it instantiates a {@link PolicySetEvaluator}.
	 * 
	 * @return the result of looking for a matching policy, or null if no policy found with PolicyId matching {@code idReference} and Version meeting the
	 *         {@code constraints}
	 * @throws ParsingException
	 *             Error parsing found policy. The policy Provider module may parse policies lazily or on the fly, i.e. only when the policy is requested/looked
	 *             for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the one matching policy of type {@code policyType}, e.g. if more than one policy is found
	 */
	<POLICY_T extends IPolicyEvaluator> POLICY_T get(String id, Class<POLICY_T> policyType, VersionConstraints constraints, Deque<String> policySetRefChain)
			throws IndeterminateEvaluationException, ParsingException;
}
