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
package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.Expression.Value;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.Bag;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.datatypes.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerBagEval;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPartlyBagEval;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;

/**
 * Base class for first-order bag function groups, as opposed to the higher-order bag functions (see
 * {@link HigherOrderBagFunction}); such as the Bag functions of section A.3.10, and the Set
 * functions of A.3.11 of the XACML spec.
 * 
 */
public abstract class FirstOrderBagFunctionSet extends FunctionSet
{

	private final Set<Function<?>> functions = new HashSet<>();

	protected FirstOrderBagFunctionSet()
	{
		super(FunctionSet.DEFAULT_ID_NAMESPACE + "first-order-bag");
		for (final DatatypeConstants<?> typeParam : DatatypeConstants.SET)
		{
			functions.addAll(getGenericFunctions(typeParam));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FunctionSet#getSupportedFunctions()
	 */
	@Override
	public final Set<Function<?>> getSupportedFunctions()
	{
		return functions;
	}

	protected abstract <AV extends AttributeValue<AV>> Set<Function<?>> getGenericFunctions(DatatypeConstants<AV> typeParameter);

	private static class SingletonBagToPrimitive<AV extends AttributeValue<AV>> extends FirstOrderFunction<AV>
	{
		/**
		 * Function ID suffix for 'primitiveType-one-and-only' functions
		 */
		private static final String NAME_SUFFIX_ONE_AND_ONLY = "-one-and-only";

		private final IndeterminateEvaluationException invalidArgEmptyException;

		private final Bag.Datatype<AV> bagType;

		private SingletonBagToPrimitive(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_ONE_AND_ONLY, typeParameter.TYPE, false, typeParameter.BAG_TYPE);
			this.bagType = typeParameter.BAG_TYPE;
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + functionId + ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		@Override
		protected final FirstOrderFunctionCall<AV> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<AV, AV>(signature, bagType, argExpressions)
			{

				@Override
				protected final AV evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private final AV eval(Bag<AV> bag) throws IndeterminateEvaluationException
		{
			if (bag.size() != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag.getSingleValue();
		}
	}

	private static class BagSize<AV extends AttributeValue<AV>> extends FirstOrderFunction<IntegerAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag-size' functions
		 */
		private static final String NAME_SUFFIX_BAG_SIZE = "-bag-size";

		private final Bag.Datatype<AV> bagType;

		private BagSize(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG_SIZE, DatatypeConstants.INTEGER.TYPE, false, typeParameter.BAG_TYPE);
			this.bagType = typeParameter.BAG_TYPE;
		}

		@Override
		protected final FirstOrderFunctionCall<IntegerAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerAttributeValue, AV>(signature, bagType, argExpressions)
			{

				@Override
				protected final IntegerAttributeValue evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private final static IntegerAttributeValue eval(Bag<?> bag)
		{
			return new IntegerAttributeValue(bag.size());
		}

	}

	private static class BagContains<AV extends AttributeValue<AV>> extends FirstOrderFunction<BooleanAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-is-in' functions
		 */
		private static final String NAME_SUFFIX_IS_IN = "-is-in";

		private final Class<AV[]> arrayClass;

		private final Bag.Datatype<AV> bagType;

		private BagContains(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_IS_IN, DatatypeConstants.BOOLEAN.TYPE, false, typeParameter.TYPE, typeParameter.BAG_TYPE);
			this.arrayClass = typeParameter.ARRAY_CLASS;
			this.bagType = typeParameter.BAG_TYPE;
		}

		@Override
		protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanAttributeValue, AV>(signature, bagType, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanAttributeValue evaluate(Deque<AV> primArgsBeforeBag, Bag<AV>[] bagArgs, AV[] remainingArgs) throws IndeterminateEvaluationException
				{
					return BooleanAttributeValue.valueOf(eval(primArgsBeforeBag.getFirst(), bagArgs[0]));
				}

			};
		}

		public static <V extends AttributeValue<V>> boolean eval(V arg0, Bag<V> bag)
		{
			return bag.contains(arg0);
		}
	}

	private static class PrimitiveToBag<AV extends AttributeValue<AV>> extends FirstOrderFunction<Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag' functions
		 */
		private static final String NAME_SUFFIX_BAG = "-bag";

		private final Datatype<AV> paramType;
		private final Bag.Datatype<AV> bagReturnType;

		private PrimitiveToBag(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG, typeParameter.BAG_TYPE, true, typeParameter.TYPE);
			this.bagReturnType = typeParameter.BAG_TYPE;
			this.paramType = typeParameter.TYPE;
		}

		@Override
		protected final FirstOrderFunctionCall<Bag<AV>> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<Bag<AV>, AV>(signature, paramType, argExpressions, remainingArgTypes)
			{

				@Override
				protected Bag<AV> evaluate(Deque<AV> args) throws IndeterminateEvaluationException
				{
					return Bag.getInstance(bagReturnType, args);
				}
			};
		}
	}

	/**
	 * 
	 * Single-bag function group, i.e. group of bag functions that takes only one bag as parameter,
	 * or no bag parameter but returns a bag. Defined in section A.3.10. As opposed to Set functions
	 * that takes multiple bags as parameters.
	 * 
	 */
	public static class SingleBagFunctionSet extends FirstOrderBagFunctionSet
	{

		@Override
		protected final <AV extends AttributeValue<AV>> Set<Function<?>> getGenericFunctions(DatatypeConstants<AV> typeParameter)
		{
			return new HashSet<>(Arrays.<Function<?>> asList(
			//
					new SingletonBagToPrimitive<>(typeParameter),
					//
					new BagSize<>(typeParameter),
					//
					new BagContains<>(typeParameter),
					//
					new PrimitiveToBag<>(typeParameter)));
		}
	}

	/**
	 * 
	 * Base class of all *-set functions
	 * 
	 * @param <AV>
	 *            primitive type of elements in bag/set
	 * @param <RETURN>
	 *            return type
	 */
	private static abstract class SetFunction<AV extends AttributeValue<AV>, RETURN extends Value<RETURN>> extends FirstOrderFunction<RETURN>
	{

		private final Bag.Datatype<AV> bagType;

		/**
		 * Creates instance
		 * 
		 * @param functionId
		 *            function ID
		 * @param returnType
		 *            return type
		 * @param varArgs
		 *            variable-length parameter (the number of parameters to set function is
		 *            variable)
		 * @param parameterType
		 *            parameter (bag/set) type
		 * @param minNumOfParameters
		 *            total number of (bag) parameters if {@code varArgs == false}; or minimum
		 *            number of parameters if {@code varArgs == true}
		 */
		public SetFunction(String functionId, Datatype<RETURN> returnType, boolean varArgs, Bag.Datatype<AV> parameterType, int minNumOfParameters)
		{
			super(functionId, returnType, varArgs, Collections.nCopies(varArgs ? minNumOfParameters + 1 : minNumOfParameters, parameterType).toArray(new Datatype<?>[minNumOfParameters]));
			this.bagType = parameterType;
		}

		@Override
		protected final FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN, AV>(signature, bagType, argExpressions)
			{

				@Override
				protected RETURN evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs);
				}
			};
		}

		abstract protected RETURN eval(Bag<AV>[] bagArgs);
	}

	private static class Intersection<AV extends AttributeValue<AV>> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-intersection' functions
		 */
		private static final String NAME_SUFFIX_INTERSECTION = "-intersection";

		private final Bag.Datatype<AV> bagReturnType;

		private Intersection(DatatypeConstants<AV> typeParam)
		{
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_INTERSECTION, typeParam.BAG_TYPE, false, typeParam.BAG_TYPE, 2);
			this.bagReturnType = typeParam.BAG_TYPE;
		}

		@Override
		protected final Bag<AV> eval(Bag<AV>[] bagArgs)
		{
			return Bag.getInstance(bagReturnType, eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> Set<V> eval(Bag<V> bag0, Bag<V> bag1)
		{
			// http://tekmarathon.com/2012/11/26/find-intersection-of-elements-in-two-arrays/
			// We use a Set because no duplicate shall exist in the result
			final Set<V> intersection = new HashSet<>();
			final Bag<V> smallerBag;
			final Bag<V> biggerBag;
			final int bag0size = bag0.size();
			final int bag1size = bag1.size();
			if (bag0size < bag1size)
			{
				smallerBag = bag0;
				biggerBag = bag1;
			} else
			{
				smallerBag = bag1;
				biggerBag = bag0;
			}

			// for each value in biggest bag, check whether it is in the smaller bag
			for (final V v : biggerBag)
			{
				if (smallerBag.contains(v))
				{
					intersection.add(v);
				}
			}

			return intersection;
		}

	}

	private static class AtLeastOneMemberOf<AV extends AttributeValue<AV>> extends SetFunction<AV, BooleanAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-at-least-one-member-of' functions
		 */
		private static final String NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF = "-at-least-one-member-of";

		private AtLeastOneMemberOf(DatatypeConstants<AV> typeParam)
		{
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, DatatypeConstants.BOOLEAN.TYPE, false, typeParam.BAG_TYPE, 2);
		}

		@Override
		protected final BooleanAttributeValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(Bag<V> bag0, Bag<V> bag1)
		{
			for (final V bag0Val : bag0)
			{
				if (bag1.contains(bag0Val))
				{
					return true;
				}
			}

			return false;
		}

	}

	private static class Union<AV extends AttributeValue<AV>> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-union' functions
		 */
		private static final String NAME_SUFFIX_UNION = "-union";

		private final Bag.Datatype<AV> bagReturnType;

		private Union(DatatypeConstants<AV> typeParameter)
		{
			/*
			 * Union function takes two or more parameters, i.e. two parameters of a specific bag
			 * type and a variable-length (zero-to-any) parameter of the same bag type
			 */
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_UNION, typeParameter.BAG_TYPE, true, typeParameter.BAG_TYPE, 2);
			this.bagReturnType = typeParameter.BAG_TYPE;
		}

		@Override
		protected final Bag<AV> eval(Bag<AV>[] bags)
		{
			final Set<AV> result = new HashSet<>();
			for (final Bag<AV> bag : bags)
			{
				for (final AV bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return Bag.getInstance(bagReturnType, result);
		}
	}

	private static class Subset<AV extends AttributeValue<AV>> extends SetFunction<AV, BooleanAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-subset' functions
		 */
		private static final String NAME_SUFFIX_SUBSET = "-subset";

		private Subset(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SUBSET, DatatypeConstants.BOOLEAN.TYPE, false, typeParameter.BAG_TYPE, 2);
		}

		@Override
		protected final BooleanAttributeValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(Bag<V> bag0, Bag<V> bag1)
		{
			for (final V v : bag0)
			{
				if (!bag1.contains(v))
				{
					return false;
				}
			}

			return true;
		}

	}

	private static class SetEquals<AV extends AttributeValue<AV>> extends SetFunction<AV, BooleanAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-set-equals' functions
		 */
		private static final String NAME_SUFFIX_SET_EQUALS = "-set-equals";

		private SetEquals(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SET_EQUALS, DatatypeConstants.BOOLEAN.TYPE, false, typeParameter.BAG_TYPE, 2);
		}

		@Override
		protected final BooleanAttributeValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(Bag<V> bag0, Bag<V> bag1)
		{
			final Set<V> set0 = new HashSet<>();
			for (final V v : bag0)
			{
				set0.add(v);
			}

			final Set<V> set1 = new HashSet<>();
			for (final V v : bag1)
			{
				set1.add(v);
			}

			return set0.equals(set1);
		}

	}

	/**
	 * 
	 * Set function group, i.e. group of bag functions that takes multiple bags as parameters.
	 * Defined in section A.3.11.
	 * 
	 */
	public static class SetFunctionSet extends FirstOrderBagFunctionSet
	{

		@Override
		protected final <AV extends AttributeValue<AV>> Set<Function<?>> getGenericFunctions(DatatypeConstants<AV> typeParameter)
		{
			return new HashSet<>(Arrays.<Function<?>> asList(
			//
					new Intersection<>(typeParameter),
					//
					new AtLeastOneMemberOf<>(typeParameter),
					//
					new Union<>(typeParameter),
					//
					new Subset<>(typeParameter),
					//
					new SetEquals<>(typeParameter)));
		}
	}

}
