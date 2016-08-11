package org.ow2.authzforce.core.pdp.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.PepActions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * Immutable version of {@link PepActions}
 *
 */
public class ImmutablePepActions implements PepActions {

	// always non-null fields, immutable
	private final List<Obligation> obligationList;
	private final List<Advice> adviceList;

	private transient volatile int hashCode = 0;
	
	private transient volatile String toString = null;

	/**
	 * Instantiates PEP action set from obligations/advice
	 *
	 * @param obligationList
	 *            obligation list; null if no obligation
	 * @param adviceList
	 *            advice list; null if no advice
	 */
	public ImmutablePepActions(List<Obligation> obligationList, List<Advice> adviceList)
	{
		this.obligationList = obligationList == null ? Collections.<Obligation>emptyList() : Collections.unmodifiableList(obligationList);
		this.adviceList = adviceList == null ? Collections.<Advice>emptyList() : Collections.unmodifiableList(adviceList);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal obligation list
	 */
	@Override
	public List<Obligation> getObligatory()
	{
		return obligationList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal advice list
	 */
	@Override
	public List<Advice> getAdvisory()
	{
		return adviceList;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.obligationList, this.adviceList);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof ImmutablePepActions))
		{
			return false;
		}

		final ImmutablePepActions other = (ImmutablePepActions) obj;
		return this.obligationList.equals(other.obligationList) && this.adviceList.equals(other.adviceList);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		if(toString == null) {
			toString = "[obligation_list=" + obligationList + ", advice_list=" + adviceList + "]";
		}
		
		return toString;
	}
}