/**
 * Copyright (C) 2011-2014 Thales Services - ThereSIS - All rights reserved.
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
