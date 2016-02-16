package com.jhhc.baseframework.record;

import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yecq
 */
public class RelationTest extends Base {

    @Autowired
    private Relation rel;

    @Test
    public void testGetRows() {
        String where = "age>20 and id>1";
        String[] order = {"age", "id"};
        rel.init1("user", where, order);
        List<Map<String, Object>> list = rel.getRows();
        assertThat(list.size(), is(2));
        Map<String, Object> map = list.get(0);
        String name = map.get("name") + "";
        String age = map.get("age") + "";
        assertThat(name, is("qindeyu"));
        assertThat(age, is("21"));
        map = list.get(1);
        name = map.get("name") + "";
        age = map.get("age") + "";
        assertThat(name, is("qwer"));
        assertThat(age, is("30"));
    }

    @Test
    public void testGetCount() {
        String where = "age>20 and id>1";
        String[] order = {"age", "id"};
        rel.init1("user", where, order);
        int count = rel.getCount();
        assertThat(count, is(2));
        where = "age>20 and id>100";
        rel.init1("user", where, order);
        count = rel.getCount();
        assertThat(count, is(0));
    }
}
