package cloudgene.mapred.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import cloudgene.mapred.core.User;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import genepi.hadoop.HadoopUtil;
import genepi.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Settings {

	private String hadoopPath = "/usr/bin/hadoop";

	private String pigPath = "/usr/";

	private String sparkPath = "/usr/bin/spark-submit";

	private String rPath = "/usr/";

	private String outputPath = "output";

	private String tempPath = "tmp";

	private String localWorkspace = "workspace";

	private String hdfsWorkspace = "cloudgene";

	private String streamingJar = "";

	private String version;

	private String name = "Cloudgene";

	private String secretKey = "default-key-change-me";

	private Map<String, String> mail;

	private Map<String, String> database;

	private List<Application> apps;

	private int retireAfter = 6;

	private int notificationAfter = 4;

	private int threadsQueue = 5;

	private int maxRunningJobs = 20;

	private int maxRunningJobsPerUser = 2;

	private boolean autoRetire = false;

	private boolean streaming = true;

	private boolean removeHdfsWorkspace = false;

	private static final Log log = LogFactory.getLog(Settings.class);

	private boolean writeStatistics = true;

	private boolean https = false;

	private String httpsKeystore = "";

	private String httpsPassword = "";

	private boolean maintenance = false;

	private String adminMail = null;

	private String slack = null;

	private Map<String, Application> indexApps;

	private String urlPrefix = "";

	private List<MenuItem> navigation = new Vector<MenuItem>();

	private boolean secureCookie = false;

	private int uploadLimit = -1;

	public Settings() {

		apps = new Vector<Application>();
		apps.add(new Application("hello", "admin", "sample/cloudgene.yaml"));
		apps.add(new Application("hello", "public", "sample/cloudgene-public.yaml"));

		reloadApplications();

		mail = new HashMap<String, String>();
		mail.put("smtp", "localhost");
		mail.put("port", "25");
		mail.put("user", "");
		mail.put("password", "");
		mail.put("name", "noreply@cloudgene");

		MenuItem helpMenuItem = new MenuItem();
		helpMenuItem.setId("help");
		helpMenuItem.setName("Help");
		helpMenuItem.setLink("#!pages/help");
		navigation.add(helpMenuItem);

		database = new HashMap<String, String>();
		database.put("driver", "h2");
		database.put("database", "data/mapred");
		database.put("user", "mapred");
		database.put("password", "mapred");

	}

	public static Settings load(String filename) throws FileNotFoundException, YamlException {

		YamlConfig config = new YamlConfig();
		config.setPropertyElementType(Settings.class, "apps", Application.class);
		YamlReader reader = new YamlReader(new FileReader(filename), config);

		Settings settings = reader.read(Settings.class);

		// auto-search

		if (settings.streamingJar.isEmpty() || !(new File(settings.streamingJar).exists())) {

			String version = HadoopUtil.getInstance().getVersion();
			String jar = "hadoop-streaming-" + version + ".jar";
			settings.streamingJar = FileUtil.path(settings.hadoopPath, "contrib", "streaming", jar);

			if (new File(settings.streamingJar).exists()) {

				log.info("Found streamingJar at " + settings.streamingJar + "");
				settings.streaming = true;

			} else {

				log.warn(
						"Streaming Jar could not be found automatically. Please specify it in config/settings.yaml. Streaming mode is disabled.");
				settings.streaming = false;
			}

		}

		log.info("Auto retire: " + settings.isAutoRetire());
		log.info("Retire jobs after " + settings.retireAfter + " days.");
		log.info("Notify user after " + settings.notificationAfter + " days.");
		log.info("Write statistics: " + settings.writeStatistics);

		return settings;

	}

	public void save() {
		try {

			FileUtil.createDirectory("config");
			YamlWriter writer = new YamlWriter(new FileWriter(FileUtil.path("config", "settings.yaml")));
			writer.write(this);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getHadoopPath() {
		return hadoopPath;
	}

	public void setHadoopPath(String hadoopPath) {
		this.hadoopPath = hadoopPath;
	}

	public void setPigPath(String pigPath) {
		this.pigPath = pigPath;
	}

	public String getPigPath() {
		return pigPath;
	}

	/*
	 * public String getAppsPath() { return appsPath; }
	 * 
	 * public void setAppsPath(String appsPath) { this.appsPath = appsPath; }
	 */

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public String getLocalWorkspace() {
		return localWorkspace;
	}

	public void setLocalWorkspace(String localWorkspace) {
		this.localWorkspace = localWorkspace;
	}

	public String getHdfsWorkspace() {
		return hdfsWorkspace;
	}

	public void setHdfsWorkspace(String hdfsWorkspace) {
		this.hdfsWorkspace = hdfsWorkspace;
	}

	public String getStreamingJar() {
		return streamingJar;
	}

	public void setStreamingJar(String streamingJar) {
		this.streamingJar = streamingJar;
	}

	public boolean isStreaming() {
		return streaming;
	}

	public void setStreaming(boolean streaming) {
		this.streaming = streaming;
	}

	public String getRPath() {
		return rPath;
	}

	public void setRPath(String rPath) {
		this.rPath = rPath;
	}

	public boolean isRemoveHdfsWorkspace() {
		return removeHdfsWorkspace;
	}

	public void setRemoveHdfsWorkspace(boolean removeHdfsWorkspace) {
		this.removeHdfsWorkspace = removeHdfsWorkspace;
	}

	public boolean testPaths() {

		String hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");

		if (!new File(hadoop).exists()) {

			log.warn("hadoop '" + hadoop + "' does not exist. please change it.");

			// return false;

		}
		/*
		 * if (!new File(streamingJar).exists()) {
		 * 
		 * log.error("streamingJar '" + streamingJar + "' does not exist.");
		 * 
		 * return false;
		 * 
		 * }
		 */

		return true;

	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getMail() {
		return mail;
	}

	public void setMail(Map<String, String> mail) {
		this.mail = mail;
	}

	public String getSlack() {
		return slack;
	}

	public void setSlack(String slack) {
		this.slack = slack;
	}

	public List<Application> getApps() {
		return apps;
	}

	public void setApps(List<Application> apps) {
		this.apps = apps;
		reloadApplications();
	}

	public void reloadApplications() {
		indexApps = new HashMap<String, Application>();
		for (Application app : apps) {
			log.info("Register application " + app.getId());

			// load application
			try {
				log.info("Load workflow file " + app.getFilename());
				app.loadWorkflow();
			} catch (IOException e) {
				log.error("Application " + app.getId() + " has syntax errors.", e);
			}
			indexApps.put(app.getId(), app);

		}
	}

	public Application getAppByIdAndUser(String id, User user) {

		Application app = getApp(id);

		if (app != null && app.isEnabled() && app.isLoaded() && !app.hasSyntaxError()) {

			if (user == null) {
				if (app.getPermission().toLowerCase().equals("public")) {
					if (app.getWdlApp().getWorkflow() != null) {
						return app;
					} else {
						return app;
					}
				} else {
					return null;
				}
			}

			if (user.isAdmin() || app.getPermission().toLowerCase().equals(user.getRole().toLowerCase())
					|| app.getPermission().toLowerCase().equals("public")) {
				if (app.getWdlApp().getWorkflow() != null) {
					return app;
				} else {
					return app;
				}

			} else {
				return null;
			}

		}

		return null;

	}

	public Application getApp(String id) {

		Application app = indexApps.get(id);
		return app;

	}

	public List<WdlHeader> getAppsByUser(User user) {

		List<WdlHeader> listApps = new Vector<WdlHeader>();

		for (Application application : getApps()) {

			boolean using = true;

			if (user == null) {
				if (application.getPermission().toLowerCase().equals("public")) {
					using = true;
				} else {
					using = false;
				}
			} else {

				if (!user.isAdmin() && !application.getPermission().toLowerCase().equals("public")) {

					if (!application.getPermission().toLowerCase().equals(user.getRole().toLowerCase())) {
						using = false;
					}
				}
			}

			if (using) {

				if (application.isEnabled() && application.isLoaded() && !application.hasSyntaxError()) {
					if (application.getWdlApp().getWorkflow() != null) {
						WdlApp app = application.getWdlApp();
						WdlHeader meta = (WdlHeader) app;
						app.setId(application.getId());
						listApps.add(meta);
					}
				}

			}

		}

		return listApps;

	}

	public void deleteApplication(Application application) throws IOException {

		// execute install steps
		if (application.getWdlApp().getDeinstallation() != null) {

			HashMap<String, String> environment = getEnvironment(application);
			application.getWdlApp().deinstall(environment);
		}

		// download
		String id = application.getId();
		String appPath = FileUtil.path("apps", id);
		FileUtil.deleteDirectory(appPath);
		apps.remove(application);
		reloadApplications();

	}

	public Application installApplicationFromUrl(String id, String url) throws IOException {
		// download file from url
		String zipFilename = FileUtil.path(getTempPath(), "download.zip");
		FileUtils.copyURLToFile(new URL(url), new File(zipFilename));
		return installApplicationFromZipFile(id, zipFilename);
	}

	public Application installApplicationFromZipFile(String id, String zipFilename) throws IOException {

		// extract in apps folder
		String appPath = FileUtil.path("apps", id);

		FileUtil.createDirectory("apps");
		FileUtil.createDirectory(appPath);
		ZipFile file;
		try {
			file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installApplicationFromDirectory(id, appPath);

	}

	public Application installApplicationFromDirectory(String id, String path) throws IOException {
		// find all cloudgene workflows (use filename as id)
		System.out.println("Search in folder " + path);
		String[] files = FileUtil.getFiles(path, "*.yaml");
		for (String filename : files) {

			return installApplicationFromYaml(id, filename);

		}

		// search in subfolders
		for (String directory : getDirectories(path)) {
			return installApplicationFromDirectory(id, directory);
		}
		return null;

	}

	public Application installApplicationFromYaml(String id, String filename) throws IOException {

		Application application = new Application();
		application.setId(id);
		application.setFilename(filename);
		application.setPermission("user");
		application.loadWorkflow();

		// TODO: check requirements (e.g. hadoop, rmd, spark, ...)

		// execute install steps
		if (application.getWdlApp().getInstallation() != null) {

			HashMap<String, String> environment = getEnvironment(application);
			application.getWdlApp().install(environment);
		}

		apps.add(application);
		indexApps.put(application.getId(), application);

		return application;

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTempFilename(String filename) {
		String path = getTempPath();
		String name = FileUtil.getFilename(filename);
		return FileUtil.path(path, name);
	}

	public void setNotificationAfter(int notificationAfter) {
		this.notificationAfter = notificationAfter;
	}

	public int getNotificationAfter() {
		return notificationAfter;
	}

	public int getNotificationAfterInSec() {
		return notificationAfter * 24 * 60 * 60;
	}

	public void setRetireAfter(int retireAfter) {
		this.retireAfter = retireAfter;
	}

	public int getRetireAfter() {
		return retireAfter;
	}

	public int getRetireAfterInSec() {
		return retireAfter * 24 * 60 * 60;
	}

	public void setAutoRetire(boolean autoRetire) {
		this.autoRetire = autoRetire;
	}

	public boolean isAutoRetire() {
		return autoRetire;
	}

	public void setWriteStatistics(boolean writeStatistics) {
		this.writeStatistics = writeStatistics;
	}

	public boolean isWriteStatistics() {
		return writeStatistics;
	}

	public void setHttps(boolean https) {
		this.https = https;
	}

	public boolean isHttps() {
		return https;
	}

	public void setHttpsKeystore(String httpsKeystore) {
		this.httpsKeystore = httpsKeystore;
	}

	public String getHttpsKeystore() {
		return httpsKeystore;
	}

	public void setHttpsPassword(String httpsPassword) {
		this.httpsPassword = httpsPassword;
	}

	public String getHttpsPassword() {
		return httpsPassword;
	}

	public void setMaintenance(boolean maintenance) {
		this.maintenance = maintenance;
	}

	public boolean isMaintenance() {
		return maintenance;
	}

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getAdminMail() {
		return adminMail;
	}

	public String getSparkPath() {
		return sparkPath;
	}

	public void setSparkPath(String sparkPath) {
		this.sparkPath = sparkPath;
	}

	public void setThreadsQueue(int threadsQueue) {
		this.threadsQueue = threadsQueue;
	}

	public int getThreadsQueue() {
		return threadsQueue;
	}

	public int getMaxRunningJobs() {
		return maxRunningJobs;
	}

	public void setMaxRunningJobs(int maxRunningJobs) {
		this.maxRunningJobs = maxRunningJobs;
	}

	public int getMaxRunningJobsPerUser() {
		return maxRunningJobsPerUser;
	}

	public void setMaxRunningJobsPerUser(int maxRunningJobsPerUser) {
		this.maxRunningJobsPerUser = maxRunningJobsPerUser;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setDatabase(Map<String, String> database) {
		this.database = database;
	}

	public Map<String, String> getDatabase() {
		return database;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setNavigation(List<MenuItem> navigation) {
		this.navigation = navigation;
	}

	public List<MenuItem> getNavigation() {
		return navigation;
	}

	public boolean isSecureCookie() {
		return secureCookie;
	}

	public void setSecureCookie(boolean secureCookie) {
		this.secureCookie = secureCookie;
	}

	public int getUploadLimit() {
		return uploadLimit;
	}

	public void setUploadLimit(int uploadLimit) {
		this.uploadLimit = uploadLimit;
	}

	private String[] getDirectories(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		int count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				count++;
			}
		}

		String[] names = new String[count];

		count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				names[count] = file.getAbsolutePath();
				count++;
			}
		}

		return names;
	}

	public HashMap<String, String> getEnvironment(Application application) {
		HashMap<String, String> environment = new HashMap<String, String>();
		String hdfsFolder = FileUtil.path(getHdfsWorkspace(), "apps", application.getId());
		String localFolder = FileUtil.path("apps", application.getId());
		environment.put("hdfs_app_folder", hdfsFolder);
		environment.put("apps", "apps");
		environment.put("local_app_folder", localFolder);
		return environment;
	}

}