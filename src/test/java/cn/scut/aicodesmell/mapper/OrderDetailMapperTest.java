package cn.scut.aicodesmell.mapper;

import cn.scut.aicodesmell.BaseTest;
import cn.scut.aicodesmell.common.MatchComponentEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghy
 */
public class OrderDetailMapperTest extends BaseTest {
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Test
    public void testBatchInsert() {
        String projectId = "YzA3MWExMTItMGMzOS00OTg1LWE4N2EtYTk0ZTUxMWM4NDBi0000000000000000";
        List<MatchComponentEntity> list = new ArrayList<>();
        list.add(new MatchComponentEntity("aaa", 0.8, "com.acc.aaa"));
        list.add(new MatchComponentEntity("bbb", 0.8, "com.bcc.bbb"));
        list.add(new MatchComponentEntity("ccc", 1.0, "com.ccc.ccc"));
        orderDetailMapper.batchAdd(projectId, list);
    }
}
