/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetMatch;
import com.sun.xacml.TargetSection;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * @author Romain Ferrari
 *
 */
public class AnyOf extends AnyOfType {
	
	/** 
    *
    */
   private List<AllOf> allOf;

   private static Log LOGGER = LogFactory.getLog(AnyOf.class);


   /** 
    * Constructor that creates a new <code>AnyOfSelection</code> based on the given elements.
    *
    * @param allOfSelections a <code>List</code> of <code>AllOfSelection</code> elements
    */
   public AnyOf(List<AllOf> allOfType) {
       if (allOfType == null) {
           this.allOf =new ArrayList<AllOf>();
       } else {
           this.allOf = allOfType;
       }
   }

	/** 
     * creates a <code>AnyOfSelection</code> based on its DOM node.
     *
     * @param root the node to parse for the AnyOfSelection
     * @param metaData meta-date associated with the policy
     *
     * @return a new <code>AnyOfSelection</code> constructed by parsing
     *
     * @throws ParsingException if the DOM node is invalid
     */
    public static AnyOf getInstance(Node root, PolicyMetaData metaData)
            throws ParsingException {
        List<AllOf> allOf = new ArrayList<AllOf>();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("AllOf".equals(DOMHelper.getLocalName(child))) {
                allOf.add(AllOf.getInstance(child, metaData));
            }   
        }   

        if(allOf.isEmpty()){
            throw new ParsingException("AnyOf must contain at least one AllOf");    
        }   

        return new AnyOf(allOf);
    }

	public static List<TargetSection> getTargetSection(Node child,
			PolicyMetaData metaData) {
		return AllOf.getTargetSection(child, metaData);
	}

}
