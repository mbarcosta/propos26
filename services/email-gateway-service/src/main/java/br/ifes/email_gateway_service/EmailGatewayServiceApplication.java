package br.ifes.email_gateway_service;

import br.ifes.email_gateway_service.service.EmailPollingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmailGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailGatewayServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner testPolling(EmailPollingService pollingService) {
        return args -> {
            var result = pollingService.poll("ppcomp-main");

            System.out.println("=== RESULTADO DO POLLING ===");
            System.out.println("Binding: " + result.getBindingId());
            System.out.println("Processed: " + result.getProcessedCount());
            System.out.println("Ignored: " + result.getIgnoredCount());
            System.out.println("Errors: " + result.getErrorCount());

            System.out.println("=== ITENS PROCESSADOS ===");
            for (var item : result.getResults()) {
                System.out.println("---------------------------------");
                System.out.println("From: " + item.getFrom());
                System.out.println("Subject: " + item.getSubject());
                System.out.println("Rule: " + item.getRuleName());
                System.out.println("Status: " + item.getStatus());
                System.out.println("Message: " + item.getMessage());
            }

            System.out.println("=== EVENTOS GERADOS ===");
            for (var event : result.getEvents()) {
                System.out.println("---------------------------------");
                System.out.println("BindingId: " + event.getBindingId());
                System.out.println("RuleName: " + event.getRuleName());
                System.out.println("ActionType: " + event.getActionType());
                System.out.println("ProcessKey: " + event.getProcessKey());
                System.out.println("From: " + event.getFrom());
                System.out.println("Subject: " + event.getSubject());
                System.out.println("Body: " + event.getBody());
                System.out.println("MessageNumber: " + event.getMessageNumber());
            }
        };
    }
}