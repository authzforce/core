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
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.BagDatatype;
import com.thalesgroup.authzforce.core.eval.Bags;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.model._3_0.finder.attribute.test.TestAttributeFinder;

/**
 * 
 * Fake AttributeFinderModule for test purposes only that can be configured to support a specific
 * set of attribute finders, but always return an empty bag as attribute value.
 * 
 */
public class TestAttributeFinderModule extends AttributeFinderModule
{
	/**
	 * module factory
	 * 
	 */
	public static class Factory extends AttributeFinderModule.Factory<TestAttributeFinder>
	{

		@Override
		public Class<TestAttributeFinder> getJaxbClass()
		{
			return TestAttributeFinder.class;
		}

		@Override
		public DependencyAwareFactory<TestAttributeFinder> parseDependencies(final TestAttributeFinder conf)
		{
			return new DependencyAwareFactory<TestAttributeFinder>()
			{

				@Override
				public Set<AttributeDesignatorType> getDependencies()
				{
					// no dependency
					return null;
				}

				@Override
				public AttributeFinderModule getInstance(DatatypeFactoryRegistry attrDatatypeFactory, AttributeFinder depAttrFinder)
				{
					return new TestAttributeFinderModule(conf);
				}
			};
		}

	}

	private final Set<AttributeDesignatorType> supportedDesignatorTypes;
	private final Set<AttributeGUID> supportedAttrIds = new HashSet<>();

	private TestAttributeFinderModule(TestAttributeFinder conf)
	{
		super(conf.getId(), null, null);
		supportedDesignatorTypes = new HashSet<>(conf.getProvidedAttributes());
		for (final AttributeDesignatorType attrDes : supportedDesignatorTypes)
		{
			supportedAttrIds.add(new AttributeGUID(attrDes));
		}
	}

	@Override
	public void close() throws IOException
	{
	}

	@Override
	public Set<AttributeDesignatorType> getProvidedAttributes()
	{
		return supportedDesignatorTypes;
	}

	@Override
	public <AV extends AttributeValue<AV>> Bag<AV> findAttribute(AttributeGUID attributeGUID, EvaluationContext context, BagDatatype<AV> returnDatatype) throws IndeterminateEvaluationException
	{
		if (supportedAttrIds.contains(attributeGUID))
		{
			return Bags.empty(returnDatatype, null);
		}

		return null;
	}

}
