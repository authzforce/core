/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.finder;

import java.io.Closeable;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import com.thalesgroup.authz.model.ext._3.AbstractAttributeFinder;
import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.JaxbBoundPdpExtension;
import com.thalesgroup.authzforce.core.datatypes.AttributeGUID;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.Bag;
import com.thalesgroup.authzforce.core.datatypes.DatatypeFactoryRegistry;

/**
 * This is the abstract class that all <code>AttributeFinder</code> modules extend.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a
 * cache, a disk, a connection to a remote server, etc. for retrieving the attribute values.
 * Therefore, these resources must be released by calling {@link #close()} when it is no longer
 * needed.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class AttributeFinderModule implements Closeable
{
	/**
	 * Intermediate dependency-aware {@link AttributeFinderModule} factory that can create instances
	 * of modules from a XML/JAXB configuration, and also provides the dependencies (required
	 * attributes) (based on this configuration), that any such instance (created by it) will need.
	 * Providing the dependencies helps to optimize the {@code depAttrFinder} argument to
	 * {@link #getInstance(DatatypeFactoryRegistry, AttributeFinder)} and therefore optimize the
	 * created module's job of finding its own supported attribute values based on other attributes
	 * in the evaluation context.
	 * 
	 */
	public interface DependencyAwareFactory
	{

		/**
		 * Returns non-null <code>Set</code> of <code>AttributeDesignator</code>s required as
		 * runtime inputs to the attribute finder module instance created by this builder. The PDP
		 * framework calls this method to know what input attributes the module will require
		 * (dependencies) before {@link #getInstance(DatatypeFactoryRegistry, AttributeFinder)} ,
		 * and based on this, creates a specific dependency attribute finder that will enable the
		 * module to find its dependency attributes. So when the PDP framework calls
		 * {@link #getInstance(DatatypeFactoryRegistry, AttributeFinder)} subsequently to
		 * instantiate the module, the last argument is this dependency attribute finder.
		 * 
		 * @return a <code>Set</code> of required <code>AttributeDesignatorType</code>s. Null or
		 *         empty if none required.
		 */
		Set<AttributeDesignatorType> getDependencies();

		/**
		 * Create AttributeFinderModule instance
		 * 
		 * @param attrDatatypeFactory
		 *            Attribute datatype factory for the module to be able to create attribute
		 *            values
		 * @param depAttrFinder
		 *            Attribute finder for the module to find dependency/required attributes
		 * 
		 * @return attribute value in internal model
		 */
		AttributeFinderModule getInstance(DatatypeFactoryRegistry attrDatatypeFactory, AttributeFinder depAttrFinder);
	}

	/**
	 * Preliminary factory that creates a dependency-aware AttributeFinderModule factory from
	 * parsing thee attribute dependencies (attributes on which the module depends to find its own
	 * supported attributes) declared in the XML configuration (possibly dynamic).
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization
	 *            parameter)
	 * 
	 *            This class follows the Step Factory Pattern to guide clients through the creation
	 *            of the object in a particular sequence of method calls:
	 *            <p>
	 *            http://rdafbn.blogspot.fr/2012/07/step-builder-pattern_28.html
	 *            </p>
	 */
	public static abstract class Factory<CONF_T extends AbstractAttributeFinder> extends JaxbBoundPdpExtension<CONF_T>
	{

		/**
		 * Creates an attribute-dependency-aware module factory by inferring attribute dependencies
		 * (required attributes) from {@code conf}.
		 * 
		 * @param conf
		 *            module configuration, that may define what attributes are required (dependency
		 *            attributes)
		 * @return a factory aware of dependencies (required attributes) possibly inferred from
		 *         input {@code conf}
		 */
		public abstract DependencyAwareFactory parseDependencies(CONF_T conf);
	}

	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_CATEGORY_EXCEPTION = new UnsupportedOperationException("Unsupported attribute category");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ISSUER_EXCEPTION = new UnsupportedOperationException("Unsupported attribute issuer");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ID_EXCEPTION = new UnsupportedOperationException("Unsupported attribute ID");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_DATATYPE_EXCEPTION = new UnsupportedOperationException("Unsupported attribute datetype");

	protected final String instanceID;
	protected final AttributeFinder dependencyAttributeFinder;
	protected final DatatypeFactoryRegistry attributeFactory;

	/**
	 * Instantiates the attribute finder module
	 * 
	 * @param instanceID
	 *            module instance ID (to be used as unique identifier for this instance in the logs
	 *            for example);
	 * @param attributeFactory
	 *            factory for creating attribute values
	 * @param depAttributeFinder
	 *            depenedency attribute finder. This module may require other attributes as input to
	 *            do the job. As it does not know how to get them (it is not its job), it may call
	 *            this {@code depAttributeFinder} to get them on its behalf.
	 * @throws IllegalArgumentException
	 *             if instanceId null
	 */
	protected AttributeFinderModule(String instanceID, DatatypeFactoryRegistry attributeFactory, AttributeFinder depAttributeFinder) throws IllegalArgumentException
	{
		if (instanceID == null)
		{
			throw new IllegalArgumentException("Undefined attribute finder module's instance ID");
		}

		this.instanceID = instanceID;
		this.dependencyAttributeFinder = depAttributeFinder;
		this.attributeFactory = attributeFactory;
	}

	/**
	 * Get user-defined ID for this module instance
	 * 
	 * @return instance ID
	 */
	public final String getInstanceID()
	{
		return this.instanceID;
	}

	/**
	 * Returns a non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s
	 * provided/supported by this module.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported
	 *         <code>AttributeDesignatorType</code>s
	 */
	public abstract Set<AttributeDesignatorType> getProvidedAttributes();

	/**
	 * Tries to find attribute values based on the given AttributeDesignator data. If no values were
	 * found, but no other error occurred, an empty bag is returned. This method may need to invoke
	 * the context data to look for other attribute values, so a module writer must take care not to
	 * create a scenario that loops forever.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/id/category, because not
	 * equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 * java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * If this is an AttributeSelector-only finder module, always return null.
	 * 
	 * @param attributeGUID
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find
	 * @param context
	 *            the representation of the request data
	 * @param returnDatatype
	 *            expected return datatype (
	 *            {@code AV is the expected type of every element in the bag})
	 * 
	 * @return the bag of attribute values
	 * @throws IndeterminateEvaluationException
	 *             if some error occurs, esp. error retrieving the attribute values
	 */
	public abstract <AV extends AttributeValue<AV>> Bag<AV> findAttribute(AttributeGUID attributeGUID, EvaluationContext context, Bag.Datatype<AV> returnDatatype) throws IndeterminateEvaluationException;
}
