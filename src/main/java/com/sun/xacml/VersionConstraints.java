/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.policy.PolicyVersion;

/**
 * Supports the three version constraints that can be included with a policy reference. This class also provides a simple set of comparison methods for matching
 * against the constraints. Note that this feature was introduced in XACML 2.0, which means that constraints are never used in pre-2.0 policy references.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class VersionConstraints
{

	// the three constraints
	private final PolicyVersionPattern versionPattern;
	private final PolicyVersionPattern earliestVersionPattern;
	private final PolicyVersionPattern latestVersionPattern;

	private static class PolicyVersionPattern
	{
		private static final int WILDCARD = -1;
		private static final int PLUS = -2;

		private final String xacmlVersionMatch;
		private final List<Integer> matchNumbers;

		private PolicyVersionPattern(String xacmlVersionMatch)
		{
			assert xacmlVersionMatch != null;
			if (xacmlVersionMatch.isEmpty() || xacmlVersionMatch.startsWith(".") || xacmlVersionMatch.endsWith("."))
			{
				throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'");
			}

			final String[] tokens = xacmlVersionMatch.split("\\.");
			matchNumbers = new ArrayList<>(tokens.length);
			for (int i = 0; i < tokens.length; i++)
			{
				final String token = tokens[i];
				switch (token)
				{
				case "*":
					matchNumbers.add(WILDCARD);
					break;
				case "+":
					matchNumbers.add(PLUS);
					break;
				default:
					final int number;
					try
					{
						number = Integer.parseInt(tokens[i], 10);
					} catch (NumberFormatException e)
					{
						throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'", e);
					}

					if (number < 0)
					{
						throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'. Number #" + i + " (=" + number
								+ ") is not a positive integer");
					}

					matchNumbers.add(number);
					break;
				}
			}

			this.xacmlVersionMatch = xacmlVersionMatch;
		}

		@Override
		public String toString()
		{
			return xacmlVersionMatch;
		}

		private boolean matches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
				case PLUS:
					// always matches everything from here
					return true;
				case WILDCARD:
					// always matches any versionNumbers[i], so go on
					break;
				default:
					if (matchNum != versionNum)
					{
						return false;
					}

					// else same number, so go on
					break;
				}
			}

			/*
			 * At this point, last matchNum is either a wildcard or integer. Version matches iff there is no extra number in either matchNumbers or
			 * versionNumbers.
			 */
			return !matchNumsIterator.hasNext() && !versionNumsIterator.hasNext();
		}

		public boolean isLaterOrMatches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
				case PLUS:
					// always matches everything from here
					return true;
				case WILDCARD:
					/*
					 * Always matches any versionNumbers[i], and we could always find an acceptable version V > version argument that matches this pattern
					 * (matchNumbers) by taking a single number greater than versionNumbers[i] at the same index in V. So versionNumbers is earlier than the
					 * latest acceptable.
					 */
					return true;
				default:
					if (matchNum < versionNum)
					{
						return false;
					}

					if (matchNum > versionNum)
					{
						return true;
					}

					// else same number, so go on
					break;
				}
			}

			/*
			 * At this point, we know matchNumbers is a sequence of numbers (no wildcard/plus symbol). It is later than or matches versionNumbers iff there is
			 * no extra number in versionNums.
			 */
			return !versionNumsIterator.hasNext();
		}

		public boolean isEarlierOrMatches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
				case PLUS:
					// always matches everything from here
					return true;
				case WILDCARD:
					if (versionNum != 0)
					{
						/*
						 * We can find an earlier matching version (with any number < versionNum here).
						 */
						return true;
					}

					// versionNum = 0. Result depends on the next numbers.
					break;
				default:
					if (matchNum < versionNum)
					{
						return true;
					}

					if (matchNum > versionNum)
					{
						return false;
					}

					// else same number, so go on
					break;
				}
			}

			/*
			 * If there is no extra numbers in matchNumbers.length, it is earlier or matches versionNums
			 */
			return !matchNumsIterator.hasNext();
		}

	}

	/**
	 * Creates a <code>VersionConstraints</code> with the three optional constraint strings. Each of the three strings must conform to the VersionMatchType type
	 * defined in the XACML schema. Any of the strings may be null to specify that the given constraint is not used.
	 * 
	 * @param versionMatch
	 *            matching expression for the version; or null if none
	 * @param earliestMatch
	 *            matching expression for the earliest acceptable version; or null if none
	 * @param latestMatch
	 *            matching expression for the earliest acceptable version; or null if none
	 */
	public VersionConstraints(String versionMatch, String earliestMatch, String latestMatch)
	{
		this.versionPattern = versionMatch == null ? null : new PolicyVersionPattern(versionMatch);
		this.earliestVersionPattern = earliestMatch == null ? null : new PolicyVersionPattern(earliestMatch);
		this.latestVersionPattern = latestMatch == null ? null : new PolicyVersionPattern(latestMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("Version=%s,EarliestVersion=%s,LatestVersion=%s", (versionPattern == null) ? "*" : versionPattern,
				(earliestVersionPattern == null) ? "*" : earliestVersionPattern, (latestVersionPattern == null) ? "*" : latestVersionPattern);
	}

	/**
	 * Check version against LatestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff LatestVersion matched
	 */
	public boolean matchLatestVersion(PolicyVersion version)
	{
		return latestVersionPattern == null || latestVersionPattern.isLaterOrMatches(version);
	}

	/**
	 * Check version against EarliestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff EarliestVersion matched
	 */
	public boolean matchEarliestVersion(PolicyVersion version)
	{
		return earliestVersionPattern == null || earliestVersionPattern.isEarlierOrMatches(version);
	}

	/**
	 * Check version against Version pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff Version matched
	 */
	public boolean matchVersion(PolicyVersion version)
	{
		return versionPattern == null || versionPattern.matches(version);
	}

	/**
	 * Get Version pattern:
	 * 
	 * @return Version to be matched
	 */
	public String getVersionPattern()
	{
		return this.versionPattern.toString();
	}

	/**
	 * Get EarliestVersion pattern: matching expression for the earliest acceptable version
	 * 
	 * @return EarliestVersion to be matched
	 */
	public String getEarliestVersionPattern()
	{
		return this.earliestVersionPattern.toString();
	}

	/**
	 * Get LatestVersion pattern: matching expression for the latest acceptable version
	 * 
	 * @return LatestVersion to be matched
	 */
	public String getLatestVersionPattern()
	{
		return this.latestVersionPattern.toString();
	}

	// public static void main(String... args)
	// {
	// PolicyVersionPattern vp = new PolicyVersionPattern("0.+");
	// PolicyVersion v = new PolicyVersion("1.2.4.5");
	// System.out.println(vp.isLaterOrMatches(v));
	// }
}
