/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.eval;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import com.sun.xacml.ParsingException;

/**
 * Low-level interface to a list of PEP action (obligation/advice) expressions
 * 
 */
public interface PepActionExpressions
{
	/**
	 * PepActionExpressions factory
	 * 
	 * @param <T>
	 *            type of created instance
	 */
	interface Factory<T extends PepActionExpressions>
	{
		T getInstance(XPathCompiler xPathCompiler, Expression.Factory expressionFactory);
	}

	/**
	 * Effect-specific obligation/advice expressions. Only expressions applying to such effect are
	 * allowed to be added to the list.
	 * 
	 */
	public static class EffectSpecific
	{
		// effect to which obligation and advice below apply
		private final EffectType effect;
		private final List<ObligationExpression> obligationExpList = new ArrayList<>();
		private final List<AdviceExpression> adviceExpList = new ArrayList<>();

		/**
		 * @param effect
		 *            Effect to which all obligation/advice expressions must apply
		 */
		public EffectSpecific(EffectType effect)
		{
			this.effect = effect;
		}

		/**
		 * Adds an ObligationExpression to the list only if matching the the effect argument to
		 * {@link EffectSpecific#EffectSpecific(EffectType)}
		 * 
		 * @param obligationExpression
		 *            ObligationExpression
		 * @return true iff {@code obligationExpression} actually added to the expressions, i.e.
		 *         fulfillOn matches the effect argument to
		 *         {@link EffectSpecific#EffectSpecific(EffectType)}
		 */
		public boolean add(ObligationExpression obligationExpression)
		{
			if (obligationExpression.getFulfillOn() != effect)
			{
				return false;
			}

			return obligationExpList.add(obligationExpression);
		}

		/**
		 * Adds an AdviceExpression to the list only if matching the the effect argument to
		 * {@link EffectSpecific#EffectSpecific(EffectType)}
		 * 
		 * @param adviceExpression
		 *            AdviceExpression
		 * @return true iff {@code adviceExpression} actually added to the expressions, i.e.
		 *         appliesTo matches the effect argument to
		 *         {@link EffectSpecific#EffectSpecific(EffectType)}
		 */
		public boolean add(AdviceExpression adviceExpression)
		{
			if (adviceExpression.getAppliesTo() != effect)
			{
				return false;
			}

			return adviceExpList.add(adviceExpression);
		}

		/**
		 * Effect-specific ObligationExpressions
		 * 
		 * @return the effect-specific ObligationExpressions
		 */
		public List<ObligationExpression> getObligationExpressions()
		{
			return this.obligationExpList;
		}

		/**
		 * Effect-specific AdviceExpressions
		 * 
		 * @return the effect-specific AdviceExpressions
		 */
		public List<AdviceExpression> getAdviceExpressions()
		{
			return this.adviceExpList;
		}

		/**
		 * Get Effect to be matched by all expressions
		 * 
		 * @return effect
		 */
		public EffectType getEffect()
		{
			return effect;
		}
	}

	/**
	 * Adds a XACML ObligationExpression to the list
	 * 
	 * @param jaxbObligationExp
	 *            XACML ObligationExpression
	 * @throws ParsingException
	 *             if error (e.g. syntax error) parsing the expression
	 */
	void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp) throws ParsingException;

	/**
	 * Adds a XACML AdviceExpression to the list
	 * 
	 * @param jaxbAdviceExp
	 *            XACML ObligationExpression
	 * @throws ParsingException
	 *             if error (e.g. syntax error) parsing the expression
	 */
	void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp) throws ParsingException;

	/**
	 * Gets all the expressions added with
	 * {@link #add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression)}
	 * 
	 * @return list of ObligationExpressions
	 */
	List<ObligationExpression> getObligationExpressionList();

	/**
	 * Gets all the expressions added with
	 * {@link #add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression)}
	 * 
	 * @return list of AdviceExpressions
	 */
	List<AdviceExpression> getAdviceExpressionList();
}
