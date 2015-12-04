/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.XACMLParsers;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;
import org.ow2.authzforce.core.value.XPathValue;
import org.ow2.authzforce.xacml.identifiers.XPATHVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class consists exclusively of constants and static methods to operate on {@link Expression}s.
 * 
 */
public final class Expressions
{
	private Expressions()
	{
	}

	private static XPathCompiler newXPathCompiler(XPATHVersion xpathVersion) throws IllegalArgumentException
	{
		final XPathCompiler xpathCompiler = XACMLParsers.SAXON_PROCESSOR.newXPathCompiler();
		xpathCompiler.setLanguageVersion(xpathVersion.getVersionNumber());
		/*
		 * No need for caching since we are only using this for XPaths in Policy/PolicySet (AttributeSelector and xpathExpression), not in the Request (not
		 * supported)
		 */
		xpathCompiler.setCaching(false);
		xpathCompiler.setSchemaAware(false);
		return xpathCompiler;
	}

	// Default XPath compilers by XPathVersion outside any namespace context
	private static final Map<String, XPathCompiler> XPATH_COMPILERS_BY_VERSION;
	static
	{
		final Map<String, XPathCompiler> mutableMap = new HashMap<>();
		// XPATH 1.0 compiler
		mutableMap.put(XPATHVersion.V1_0.getURI(), newXPathCompiler(XPATHVersion.V1_0));
		// XPATH 2.0 compiler
		mutableMap.put(XPATHVersion.V2_0.getURI(), newXPathCompiler(XPATHVersion.V2_0));
		XPATH_COMPILERS_BY_VERSION = Collections.unmodifiableMap(mutableMap);
	}

	private static final IllegalArgumentException NULL_NAMESPACE_PREFIX_EXCEPTION = new IllegalArgumentException(
			"Invalid XPath compiler input: null namespace prefix in namespace prefix-URI mappings");
	private static final IllegalArgumentException NULL_NAMESPACE_URI_EXCEPTION = new IllegalArgumentException(
			"Invalid XPath compiler input: null namespace URI in namespace prefix-URI mappings");

	/**
	 * Create XPath compiler for given XPath version and namespace context. For single evaluation of a given XPath with
	 * {@link XPathCompiler#evaluateSingle(String, XdmItem)}. For repeated evaluation of the same XPath, use {@link XPathEvaluator} instead. What we have in
	 * XACML Policy/PolicySetDefaults is the version URI so we need this map to map the URI to the XPath compiler
	 * 
	 * @param xpathVersionURI
	 *            XPath version URI, e.g. "http://www.w3.org/TR/1999/REC-xpath-19991116"
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mapping to be part of the static context for XPath expressions compiled using the created XPathCompiler
	 * @return XPath compiler instance
	 * @throws IllegalArgumentException
	 *             if {@code xpathVersionURI} is invalid or unsupported XPath version or one of the namespace prefixes/URIs in {@code namespaceURIsByPrefix} is
	 *             null
	 */
	public static XPathCompiler newXPathCompiler(String xpathVersionURI, Map<String, String> namespaceURIsByPrefix) throws IllegalArgumentException
	{
		if (namespaceURIsByPrefix == null || namespaceURIsByPrefix.isEmpty())
		{
			final XPathCompiler xpathCompiler = XPATH_COMPILERS_BY_VERSION.get(xpathVersionURI);
			if (xpathCompiler == null)
			{
				throw new IllegalArgumentException("Invalid or unsupported XPathVersion: " + xpathVersionURI);
			}

			return xpathCompiler;
		}

		final XPATHVersion xpathVersion = XPATHVersion.fromURI(xpathVersionURI);
		final XPathCompiler xpathCompiler = newXPathCompiler(xpathVersion);
		for (final Entry<String, String> nsPrefixToURI : namespaceURIsByPrefix.entrySet())
		{
			final String prefix = nsPrefixToURI.getKey();
			final String uri = nsPrefixToURI.getValue();
			if (prefix == null)
			{
				throw NULL_NAMESPACE_PREFIX_EXCEPTION;
			}

			if (uri == null)
			{
				throw NULL_NAMESPACE_URI_EXCEPTION;
			}

			xpathCompiler.declareNamespace(prefix, uri);
		}

		return xpathCompiler;
	}

	/**
	 * Wrapper around XPathExecutable that provides the original XPath expression from which the XPathExecutable was compiled, via toString() method. To be used
	 * for XPath-based Expression evaluations, e.g. {@link AttributeSelectorExpression}, {@link XPathValue}, etc.
	 */
	public static final class XPathEvaluator
	{
		private final XPathExecutable exec;
		private final String expr;

		/**
		 * Creates instance
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
		 * @return An XPathSelector. The returned XPathSelector can be used to set up the dynamic context, and then to evaluate the expression.
		 * @see XPathExecutable#load()
		 */
		public XPathSelector load()
		{
			return exec.load();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Expressions.class);
	private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException(
			"No value returned by arg evaluation in the current context", StatusHelper.STATUS_PROCESSING_ERROR);
	private static final IndeterminateEvaluationException NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException(
			"Undefined expected attribute datatype", StatusHelper.STATUS_SYNTAX_ERROR);

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
	 *             if no value returned from evaluation, or <code>returnType</code> is not a supertype of the result value datatype
	 */
	public static <V extends Value> V eval(Expression<?> arg, EvaluationContext context, Datatype<V> returnType) throws IndeterminateEvaluationException
	{
		if (returnType == null)
		{
			throw NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION;
		}

		final Value val = arg.evaluate(context);
		LOGGER.debug("eval( arg = <{}>, <context>, expectedType = <{}> ) -> <{}>", arg, returnType, val);
		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return returnType.cast(val);
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: " + returnType,
					StatusHelper.STATUS_PROCESSING_ERROR, e);
		}
	}

	/**
	 * Evaluate single-valued (primitive) argument expression
	 * 
	 * @param arg
	 *            argument expression
	 * @param context
	 *            context in which argument expression is evaluated
	 * @return result of evaluation
	 * @throws IndeterminateEvaluationException
	 *             if no value returned from evaluation, or <code>returnType</code> is not a supertype of the result value datatype
	 */
	public static AttributeValue evalPrimitive(Expression<?> arg, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final Value val = arg.evaluate(context);
		LOGGER.debug("evalPrimitive( arg = <{}>, <context>) -> <{}>", arg, val);
		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return (AttributeValue) val;
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: any primitive type",
					StatusHelper.STATUS_PROCESSING_ERROR, e);
		}
	}
}