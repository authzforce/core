package com.thalesgroup.authzforce.core.combining;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.PdpExtensionRegistry;
import com.thalesgroup.authzforce.core.eval.Decidable;

/**
 * Provides a registry mechanism for adding and retrieving combining algorithms.
 */
public interface CombiningAlgRegistry extends PdpExtensionRegistry<CombiningAlgorithm<? extends Decidable>>
{

	/**
	 * Tries to return the correct combinging algorithm based on the given algorithm ID.
	 * 
	 * @param algId
	 *            the identifier by which the algorithm is known
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedElementType
	 *           type of combined element
	 * 
	 * @return a combining algorithm
	 * 
	 * @throws UnknownIdentifierException
	 *             algId is unknown
	 */
	public abstract <T extends Decidable> CombiningAlgorithm<T> getAlgorithm(String algId, Class<T> combinedElementType) throws UnknownIdentifierException;

}
