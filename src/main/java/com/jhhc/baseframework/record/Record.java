package com.jhhc.baseframework.record;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 *
 * @author yecq
 */
@Component
@Scope("prototype")     // 不注明这个，在使用时会出现问题，导致各个对象公用一个record
public class Record {

    protected String id;    // 用于modify、getInfo、delete等
    protected Map<String, Object> hv;   // 用于add和exist的
    protected String table;     // 表名
    protected Set<String> header; // 存储表头名称
    private static boolean log = false;      // 是否显示sql语句

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    public static void setSqlShow(boolean b) {
        log = b;
    }

    // 用于创建新对象
    public void init0(String table, Map<String, Object> header_value) {
        if (table == null || table.trim().equals("")) {
            throw new IllegalArgumentException("表名为空");
        }
        table = table.trim();
        if (this.jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate没有自动注入");
        }
        checkTable(table);
        this.table = table;
        pubIntoHeader();
        checkHv(header_value);
        this.hv = header_value;
        this.id = null;
    }

    // 用于使用已有对象
    public void init1(String table, String id) {
        if (table == null || table.trim().equals("")) {
            throw new IllegalArgumentException("表名为空");
        }
        table = table.trim();
        if (this.jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate没有自动注入");
        }
        checkTable(table);
        this.table = table;
        pubIntoHeader();
        checkId(id);
        this.id = id;
        this.hv = null;
    }

    private void check0() {
        if (this.jdbcTemplate == null || this.table == null || this.hv == null) {
            throw new IllegalStateException("Record对象没有正确初始化，请调用init0()方法");
        }
    }

    private void check1() {
        if (this.jdbcTemplate == null || this.table == null || this.id == null) {
            throw new IllegalStateException("Record对象没有正确初始化，请调用init1()方法");
        }
    }

    public String add() {
        check0();
        String header = "";
        String value = "";
        final Object[] o = new Object[this.hv.size()];
        int i = 0;
        Iterator<Entry<String, Object>> ite = this.hv.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, Object> en = ite.next();
            header += en.getKey() + ",";
            o[i++] = en.getValue();
            value += "?,";
        }
        if (value.length() > 0) {
            header = header.substring(0, header.length() - 1);
            value = value.substring(0, value.length() - 1);
        }
        final String stmt = "insert into " + this.table + " (" + header + ") values(" + value + ")";
        if (log) {
            System.out.println(stmt);
        }

        // 取得id值
        KeyHolder kh = new GeneratedKeyHolder();
        this.jdbcTemplate.update(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
                for (int i = 1; i <= hv.size(); i++) {
                    ps.setObject(i, o[i - 1]);
                }
                return ps;
            }
        }, kh);
        return kh.getKey() + "";
    }

    public void delete() {
        check1();
        final String stmt = "delete from " + this.table + " where id = ?";
        if (log) {
            System.out.println(stmt);
        }
        int i = this.jdbcTemplate.update(stmt, new Object[]{this.id});
    }

    public Map<String, Object> getInfo() {
        check1();
        String stmt = "select * from " + this.table + " where id = ?";
        if (log) {
            System.out.println(stmt);
        }
        final Map<String, Object> ret = new HashMap();
        this.jdbcTemplate.query(stmt, new Object[]{this.id}, new RowCallbackHandler() {

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

    public void modify(Map<String, Object> hv) {
        check1();
        checkHv(hv);
        String stmt = "update " + this.table + " set ";
        String h = "";
        Object[] o = new Object[hv.size() + 1];
        int i = 0;
        Iterator<Entry<String, Object>> ite = hv.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, Object> en = ite.next();
            h += en.getKey() + "=?,";
            o[i++] = en.getValue();
        }
        if (h.length() > 0) {
            h = h.substring(0, h.length() - 1);
        }
        o[i] = this.id;
        stmt += h + " where id=?";
        if (log) {
            System.out.println(stmt);
        }
        this.jdbcTemplate.update(stmt, o);
    }

    public String[] exist() {
        check0();
        String stmt = "select id from " + this.table + " where ";
        String h = "";
        Object[] o = new Object[this.hv.size()];
        int i = 0;
        Iterator<Entry<String, Object>> ite = this.hv.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, Object> en = ite.next();
            h += en.getKey() + "=? and ";
            o[i++] = en.getValue();
        }
        if (h.length() > 0) {
            h = h.substring(0, h.length() - 5);
        }
        stmt += h;
        if (log) {
            System.out.println(stmt);
        }
        List<String> ids = this.jdbcTemplate.query(stmt, o, new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getString("id");
            }
        });

        // 转化为返回的String[]
        String[] ret = new String[ids.size()];
        for (int k = 0; k < ret.length; k++) {
            ret[k] = (String) ids.get(k);
        }

        return ret;
    }

    public Object getInfo(String header) {
        check1();
        String stmt = "select " + header + " from " + this.table + " where id =? ";
        if (log) {
            System.out.println(stmt);
        }
        List<Map<String, Object>> li = this.jdbcTemplate.query(stmt, new Object[]{this.id}, new RowMapper<Map<String, Object>>() {

            @Override
            public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
                Map<String, Object> map = new HashMap();
                ResultSetMetaData meta = rs.getMetaData();
                map.put(meta.getColumnName(1), rs.getObject(1));
                return map;
            }
        });

        Map<String, Object> mm = li.get(0);
        return mm.get(header);
    }

    public String[] getHeader() {
        SqlRowSet set = this.jdbcTemplate.queryForRowSet("select * from " + this.table);
        String[] names = set.getMetaData().getColumnNames();
        return names;
    }

    // 检查表是否存在
    private void checkTable(String table) {
        Connection con = null;
        try {
//            DatabaseMetaData meta = this.jdbcTemplate.getDataSource().getConnection().getMetaData();
            con = this.dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String nm = rs.getString("TABLE_NAME");
                if (table.equals(nm)) {
                    return;
                }
            }
            throw new IllegalArgumentException("表" + table + "不存在");
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

    private void pubIntoHeader() {
        this.header = new HashSet();
        String[] h = getHeader();
        for (int i = 0; i < h.length; i++) {
            this.header.add(h[i]);
        }
    }

    private void checkId(String id1) {
        String stmt = "select * from " + this.table + " where id=?";
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(stmt, new Object[]{id1});
        if (list.isEmpty()) {
            throw new IllegalStateException("记录不存在");
        }
    }

    // 可以允许hv为null，已便于某些反射方法创建对象
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

    // 获取所有外键，每一个字符串格式为 本表.字段->表.字段
    public String[] getForeignKeys() {
        Connection con = null;
        try {
//            DatabaseMetaData meta = this.jdbcTemplate.getDataSource().getConnection().getMetaData();
            con = this.dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getImportedKeys(null, null, this.table);
            List<String> ret = new LinkedList();
            while (rs.next()) {
                // 3为外键表，4为关联的外键字段，7为本表，8为本表外键字段
                ret.add(rs.getObject(7) + "." + rs.getObject(8) + "->" + rs.getObject(3) + "." + rs.getObject(4));
            }
            String[] ret1 = new String[ret.size()];
            return ret.toArray(ret1);

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
}
