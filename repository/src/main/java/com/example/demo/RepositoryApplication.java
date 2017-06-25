package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

@SpringBootApplication
public class RepositoryApplication {
	// 無限ループ防止用
	private final int MAX_ROOP = 100;
	// ページ取得件数
	private final int PER_PAGE = 100;

	@Autowired
	private ConfigReader configReader;

	/**
	 * メイン
	 * 
	 * @param args
	 *            コマンドライン引数
	 */
	public static void main(String[] args) {
		try (ConfigurableApplicationContext context = SpringApplication.run(RepositoryApplication.class, args)) {
			RepositoryApplication app = context.getBean(RepositoryApplication.class);
			app.downloadData();
		}
	}

	/**
	 * データダウンロード
	 */
	public void downloadData() {
		// 設定ファイルチェック
		if (!isEnableSetting(configReader)) {
			System.out.println("[ERROR]Setting is disable");
			return;
		}
		// 設定取得
		Settings settings = configReader.getSettings();
		// プロキシ設定取得
		ProxySettings proxySettings = configReader.getProxySettings();
		// ダウンロードして保存したIssueのファイルパスのリスト
		List<String> successIssuesInfos = new ArrayList<>();
		// ダウンロードして保存したPullsのファイルパスのリスト
		List<String> successPullsInfos = new ArrayList<>();
		// 失敗リスト
		List<String> errorList = new ArrayList<>();
		// パス作成
		String saveRootPath = settings.getSaveRoot();
		String dataDirname = makeDataDirname(settings.getDataDirnamePrefix());
		String dataDirPath = buildPath(saveRootPath, dataDirname);
		String fileInfoDirPath = buildPath(dataDirPath, settings.getFileInfoDirname());
		String issuesSaveDirPath = buildPath(dataDirPath, settings.getIssuesSaveDirname());
		String pullsSaveDirPath = buildPath(dataDirPath, settings.getPullsSaveDirname());
		String issuesInfoFilePath = buildPath(fileInfoDirPath, settings.getIssuesListFilename());
		String pullsInfoFilePath = buildPath(fileInfoDirPath, settings.getPullsListFilename());
		String errorsInfoFilePath = buildPath(fileInfoDirPath, settings.getErrorsListFilename());
		// フォルダ作成（ルートフォルダは既に存在すれば移動）
		if (!makeDir(dataDirPath)) {
			System.out.println("[ERROR]Failed to make data_dir");
			return;
		}
		if (!makeDir(fileInfoDirPath)) {
			System.out.println("[ERROR]Failed to make file_info_dir");
			return;
		}
		if (!makeDir(issuesSaveDirPath)) {
			System.out.println("[ERROR]Failed to make issues_save_dir");
			return;
		}
		if (!makeDir(pullsSaveDirPath)) {
			System.out.println("[ERROR]Failed to make pulls_save_dir");
			return;
		}
		// データダウンロード
		int indexForIssues = 1;
		int indexForPulls = 1;
		String relativeIssuesPath = buildPath(dataDirname, settings.getIssuesSaveDirname());
		String relativePullsPath = buildPath(dataDirname, settings.getPullsSaveDirname());
		for (DownloadSetting downloadSetting : configReader.getSettings().getDownloadSettings()) {
			// リポジトリごとにデータ取得
			for (String repositoryName : downloadSetting.getRepositoryNames()) {
				boolean isOccurError = false;
				// 識別名作成
				String identifier = makeIdentifier(repositoryName, downloadSetting);
				// ファイルのリスト
				List<String> issuesFilePathes = new ArrayList<>();
				List<String> pullsFilePathes = new ArrayList<>();
				try {
					// Issuesダウンロード
					for (int i = 0; i < downloadSetting.getIssuesSettings().size(); i++) {
						IssuesSetting issuesSetting = downloadSetting.getIssuesSettings().get(i);
						issuesFilePathes.addAll(downloadIssues(saveRootPath, relativeIssuesPath, repositoryName,
								downloadSetting, issuesSetting, proxySettings, indexForIssues));
						indexForIssues++;
					}
					// Pullsダウンロード
					for (int i = 0; i < downloadSetting.getPullsSettings().size(); i++) {
						PullsSetting pullsSetting = downloadSetting.getPullsSettings().get(i);
						pullsFilePathes.addAll(downloadPulls(saveRootPath, relativePullsPath, repositoryName,
								downloadSetting, pullsSetting, proxySettings, indexForPulls));
						indexForPulls++;
					}
					// 成功した情報を追加（全て成功して意味があるため、Pullsが失敗したらIssuesも失敗とみなす）
					successIssuesInfos.addAll(makeFileInfos(identifier, issuesFilePathes));
					successPullsInfos.addAll(makeFileInfos(identifier, pullsFilePathes));
				} catch (IOException e) {
					isOccurError = true;
				} catch (NetworkException e) {
					isOccurError = true;
				} catch (Exception e) {
					isOccurError = true;
				}
				// エラーが発生していたらエラーリストに追加
				if (isOccurError) {
					System.out.println("[ERROR]Failed to donwload identifier=" + identifier);
					errorList.add(identifier);
				}
			}
		}
		// ファイル情報書き込み
		try {
			writeFile(issuesInfoFilePath, successIssuesInfos);
		} catch (IOException e) {
			System.out.println("[ERROR]Failed to write issues info");
		}
		try {
			writeFile(pullsInfoFilePath, successPullsInfos);
		} catch (IOException e) {
			System.out.println("[ERROR]Failed to write pulls info");
		}
		if (errorList.size() > 0) {
			try {
				writeFile(errorsInfoFilePath, errorList);
			} catch (IOException e) {
				System.out.println("[ERROR]Failed to write error info");
			}
		}
		System.out.println("[Info]Finish data_path=" + dataDirPath);
	}

	/**
	 * フォルダ作成
	 * 
	 * @param path
	 *            フォルダ
	 * @return 成功フラグ
	 */
	private boolean makeDir(String path) {
		File dir = new File(path);
		return dir.mkdir();
	}

	/**
	 * データフォルダ名作成
	 * 
	 * @param dataDirnamePrefix
	 *            フォルダ名の接頭辞
	 * @return データフォルダ名
	 */
	private String makeDataDirname(String dataDirnamePrefix) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = new Date();
		return dataDirnamePrefix + "_" + df.format(now);
	}

	/**
	 * 保存先フォルダ作成
	 * 
	 * @param path
	 *            フォルダパス
	 * @return 成功フラグ
	 */
	private boolean makeSaveRootDir(String path) {
		File saveDir = new File(path);
		// 既にフォルダが存在していれば移動
		if (saveDir.exists()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			String postFix = "_" + df.format(now);
			String oldDirPath = path + postFix;
			File oldDir = new File(oldDirPath);
			System.out.println("[INFO]Move old save_dir_root to " + oldDirPath);
			if (!saveDir.renameTo(oldDir)) {
				return false;
			}
		}
		// フォルダ作成
		return makeDir(path);
	}

	/**
	 * 識別ID作成
	 * 
	 * @param repositoryName
	 *            リポジトリ名
	 * @param issueSettings
	 *            Issuesの設定
	 * @return 識別ID
	 */
	private String makeIdentifier(String repositoryName, DownloadSetting downloadSetting) {
		String identifier = repositoryName;
		if (!StringUtils.isEmpty(downloadSetting.getIdentifierPrefix())) {
			identifier = downloadSetting.getIdentifierPrefix() + identifier;
		}
		if (!StringUtils.isEmpty(downloadSetting.getIdentifierPostfix())) {
			identifier = identifier + downloadSetting.getIdentifierPostfix();
		}
		return identifier;
	}

	/**
	 * ファイル情報作成
	 * 
	 * @param identifier
	 *            識別ID
	 * @param filePathes
	 *            ファイルパス
	 * @return ファイル情報
	 */
	private List<String> makeFileInfos(String identifier, List<String> filePathes) {
		List<String> fileInfos = new ArrayList<>();
		for (String filePath : filePathes) {
			fileInfos.add(identifier + "," + filePath);
		}
		return fileInfos;
	}

	/**
	 * Issues情報ダウンロード
	 * 
	 * @param saveDirPath
	 *            保存先ルートディレクトリ
	 * @param relativeIssuesPath
	 *            Issues保存先相対パス
	 * @param repositoryName
	 *            リポジトリ名
	 * @param downloadSetting
	 *            Download設定
	 * @param issueSettings
	 *            Issues設定
	 * @param proxySettings
	 *            プロキシ設定
	 * @param indexForFilename
	 *            ファイル名用インデックス
	 * @return ダウンロードしたIssues情報のファイルパスリスト
	 * @throws IOException
	 * @throws NetworkException
	 */
	private List<String> downloadIssues(String saveRootPath, String relativeIssuesPath, String repositoryName,
			DownloadSetting downloadSetting, IssuesSetting issuesSetting, ProxySettings proxySettings,
			int indexForFilename) throws IOException, NetworkException {
		List<String> filePathList = new ArrayList<>();

		// Issueごとに固定のurlパラメータ組み立て
		String additionalUrlParam = makeUrlParamForIssues(issuesSetting);
		// issue取得APIのURL組み立て
		String baseUrl = GitHubApi.makeIssuesUrl(downloadSetting.getApiRoot(), downloadSetting.getOrg(),
				repositoryName);
		// ページごとにjson取得
		for (int i = 1; i < MAX_ROOP; i++) {
			// ページ取得のパラメータ作成
			String pageUrlParam = "?per_page=" + PER_PAGE + "&page=" + i;
			String urlForIssues = baseUrl + pageUrlParam + additionalUrlParam;
			// TODO:ファイルパスの与え方がイマイチ
			// ファイルパス作成
			String relativeFilePath = makeIssuesFilePath(relativeIssuesPath, indexForFilename, i);
			String filePath = buildPath(saveRootPath, relativeFilePath);
			// メッセージ
			System.out.println("[Info]Downloading " + downloadSetting.getOrg() + "/" + repositoryName + " index="
					+ indexForFilename + " issues page=" + i);

			// jsonデータ取得
			String jsonText = requestGetJson(urlForIssues, downloadSetting.getToken(), proxySettings);
			try {
				System.out.println("[Info]Sleep " + downloadSetting.getSleepSecond() + " second");
				Thread.sleep(downloadSetting.getSleepSecond() * 1000);
			} catch (InterruptedException e) {
				System.out.println("[WARNING]Failed to sleep");
			}
			// TODO:終了判定を件数にしたい（GitBucketをうまく判定する必要あり）
			// 終了判定（データが無ければ"[]"が返ってくるが、いったんてきとうに判定）
			if (jsonText.length() < 10) {
				System.out.println("[Info]No data");
				break;
			}
			// ファイルに保存
			writeFile(filePath, jsonText);
			// ファイルパスのリスト追加
			filePathList.add(relativeFilePath);
		}
		return filePathList;
	}

	/**
	 * Pulls情報ダウンロード
	 * 
	 * @param saveDirPath
	 *            保存先ルートディレクトリ
	 * @param relativePullsPath
	 *            Pulls保存先相対パス
	 * @param downloadSetting
	 *            Download設定
	 * @param pullsSetting
	 *            Pulls設定
	 * @param proxySettings
	 *            プロキシ設定
	 * @param indexForFilename
	 *            ファイル名用インデックス
	 * @return ダウンロードしたPulls情報のファイルパスリスト
	 * @throws IOException
	 * @throws NetworkException
	 */
	private List<String> downloadPulls(String saveDirPath, String relativePullsPath, String repositoryName,
			DownloadSetting downloadSetting, PullsSetting pullsSetting, ProxySettings proxySettings,
			int indexForFilename) throws IOException, NetworkException {
		List<String> filePathList = new ArrayList<>();

		// Pullsごとに固定のurlパラメータ組み立て
		String additionalUrlParam = makeUrlParamForPulls(pullsSetting);
		// pulls取得APIのURL組み立て
		String baseUrl = GitHubApi.makePullsUrl(downloadSetting.getApiRoot(), downloadSetting.getOrg(), repositoryName);
		// ページごとにjson取得
		for (int i = 1; i < MAX_ROOP; i++) {
			// ページ取得のパラメータ作成
			String pageUrlParam = "?per_page=" + PER_PAGE + "&page=" + i;
			String urlForPulls = baseUrl + pageUrlParam + additionalUrlParam;
			// ファイルパス作成
			String relativeFilePath = makePullsFilePath(relativePullsPath, indexForFilename, i);
			String filePath = buildPath(saveDirPath, relativeFilePath);
			// メッセージ
			System.out.println("[Info]Downloading " + downloadSetting.getOrg() + "/" + repositoryName + " index="
					+ indexForFilename + " pulls page=" + i);

			// jsonデータ取得
			String jsonText = requestGetJson(urlForPulls, downloadSetting.getToken(), proxySettings);
			try {
				System.out.println("[Info]Sleep " + downloadSetting.getSleepSecond() + " second");
				Thread.sleep(downloadSetting.getSleepSecond() * 1000);
			} catch (InterruptedException e) {
				System.out.println("[WARNING]Failed to sleep");
			}
			// 終了判定（データが無ければ"[]"が返ってくるが、いったんてきとうに判定）
			if (jsonText.length() < 10) {
				System.out.println("[Info]No data");
				break;
			}
			// ファイルに保存
			writeFile(filePath, jsonText);
			// ファイルパスのリスト追加
			filePathList.add(relativeFilePath);
		}
		return filePathList;
	}

	/**
	 * Issueのファイルパス作成
	 * 
	 * @param rootDirPath
	 *            保存先フォルダ
	 * @param seqNum
	 *            ファイル名に付与する連番
	 * @param page
	 *            ページ番号
	 * @return Issueのファイルパス
	 */
	private String makeIssuesFilePath(String rootDirPath, int seqNum, int page) {
		return buildPath(rootDirPath,
				"issues" + String.format("%02d", seqNum) + "_page" + String.format("%04d", page) + ".json");
	}

	/**
	 * Pullsのファイルパス作成
	 * 
	 * @param rootDirPath
	 *            保存先フォルダ
	 * @param seqNum
	 *            ファイル名に付与する連番
	 * @param page
	 *            ページ番号
	 * @return Pullsのファイルパス
	 */
	private String makePullsFilePath(String rootDirPath, int seqNum, int page) {
		return buildPath(rootDirPath,
				"pulls" + String.format("%02d", seqNum) + "_page" + String.format("%04d", page) + ".json");
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
	 * パス組み立て
	 * 
	 * @param pathParent
	 *            親のパス
	 * @param childName
	 *            子の名前
	 * @return 組み立てたパス
	 */
	private String buildPath(String pathParent, String childName) {
		return buildPath(pathParent, childName, "\\");
	}

	/**
	 * パス組み立て
	 * 
	 * @param pathParent
	 *            親のパス
	 * @param childName
	 *            子の名前
	 * @param sep
	 *            ファイルパスのセパレータ
	 * @return 組み立てたパス
	 */
	private String buildPath(String pathParent, String childName, String sep) {
		return trimRightMark(pathParent) + sep + childName;
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
		System.out.println("[Info]Request json url=" + url);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		// ヘッダーにトークン設定
		if (!StringUtils.isEmpty(token)) {
			restTemplateBuilder.basicAuthorization("Authorization", "token " + token);
			headers.add("Authorization", "token " + token);
		}
		RestOperations restOperations = restTemplateBuilder.build();
		ResponseEntity<String> response = restOperations.exchange(url, HttpMethod.GET, entity, String.class);
		if (response.getStatusCodeValue() != 200) {
			String message = "[ERROR]Failes to request status code=" + response.getStatusCodeValue();
			System.out.println(message);
			throw new NetworkException(message);
		}
		return response.getBody();
	}

	/**
	 * Issues用URLパラメータ組み立て
	 * 
	 * @param issueSettings
	 *            Issue取得設定
	 * @return URLパラメータ
	 */
	private String makeUrlParamForIssues(IssuesSetting issueSettings) {
		List<String> paramList = new ArrayList<>();
		if (!StringUtils.isEmpty(issueSettings.getFilter())) {
			paramList.add("filter=" + issueSettings.getFilter());
		}
		if (!StringUtils.isEmpty(issueSettings.getState())) {
			paramList.add("state=" + issueSettings.getState());
		}
		if (!StringUtils.isEmpty(issueSettings.getLabels())) {
			paramList.add("labals=" + issueSettings.getLabels());
		}
		if (!StringUtils.isEmpty(issueSettings.getSort())) {
			paramList.add("sort=" + issueSettings.getSort());
		}
		if (!StringUtils.isEmpty(issueSettings.getDirection())) {
			paramList.add("direction=" + issueSettings.getDirection());
		}
		if (paramList.size() > 0) {
			return "&" + String.join("&", paramList.toArray(new String[0]));
		}
		return "";
	}

	private String makeUrlParamForPulls(PullsSetting pullsSetting) {
		List<String> paramList = new ArrayList<>();

		if (!StringUtils.isEmpty(pullsSetting.getState())) {
			paramList.add("state=" + pullsSetting.getState());
		}
		if (!StringUtils.isEmpty(pullsSetting.getHead())) {
			paramList.add("head=" + pullsSetting.getHead());
		}
		if (!StringUtils.isEmpty(pullsSetting.getBase())) {
			paramList.add("base=" + pullsSetting.getBase());
		}
		if (!StringUtils.isEmpty(pullsSetting.getSort())) {
			paramList.add("sort=" + pullsSetting.getSort());
		}
		if (!StringUtils.isEmpty(pullsSetting.getDirection())) {
			paramList.add("direction=" + pullsSetting.getDirection());
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

	/**
	 * ファイル書き込み
	 * 
	 * @param path
	 *            ファイルパス
	 * @param strings
	 *            データ
	 * @throws IOException
	 */
	private void writeFile(String path, List<String> strings) throws IOException {
		writeFile(path, strings, "\r\n");
	}

	/**
	 * ファイル書き込み
	 * 
	 * @param path
	 *            ファイルパス
	 * @param strings
	 *            データ
	 * @param sep
	 *            データのセパレータ
	 * @throws IOException
	 */
	private void writeFile(String path, List<String> strings, String sep) throws IOException {
		String text = String.join("\r\n", strings.toArray(new String[0]));
		writeFile(path, text);
	}

	/**
	 * ファイル書き込み
	 * 
	 * @param filePath
	 *            ファイルパス
	 * @param text
	 *            データ
	 * @throws IOException
	 */
	private void writeFile(String filePath, String text) throws IOException {
		// ファイルに保存
		File file = new File(filePath);
		FileWriter filewriter = new FileWriter(file);
		try {
			filewriter.write(text);
		} finally {
			filewriter.close();
		}
	}
}
