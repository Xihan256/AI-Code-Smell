package cn.scut.aicodesmell.service;

import cn.scut.aicodesmell.common.dto.UserDto;
import cn.scut.aicodesmell.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author wanghy
 */
public interface UserService {

    String USER_LOGIN_STATE = "USER_LOGIN";

    /**
     * 注册用户
     *
     * @param userDto userDto
     * @return Result
     */
    Result register(UserDto userDto, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userDto userDto
     * @param request httpServletRequest
     * @return Result
     */
    Result login(UserDto userDto, HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request httpServletRequest
     * @return Result
     */
    Result logout(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param userId  userId
     * @param request httpServletRequest
     * @return Result
     */
    Result signOut(Integer userId, HttpServletRequest request);
}
