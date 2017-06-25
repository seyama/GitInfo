package com.example.demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "settings")
public class Settings {
	private String issuesListFilePath;

	private String pullsListFilePath;

	private String errorsListFilePath;

	private String issuesSaveDir;

	private String pullsSaveDir;

	private List<IssueSettings> issues;

	public String getIssuesListFilePath() {
		return issuesListFilePath;
	}

	public void setIssuesListFilePath(String issuesListFilePath) {
		this.issuesListFilePath = issuesListFilePath;
	}

	public String getPullsListFilePath() {
		return pullsListFilePath;
	}

	public void setPullsListFilePath(String pullsListFilePath) {
		this.pullsListFilePath = pullsListFilePath;
	}

	public String getErrorsListFilePath() {
		return errorsListFilePath;
	}

	public void setErrorsListFilePath(String errorsListFilePath) {
		this.errorsListFilePath = errorsListFilePath;
	}

	public String getIssuesSaveDir() {
		return issuesSaveDir;
	}

	public void setIssuesSaveDir(String issuesSaveDir) {
		this.issuesSaveDir = issuesSaveDir;
	}

	public String getPullsSaveDir() {
		return pullsSaveDir;
	}

	public void setPullsSaveDir(String pullsSaveDir) {
		this.pullsSaveDir = pullsSaveDir;
	}

	public List<IssueSettings> getIssues() {
		return issues;
	}

	public void setIssues(List<IssueSettings> issues) {
		this.issues = issues;
	}
}
