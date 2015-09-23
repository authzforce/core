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

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.ResourceUtils;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.pdp.model._2015._06.BaseStaticPolicyFinder;

/**
 * This is a simple implementation of <code>RootPolicyFinderModule</code> that supports static
 * retrieval of the root policy. Its constructor accepts a location that represent a
 * Spring-compatible resource URL, and it is resolved to the actual policy when the module is
 * initialized. Beyond this, there is no modifying or re-loading the policy represented by this
 * class.
 * <p>
 * Note that this class is designed to complement <code>BaseStaticRefPolicyFinderModule</code>. The
 * reason is that when you define a configuration for your PDP, it's easier to specify the two sets
 * of policies by using two different finder modules.
 */
public class BaseStaticRootPolicyFinderModule extends RootPolicyFinderModule.Static
{
	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RootPolicyFinderModule.Factory<BaseStaticPolicyFinder>
	{

		@Override
		public Class<BaseStaticPolicyFinder> getJaxbClass()
		{
			return BaseStaticPolicyFinder.class;
		}

		@Override
		public RootPolicyFinderModule getInstance(BaseStaticPolicyFinder conf, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, RefPolicyFinder refPolicyFinder)
		{
			final URL rootPolicyURL;
			try
			{
				// try to load the policy location as a Spring resource
				rootPolicyURL = ResourceUtils.getResourceURL(conf.getPolicyLocation());
			} catch (IOException ioe)
			{
				throw new IllegalArgumentException("Error loading root policy (as Spring resource) from the following URL : " + conf.getPolicyLocation(), ioe);
			}

			if (rootPolicyURL == null)
			{
				throw new IllegalArgumentException("No root policy file found at the specified location: " + conf.getPolicyLocation());
			}

			return BaseStaticRootPolicyFinderModule.getInstance(rootPolicyURL, expressionFactory, combiningAlgRegistry, refPolicyFinder);
		}
	}

	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BaseStaticRootPolicyFinderModule.class);

	private final IPolicy rootPolicy;

	/**
	 * Creates a <code>BaseStaticRootPolicyFinderModule</code> with the root Policy already resolved
	 * once and for all
	 * 
	 * @param rootPolicyURL
	 *            location of root Policy(Set) (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param refPolicyFinder
	 *            module finding policies by reference, i.e. resolving any Policy(Set)IdReference in
	 *            the root policy located at {@code policyURL}
	 * @return instance of this class
	 */
	public static BaseStaticRootPolicyFinderModule getInstance(URL rootPolicyURL, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, RefPolicyFinder refPolicyFinder)
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
			return new BaseStaticRootPolicyFinderModule((oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) jaxbPolicyOrPolicySetObj, expressionFactory, combiningAlgRegistry);
		} else if (jaxbPolicyOrPolicySetObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
		{
			return new BaseStaticRootPolicyFinderModule((oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) jaxbPolicyOrPolicySetObj, expressionFactory, combiningAlgRegistry, refPolicyFinder);
		} else
		{
			throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
		}
	}

	/**
	 * Creates a <code>BaseStaticRootPolicyFinderModule</code> with the root Policy already resolved
	 * once and for all
	 * 
	 * @param jaxbPolicy
	 *            root Policy (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 */
	public BaseStaticRootPolicyFinderModule(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy jaxbPolicy, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry)
	{
		super(expressionFactory, null);
		if (expressionFactory == null)
		{
			throw new IllegalArgumentException("Undefined Expression factory");
		}

		if (combiningAlgRegistry == null)
		{
			throw new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		}

		try
		{
			rootPolicy = new Policy(jaxbPolicy, null, expressionFactory, combiningAlgRegistry);
		} catch (ParsingException e)
		{
			throw new IllegalArgumentException("Error parsing Policy: " + jaxbPolicy.getPolicyId(), e);
		}
	}

	/**
	 * Creates a <code>BaseStaticRootPolicyFinderModule</code> with the root PolicySet already
	 * resolved once and for all
	 * 
	 * @param jaxbPolicySet
	 *            root PolicySet (JAXB) to be parsed
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param refPolicyFinder
	 *            module finding policies by reference, i.e. resolving any Policy(Set)IdReference in
	 *            the root policy located at {@code policyURL}
	 */
	public BaseStaticRootPolicyFinderModule(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet jaxbPolicySet, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, RefPolicyFinder refPolicyFinder)
	{
		super(expressionFactory, refPolicyFinder);
		if (expressionFactory == null)
		{
			throw new IllegalArgumentException("Undefined Expression factory");
		}

		if (combiningAlgRegistry == null)
		{
			throw new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		}

		try
		{
			rootPolicy = new PolicySet(jaxbPolicySet, null, expressionFactory, combiningAlgRegistry, refPolicyFinder, null);
		} catch (ParsingException e)
		{
			throw new IllegalArgumentException("Error parsing PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
		}
	}

	@Override
	public IPolicy getRootPolicy()
	{
		return rootPolicy;
	}
}
