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
package org.ow2.authzforce.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Closeable AttributeFinder
 * <p>
 * The sub-modules may very likely hold resources such as network resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore,
 * you are required to call {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in
 * order to make sure these resources are released properly by each underlying module (e.g. close the attribute caches).
 * 
 */
public final class CloseableAttributeFinder extends BaseAttributeProvider implements Closeable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CloseableAttributeFinder.class);

	// AttributeDesignator finder modules by supported/provided attribute ID (global ID: category, issuer,
	// AttributeId)
	protected final BaseAttributeProviderModule.Map designatorModsByAttrId;

	/**
	 * Instantiates attribute finder that tries to find attribute values in evaluation context, then, if not there, query the {@code module} providing the
	 * requested attribute ID, if any.
	 * 
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * 
	 * @param jaxbAttributeFinderConfs
	 *            (optional) XML/JAXB configurations of Attribute Finders for AttributeDesignator/AttributeSelector evaluation; may be null for static
	 *            expression evaluation (out of context), in which case AttributeSelectors/AttributeDesignators are not supported
	 * @throws IllegalArgumentException
	 *             If any of attribute finder modules created from {@code jaxbAttributeFinderConfs} does not provide any attribute; or it is in conflict with
	 *             another one already registered to provide the same or part of the same attributes.
	 * @throws IOException
	 *             error closing the attribute finder modules created from {@code jaxbAttributeFinderConfs}, when and before an {@link IllegalArgumentException}
	 *             is raised
	 */
	public CloseableAttributeFinder(List<AbstractAttributeFinder> jaxbAttributeFinderConfs, DatatypeFactoryRegistry attributeFactory) throws IOException
	{
		if (jaxbAttributeFinderConfs == null)
		{
			designatorModsByAttrId = null;
		} else
		{
			designatorModsByAttrId = new BaseAttributeProviderModule.Map(jaxbAttributeFinderConfs.size());
			for (final AbstractAttributeFinder jaxbAttrFinder : jaxbAttributeFinderConfs)
			{
				try
				{
					this.designatorModsByAttrId.addModule(jaxbAttrFinder, attributeFactory);
				} catch (IllegalArgumentException e)
				{
					this.designatorModsByAttrId.close();
					throw e;
				}
			}
		}
	}

	@Override
	public void close() throws IOException
	{
		// designatorModsByAttrId not null
		/**
		 * Invalidate caches of old/replaced modules since they are no longer used
		 */
		this.designatorModsByAttrId.close();
	}

	@Override
	protected AttributeProviderModule getProvider(AttributeGUID attributeGUID)
	{
		LOGGER.debug("Requesting attribute {} from finder modules (by provided attribute ID): {}", attributeGUID, designatorModsByAttrId);
		return designatorModsByAttrId == null ? null : designatorModsByAttrId.get(attributeGUID);
	}
}