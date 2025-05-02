package cn.scut.aicodesmell.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wanghy
 */
@Mapper
public interface ComponentDocPhrasesMapper {
    void batchAdd(@Param("orderId") String orderId, @Param("componentName") String componentName,
                  @Param("phrasesMatching") List<String> phrasesMatching);

    List<String> getByOrderIdAndComponentName(@Param("orderId") String orderId, @Param("componentName") String componentName);

    List<String> getByOrderIdLim3(@Param("orderId") String orderId, @Param("componentName") String componentName);
}
