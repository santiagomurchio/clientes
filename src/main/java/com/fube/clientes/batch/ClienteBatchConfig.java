package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ClienteBatchConfig {

    @Bean
    public Step fillClientesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  ClienteItemReader reader,
                                  ClienteItemProcessor processor,
                                  ClienteItemWriter writer) {
        return new StepBuilder("fillClientesStep", jobRepository)
                .<Cliente, Cliente>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job fillClientesJob(JobRepository jobRepository, Step fillClientesStep) {
        return new JobBuilder("fillClientesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .start(fillClientesStep)
                .build();
    }

    private JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("=== Iniciando job: {} ===", jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("=== Job finalizado: {} | Estado: {} ===",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStatus());
            }
        };
    }
}
