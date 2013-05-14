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
package com.sun.xacml;

import java.util.Arrays;


/**
 * Provides flexible indenting for XML encoding.  This class generates
 * strings of spaces to be prepended to lines of XML.  The strings are
 * formed according to a specified indent width and the given depth.
 *
 * @since 1.0
 * @author Marco Barreno
 * @author Seth Proctor
 */
public class Indenter
{

    /**
     * The default indentation width
     */
    public static final int DEFAULT_WIDTH = 2;

    // The width of one indentation level
    private int width;

    // the current depth
    private int depth;

    /**
     * Constructs an <code>Indenter</code> with the default indent
     * width.
     */
    public Indenter() {
        this(DEFAULT_WIDTH);
    }

    /**
     * Constructs an <code>Indenter</code> with a user-supplied indent
     * width.
     *
     * @param userWidth the number of spaces to use for each indentation level
     */
    public Indenter(int userWidth) {
        width = userWidth;
        depth = 0;
    }

    /**
     * Move in one width.
     */
    public void in() {
        depth += width;
    }

    /**
     * Move out one width.
     */
    public void out() {
        depth -= width;
    }

    /**
     * Create a <code>String</code> of spaces for indentation based on the
     * current depth.
     *
     * @return an indent string to prepend to lines of XML
     */
    public String makeString() {
        // Return quickly if no indenting
        if (depth <= 0) {
            return "";
        }

        // Make a char array and fill it with spaces
        char[] array = new char[depth];
        Arrays.fill(array, ' ');

        // Now return a string built from that char array
        return new String(array);
    }

}
