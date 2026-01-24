package com.oman.domain.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngredientStatisticScheduler {
    private final JobLauncherApplicationRunner jobLauncherApplicationRunner;

//    @Scheduled(cron = "0 0 4 * * *") // production
    @Scheduled(cron = "0 */5 * * * *") // test
    public void runJob() {
        try {
            String jobName = "dailyCulinaryStatisticsJob";
            String params = "time=" + System.currentTimeMillis();

            jobLauncherApplicationRunner.run(jobName, params);

            log.info("스케줄러 Batch 실행 요청 완료");
        } catch (Exception e) {
            log.error("통계 배치 실행 실패", e);
        }
    }
}
