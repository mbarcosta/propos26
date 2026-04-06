package br.ifes.cir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Classe de configuração da infraestrutura HTTP do CIR.
 *
 * <p>Seu papel, neste momento, é disponibilizar um objeto {@link RestTemplate}
 * para que outras classes da aplicação possam realizar chamadas HTTP para
 * serviços externos, como o GMS.</p>
 *
 * <p>No Spring, métodos anotados com {@code @Bean} registram objetos no
 * contêiner de injeção de dependência. Assim, qualquer classe que precise
 * de um {@code RestTemplate} poderá recebê-lo automaticamente no construtor.</p>
 */
@Configuration
public class RestConfig {

    /**
     * Cria e registra um {@link RestTemplate} no contexto do Spring.
     *
     * <p>Por enquanto, estamos usando uma configuração simples, sem timeout,
     * interceptadores ou autenticação. Mais adiante, se necessário, essa
     * configuração pode ser expandida.</p>
     *
     * @return instância padrão de {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}