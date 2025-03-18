package cn.scut.aicodesmell.mapper;

import cn.scut.aicodesmell.common.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wanghy
 */
@Mapper
public interface OrderMapper {

    Integer insertOrder(@Param("orderId") String orderId, @Param("userId") Integer userId);

    OrderEntity getOrderById(@Param("orderId") String orderId);

    void deleteOrder(@Param("orderId") String orderId);

    Integer getUserOrderCount(@Param("userId") Integer userId);

    List<OrderEntity> batchGetOrderByPage(@Param("userId") Integer userId, @Param("limit") Integer limit, @Param("offset") Integer offset);

    void batchDeleteExpiredOrder(Integer differentDays);

    void updateCodeFilePath(@Param("orderId") String orderId, @Param("path") String path);

    void updateDocFilePath(@Param("orderId") String orderId, @Param("path") String path);

    String getOrderStatus(@Param("orderId") String orderId);

    List<OrderEntity> getExpiredOrders(Integer differentDays);

    void setStatusByProjectId(@Param("orderId") String orderId, @Param("status") String status);

    /*
     * 这个同时也要设置状态为finished, 不用事务了, 直接改
     */
    void setResultUrl(@Param("orderId") String orderId, @Param("resultUrl") String resultUrl);
}
