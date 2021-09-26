package top.herouu.split;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import top.herouu.util.FileUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Slf4j
public class SplitFile {

    @SneakyThrows
    public static void main(String[] args) {
        // String filePath = "C:\\Users\\86138\\Desktop\\lugusun_english.txt";
        String filePath = "C:\\Users\\86138\\Desktop\\牛津英汉汉英\\Oxford Chinese Dictionary.txt";
        String outDir = "/opt1";
        String lineSeparator = System.lineSeparator();
        File file = FileUtils.getFile(filePath);
        String str;
        Integer count = 50;
        Integer countSize = 0;
        Integer fileIndex = 0;
        Integer padLength = 6;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        while ((str = bufferedReader.readLine()) != null) {
            sb.append(str).append(lineSeparator);
            if (StringUtils.isNotBlank(str) && StringUtils.containsNone(str, '<', '>')) {
                sb.append("<br/>").append(lineSeparator);
            }
            if ("</>".equals(str)) {
                sb.append(lineSeparator).append("<br/>%%%<br/>").append(lineSeparator);
                countSize++;
            }
            if (count.equals(countSize)) {
                // 写入新文件
                fileIndex++;
                String fileName = StringUtils.leftPad(String.valueOf(fileIndex), padLength, "0");
                FileUtil.writeFile(outDir, sb, fileName);
                log.info("写入{}", fileName);
                sb.delete(0, sb.length());
                countSize = 0;
            }
        }
        // sb中有内容未进行输出
        if (sb.length() > 0) {
            fileIndex++;
            String fileName = StringUtils.leftPad(String.valueOf(fileIndex), padLength, "0");
            FileUtil.writeFile(outDir, sb, fileName);
            log.info("写入{}", fileName);
            sb.delete(0, sb.length());
        }
    }
}
