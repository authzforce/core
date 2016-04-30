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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgSet;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPdpExtension;

/**
 * Loads PDP extensions (implementing {@link PdpExtension}) from classpath using {@link ServiceLoader}.
 *
 * @author cdangerv
 * @version $Id: $
 */
public class PdpExtensionLoader
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(PdpExtensionLoader.class);

	/*
	 * For each type of extension, we build the maps allowing to get the compatible/supporting extension class, using {@link ServiceLoader} API, to discover
	 * these classes from files 'META-INF/services/com.thalesgroup.authzforce.core.PdpExtension' on the classpath, in the format described by {@link
	 * ServiceLoader} API documentation.
	 */

	/*
	 * For each type of XML/JAXB-bound extension, map XML/JAXB conf class to corresponding extension (we assume a one-to-one relationship between the XML/JAXB
	 * type and the extension class)
	 */
	private final static Map<Class<? extends AbstractPdpExtension>, JaxbBoundPdpExtension<? extends AbstractPdpExtension>> JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS = new HashMap<>();

	/*
	 * Types of zero-conf (non-JAXB-bound) extesnsion
	 */
	private static final Set<Class<? extends PdpExtension>> NON_JAXB_BOUND_EXTENSION_CLASSES = new HashSet<>(Arrays.asList(DatatypeFactory.class,
			Function.class, FunctionSet.class, CombiningAlg.class, RequestFilter.Factory.class, DecisionResultFilter.class));
	/*
	 * For each type of zero-conf (non-JAXB-bound) extension, have a map (extension ID -> extension instance), so that the extension ID is scoped to the
	 * extension type among the ones listed in NON_JAXB_BOUND_EXTENSION_CLASSES (you can have same ID but for different types of extensions).
	 */
	private final static Map<Class<? extends PdpExtension>, Map<String, PdpExtension>> NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID = new HashMap<>();

	static
	{
		/*
		 * REMINDER: every service provider (implementation class) loaded by ServiceLoader MUST HAVE a ZERO-ARGUMENT CONSTRUCTOR.
		 */
		final ServiceLoader<PdpExtension> extensionLoader = ServiceLoader.load(PdpExtension.class);
		for (final PdpExtension extension : extensionLoader)
		{
			boolean isValidExt = false;
			if (extension instanceof JaxbBoundPdpExtension<?>)
			{
				final JaxbBoundPdpExtension<?> jaxbBoundExt = (JaxbBoundPdpExtension<?>) extension;
				final JaxbBoundPdpExtension<?> conflictingExt = JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.put(jaxbBoundExt.getJaxbClass(), jaxbBoundExt);
				if (conflictingExt != null)
				{
					throw new IllegalArgumentException("Extension " + jaxbBoundExt + " (" + jaxbBoundExt.getClass() + ") is conflicting with " + conflictingExt
							+ "(" + conflictingExt.getClass() + ") for the same XML/JAXB configuration class: " + jaxbBoundExt.getJaxbClass());
				}

				isValidExt = true;
			} else
			{
				for (final Class<? extends PdpExtension> extClass : NON_JAXB_BOUND_EXTENSION_CLASSES)
				{
					if (extClass.isInstance(extension))
					{
						final Map<String, PdpExtension> oldMap = NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.get(extClass);
						final Map<String, PdpExtension> newMap;
						if (oldMap == null)
						{
							newMap = new HashMap<>();
							NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.put(extClass, newMap);
						} else
						{
							newMap = oldMap;
						}

						final PdpExtension conflictingExt = newMap.put(extension.getId(), extension);
						if (conflictingExt != null)
						{
							throw new IllegalArgumentException("Extension " + extension + " is conflicting with " + conflictingExt
									+ " registered with same ID: " + extension.getId());
						}

						isValidExt = true;
						break;
					}
				}
			}

			if (!isValidExt)
			{
				throw new UnsupportedOperationException("Unsupported/invalid type of PDP extension: " + extension.getClass() + " (extension ID = "
						+ extension.getId() + ")");
			}
		}
	}

	/**
	 * Get PDP extension configuration classes (JAXB-generated from XML schema)
	 *
	 * @return classes representing datamodels of configurations of all PDP extensions
	 */
	public static Set<Class<? extends AbstractPdpExtension>> getExtensionJaxbClasses()
	{
		return Collections.unmodifiableSet(JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
	}

	/**
	 * Get non-JAXB-bound (aka zero-configuration) extension
	 *
	 * @param extensionType
	 *            type of extension: {@link DatatypeFactory }, {@link Function}, {@link CombiningAlgSet}, etc.
	 * @param id
	 *            extension ID
	 * @return PDP extension instance of class {@code extensionType} and such that its method {@link PdpExtension#getId()} returns {@code id}
	 * @throws java.lang.IllegalArgumentException
	 *             if there is not any extension found for type {@code extensionType} with ID {@code id}
	 * @param <T> a T object.
	 */
	public static <T extends PdpExtension> T getExtension(Class<T> extensionType, String id) throws IllegalArgumentException
	{
		if(!NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.containsKey(extensionType)) 
		{
			throw new IllegalArgumentException("Invalid (non-JAXB-bound) PDP extension type: " + extensionType + ". Expected types: "
					+ NON_JAXB_BOUND_EXTENSION_CLASSES);	
		}
		
		final Map<String, PdpExtension> typeSpecificExtsById = NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.get(extensionType);
		if (typeSpecificExtsById == null)
		{
			throw new IllegalArgumentException("No PDP extension of type '" + extensionType + "' found");	
		}

		final PdpExtension ext = typeSpecificExtsById.get(id);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension of type '" + extensionType + "' found with ID: " + id + ". Expected IDs: "
					+ typeSpecificExtsById.keySet());
		}

		return extensionType.cast(ext);
	}

	/**
	 * Get XML/JAXB-bound extension
	 *
	 * @param extensionType
	 *            type of extension, e.g. {@link org.ow2.authzforce.core.pdp.api.RootPolicyProviderModule.Factory}, etc.
	 * @param jaxbPdpExtensionClass
	 *            JAXB class representing XML configuration type that the extension must support
	 * @return PDP extension instance of class {@code extensionType} and such that its method {@link JaxbBoundPdpExtension#getClass()} returns
	 *         {@code jaxbPdpExtensionClass}
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension supporting {@code jaxbPdpExtensionClass}
	 * @param <JAXB_T> a JAXB_T object.
	 * @param <T> a T object.
	 */
	public static <JAXB_T extends AbstractPdpExtension, T extends JaxbBoundPdpExtension<JAXB_T>> T getJaxbBoundExtension(Class<T> extensionType,
			Class<JAXB_T> jaxbPdpExtensionClass) throws IllegalArgumentException
	{
		final JaxbBoundPdpExtension<?> ext = JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbPdpExtensionClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbPdpExtensionClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		return extensionType.cast(ext);
	}

	/**
	 * Create instance of PDP extension (AttributeProvider, ReferencedPolicyProvider...) with input configuration. The extension implementation class has been
	 * discovered by {@link ServiceLoader} from files 'META-INF/services/com.thalesgroup.authzforce.core.IPdpExtensionFactory' on the classpath, in the format
	 * described by {@link ServiceLoader} API documentation. Such class must have a constructor matching {@code constructorArgs} that is called to instantiate
	 * the extension, or a default constructor that is called instead if none matching such parameters; and it must implement {@code IPdpExtensionFactory} and
	 * therefore have a {@code IPdpExtensionFactory#init(EXTENSION_CONF_CLASS conf)} method to initialize the instance.
	 * 
	 * @param extensionConf
	 *            extension configuration (instance of custom type of PDP extension defined in XML schema)
	 * @param constructorArgs
	 *            optional Constructor arguments
	 * @return extension instance
	 * @throws IllegalArgumentException
	 *             handlerClass is not compatible with handlerconf
	 */
	// public static <EXTENSION_CONF_CLASS extends AbstractPdpExtension, EXTENSION_CLASS extends
	// PdpExtension> EXTENSION_CLASS getInstance(
	// EXTENSION_CONF_CLASS extensionConf, Object... constructorArgs)
	// {
	// @SuppressWarnings("rawtypes")
	// final Class<? extends PdpExtension> implClass =
	// EXTENSIONS_BY_CONF_TYPE.get(extensionConf.getClass());
	// final Class<?>[] constructorParameters = new Class<?>[constructorArgs.length];
	// for (int i = 0; i < constructorArgs.length; i++)
	// {
	// constructorParameters[i] = constructorArgs[i].getClass();
	// }
	//
	// Constructor<? extends PdpExtension> implConstructor;
	// try
	// {
	// implConstructor = implClass.getConstructor(constructorParameters);
	// } catch (NoSuchMethodException | SecurityException e)
	// {
	// LOGGER.info("PDP extension '{}' has no constructor matching parameters {}. Falling back to default constructor.",
	// implClass,
	// constructorParameters, e);
	// implConstructor = null;
	// }
	//
	// final PdpExtension extImpl;
	// try
	// {
	// extImpl = implConstructor == null ? implClass.newInstance() :
	// implConstructor.newInstance(constructorArgs);
	// extImpl.init(extensionConf);
	// @SuppressWarnings("unchecked")
	// final EXTENSION_CLASS extInstance = (EXTENSION_CLASS) extImpl;
	// return extInstance;
	// } catch (ClassCastException e)
	// {
	// throw new IllegalArgumentException("'" + implClass +
	// "' defined in one of the files 'META-INF/services/"
	// + IPdpExtensionFactory.class.getName() +
	// "' on the classpath is not a valid extension class for configuration type '"
	// + extensionConf.getClass() + "'", e);
	// } catch (Exception e)
	// {
	// throw new RuntimeException("Failed to instantiate extension implementation " + implClass +
	// " for configuration '"
	// + extensionConf.getClass() + "'", e);
	// }
	// }

	// /**
	// * Get class implementing specific PDP extension from class name.
	// *
	// * @param classname
	// * name of implementation class
	// * @param superclass
	// * mandatory superclass of class whose name is specified as first argument.
	// *
	// * @return implementation class
	// * @throws IllegalArgumentException
	// * implementation is not subclass of extension superclass or class unknown (not on
	// * classpath)
	// */
	// public static <EXTENSION_SUPERCLASS> Class<? extends EXTENSION_SUPERCLASS>
	// getExtensionClass(String classname,
	// Class<EXTENSION_SUPERCLASS> superclass)
	// {
	// final Class<?> implClass;
	// try
	// {
	// implClass = Class.forName(classname);
	// } catch (ClassNotFoundException e)
	// {
	// throw new IllegalArgumentException("Extension class '" + classname +
	// "' not found in classpath", e);
	// }
	//
	// if (!superclass.isAssignableFrom(implClass))
	// {
	// throw new IllegalArgumentException(implClass + " is not a subclass of " + superclass);
	// }
	//
	// return implClass.asSubclass(superclass);
	// }

	// /**
	// * Create instance of T using the default constructor of the class given as first argument.
	// *
	// * @param classname
	// * name of class with default constructor used to create the instance
	// * @param superclass
	// * class of which the returned instance must be a sub-class
	// *
	// * @return instance of superclass (type of extension)
	// * @throws IllegalArgumentException
	// * handlerClass is not compatible with handlerconf
	// */
	// public static <T> T getInstance(String classname, Class<T> superclass)
	// {
	// final Class<?> instanceClass;
	// try
	// {
	// instanceClass = Class.forName(classname);
	// } catch (ClassNotFoundException e)
	// {
	// throw new IllegalArgumentException("Extension class '" + classname +
	// "' not found in classpath", e);
	// }
	//
	// if (!superclass.isAssignableFrom(instanceClass))
	// {
	// throw new IllegalArgumentException(instanceClass + " is not a subclass of " + superclass);
	// }
	//
	// try
	// {
	// return (T) instanceClass.newInstance();
	// } catch (InstantiationException ie)
	// {
	// throw new IllegalArgumentException("Cannot instantiate " + instanceClass +
	// " with default constructor.", ie);
	// } catch (IllegalAccessException iae)
	// {
	// throw new RuntimeException("Cannot access any default constructor of " + instanceClass, iae);
	// }
	// }

}
