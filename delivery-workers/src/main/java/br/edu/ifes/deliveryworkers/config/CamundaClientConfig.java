package br.edu.ifes.deliveryworkers.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaClientConfig {

    @Bean
    public ExternalTaskClient externalTaskClient(
            @Value("${camunda.bpm.client.base-url}") String baseUrl,
            @Value("${camunda.bpm.client.worker-id}") String workerId,
            @Value("${camunda.bpm.client.async-response-timeout}") long asyncResponseTimeout
    ) {
        return ExternalTaskClient.create()
                .baseUrl(baseUrl)
                .workerId(workerId)
                .asyncResponseTimeout(asyncResponseTimeout)
                .build();
    }
}