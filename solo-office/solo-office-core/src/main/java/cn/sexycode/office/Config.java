package cn.sexycode.office;

public class Config {
    private static String workDir = System.getProperty("java.io.tmpdir") + "solof";

    public static String getWorkDir() {
        return workDir;
    }

    public static void setWorkDir(String workDir) {
        Config.workDir = workDir;
    }
}
