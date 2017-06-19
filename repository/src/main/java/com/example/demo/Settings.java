package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "settings")
public class Settings {
	private List<IssueSettings> issues;

	public List<IssueSettings> getIssues() {
		return issues;
	}
	public void setIssues(List<IssueSettings> issues) {
		this.issues = issues;
	}
}
