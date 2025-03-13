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
}
