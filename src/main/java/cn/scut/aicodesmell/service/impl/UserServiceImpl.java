package cn.scut.aicodesmell.service.impl;

import cn.scut.aicodesmell.common.UserEntity;
import cn.scut.aicodesmell.common.dto.UserDto;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.mapper.UserMapper;
import cn.scut.aicodesmell.service.UserService;
import cn.scut.aicodesmell.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author wanghy
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result register(UserDto userDto, HttpServletRequest request) {
        String passwordEncrypted = SecurityUtils.String2MD5(userDto.getPassword());
        //确认用户名不存在
        UserEntity user = userMapper.getByName(userDto.getUserName());
        if (Objects.nonNull(user)) {
            return Results.userNameError("用户name已被占用");
        }

        UserEntity userEntity = new UserEntity(null, userDto.getUserName(), passwordEncrypted);
        userMapper.insertUser(userEntity);
        log.info("创建用户成功, id: {}", userEntity.getUserId());
        request.getSession().setAttribute(USER_LOGIN_STATE, Boolean.TRUE);
        return Results.ok(userEntity.getUserId());
    }

    @Override
    public Result login(UserDto userDto, HttpServletRequest request) {
        UserEntity user = userMapper.getByName(userDto.getUserName());
        if (Objects.isNull(user)) {
            return Results.paramWrong("用户不存在");
        }

        String passwordEncrypted = SecurityUtils.String2MD5(userDto.getPassword());
        if (!passwordEncrypted.equals(user.getPassword())) {
            //作废当前session
            request.getSession().invalidate();
            return Results.paramWrong("密码错误");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, Boolean.TRUE);
        return Results.ok(user.getUserId());
    }

    @Override
    public Result logout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        //作废当前session
        request.getSession().invalidate();
        return Results.ok("success");
    }

    @Override
    public Result signOut(Integer userId, HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        //作废当前session
        request.getSession().invalidate();

        userMapper.deleteUser(userId);
        log.info("用户注销, id: {}", userId);
        return Results.ok("success");
    }
}
