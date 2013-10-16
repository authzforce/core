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
//package com.thalesgroup.authzforce.audit;
//
//import org.apache.log4j.Level;
//
//public class AuditLevel extends Level {
//
//	private static final long serialVersionUID = 1L;
//
//	/**
//	 * Value of my trace level. This value is lesser than
//	 * {@link org.apache.log4j.Priority#FATAL_INT}
//	 */
//	public static final int AUDIT_INT = FATAL_INT - 10000;
//
//	/**
//	 * {@link Level} representing my log level
//	 */
//	public static final Level AUDIT = new AuditLevel(AUDIT_INT,
//			"AUDIT", 7);
//
//	/**
//	 * Constructor
//	 * 
//	 * @param arg0
//	 * @param arg1
//	 * @param arg2
//	 */
//	protected AuditLevel(int arg0, String arg1, int arg2) {
//		super(arg0, arg1, arg2);
//
//	}
//
//	/**
//	 * Checks whether sArg is "AUDIT" level. If yes then returns
//	 * {@link AuditLevel#AUDIT}, else calls
//	 * {@link AuditLevel#toLevel(String, Level)} passing it {@link Level#DEBUG}
//	 * as the defaultLevel.
//	 * 
//	 * @see Level#toLevel(java.lang.String)
//	 * @see Level#toLevel(java.lang.String, org.apache.log4j.Level)
//	 * 
//	 */
//	public static Level toLevel(String sArg) {
//		if (sArg != null && sArg.toUpperCase().equals("AUDIT")) {
//			return AUDIT;
//		}
//		return (Level) toLevel(sArg, Level.DEBUG);
//	}
//
//	/**
//	 * Checks whether val is {@link AuditLevel#AUDIT_INT}. If yes then
//	 * returns {@link AuditLevel#AUDIT}, else calls
//	 * {@link AuditLevel#toLevel(int, Level)} passing it {@link Level#DEBUG} as
//	 * the defaultLevel
//	 * 
//	 * @see Level#toLevel(int)
//	 * @see Level#toLevel(int, org.apache.log4j.Level)
//	 * 
//	 */
//	public static Level toLevel(int val) {
//		if (val == AUDIT_INT) {
//			return AUDIT;
//		}
//		return (Level) toLevel(val, Level.DEBUG);
//	}
//
//	/**
//	 * Checks whether val is {@link AuditLevel#AUDIT_INT}. If yes then
//	 * returns {@link AuditLevel#AUDIT}, else calls
//	 * {@link Level#toLevel(int, org.apache.log4j.Level)}
//	 * 
//	 * @see Level#toLevel(int, org.apache.log4j.Level)
//	 */
//	public static Level toLevel(int val, Level defaultLevel) {
//		if (val == AUDIT_INT) {
//			return AUDIT;
//		}
//		return Level.toLevel(val, defaultLevel);
//	}
//
//	/**
//	 * Checks whether sArg is "MY_TRACE" level. If yes then returns
//	 * {@link AuditLevel#AUDIT}, else calls
//	 * {@link Level#toLevel(java.lang.String, org.apache.log4j.Level)}
//	 * 
//	 * @see Level#toLevel(java.lang.String, org.apache.log4j.Level)
//	 */
//	public static Level toLevel(String sArg, Level defaultLevel) {
//		if (sArg != null && sArg.toUpperCase().equals("AUDIT")) {
//			return AUDIT;
//		}
//		return Level.toLevel(sArg, defaultLevel);
//	}
//}
