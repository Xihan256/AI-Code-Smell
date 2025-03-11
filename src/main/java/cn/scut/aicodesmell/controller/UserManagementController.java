package cn.scut.aicodesmell.controller;

import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wanghy
 */
@RestController
@Slf4j
@RequestMapping("/api/user_mng")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @GetMapping("/logout")
    public Result userLogout(HttpServletRequest request) {
        return userService.logout(request);
    }

    @GetMapping("/sign_out")
    public Result userSignOut(@RequestParam Integer userId, HttpServletRequest request) {
        return userService.signOut(userId, request);
    }
}
