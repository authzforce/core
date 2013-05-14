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
package com.sun.xacml.cond;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;


import java.util.List;


/**
 * Generic interface that is implemented by all objects that can be evaluated
 * directly (<code>AttributeDesignator</code>, <code>Apply</code>,
 * <code>AttributeValue</code>, etc.). As of version 2.0 several methods
 * were extracted to the new <code>Expression</code> super-interface.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public interface Evaluatable
{

    /**
     * Evaluates the object using the given context, and either returns an
     * error or a resulting value.
     *
     * @param context the representation of the request
     *
     * @return the result of evaluation
     */
    public EvaluationResult evaluate(EvaluationCtx context);

    /**
     * Tells whether evaluation will return a bag or a single value.
     *
     * @deprecated As of 2.0, you should use the <code>returnsBag</code>
     *             method from the super-interface <code>Expression</code>.
     *
     * @return true if evaluation will return a bag, false otherwise
     */
    public boolean evaluatesToBag();

    /**
     * Returns all children, in order, of this element in the Condition
     * tree, or en empty set if this element has no children. In XACML 1.x,
     * only the ApplyType ever has children.
     *
     * @return a <code>List</code> of <code>Evaluatable</code>s
     */
    public List getChildren();

}
