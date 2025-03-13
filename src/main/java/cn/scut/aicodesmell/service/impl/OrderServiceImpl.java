package cn.scut.aicodesmell.service.impl;

import cn.scut.aicodesmell.common.OrderEntity;
import cn.scut.aicodesmell.common.PageEntity;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.factory.PageFactory;
import cn.scut.aicodesmell.mapper.OrderMapper;
import cn.scut.aicodesmell.service.OrderService;
import cn.scut.aicodesmell.util.SecurityUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author wanghy
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result createOrder(Integer userId) {
        String orderId = SecurityUtils.get64bitUUID();

        //插入一条订单
        Integer rowInserted = orderMapper.insertOrder(orderId, userId);

        if (rowInserted < 1) {
            log.error("创建订单失败, orderId: {}", orderId);
            return Results.internalError("创建订单失败, 请重试");
        }

        return Results.ok(orderId);
    }

    @Override
    public Result deleteOrder(String orderId, Integer userId) {
        OrderEntity orderEntity = orderMapper.getOrderById(orderId);

        //订单不存在
        if (Objects.isNull(orderEntity) || !userId.equals(orderEntity.getUserId())) {
            log.info("查询的订单不存在或不属于用户, orderId: {}, userId: {}", orderId, userId);
            return Results.paramWrong("查询的订单不存在或不属于用户");
        }

        orderMapper.deleteOrder(orderId);
        return Results.ok("已删除");
    }

    @Override
    public Result getSingleOrder(String orderId, Integer userId) {
        OrderEntity orderEntity = orderMapper.getOrderById(orderId);

        //订单不存在
        if (Objects.isNull(orderEntity) || !userId.equals(orderEntity.getUserId())) {
            log.info("查询的订单不存在或不属于用户, orderId: {}, userId: {}", orderId, userId);
            return Results.paramWrong("查询的订单不存在或不属于用户");
        }

        Object json = JSON.toJSON(orderEntity);
        return Results.ok(json);
    }

    @Override
    public Result getOrderCount(Integer userId) {
        Integer count = orderMapper.getUserOrderCount(userId);
        return Results.ok(count);
    }

    @Override
    public Result getOrderByPage(Integer userId, Integer total, Integer curPage, Integer pageSize) {
        PageFactory.PagePO pageHelper = PageFactory.pageHelper(curPage, pageSize);
        PageEntity<OrderEntity> objectPageEntity = PageFactory.buildPage(total, curPage, pageSize);

        //查询
        List<OrderEntity> orders = orderMapper.batchGetOrderByPage(userId, pageHelper.limit, pageHelper.offset);
        if (Objects.isNull(orders)) {
            orders = Collections.emptyList();
        }
        objectPageEntity.setData(orders);
        return Results.ok(JSON.toJSON(objectPageEntity));
    }
}
