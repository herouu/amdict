package top.herouu.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import top.herouu.model.Word;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class BaseDao {
    protected static final QueryRunner queryRunner;

    //异步查询对象
    protected static final AsyncQueryRunner asyncQueryRunner;

    //线程池
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    static {
        HikariConfig config = new HikariConfig("/db.properties");
        HikariDataSource dataSource = new HikariDataSource(config);
        queryRunner = new QueryRunner(dataSource);

        //初始化异步查询对象
        asyncQueryRunner = new AsyncQueryRunner(executorService, queryRunner);
    }

    public static void insertWord(String tableName, List<Word> wordList) throws SQLException {
        String insert = "insert into " + tableName + "(word_name,word_value) values(?,?)";
        for (Word word : wordList) {
            log.info("{}", word);
            queryRunner.insert(insert, new ScalarHandler<>(), word.getWordName(), word.getWordValue());
        }
    }

    @SneakyThrows
    public static void truncateTable(String tableName) {
        String sql = "truncate table " + tableName;
        queryRunner.query(sql, new ScalarHandler<>());
        log.info("清库完毕！");
    }

}