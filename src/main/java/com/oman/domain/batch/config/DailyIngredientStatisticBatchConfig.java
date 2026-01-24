package com.oman.domain.batch.config;

import com.oman.domain.batch.listener.BatchExecutionListener;
import com.oman.domain.batch.processor.IngredientStatisticProcessor;
import com.oman.domain.batch.reader.CulinaryItemReader;
import com.oman.domain.batch.writer.IngredientStatisticWriter;
import com.oman.domain.culinary.entity.Culinary;
import com.oman.domain.statistic.entity.IngredientStatistic;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class DailyIngredientStatisticBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final CulinaryItemReader culinaryItemReader;
    private final IngredientStatisticProcessor processor;
    private final IngredientStatisticWriter writer;

    @Bean
    public Job dailyCulinaryStatisticsJob(BatchExecutionListener listener) {
        return new JobBuilder("dailyCulinaryStatisticsJob", jobRepository)
            .listener(listener)
            .start(statisticsStep())
            .build();
    }

    @Bean
    public Step statisticsStep() {
        return new StepBuilder("statisticsStep", jobRepository)
            .<Culinary, List<IngredientStatistic>>chunk(10)
            .reader(culinaryItemReader.reader())
            .processor(processor)
            .writer(writer)
            .faultTolerant() // 결함 허용 설정 활성화
            .skip(EntityNotFoundException.class) // 특정 예외 시 스킵
            .skipLimit(10) // 최대 10개까지 허용
            .retry()
            .retryLimit(3)
            .transactionManager(transactionManager)
            .build();
    }
}
