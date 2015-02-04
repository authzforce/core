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
package com.thalesgroup.authzforce.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.xml.transform.ResourceSource;

import com.sun.xacml.PDPConfig;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeProxy;
import com.sun.xacml.attr.BaseAttributeFactory;
import com.sun.xacml.attr.StandardAttributeFactory;
import com.sun.xacml.combine.BaseCombiningAlgFactory;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.StandardCombiningAlgFactory;
import com.sun.xacml.cond.BaseFunctionFactory;
import com.sun.xacml.cond.BasicFunctionFactoryProxy;
import com.sun.xacml.cond.FunctionFactoryProxy;
import com.sun.xacml.cond.StandardFunctionFactory;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.ResourceFinder;
import com.sun.xacml.finder.ResourceFinderModule;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.SelectorModule;
import com.sun.xacml.support.finder.StaticPolicyFinderModule;
import com.sun.xacml.support.finder.StaticRefPolicyFinderModule;
import com.thalesgroup.authz.model.ext._3.AbstractAttributeFinder;
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;
import com.thalesgroup.authz.model.ext._3.AbstractResourceFinder;
import com.thalesgroup.authz.model.ext._3.Cache;
import com.thalesgroup.authz.model.ext._3.CacheMemoryStoreEvictionPolicy;
import com.thalesgroup.authzforce.pdp.model._2014._12.AttributeFactory;
import com.thalesgroup.authzforce.pdp.model._2014._12.AttributeSelectorXPathFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.CombiningAlgFactory;
import com.thalesgroup.authzforce.pdp.model._2014._12.CombiningAlgFactory.Algorithm;
import com.thalesgroup.authzforce.pdp.model._2014._12.CurrentDateTimeFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.FunctionFactory;
import com.thalesgroup.authzforce.pdp.model._2014._12.Functions;
import com.thalesgroup.authzforce.pdp.model._2014._12.Functions.Function;
import com.thalesgroup.authzforce.pdp.model._2014._12.Functions.FunctionCluster;
import com.thalesgroup.authzforce.pdp.model._2014._12.PDP;
import com.thalesgroup.authzforce.pdp.model._2014._12.Pdps;
import com.thalesgroup.authzforce.pdp.model._2014._12.StaticPolicyFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.StaticRefPolicyFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.XacmlFeatureIdToImplementation;

/**
 * XML-based Configuration manager using XML schema and JAXB to load PDP configurations
 * 
 */
public class PdpConfigurationManager
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpConfigurationManager.class);
	private final Map<String, PDPConfig> pdpMap = new HashMap<>();
	private final Map<String, com.sun.xacml.attr.AttributeFactory> attrFactoryMap = new HashMap<>();
	private final Map<String, com.sun.xacml.combine.CombiningAlgFactory> combAlgFactoryMap = new HashMap<>();
	private final Map<String, FunctionFactoryProxy> functionFactoryProxyMap = new HashMap<>();
	private final PDPConfig defaultPDPConfig;
	private final com.sun.xacml.attr.AttributeFactory defaultAttributeFactory;
	private final com.sun.xacml.combine.CombiningAlgFactory defaultCombiningAlgFactory;
	private final FunctionFactoryProxy defaultFunctionFactoryProxy;

	/**
	 * Load PDP configuration handler.
	 * 
	 * @param confLocation
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * 
	 * @throws IOException
	 *             I/O error reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * 
	 */
	public PdpConfigurationManager(String confLocation) throws IOException, JAXBException
	{
		this(confLocation, null, null);
	}

	/**
	 * Load PDP configuration handler. Locations here can be any resource string supported by Spring
	 * ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
	 * @param confLocation
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * 
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if
	 *            exists; in such XSD, there must be a XSD import for each extension, where the
	 *            'schemaLocation' attribute value must be
	 *            ${fully_qualidifed_jaxb_class_bound_to_extension_XML_type}.xsd, for example:
	 * 
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?> 
	 * 		  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 *            targetNamespace="http://thalesgroup.com/authzforce/model/3.0"
	 *            xmlns:tns="http://thalesgroup.com/authzforce/model/3.0"
	 *            elementFormDefault="qualified" attributeFormDefault="unqualified">
	 * 
	 *            <xs:import
	 *            namespace="http://thalesgroup.com/authzforce/model/3.0/finder/attribute/rest"
	 *            schemaLocation=
	 *            "com.thalesgroup.authzforce.model._3_0.finder.attribute.rest.RESTfulAttributeFinder.xsd"
	 *            />
	 * 
	 *            </xs:schema>
	 * 			}
	 * </pre>
	 * 
	 *            In this example,
	 *            'com.thalesgroup.authzforce.model._3_0.finder.attribute.rest.RESTfulAttributeFinde
	 *            r ' is the JAXB-annotated class bound to XML type 'RESTfulAttributeFinder'. We
	 *            assume that this XML type is an extension of one the PDP extension base types,
	 *            'AbstractAttributeFinder' (that extends 'AbstractPdpExtension' like all other
	 *            extension base types) in this case.
	 * 
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP
	 *            configuration schema) and the extension XSD specified as 'extensionXsdLocation'
	 *            argument (may be null)
	 * @throws IOException
	 *             I/O error reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * 
	 */
	public PdpConfigurationManager(String confLocation, String catalogLocation, String extensionXsdLocation) throws IOException, JAXBException
	{
		this(confLocation, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	/**
	 * @param confLocation
	 *            location of PDP configuration
	 * @param modelhandler
	 *            PDP configuration model handler
	 * @throws IOException
	 *             I/O error occurred reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 */
	public PdpConfigurationManager(String confLocation, PdpModelHandler modelhandler) throws IOException, JAXBException
	{
		final Resource confResource = ResourceUtils.getResource(confLocation);
		if (confResource == null || !confResource.exists())
		{
			throw new IllegalArgumentException("No resource available at this location: " + confLocation);
		}

		final Source src;
		try
		{
			src = new ResourceSource(confResource);
		} catch (IOException e)
		{
			throw new IOException("Error reading from configuration location: " + confLocation, e);
		}

		/*
		 * Base directory for resolving relative file paths in configuration = parent directory of
		 * the configuration file
		 */
		File confFile = null;
		try
		{
			confFile = confResource.getFile();
		} catch (IOException e)
		{
			LOGGER.warn("Location of PDP configuration could not be resolved to a file on the filesystem, therefore no parent directory is defined and relative paths in the configuration cannot be resolved as such.");
		}

		final File baseDirectory = confFile == null ? null : confFile.getParentFile();
		final Pdps pdpsConf = modelhandler.unmarshal(src, Pdps.class);
		for (final PDP pdp : pdpsConf.getPdps())
		{
			pdpMap.put(pdp.getName(), getPDPConfig(pdp, baseDirectory));
		}

		for (final AttributeFactory attrFactoryConf : pdpsConf.getAttributeFactories())
		{
			attrFactoryMap.put(attrFactoryConf.getName(), getAttributeFactory(attrFactoryConf));
		}

		for (final CombiningAlgFactory combAlgFactoryConf : pdpsConf.getCombiningAlgFactories())
		{
			combAlgFactoryMap.put(combAlgFactoryConf.getName(), getCombiningAlgFactory(combAlgFactoryConf));
		}

		for (final FunctionFactory functionFactoryConf : pdpsConf.getFunctionFactories())
		{
			functionFactoryProxyMap.put(functionFactoryConf.getName(), getFunctionFactoryProxy(functionFactoryConf));
		}

		final String defaultPdpName = pdpsConf.getDefaultPDP();
		if (defaultPdpName == null)
		{
			throw new IllegalArgumentException("No default PDP defined");
		}

		defaultPDPConfig = pdpMap.get(defaultPdpName);
		if (defaultPDPConfig == null)
		{
			throw new IllegalArgumentException("No default PDP found with name '" + defaultPdpName + "'");
		}

		final String defaultAttributeFactoryName = pdpsConf.getDefaultAttributeFactory();
		if (defaultAttributeFactoryName == null)
		{
			throw new IllegalArgumentException("No default AttributeFactory defined");
		}

		defaultAttributeFactory = attrFactoryMap.get(defaultAttributeFactoryName);
		if (defaultAttributeFactory == null)
		{
			throw new IllegalArgumentException("No default AttributeFactory found with name '" + defaultAttributeFactoryName + "'");
		}

		final String defaultCombiningAlgFactoryName = pdpsConf.getDefaultCombiningAlgFactory();
		if (defaultCombiningAlgFactoryName == null)
		{
			throw new IllegalArgumentException("No default CombiningAlgFactory defined");
		}

		defaultCombiningAlgFactory = this.combAlgFactoryMap.get(defaultCombiningAlgFactoryName);
		if (defaultCombiningAlgFactory == null)
		{
			throw new IllegalArgumentException("No default CombiningAlgFactory found with name '" + defaultCombiningAlgFactoryName + "'");
		}

		final String defaultFunctionFactoryName = pdpsConf.getDefaultFunctionFactory();
		if (defaultFunctionFactoryName == null)
		{
			throw new IllegalArgumentException("No default FunctionFactory defined");
		}

		defaultFunctionFactoryProxy = this.functionFactoryProxyMap.get(defaultFunctionFactoryName);
		if (defaultFunctionFactoryProxy == null)
		{
			throw new IllegalArgumentException("No default FunctionFactory found with name '" + defaultFunctionFactoryName + "'");
		}
	}

	/**
	 * Returns the default PDP configuration. If no default was specified then this throws an
	 * exception.
	 * 
	 * @return the default PDP configuration, null if none defined
	 * 
	 */
	public PDPConfig getDefaultPDPConfig()
	{
		return defaultPDPConfig;
	}

	/**
	 * Get default AttributeFactory
	 * 
	 * @return default AttributeFactory
	 */
	public com.sun.xacml.attr.AttributeFactory getDefaultAttributeFactory()
	{
		return defaultAttributeFactory;
	}

	/**
	 * Get default CombiningAlgFactory
	 * 
	 * @return default CombiningAlgFactory
	 */
	public com.sun.xacml.combine.CombiningAlgFactory getDefaultCombiningAlgFactory()
	{
		return defaultCombiningAlgFactory;
	}

	/**
	 * Get default FunctionFactoryProxy
	 * 
	 * @return default FunctionFactoryProxy
	 */
	public FunctionFactoryProxy getDefaultFunctionFactoryProxy()
	{
		return defaultFunctionFactoryProxy;
	}

	private static PDPConfig getPDPConfig(PDP pdp, File baseDirectory)
	{
		// PolicyFinders
		final List<PolicyFinderModule<?>> policyFinderModuleList = new ArrayList<>();
		for (final AbstractPolicyFinder policyFinderConf : pdp.getPolicyFinders())
		{
			final PolicyFinderModule<?> policyFinderModule;
			// check core PolicyFinders first
			if (policyFinderConf instanceof StaticPolicyFinder)
			{
				final StaticPolicyFinder staticPolicyFinderConf = (StaticPolicyFinder) policyFinderConf;
				try
				{
					policyFinderModule = new StaticPolicyFinderModule(staticPolicyFinderConf.getCombiningAlgId(),
							staticPolicyFinderConf.getPolicyLocations());
				} catch (URISyntaxException | UnknownIdentifierException e)
				{
					throw new IllegalArgumentException("Invalid StaticPolicyFinder configuration", e);
				}
			} else if (policyFinderConf instanceof StaticRefPolicyFinder)
			{
				final StaticRefPolicyFinder staticRefPolicyFinderConf = (StaticRefPolicyFinder) policyFinderConf;
				final List<URL> policyURLs = new ArrayList<>();
				for (final String policyLocation : staticRefPolicyFinderConf.getPolicyLocations())
				{
					try
					{
						policyURLs.add(new URL(policyLocation));
					} catch (MalformedURLException e)
					{
						throw new IllegalArgumentException("Invalid StaticRefPolicyFinder configuration: policyLocation is not a valid URL", e);
					}
				}
				policyFinderModule = new StaticRefPolicyFinderModule(policyURLs, PdpModelHandler.XACML_3_0_SCHEMA);
			} else
			{
				policyFinderModule = PdpExtensionFactory.getInstance(policyFinderConf);
			}
			policyFinderModuleList.add(policyFinderModule);
		}

		/*
		 * Sets the policyFinder's base directory for finder modules to resolve relative policy file
		 * paths as relative to the same parent directory of the PDP config file
		 */
		final PolicyFinder policyFinder = new PolicyFinder(baseDirectory);
		policyFinder.setModules(policyFinderModuleList);

		// AttributeFinders
		final List<AttributeFinderModule<?>> attrFinderModuleList = new ArrayList<>();
		for (final AbstractAttributeFinder attrFinderConf : pdp.getAttributeFinders())
		{
			final AttributeFinderModule<?> attrFinderModule;
			// check core AttributeFinders first
			if (attrFinderConf instanceof CurrentDateTimeFinder)
			{
				attrFinderModule = new CurrentEnvModule();
			} else if (attrFinderConf instanceof AttributeSelectorXPathFinder)
			{
				attrFinderModule = new SelectorModule();
			} else
			{
				// extensions
				attrFinderModule = PdpExtensionFactory.getInstance(attrFinderConf);
			}

			attrFinderModuleList.add(attrFinderModule);
		}

		final AttributeFinder attrFinder = new AttributeFinder();
		attrFinder.setModules(attrFinderModuleList);

		// ResourceFinders
		final List<ResourceFinderModule<?>> resFinderModuleList = new ArrayList<>();
		for (final AbstractResourceFinder resFinderConf : pdp.getResourceFinders())
		{
			final ResourceFinderModule<?> resFinderModule = PdpExtensionFactory.getInstance(resFinderConf);
			resFinderModuleList.add(resFinderModule);
		}

		final ResourceFinder resFinder = new ResourceFinder();
		resFinder.setModules(resFinderModuleList);

		// cache
		final Cache cacheConf = pdp.getCache();
		final net.sf.ehcache.Cache cache;
		if (cacheConf == null)
		{
			cache = null;
		} else
		{
			final String cacheName = PDPConfig.class + "#" + pdp.getName();
			final int maxEltsInMem = cacheConf.getMaxElementsInMemory().intValue();
			final boolean overflowToDisk = cacheConf.isOverflowToDisk();
			final boolean isDiskPersistent = cacheConf.isDiskPersistent();
			final int diskExpiryThreadIntervalSec = cacheConf.getDiskExpiryThreadIntervalSeconds().intValue();
			final int maxEltsOnDisk = cacheConf.getMaxElementsOnDisk().intValue();
			final CacheMemoryStoreEvictionPolicy memStoreEvictionPolicy = cacheConf.getMemoryStoreEvictionPolicy();
			final MemoryStoreEvictionPolicy evictionPolicy = MemoryStoreEvictionPolicy.fromString(memStoreEvictionPolicy.name());
			final int TTIsec = cacheConf.getTimeToIdleSec();
			final int TTLsec = cacheConf.getTimeToLiveSec();
			final boolean eternal = TTIsec == 0 && TTLsec == 0;
			/*
			 * diskStorePath parameter ignored in constructor below, therefore set to null
			 */
			final String diskStorePath = null;
			cache = new net.sf.ehcache.Cache(cacheName, maxEltsInMem, evictionPolicy, overflowToDisk, diskStorePath, eternal, TTLsec, TTIsec,
					isDiskPersistent, diskExpiryThreadIntervalSec, null, null, maxEltsOnDisk);
		}
		
		return new PDPConfig(attrFinder, policyFinder, resFinder, cache);
	}

	/**
	 * Private helper used to instantiate functions based on XML definition (JAXB) and add them to a
	 * factory, so the factory may be modified by this method.
	 */
	private static void addFunctionsToFactory(Functions functionsDef, com.sun.xacml.cond.FunctionFactory factory)
	{
		for (final Function funcDef : functionsDef.getFunctions())
		{
			final com.sun.xacml.cond.Function function = PdpExtensionFactory.getInstance(funcDef.getClazz(), com.sun.xacml.cond.Function.class);
			factory.addFunction(function);
		}

		for (final XacmlFeatureIdToImplementation funcDef : functionsDef.getAbstractFunctions())
		{
			final com.sun.xacml.cond.FunctionProxy function = PdpExtensionFactory.getInstance(funcDef.getClazz(),
					com.sun.xacml.cond.FunctionProxy.class);
			final URI uri = URI.create(funcDef.getId());
			factory.addAbstractFunction(function, uri);
		}

		for (final FunctionCluster funcDef : functionsDef.getFunctionClusters())
		{
			final com.sun.xacml.cond.cluster.FunctionCluster functionCluster = PdpExtensionFactory.getInstance(funcDef.getClazz(),
					com.sun.xacml.cond.cluster.FunctionCluster.class);
			for (com.sun.xacml.cond.Function func : functionCluster.getSupportedFunctions())
			{
				factory.addFunction(func);
			}
		}
	}

	private static FunctionFactoryProxy getFunctionFactoryProxy(FunctionFactory functionFactoryConf)
	{
		final FunctionFactoryProxy proxy;
		final com.sun.xacml.cond.FunctionFactory generalFactory;
		final com.sun.xacml.cond.FunctionFactory conditionFactory;
		final com.sun.xacml.cond.FunctionFactory targetFactory;

		if (functionFactoryConf.isUseStandardFunctions())
		{
			proxy = StandardFunctionFactory.getNewFactoryProxy();
			generalFactory = proxy.getGeneralFactory();
			conditionFactory = proxy.getConditionFactory();
			targetFactory = proxy.getTargetFactory();
		} else
		{
			generalFactory = new BaseFunctionFactory();
			conditionFactory = new BaseFunctionFactory(generalFactory);
			targetFactory = new BaseFunctionFactory(conditionFactory);
			proxy = new BasicFunctionFactoryProxy(targetFactory, conditionFactory, generalFactory);
		}

		final Functions targetFunctions = functionFactoryConf.getTarget();
		if (targetFunctions != null)
		{
			addFunctionsToFactory(targetFunctions, targetFactory);
		}

		final Functions conditionFunctions = functionFactoryConf.getCondition();
		if (conditionFunctions != null)
		{
			addFunctionsToFactory(conditionFunctions, conditionFactory);
		}

		final Functions generalFunctions = functionFactoryConf.getGeneral();
		if (generalFunctions != null)
		{
			addFunctionsToFactory(generalFunctions, generalFactory);
		}

		return proxy;
	}

	private static com.sun.xacml.combine.CombiningAlgFactory getCombiningAlgFactory(CombiningAlgFactory combAlgFactoryConf)
	{
		final com.sun.xacml.combine.CombiningAlgFactory factory;
		// check if we're starting with the standard factory setup
		if (combAlgFactoryConf.isUseStandardAlgorithms())
		{
			factory = StandardCombiningAlgFactory.getNewFactory();
		} else
		{
			factory = new BaseCombiningAlgFactory();
		}

		for (final Algorithm algConf : combAlgFactoryConf.getAlgorithms())
		{
			final String implClassName = algConf.getClazz();
			if (implClassName == null)
			{
				throw new IllegalArgumentException("Undefined implementation class for one of CombiningAlgFactory algorithms");
			}

			final CombiningAlgorithm alg = PdpExtensionFactory.getInstance(implClassName, CombiningAlgorithm.class);
			factory.addAlgorithm(alg);
		}

		return factory;
	}

	private static com.sun.xacml.attr.AttributeFactory getAttributeFactory(AttributeFactory attrFactoryConf)
	{
		final com.sun.xacml.attr.AttributeFactory factory;
		if (attrFactoryConf.isUseStandardDatatypes())
		{
			factory = StandardAttributeFactory.getNewFactory();
		} else
		{
			factory = new BaseAttributeFactory();
		}

		for (final XacmlFeatureIdToImplementation datatypeIdToFactoryConf : attrFactoryConf.getDatatypes())
		{
			final String datatypeId = datatypeIdToFactoryConf.getId();
			if (datatypeId == null)
			{
				throw new IllegalArgumentException("Undefined id for one of AttributeFactory datatype");
			}

			final String implClassName = datatypeIdToFactoryConf.getClazz();
			if (implClassName == null)
			{
				throw new IllegalArgumentException("Undefined implementation class for AttributeFactory datatype: " + datatypeId);
			}

			final AttributeProxy attrProxy = PdpExtensionFactory.getInstance(implClassName, AttributeProxy.class);
			factory.addDatatype(datatypeId, attrProxy);
		}

		return factory;
	}

}
