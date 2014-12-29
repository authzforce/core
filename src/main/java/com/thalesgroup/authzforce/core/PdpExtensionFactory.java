/**
 * Copyright (C) 2011-2014 Thales Services SAS.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.thalesgroup.authz.model.ext._3.AbstractPdpExtension;

/**
 * Factory for creating instances of PDP extensions (AttributeFinder, PolicyFinder...)
 * 
 */
public class PdpExtensionFactory
{
	/**
	 * Map allowing to get the compatible/supporting extension from a given (XML-schema-defined)
	 * configuration type. Using {@link ServiceLoader} API, extension implementation classes are
	 * discovered from files 'META-INF/services/com.thalesgroup.authzforce.core.IPdpExtension' on
	 * the classpath, in the format described by {@link ServiceLoader} API documentation.
	 */
	@SuppressWarnings("rawtypes")
	private final static Map<Class<? extends AbstractPdpExtension>, Class<? extends IPdpExtension>> extensionS_BY_CONF_TYPE = new HashMap<>();

	static
	{
		final String modInitMethodName = IPdpExtension.class.getMethods()[IPdpExtension.INIT_METHOD_INDEX].getName();
		@SuppressWarnings("rawtypes")
		final ServiceLoader<IPdpExtension> extensionLoader = ServiceLoader.load(IPdpExtension.class);
		for (IPdpExtension<?> extension : extensionLoader)
		{
			for (Method m : extension.getClass().getMethods())
			{
				if (m.getName().equals(modInitMethodName))
				{
					final Class<?>[] types = m.getParameterTypes();
					// ignore abstract handler model classes (implicitly declared in impl classes by
					// derivation)
					if (types.length > 0 && AbstractPdpExtension.class.isAssignableFrom(types[0]) && !Modifier.isAbstract(types[0].getModifiers()))
					{
						@SuppressWarnings("rawtypes")
						final Class<? extends IPdpExtension> oldextension = extensionS_BY_CONF_TYPE.put(types[0].asSubclass(AbstractPdpExtension.class),
								extension.getClass().asSubclass(IPdpExtension.class));
						if (oldextension != null)
						{
							throw new IllegalArgumentException(
									"Conflict: multiple extensions found for the same configuration type. Only one is allowed on the classpath. Check files named 'META-INF/services/"
											+ IPdpExtension.class.getName() + "'.");
						}
					}
				}
			}
		}
	}

	/**
	 * Create instance of PDP extension (AttributeFinder, PolicyFinder...) with input configuration.
	 * The extension implementation class has been discovered by {@link ServiceLoader} from files
	 * 'META-INF/services/com.thalesgroup.authzforce.core.IPdpExtension' on the classpath, in the
	 * format described by {@link ServiceLoader} API documentation. Such class must implement
	 * {@code IPdpExtension} and therefore have a {@code IPdpExtension#init(EXTENSION_CONF_CLASS conf)}
	 * method to initialize the instance.
	 * 
	 * @param extensionConf
	 *            extension configuration (instance of custom type of PDP extension defined in XML schema)
	 * @return extension instance
	 * @throws IllegalArgumentException
	 *             handlerClass is not compatible with handlerconf
	 */
	public static <EXTENSION_CONF_CLASS extends AbstractPdpExtension, EXTENSION_CLASS extends IPdpExtension<?>> EXTENSION_CLASS getInstance(
			EXTENSION_CONF_CLASS extensionConf)
	{
		@SuppressWarnings("rawtypes")
		final Class<? extends IPdpExtension> implClass = extensionS_BY_CONF_TYPE.get(extensionConf.getClass());
		final IPdpExtension<EXTENSION_CONF_CLASS> extImpl;
		try
		{
			extImpl = implClass.newInstance();
			extImpl.init(extensionConf);
			@SuppressWarnings("unchecked")
			final EXTENSION_CLASS extInstance = (EXTENSION_CLASS) extImpl;
			return extInstance;
		} catch (ClassCastException e)
		{
			throw new IllegalArgumentException("'" + implClass + "' defined in one of the files 'META-INF/services/" + IPdpExtension.class.getName()
					+ "' on the classpath is not a valid extension class for configuration type '" + extensionConf.getClass() + "'", e);
		} catch (Exception e)
		{
			throw new RuntimeException("Failed to instantiate extension implementation " + implClass + " for configuration '" + extensionConf.getClass()
					+ "'", e);
		}
	}

	/**
	 * Create instance of PDP extension (AttributeFinder, PolicyFinder...) using the default
	 * constructor of the class given as first argument.
	 * @param classname name of class with default constructor used to create the instance
	 * @param superclass mandatory superclass of class whose name is specified as first argument; this is the return type.
	 * 
	 * @return instance of superclass (type of extension)
	 * @throws IllegalArgumentException
	 *             handlerClass is not compatible with handlerconf
	 */
	public static <EXTENSION_SUPERCLASS> EXTENSION_SUPERCLASS getInstance(String classname, Class<EXTENSION_SUPERCLASS> superclass)
	{
		final Class<?> implClass;
		try
		{
			implClass = Class.forName(classname);
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Extension class '" + classname + "' not found in classpath", e);
		}
		
		if(!superclass.isAssignableFrom(implClass)) {
			throw new IllegalArgumentException(implClass + " is not a subclass of " + superclass);
		}
		
		final Object implInstance;
		try {
			 implInstance = implClass.newInstance();
		} catch (InstantiationException ie) {
			throw new IllegalArgumentException("Cannot instantiate " + implClass + " with default constructor.", ie);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException("Cannot access any default constructor of " + implClass, iae);
		}
		
		final EXTENSION_SUPERCLASS extImpl;
		try
		{
			extImpl = superclass.cast(implInstance);
		} catch (ClassCastException e)
		{
			throw new IllegalArgumentException("'" + implClass + "' object cannot be cast to requested superclass '" + superclass + "'", e);
		}
		
		return extImpl;
	}
}
