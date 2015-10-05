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
package com.thalesgroup.authzforce.core.eval;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder;
import com.thalesgroup.authzforce.core.attr.XPathAttributeValue;
import com.thalesgroup.authzforce.xacml.schema.XPATHVersion;

/**
 * Super interface of any kinds of expression in a policy that the PDP evaluation engine may
 * evaluate in a given authorization request context:
 * <ul>
 * <li>AttributeValue</li>
 * <li>Apply</li>
 * <li>AttributeSelector</li>
 * <li>VariableReference</li>
 * <li>AttributeDesignator</li>
 * <li>Function</li>
 * </ul>
 * 
 * @param <V>
 *            type of result from evaluating the expression
 */
public interface Expression<V extends Expression.Value<?, V>>
{
	/**
	 * Expression evaluation result value. A Value may be itself used as an input {@link Expression}
	 * of a function for instance, therefore itself extends {@link Expression}. Not used for
	 * returning evaluation errors, i.e. "Indeterminate" results, in which case,
	 * {@link IndeterminateEvaluationException} should be used instead.
	 * 
	 * @param <AV>
	 *            concrete type subclass of:
	 *            <ul>
	 *            <li>the value itself if single-valued,</li>
	 *            <li>every element value if multi-valued (collection of values).</li>
	 *            </ul>
	 * @param <V>
	 *            concrete type subclass, same as V iff single-valued type, else multi-valued type
	 */
	public interface Value<AV extends AttributeValue<AV>, V extends Value<AV, V>> extends Expression<V>
	{
		/**
		 * Returns the (first) attribute value or null if no value found
		 * 
		 * @return the first attribute value if this is multi-valed (bag), the one and only
		 *         attribute value if this is single-valued (not a bag); or null in both cases if no
		 *         value
		 */
		AV one();

		/**
		 * Returns attribute value(s) in the result
		 * 
		 * @return all attribute value(s); may be empty if no value, but never null. It is the
		 *         responsability of the implementation to ensure empty (zero-length) array is
		 *         returned instead of null, according to "Effective Java (2nd Edition)" by J.
		 *         Bloch, "Item 43: Return empty arrays or collections, not nulls"
		 *         <p>
		 *         Although it is usally recommended to use Collection instead of array in API, we
		 *         use here array as return type to allow for type-safe generic cast, e.g. see
		 *         FirstOrderFunctionCall#evalBagArg(). In general, if we are expecting a bag of
		 *         type V (extends AttributeValue<V>) from a given input bag (of type originally
		 *         unknown), we want to be able to cast the input bag values simply and safely. In
		 *         this case, when using an array for the bag values returned by this method, we can
		 *         use the class of the array of V, e.g. some variable {@code Class<V[]> vClass} to
		 *         cast the input bag values to what we want as bag type:
		 *         {@code vClass.cast(inputBag.all())}. This would be more difficult with Collection
		 *         (requires to iterate over all collection items for type-safety). Indeed,
		 *         {@code vClass} is easy to instantiate for array {@code V[]} (e.g. for V =
		 *         StringAttributeValue, {@code vClass = StringAttributeValue[].class}, but not for
		 *         {@code Collection<V>} ({@code vClass = Collection<V>.class} is not valid for
		 *         instance).
		 *         </p>
		 */
		public AV[] all();

	}

	/**
	 * Expression evaluation return type
	 * 
	 * @param <V>
	 *            Java value type, which is one of the following:
	 */
	public static class Datatype<V extends Value<?, ?>>
	{

		private static final IllegalArgumentException NULL_VALUE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined value (datatype implementation) class arg");
		private static final IllegalArgumentException NULL_VALUE_TYPE_URI_EXCEPTION = new IllegalArgumentException("Undefined datatype ID arg");

		// private final boolean isBag;
		private final String id;
		private final Class<V> valueClass;
		protected final Datatype<?> subTypeParam;

		// cached method results
		private String toString = null;
		private int hashCode = 0;

		/**
		 * Instantiates generic datatype, i.e. taking a datatype parameter, like Java Generics, but
		 * more like Java Collection since there is only one type parameter in this case.
		 * 
		 * @param valueClass
		 *            Java (implementation) class of values of this datatype
		 * @param id
		 *            datatype ID
		 * @param subType
		 *            datatype of sub-elements
		 * @throws IllegalArgumentException
		 *             if {@code valueClass == null || id == null }
		 */
		protected Datatype(Class<V> valueClass, String id, Datatype<?> typeParameter) throws IllegalArgumentException
		{
			if (valueClass == null)
			{
				throw NULL_VALUE_CLASS_EXCEPTION;
			}

			if (id == null)
			{
				throw NULL_VALUE_TYPE_URI_EXCEPTION;
			}

			this.valueClass = valueClass;
			this.id = id;
			this.subTypeParam = typeParameter;
		}

		/**
		 * Instantiates primitive datatype
		 * 
		 * @param valueClass
		 *            class implementing this primitive datatype
		 * 
		 * @param id
		 *            datatype ID (e.g. XACML datatype URI) which identifies this primitive datatype
		 * @throws IllegalArgumentException
		 *             if {@code valueClass == null || id == null }
		 */
		public Datatype(Class<V> valueClass, String id) throws IllegalArgumentException
		{
			this(valueClass, id, null);
		}

		/**
		 * Get value class, which is the Java (implementation) class of all instances of this
		 * datatype
		 * 
		 * @return value class
		 */
		public Class<V> getValueClass()
		{
			return valueClass;
		}

		/**
		 * Get ID (URI) of this datatype
		 * 
		 * @return datatype ID
		 */
		public String getId()
		{
			return this.id;
		}

		/**
		 * Return true iff bag datatype
		 * 
		 * @return true iff it is a bag datatype
		 */
		public boolean isBag()
		{
			return subTypeParam != null;
		}

		/**
		 * Return datatype of sub-elements for this datatype, e.g. the bag element datatype
		 * (datatype of every element in a bag of this datatype); null if this is a primitive type
		 * (no sub-elements)
		 * 
		 * @return datatype parameter, null for non-bag/primitive values
		 */
		public Datatype<?> getTypeParameter()
		{
			return subTypeParam;
		}

		/**
		 * Casts a value to the class or interface represented by this datatype.
		 * 
		 * @param val
		 *            value to be cast
		 * @return the value after casting, or null if {@code val} is null
		 * @throws ClassCastException
		 *             if the value is not null and is not assignable to the type V.
		 */
		public V cast(Value<?, ?> val) throws ClassCastException
		{
			return this.valueClass.cast(val);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = subTypeParam == null ? id : id + "<" + subTypeParam + ">";
			}

			return toString;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				// there should be one-to-one mapping between valueClass and id, so hashing
				// only one of these two is necessary
				hashCode = Objects.hash(valueClass, subTypeParam);
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}

			final Datatype<?> other = (Datatype<?>) obj;
			if (!valueClass.equals(other.valueClass))
			{
				return false;
			}
			// there should be one-to-one mapping between valueClass and id, so hashing
			// only one of these two is necessary
			// if (!this.id.equals(other.id))
			// {
			// return false;
			// }

			if (this.subTypeParam == null)
			{
				if (other.subTypeParam == null)
				{
					return true;
				}

				return false;
			}

			// this.elementDatatype != null
			if (other.subTypeParam == null)
			{
				return false;
			}

			return this.subTypeParam.equals(other.subTypeParam);
		}
	}

	/**
	 * Gets the expected return type of the expression if evaluated.
	 * 
	 * @return expression evaluation's return type
	 */
	Datatype<V> getReturnType();

	/**
	 * Evaluates the expression using the given context.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation that may be a single value T (e.g. function result,
	 *         AttributeValue, Condition, Match...) or bag of values (e.g. AttributeDesignator,
	 *         AttributeSelector)
	 * @throws IndeterminateEvaluationException
	 *             if evaluation "Indeterminate" (see XACML core specification)
	 */
	V evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Tells whether this expression is actually a static value, i.e. independent from the
	 * evaluation context (e.g. AttributeValue, VariableReference to AttributeValue...). This
	 * enables expression consumers to do optimizations, e.g. functions may pre-compile/pre-evaluate
	 * parts of their inputs knowing some are constant values.
	 * 
	 * @return true iff a static/fixed/constant value
	 */
	boolean isStatic();

	/**
	 * Gets the instance of the Java representation of the XACML-schema-defined Expression
	 * bound/equivalent to this expression
	 * 
	 * @return JAXB element equivalent
	 */
	JAXBElement<? extends ExpressionType> getJAXBElement();

	/**
	 * Expression factory for parsing XACML {@link ExpressionType}s: AttributeDesignator,
	 * AttributeSelector, Apply, etc.
	 * <p>
	 * Extends {@link Closeable} because it may use an {@link CloseableAttributeFinder} to resolve
	 * AttributeDesignators for attributes not provided in the request; and that attribute finder
	 * needs to be closed by calling {@link #close()} (in order to call
	 * {@link CloseableAttributeFinder#close()}) when it is no longer needed.
	 */
	public static interface Factory extends Closeable
	{

		/**
		 * Parses an XACML Expression into internal model of expression (evaluable).
		 * 
		 * @param expr
		 *            the JAXB ExpressionType derived from XACML model
		 * @param xPathCompiler
		 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default
		 *            XPath version specified in {@link DefaultsType} element; null if none
		 *            specified
		 * @param longestVarRefChain
		 *            Longest chain of VariableReference references in the VariableDefinition's
		 *            expression that is <code>expr</code> or contains <code>expr</code>, or null if
		 *            <code>expr</code> is not in a VariableDefinition. A VariableReference
		 *            reference chain is a list of VariableIds, such that V1-> V2 ->... -> Vn ->
		 *            <code>expr</code> , where "V1 -> V2" means: the expression in
		 *            VariableDefinition of V1 has a VariableReference to V2. This is used to detect
		 *            exceeding depth of VariableReference reference in VariableDefinitions'
		 *            expressions. Again, <code>longestVarRefChain</code> may be null, if this
		 *            expression is not used in a VariableDefinition.
		 * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid
		 *         Expression
		 * @throws ParsingException
		 *             error parsing instance of ExpressionType
		 */
		Expression<?> getInstance(ExpressionType expr, XPathCompiler xPathCompiler, List<String> longestVarRefChain) throws ParsingException;

		/**
		 * Parse/create an attribute value from XACML-schema-derived JAXB model
		 * 
		 * @param jaxbAttrVal
		 *            XACML-schema-derived JAXB AttributeValue
		 * @param xPathCompiler
		 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default
		 *            XPath version specified in {@link DefaultsType} element; null if none
		 *            specified
		 * @return attribute value
		 * @throws ParsingException
		 *             if value cannot be parsed into the value's defined datatype
		 */
		AttributeValue<?> createAttributeValue(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws ParsingException;

		/**
		 * Add VariableDefinition to be managed
		 * 
		 * @param varDef
		 *            VariableDefinition
		 * @param xPathCompiler
		 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default
		 *            XPath version specified in {@link DefaultsType} element.
		 * @return The previous VariableReference if VariableId already used
		 * @throws ParsingException
		 *             error parsing expression in <code>var</code>
		 */
		VariableReference<?> addVariable(oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition varDef, XPathCompiler xPathCompiler) throws ParsingException;

		/**
		 * Removes the VariableReference(Definition) from the manager
		 * 
		 * @param varId
		 * @return the VariableReference previously identified by <code>varId</code>, or null if
		 *         there was no such variable.
		 */
		VariableReference<?> removeVariable(String varId);

		/**
		 * Gets a non-generic function instance
		 * 
		 * @param functionId
		 *            function ID (XACML URI)
		 * @return function instance; or null if no such function with ID {@code functionId}
		 * 
		 */
		Function<?> getFunction(String functionId);

		/**
		 * Gets a function instance (generic or non-generic).
		 * 
		 * @param functionId
		 *            function ID (XACML URI)
		 * @param subFunctionReturnType
		 *            optional sub-function's return type required only if a generic higher-order
		 *            function is expected as the result, of which the sub-function is expected to
		 *            be the first parameter; otherwise null (for first-order function). A generic
		 *            higher-order function is a function whose return type depends on the
		 *            sub-function ('s return type).
		 * @return function instance; or null if no such function with ID {@code functionId}, or if
		 *         non-null {@code subFunctionReturnTypeId} specified and no higher-order function
		 *         compatible with sub-function's return type {@code subFunctionReturnTypeId}
		 * @throws UnknownIdentifierException
		 *             if datatype {@code subFunctionReturnType} is not supported
		 * 
		 */
		Function<?> getFunction(String functionId, Datatype<?> subFunctionReturnType) throws UnknownIdentifierException;
	}

	/**
	 * Utility class that provide functions to help evaluate Expressions
	 * 
	 */
	public static class Utils
	{
		/**
		 * Saxon configuration file for Attributes/Content XML parsing (into XDM data model) and
		 * AttributeSelector's XPath evaluation
		 */
		public static final String SAXON_CONFIGURATION_PATH = "classpath:saxon.xml";
		/**
		 * SAXON XML/XPath Processor
		 */
		public static final Processor SAXON_PROCESSOR;
		static
		{
			final ResourceLoader resLoader = new DefaultResourceLoader();
			final Resource saxonConfRes = resLoader.getResource(SAXON_CONFIGURATION_PATH);
			if (!saxonConfRes.exists())
			{
				throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_PATH);
			}

			final File saxonConfFile;
			try
			{
				saxonConfFile = saxonConfRes.getFile();
			} catch (IOException e)
			{
				throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_PATH, e);
			}

			try
			{
				SAXON_PROCESSOR = new Processor(new StreamSource(saxonConfFile));
			} catch (SaxonApiException e)
			{
				throw new RuntimeException("Error loading Saxon processor from configuration file at this location: " + SAXON_CONFIGURATION_PATH, e);
			}
		}

		private static XPathCompiler newXPathCompiler(XPATHVersion xpathVersion)
		{
			final XPathCompiler xpathCompiler = Utils.SAXON_PROCESSOR.newXPathCompiler();
			final String versionString;
			switch (xpathVersion)
			{
				case V1_0:
					versionString = "1.0";
					break;
				case V2_0:
					versionString = "2.0";
					break;
				default:
					throw new UnsupportedOperationException("Unsupported XPath version: " + xpathVersion + ". Versions supported: " + Arrays.asList(XPATHVersion.values()));

			}

			xpathCompiler.setLanguageVersion(versionString);
			xpathCompiler.setSchemaAware(false);

			/*
			 * TODO: we could enable caching of XPATH compiled queries but only once we have
			 * implemented a way to clear the cache periodically, otherwise it grows indefinitely.
			 */
			// xpathCompiler.setCaching(true);
			return xpathCompiler;
		}

		/**
		 * XPath compilers by XPath version, for single evaluation of a given XPath with
		 * {@link XPathCompiler#evaluateSingle(String, XdmItem)}. For repeated evaluation of the
		 * same XPath, use {@link XPathEvaluator} instead. What we receive in XACML Request is the
		 * version URI so we need this map to map the URI to the XPath compiler
		 */
		public static final Map<String, XPathCompiler> XPATH_COMPILERS_BY_VERSION = new HashMap<>();
		static
		{
			// XPATH 1.0 compiler
			XPATH_COMPILERS_BY_VERSION.put(XPATHVersion.V1_0.getURI(), newXPathCompiler(XPATHVersion.V1_0));
			// XPATH 2.0 compiler
			XPATH_COMPILERS_BY_VERSION.put(XPATHVersion.V2_0.getURI(), newXPathCompiler(XPATHVersion.V2_0));
		}

		/**
		 * Wrapper around XPathExecutable that provides the original XPath expression from which the
		 * XPathExecutable was compiled, via toString() method. To be used for XPath-based
		 * Expression evaluations, e.g. {@link AttributeSelector}, {@link XPathAttributeValue}, etc.
		 */
		public static class XPathEvaluator
		{
			private final XPathExecutable exec;
			private final String expr;

			/**
			 * Creates instanace
			 * 
			 * @param path
			 *            XPath executable
			 * @param xPathCompiler
			 *            XPath compiler
			 * @throws IllegalArgumentException
			 *             in case of invalid XPath
			 */
			public XPathEvaluator(String path, XPathCompiler xPathCompiler) throws IllegalArgumentException
			{
				try
				{
					this.exec = xPathCompiler.compile(path);
				} catch (SaxonApiException e)
				{
					throw new IllegalArgumentException(this + ": Invalid XPath", e);
				}

				this.expr = path;
			}

			@Override
			public String toString()
			{
				return expr;
			}

			/**
			 * @return An XPathSelector. The returned XPathSelector can be used to set up the
			 *         dynamic context, and then to evaluate the expression.
			 * @see XPathExecutable#load()
			 */
			public XPathSelector load()
			{
				return exec.load();
			}
		}

		private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
		private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException("No value returned by arg evaluation in the current context", Status.STATUS_PROCESSING_ERROR);

		/**
		 * Evaluate single-valued (primitive) argument expression
		 * 
		 * @param arg
		 *            argument expression
		 * @param context
		 *            context in which argument expression is evaluated
		 * @param returnType
		 *            type of returned attribute value
		 * @return result of evaluation
		 * @throws IndeterminateEvaluationException
		 *             if no value returned from evaluation, or <code>returnType</code> is not a
		 *             supertype of the result value datatype
		 */
		public static <AV extends AttributeValue<?>> AV evalSingle(Expression<?> arg, EvaluationContext context, Class<AV> returnType) throws IndeterminateEvaluationException
		{
			final AttributeValue<?> val = arg.evaluate(context).one();
			LOGGER.debug("evalSingle( arg = <{}>, <context>, expectedType = <{}> ) -> <{}>", arg, returnType, val);
			if (val == null)
			{
				throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
			}

			try
			{
				return returnType.cast(val);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Invalid expresion evaluation result type: " + val.getClass().getName() + ". Expected: " + returnType.getName(), Status.STATUS_PROCESSING_ERROR, e);
			}
		}
	}

}
