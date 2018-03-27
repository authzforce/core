/**
 * Copyright 2012-2018 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.ow2.authzforce.core.pdp.api.AttributeSource;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.ConstantPrimitiveAttributeValueExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue.StringParseableValueFactory;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.s9api.XPathCompiler;

/**
 * Immutable <code>AttributeValueFactoryRegistry</code>.
 *
 * 
 * @version $Id: $
 */
public final class ImmutableAttributeValueFactoryRegistry extends BasePdpExtensionRegistry<AttributeValueFactory<?>> implements AttributeValueFactoryRegistry
{
	private static final IllegalArgumentException ILLEGAL_DATATYPE_ID_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined datatype ID");

	/**
	 * <p>
	 * createValue
	 * </p>
	 *
	 * @param attValFactory
	 *            a {@link org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory} object.
	 * @param content
	 *            attribute value's mixed content.
	 * @param otherAttributes
	 *            attribute value's optional XML attributes.
	 * @param xPathCompiler
	 *            a {@link net.sf.saxon.s9api.XPathCompiler} object.
	 * @return a V object.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	private static <V extends AttributeValue> V newAttributeValue(final AttributeValueFactory<V> attValFactory, final List<Serializable> content, final Map<QName, String> otherAttributes,
	        final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		assert attValFactory != null;
		final V attrVal;
		try
		{
			attrVal = attValFactory.getInstance(content, otherAttributes, xPathCompiler);
		} catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid Attribute value for datatype '" + attValFactory.getDatatype() + "'", e);
		}

		return attrVal;
	}

	private static <V extends AttributeValue> ConstantExpression<V> newExpression(final AttributeValueFactory<V> attValFactory, final List<Serializable> content,
	        final Map<QName, String> otherAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		assert attValFactory != null;
		final V rawValue = newAttributeValue(attValFactory, content, otherAttributes, xPathCompiler);
		return new ConstantPrimitiveAttributeValueExpression<>(attValFactory.getDatatype(), rawValue);
	}

	/**
	 * Creates instance of immutable attribute bag from raw values, using {@link Bags#newAttributeBag(Datatype, Collection)} and {@code attributeValueFactory.getDatatype()} as datatype argument.
	 * 
	 * @param attributeValueFactory
	 *            factory in charge of create attribute values in the bag
	 * 
	 * @param rawValues
	 *            raw values to be parsed by {@code attributeValueFactory} to create {@link AttributeValue}s
	 * 
	 * @return attribute bag
	 * @throws IllegalArgumentException
	 *             if {@code attributeValueFactory == null } or {@code rawValues} has at least one element which is null:
	 *             {@code rawValues != null && !rawValues.isEmpty() && rawValues.iterator().next() == null}
	 */
	private static <AV extends AttributeValue> AttributeBag<AV> newAttributeBag(final StringParseableValueFactory<AV> attributeValueFactory, final Collection<? extends Serializable> rawValues,
	        final AttributeSource attValSrc) throws IllegalArgumentException
	{
		assert attributeValueFactory != null;

		final Datatype<AV> elementDatatype = attributeValueFactory.getDatatype();

		if (rawValues == null || rawValues.isEmpty())
		{
			return Bags.emptyAttributeBag(elementDatatype, null, attValSrc);
		}

		return Bags.newAttributeBag(elementDatatype, rawValues.stream().map(rawValue -> attributeValueFactory.getInstance(rawValue)).collect(Collectors.toList()), attValSrc);
	}

	private final Map<Class<? extends Serializable>, StringParseableValueFactory<?>> inputClassToAttValFactory;
	private final Set<Entry<Class<? extends Serializable>, StringParseableValueFactory<?>>> nonFinalInputClassToAttValFactory;

	/**
	 * <p>
	 * Constructor for BaseDatatypeFactoryRegistry.
	 * </p>
	 *
	 * @param attributeValueFactories
	 *            attribute value factories
	 */
	public ImmutableAttributeValueFactoryRegistry(final Collection<? extends AttributeValueFactory<?>> attributeValueFactories)
	{
		super(AttributeValueFactory.class,
		        HashCollections.newImmutableSet(Preconditions.checkNotNull(attributeValueFactories, "Input attribute datatype factories undefined (attributeValueFactories == null)")));

		final Map<Class<? extends Serializable>, StringParseableValueFactory<?>> mutableJavaClassToAttValFactory = HashCollections.newUpdatableMap();
		attributeValueFactories.forEach(factory -> {
			if (factory instanceof StringParseableValueFactory)
			{
				final StringParseableValueFactory<?> simpleValueFactory = (StringParseableValueFactory<?>) factory;
				simpleValueFactory.getSupportedInputTypes().forEach(inputType -> mutableJavaClassToAttValFactory.putIfAbsent(inputType, simpleValueFactory));
			}
		});

		inputClassToAttValFactory = HashCollections.newImmutableMap(mutableJavaClassToAttValFactory);
		/*
		 * Using inputClassToAttValFactory.get(instanceClass) to get the corresponding factory is faster that doing many instanceOf checks but only works for equal match. For non-final classes, we
		 * still have to do the instanceOf check because the instance class might not be equal (i.e same class) but a subclass. So we gather the non-final classes for which instanceOf check is
		 * necessary iff no equal match.
		 */
		final Set<Entry<Class<? extends Serializable>, StringParseableValueFactory<?>>> mutableSet = inputClassToAttValFactory.entrySet().stream()
		        .filter(e -> !Modifier.isFinal(e.getKey().getModifiers())).collect(Collectors.toSet());// HashCollections.newUpdatableSet(JAVA_TYPE_TO_ATT_VALUE_FACTORY.size());
		nonFinalInputClassToAttValFactory = ImmutableSet.copyOf(mutableSet);
	}

	/** {@inheritDoc} */
	@Override
	public ConstantExpression<? extends AttributeValue> newExpression(final String datatypeId, final List<Serializable> content, final Map<QName, String> otherAttributes,
	        final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		if (datatypeId == null)
		{
			throw ILLEGAL_DATATYPE_ID_ARGUMENT_EXCEPTION;
		}

		final AttributeValueFactory<?> datatypeFactory = getExtension(datatypeId);
		if (datatypeFactory == null)
		{
			throw new IllegalArgumentException("Unsupported datatype ID (no supporting attribute value factory/parser): " + datatypeId);
		}

		return newExpression(datatypeFactory, content, otherAttributes, xPathCompiler);
	}

	private StringParseableValueFactory<?> getCompatibleFactory(final Class<? extends Serializable> rawValueClass)
	{
		final StringParseableValueFactory<?> attValFactoryFromMap = inputClassToAttValFactory.get(rawValueClass);
		if (attValFactoryFromMap == null)
		{
			/*
			 * This may look like the collection is fully filtered before findfirst() is called but it is not the case. "All intermediate operations e.g. filter(), map() etc are lazy and they are only
			 * executed when a terminal operation like findFirst() or forEach() is called.
			 * 
			 * This also means, a lot of opportunity for optimization depending upon the size of the original list." (Quote from:
			 * http://javarevisited.blogspot.fr/2016/03/how-to-find-first-element-of-stream-in.html)
			 */
			final Optional<Entry<Class<? extends Serializable>, StringParseableValueFactory<?>>> optionalResult = nonFinalInputClassToAttValFactory.stream()
			        .filter(e -> e.getKey().isAssignableFrom(rawValueClass)).findFirst();
			if (optionalResult.isPresent())
			{
				return optionalResult.get().getValue();
			}

			throw new UnsupportedOperationException("Unsupported input value type: '" + rawValueClass + "' (no suitable XACML datatype factory found)");
		}

		return attValFactoryFromMap;
	}

	@Override
	public AttributeValue newAttributeValue(final Serializable rawValue) throws IllegalArgumentException, UnsupportedOperationException
	{
		Preconditions.checkArgument(rawValue != null, "Null input value");
		final StringParseableValueFactory<?> factory = getCompatibleFactory(rawValue.getClass());
		if (factory == null)
		{
			throw new UnsupportedOperationException("Unsupported input value type: '" + rawValue.getClass() + "' (no suitable XACML datatype factory found)");
		}
		return factory.getInstance(rawValue);
	}

	@Override
	public AttributeBag<?> newAttributeBag(final Collection<? extends Serializable> rawVals, AttributeSource attributeValueSource) throws UnsupportedOperationException, IllegalArgumentException
	{
		Preconditions.checkArgument(rawVals != null && !rawVals.isEmpty(), "Null/empty list of input values");
		final Serializable rawVal0 = rawVals.iterator().next();
		Preconditions.checkArgument(rawVal0 != null, "One of the input values (#0) is null");
		final StringParseableValueFactory<?> factory = getCompatibleFactory(rawVal0.getClass());
		if (factory == null)
		{
			throw new UnsupportedOperationException("Unsupported input value type: '" + rawVal0.getClass() + "' (no suitable XACML datatype factory found)");
		}

		return newAttributeBag(factory, rawVals, attributeValueSource);
	}

	@Override
	public AttributeBag<?> newAttributeBag(final Collection<? extends Serializable> rawVals) throws UnsupportedOperationException, IllegalArgumentException
	{
		return newAttributeBag(rawVals, AttributeSources.REQUEST);
	}

}
