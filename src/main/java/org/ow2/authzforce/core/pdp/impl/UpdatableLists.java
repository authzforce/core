/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.UpdatableList;

import com.google.common.collect.ImmutableList;

/**
 * Factory for {@code UpdatableList}s
 * 
 */
public final class UpdatableLists
{
	private UpdatableLists()
	{
		// prevent instantiation
	}

	/**
	 * {@link ArrayList}-based implementation of {@link UpdatableList}
	 * 
	 * @param <E>
	 *            type of elements in this list
	 */
	private static final class UpdatableArrayList<E> implements UpdatableList<E>
	{
		private final List<E> list = new ArrayList<>();

		private UpdatableArrayList()
		{
			// list initialized above
		}

		private UpdatableArrayList(final E e)
		{
			list.add(e);
		}

		@Override
		public boolean add(final E e) throws NullPointerException
		{
			return list.add(e);
		}

		@Override
		public boolean addAll(final List<E> elements) throws NullPointerException
		{
			return list.addAll(elements);
		}

		@Override
		public ImmutableList<E> copy()
		{
			return ImmutableList.copyOf(list);
		}

	}

	private static final class VoidUpdatableList<E> implements UpdatableList<E>
	{

		@Override
		public boolean add(final E e)
		{
			return false;
		}

		@Override
		public boolean addAll(final List<E> elements)
		{
			return false;
		}

		@Override
		public ImmutableList<E> copy()
		{
			return ImmutableList.of();
		}

	}

	/**
	 * Get instance of UpdatableList that does not update anything and {@link UpdatableList#copy()} always return an
	 * empty list. This implementation does not raise any exception on {@link UpdatableList#add(Object)} and
	 * {@link UpdatableList#addAll(List)} method but merely return false always. This is useful merely for polymorphism.
	 * 
	 * @return "empty" list, i.e. list that silently ignores updates and always stays empty
	 */
	public static <E> UpdatableList<E> empty()
	{
		return new VoidUpdatableList<>();
	}

	/**
	 * Create new instance of UpdatableList not accepting null values
	 * 
	 * @return new instance
	 */
	public static <E> UpdatableList<E> newUpdatableList()
	{
		return new UpdatableArrayList<>();
	}

}