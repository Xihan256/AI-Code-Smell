package cn.scut.aicodesmell.aop;

import cn.scut.aicodesmell.exception.CoreTaskException;
import cn.scut.aicodesmell.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wanghy
 */
@Slf4j
@Aspect
@Component
public class ProcessTaskExceptionInterceptor {

    @Autowired
    private OrderMapper orderMapper;

    @Around("execution(* cn.scut.aicodesmell.core..*(..))")  // 拦截特定包及其子包
    public Object handleException(ProceedingJoinPoint joinPoint) {
        Thread currentThread = Thread.currentThread();
        try {
            return joinPoint.proceed();
        } catch (CoreTaskException e) {  // 只拦截业务异常
            log.error("推理过程失败!: orderId: {}, 信息: {}", e.orderId, e.getMessage());
            updateDatabaseStatusToFail(e.orderId);
            // 直接抛出异常，阻止后续执行
            throw e;
        } catch (Throwable e) {
            log.error("其他异常捕获: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void updateDatabaseStatusToFail(String orderId) {
        orderMapper.setStatusByProjectId(orderId, "failed");
    }
}
