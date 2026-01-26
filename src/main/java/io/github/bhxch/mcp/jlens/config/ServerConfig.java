package io.github.bhxch.mcp.jlens.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Server configuration with virtual thread settings
 */
public class ServerConfig {

    // Virtual thread configuration
    private int virtualThreadCount = 1000;
    private int platformThreadCount = Runtime.getRuntime().availableProcessors();
    private boolean enableVirtualThreads = true;

    // Timeout configurations
    private int requestTimeoutSeconds = 30;
    private int parallelTaskTimeoutSeconds = 60;

    // Maven configuration
    private Path mavenExecutable;
    private Path mavenSettings;
    private Path mavenLocalRepository;

    // Decompiler configuration
    private DecompilerConfig decompilerConfig = new DecompilerConfig();

    // Cache configuration
    private int cacheSize = 1000;
    private long cacheTtlSeconds = 3600;

    // Test coverage requirements
    private int minTestCoverage = 80;
    private boolean enforceCoverage = true;

    // Logging configuration
    private String logLevel = "INFO";

    // Server configuration
    private int serverPort = 8080;
    private String serverHost = "localhost";

    public ServerConfig() {
    }

    public int getVirtualThreadCount() {
        return virtualThreadCount;
    }

    public void setVirtualThreadCount(int virtualThreadCount) {
        this.virtualThreadCount = virtualThreadCount;
    }

    public int getPlatformThreadCount() {
        return platformThreadCount;
    }

    public void setPlatformThreadCount(int platformThreadCount) {
        this.platformThreadCount = platformThreadCount;
    }

    public boolean isEnableVirtualThreads() {
        return enableVirtualThreads;
    }

    public void setEnableVirtualThreads(boolean enableVirtualThreads) {
        this.enableVirtualThreads = enableVirtualThreads;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public int getParallelTaskTimeoutSeconds() {
        return parallelTaskTimeoutSeconds;
    }

    public void setParallelTaskTimeoutSeconds(int parallelTaskTimeoutSeconds) {
        this.parallelTaskTimeoutSeconds = parallelTaskTimeoutSeconds;
    }

    public Path getMavenExecutable() {
        return mavenExecutable;
    }

    public void setMavenExecutable(Path mavenExecutable) {
        this.mavenExecutable = mavenExecutable;
    }

    public void setMavenExecutable(String mavenExecutable) {
        this.mavenExecutable = Paths.get(mavenExecutable);
    }

    public Path getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(Path mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = Paths.get(mavenSettings);
    }

    public Path getMavenLocalRepository() {
        return mavenLocalRepository;
    }

    public void setMavenLocalRepository(Path mavenLocalRepository) {
        this.mavenLocalRepository = mavenLocalRepository;
    }

    public void setMavenLocalRepository(String mavenLocalRepository) {
        this.mavenLocalRepository = Paths.get(mavenLocalRepository);
    }

    public DecompilerConfig getDecompilerConfig() {
        return decompilerConfig;
    }

    public void setDecompilerConfig(DecompilerConfig decompilerConfig) {
        this.decompilerConfig = decompilerConfig;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public int getMinTestCoverage() {
        return minTestCoverage;
    }

    public void setMinTestCoverage(int minTestCoverage) {
        this.minTestCoverage = minTestCoverage;
    }

    public boolean isEnforceCoverage() {
        return enforceCoverage;
    }

    public void setEnforceCoverage(boolean enforceCoverage) {
        this.enforceCoverage = enforceCoverage;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    /**
     * Static factory method for creating config from command line arguments
     */
    public static ServerConfig fromCommandLine(String[] args) {
        ServerConfig config = new ServerConfig();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--virtual-threads":
                case "-vt":
                    if (i + 1 < args.length) {
                        config.setVirtualThreadCount(Integer.parseInt(args[++i]));
                    }
                    break;
                case "--maven-executable":
                case "-me":
                    if (i + 1 < args.length) {
                        config.setMavenExecutable(args[++i]);
                    }
                    break;
                case "--maven-settings":
                case "-ms":
                    if (i + 1 < args.length) {
                        config.setMavenSettings(args[++i]);
                    }
                    break;
                case "--maven-repo":
                case "-mr":
                    if (i + 1 < args.length) {
                        config.setMavenLocalRepository(args[++i]);
                    }
                    break;
                case "--decompiler":
                case "-d":
                    if (i + 1 < args.length) {
                        config.getDecompilerConfig().setDecompilerType(args[++i]);
                    }
                    break;
                case "--port":
                case "-p":
                    if (i + 1 < args.length) {
                        config.setServerPort(Integer.parseInt(args[++i]));
                    }
                    break;
                case "--log-level":
                case "-l":
                    if (i + 1 < args.length) {
                        config.setLogLevel(args[++i]);
                    }
                    break;
                case "--help":
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    if (arg.startsWith("--")) {
                        System.err.println("Unknown option: " + arg);
                        printHelp();
                        System.exit(1);
                    }
            }
        }

        return config;
    }

    private static void printHelp() {
        System.out.println("Java Maven Classpath MCP Server");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -vt, --virtual-threads <count>    Maximum number of virtual threads (default: 1000)");
        System.out.println("  -me, --maven-executable <path>    Path to Maven executable");
        System.out.println("  -ms, --maven-settings <path>       Path to Maven settings.xml");
        System.out.println("  -mr, --maven-repo <path>          Path to Maven local repository");
        System.out.println("  -d, --decompiler <type>           Decompiler to use: fernflower, cfr (default: fernflower)");
        System.out.println("  -p, --port <port>                 Server port (default: 8080)");
        System.out.println("  -l, --log-level <level>           Log level: ERROR, WARN, INFO, DEBUG (default: INFO)");
        System.out.println("  -h, --help                        Show this help message");
    }

    public MavenConfig getMavenConfig() {
        MavenConfig mavenConfig = new MavenConfig();
        mavenConfig.setExecutable(mavenExecutable);
        mavenConfig.setSettingsFile(mavenSettings);
        mavenConfig.setLocalRepository(mavenLocalRepository);
        return mavenConfig;
    }
}



