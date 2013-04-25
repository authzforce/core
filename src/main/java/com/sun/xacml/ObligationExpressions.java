/**
 * 
 */
package com.sun.xacml;

import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Romain Ferrari
 *
 */
public class ObligationExpressions extends ObligationExpressionsType {

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

	public static ObligationExpressionsType getInstance(Set<ObligationExpressionType> obligations) {
		ObligationExpressionsType oblgExpr = new ObligationExpressionsType();
		oblgExpr.getObligationExpression().addAll(obligations);
		
		return oblgExpr;
	}

}
