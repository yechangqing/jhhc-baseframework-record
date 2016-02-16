package com.jhhc.baseframework.record;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author yecq
 */
@Component
public class Prepare {
//    @Autowired
//    private BasicDataSource dataSource;

    private IDatabaseConnection database;
    private IDataSet dataSet;

    @Autowired
    public Prepare(JdbcTemplate jdbcTemplate) {
        try {
            // 获得IDatabaseConnection，使用spring自行的数据源，这里貌似有问题，因为每次getConnection以后
            //会获得一个新的Connection，而不是dataSource本来的Connection
            this.database = new DatabaseConnection(jdbcTemplate.getDataSource().getConnection());

            // 获得xml数据
            File file = new File("target/test-classes/data.xml");
            this.dataSet = new FlatXmlDataSet(file);    
            
            // excel数据源，支持2003，但不支持更高版本
//            File file1 = new File("target/test-classes/data.xls");
//            this.dataSet = new XlsDataSet(file1);    
        } catch (DatabaseUnitException ex) {
            Logger.getLogger(RecordTest.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(RecordTest.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(RecordTest.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public IDatabaseConnection getIDatabaseConnection() {
        return this.database;
    }

    public IDataSet getIDataSet() {
        return this.dataSet;
    }

}
