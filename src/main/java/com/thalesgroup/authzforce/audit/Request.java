/**
 * Copyright (C) 2011-2015 Thales Services SAS.
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
package com.thalesgroup.authzforce.audit;

import java.util.List;
import java.util.Map;

public class Request {
	
	protected List<Map<String,String>> subjects;
	
	protected List<Map<String,String>> resources; 
	
	protected List<Map<String,String>> actions;
	
	protected List<Map<String,String>> environments;
		
	public List<Map<String, String>> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<Map<String, String>> subjects) {
		this.subjects = subjects;
	}

	public List<Map<String, String>> getResources() {
		return resources;
	}

	public void setResources(List<Map<String, String>> resources) {
		this.resources = resources;
	}

	public List<Map<String, String>> getActions() {
		return actions;
	}

	public void setActions(List<Map<String, String>> actions) {
		this.actions = actions;
	}

	public List<Map<String, String>> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<Map<String, String>> environments) {
		this.environments = environments;
	}
}
