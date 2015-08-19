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
 *  
 */

package com.sun.xacml.combine;

import java.util.List;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;

/**
 * The base type for all combining algorithms. It provides one method that must be implemented. In
 * combining policies, obligations and advice must be handled correctly. Specifically, no
 * obligation/advice may be included in the <code>Result</code> that doesn't match the permit/deny
 * decision being returned. So, if INDETERMINATE or NOT_APPLICABLE is the returned decision, no
 * obligations/advice may be included in the result. If the decision of the combining algorithm is
 * PERMIT or DENY, then obligations/advice with a matching fulfillOn/AppliesTo effect are also
 * included in the result.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @param <T>
 *            type of Decidable element (Policy, Rule...)
 */
public abstract class CombiningAlgorithm<T extends Decidable> implements PdpExtension
{
	private static final String LEGACY_ALG_WARNING = "{} is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	// the TYPE_URI for the algorithm
	private final String id;

	private final String toString;

	protected final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	private final Class<T> combinedElementType;

	/**
	 * Constructor that takes the algorithm's identifiers.
	 * 
	 * @param TYPE_URI
	 *            the algorithm's TYPE_URI WARNING: java.net.URI cannot be used here for XACML
	 *            category and ID, because not equivalent to XML schema anyURI type. Spaces are
	 *            allowed in XSD anyURI [1], not in java.net.URI for example. That's why we use
	 *            String instead. </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedType
	 *            combined element type
	 */
	public CombiningAlgorithm(String identifier, Class<T> combinedType)
	{
		this(identifier, false, combinedType);
	}

	/**
	 * Constructor that takes the algorithm's TYPE_URI.
	 * 
	 * @param TYPE_URI
	 *            the algorithm's TYPE_URI
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param isLegacy
	 *            whether this algorithm is legacy. true, implementations not willing to support
	 *            legacy algorithms can throw {@link #unsupportedLegacyAlgorithmException} that uses
	 *            the message format below to produce the exception message:
	 *            {@value #LEGACY_ALG_WARNING}. Else {@link #unsupportedLegacyAlgorithmException} is
	 *            null.
	 * @param combinedType
	 *            combined element type
	 */
	public CombiningAlgorithm(String identifier, boolean isLegacy, Class<T> combinedType)
	{
		this.combinedElementType = combinedType;
		this.id = identifier;
		this.toString = "CombiningAlgorithm[" + identifier + "]";
		this.unsupportedLegacyAlgorithmException = isLegacy ? new UnsupportedOperationException(String.format(LEGACY_ALG_WARNING, this)) : null;
	}

	@Override
	public final String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return this.toString;
	}

	/**
	 * Get to know whether this is a policy/policySet or rule-combining algorithm
	 * 
	 * @return the combinedElementType
	 */
	public final Class<T> getCombinedElementType()
	{
		return combinedElementType;
	}

	/**
	 * Combines the child elements (rules for rule combining, policies for policy combining) based
	 * on the context to produce some unified result. To be implemented by algorithm
	 * implementations.
	 * 
	 * @param context
	 *            the representation of the request
	 * @param params
	 *            list of CombinerParameters that may be associated with a particular child element
	 * @param combinedElements
	 *            combined child elements
	 * 
	 * @return a combined result based on the combining logic
	 */
	public abstract DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends T>> params, List<? extends T> combinedElements);
}
