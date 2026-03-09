package com.pcs8.orientasi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;

/**
 * Konfigurasi untuk async task executor.
 * 
 * <p>Digunakan terutama untuk audit logging agar tidak memblok
 * main transaction.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool executor khusus untuk audit tasks.
     * 
     * <p>Konfigurasi:</p>
     * <ul>
     *   <li>Core pool size: 2 threads</li>
     *   <li>Max pool size: 5 threads</li>
     *   <li>Queue capacity: 100 tasks</li>
     *   <li>Thread name prefix: "Audit-"</li>
     *   <li>TaskDecorator untuk copy request context ke async thread</li>
     * </ul>
     */
    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // TaskDecorator untuk meng-copy request context ke async thread
        executor.setTaskDecorator(new ContextCopyingTaskDecorator());
        
        executor.initialize();
        return executor;
    }

    /**
     * TaskDecorator yang meng-copy RequestContextHolder ke async thread.
     * Ini memungkinkan audit service mendapatkan user info dari JWT
     * meskipun berjalan di thread terpisah.
     */
    static class ContextCopyingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // Capture request attributes dari main thread
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            
            return () -> {
                try {
                    // Set request attributes ke async thread
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes, true);
                    }
                    runnable.run();
                } finally {
                    // Clean up setelah task selesai
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
