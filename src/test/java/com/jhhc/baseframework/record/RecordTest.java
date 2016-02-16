package com.jhhc.baseframework.record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yecq
 */
public class RecordTest extends Base {

    @Autowired
    private Record record;

    @Autowired
    private SqlOperator sql;

    @Test
    public void test_init1() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("表abcdf不存在");
        record.init1("abcdf", "1");
    }

    @Test
    public void test1_init1() {
        record.init1("user", "1");
    }

    @Test
    public void test_init0() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("表abcdf不存在");
        Map hv1 = new HashMap();
        hv1.put("name", "asdf");
        record.init0("abcdf", hv1);
    }

    @Test
    public void test1_init0() {
        Map hv1 = new HashMap();
        hv1.put("name", "asdf");
        record.init0("user", hv1);
    }

    @Test
    public void test2_init0() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("属性值为null");
        record.init0("user", null);
    }

    @Test
    public void test3_init0() {
        record.init0("user", new HashMap());
    }

    @Test
    public void testAdd() {
        Map map = new HashMap();
        map.put("name", "test1");
        map.put("age", 33);
        map.put("id", 9);
        record.init0("user", map);
        String id = record.add();
        assertThat(id, equalTo("9"));
        record.init1("user", id);
        String name = record.getInfo("name") + "";
        String age = record.getInfo("age") + "";
        assertThat(name, equalTo("test1"));
        assertThat(age, equalTo("33"));
    }

    @Test
    // 测试空的插入
    public void testNullAdd() {
        Map map = new HashMap();
        map.put("id", "101");
        record.init0("user", map);
        String id = record.add();
        assertThat(id, is("101"));
    }

    @Test
    public void testDelete() {
        record.init1("user", "1");
        // 验证当时是有数据的
        String id = record.getInfo("id") + "";
        assertThat(id, is("1"));
        record.delete();
        List list = sql.query("select id from user where id = ?", new Object[]{1});
        assertThat(list.size(), is(0));
    }

    @Test
    public void testModify() {
        record.init1("user", "1");
        record.setSqlShow(true);
        Map map = new HashMap();
        map.put("name", "modify");
        map.put("age", 100);
        record.modify(map);

        String name = record.getInfo("name") + "";
        String age = record.getInfo("age") + "";
        assertThat(name, is("modify"));
        assertThat(age, is("100"));
    }

    @Test
    public void test1_modify() {
        record.init1("user", "1");
        Map map = new HashMap();
        map.put("name", "modify");
        map.put("age1", 100);
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("没有属性age1");
        record.modify(map);
    }

    @Test
    public void test_getHeader() {
        Map<String, Object> hv1 = new HashMap();
        hv1.put("value", "value1");
        hv1.put("text", "text1");
        this.record.init0("name", hv1);
        String[] names = record.getHeader();
        assertThat(names.length, is(3));
        assertThat(names[0], is("id"));
        assertThat(names[1], is("value"));
        assertThat(names[2], is("text"));
    }

    @Test
    public void test_checkHv() {
        Map<String, Object> hv = new HashMap();
        hv.put("name", "abcd");
        hv.put("age1", 21);
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("没有属性age1");
        this.record.init0("user", hv);
    }

    @Test
    public void test_checkId() {
        this.expectedEx.expect(IllegalStateException.class);
        this.expectedEx.expectMessage("记录不存在");
        this.record.init1("user", "5");
    }

    @Test
    public void test_checkTable() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("表user1不存在");
        this.record.init1("user1", "3");
    }

    @Test
    public void test1_checkTable() {
        this.record.init1("user", "3");
    }

    @Test
    public void test_getForeignKeys() {
        this.record.init1("info", "2");
        String[] keys = this.record.getForeignKeys();
        assertThat(keys.length, is(1));
        for (int i = 0; i < keys.length; i++) {
            String[] tmp = keys[i].split("->");
            String[] t1 = tmp[0].split("\\.");
            String my = t1[0];
            String my_id = t1[1];
            t1 = tmp[1].split("\\.");
            String to = t1[0];
            String to_id = t1[1];
            assertThat(my, is("info"));
            assertThat(my_id, is("user_id"));
            assertThat(to, is("user"));
            assertThat(to_id, is("id"));
        }
    }
}
