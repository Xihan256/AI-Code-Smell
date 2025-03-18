package cn.scut.aicodesmell.threadPool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wanghy
 */
@Configuration
@EnableAsync
public class ProcessOrderPool {
    /**
     * ArDoCo推理用这个线程池
     *
     * @return bean
     */
    @Bean("processOrderThreadPool")
    public ThreadPoolTaskExecutor arDoCoThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //IO 密集型 2n+1
        threadPoolTaskExecutor.setCorePoolSize(17);
        threadPoolTaskExecutor.setMaxPoolSize(30);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setThreadNamePrefix("processOrderThreadPool--");
        // 拒绝策略,当工作队列已满,线程数为最大线程数的时候,丢掉一个最旧的任务
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        // 初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
