package cn.sexycode.office;

public class Config {
    private String workDir = System.getProperty("temp.dir");

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
}
