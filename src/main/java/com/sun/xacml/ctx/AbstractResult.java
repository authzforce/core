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
