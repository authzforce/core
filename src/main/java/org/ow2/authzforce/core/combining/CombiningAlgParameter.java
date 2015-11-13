package org.ow2.authzforce.core.combining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.expression.ExpressionFactory;

import com.sun.xacml.ParsingException;

/**
 * Represents a set of CombinerParameters to a combining algorithm that may or may not be associated with a policy/rule
 * 
 * @param <T>
 *            Type of combined element (Policy, Rule...) with which the CombinerParameters are associated
 */
public class CombiningAlgParameter<T extends Decidable>
{

	// the element to be combined
	private final T element;

	// the parameters used with this element
	private final List<CombinerParameterEvaluator> parameters;

	/**
	 * Constructor that takes both the element to combine and its associated combiner parameters.
	 * 
	 * @param element
	 *            combined element; null if
	 * 
	 * @param jaxbCombinerParameters
	 *            a (possibly empty) non-null <code>List</code> of <code>CombinerParameter<code>s provided for general
	 *                   use
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null
	 *            if none specified
	 * @param expFactory
	 *            attribute value factory
	 * @throws ParsingException
	 *             if error parsing CombinerParameters
	 */
	public CombiningAlgParameter(T element, List<CombinerParameter> jaxbCombinerParameters, ExpressionFactory expFactory, XPathCompiler xPathCompiler)
			throws ParsingException
	{
		this.element = element;
		if (jaxbCombinerParameters == null)
		{
			this.parameters = Collections.emptyList();
		} else
		{
			final List<CombinerParameterEvaluator> modifiableParamList = new ArrayList<>();
			int paramIndex = 0;
			for (CombinerParameter jaxbCombinerParam : jaxbCombinerParameters)
			{
				try
				{
					final CombinerParameterEvaluator combinerParam = new CombinerParameterEvaluator(jaxbCombinerParam, expFactory, xPathCompiler);
					modifiableParamList.add(combinerParam);
				} catch (ParsingException e)
				{
					throw new ParsingException("Error parsing CombinerParameters/CombinerParameter#" + paramIndex, e);
				}

				paramIndex++;
			}

			this.parameters = Collections.unmodifiableList(modifiableParamList);
		}
	}

	/**
	 * Returns the combined element. If null, it means, this CombinerElement (i.e. all its CombinerParameters) is not associated with a particular rule
	 * 
	 * @return the combined element
	 */
	public T getCombinedElement()
	{
		return element;
	}

	/**
	 * Returns the <code>CombinerParameterEvaluator</code>s associated with this element.
	 * 
	 * @return a <code>List</code> of <code>CombinerParameterEvaluator</code>s
	 */
	public List<CombinerParameterEvaluator> getParameters()
	{
		return parameters;
	}

}
