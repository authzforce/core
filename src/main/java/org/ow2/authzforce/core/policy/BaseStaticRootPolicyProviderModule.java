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

import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ow2.authzforce.core.XACMLBindingUtils;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.core.xmlns.pdp.BaseStaticPolicyProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.springframework.util.ResourceUtils;

import com.sun.xacml.ParsingException;

/**
 * This is a simple implementation of <code>RootPolicyProviderModule</code> that supports static retrieval of the root policy. Its constructor accepts a
 * location that represent a Spring-compatible resource URL, and it is resolved to the actual policy when the module is initialized. Beyond this, there is no
 * modifying or re-loading the policy represented by this class.
 * <p>
 * Note that this class is designed to complement <code>BaseStaticRefPolicyProviderModule</code>. The reason is that when you define a configuration for your
 * PDP, it's easier to specify the two sets of policies by using two different Provider modules.
 */
public class BaseStaticRootPolicyProviderModule extends RootPolicyProviderModule.Static
{
	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RootPolicyProviderModule.Factory<BaseStaticPolicyProvider>
	{

		@Override
		public Class<BaseStaticPolicyProvider> getJaxbClass()
		{
			return BaseStaticPolicyProvider.class;
		}

		@Override
		public RootPolicyProviderModule getInstance(BaseStaticPolicyProvider conf, ExpressionFactory expressionFactory,
				CombiningAlgRegistry combiningAlgRegistry, AbstractPolicyProvider jaxbRefPolicyProviderConf, int maxPolicySetRefDepth)
		{
			final URL rootPolicyURL;
			try
			{
				// try to load the policy location as a Spring resource
				rootPolicyURL = ResourceUtils.getURL(conf.getPolicyLocation());
			} catch (FileNotFoundException ioe)
			{
				throw new IllegalArgumentException("Error loading root policy (as Spring resource) from the following URL : " + conf.getPolicyLocation(), ioe);
			}

			if (rootPolicyURL == null)
			{
				throw new IllegalArgumentException("No root policy file found at the specified location: " + conf.getPolicyLocation());
			}

			return BaseStaticRootPolicyProviderModule.getInstance(rootPolicyURL, expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf,
					maxPolicySetRefDepth);
		}
	}

	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BaseStaticRootPolicyProviderModule.class);

	private final IPolicyEvaluator rootPolicy;

	/**
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root Policy already resolved once and for all
	 * 
	 * @param jaxbPolicy
	 *            root Policy (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 */
	public BaseStaticRootPolicyProviderModule(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy jaxbPolicy, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry)
	{
		super(expressionFactory, combiningAlgRegistry, null, 0);

		try
		{
			rootPolicy = PolicyEvaluator.getInstance(jaxbPolicy, null, expressionFactory, combiningAlgRegistry);
		} catch (ParsingException e)
		{
			throw new IllegalArgumentException("Error parsing Policy: " + jaxbPolicy.getPolicyId(), e);
		}
	}

	/**
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root PolicySet already resolved once and for all
	 * 
	 * @param jaxbPolicySet
	 *            root PolicySet (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param jaxbRefPolicyProviderConf
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in {@code jaxbPolicySet}; may be null if
	 *            support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 */
	public BaseStaticRootPolicyProviderModule(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet jaxbPolicySet, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry, AbstractPolicyProvider jaxbRefPolicyProviderConf, int maxPolicySetRefDepth)
	{
		super(expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf, maxPolicySetRefDepth);

		try
		{
			rootPolicy = PolicySetEvaluator.getInstance(jaxbPolicySet, null, expressionFactory, combiningAlgRegistry, refPolicyProvider, null);
		} catch (ParsingException e)
		{
			throw new IllegalArgumentException("Error parsing PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
		}
	}

	/**
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root Policy already resolved once and for all
	 * 
	 * @param rootPolicyURL
	 *            location of root Policy(Set) (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param jaxbRefPolicyProviderConf
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in policy located at {@code rootPolicyURL};
	 *            may be null if support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 *
	 * @return instance of this class
	 * 
	 */
	public static BaseStaticRootPolicyProviderModule getInstance(URL rootPolicyURL, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry, AbstractPolicyProvider jaxbRefPolicyProviderConf, int maxPolicySetRefDepth)
	{
		if (rootPolicyURL == null)
		{
			throw new IllegalArgumentException("Undefined root policy URL");
		}

		final Unmarshaller unmarshaller;
		try
		{
			unmarshaller = XACMLBindingUtils.createXacml3Unmarshaller();
		} catch (JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Object jaxbPolicyOrPolicySetObj;
		try
		{
			jaxbPolicyOrPolicySetObj = unmarshaller.unmarshal(rootPolicyURL);
		} catch (JAXBException e)
		{
			throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + rootPolicyURL, e);
		}

		if (jaxbPolicyOrPolicySetObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
		{
			return new BaseStaticRootPolicyProviderModule((oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) jaxbPolicyOrPolicySetObj, expressionFactory,
					combiningAlgRegistry);
		} else if (jaxbPolicyOrPolicySetObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
		{
			return new BaseStaticRootPolicyProviderModule((oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) jaxbPolicyOrPolicySetObj, expressionFactory,
					combiningAlgRegistry, jaxbRefPolicyProviderConf, maxPolicySetRefDepth);
		} else
		{
			throw new IllegalArgumentException("Unexpected element found as root of the policy document: "
					+ jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
		}
	}

	@Override
	public IPolicyEvaluator getRootPolicy()
	{
		return rootPolicy;
	}
}
