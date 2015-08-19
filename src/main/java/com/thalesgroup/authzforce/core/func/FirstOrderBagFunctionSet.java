package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerBagEval;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPartlyBagEval;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

/**
 * Base class for first-order bag function groups, as opposed to the higher-order bag functions (see
 * {@link HigherOrderBagFunction}); such as the Bag functions of section A.3.10, and the Set
 * functions of A.3.11 of the XACML spec.
 * 
 */
public abstract class FirstOrderBagFunctionSet extends FunctionSet
{
	private static enum StandardTypeParameter
	{
		STRING(new GenericTypeParameter<>(StringAttributeValue.TYPE_URI, StringAttributeValue[].class, Function.FUNCTION_NS_1 + "string")),
		//
		BOOLEAN(new GenericTypeParameter<>(BooleanAttributeValue.TYPE_URI, BooleanAttributeValue[].class, Function.FUNCTION_NS_1 + "boolean")),
		//
		INTEGER(new GenericTypeParameter<>(IntegerAttributeValue.TYPE_URI, IntegerAttributeValue[].class, Function.FUNCTION_NS_1 + "integer")),
		//
		DOUBLE(new GenericTypeParameter<>(DoubleAttributeValue.TYPE_URI, DoubleAttributeValue[].class, Function.FUNCTION_NS_1 + "double")),
		//
		TIME(new GenericTypeParameter<>(TimeAttributeValue.TYPE_URI, TimeAttributeValue[].class, Function.FUNCTION_NS_1 + "time")),
		//
		DATE(new GenericTypeParameter<>(DateAttributeValue.TYPE_URI, DateAttributeValue[].class, Function.FUNCTION_NS_1 + "date")),
		//
		DATETIME(new GenericTypeParameter<>(DateTimeAttributeValue.TYPE_URI, DateTimeAttributeValue[].class, Function.FUNCTION_NS_1 + "dateTime")),
		//
		ANYURI(new GenericTypeParameter<>(AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue[].class, Function.FUNCTION_NS_1 + "anyURI")),
		//
		HEXBINARY(new GenericTypeParameter<>(HexBinaryAttributeValue.TYPE_URI, HexBinaryAttributeValue[].class, Function.FUNCTION_NS_1 + "hexBinary")),
		//
		BASE64BINARY(new GenericTypeParameter<>(Base64BinaryAttributeValue.TYPE_URI, Base64BinaryAttributeValue[].class, Function.FUNCTION_NS_1 + "base64Binary")),
		//
		X500NAME(new GenericTypeParameter<>(X500NameAttributeValue.TYPE_URI, X500NameAttributeValue[].class, Function.FUNCTION_NS_1 + "x500Name")),
		//
		RFC822NAME(new GenericTypeParameter<>(RFC822NameAttributeValue.TYPE_URI, RFC822NameAttributeValue[].class, Function.FUNCTION_NS_1 + "rfc822Name")),
		//
		IPADDRESS(new GenericTypeParameter<>(IPAddressAttributeValue.identifier, IPAddressAttributeValue[].class, Function.FUNCTION_NS_2 + "ipAddress")),
		//
		DNSNAME(new GenericTypeParameter<>(DNSNameAttributeValue.identifier, DNSNameAttributeValue[].class, Function.FUNCTION_NS_2 + "dnsName")),
		//
		DAYTIMEDURATION(new GenericTypeParameter<>(DayTimeDurationAttributeValue.TYPE_URI, DayTimeDurationAttributeValue[].class, Function.FUNCTION_NS_3 + "dayTimeDuration")),
		//
		YEARMONTHDURATION(new GenericTypeParameter<>(YearMonthDurationAttributeValue.TYPE_URI, YearMonthDurationAttributeValue[].class, Function.FUNCTION_NS_3 + "yearMonthDuration"));

		private final GenericTypeParameter<? extends AttributeValue> genericTypeParameter;

		private StandardTypeParameter(GenericTypeParameter<? extends AttributeValue> genericTypeParam)
		{
			this.genericTypeParameter = genericTypeParam;
		}
	}

	private static class GenericTypeParameter<V extends AttributeValue>
	{
		private final String funcIdPrefix;
		private final String datatypeURI;
		private final Class<V[]> arrayClass;

		private GenericTypeParameter(String primitiveTypeURI, Class<V[]> typeValArrayClass, String functionIdPrefix)
		{
			this.datatypeURI = primitiveTypeURI;
			this.arrayClass = typeValArrayClass;
			this.funcIdPrefix = functionIdPrefix;
		}
	}

	private final Set<Function<?>> functions = new HashSet<>();

	protected FirstOrderBagFunctionSet()
	{
		super(FunctionSet.DEFAULT_ID_NAMESPACE + "first-order-bag");
		for (final StandardTypeParameter typeParam : StandardTypeParameter.values())
		{
			functions.addAll(getGenericFunctions(typeParam.genericTypeParameter));
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

	protected abstract Set<Function<?>> getGenericFunctions(GenericTypeParameter<? extends AttributeValue> typeParameter);

	/**
	 * 
	 * Single-bag function group, i.e. group of bag functions that takes only one bag as parameter,
	 * or no bag parameter but returns a bag. Defined in section A.3.10. As opposed to Set functions
	 * that takes multiple bags as parameters.
	 * 
	 */
	public static class SingleBagFunctionSet extends FirstOrderBagFunctionSet
	{
		/**
		 * Function ID suffix for 'primitiveType-one-and-only' functions
		 */
		private static final String NAME_SUFFIX_ONE_AND_ONLY = "-one-and-only";

		/**
		 * Function ID suffix for 'primitiveType-bag-size' functions
		 */
		private static final String NAME_SUFFIX_BAG_SIZE = "-bag-size";

		/**
		 * Function ID suffix for 'primitiveType-is-in' functions
		 */
		private static final String NAME_SUFFIX_IS_IN = "-is-in";

		/**
		 * Function ID suffix for 'primitiveType-bag' functions
		 */
		private static final String NAME_SUFFIX_BAG = "-bag";

		@Override
		protected final Set<Function<?>> getGenericFunctions(GenericTypeParameter<? extends AttributeValue> typeParameter)
		{
			return new HashSet<>(Arrays.<Function<?>> asList(
			//
					new SingletonBagToPrimitive<>(typeParameter.funcIdPrefix + NAME_SUFFIX_ONE_AND_ONLY, typeParameter.datatypeURI, typeParameter.arrayClass),
					//
					new BagSize<>(typeParameter.funcIdPrefix + NAME_SUFFIX_BAG_SIZE, typeParameter.datatypeURI, typeParameter.arrayClass),
					//
					new BagContains<>(typeParameter.funcIdPrefix + NAME_SUFFIX_IS_IN, typeParameter.datatypeURI, typeParameter.arrayClass),
					//
					new PrimitiveToBag<>(typeParameter.funcIdPrefix + NAME_SUFFIX_BAG, typeParameter.datatypeURI, typeParameter.arrayClass)));
		}
	}

	private static class SingletonBagToPrimitive<T extends AttributeValue> extends FirstOrderFunction<T>
	{

		private final Class<T[]> arrayClass;
		private final IndeterminateEvaluationException invalidArgEmptyException;

		private SingletonBagToPrimitive(String functionId, String typeURI, Class<T[]> typeValArrayClass)
		{
			super(functionId, new DatatypeDef(typeURI), false, new DatatypeDef(typeURI, true));
			this.arrayClass = typeValArrayClass;
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + functionId + ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.", Status.STATUS_PROCESSING_ERROR);
		}

		@Override
		protected final FirstOrderFunctionCall<T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<T, T>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final T evaluate(T[][] bagArgs, T[] remainingArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private final T eval(T[] bag) throws IndeterminateEvaluationException
		{
			if (bag.length != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag[0];
		}
	}

	private static class BagSize<T extends AttributeValue> extends FirstOrderFunction<IntegerAttributeValue>
	{

		private final Class<T[]> arrayClass;

		private BagSize(String functionId, String typeURI, Class<T[]> typeValArrayClass)
		{
			super(functionId, IntegerAttributeValue.TYPE, false, new DatatypeDef(typeURI, true));
			this.arrayClass = typeValArrayClass;
		}

		@Override
		protected final FirstOrderFunctionCall<IntegerAttributeValue> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerAttributeValue, T>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final IntegerAttributeValue evaluate(T[][] bagArgs, T[] remainingArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private final static IntegerAttributeValue eval(AttributeValue[] bag)
		{
			return new IntegerAttributeValue(bag.length);
		}

	}

	private static class BagContains<T extends AttributeValue> extends FirstOrderFunction<BooleanAttributeValue>
	{

		private final Class<T[]> arrayClass;

		private BagContains(String functionId, String typeURI, Class<T[]> typeValArrayClass)
		{
			super(functionId, BooleanAttributeValue.TYPE, false, new DatatypeDef(typeURI), new DatatypeDef(typeURI, true));
			this.arrayClass = typeValArrayClass;
		}

		@Override
		protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanAttributeValue, T>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanAttributeValue evaluate(T[] primArgsBeforeBag, T[][] bagArgs, T[] remainingArgs) throws IndeterminateEvaluationException
				{
					return BooleanAttributeValue.valueOf(eval(primArgsBeforeBag[0], bagArgs[0]));
				}

			};
		}

		public static <V extends AttributeValue> boolean eval(V arg0, V[] bag)
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

	private static class PrimitiveToBag<T extends AttributeValue> extends FirstOrderFunction<BagResult<T>>
	{

		private final Class<T[]> arrayClass;
		private final Class<T> datatypeClass;

		private PrimitiveToBag(String functionId, String typeURI, Class<T[]> typeValArrayClass)
		{
			super(functionId, new DatatypeDef(typeURI, true), true, new DatatypeDef(typeURI));
			this.arrayClass = typeValArrayClass;
			this.datatypeClass = (Class<T>) arrayClass.getComponentType();
		}

		@Override
		protected final FirstOrderFunctionCall<BagResult<T>> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPrimitiveEval<BagResult<T>, T>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected BagResult<T> evaluate(T[] args) throws IndeterminateEvaluationException
				{
					return new BagResult<>(args, datatypeClass, signature.getReturnType());
				}
			};
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
		/**
		 * Function ID suffix for 'primitiveType-intersection' functions
		 */
		private static final String NAME_SUFFIX_INTERSECTION = "-intersection";

		/**
		 * Function ID suffix for 'primitiveType-at-least-one-member-of' functions
		 */
		private static final String NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF = "-at-least-one-member-of";

		/**
		 * Function ID suffix for 'primitiveType-union' functions
		 */
		private static final String NAME_SUFFIX_UNION = "-union";

		/**
		 * Function ID suffix for 'primitiveType-subset' functions
		 */
		private static final String NAME_SUFFIX_SUBSET = "-subset";

		/**
		 * Function ID suffix for 'primitiveType-set-equals' functions
		 */
		private static final String NAME_SUFFIX_SET_EQUALS = "-set-equals";

		@Override
		protected final Set<Function<?>> getGenericFunctions(GenericTypeParameter<? extends AttributeValue> typeParameter)
		{
			final DatatypeDef bagType = new DatatypeDef(typeParameter.datatypeURI, true);
			return new HashSet<>(Arrays.<Function<?>> asList(
			//
					new Intersection<>(typeParameter.funcIdPrefix + NAME_SUFFIX_INTERSECTION, bagType, typeParameter.arrayClass),
					//
					new AtLeastOneMemberOf<>(typeParameter.funcIdPrefix + NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, bagType, typeParameter.arrayClass),
					//
					new Union<>(typeParameter.funcIdPrefix + NAME_SUFFIX_UNION, bagType, typeParameter.arrayClass),
					//
					new Subset<>(typeParameter.funcIdPrefix + NAME_SUFFIX_SUBSET, bagType, typeParameter.arrayClass),
					//
					new SetEquals<>(typeParameter.funcIdPrefix + NAME_SUFFIX_SET_EQUALS, bagType, typeParameter.arrayClass)));
		}
	}

	/**
	 * 
	 * Base class of all *-set functions
	 * 
	 * @param <PRIMITIVE_T>
	 *            primitive type of elements in bag/set
	 * @param <RETURN_T>
	 *            return type
	 */
	private static abstract class SetFunction<PRIMITIVE_T extends AttributeValue, RETURN_T extends ExpressionResult<? extends AttributeValue>> extends FirstOrderFunction<RETURN_T>
	{

		private final Class<PRIMITIVE_T[]> arrayClass;
		protected final Class<PRIMITIVE_T> datatypeClass;

		protected SetFunction(String functionId, Class<PRIMITIVE_T[]> typeValArrayClass, DatatypeDef returnType, boolean varArgs, DatatypeDef... bagParamTypes)
		{
			super(functionId, returnType, varArgs, bagParamTypes);
			for (final DatatypeDef bagParamType : bagParamTypes)
			{
				if (!bagParamType.isBag())
				{
					throw new IllegalArgumentException("Invalid generic parameter type: not a bag type");
				}
			}

			this.arrayClass = typeValArrayClass;
			this.datatypeClass = (Class<PRIMITIVE_T>) arrayClass.getComponentType();
		}

		@Override
		protected final FirstOrderFunctionCall<RETURN_T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN_T, PRIMITIVE_T>(signature, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected RETURN_T evaluate(PRIMITIVE_T[][] bagArgs, PRIMITIVE_T[] remainingArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs);
				}
			};
		}

		abstract protected RETURN_T eval(PRIMITIVE_T[][] bagArgs);
	}

	private static class Intersection<T extends AttributeValue> extends SetFunction<T, BagResult<T>>
	{
		private Intersection(String functionId, DatatypeDef bagType, Class<T[]> typeValArrayClass)
		{
			super(functionId, typeValArrayClass, bagType, false, bagType, bagType);
		}

		@Override
		protected final BagResult<T> eval(T[][] bagArgs)
		{
			return new BagResult<>(eval(bagArgs[0], bagArgs[1]), datatypeClass, signature.getReturnType());
		}

		private final static <V extends AttributeValue> Set<V> eval(V[] bag0, V[] bag1)
		{
			// TODO: compare performances with the solution using arrays directly (not Set)
			final Set<V> result = new HashSet<>(Arrays.asList(bag0));
			result.retainAll(Arrays.asList(bag1));
			return result;
		}

	}

	private static class AtLeastOneMemberOf<T extends AttributeValue> extends SetFunction<T, BooleanAttributeValue>
	{
		private AtLeastOneMemberOf(String functionId, DatatypeDef bagType, Class<T[]> typeValArrayClass)
		{
			super(functionId, typeValArrayClass, BooleanAttributeValue.TYPE, false, bagType, bagType);
		}

		@Override
		protected final BooleanAttributeValue eval(T[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue> boolean eval(V[] bag0, V[] bag1)
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

	private static class Union<T extends AttributeValue> extends SetFunction<T, BagResult<T>>
	{
		private Union(String functionId, DatatypeDef bagType, Class<T[]> typeValArrayClass)
		{
			super(functionId, typeValArrayClass, bagType, true, bagType, bagType, bagType);
		}

		@Override
		protected final BagResult<T> eval(T[][] bags)
		{
			// TODO: compare performances with the solution using arrays directly (not Set)
			final Set<T> result = new HashSet<>(Arrays.asList(bags[0]));
			for (final T[] bag : bags)
			{
				for (final T bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return new BagResult<>(result, datatypeClass, signature.getReturnType());
		}
	}

	private static class Subset<T extends AttributeValue> extends SetFunction<T, BooleanAttributeValue>
	{
		private Subset(String functionId, DatatypeDef bagType, Class<T[]> typeValArrayClass)
		{
			super(functionId, typeValArrayClass, BooleanAttributeValue.TYPE, false, bagType, bagType);
		}

		@Override
		protected final BooleanAttributeValue eval(T[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue> boolean eval(V[] bag0, V[] bag1)
		{
			// TODO: compare performances with the solution using arrays directly (not Set)
			final Set<V> set1 = new HashSet<>(Arrays.asList(bag1));
			return set1.containsAll(Arrays.asList(bag0));
		}

	}

	private static class SetEquals<T extends AttributeValue> extends SetFunction<T, BooleanAttributeValue>
	{
		private SetEquals(String functionId, DatatypeDef bagType, Class<T[]> typeValArrayClass)
		{
			super(functionId, typeValArrayClass, BooleanAttributeValue.TYPE, false, bagType, bagType);
		}

		@Override
		protected final BooleanAttributeValue eval(T[][] bagArgs)
		{
			return BooleanAttributeValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private final static <V extends AttributeValue> boolean eval(V[] bag0, V[] bag1)
		{

			final Set<V> set0 = new HashSet<>(Arrays.asList(bag0));
			final Set<V> set1 = new HashSet<>(Arrays.asList(bag1));
			return set0.equals(set1);
		}

	}

}
