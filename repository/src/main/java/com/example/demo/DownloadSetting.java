package com.example.demo;

import java.util.List;

public class DownloadSetting {
	private String type;
	private String apiRoot;
	private String token;
	private String org;
	private String identifierPrefix;
	private String identifierPostfix;
	private int sleepSecond;
	private List<String> repositoryNames;
	private List<IssuesSetting> issuesSettings;
	private List<PullsSetting> pullsSettings;

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

	public List<IssuesSetting> getIssuesSettings() {
		return issuesSettings;
	}

	public void setIssuesSettings(List<IssuesSetting> issuesSettings) {
		this.issuesSettings = issuesSettings;
	}

	public List<PullsSetting> getPullsSettings() {
		return pullsSettings;
	}

	public void setPullsSettings(List<PullsSetting> pullsSettings) {
		this.pullsSettings = pullsSettings;
	}

}
