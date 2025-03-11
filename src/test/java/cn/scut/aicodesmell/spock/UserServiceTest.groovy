package cn.scut.aicodesmell.spock

import cn.scut.aicodesmell.common.UserEntity
import cn.scut.aicodesmell.common.dto.UserDto
import cn.scut.aicodesmell.common.response.Result
import cn.scut.aicodesmell.mapper.UserMapper
import cn.scut.aicodesmell.service.impl.UserServiceImpl
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import spock.lang.Specification

class UserServiceTest extends Specification {

    def userMapper = Mock(UserMapper)

    def userService = new UserServiceImpl(userMapper: userMapper)

    def "register"() {
        given:
        UserDto userDto = new UserDto(userName: "name1", password: "password")
        userMapper.getByName(*_) >> userGetted
        userMapper.insertUser(*_) >> 1
        HttpServletRequest httpServletRequest = Mock(HttpServletRequest)
        HttpSession session = Mock(HttpSession)
        httpServletRequest.getSession() >> session

        session.setAttribute(*_) >> {}
        session.invalidate() >> {}

        when:
        Result result = userService.register(userDto, httpServletRequest)

        then:
        result.code == code

        where:
        userGetted                                                       | code
        null                                                             | 200
        new UserEntity(userId: 1, userName: "name1", password: "123123") | 40002
    }

    def "login"() {
        given:
        HttpServletRequest httpServletRequest = Mock(HttpServletRequest)
        HttpSession session = Mock(HttpSession)
        httpServletRequest.getSession() >> session

        session.setAttribute(*_) >> {}
        session.invalidate() >> {}
        userMapper.getByName(*_) >> userGetted

        when:
        Result result = userService.login(userDto, httpServletRequest)

        then:
        result.code == code

        where:
        userGetted                                                                                 | userDto                                            | code
        null                                                                                       | new UserDto(userName: "name1", password: "123123") | 40001
        new UserEntity(userId: 1, userName: "name1", password: "123123")                           | new UserDto(userName: "name1", password: "123123") | 40001
        new UserEntity(userId: 1, userName: "name1", password: "4297f44b13955235245b2497399d7a93") | new UserDto(userName: "name1", password: "123123") | 200
    }
}
