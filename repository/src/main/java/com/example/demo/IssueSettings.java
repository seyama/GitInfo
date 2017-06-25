package com.example.demo;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class IssueSettings {
	public static final String ISSUE_TYPE_GITHUB = "github";
	public static final String ISSUE_TYPE_GITBUCKET = "gitbucket";

	private String type;
	private String apiRoot;
	private String token;
	private String org;
	private String filter;
	private String state;
	private String labels;
	private String sort;
	private String direction;
	private String since;
	private int sleepSecond;
	private String identifierPrefix;
	private String identifierPostfix;
	private List<String> repositoryNames;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getApiRoot() {
		return apiRoot;
	}

	public void setApiRoot(String apiRoot) {
		this.apiRoot = apiRoot;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

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

	public String getIdentifierPrefix() {
		return identifierPrefix;
	}

	public void setIdentifierPrefix(String identifierPrefix) {
		this.identifierPrefix = identifierPrefix;
	}

	public String getIdentifierPostfix() {
		return identifierPostfix;
	}

	public void setIdentifierPostfix(String identifierPostfix) {
		this.identifierPostfix = identifierPostfix;
	}

	public int getSleepSecond() {
		return sleepSecond;
	}

	public void setSleepSecond(int sleepSecond) {
		this.sleepSecond = sleepSecond;
	}

	public List<String> getRepositoryNames() {
		return repositoryNames;
	}

	public void setRepositoryNames(List<String> repositoryNames) {
		this.repositoryNames = repositoryNames;
	}
}
