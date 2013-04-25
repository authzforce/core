/**
 * 
 */
package com.sun.xacml;

import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionsType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Romain Ferrari
 * 
 */
public class ObligationExpressions extends ObligationExpressionsType {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.jaxb2_commons.lang.Equals#equals(java.lang.Object,
	 * org.apache.commons.lang.builder.EqualsBuilder)
	 */
	@Override
	public void equals(Object object, EqualsBuilder equalsBuilder) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.jaxb2_commons.lang.HashCode#hashCode(org.apache.commons.lang
	 * .builder.HashCodeBuilder)
	 */
	@Override
	public void hashCode(HashCodeBuilder hashCodeBuilder) {
		// TODO Auto-generated method stub

	}

	public static ObligationExpressionsType getInstance(
			Set<ObligationExpressionType> obligations) {
		ObligationExpressionsType oblgExpr = new ObligationExpressionsType();
		oblgExpr.getObligationExpression().addAll(obligations);

		return oblgExpr;
	}

	public static ObligationExpressionsType getInstance(Node root) {
		NodeList nodes = root.getChildNodes();
		ObligationExpressionsType obligationExpressions = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("ObligationExpression")) {
				JAXBElement<ObligationExpressionsType> match = null;
				try {
					JAXBContext jc = JAXBContext
							.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					match = (JAXBElement<ObligationExpressionsType>) u
							.unmarshal(root);
				} catch (Exception e) {
					System.err.println(e);
				}

				obligationExpressions = match.getValue();
			}
		}
		
		return obligationExpressions;
	}

}
