package com.thalesgroup.authzforce.core.combining;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.combine.OnlyOneApplicablePolicyAlg;
import com.thalesgroup.authzforce.core.eval.Decidable;

/**
 * This registry supports the standard set of algorithms specified in XACML.
 * <p>
 * Note that because this supports only the standard algorithms, this factory does not allow the
 * addition of any other algorithms. If you need a standard factory that is modifiable, you should
 * create a new <code>BaseCombiningAlgRegistry</code> by passing this to
 * {@link BaseCombiningAlgRegistry#BaseCombiningAlgRegistry(com.thalesgroup.authzforce.core.BasePdpExtensionRegistry)}.
 * 
 */
public class StandardCombiningAlgRegistry extends BaseCombiningAlgRegistry
{
	/**
	 * Singleton function registry instance for standard functions
	 */
	public static StandardCombiningAlgRegistry INSTANCE;
	static
	{
		final Set<CombiningAlgorithm<? extends Decidable>> standardExtensions = new HashSet<>();
		// XACML 3.0 algorithms
		// deny-overrides and ordered-deny-overrides
		standardExtensions.addAll(DenyOverridesAlg.SET.getSupportedAlgorithms());
		// permit-overrides and ordered-permit-overrides
		standardExtensions.addAll(PermitOverridesAlg.SET.getSupportedAlgorithms());
		// deny-unless-permit
		standardExtensions.addAll(DenyUnlessPermitAlg.SET.getSupportedAlgorithms());
		// permit-unless-deny
		standardExtensions.addAll(PermitUnlessDenyAlg.SET.getSupportedAlgorithms());
		// first-applicable
		standardExtensions.addAll(FirstApplicableAlg.SET.getSupportedAlgorithms());
		// only-one-applicable
		standardExtensions.add(new OnlyOneApplicablePolicyAlg());
		//
		// Legacy
		// (ordered-)deny-overrides
		standardExtensions.addAll(LegacyDenyOverridesAlg.SET.getSupportedAlgorithms());
		// (orderered-)permit-overrides
		standardExtensions.addAll(LegacyPermitOverridesAlg.SET.getSupportedAlgorithms());

		final Map<String, CombiningAlgorithm<? extends Decidable>> stdExtMap = new HashMap<>();
		for (final CombiningAlgorithm<? extends Decidable> stdExt : standardExtensions)
		{
			stdExtMap.put(stdExt.getId(), stdExt);
		}

		INSTANCE = new StandardCombiningAlgRegistry(stdExtMap);
	}

	private StandardCombiningAlgRegistry(Map<String, CombiningAlgorithm<? extends Decidable>> stdExtMap)
	{
		super(Collections.unmodifiableMap(stdExtMap));
	}

	@Override
	public void addExtension(CombiningAlgorithm<? extends Decidable> alg)
	{
		throw new UnsupportedOperationException("a standard factory cannot be modified");
	}

}
