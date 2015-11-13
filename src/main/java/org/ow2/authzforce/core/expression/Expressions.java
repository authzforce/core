package org.ow2.authzforce.core.expression;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;
import org.ow2.authzforce.core.value.XPathValue;
import org.ow2.authzforce.xacml.identifiers.XPATHVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * This class consists exclusively of constants and static methods to operate on {@link Expression}s.
 * 
 */
public final class Expressions
{
	private Expressions()
	{
	}

	/**
	 * Saxon configuration file for Attributes/Content XML parsing (into XDM data model) and AttributeSelector's XPath evaluation
	 */
	public static final String SAXON_CONFIGURATION_PATH = "classpath:saxon.xml";
	/**
	 * SAXON XML/XPath Processor
	 */
	public static final Processor SAXON_PROCESSOR;
	static
	{
		final URL saxonConfURL;
		try
		{
			saxonConfURL = ResourceUtils.getURL(SAXON_CONFIGURATION_PATH);
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_PATH, e);
		}

		try
		{
			SAXON_PROCESSOR = new Processor(new StreamSource(saxonConfURL.toString()));
		} catch (SaxonApiException e)
		{
			throw new RuntimeException("Error loading Saxon processor from configuration file at this location: " + SAXON_CONFIGURATION_PATH, e);
		}
	}

	private static XPathCompiler newXPathCompiler(XPATHVersion xpathVersion)
	{
		final XPathCompiler xpathCompiler = Expressions.SAXON_PROCESSOR.newXPathCompiler();
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
			throw new UnsupportedOperationException("Unsupported XPath version: " + xpathVersion + ". Versions supported: "
					+ Arrays.asList(XPATHVersion.values()));

		}

		xpathCompiler.setLanguageVersion(versionString);
		xpathCompiler.setSchemaAware(false);

		/*
		 * TODO: we could enable caching of XPATH compiled queries but only once we have implemented a way to clear the cache periodically, otherwise it grows
		 * indefinitely.
		 */
		// xpathCompiler.setCaching(true);
		return xpathCompiler;
	}

	/**
	 * XPath compilers by XPath version, for single evaluation of a given XPath with {@link XPathCompiler#evaluateSingle(String, XdmItem)}. For repeated
	 * evaluation of the same XPath, use {@link XPathEvaluator} instead. What we receive in XACML Request is the version URI so we need this map to map the URI
	 * to the XPath compiler
	 */
	public static final Map<String, XPathCompiler> XPATH_COMPILERS_BY_VERSION;
	static
	{
		final Map<String, XPathCompiler> mutableMap = new HashMap<>();
		// XPATH 1.0 compiler
		mutableMap.put(XPATHVersion.V1_0.getURI(), newXPathCompiler(XPATHVersion.V1_0));
		// XPATH 2.0 compiler
		mutableMap.put(XPATHVersion.V2_0.getURI(), newXPathCompiler(XPATHVersion.V2_0));
		XPATH_COMPILERS_BY_VERSION = Collections.unmodifiableMap(mutableMap);
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