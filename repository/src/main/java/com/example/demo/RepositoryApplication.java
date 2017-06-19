package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class RepositoryApplication {
	// 無限ループ防止用
	private final int MAX_ROOP = 100;
	
	@Autowired
	private ConfigReader configReader;

	public static void main(String[] args) {
		try (ConfigurableApplicationContext context = SpringApplication.run(RepositoryApplication.class, args)) {
			RepositoryApplication app = context.getBean(RepositoryApplication.class);
			app.downloadData();
		}
	}
	
	public void downloadData() {
		ProxySettings proxySettings = configReader.getProxySettings();
		
		for (IssueSettings issueSettings : configReader.getSettings().getIssues()) {
			// repository取得APIのURL組み立て
			String urlForRepositories = makeUrlForRepositories(issueSettings);
			
			RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
			// ヘッダーにトークン設定
			if (!StringUtils.isEmpty(issueSettings.getToken())) {
				restTemplateBuilder.basicAuthorization("Authorization", "token " + issueSettings.getToken());
			}

			try {
				URI uriForRepositories = new URI(urlForRepositories);
				RestOperations restOperations = restTemplateBuilder.build();
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", "token " + issueSettings.getToken());
				LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
				params.add("state", "all");
				HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
				HttpEntity entity = new HttpEntity(headers);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<List<Object>> responseEntity = restTemplate.exchange(urlForRepositories,
				                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<Object>>() {});
				List<Object> repositories = responseEntity.getBody();
				for (Object object : repositories) {
					LinkedHashMap map = (LinkedHashMap)object;
					String repositoryName = (String)map.get("name");
					// issue取得APIのURL組み立て
					String urlForIssues = makeUrlForIssues(issueSettings, repositoryName);
					if (StringUtils.isEmpty(urlForIssues)) {
						System.out.println("[WARNING] failed to make url for api access");
						continue;
					}
					// urlパラメータ組み立て
					String additionalUrlParam = makeUrlParam(issueSettings);
					// json取得
					for (int i=1; i<MAX_ROOP; i++) {
						String pageUrlParam = "?per_page=100&page=" + i;
						urlForIssues += pageUrlParam + additionalUrlParam;
						ResponseEntity<String> test = restOperations.exchange(urlForIssues, HttpMethod.GET, entity, String.class);
						System.out.println(test.getBody());
						// 終了判定（データが無ければ"[]"が返ってくるが、いったんてきとうに判定）
						if (test.getBody().length() < 10) {
							break;
						}
						
						try {
							File file = new File(issueSettings.getSavePath() + "\\" + issueSettings.getSaveFilePrefix() + repositoryName + i + ".json");
							FileWriter filewriter = new FileWriter(file);
							filewriter.write(test.getBody());
							filewriter.close();
						} catch (IOException e){
							System.out.println(e);
						}
					}
				}
			} catch (URISyntaxException ex) {
				System.out.println("[ERROR] failed to make uri for api access");
				System.out.println("[ERROR] api_root:" + issueSettings.getApiRoot());
				System.out.println(ex.getStackTrace());
			}
		}
	}
	
	/**
	 * Repository取得URL組み立て
	 * @param issueSettings
	 * @return
	 */
	private String makeUrlForRepositories(IssueSettings issueSettings) {
		String url = "";
		switch (issueSettings.getType()) {
		case IssueSettings.ISSUE_TYPE_GITHUB:
			url = GitHubApi.makeRepositoriesUrl(issueSettings.getApiRoot(), issueSettings.getOrg());
			break;
		case IssueSettings.ISSUE_TYPE_GITBUCKET:
			break;
		}
		return url;
	}
	
	/**
	 * Issue取得URL組み立て
	 * @param issueSettings
	 * @return
	 */
	private String makeUrlForIssues(IssueSettings issueSettings, String repositoryName) {
		String url = "";
		switch (issueSettings.getType()) {
		case IssueSettings.ISSUE_TYPE_GITHUB:
			url = GitHubApi.makeIssuesUrl(issueSettings.getApiRoot(), issueSettings.getOrg(), repositoryName);
			break;
		case IssueSettings.ISSUE_TYPE_GITBUCKET:
			break;
		}
		return url;
	}
	
	/**
	 * URLパラメータ組み立て
	 * @param issueSettings
	 * @return
	 */
	private String makeUrlParam(IssueSettings issueSettings) {
		List<String> paramList = new ArrayList<>();
		if (issueSettings.getFilter() != null) {
			paramList.add("filter=" + issueSettings.getFilter());
		}
		if (issueSettings.getFilter() != null) {
			paramList.add("state=" + issueSettings.getState());
		}
		if (issueSettings.getFilter() != null) {
			paramList.add("labals=" + issueSettings.getLabels());
		}
		if (issueSettings.getFilter() != null) {
			paramList.add("sort=" + issueSettings.getSort());
		}
		if (issueSettings.getFilter() != null) {
			paramList.add("direction=" + issueSettings.getDirection());
		}
		if (paramList.size() > 0) {
			return "&" + String.join("&", paramList.toArray(new String[0]));
		}
		return "";
	}
}
