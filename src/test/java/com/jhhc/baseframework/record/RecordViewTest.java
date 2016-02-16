package com.jhhc.baseframework.record;

import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yecq
 */
public class RecordViewTest extends Base {

    @Autowired
    private SqlOperator sql;

    @Autowired
    private RecordView recv;

    @Test
    public void test_init1() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("视图abcdf不存在");
        Map ids = new HashMap();
        ids.put("id", "1");
        this.recv.init1("abcdf", ids);
    }

    @Test
    public void test1_init1() {
        Map ids = new HashMap();
        ids.put("user_id", "1");
        this.recv.init1("v_user", ids);
    }

    @Test
    public void test2_init1() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("属性值为null");
        this.recv.init1("v_user", null);
    }

    @Test
    public void test3_init1() {
        this.recv.init1("v_user", new HashMap());
    }

    @Test
    public void testAdd() {
        expectedEx.expect(UnsupportedOperationException.class);
        expectedEx.expectMessage("视图不能进行增加方法");
        Map map = new HashMap();
        map.put("user_id", 3);
        map.put("stu_id", 11);
        recv.init1("v_user", map);
        recv.add();
    }

    @Test
    public void testDelete() {
        expectedEx.expect(UnsupportedOperationException.class);
        expectedEx.expectMessage("视图不能进行删除方法");
        Map map = new HashMap();
        map.put("user_id", 3);
        map.put("stu_id", 11);
        recv.init1("v_user", map);
        recv.delete();
    }

    @Test
    public void testModify() {
        expectedEx.expect(UnsupportedOperationException.class);
        expectedEx.expectMessage("视图不能进行修改方法");
        Map map = new HashMap();
        map.put("user_id", 3);
        map.put("stu_id", 11);
        recv.init1("v_user", map);
        recv.modify(new HashMap());
    }

    @Test
    public void testExist() {
        expectedEx.expect(UnsupportedOperationException.class);
        expectedEx.expectMessage("视图暂时无此方法");
        Map map = new HashMap();
        map.put("user_id", 3);
        map.put("stu_id", 11);
        recv.init1("v_user", map);
        recv.exist();
    }

    @Test
    public void testGetInfo() {
        Map map = new HashMap();
        map.put("user_id", 3);
        map.put("stu_id", 11);
        recv.init1("v_user", map);
        Map<String, Object> ret = recv.getInfo();
        assertThat(ret.size(), greaterThan(0));
        String name = ret.get("name") + "";
        String age = ret.get("age") + "";
        String depart = ret.get("depart") + "";
        assertThat(name, is("qindeyu"));
        assertThat(age, is("21"));
        assertThat(depart, is("ise"));

        // 测只有一个参数的情况
        map.clear();
        map.put("user_id", 3);
        recv.init1("v_user", map);
        ret = recv.getInfo();
        assertThat(ret.size(), greaterThan(0));
        name = ret.get("name") + "";
        age = ret.get("age") + "";
        depart = ret.get("depart") + "";
        assertThat(name, is("qindeyu"));
        assertThat(age, is("21"));
        assertThat(depart, is("ise"));
    }

    @Test
    public void testGetInfo1() {
        Map map = new HashMap();
        map.put("user_id", 4);
        map.put("stu_id", 12);
        recv.init1("v_user", map);
        String name = recv.getInfo("name") + "";
        String age = recv.getInfo("age") + "";
        String depart = recv.getInfo("depart") + "";
        assertThat(name, is("sunwenqin"));
        assertThat(age, is("20"));
        assertThat(depart, is("ise"));
    }

    @Test
    public void test_getHeader() {
        Map map = new HashMap();
        map.put("user_id", "1");
        recv.init1("v_user", map);
        String[] names = recv.getHeader();
        assertThat(names.length, is(5));
        assertThat(names[0], is("name"));
        assertThat(names[1], is("age"));
        assertThat(names[2], is("depart"));
        assertThat(names[3], is("user_id"));
        assertThat(names[4], is("stu_id"));
    }

    @Test
    public void test_checkHv() {
        Map<String, String> hv = new HashMap();
        hv.put("id1", "3");
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("没有属性id1");
        this.recv.init1("v_user", hv);
    }

    @Test
    public void test_checkTable() {
        this.expectedEx.expect(IllegalArgumentException.class);
        this.expectedEx.expectMessage("视图v_user11不存在");
        Map<String, String> ids = new HashMap();
        ids.put("user_id", "1");
        this.recv.init1("v_user11", ids);
    }

    @Test
    public void test1_checkTable() {
        Map<String, String> ids = new HashMap();
        ids.put("user_id", "1");
        this.recv.init1("v_user", ids);
    }
}
