/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Romain Ferrari
 *
 */
public class AdviceExpressions extends AdviceExpressionsType {
	
	public static AdviceExpressionsType getInstance(Set<AdviceExpressionType> advice) {
		AdviceExpressionsType adviceExpr = new AdviceExpressionsType();
		adviceExpr.getAdviceExpression().addAll(advice);
		
		return adviceExpr;
	}

	/* (non-Javadoc)
	 * @see org.jvnet.jaxb2_commons.lang.Equals#equals(java.lang.Object, org.apache.commons.lang.builder.EqualsBuilder)
	 */
	@Override
	public void equals(Object object, EqualsBuilder equalsBuilder) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jvnet.jaxb2_commons.lang.HashCode#hashCode(org.apache.commons.lang.builder.HashCodeBuilder)
	 */
	@Override
	public void hashCode(HashCodeBuilder hashCodeBuilder) {
		// TODO Auto-generated method stub

	}

}
