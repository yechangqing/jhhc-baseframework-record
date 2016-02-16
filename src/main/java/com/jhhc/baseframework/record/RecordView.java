package com.jhhc.baseframework.record;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 *
 * @author yecq
 */
@Component
@Scope("prototype")     // 不注明这个，在使用时会出现问题，导致各个对象公用一个record
public class RecordView {

    protected Map<String, String> ids;     // 主键们
    protected String table;
    protected Set<String> header; // 存储表头名称
    private static boolean log = false;

    public static void setSqlShow(boolean b) {
        log = b;
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    // 用于创建新对象
    public void init1(String table, Map<String, String> ids) {
        if (table == null || table.trim().equals("")) {
            throw new IllegalArgumentException("表名为空");
        }
        table = table.trim();
        if (this.jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate没有自动注入");
        }
        checkTable(table);
        this.table = table;
        this.header = new HashSet();
        putIntoHeader();
        checkHv(ids);
        this.ids = ids;
    }

    private void check1() {
        if (this.jdbcTemplate == null || this.table == null || this.ids == null) {
            throw new IllegalStateException("Record对象没有正确初始化，请调用init1()方法");
        }
    }

    public String add() {
        throw new UnsupportedOperationException("视图不能进行增加方法"); //To change body of generated methods, choose Tools | Templates.
    }

    public void delete() {
        throw new UnsupportedOperationException("视图不能进行删除方法"); //To change body of generated methods, choose Tools | Templates.
    }

    public void modify(Map<String, Object> hv) {
        throw new UnsupportedOperationException("视图不能进行修改方法"); //To change body of generated methods, choose Tools | Templates.
    }

    public Object getInfo(String header) {
        check1();
        Map<String, Object> map = getInfo();
        return map.get(header);
    }

    public String[] exist() {
        throw new UnsupportedOperationException("视图暂时无此方法");
    }

    public Map<String, Object> getInfo() {
        check1();
        String stmt = "";
        Object[] o = new Object[this.ids.size()];
        int i = 0;
        Iterator<Map.Entry<String, String>> ite = this.ids.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> en = ite.next();
            stmt += en.getKey() + "=? and ";
            o[i++] = en.getValue();
        }

        if (stmt.length() > 0) {
            stmt = stmt.substring(0, stmt.length() - 5);
        }

        stmt = "select * from " + this.table + " where " + stmt;
        if (log) {
            System.out.println(stmt);
        }
        final Map<String, Object> ret = new HashMap();
        this.jdbcTemplate.query(stmt, o, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
//                while (rs.next()) {       // 放这里是为了说明，rs.next操作已经在spring里面做过了，这里只需取值
                for (int i = 1; i <= cols; i++) {
                    ret.put(meta.getColumnName(i), rs.getObject(i));
                }
            }
        });
        return ret;
    }

    public String[] getHeader() {
        SqlRowSet set = this.jdbcTemplate.queryForRowSet("select * from " + this.table);
        String[] names = set.getMetaData().getColumnNames();
        return names;
    }

    private void putIntoHeader() {
        String[] h = getHeader();
        for (int i = 0; i < h.length; i++) {
            this.header.add(h[i]);
        }
    }

    // 检查表是否存在
    private void checkTable(String table) {
        Connection con = null;
        try {
//            DatabaseMetaData meta = this.jdbcTemplate.getDataSource().getConnection().getMetaData();
            con = this.dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[]{"VIEW"});
            while (rs.next()) {
                String nm = rs.getString("TABLE_NAME");
                if (table.equals(nm)) {
                    return;
                }
            }
            throw new IllegalArgumentException("视图" + table + "不存在");
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage()) {
            };
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {

                }
            }
        }
    }

    // 允许hv为null
    private void checkHv(Map hv1) {
        if (hv1 == null) {
            throw new IllegalArgumentException("属性值为null");
        }
        Iterator<String> ite = hv1.keySet().iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            if (!this.header.contains(key)) {
                throw new IllegalArgumentException("没有属性" + key);
            }
        }
    }
}
