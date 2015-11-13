package org.ow2.authzforce.core.combining;

import java.util.List;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.PdpExtension;

/**
 * The base type for all combining algorithms. In combining policies, obligations and advice must be handled correctly. Specifically, no obligation/advice may
 * be included in the <code>Result</code> that doesn't match the permit/deny decision being returned. So, if INDETERMINATE or NOT_APPLICABLE is the returned
 * decision, no obligations/advice may be included in the result. If the decision of the combining algorithm is PERMIT or DENY, then obligations/advice with a
 * matching fulfillOn/AppliesTo effect are also included in the result.
 * 
 * @param <T>
 *            type of combined element (Policy, Rule...)
 */
public abstract class CombiningAlg<T extends Decidable> implements PdpExtension
{
	private static final String LEGACY_ALG_WARNING = "%s is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	/**
	 * Combining algorithm evaluator
	 *
	 */
	public interface Evaluator
	{
		/**
		 * Runs the combining algorithm in a specific evaluation context
		 * 
		 * @param context
		 *            the request evaluation context
		 * 
		 * @return combined result
		 */
		DecisionResult eval(EvaluationContext context);
	}

	// the identifier for the algorithm
	private final String id;

	private transient volatile String toString = null;

	protected final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	private final Class<T> combinedElementType;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the algorithm's id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in
	 *            XSD anyURI [1], not in java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param isLegacy
	 *            true iff the algorithm to instantiate is legacy ("legacy" as defined in XACML 3.0 or any combining algorithm replaced with new one).
	 * @param combinedType
	 *            combined element type
	 */
	public CombiningAlg(String id, boolean isLegacy, Class<T> combinedType)
	{
		this.combinedElementType = combinedType;
		this.id = id;
		this.toString = "CombiningAlgorithm[" + id + "]";
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
	 * Creates instance of algorithm. To be implemented by algorithm implementations.
	 * 
	 * @param params
	 *            list of combining algorithm parameters that may be associated with a particular child element
	 * @param combinedElements
	 *            combined child elements
	 * 
	 * @return an instance of algorithm evaluator
	 * @throws UnsupportedOperationException
	 *             if this is a legacy algorithm and legacy support is disabled
	 * @throws IllegalArgumentException
	 *             if {@code params} are invalid for this algorithm
	 */
	public abstract Evaluator getInstance(List<CombiningAlgParameter<? extends T>> params, List<? extends T> combinedElements)
			throws UnsupportedOperationException, IllegalArgumentException;
}
