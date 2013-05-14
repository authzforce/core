/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.sun.xacml.support.finder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.PolicySet;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.xacmlv3.AnyOf;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.xacmlv3.Target;


/**
 * This class handles collections of <code>AbstractPolicy</code> instances,
 * and provides some commonly useful operations. Specifically, it lets you
 * retrieve matching policies (based on reference or context), it optionally
 * handles wrapping multiple matches under a single PolicySet, and it manages
 * different versions of policies correctly. This class is intended for use
 * as a backing store to <code>PolicyFinderModule</code>s, but in practice
 * may have many uses.
 * <p>
 * Note that this class will accept multiple versions of the same policy. This
 * means that when you retieve a policy by reference, you will get the
 * correct version. It also means that when you retrieve a policy based on
 * context, there may be multiple revisions of the same policy, any number
 * of which may apply. Generally speaking, the correct behavior here is not
 * to return all of these policies, since they are (virtually speaking) the
 * same policy, but may have conflicting rules. So, as a simplification, and
 * to handle the most common cases, only the most recent version of a policy
 * is returned in these cases. If you need a more complex solution, you
 * will need to implement it yourself. Because the support modules use this
 * class as their backing store, this is true also of those modules.
 * <p>
 * Note that this is not a heavily optimized class. It is intended more as
 * an example, support code for the finder modules, and a starting utility
 * for other programmers than as an enterprise-quality implementation. That
 * said, it is fully functional, and should be useful for many applications.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class PolicyCollection
{

    // the actual collection of policies
    private HashMap policies;
    
    private HashMap policieSets;

    // the single instance of the comparator we'll use for managing versions
    private VersionComparator versionComparator = new VersionComparator();

    // the optional combining algorithm used when wrapping multiple policies
    private PolicyCombiningAlgorithm combiningAlg;

    // the optional policy id used when wrapping multiple policies
    private URI parentId;

    // default target that matches anything, used in wrapping policies
    private static final Target target;

    /**
     * This static initializer just sets up the default target, which is
     * used by all wrapping policy sets.
     */
    static {
        target = new Target(new AnyOf());
//            new Target(new TargetSection(null, TargetMatch.SUBJECT,
//                                         PolicyMetaData.XACML_VERSION_2_0),
//                       new TargetSection(null, TargetMatch.RESOURCE,
//                                         PolicyMetaData.XACML_VERSION_2_0),
//                       new TargetSection(null, TargetMatch.ACTION,
//                                         PolicyMetaData.XACML_VERSION_2_0),
//                       new TargetSection(null, TargetMatch.ENVIRONMENT,
//                                         PolicyMetaData.XACML_VERSION_2_0));
    };

    /**
     * Creates a new <code>PolicyCollection</code> that will return errors
     * when multiple policies match for a given request.
     */
    public PolicyCollection() {
        policies = new HashMap();
        policieSets = new HashMap();
        combiningAlg = null;
    }

    /**
     * Creates a new <code>PolicyCollection</code> that will create a new
     * top-level PolicySet when multiple policies match for a given request.
     *
     * @param combiningAlg the algorithm to use in a new PolicySet when more
     *                     than one policy applies
     * @param parentPolicyId the identifier to use for the new PolicySet
     */
    public PolicyCollection(PolicyCombiningAlgorithm combiningAlg,
                            URI parentPolicyId) {
        policies = new HashMap();
        policieSets = new HashMap();

        this.combiningAlg = combiningAlg;
        this.parentId = parentPolicyId;
    }

    /**
     * Adds a new policy to the collection, and uses the policy's identifier
     * as the reference identifier. If this identifier already exists in the
     * collection, and this policy does not represent a new version of the
     * policy, then the policy is not added.
     *
     * @param policy the policy to add
     *
     * @return true if the policy was added, false otherwise
     */
    public boolean addPolicy(Policy  policy) {
        return addPolicy(policy, policy.getPolicyId());
    }
    
    public boolean addPolicySet(PolicySet  policySet) {
        return addPolicySet(policySet, policySet.getPolicySetId());
    }

    /**
     * Adds a new policy to the collection using the given identifier as
     * the reference identifier. If this identifier already exists in the
     * collection, and this policy does not represent a new version of the
     * policy, then the policy is not added.
     *
     * @param policy the policy to add
     * @param identifier the identifier to use when referencing this policy
     *
     * @return true if the policy was added, false otherwise
     */
    public boolean addPolicy(Policy policy, String identifier) {
        if (policies.containsKey(identifier)) {
            // this identifier is already is use, so see if this version is
            // already in the set
            TreeSet set = (TreeSet)(policies.get(identifier));
            return set.add(policy);
        } else {
            // this identifier isn't already being used, so create a new
            // set in the map for it, and add the policy
            TreeSet set = new TreeSet(versionComparator);
            policies.put(identifier, set);
            return set.add(policy);
        }
    }
    
    public boolean addPolicySet(PolicySet policySet, String identifier) {
        if (policieSets.containsKey(identifier)) {
            // this identifier is already is use, so see if this version is
            // already in the set
            TreeSet set = (TreeSet)(policieSets.get(identifier));
            return set.add(policySet);
        } else {
            // this identifier isn't already being used, so create a new
            // set in the map for it, and add the policy
            TreeSet set = new TreeSet(versionComparator);
            policieSets.put(identifier, set);
            return set.add(policySet);
        }
    }
    
    /**
     * Attempts to retrieve a policy based on the given context. If multiple
     * policies match then this will either throw an exception or wrap the
     * policies under a new PolicySet (depending on how this instance was
     * constructed). If no policies match, then this will return null. See
     * the comment in the class header about how this behaves when multiple
     * versions of the same policy exist.
     *
     * @param context representation of a request
     *
     * @return a matching policy, or null if no policy matches
     *
     * @throws TopLevelPolicyException if multiple policies match but this
     *                                 instance wasn't setup to wrap policies
     */
    public PolicySet getPolicySet(EvaluationCtx context)
        throws TopLevelPolicyException
    {
        // setup a list of matching policies
        ArrayList list = new ArrayList();
        // get an iterator over all the identifiers
        Iterator it = policieSets.values().iterator();

        while (it.hasNext()) {
            // for each identifier, get only the most recent policy
            PolicySet policy = (PolicySet)(((TreeSet)(it.next())).first());

            // see if we match
            MatchResult match = policy.match(context);
            int result = match.getResult();
            
            // if there was an error, we stop right away
            if (result == MatchResult.INDETERMINATE)
                throw new TopLevelPolicyException(match.getStatus());

            // if we matched, we keep track of the matching policy...
            if (result == MatchResult.MATCH) {
                // ...first checking if this is the first match and if
                // we automaticlly nest policies
                if ((combiningAlg == null) && (list.size() > 0)) {
                    ArrayList code = new ArrayList();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    Status status = new Status(code, "too many applicable"
                                               + " top-level policies");
                    throw new TopLevelPolicyException(status);
                }

                list.add(policy);
            }
        }
        
        // no errors happened during the search, so now take the right
        // action based on how many policies we found
        switch (list.size()) {
        case 0:
            return null;
        case 1:
            return ((PolicySet)(list.get(0)));
        //FIXME
        default:        	
            return new PolicySet(parentId, combiningAlg, target, list);
        }
    }

    /**
     * Attempts to retrieve a policy based on the given context. If multiple
     * policies match then this will either throw an exception or wrap the
     * policies under a new PolicySet (depending on how this instance was
     * constructed). If no policies match, then this will return null. See
     * the comment in the class header about how this behaves when multiple
     * versions of the same policy exist.
     *
     * @param context representation of a request
     *
     * @return a matching policy, or null if no policy matches
     *
     * @throws TopLevelPolicyException if multiple policies match but this
     *                                 instance wasn't setup to wrap policies
     */
    public Object getPolicy(EvaluationCtx context)
        throws TopLevelPolicyException
    {
        // setup a list of matching policies
        ArrayList list = new ArrayList();
        // get an iterator over all the identifiers
        Iterator it = policies.values().iterator();

        while (it.hasNext()) {
            // for each identifier, get only the most recent policy
            Policy policy = (Policy)(((TreeSet)(it.next())).first());

            // see if we match
            MatchResult match = policy.match(context);
            int result = match.getResult();
            
            // if there was an error, we stop right away
            if (result == MatchResult.INDETERMINATE)
                throw new TopLevelPolicyException(match.getStatus());

            // if we matched, we keep track of the matching policy...
            if (result == MatchResult.MATCH) {
                // ...first checking if this is the first match and if
                // we automaticlly nest policies
                if ((combiningAlg == null) && (list.size() > 0)) {
                    ArrayList code = new ArrayList();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    Status status = new Status(code, "too many applicable"
                                               + " top-level policies");
                    throw new TopLevelPolicyException(status);
                }

                list.add(policy);
            }
        }
        
        // no errors happened during the search, so now take the right
        // action based on how many policies we found
        switch (list.size()) {
        case 0:
            return null;
        case 1:
            return list.get(0);
        //TODO: build a real policySet with obligations, advices etc... 
        default:
//        	return new PolicySet(parentId, policySet.getVersion(), combiningAlg, policySet.getDescription(), target, list, policySet.getDefaultVersion(), policySet.getObligationExpressions(), policySet.getAdviceExpressions());
            return new PolicySet(parentId, combiningAlg, target, list);
        }
    }

    /**
     * Attempts to retrieve a policy based on the given identifier and other
     * constraints. If there are multiple versions of the identified policy
     * that meet the version constraints, then the most recent version is
     * returned.
     *
     * @param identifier an identifier specifying some policy
     * @param type type of reference (policy or policySet) as identified by
     *             the fields in <code>PolicyReference</code>
     * @param constraints any optional constraints on the version of the
     *                    referenced policy (this will never be null, but
     *                    it may impose no constraints, and in fact will
     *                    never impose constraints when used from a pre-2.0
     *                    XACML policy)
     */
    public Policy getPolicy(String identifier, int type,
                                    VersionConstraints constraints) {
        TreeSet set = (TreeSet)(policies.get(identifier));
        
        // if we don't know about this identifier then there's nothing to do
        if (set == null) {
            return null;
        }

        // walk through the set starting with the most recent version, looking
        // for a match until we exhaust all known versions
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Policy policy = (Policy)(it.next());
            if (constraints.meetsConstraint(policy.getVersion())) {
                // we found a valid version, so see if it's the right kind,
                // and if it is then we return it
                if (type == PolicyReference.POLICY_REFERENCE) {
                    if (policy instanceof Policy) {
                        return policy;
                    }
                } 
//                else {
//                    if (policy instanceof PolicySet)
//                        return policy;
//                }
            }
        }

        // we didn't find a match
        return null;
    }

    /**
     * A <code>Comparator</code> that is used within this class to maintain
     * ordering amongst different versions of the same policy. Note that
     * it actually maintains reverse-ordering, since we want to traverse the
     * sets in decreasing, not increasing order.
     * <p>
     * Note that this comparator is only used when there are multiple versions
     * of the same policy, which in practice will probably happen far less
     * (from this class' point of view) than additions or fetches.
     */
    class VersionComparator implements Comparator {
        public int compare(Object o1, Object o2) {
        	if(o1 instanceof Policy && o2 instanceof Policy) {
        		return comparePolicy((Policy)o1, (Policy)o2);
        	} else if(o1 instanceof PolicySet && o2 instanceof PolicySet) {
        		return comparePolicySet((PolicySet)o1, (PolicySet)o2);
        	}
        	return 0;
        }
        private int comparePolicy(Policy o1, Policy o2) {

            // we swap the parameters so that sorting goes largest to smallest
            String v1 = o2.getVersion();
            String v2 = o1.getVersion();

            // do a quick check to see if the strings are equal (note that
            // even if the strings aren't equal, the versions can still
            // be equal)
            if (v1.equals(v2))
                return 0;

            // setup tokenizers, and walk through both strings one set of
            // numeric values at a time
            StringTokenizer tok1 = new StringTokenizer(v1, ".");
            StringTokenizer tok2 = new StringTokenizer(v2, ".");

            while (tok1.hasMoreTokens()) {
                // if there's nothing left in tok2, then v1 is bigger
                if (! tok2.hasMoreTokens()) {
                    return 1;
                }

                // get the next elements in the version, convert to numbers,
                // and compare them (continuing with the loop only if the
                // two values were equal)
                int num1 = Integer.parseInt(tok1.nextToken());
                int num2 = Integer.parseInt(tok2.nextToken());

                if (num1 > num2) {
                    return 1;
                }

                if (num1 < num2) {
                    return -1;
                }
            }

            // if there's still something left in tok2, then it's bigger
            if (tok2.hasMoreTokens()) {
                return -1;
            }

            // if we got here it means both versions had the same number of
            // elements and all the elements were equal, so the versions
            // are in fact equal
            return 0;
        
        }
        private int comparePolicySet(PolicySet o1, PolicySet o2) {

            // we swap the parameters so that sorting goes largest to smallest
            String v1 = o2.getVersion();
            String v2 = o1.getVersion();

            // do a quick check to see if the strings are equal (note that
            // even if the strings aren't equal, the versions can still
            // be equal)
            if (v1.equals(v2)) {
                return 0;
            }

            // setup tokenizers, and walk through both strings one set of
            // numeric values at a time
            StringTokenizer tok1 = new StringTokenizer(v1, ".");
            StringTokenizer tok2 = new StringTokenizer(v2, ".");

            while (tok1.hasMoreTokens()) {
                // if there's nothing left in tok2, then v1 is bigger
                if (! tok2.hasMoreTokens()) {
                	return 1;	
                }                    

                // get the next elements in the version, convert to numbers,
                // and compare them (continuing with the loop only if the
                // two values were equal)
                int num1 = Integer.parseInt(tok1.nextToken());
                int num2 = Integer.parseInt(tok2.nextToken());

                if (num1 > num2) {
                    return 1;
                }

                if (num1 < num2) {
                    return -1;
                }
            }

            // if there's still something left in tok2, then it's bigger
            if (tok2.hasMoreTokens()) {
                return -1;
            }

            // if we got here it means both versions had the same number of
            // elements and all the elements were equal, so the versions
            // are in fact equal
            return 0;
        
        }
    }

	public int getNbPolicies() {
		return this.policies.size();
	}

}
