/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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
package com.sun.xacml.attr;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sun.xacml.attr.xacmlv3.AttributeValue;


/**
 * Represents a bag used in the XACML spec as return values from functions
 * and designators/selectors that provide more than one value. All values in
 * the bag are of the same type, and the bag may be empty. The bag is
 * immutable, although its contents may not be.
 * <p>
 * NOTE: This is the one standard attribute type that can't be created from
 * the factory, since you can't have this in an XML block (it is used only
 * in return values & dynamic inputs). I think this is right, but we may need
 * to add some functionality to let this go into the factory.
 *
 * @since 1.0
 * @author Seth Proctor
 * @author Steve Hanna
 */
public class BagAttribute extends AttributeValue
{

    // The Collection of AttributeValues that this object encapsulates
    private Collection bag;

    /**
     * Creates a new <code>BagAttribute</code> that represents
     * the <code>Collection</code> of <code>AttributeValue</code>s supplied.
     * If the set is null or empty, then the new bag is empty.
     *
     * @param type the data type of all the attributes in the set
     * @param bag a <code>Collection</code> of <code>AttributeValue</code>s
     */
    public BagAttribute(URI type, Collection bag) {
        super(type);

        if (type == null) {
            throw new IllegalArgumentException("Bags require a non-null " +
                                               "type be provided");
        }

        // see if the bag is empty/null
        if ((bag == null) || (bag.size() == 0)) {
            // empty bag
            this.bag = new ArrayList();
        } else {
            // go through the collection to make sure it's a valid bag
            Iterator it = bag.iterator();
            
            while (it.hasNext()) {
                AttributeValue attr = (AttributeValue)(it.next());
                // a bag cannot contain other bags, so make sure that each
                // value isn't actually another bag
                // FIXME: Find a way to check that there isn't another bag inside
//                if(attr.getContent().size() > 0) {
//                	throw new IllegalArgumentException("bags cannot contain " +
//                            "other bags");
//                }
                // make sure that they're all the same type
                if (! this.dataType.equals(attr.getDataType())) {
                    throw new
                        IllegalArgumentException("Bag items must all be of " +
                                                 "the same type");
                }
                for (Serializable content : attr.getContent()) {
					this.getContent().add(content);
				}
            }

            // if we get here, then they're all the same type
            this.bag = bag;            
        }
    }

    /**
     * Overrides the default method to always return true.
     *
     * @return a value of true
     */
    public boolean isBag() {
        return true;
    }

    /**
     * Convenience function that returns a bag with no elements
     *
     * @param type the types contained in the bag
     *
     * @return a new empty bag
     */
    public static BagAttribute createEmptyBag(URI type) {
        return new BagAttribute(type, null);
    }

    /**
     * A convenience function that returns whether or not the bag is empty
     * (ie, whether or not the size of the bag is zero)
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
     * (value==null ? v==null : value.equals(v)). Note that this will only
     * work correctly if the <code>AttributeValue</code> has overridden the
     * <code>equals</code> method.
     *
     * @param value the value to look for
     *
     * @return true if the value is in the bag
     */
    public boolean contains(AttributeValue value) {
        return bag.contains(value);
    }

    /**
     * Returns true if this bag contains all of the values of the specified bag.
     * Note that this will only work correctly if the
     * <code>AttributeValue</code> type contained in the bag has overridden
     * the <code>equals</code> method.
     *
     * @param bag the bag to compare
     *
     * @return true if the input is a subset of this bag
     */
    public boolean containsAll(BagAttribute bag) {
        return this.bag.containsAll(bag.bag);
    }


    /**
     * Returns an iterator over te 
     */
    public Iterator iterator() {
        return new ImmutableIterator(bag.iterator());
    }

    /**
     * This is a version of Iterator that overrides the <code>remove</code>
     * method so that items can't be taken out of the bag.
     */
    private class ImmutableIterator implements Iterator {

        // the iterator we're wrapping
        private Iterator iterator;

        /**
         * Create a new ImmutableIterator
         */
        public ImmutableIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        
        /**
         * Standard hasNext method
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Standard next method
         */
        public Object next() throws NoSuchElementException {
            return iterator.next();
        }

        /**
         * Makes sure that no one can remove any elements from the bag
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
    }

    /**
     * Because a bag cannot be included in a request/response or a 
     * policy, this will always throw an
     * <code>UnsupportedOperationException</code>.
     */
    public String encode() {
        throw new UnsupportedOperationException("Bags cannot be encoded");
    }

    public Collection getValue() {
        return bag;
    }

}
