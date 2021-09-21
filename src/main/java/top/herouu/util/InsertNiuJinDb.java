package top.herouu.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import top.herouu.dao.BaseDao;
import top.herouu.model.CompressedRecord;
import top.herouu.model.Dictionary;
import top.herouu.model.Word;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

@Slf4j
public class InsertNiuJinDb extends BaseDao {


    private static final int size = 100;
    private static final String OPT_DIR = "C:\\Users\\86138\\IdeaProjects\\dict\\opt";
    private static final String tableName = "niujingaojie8";

    public static void query(String dictName) throws IOException {
        Dictionary dict = DictionaryQuerier.getDicts().get(dictName);
        if (dict == null) {
            return;
        }
        List<String> keys = dict.getOriKeys();
        HashMap<Long, CompressedRecord> recordsMap = dict.getRecords();
        StringBuilder strB = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            //确定要显示的词，拿到偏移量
            String item = keys.get(i);
            Long wordOffset = dict.getOffsets().get(item);

            //根据偏移量定位到块
            long pre = 0;
            Set<Long> offSets = dict.getRecords().keySet();
            for (Long offSet : offSets) {
                if (wordOffset < offSet) {
                    break;
                } else if (wordOffset >= offSet) {
                    pre = offSet;
                    continue;
                }
            }

            //拿出记录块，从里面解压出对应的词条
            long position = wordOffset - pre;
            CompressedRecord record = recordsMap.get(pre);
            strB.append(record.getString(position));

            int length = i + 1;
            int step = length / size;
            String stepName = StringUtils.leftPad(String.valueOf(step), 3, "0");
            if (length % size == 0) {
                log.info("正在生成第{}文件", stepName);
                extracted(strB, stepName);
                strB = new StringBuilder();
            }

            // if (step == 2) {
            //     return;
            // }
            // 最后一个文件
            if (length == keys.size()) {
                log.info("正在生成最后第{}文件", stepName);
                extracted(strB, stepName);
                strB = new StringBuilder();
            }
            if (StringUtils.isNoneBlank(strB.toString())) {
                strB.append("<p>%%%</p>");
            }
        }

    }

    private static void extracted(StringBuilder strB, String stepName) throws IOException {
        File file = FileUtils.getFile(OPT_DIR, stepName + ".html");
        if (file.exists()) {
            file.delete();
        }
        FileUtils.touch(file);
        FileUtils.write(file, strB.toString(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws InterruptedException, IOException, SQLException {
        // 遍历词典
        query("牛津高阶8简体");
        // 清理表
        clearTable(tableName);
        WebDriver webDriver = getWebDriver();
        selenium(webDriver);
        // 关闭浏览器
        webDriver.close();
        // 清理文件
        clearFile();
    }

    public static void dbHandler(List<Word> wordList) throws SQLException {
        String insert = "insert into niujingaojie8(word_name,word_value) values(?,?)";
        for (Word word : wordList) {
            log.info("{}", word);
            queryRunner.insert(insert, new ScalarHandler<>(), word.getWordName(), word.getWordValue());
        }
    }

    private static void clearTable(String tableName) throws SQLException {
        String sql = "truncate table " + tableName;
        queryRunner.query(sql, new ScalarHandler<>());
        log.info("清库完毕！");
    }

    private static void clearFile() {
        File dir = FileUtils.getFile(OPT_DIR);
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


    @SneakyThrows
    static void selenium(WebDriver driver) throws InterruptedException {
        File file = FileUtils.getFile(OPT_DIR);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                if ("css".equals(StringUtils.substringAfterLast(file1.getName(), "."))) {
                    continue;
                }
                driver.get(file1.getAbsolutePath());
                driver.findElement(By.xpath("//body")).sendKeys(Keys.chord(Keys.CONTROL, "a"));
                driver.findElement(By.xpath("//body")).sendKeys(Keys.chord(Keys.CONTROL, "c"));
                String sysClipboardText = getSysClipboardText();

                // log.info(sysClipboardText);
                List<Word> maps = handlerText(sysClipboardText);

                dbHandler(maps);
                log.info("handle file:{} over! 已入库！", file1.getName());
            }
        }
    }

    @SneakyThrows
    private static List<Word> handlerText(String sysClipboardText) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(sysClipboardText));
        String str;
        StringBuilder sb = new StringBuilder();
        String wordN = null;
        ArrayList<Word> words = new ArrayList<>();
        while ((str = reader.readLine()) != null) {
            if (StringUtils.isBlank(str)) {
                continue;
            }
            if (StringUtils.indexOf(str, "See ") == 0) {
                continue;
            }
            if (StringUtils.indexOf(str, "@@@LINK=") == 0) {
                continue;
            }
            if (StringUtils.equals("%%%", str)) {
                if (sb.length() != 0) {
                    addElement(words, wordN, sb.toString());
                }
                sb.delete(0, sb.length());
                continue;
            }
            // value
            if (sb.length() == 0) {
                if (StringUtils.countMatches(str, "/") >= 2) {
                    wordN = StringUtils.substringBefore(str, " /");
                    wordN = StringUtils.replace(wordN, "★", "");
                    wordN = StringUtils.replace(wordN, "·", "").trim();
                } else {
                    wordN = StringUtils.substringBefore(str, "\n");
                }
            }
            sb.append(str).append("\n");
        }
        // 文档读取完毕
        addElement(words, wordN, sb.toString());
        return words;
    }

    private static void addElement(List<Word> wordList, String wordN, String wordValue) {
        if (StringUtils.equals(wordN, StringUtils.substringBeforeLast(wordValue, "\n"))) {
            return;
        }
        Word word = new Word();
        word.setWordName(RegExUtils.removeAll(wordN, "ˌ|ˈ"));
        word.setWordValue(wordValue);
        wordList.add(word);
    }

    @NotNull
    private static WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\86138\\Desktop\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        return driver;
    }

    public static String getSysClipboardText() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 获取剪切板中的内容
        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {
            // 检查内容是否是文本类型
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf
                            .getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }
}
