package cn.scut.aicodesmell.controller.user;

import cn.scut.aicodesmell.common.dto.UserDto;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wanghy
 */
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserDto userDto, HttpServletRequest request) {
        if (StringUtils.isEmpty(userDto.getUserName()) || StringUtils.isEmpty(userDto.getPassword())) {
            log.info("username和password同时为空");
            return Results.paramWrong("username和password同时为空");
        }

        return userService.register(userDto, request);
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserDto userDto, HttpServletRequest request) {
        if (StringUtils.isEmpty(userDto.getUserName()) || StringUtils.isEmpty(userDto.getPassword())) {
            log.info("username和password同时为空");
            return Results.paramWrong("username和password同时为空");
        }

        return userService.login(userDto, request);
    }
}
