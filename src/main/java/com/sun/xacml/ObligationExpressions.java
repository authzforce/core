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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Romain Ferrari
 * 
 */
public class ObligationExpressions extends ObligationExpressionsType {
	
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(ObligationExpressions.class);

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
					LOGGER.error(e);
				}

				obligationExpressions = match.getValue();
			}
		}
		
		return obligationExpressions;
	}

}
