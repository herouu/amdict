package top.herouu.split;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import top.herouu.dao.BaseDao;
import top.herouu.model.Word;
import top.herouu.util.SeleniumUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class SelenumInsert {


    public static void main(String[] args) {
        String tableName = "lugusun_english";
        String dirPath = "/opt";
        WebDriver webDriver = SeleniumUtil.getWebDriver("C:\\Users\\86138\\Desktop\\chromedriver.exe");
        BaseDao.truncateTable(tableName);
        try {
            selenium(webDriver, dirPath, tableName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            webDriver.close();
        }
    }

    static void selenium(WebDriver driver, String dirPath, String tableName) throws SQLException {
        File file = FileUtils.getFile(dirPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                if ("css".equals(StringUtils.substringAfterLast(file1.getName(), "."))) {
                    continue;
                }
                driver.get(file1.getAbsolutePath());
                driver.findElement(By.xpath("//body")).sendKeys(Keys.chord(Keys.CONTROL, "a"));
                driver.findElement(By.xpath("//body")).sendKeys(Keys.chord(Keys.CONTROL, "c"));
                String sysClipboardText = SeleniumUtil.getSysClipboardText();

                // log.info(sysClipboardText);
                List<Word> wordList = handlerText(sysClipboardText);

                BaseDao.insertWord(tableName, wordList);
                log.info("handle file:{} over! 已入库！", file1.getName());
            }
        }
    }

    @SneakyThrows
    private static List<Word> handlerText(String sysClipboardText) {
        BufferedReader reader = new BufferedReader(new StringReader(sysClipboardText));
        String str;
        StringBuilder sb = new StringBuilder();
        String wordN = null;
        ArrayList<Word> words = new ArrayList<>();
        Boolean wordFlag = true;
        while ((str = reader.readLine()) != null) {
            if (StringUtils.isBlank(str)) {
                continue;
            }
            if (StringUtils.equals("%%%", str)) {
                if (sb.length() != 0) {
                    addElement(words, wordN, sb.toString());
                }
                sb.delete(0, sb.length());
                wordFlag = true;
                continue;
            }
            // value
            if (wordFlag) {
                wordN = str;
                wordFlag = false;
                continue;
            }
            sb.append(str).append("\n");
        }
        // 文档读取完毕
        addElement(words, wordN, sb.toString());
        return words;
    }

    private static void addElement(List<Word> wordList, String wordN, String wordValue) {
        if (StringUtils.isEmpty(wordValue)) {
            return;
        }
        Iterator<Word> iterator = wordList.iterator();
        while (iterator.hasNext()) {
            Word next = iterator.next();
            if (next.getWordName().equals(wordN)) {
                return;
            }
        }
        Word word = new Word();
        word.setWordName(RegExUtils.removeAll(wordN, "ˌ|ˈ"));
        word.setWordValue(wordValue);
        wordList.add(word);
    }

}
