package cn.scut.aicodesmell.service;

import cn.scut.aicodesmell.BaseTest;
import cn.scut.aicodesmell.common.response.Result;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * @author wanghy
 */
public class OrderServiceTest extends BaseTest {
    @Autowired
    private OrderService orderService;

    @Test
    public void createOrder() {
        int userId = 4;
        Result result = orderService.createOrder(userId);

        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    public void deleteOrder() {
        int userId = 4;
        String orderId = "MGFiOTM2ZWItNjFjYy00YWEzLWFjZDMtNmZiZDg1N2FjMzQ00000000000000000";
        Result result = orderService.deleteOrder(orderId, userId);

        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    @Ignore
    public void deleteOrderFail() {
        int userId = 5;
        String orderId = "MGFiOTM2ZWItNjFjYy00YWEzLWFjZDMtNmZiZDg1N2FjMzQ00000000000000000";
        Result result = orderService.deleteOrder(orderId, userId);

        Assert.isTrue(40001 == result.getCode(), "应为失败");
    }

    @Test
    public void getSingleOrder() {
        int userId = 4;
        String orderId = "MGFiOTM2ZWItNjFjYy00YWEzLWFjZDMtNmZiZDg1N2FjMzQ00000000000000000";
        Result result = orderService.getSingleOrder(orderId, userId);
        System.out.println(result);
        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    public void getOrderCount() {
        int userId = 4;

        Result result = orderService.getOrderCount(userId);
        System.out.println(result.getData());
        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    public void getOrderByPage() {
        int userId = 4;
        int total = 4;
        int curPage = 2;
        int pageSize = 2;

        Result result = orderService.getOrderByPage(userId, total, curPage, pageSize);

        System.out.println(result.getData());
        Assert.isTrue(200 == result.getCode(), "应为成功");
    }
}
