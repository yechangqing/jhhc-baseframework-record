package com.jhhc.baseframework.record;

import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Before和@After都是一样的，作为基类 
 * 类上的注解能够被继承，所以子类就不必再重复写注解了，只需要实现@Test方法
 * @author yecq
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class Base {

    @Autowired
    protected Prepare prepare;

    // 测试异常用的，必须为public
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void before() throws DatabaseUnitException, SQLException {
        // 初始化数据库
            DatabaseOperation.CLEAN_INSERT.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.DELETE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.DELETE_ALL.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//        DatabaseOperation.INSERT.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.NONE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.REFRESH.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.TRUNCATE_TABLE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.UPDATE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());

    }

    @After
    public void after() throws DatabaseUnitException, SQLException {
        // 恢复数据库
//            DatabaseOperation.CLEAN_INSERT.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.DELETE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
        DatabaseOperation.DELETE_ALL.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.INSERT.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.NONE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.REFRESH.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.TRUNCATE_TABLE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());
//            DatabaseOperation.UPDATE.execute(prepare.getIDatabaseConnection(), prepare.getIDataSet());

    }
}
