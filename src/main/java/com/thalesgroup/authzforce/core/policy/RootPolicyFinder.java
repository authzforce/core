package com.thalesgroup.authzforce.core.policy;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * This class is used by the PDP to find the root (aka top-level) policy matching a given request
 * context for evaluation.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a
 * cache, a disk, a connection to a remote server, etc. for retrieving the root policy and any
 * policy referenced by it. Therefore, these resources must be released by calling {@link #close()}
 * when it is no longer needed.
 */
public abstract class RootPolicyFinder implements Closeable
{
	/**
	 * Finds one and only one policy applicable to the given request context and evaluates the
	 * request context against it. This will always do a Target match to make sure that the given
	 * policy applies.
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of evaluating the request against the applicable policy; or NotApplicable
	 *         if none is applicable; or Indeterminate if error determining an applicable policy or
	 *         more than one applies or evaluation of the applicable policy returned Indeterminate
	 *         Decision
	 */
	public abstract DecisionResult findAndEvaluate(EvaluationContext context);

	/**
	 * Get instance of this class depending on whether {@code mod} is a
	 * {@link RootPolicyFinderModule.Static} or not
	 * 
	 * @param mod
	 * @return instance of RootPolicyFinder
	 */
	public static RootPolicyFinder getInstance(RootPolicyFinderModule mod)
	{
		if (mod instanceof RootPolicyFinderModule.Static)
		{
			return new Static(((RootPolicyFinderModule.Static) mod).getRootPolicy());
		}

		return new Dynamic(mod);
	}

	private static class Static extends RootPolicyFinder
	{
		private final IPolicy rootPolicy;

		private Static(IPolicy rootPolicy)
		{
			assert rootPolicy != null;
			this.rootPolicy = rootPolicy;
		}

		@Override
		public DecisionResult findAndEvaluate(EvaluationContext context)
		{
			return rootPolicy.evaluate(context);
		}

		@Override
		public void close() throws IOException
		{
			// policy is statically resolved once and for all at initialization, so nothing left to
			// close
		}
	}

	/**
	 * Mutable AttributeFinder initialized with sub-modules, each responsible of finding attributes
	 * in a specific way from a specific source. This attribute finder tries to resolve attribute
	 * values in current evaluation context first, then if not there, query the sub-modules.
	 * <p>
	 * This class implements {@link Closeable} because the sub-modules may very likely hold
	 * resources such as network resources to get attributes remotely, attribute caches to speed up
	 * finding, etc. Therefore, you are required to call {@link #close()} when you no longer need an
	 * instance - especially before replacing with a new instance (with different modules) - in
	 * order to make sure these resources are released properly by each underlying module (e.g.
	 * invalidate the attribute caches).
	 */
	private static class Dynamic extends RootPolicyFinder implements Closeable
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(Dynamic.class);

		private final RootPolicyFinderModule rootPolicyFinderMod;

		/**
		 * Creates RootPolicyFinder instance
		 * 
		 * @param rootPolicyFinderMod
		 *            root policy finder module
		 * 
		 */
		private Dynamic(RootPolicyFinderModule rootPolicyFinderMod)
		{
			assert rootPolicyFinderMod != null;
			this.rootPolicyFinderMod = rootPolicyFinderMod;
		}

		@Override
		public DecisionResult findAndEvaluate(EvaluationContext context)
		{
			final IPolicy policy;
			try
			{
				policy = rootPolicyFinderMod.findPolicy(context);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.info("Error finding applicable root policy to evaluate with root policy finder module {}", rootPolicyFinderMod, e);
				return new DecisionResult(e.getStatus());
			} catch (ParsingException e)
			{
				LOGGER.warn("Error parsing one of the possible root policies (handled by root policy finder module {})", rootPolicyFinderMod, e);
				return e.getIndeterminateResult();
			}

			if (policy == null)
			{
				return DecisionResult.NOT_APPLICABLE;
			}

			return policy.evaluate(context, true);
		}

		@Override
		public void close() throws IOException
		{
			rootPolicyFinderMod.close();
		}

	}
}
