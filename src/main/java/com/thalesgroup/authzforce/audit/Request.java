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
