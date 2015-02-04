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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.finder.AttributeFinderModule;
import com.thalesgroup.authzforce.model._3_0.finder.attribute.test.TestAttributeFinder;
import com.thalesgroup.authzforce.xacml.schema.XACMLCategory;

/**
 * 
 * Fake AttributeFinderModule for test purposes only that can be configured to support a specific
 * set of attribute finders, but always return an empty bag as attribute value.
 * 
 */
public class TestAttributeFinderModule extends AttributeFinderModule<TestAttributeFinder>
{

	private final Set<Integer> supportedDesignatorTypes = new HashSet<>();

	@Override
	public void init(TestAttributeFinder conf)
	{
		for(final AttributeDesignatorType attrDes: conf.getProvidedAttributes()) {
			final int attrCatId;
			final XACMLCategory category = XACMLCategory.fromValue(attrDes.getCategory());
			switch (category)
			{
				case XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_CODEBASE:
				case XACML_1_0_SUBJECT_CATEGORY_INTERMEDIARY_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_RECIPIENT_SUBJECT:
				case XACML_1_0_SUBJECT_CATEGORY_REQUESTING_MACHINE:
					attrCatId = AttributeDesignator.SUBJECT_TARGET;
					break;
				case XACML_3_0_RESOURCE_CATEGORY_RESOURCE:
					attrCatId = AttributeDesignator.RESOURCE_TARGET;
					break;
				case XACML_3_0_ACTION_CATEGORY_ACTION:
					attrCatId = AttributeDesignator.ACTION_TARGET;
					break;
				case XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT:
					attrCatId = AttributeDesignator.ENVIRONMENT_TARGET;
					break;
				default:
					throw new IllegalArgumentException("Unknown attribute category: " + category);
			}
			
			supportedDesignatorTypes.add(attrCatId);
		}

	}

	@Override
	public boolean isDesignatorSupported()
	{
		return true;
	}

	@Override
	public boolean isSelectorSupported()
	{
		return false;
	}

	@Override
	public Set<Integer> getSupportedDesignatorTypes()
	{
		return supportedDesignatorTypes;
	}

	@Override
	public Set getSupportedIds()
	{
		return null;
	}

	@Override
	public void invalidateCache()
	{
	}

	@Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, URI issuer, URI subjectCategory, EvaluationCtx context,
			int designatorType)
	{
		return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
	}

}
