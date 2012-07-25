
/*
 * @(#)AttributeFinder.java
 *
 * Copyright 2003-2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml.finder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeSelector;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.thalesgroup.authzforce.audit.AttributesResolved;
import com.thalesgroup.authzforce.audit.AuditLogs;


/**
 * This class is used by the PDP to find attribute values that weren't
 * originally supplied in the request. It can be called with the data supplied
 * in {@link AttributeDesignator}s or {@link AttributeSelector}s.
 * Because the modules in this finder may themselves need attribute data
 * to search for attribute data, it's possible that the modules will look
 * for values in the {@link EvaluationCtx}, which may in turn result
 * in the invocation of this finder again, so module writers need to be
 * careful about how they build their modules.
 * <p>
 * Note that unlike the PolicyFinder, this class doesn't always need to
 * use every module it has to find a value. The ordering is maintained,
 * however, so it will always start with the first module, and proceed
 * in order until it finds a value or runs out of modules.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class AttributeFinder
{

    // the list of all modules
    private List allModules;

    //
    private List designatorModules;

    //
    private List selectorModules;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(AttributeFinder.class.getName());

    private static final org.apache.log4j.Logger log4jLogger = 
			org.apache.log4j.Logger.getLogger(AttributeFinder.class);    
    
    /**
     * Default constructor.
     */
    public AttributeFinder() {
        allModules = new ArrayList();
        designatorModules = new ArrayList();
        selectorModules = new ArrayList();
    }

    /**
     * Returns the ordered list of
     * {@link AttributeFinderModule}s used by this class to find
     * attribute values.
     *
     * @return a list of <code>AttributeFinderModule</code>s
     */
    public List getModules() {
        return new ArrayList(allModules);
    }

    /**
     * Sets the ordered list of
     * {@link AttributeFinderModule}s used by this class to find
     * attribute values. The ordering will be maintained.
     *
     * @param modules a list of
     *                <code>AttributeFinderModule</code>s
     */
    public void setModules(List modules) {
        Iterator it = modules.iterator();

        allModules = new ArrayList(modules);
        designatorModules = new ArrayList();
        selectorModules = new ArrayList();

        while (it.hasNext()) {
            AttributeFinderModule module = (AttributeFinderModule)(it.next());
            
            if (module.isDesignatorSupported())
                designatorModules.add(module);

            if (module.isSelectorSupported())
                selectorModules.add(module);
        }
    }

    /**
     * Tries to find attribute values based on the given designator data.
     * The result, if successful, will always contain a
     * {@link BagAttribute}, even if only one value was found. If no
     * values were found, but no other error occurred, an empty bag is
     * returned.
     *
     * @param attributeType the datatype of the attributes to find
     * @param attributeId the identifier of the attributes to find
     * @param issuer the issuer of the attributes, or null if unspecified
     * @param subjectCategory the category of the attribute if the
     *                        designatorType is {@link AttributeDesignator#SUBJECT_TARGET}, otherwise null
     * @param context the representation of the request data
     * @param designatorType the type of designator as named by the *_TARGET
     *                       fields in {@link AttributeDesignator}
     *
     * @return the result of attribute retrieval, which will be a bag of
     *         attributes or an error
     */
    public EvaluationResult findAttribute(URI attributeType, URI attributeId,
                                          URI issuer, URI subjectCategory,
                                          EvaluationCtx context,
                                          int designatorType) {
        Iterator it = designatorModules.iterator();       

        
        // go through each module in order
        while (it.hasNext()) {
            AttributeFinderModule module = (AttributeFinderModule)(it.next());
            
            // see if the module supports this type
            Set types = module.getSupportedDesignatorTypes();
            if ((types == null) || (types.
                                    contains(new Integer(designatorType)))) {
                // see if the module can find an attribute value
                EvaluationResult result =
                    module.findAttribute(attributeType, attributeId, issuer,
                                         subjectCategory, context,
                                         designatorType);

                // if there was an error, we stop right away
                if (result.indeterminate()) {
                    if (logger.isLoggable(Level.INFO))
                        logger.info("Error while trying to resolve values: " +
                                    result.getStatus().getMessage());
                    return result;
                }
                
                log4jLogger.debug("Finish to resolv attribute value for attribute: "+attributeId +" values are : ");
                AuditLogs audit = AuditLogs.getInstance();
                /**
                 * Cache management (Deleting cache)
                 * @author romain.ferrari[AT]thalesgroup.com
                 */
                module.invalidateCache();

                // if the result wasn't empty, then return the result
                BagAttribute bag = (BagAttribute)(result.getAttributeValue());
                BagAttribute auditBag = bag;
                Iterator iter = auditBag.getValue().iterator();
                AttributesResolved attrResolv = null;
                /**
                 * Parsing for auditlog (FIX: Romain Ferrari)
                 * @author romain.guignard[AT]thalesgroup.com
                 */
				while (iter.hasNext()){
					String attrval = iter.next().toString();
					attrResolv = new AttributesResolved();
					try {
						if (attributeType.equals(new URI("http://www.w3.org/2001/XMLSchema#string"))) {
							attrResolv.setAttributeValue(attrval.split(":")[1]);
						} else if (attributeType.equals(new URI("http://www.w3.org/2001/XMLSchema#integer"))) {
							attrResolv.setAttributeValue(attrval.split("@")[1]);
						}
					} catch (URISyntaxException e) {
						log4jLogger.fatal("Error while building URI");
						log4jLogger.fatal(e.getLocalizedMessage());
					}
					attrResolv.setAttributeId(attributeId);
					audit.getAttrResolv().add(attrResolv);	
					log4jLogger.debug("Val : "+attrval);
				}
				/**
				 * End of parsing for auditlog (NOTE: Romain Guignard)
				 */
                if (! bag.isEmpty())
                    return result;
            }            
        }
        
        
        // if we got here then there were no errors but there were also no
        // matches, so we have to return an empty bag
        if (logger.isLoggable(Level.INFO))
            logger.info("Failed to resolve any values for " +
                        attributeId.toString());

        return new EvaluationResult(BagAttribute.
                                    createEmptyBag(attributeType));
    }

    /**
     * Tries to find attribute values based on the given selector data.
     * The result, if successful, must always contain a
     * {@link BagAttribute}, even if only one value was found. If no
     * values were found, but no other error occurred, an empty bag is
     * returned.
     *
     * @param contextPath the XPath expression to search against
     * @param namespaceNode the DOM node defining namespace mappings to use,
     *                      or null if mappings come from the context root
     * @param attributeType the datatype of the attributes to find
     * @param context the representation of the request data
     * @param xpathVersion the XPath version to use
     *
     * @return the result of attribute retrieval, which will be a bag of
     *         attributes or an error
     */
    public EvaluationResult findAttribute(String contextPath,
                                          Node namespaceNode,
                                          URI attributeType,
                                          EvaluationCtx context,
                                          String xpathVersion) {
        Iterator it = selectorModules.iterator();

        // go through each module in order
        while (it.hasNext()) {
            AttributeFinderModule module = (AttributeFinderModule)(it.next());
            
            // see if the module can find an attribute value
            EvaluationResult result =
                module.findAttribute(contextPath, namespaceNode, attributeType,
                                     context, xpathVersion);

            // if there was an error, we stop right away
            if (result.indeterminate()) {
                if (logger.isLoggable(Level.INFO))
                    logger.info("Error while trying to resolve values: " +
                                result.getStatus().getMessage());
                return result;
            }

            // if the result wasn't empty, then return the result
            BagAttribute bag = (BagAttribute)(result.getAttributeValue());
            if (! bag.isEmpty())
                return result;
        }

        // if we got here then there were no errors but there were also no
        // matches, so we have to return an empty bag
        if (logger.isLoggable(Level.INFO))
            logger.info("Failed to resolve any values for " + contextPath);

        return new EvaluationResult(BagAttribute.
                                    createEmptyBag(attributeType));
    }

}
