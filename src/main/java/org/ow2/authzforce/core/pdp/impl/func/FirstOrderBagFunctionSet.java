/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.func.FirstOrderFunctionCall.EagerBagEval;
import org.ow2.authzforce.core.func.FirstOrderFunctionCall.EagerPartlyBagEval;
import org.ow2.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.BagDatatype;
import org.ow2.authzforce.core.value.Bags;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;
import org.ow2.authzforce.core.value.IntegerValue;
import org.ow2.authzforce.core.value.Value;

import com.sun.xacml.Function;

/**
 * First-order bag function groups, as opposed to the higher-order bag functions (see {@link HigherOrderBagFunction}); such as the Bag functions of section
 * A.3.10, and the Set functions of A.3.11 of the XACML spec.
 * 
 */
public final class FirstOrderBagFunctionSet
{

	private static final class SingletonBagToPrimitive<AV extends AttributeValue> extends FirstOrderFunction.SingleParameterTyped<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-one-and-only' functions
		 */
		private static final String NAME_SUFFIX_ONE_AND_ONLY = "-one-and-only";

		private final IndeterminateEvaluationException invalidArgEmptyException;

		private SingletonBagToPrimitive(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_ONE_AND_ONLY, typeParameter.TYPE, false, Arrays.asList(typeParameter.BAG_TYPE));
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + functionId
					+ ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		@Override
		protected FirstOrderFunctionCall<AV> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<AV, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final AV evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private AV eval(Bag<AV> bag) throws IndeterminateEvaluationException
		{
			if (bag.size() != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag.getSingleValue();
		}
	}

	private static final class BagSize<AV extends AttributeValue> extends FirstOrderFunction.SingleParameterTyped<IntegerValue, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag-size' functions
		 */
		private static final String NAME_SUFFIX_BAG_SIZE = "-bag-size";

		private BagSize(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG_SIZE, DatatypeConstants.INTEGER.TYPE, false, Arrays.asList(typeParameter.BAG_TYPE));
		}

		@Override
		protected FirstOrderFunctionCall<IntegerValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
				throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerValue, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final IntegerValue evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private static IntegerValue eval(Bag<?> bag)
		{
			return new IntegerValue(bag.size());
		}

	}

	private static final class BagContains<AV extends AttributeValue> extends FirstOrderFunction.MultiParameterTyped<BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-is-in' functions
		 */
		private static final String NAME_SUFFIX_IS_IN = "-is-in";

		private final Class<AV[]> arrayClass;

		private final BagDatatype<AV> bagType;

		private BagContains(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_IS_IN, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(typeParameter.TYPE,
					typeParameter.BAG_TYPE));
			this.arrayClass = typeParameter.ARRAY_CLASS;
			this.bagType = typeParameter.BAG_TYPE;
		}

		@Override
		protected FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
				throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanValue, AV>(functionSignature, bagType, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanValue evaluate(Deque<AV> primArgsBeforeBag, Bag<AV>[] bagArgs, AV[] remainingArgs)
						throws IndeterminateEvaluationException
				{
					return BooleanValue.valueOf(eval(primArgsBeforeBag.getFirst(), bagArgs[0]));
				}

			};
		}

		public static <V extends AttributeValue> boolean eval(V arg0, Bag<V> bag)
		{
			return bag.contains(arg0);
		}
	}

	private static final class PrimitiveToBag<AV extends AttributeValue> extends FirstOrderFunction.SingleParameterTyped<Bag<AV>, AV>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag' functions
		 */
		private static final String NAME_SUFFIX_BAG = "-bag";

		private final Datatype<AV> paramType;

		private PrimitiveToBag(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG, typeParameter.BAG_TYPE, true, Arrays.asList(typeParameter.TYPE));
			this.paramType = typeParameter.TYPE;
		}

		@Override
		protected FirstOrderFunctionCall<Bag<AV>> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<Bag<AV>, AV>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected Bag<AV> evaluate(Deque<AV> args) throws IndeterminateEvaluationException
				{
					return Bags.getInstance(paramType, args);
				}
			};
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
	private static abstract class SetFunction<AV extends AttributeValue, RETURN extends Value> extends FirstOrderFunction.SingleParameterTyped<RETURN, Bag<AV>>
	{

		/**
		 * Creates instance
		 * 
		 * @param functionId
		 *            function ID
		 * @param returnType
		 *            return type
		 * @param varArgs
		 *            variable-length parameter (the number of parameters to set function is variable)
		 * @param parameterTypes
		 *            parameter (bag/set) types (repetitions of same one as many times as the function requires)
		 */
		public SetFunction(String functionId, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<Bag<AV>>> parameterTypes)
		{
			super(functionId, returnType, varArgs, parameterTypes);
		}

		@Override
		protected final FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
				throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN, AV>(functionSignature, argExpressions)
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

	private static final class Intersection<AV extends AttributeValue> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-intersection' functions
		 */
		private static final String NAME_SUFFIX_INTERSECTION = "-intersection";

		private final Datatype<AV> paramType;

		private Intersection(DatatypeConstants<AV> typeParam)
		{
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_INTERSECTION, typeParam.BAG_TYPE, false, Arrays.asList(typeParam.BAG_TYPE, typeParam.BAG_TYPE));
			this.paramType = typeParam.TYPE;
		}

		@Override
		protected Bag<AV> eval(Bag<AV>[] bagArgs)
		{
			return Bags.getInstance(this.paramType, eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> Set<V> eval(Bag<V> bag0, Bag<V> bag1)
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

	private static final class AtLeastOneMemberOf<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-at-least-one-member-of' functions
		 */
		private static final String NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF = "-at-least-one-member-of";

		private AtLeastOneMemberOf(DatatypeConstants<AV> typeParam)
		{
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(typeParam.BAG_TYPE,
					typeParam.BAG_TYPE));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
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

	private static final class Union<AV extends AttributeValue> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-union' functions
		 */
		private static final String NAME_SUFFIX_UNION = "-union";

		private final Datatype<AV> paramType;

		private Union(DatatypeConstants<AV> typeParameter)
		{
			/*
			 * Union function takes two or more parameters, i.e. two parameters of a specific bag type and a variable-length (zero-to-any) parameter of the same
			 * bag type
			 */
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_UNION, typeParameter.BAG_TYPE, true, Arrays.asList(typeParameter.BAG_TYPE,
					typeParameter.BAG_TYPE, typeParameter.BAG_TYPE));
			this.paramType = typeParameter.TYPE;
		}

		@Override
		protected Bag<AV> eval(Bag<AV>[] bags)
		{
			final Set<AV> result = new HashSet<>();
			for (final Bag<AV> bag : bags)
			{
				for (final AV bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return Bags.getInstance(this.paramType, result);
		}
	}

	private static final class Subset<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-subset' functions
		 */
		private static final String NAME_SUFFIX_SUBSET = "-subset";

		private Subset(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SUBSET, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(typeParameter.BAG_TYPE,
					typeParameter.BAG_TYPE));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
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

	private static final class SetEquals<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-set-equals' functions
		 */
		private static final String NAME_SUFFIX_SET_EQUALS = "-set-equals";

		private SetEquals(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SET_EQUALS, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(typeParameter.BAG_TYPE,
					typeParameter.BAG_TYPE));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
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

	private static Set<Function<?>> getFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final DatatypeConstants<?> typeParam : DatatypeConstants.MANDATORY_DATATYPE_SET)
		{
			/**
			 * 
			 * Single-bag function group, i.e. group of bag functions that takes only one bag as parameter, or no bag parameter but returns a bag. Defined in
			 * section A.3.10. As opposed to Set functions that takes multiple bags as parameters.
			 * 
			 */
			mutableSet.add(new SingletonBagToPrimitive<>(typeParam));
			mutableSet.add(new BagSize<>(typeParam));
			mutableSet.add(new BagContains<>(typeParam));
			mutableSet.add(new PrimitiveToBag<>(typeParam));
			/**
			 * 
			 * Add bag functions that takes multiple bags as parameters. Defined in section A.3.11.
			 * 
			 */
			mutableSet.add(new Intersection<>(typeParam));
			mutableSet.add(new AtLeastOneMemberOf<>(typeParam));
			mutableSet.add(new Union<>(typeParam));
			mutableSet.add(new Subset<>(typeParam));
			mutableSet.add(new SetEquals<>(typeParam));
		}

		return mutableSet;

	}

	/**
	 * FirstOrderBagFunctionSet instance (singleton)
	 */
	public static final FunctionSet INSTANCE = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "first-order-bag", getFunctions());

}
