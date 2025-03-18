package cn.scut.aicodesmell.service;

import cn.scut.aicodesmell.common.response.Result;

/**
 * @author wanghy
 */
public interface OrderService {
    /**
     * 创建订单
     *
     * @param userId userId
     * @return 订单Id
     */
    Result createOrder(Integer userId);

    /**
     * 删除订单
     *
     * @param orderId orderId
     * @param userId  userId
     * @return Result
     */
    Result deleteOrder(String orderId, Integer userId);

    /**
     * 获取单个订单ById
     *
     * @param orderId orderId
     * @param userId  userId
     * @return Result
     */
    Result getSingleOrder(String orderId, Integer userId);

    /**
     * 获取订单总数量
     *
     * @param userId userId
     * @return Result
     */
    Result getOrderCount(Integer userId);

    /**
     * 分页获取订单
     *
     * @param userId   userId
     * @param total    总数
     * @param curPage  当前页
     * @param pageSize 页大小
     * @return Result
     */
    Result getOrderByPage(Integer userId, Integer total, Integer curPage, Integer pageSize);
}
