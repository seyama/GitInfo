package com.example.demo;

public class GitHubApi {
	/**
	 * Repository一覧取得URL組み立て
	 * 
	 * @param apiRoot
	 * @param org
	 * @return Repository一覧取得URL
	 */
	public static String makeRepositoriesUrl(String apiRoot, String org) {
		return apiRoot + "/orgs/" + org + "/repos";
	}

	/**
	 * Issues取得URL組み立て
	 * 
	 * @param apiRoot
	 * @param org
	 * @return Issue一覧取得URL
	 */
	public static String makeIssuesUrl(String apiRoot, String org, String repositoryName) {
		return apiRoot + "/repos/" + org + "/" + repositoryName + "/issues";
	}

	/**
	 * Pulls取得URL組み立て
	 * 
	 * @param apiRoot
	 * @param org
	 * @return Issue一覧取得URL
	 */
	public static String makePullsUrl(String apiRoot, String org, String repositoryName) {
		return apiRoot + "/repos/" + org + "/" + repositoryName + "/pulls";
	}
}
