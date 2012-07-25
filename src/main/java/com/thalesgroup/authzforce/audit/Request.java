package com.thalesgroup.authzforce.audit;

import java.util.List;
import java.util.Map;

public class Request {
	
	List<Map<String,String>> Subjects;
	
	List<Map<String,String>> resources; 
	
	List<Map<String,String>> actions;
	
	List<Map<String,String>> environments;
		
	public List<Map<String, String>> getSubjects() {
		return Subjects;
	}

	public void setSubjects(List<Map<String, String>> subjects) {
		Subjects = subjects;
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
