package com.jhhc.baseframework.record;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yecq
 */
public class SqlOperatorTest extends Base {

    @Autowired
    private SqlOperator sql;

    @Test
    public void testUpdate() {
        // 先证明该条数据存在
        String stmt = "select id from student where name=? and depart=?";
        List<Map<String, Object>> list = sql.query(stmt, new Object[]{"qindeyu", "ise"});
        assertThat(list.size(), greaterThan(0));
        String id = list.get(0).get("id") + "";
        stmt = "update student set name=?, depart=? where id = ?";
        sql.update(stmt, new Object[]{"what", "art", id});
        stmt = "select * from student where id=?";
        list = sql.query(stmt, new Object[]{id});
        assertThat(list.size(), greaterThan(0));
        Map<String, Object> map = list.get(0);
        String name = map.get("name") + "";
        String depart = map.get("depart") + "";
        assertThat(name, is("what"));
        assertThat(depart, is("art"));
    }

    @Test
    public void testDelete() {
        // 先证明该条数据存在
        String stmt = "select * from student where name=? and depart=?";
        List list = sql.query(stmt, new Object[]{"qindeyu", "ise"});
        assertThat(list.size(), greaterThan(0));
        stmt = "delete from student where name=? and depart=?";
        sql.update(stmt, new Object[]{"abcd", "ise"});
        stmt = "select * from student where name=? and depart=?";
        list = sql.query(stmt, new Object[]{"abcd", "ise"});
        assertThat(list.size(), is(0));
    }

    @Test
    public void testInsert() {
        String stmt = "insert into user(id,name,age) values(?,?,?)";
        String[] id = sql.insert(stmt, new Object[]{10, "mamama", 43});
        assertThat(id[0], is("10"));
    }

    @Test
    public void test_getHeader() {
        String[] names = sql.getHeader("v_user");
        assertThat(names.length, is(5));
        assertThat(names[0], is("name"));
        assertThat(names[1], is("age"));
        assertThat(names[2], is("depart"));
        assertThat(names[3], is("user_id"));
        assertThat(names[4], is("stu_id"));
    }

    @Test
    public void test1_getHeader() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("表v_user12不存在");
        String[] names = sql.getHeader("v_user12");
    }

    @Test
    public void test_showCorssReference() {
        String[] ret = sql.getCrossReference("info", "user");
        assertThat(ret.length, is(1));
        System.out.println(ret[0]);
        assertThat(ret[0], is("info.user_id->user.id"));
    }

    @Test
    public void test_checkHeader() {
        Set<String> headers = new HashSet();
        headers.add("id");
        headers.add("name");
        headers.add("age");
        sql.checkHeader("user", headers);

        headers.clear();
        headers.add("name");
        headers.add("age");
        headers.add("depart");
        headers.add("user_id");
        headers.add("stu_id");
        sql.checkHeader("v_user", headers);
    }

    @Test
    public void test1_checkHeader() {
        Set<String> headers = new HashSet();
        headers.add("id");
        headers.add("name");
        headers.add("age1");
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("不含有属性age1");
        sql.checkHeader("user", headers);
    }

    @Test
    public void test2_checkHeader() {
        Set<String> headers = new HashSet();
        headers.add("name");
        headers.add("age");
        headers.add("depart");
        headers.add("user_id1");
        headers.add("stu_id");
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("不含有属性user_id1");
        sql.checkHeader("v_user", headers);
    }

    @Test
    public void test3_checkHeader() {
        Set<String> headers = new HashSet();
        headers.add("id");
        headers.add("name");
        headers.add("age1");
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("表user12不存在");
        sql.checkHeader("user12", headers);
    }
}
