/*
 * Copyright 2012-2026 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/** Internal placeholder expansion support. */
final class PlaceholderResolver
{
	private static final String PREFIX = "${";
	private static final char SUFFIX = '}';

	private PlaceholderResolver()
	{
		// Utility class.
	}

	static String replace(final String input, final String defaultValueSeparator, final Function<String, String> valueResolver)
	{
		return replace(input, defaultValueSeparator, valueResolver, new HashSet<>());
	}

	private static String replace(final String input, final String defaultValueSeparator, final Function<String, String> valueResolver, final Set<String> activePlaceholders)
	{
		final StringBuilder output = new StringBuilder(input);
		int startIndex = output.indexOf(PREFIX);
		while (startIndex >= 0)
		{
			final int endIndex = findEndIndex(output, startIndex + PREFIX.length());
			if (endIndex < 0)
			{
				break;
			}

			final String originalPlaceholder = output.substring(startIndex + PREFIX.length(), endIndex);
			if (!activePlaceholders.add(originalPlaceholder))
			{
				throw new IllegalArgumentException("Circular placeholder reference '" + originalPlaceholder + "'");
			}

			final String placeholder = replace(originalPlaceholder, defaultValueSeparator, valueResolver, activePlaceholders);
			String value = valueResolver.apply(placeholder);
			if (value == null && defaultValueSeparator != null)
			{
				final int separatorIndex = placeholder.indexOf(defaultValueSeparator);
				if (separatorIndex >= 0)
				{
					value = valueResolver.apply(placeholder.substring(0, separatorIndex));
					if (value == null)
					{
						value = placeholder.substring(separatorIndex + defaultValueSeparator.length());
					}
				}
			}

			if (value == null)
			{
				throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "'");
			}

			value = replace(value, defaultValueSeparator, valueResolver, activePlaceholders);
			output.replace(startIndex, endIndex + 1, value);
			activePlaceholders.remove(originalPlaceholder);
			startIndex = output.indexOf(PREFIX, startIndex + value.length());
		}

		return output.toString();
	}

	private static int findEndIndex(final CharSequence input, final int contentStartIndex)
	{
		int nested = 0;
		for (int index = contentStartIndex; index < input.length(); index++)
		{
			if (index + 1 < input.length() && input.charAt(index) == '$' && input.charAt(index + 1) == '{')
			{
				nested++;
				index++;
			}
			else if (input.charAt(index) == SUFFIX)
			{
				if (nested == 0)
				{
					return index;
				}
				nested--;
			}
		}
		return -1;
	}
}
