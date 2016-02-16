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
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * JdbcTemplate的sql操作器
 *
 * @author yecq
 */
@Component
@Scope("prototype")     // 不注明这个，在使用时会出现问题，导致各个对象公用一个record
public class SqlOperator {

    private static boolean log = false;

    public static void setSqlShow(boolean b) {
        log = b;
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    // 查询，返回List<Map<String,Object>>
    public List<Map<String, Object>> query(String stmt) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.query(stmt, new RowMapper<Map<String, Object>>() {

            @Override
            public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
                Map<String, Object> map = new HashMap();
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int k = 1; k <= cols; k++) {
//                    System.out.println("catalog="+meta.getCatalogName(k)+", class name ="+meta.getColumnClassName(k)+", label="+meta.getColumnLabel(k)
//                            +", column type="+meta.getColumnTypeName(k)+", name="+meta.getColumnName(k));
                    map.put(meta.getColumnLabel(k), rs.getObject(k));
                }
                return map;
            }
        });
    }

    public List<Map<String, Object>> query(String stmt, Object[] args) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.query(stmt, args, new RowMapper<Map<String, Object>>() {

            @Override
            public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
                Map<String, Object> map = new HashMap();
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int k = 1; k <= cols; k++) {
                    map.put(meta.getColumnLabel(k), rs.getObject(k));
                }
                return map;
            }
        });
    }

    // 插入记录，返回id
    public String[] insert(String stmt) {
        if (log) {
            System.out.println(stmt);
        }
        final String stmt1 = stmt;
        KeyHolder kh = new GeneratedKeyHolder();
        this.jdbc.update(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(stmt1, Statement.RETURN_GENERATED_KEYS);
                return ps;
            }
        }, kh);
        List<Map<String, Object>> ret = kh.getKeyList();
        String[] ids = new String[ret.size()];
        int i = 0;
        Iterator<Map<String, Object>> ite = ret.iterator();
        while (ite.hasNext()) {
            Map<String, Object> map = ite.next();
            ids[i++] = map.get("GENERATED_KEY") + "";
        }
        return ids;
    }

    public String[] insert(String stmt, final Object[] args) {
        if (log) {
            System.out.println(stmt);
        }
        final String stmt1 = stmt;
        KeyHolder kh = new GeneratedKeyHolder();
        this.jdbc.update(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(stmt1, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                return ps;
            }
        }, kh);
        List<Map<String, Object>> ret = kh.getKeyList();
        String[] ids = new String[ret.size()];
        int i = 0;
        Iterator<Map<String, Object>> ite = ret.iterator();
        while (ite.hasNext()) {
            Map<String, Object> map = ite.next();
            ids[i++] = map.get("GENERATED_KEY") + "";
        }
        return ids;
    }

    // 修改
    public int update(String stmt) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.update(stmt);
    }

    public int update(String stmt, Object[] args) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.update(stmt, args);
    }

    // 删除
    public int delete(String stmt) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.update(stmt);
    }

    public int delete(String stmt, Object[] args) {
        if (log) {
            System.out.println(stmt);
        }
        return this.jdbc.update(stmt, args);
    }

    public String[] getHeader(String table) {
        checkTable(table);
        SqlRowSet set = this.jdbc.queryForRowSet("select * from " + table);
        String[] names = set.getMetaData().getColumnNames();
        return names;
    }

    // 返回两个表的外键关系，结构为 from表.字段名->to表.字段名
    public String[] getCrossReference(String from, String to) {
        Connection con = null;
        try {
            con = this.dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCrossReference(null, null, to, null, null, from);
            List<String> ret = new LinkedList();
            while (rs.next()) {
                ret.add(rs.getObject(7) + "." + rs.getObject(8) + "->" + rs.getObject(3) + "." + rs.getObject(4));
            }
            String[] ret1 = new String[ret.size()];
            ret.toArray(ret1);
            return ret1;
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

    // 检查表头
    public void checkHeader(String table, Set<String> headers) {
        String[] h = getHeader(table);
        Set<String> tmp = new HashSet();
        for (int i = 0; i < h.length; i++) {
            tmp.add(h[i]);
        }

        Iterator<String> ite = headers.iterator();
        while (ite.hasNext()) {
            String header = ite.next();
            if (!tmp.contains(header)) {
                throw new IllegalArgumentException("不含有属性" + header);
            }
        }
    }

    public void checkTable(String table) {
        Connection con = null;
        try {
            con = this.dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[]{"VIEW", "TABLE"});
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
}
