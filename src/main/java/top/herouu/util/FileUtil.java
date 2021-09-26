package top.herouu.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtil {


    public static void writeFile(String dir, StringBuilder strB, String fileName) throws IOException {
        File file = FileUtils.getFile(dir, fileName + ".html");
        if (file.exists()) {
            file.delete();
        }
        FileUtils.touch(file);
        FileUtils.write(file, strB.toString(), StandardCharsets.UTF_8);
    }


    public static void clearFile(String fileDir) {
        File dir = FileUtils.getFile(fileDir);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if ("css".equals(StringUtils.substringAfterLast(file.getName(), "."))) {
                    continue;
                }
                FileUtils.deleteQuietly(file);
            }
        }
    }
}
