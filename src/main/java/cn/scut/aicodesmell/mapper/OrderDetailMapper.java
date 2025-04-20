package cn.scut.aicodesmell.mapper;

import cn.scut.aicodesmell.common.MatchComponentEntity;
import cn.scut.aicodesmell.common.dto.OrderDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wanghy
 */
@Mapper
public interface OrderDetailMapper {
    void batchAdd(@Param("projectId") String projectId, @Param("matchComponents") List<MatchComponentEntity> matchComponents);

    void deleteById(@Param("toDeleteId") String toDeleteId);

    void deleteByIds(@Param("toDeleteIds") List<String> toDeleteIds);

    List<OrderDetailDto> getByOrderId(@Param("orderId") String orderId);
}
