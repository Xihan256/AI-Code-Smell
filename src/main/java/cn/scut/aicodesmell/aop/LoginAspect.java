package cn.scut.aicodesmell.aop;

import cn.scut.aicodesmell.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * @author wanghy
 */
@Aspect
@Component
@Slf4j
public class LoginAspect {

    /**
     * 执行拦截
     */
    @Around("execution(* cn.scut.aicodesmell.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Object loginStatusObj = httpServletRequest.getSession().getAttribute(UserService.USER_LOGIN_STATE);

        boolean loginValid = Objects.nonNull(loginStatusObj) && ((boolean) loginStatusObj);
        if (loginValid) {
            return point.proceed();
        } else {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            if (response != null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"未登录! 请求被拦截\", \"code\": \"40300\"}");
                response.getWriter().flush();
            }
            //终止方法
            return null;
        }


    }
}
