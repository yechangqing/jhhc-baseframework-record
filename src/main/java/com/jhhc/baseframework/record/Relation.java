package com.jhhc.baseframework.record;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author yecq
 */
@Component
@Scope("prototype")     // 不注明这个，在使用时会出现问题，导致各个对象公用一个record
public class Relation {

    protected String table;
    private static boolean log = false;

    public static void setSqlShow(boolean b) {
        log = b;
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected String where;
    protected String[] order;

    public void init1(String table, String where, String[] order) {
        if (this.jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate没有自动注入");
        }
        this.table = table;
        this.where = where;
        this.order = order;
    }

    private void check() {
        if (this.jdbcTemplate == null || this.table == null) {
            throw new IllegalStateException("Relation对象没有正确初始化，请调用init()方法");
        }
    }

    public List<Map<String, Object>> getRows() {
        check();
        String stmt = "select * from " + this.table;
        if (this.where != null) {
            stmt += " where " + this.where;
        }

        if (this.order != null) {
            String or = "";
            for (int i = 0; i < order.length; i++) {
                or += this.order[i] + ",";
            }
            if (or.length() > 0) {
                or = or.substring(0, or.length() - 1);
            }
            stmt += " order by " + or;
        }

        if (log) {
            System.out.println(stmt);
        }

        List<Map<String, Object>> li = this.jdbcTemplate.query(stmt, new RowMapper<Map<String, Object>>() {

            @Override
            public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
                Map<String, Object> map = new HashMap();
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int k = 1; k <= cols; k++) {
                    map.put(meta.getColumnName(k), rs.getObject(k));
                }
                return map;
            }
        });
        return li;
    }

    public int getCount() {
        check();
        return getRows().size();
    }

}
