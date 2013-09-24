package com.thalesgroup.authzforce.audit;

import org.apache.log4j.Level;

public class AuditLevel extends Level {

	
	private static final long serialVersionUID = 1L;

	/**
	 * Value of my trace level. This value is lesser than
	 * {@link org.apache.log4j.Priority#FATAL_INT}
	 */
	public static final int AUDIT_INT = FATAL_INT - 10000;

	/**
	 * {@link Level} representing my log level
	 */
	public static final Level AUDIT = new AuditLevel(AUDIT_INT,
			"AUDIT", 7);

	/**
	 * Constructor
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	protected AuditLevel(int arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);

	}

	
}
