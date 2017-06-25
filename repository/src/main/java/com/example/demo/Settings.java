package com.example.demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "settings")
public class Settings {
	private String saveRoot;

	private String dataDirnamePrefix;

	private String fileInfoDirname;

	private String issuesSaveDirname;

	private String pullsSaveDirname;

	private String issuesListFilename;

	private String pullsListFilename;

	private String errorsListFilename;

	private List<DownloadSetting> downloadSettings;

	public String getSaveRoot() {
		return saveRoot;
	}

	public void setSaveRoot(String saveRoot) {
		this.saveRoot = saveRoot;
	}

	public String getDataDirnamePrefix() {
		return dataDirnamePrefix;
	}

	public void setDataDirnamePrefix(String dataDirnamePrefix) {
		this.dataDirnamePrefix = dataDirnamePrefix;
	}

	public String getFileInfoDirname() {
		return fileInfoDirname;
	}

	public void setFileInfoDirname(String fileInfoDirname) {
		this.fileInfoDirname = fileInfoDirname;
	}

	public String getIssuesSaveDirname() {
		return issuesSaveDirname;
	}

	public void setIssuesSaveDirname(String issuesSaveDirname) {
		this.issuesSaveDirname = issuesSaveDirname;
	}

	public String getPullsSaveDirname() {
		return pullsSaveDirname;
	}

	public void setPullsSaveDirname(String pullsSaveDirname) {
		this.pullsSaveDirname = pullsSaveDirname;
	}

	public String getIssuesListFilename() {
		return issuesListFilename;
	}

	public void setIssuesListFilename(String issuesListFilename) {
		this.issuesListFilename = issuesListFilename;
	}

	public String getPullsListFilename() {
		return pullsListFilename;
	}

	public void setPullsListFilename(String pullsListFilename) {
		this.pullsListFilename = pullsListFilename;
	}

	public String getErrorsListFilename() {
		return errorsListFilename;
	}

	public void setErrorsListFilename(String errorsListFilename) {
		this.errorsListFilename = errorsListFilename;
	}

	public List<DownloadSetting> getDownloadSettings() {
		return downloadSettings;
	}

	public void setDownloadSettings(List<DownloadSetting> downloadSettings) {
		this.downloadSettings = downloadSettings;
	}

}
