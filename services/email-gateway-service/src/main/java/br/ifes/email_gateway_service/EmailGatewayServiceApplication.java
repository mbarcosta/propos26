package br.ifes.email_gateway_service;

import br.ifes.email_gateway_service.config.RuleLoader;
import br.ifes.email_gateway_service.dispatcher.ProcessDispatcher;
import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.EmailRule;
import br.ifes.email_gateway_service.repository.MailBindingRepository;
import br.ifes.email_gateway_service.service.MailReaderService;
import br.ifes.email_gateway_service.service.RuleMatcher;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;


@SpringBootApplication
public class EmailGatewayServiceApplication {
	public static void main(String[] args) {
	    SpringApplication.run(EmailGatewayServiceApplication.class, args);
	    
	}
	 @Bean
	    CommandLineRunner testBinding(MailBindingRepository repository) {
	        return args -> {

	            var binding = repository.findById("ppcomp-main");

	            if (binding == null) {
	                System.out.println("Binding não encontrado");
	                return;
	            }

	            System.out.println("=== TESTE DE BINDING ===");
	            System.out.println("ID: " + binding.getId());
	            System.out.println("Nome: " + binding.getName());
	            System.out.println("Email: " + binding.getMailboxAddress());
	            System.out.println("Host: " + binding.getImapHost());
	            System.out.println("Porta: " + binding.getImapPort());
	            System.out.println("Regras: " + binding.getRules().size());
	        };
	    }
}