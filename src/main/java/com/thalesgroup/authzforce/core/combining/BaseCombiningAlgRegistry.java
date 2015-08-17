package com.thalesgroup.authzforce.core.combining;

import java.util.Map;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.eval.Decidable;

/**
 * This is a com.thalesgroup.authzforce.core.test.basic implementation of <code>CombiningAlgRegistry</code>.
 */
public class BaseCombiningAlgRegistry extends BasePdpExtensionRegistry<CombiningAlgorithm<? extends Decidable>> implements CombiningAlgRegistry
{
	protected BaseCombiningAlgRegistry(Map<String, CombiningAlgorithm<? extends Decidable>> algorithmsById)
	{
		super(algorithmsById);
	}

	/**
	 * @see BasePdpExtensionRegistry#BasePdpExtensionRegistry(BasePdpExtensionRegistry)
	 */
	public BaseCombiningAlgRegistry(BasePdpExtensionRegistry<CombiningAlgorithm<? extends Decidable>> baseRegistry)
	{
		super(baseRegistry);
	}

	/**
	 * Default constructor.
	 */
	public BaseCombiningAlgRegistry()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Decidable> CombiningAlgorithm<T> getAlgorithm(String algId, Class<T> combinedEltType) throws UnknownIdentifierException
	{
		final CombiningAlgorithm<? extends Decidable> alg = this.getExtension(algId);
		if (alg.getCombinedElementType().isAssignableFrom(combinedEltType))
		{
			return (CombiningAlgorithm<T>) alg;
		}

		// wrong type of alg
		throw new IllegalArgumentException("Registered combining algorithm for ID=" + algId + " combines instances of type '" + alg.getCombinedElementType() + "' which is not compatible (not same or supertype) with requested type of combined elements : " + combinedEltType);
	}

}
