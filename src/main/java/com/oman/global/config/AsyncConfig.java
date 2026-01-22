package com.oman.global.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class AsyncConfig {


    @Bean(destroyMethod = "shutdown")
    public ExecutorService youtubeApiExecutorService() {
        // CPU 코어 수 * 2 (일반적으로 I/O 바운드 작업에 대한 권장치)
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        long keepAliveTime = 60;

        ExecutorService executorService = Executors.newFixedThreadPool(corePoolSize);
        ((ThreadPoolExecutor) executorService).setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

        return executorService;
    }
}