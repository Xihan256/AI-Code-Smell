package cn.scut.aicodesmell.threadPool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wanghy
 */
@Configuration
public class TextPostProcessPool {
    /**
     * 推理后分割文本线程池
     *
     * @return bean
     */
    @Bean("TaskPostProcessPool")
    public ThreadPoolTaskExecutor arDoCoThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //CPU 密集型 n - 1
        threadPoolTaskExecutor.setCorePoolSize(7);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setThreadNamePrefix("TaskPostProcessPool--");
        // 拒绝策略,当工作队列已满,线程数为最大线程数的时候,丢掉一个最旧的任务
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        // 初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
