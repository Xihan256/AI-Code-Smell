package cn.scut.aicodesmell.mapper;

import cn.scut.aicodesmell.common.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author wanghy
 */
@Mapper
public interface UserMapper {
    Integer insertUser(@Param("userEntity") UserEntity userEntity);

    UserEntity getByName(@Param("userName") String userName);

    Integer deleteUser(@Param("userId") Integer userId);
}
