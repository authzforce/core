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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.core.eval.BagDatatype;
import com.thalesgroup.authzforce.core.eval.Bags;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
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

		private final Class<AV[]> arrayClass;
		private final IndeterminateEvaluationException invalidArgEmptyException;

		private SingletonBagToPrimitive(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_ONE_AND_ONLY, typeParameter.TYPE, false, typeParameter.BAG_TYPE);
			this.arrayClass = typeParameter.ARRAY_CLASS;
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + functionId + ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.", Status.STATUS_PROCESSING_ERROR);
		}

		@Override
		protected final FirstOrderFunctionCall<AV> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<AV, AV>(signature, arrayClass, argExpressions)
			{

				@Override
				protected final AV evaluate(AV[][] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private final AV eval(AV[] bag) throws IndeterminateEvaluationException
		{
			if (bag.length != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag[0];
		}
	}

	private static class BagSize<AV extends AttributeValue<AV>> extends FirstOrderFunction<IntegerAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag-size' functions
		 */
		private static final String NAME_SUFFIX_BAG_SIZE = "-bag-size";

		private final Class<AV[]> arrayClass;

		private BagSize(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG_SIZE, DatatypeConstants.INTEGER.TYPE, false, typeParameter.BAG_TYPE);
			this.arrayClass = typeParameter.ARRAY_CLASS;
		}

		@Override
		protected final FirstOrderFunctionCall<IntegerAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerAttributeValue, AV>(signature, arrayClass, argExpressions)
			{

				@Override
				protected final IntegerAttributeValue evaluate(AV[][] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private final static IntegerAttributeValue eval(AttributeValue<?>[] bag)
		{
			return new IntegerAttributeValue(bag.length);
		}

	}

	private static class BagContains<AV extends AttributeValue<AV>> extends FirstOrderFunction<BooleanAttributeValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-is-in' functions
		 */
		private static final String NAME_SUFFIX_IS_IN = "-is-in";

		private final Class<AV[]> arrayClass;

		private BagContains(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_IS_IN, DatatypeConstants.BOOLEAN.TYPE, false, typeParameter.TYPE, typeParameter.BAG_TYPE);
			this.arrayClass = typeParameter.ARRAY_CLASS;
		}

		@Override
		protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanAttributeValue, AV>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanAttributeValue evaluate(AV[] primArgsBeforeBag, AV[][] bagArgs, AV[] remainingArgs) throws IndeterminateEvaluationException
				{
					return BooleanAttributeValue.valueOf(eval(primArgsBeforeBag[0], bagArgs[0]));
				}

			};
		}

		public static <V extends AttributeValue<V>> boolean eval(V arg0, V[] bag)
		{
			for (final V bagVal : bag)
			{
				if (arg0.equals(bagVal))
				{
					return true;
				}
			}

			return false;
		}
	}

	private static class PrimitiveToBag<AV extends AttributeValue<AV>> extends FirstOrderFunction<Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag' functions
		 */
		private static final String NAME_SUFFIX_BAG = "-bag";

		private final Class<AV[]> arrayClass;
		private final BagDatatype<AV> bagReturnType;

		private PrimitiveToBag(DatatypeConstants<AV> typeParameter)
		{
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_BAG, typeParameter.BAG_TYPE, true, typeParameter.TYPE);
			this.bagReturnType = typeParameter.BAG_TYPE;
			this.arrayClass = typeParameter.ARRAY_CLASS;
		}

		@Override
		protected final FirstOrderFunctionCall<Bag<AV>> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<Bag<AV>, AV>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected Bag<AV> evaluate(AV[] args) throws IndeterminateEvaluationException
				{
					return Bags.getInstance(bagReturnType, args);
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
	private static abstract class SetFunction<AV extends AttributeValue<AV>, RETURN extends Value<?, RETURN>> extends FirstOrderFunction<RETURN>
	{

		private final Class<AV[]> arrayClass;

		// protected final Class<AV> datatypeClass;

		protected SetFunction(String functionId, Class<AV[]> typeValArrayClass, Datatype<RETURN> returnType, boolean varArgs, List<Datatype<Bag<AV>>> bagParamTypes)
		{
			super(functionId, returnType, varArgs, (Datatype<Bag<AV>>[]) bagParamTypes.toArray());
			this.arrayClass = typeValArrayClass;
			// this.datatypeClass = (Class<AV>) arrayClass.getComponentType();
		}

		@Override
		protected final FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN, AV>(signature, arrayClass, argExpressions)
			{

				@Override
				protected RETURN evaluate(AV[][] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs);
				}
			};
		}

		abstract protected RETURN eval(AV[][] bagArgs);
	}

	private static class Intersection<AV extends AttributeValue<AV>> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-intersection' functions
		 */
		private static final String NAME_SUFFIX_INTERSECTION = "-intersection";

		private final BagDatatype<AV> bagReturnType;

		private Intersection(DatatypeConstants<AV> typeParam)
		{
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_INTERSECTION, typeParam.ARRAY_CLASS, typeParam.BAG_TYPE, false, Arrays.<Datatype<Bag<AV>>> asList(typeParam.BAG_TYPE, typeParam.BAG_TYPE));
			this.bagReturnType = typeParam.BAG_TYPE;
		}

		@Override
		protected final Bag<AV> eval(AV[][] bagArgs)
		{
			return Bags.getInstance(bagReturnType, eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> Set<V> eval(V[] bag0, V[] bag1)
		{
			// TODO: compare performances with the solution using arrays directly (not Sets)
			final Set<V> result = new HashSet<>(Arrays.asList(bag0));
			result.retainAll(Arrays.asList(bag1));
			return result;
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
			super(typeParam.FUNCTION_ID_PREFIX + NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, typeParam.ARRAY_CLASS, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.<Datatype<Bag<AV>>> asList(typeParam.BAG_TYPE, typeParam.BAG_TYPE));
		}

		@Override
		protected final BooleanAttributeValue eval(AV[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(V[] bag0, V[] bag1)
		{
			for (final V bag0Val : bag0)
			{
				if (BagContains.eval(bag0Val, bag1))
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

		private final BagDatatype<AV> bagReturnType;

		private Union(DatatypeConstants<AV> typeParameter)
		{
			/*
			 * Union function takes two or more parameters, i.e. two parameters of a specific bag
			 * type and a variable-length (zero-to-any) parameter of the same bag type
			 */
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_UNION, typeParameter.ARRAY_CLASS, typeParameter.BAG_TYPE, true, Arrays.<Datatype<Bag<AV>>> asList(typeParameter.BAG_TYPE, typeParameter.BAG_TYPE, typeParameter.BAG_TYPE));
			this.bagReturnType = typeParameter.BAG_TYPE;
		}

		@Override
		protected final Bag<AV> eval(AV[][] bags)
		{
			// TODO: compare performances with the solution using arrays directly (not Set)
			final Set<AV> result = new HashSet<>(Arrays.asList(bags[0]));
			for (final AV[] bag : bags)
			{
				for (final AV bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return Bags.getInstance(bagReturnType, result);
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
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SUBSET, typeParameter.ARRAY_CLASS, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.<Datatype<Bag<AV>>> asList(typeParameter.BAG_TYPE, typeParameter.BAG_TYPE));
		}

		@Override
		protected final BooleanAttributeValue eval(AV[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(V[] bag0, V[] bag1)
		{
			// TODO: compare performances with the solution using arrays directly (not Set)
			final Set<V> set1 = new HashSet<>(Arrays.asList(bag1));
			return set1.containsAll(Arrays.asList(bag0));
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
			super(typeParameter.FUNCTION_ID_PREFIX + NAME_SUFFIX_SET_EQUALS, typeParameter.ARRAY_CLASS, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.<Datatype<Bag<AV>>> asList(typeParameter.BAG_TYPE, typeParameter.BAG_TYPE));
		}

		@Override
		protected final BooleanAttributeValue eval(AV[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue<V>> boolean eval(V[] bag0, V[] bag1)
		{

			final Set<V> set0 = new HashSet<>(Arrays.asList(bag0));
			final Set<V> set1 = new HashSet<>(Arrays.asList(bag1));
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
