/**
 * Copyright (C) 2011-2014 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Basic XML schema parsing error handler
 * 
 *
 */
public class SchemaParsingErrorHandler implements ErrorHandler
{
	
	@Override
	public void warning(SAXParseException exception) throws SAXException
	{
		throw exception;		
	}

	@Override
	public void error(SAXParseException exception) throws SAXException
	{
		throw exception;		
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException
	{
		throw exception;		
	}
}
