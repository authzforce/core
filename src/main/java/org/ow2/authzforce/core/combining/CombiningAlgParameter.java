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
package com.sun.xacml.combine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.Decidable;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.combining.CombinerParameterEvaluator;

/**
 * Represents a set of CombinerParameters to a combining algorithm that may or may not be associated
 * with a policy/rule
 * 
 * @since 2.0
 * @author Seth Proctor
 * @param <T>
 *            Type of combined element (Policy, Rule...) with which the CombinerParameters are
 *            associated
 */
public class CombinerElement<T extends Decidable>
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
	 *            a (possibly empty) non-null <code>List</code> of
	 *            <code>CombinerParameter<code>s provided for general
	 *                   use
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default
	 *            XPath version specified in {@link DefaultsType} element; null if none specified
	 * @param expFactory
	 *            attribute value factory
	 * @throws ParsingException
	 *             if error parsing CombinerParameters
	 */
	public CombinerElement(T element, List<oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter> jaxbCombinerParameters, Expression.Factory expFactory, XPathCompiler xPathCompiler) throws ParsingException
	{
		this.element = element;
		if (jaxbCombinerParameters == null)
		{
			this.parameters = Collections.EMPTY_LIST;
		} else
		{
			final List<CombinerParameterEvaluator> modifiableParamList = new ArrayList<>();
			int paramIndex = 0;
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter jaxbCombinerParam : jaxbCombinerParameters)
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
	 * Returns the combined element. If null, it means, this CombinerElement (i.e. all its
	 * CombinerParameters) is not associated with a particular rule
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
