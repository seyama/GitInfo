package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class IssuesSetting {
	public static final String ISSUE_TYPE_GITHUB = "github";
	public static final String ISSUE_TYPE_GITBUCKET = "gitbucket";

	private String filter;
	private String state;
	private String labels;
	private String sort;
	private String direction;
	private String since;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getSince() {
		return since;
	}

	public void setSince(String since) {
		this.since = since;
	}

}
