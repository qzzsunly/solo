package cn.sexycode.util.core.file;

import cn.sexycode.util.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author qinzaizhen
 */
public class ZipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * @param replaceEntry    替换zip的文件路径以及对应的文件流
     * @param zipInputStream  源zip
     * @param zipOutputStream 目标zip
     */
    public static void replaceItem(Map<String, InputStream> replaceEntry, ZipInputStream zipInputStream,
            ZipOutputStream zipOutputStream) throws IOException {
        //
        ZipEntry entryIn;
        try {
            while ((entryIn = zipInputStream.getNextEntry()) != null) {
                String entryName = entryIn.getName();
                ZipEntry entryOut = new ZipEntry(entryName);
                // 只使用 name
                zipOutputStream.putNextEntry(entryOut);
                if (replaceEntry.containsKey(entryName)) {
                    InputStream in = replaceEntry.get(entryName);
                    if (in != null) {
                        // 使用替换流（替换文字内容）
                        zipOutputStream.write(IOUtils.toByteArray(in));
                        in.close();
                    }
                } else {
                    // 输出普通Zip流
                    zipOutputStream.write(IOUtils.toByteArray(zipInputStream));
                }

                // 关闭此 entry
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            LOGGER.error("失败", e);
            throw e;
        } finally {
            close(zipInputStream);
            close(zipOutputStream);
        }
    }

    /**
     * zip解压
     *
     * @param srcFile     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(File srcFile, String destDirPath) {
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        // 开始解压
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                LOGGER.debug("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new RuntimeException("解压失败, 无法创建目录" + dir.getAbsolutePath());
                    }
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        if (!targetFile.getParentFile().mkdirs()) {
                            throw new RuntimeException("解压失败, 无法创建目录" + targetFile.getParentFile().getAbsolutePath());
                        }
                    }
                    if (!targetFile.exists() && !targetFile.createNewFile()) {
                        throw new RuntimeException("解压失败, 无法创建文件" + targetFile.getAbsolutePath());
                    }
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    fos.write(IOUtils.toByteArray(is));
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            LOGGER.debug("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            LOGGER.error("异常", e);
            throw new RuntimeException("unzip error", e);
        }
    }

    /**
     * 包装输入流
     */
    public static ZipInputStream wrapZipInputStream(InputStream inputStream) {
        return new ZipInputStream(inputStream);
    }

    /**
     * 包装输出流
     */
    public static ZipOutputStream wrapZipOutputStream(OutputStream outputStream) {
        return new ZipOutputStream(outputStream);
    }

    private static void close(InputStream inputStream) {
        if (null != inputStream) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
