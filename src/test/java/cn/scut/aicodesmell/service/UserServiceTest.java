package cn.scut.aicodesmell.service;

import cn.scut.aicodesmell.BaseTest;
import cn.scut.aicodesmell.common.dto.UserDto;
import cn.scut.aicodesmell.common.response.Result;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.Assert;

/**
 * @author wanghy
 */
public class UserServiceTest extends BaseTest {

    @Autowired
    UserService userService;

    @Test
    @Ignore
    public void testRegister() {
        UserDto userDto = new UserDto();
        userDto.setUserName("name");
        userDto.setPassword("password");
        MockHttpServletRequest req = new MockHttpServletRequest();
        Result result = userService.register(userDto, req);

        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    public void testLoginSuccess() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        UserDto userDto = new UserDto("name", "password");
        Result result = userService.login(userDto, req);
        Assert.isTrue(200 == result.getCode(), "应为成功");
    }

    @Test
    public void testLoginFailByPassword() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        UserDto userDto = new UserDto("name", "password1");
        Result result = userService.login(userDto, req);
        Assert.isTrue(40001 == result.getCode(), "应为失败");
    }

    @Test
    public void testLoginFailByName() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        UserDto userDto = new UserDto("name1", "password1");
        Result result = userService.login(userDto, req);
        Assert.isTrue(40001 == result.getCode(), "应为失败");
    }
}
