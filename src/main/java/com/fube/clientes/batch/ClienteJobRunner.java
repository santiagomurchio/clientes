package com.fube.clientes.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClienteJobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job fillClientesJob;

    public ClienteJobRunner(JobLauncher jobLauncher, Job fillClientesJob) {
        this.jobLauncher = jobLauncher;
        this.fillClientesJob = fillClientesJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        log.info("Lanzando job manualmente con timestamp={}", params.getLong("timestamp"));
        jobLauncher.run(fillClientesJob, params);
    }
}
