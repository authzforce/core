/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.ctx;

import java.util.List;

public abstract class AbstractResult {
	
	/** 
     * The decision to permit the request
     */
    public static final int DECISION_PERMIT = 0;

    /** 
     * The decision to deny the request
     */
    public static final int DECISION_DENY = 1;

    /** 
     * The decision that a decision about the request cannot be made
     */
    public static final int DECISION_INDETERMINATE = 2;

    /** 
     * The decision that nothing applied to us
     */
    public static final int DECISION_NOT_APPLICABLE = 3;

    /** 
     * The decision that a decision about the request cannot be made
     */
    public static final int DECISION_INDETERMINATE_DENY = 4;

    /** 
     * The decision that a decision about the request cannot be made
     */
    public static final int DECISION_INDETERMINATE_PERMIT = 5;

    /** 
     * The decision that a decision about the request cannot be made
     */
    public static final int DECISION_INDETERMINATE_DENY_OR_PERMIT = 6;
    
    /** 
     * string versions of the 4 Decision types used for encoding
     */
    public static final String[] DECISIONS = { "Permit", "Deny", "Indeterminate", "NotApplicable"};

    /** 
     * the decision effect
     */
    protected int decision = -1; 

    /** 
     * the status data
     */
    protected Status status = null;

    /** 
     * XACML version
     */
    protected int xacmlVersion;

	/**
	 * @return the decision
	 */
	public int getDecision() {
		return decision;
	}

	/**
	 * @param decision the decision to set
	 */
	public void setDecision(int decision) {
		this.decision = decision;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the xacmlVersion
	 */
	public int getXacmlVersion() {
		return xacmlVersion;
	}

	/**
	 * @param xacmlVersion the xacmlVersion to set
	 */
	public void setXacmlVersion(int xacmlVersion) {
		this.xacmlVersion = xacmlVersion;
	}
	
	/**
     * Constructs a <code>AbstractResult</code> object with decision status data, obligations, advices
     *  and evaluation ctx
     *
     * @param decision the decision effect to include in this result. This must be one of the four
     *            fields in this class.
     * @param status the <code>Status</code> to include in this result
     * @param version XACML version
     * @throws IllegalArgumentException if decision is not valid
     */
    public AbstractResult(int decision, Status status, int version) throws IllegalArgumentException {

        this.xacmlVersion = version;

        // check that decision is valid
        if ((decision != DECISION_PERMIT) && (decision != DECISION_DENY)
                && (decision != DECISION_INDETERMINATE) && (decision != DECISION_NOT_APPLICABLE)
                && (decision != DECISION_INDETERMINATE_DENY) && (decision != DECISION_INDETERMINATE_PERMIT)
                && (decision != DECISION_INDETERMINATE_DENY_OR_PERMIT)) {
            throw new IllegalArgumentException("invalid decision value");
        }

        this.decision = decision;
        if (status == null){
            this.status = Status.getOkInstance();
        } else {
            this.status = status;
        }
    }
}
