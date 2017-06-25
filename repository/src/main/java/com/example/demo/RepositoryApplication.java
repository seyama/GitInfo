package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class RepositoryApplication {
	// 無限ループ防止用
	private final int MAX_ROOP = 100;
	// ページ取得件数
	private final int PER_PAGE = 100;

	@Autowired
	private ConfigReader configReader;

	public static void main(String[] args) {
		try (ConfigurableApplicationContext context = SpringApplication.run(RepositoryApplication.class, args)) {
			RepositoryApplication app = context.getBean(RepositoryApplication.class);
			app.downloadData();
		}
	}

	public void downloadData() {
		// 設定ファイルチェック
		if (!isEnableSetting(configReader)) {
			System.out.println("[ERROR]Setting is disable.");
			return;
		}
		// 設定取得
		Settings settings = configReader.getSettings();
		// プロキシ設定取得
		ProxySettings proxySettings = configReader.getProxySettings();

		for (IssueSettings issueSettings : configReader.getSettings().getIssues()) {
			RestTemplate restTemplate = new RestTemplate();
			// Issueごとに固定のurlパラメータ組み立て
			String additionalUrlParam = makeUrlParam(issueSettings);
			// リポジトリごとにデータ取得
			for (String repositoryName : issueSettings.getRepositoryNames()) {
				// issue取得APIのURL組み立て
				String urlForIssues = GitHubApi.makeIssuesUrl(issueSettings.getApiRoot(), issueSettings.getOrg(),
						repositoryName);
				// ページごとにjson取得
				for (int i = 1; i < MAX_ROOP; i++) {
					// ページ取得のパラメータ作成
					String pageUrlParam = "?per_page=" + PER_PAGE + "&page=" + i;
					urlForIssues += pageUrlParam + additionalUrlParam;
					// ファイルパス作成
					String filePath = makeIssuesFilePath(settings.getIssuesSaveDir(), i);

					try {
						// jsonデータ取得
						String jsonText = requestGetJson(urlForIssues, issueSettings.getToken(), proxySettings);
						// 終了判定（データが無ければ"[]"が返ってくるが、いったんてきとうに判定）
						if (jsonText.length() < 10) {
							break;
						}
						// ファイルに保存
						File file = new File(filePath);
						FileWriter filewriter = new FileWriter(file);
						filewriter.write(jsonText);
						filewriter.close();
					} catch (IOException e) {
						System.out.println(e);
					} catch (NetworkException e) {

					}

					try {
						Thread.sleep(issueSettings.getSleepSecond() * 1000);
					} catch (InterruptedException e) {
						System.out.println("[WARNING]Failed to sleep.");
					}
				}
			}
		}
	}

	/**
	 * Issueのファイルパス作成
	 * 
	 * @param rootDirPath
	 *            保存先フォルダ
	 * @param seqNum
	 *            ファイル名に付与する連番
	 * @return Issueのファイルパス
	 */
	private String makeIssuesFilePath(String rootDirPath, int seqNum) {
		String path = trimRightMark(rootDirPath) + "\\";
		path += "issues" + String.format("%04d", seqNum) + ".json";
		return path;
	}

	/**
	 * パスの最後の/と\を削除
	 * 
	 * @param path
	 *            トリム対象パス
	 * @return トリムしたパス
	 */
	private String trimRightMark(String path) {
		return path.replace("[\\/]*$", "");
	}

	/**
	 * JSONデータ取得
	 * 
	 * @param url
	 *            リクエストURL
	 * @param token
	 *            リクエスト用トークン
	 * @param proxySettings
	 *            プロキシ設定
	 * @return JSONテキスト
	 */
	private String requestGetJson(String url, String token, ProxySettings proxySettings) throws NetworkException {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		// ヘッダーにトークン設定
		if (!StringUtils.isEmpty(token)) {
			restTemplateBuilder.basicAuthorization("Authorization", "token " + token);
			headers.add("Authorization", "token " + token);
		}
		RestOperations restOperations = restTemplateBuilder.build();
		ResponseEntity<String> test = restOperations.exchange(url, HttpMethod.GET, entity, String.class);
		return test.getBody();
	}

	/**
	 * Issues用URLパラメータ組み立て
	 * 
	 * @param issueSettings
	 *            Issue取得設定
	 * @return URLパラメータ
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

	/**
	 * 設定確認
	 * 
	 * @param configReader
	 *            設定
	 * @return 設定が正常か否か
	 */
	private boolean isEnableSetting(ConfigReader configReader) {

		return true;
	}
}
