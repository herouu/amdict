package top.herouu.util;

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import top.herouu.dao.BaseDao;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test extends BaseDao {

    public static void main(String[] args) throws SQLException {
        String str = "See new";
        System.out.println(StringUtils.indexOf(str, "See "));

        // String insert = "insert into niujingaojie8(word_name,word_value) values(?,?)";
        // queryRunner.insert(insert, new ScalarHandler<>(), "test", "你好");

        String wname = "ˌbloody-ˈminded";
        // Pattern pattern = Pattern.compile("ˌ|ˈ"); //去掉空格符合换行符
        // Matcher matcher = pattern.matcher(wname);
        // String result = matcher.replaceAll("");
        String result = RegExUtils.removeAll(wname, "ˌ|ˈ");
        System.out.println(result);

    }
}
