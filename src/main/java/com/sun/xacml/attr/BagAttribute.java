/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.attr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.sun.xacml.attr.xacmlv3.AttributeValue;

/**
 * Represents a bag used in the XACML spec as return values from functions and
 * designators/selectors that provide more than one value. All values in the bag
 * are of the same type, and the bag may be empty. The bag is immutable,
 * although its contents may not be.
 * <p>
 * NOTE: This is the one standard attribute type that can't be created from the
 * factory, since you can't have this in an XML block (it is used only in return
 * values & dynamic inputs). I think this is right, but we may need to add some
 * functionality to let this go into the factory.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Steve Hanna
 */
public class BagAttribute extends AttributeValue {

	// The Collection of AttributeValues that this object encapsulates
	private Collection<AttributeValue> bag;
	
	/**
	 * Creates a new <code>BagAttribute</code> that represents the
	 * <code>Collection</code> of <code>AttributeValue</code>s supplied. If the
	 * set is null or empty, then the new bag is empty.
	 * 
	 * @param type
	 *            the data type of all the attributes in the set
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML datatype, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param bag
	 *            a <code>Collection</code> of <code>AttributeValue</code>s
	 */
	public BagAttribute(String type, Collection<AttributeValue> bag) {
		super(type);

		if (type == null) {
			throw new IllegalArgumentException("Bags require a non-null "
					+ "type be provided");
		}

		// see if the bag is empty/null
		if ((bag == null) || (bag.size() == 0)) {
			// empty bag
			this.bag = new ArrayList<>();
		} else {
			// go through the collection to make sure it's a valid bag
			for (AttributeValue attr : bag) {
				// a bag cannot contain other bags, so make sure that each
				// value isn't actually another bag
				if (attr instanceof BagAttribute) {
					throw new IllegalArgumentException(
							"Invalid bag item type: " + BagAttribute.class
									+ ". A bag cannot contain bags.");
				}

				// make sure that they're all the same type
				if (!this.dataType.equals(attr.getDataType())) {
					throw new IllegalArgumentException(
							"Bag items must all be of the same type");
				}
				for (Serializable attrContent : attr.getContent()) {
					this.getContent().add(attrContent);
				}
			}
			// if we get here, then they're all the same type
			this.bag = Collections.unmodifiableCollection(bag);

		}
	}

	/**
	 * Overrides the default method to always return true.
	 * 
	 * @return a value of true
	 */
	@Override
	public boolean isBag() {
		return true;
	}

	/**
	 * Convenience function that returns a bag with no elements
	 * 
	 * @param type
	 *            the types contained in the bag
	 * 
	 * @return a new empty bag
	 */
	public static BagAttribute createEmptyBag(String type) {
		return new BagAttribute(type, null);
	}

	/**
	 * A convenience function that returns whether or not the bag is empty (ie,
	 * whether or not the size of the bag is zero)
	 * 
	 * @return whether or not the bag is empty
	 */
	public boolean isEmpty() {
		return (bag.size() == 0);
	}

	/**
	 * Returns the number of elements in this bag
	 * 
	 * @return the number of elements in this bag
	 */
	public int size() {
		return bag.size();
	}

	/**
	 * Returns true if this set contains the specified value. More formally,
	 * returns true if and only if this bag contains a value v such that
	 * (value==null ? v==null : value.equals(v)). Note that this will only work
	 * correctly if the <code>AttributeValue</code> has overridden the
	 * <code>equals</code> method.
	 * 
	 * @param value
	 *            the value to look for
	 * 
	 * @return true if the value is in the bag
	 */
	public boolean contains(AttributeValue value) {
		return bag.contains(value);
	}

	/**
	 * Returns true if this bag contains all of the values of the specified bag.
	 * Note that this will only work correctly if the
	 * <code>AttributeValue</code> type contained in the bag has overridden the
	 * <code>equals</code> method.
	 * 
	 * @param bagParam
	 *            the bag to compare
	 * 
	 * @return true if the input is a subset of this bag
	 */
	public boolean containsAll(BagAttribute bagParam) {
		return this.bag.containsAll(bagParam.bag);
	}

	/**
	 * Get bag values
	 * 
	 * @return values in the bag
	 */
	public Collection<AttributeValue> getValues() {
		return bag;
	}

}
