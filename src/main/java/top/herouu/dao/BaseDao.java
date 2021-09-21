package top.herouu.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}