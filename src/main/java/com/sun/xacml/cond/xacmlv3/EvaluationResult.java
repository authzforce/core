/**
 * Copyright (C) 2011-2014 Thales Services SAS - All rights reserved.
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
package com.sun.xacml.cond.xacmlv3;

import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;


/**
 * This is used in cases where a normal result is some AttributeValue, but
 * if an attribute couldn't be resolved (or some other problem occurred), 
 * then a Status object needs to be returned instead. This is used instead of
 * throwing an exception for performance, but mainly because failure to resolve
 * an attribute is not an error case for the code, merely for the evaluation,
 * and represents normal operation. Separate exception types will be added
 * later to represent errors in pdp operation.
 */
public class EvaluationResult
{
    
    //
    private boolean wasInd;
    private AttributeValue value;
    private Status status;

    /**
     * Single instances of EvaluationResults with false and true
     * BooleanAttributes in them. This avoids the need to create
     * new objects when performing boolean operations, which we
     * do a lot of.
     */
    private static EvaluationResult falseBooleanResult;
    private static EvaluationResult trueBooleanResult;

    /**
     * Constructor that creates an <code>EvaluationResult</code> containing
     * a single <code>AttributeValue</code>
     *
     * @param value the attribute value
     */
    public EvaluationResult(AttributeValue value) {
        wasInd = false;
        this.value = value;
        this.status = null;
    }
    
    /**
     * Constructor that creates an <code>EvaluationResult</code> of
     * Indeterminate, including Status data.
     *
     * @param status the error information
     */
    public EvaluationResult(Status status) {
        wasInd = true;
        this.value = null;
        this.status = status;
    }

	/**
     * Returns true if the result was indeterminate
     *
     * @return true if there was an error
     */
    public boolean indeterminate() {
        return wasInd;
    }

    /**
     * Returns the attribute value, or null if there was an error
     *
     * @return the attribute value or null
     */
    public AttributeValue getAttributeValue() {
        return value;
    }

    /**
     * Returns the status if there was an error, or null it no error occurred
     *
     * @return the status or null
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns an <code>EvaluationResult</code> that represents
     * the boolean value provided.
     *
     * @param value a boolean representing the desired value
     * @return an <code>EvaluationResult</code> representing the
     *         appropriate value
     */
    public static EvaluationResult getInstance(boolean value) {
        if (value) {
            return getTrueInstance();
        }
        
		return getFalseInstance();
    }

    /**
     * Returns an <code>EvaluationResult</code> that represents
     * a false value.
     *
     * @return an <code>EvaluationResult</code> representing a
     *         false value
     */
    public static EvaluationResult getFalseInstance() {
        if (falseBooleanResult == null) {
            falseBooleanResult =
                new EvaluationResult(BooleanAttribute.getFalseInstance());
        }
        return falseBooleanResult;
    }

    /**
     * Returns an <code>EvaluationResult</code> that represents
     * a true value.
     *
     * @return an <code>EvaluationResult</code> representing a
     *         true value
     */
    public static EvaluationResult getTrueInstance() {
        if (trueBooleanResult == null) {
            trueBooleanResult =
                new EvaluationResult(BooleanAttribute.getTrueInstance());
        }
        return trueBooleanResult;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.wasInd ? 1 : 0);
        hash = 53 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 53 * hash + (this.status != null ? this.status.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EvaluationResult other = (EvaluationResult) obj;
        if (this.wasInd != other.wasInd) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        if (this.status != other.status && (this.status == null || !this.status.equals(other.status))) {
            return false;
        }
        return true;
    }
}
