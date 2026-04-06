package br.ifes.email_gateway_service.api;

import br.ifes.email_gateway_service.api.dto.MarkAsProcessedRequest;
import br.ifes.email_gateway_service.model.OperationResult;
import br.ifes.email_gateway_service.model.PollResult;
import br.ifes.email_gateway_service.service.EmailPollingService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por expor as operações do GMS (Email Gateway Service)
 * relacionadas à leitura e ao pós-processamento de mensagens de e-mail.
 *
 * <p>Este controller representa a interface HTTP do GMS e define o contrato
 * de integração com sistemas clientes (como o futuro CIR).</p>
 *
 * <p>Responsabilidades principais:</p>
 * <ul>
 *   <li>disponibilizar a operação de polling de e-mails por binding</li>
 *   <li>permitir a confirmação de processamento de mensagens</li>
 * </ul>
 *
 * <p>Importante:</p>
 * <ul>
 *   <li>o GMS não aplica regras de negócio</li>
 *   <li>não decide para qual processo a mensagem será enviada</li>
 *   <li>não interage diretamente com motores de processo (ex: Camunda)</li>
 * </ul>
 *
 * <p>O fluxo esperado de uso por um cliente (ex: CIR) é:</p>
 * <ol>
 *   <li>chamar o endpoint de polling para obter mensagens</li>
 *   <li>processar cada mensagem conforme regras de negócio</li>
 *   <li>confirmar o processamento chamando o endpoint de "processed"</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/bindings")
public class EmailPollingController {

    /**
     * Serviço responsável pela lógica de polling e pós-processamento.
     */
    private final EmailPollingService pollingService;

    /**
     * Construtor com injeção de dependência do serviço principal.
     *
     * @param pollingService serviço de polling de e-mails
     */
    public EmailPollingController(EmailPollingService pollingService) {
        this.pollingService = pollingService;
    }

    /**
     * Executa o polling da caixa postal associada a um binding.
     *
     * <p>Essa operação:</p>
     * <ul>
     *   <li>acessa a caixa de e-mail configurada</li>
     *   <li>lê mensagens conforme a política do binding</li>
     *   <li>retorna as mensagens de forma padronizada</li>
     * </ul>
     *
     * <p>Importante:</p>
     * <ul>
     *   <li>nenhuma regra de negócio é aplicada aqui</li>
     *   <li>nenhuma mensagem é movida ou alterada</li>
     * </ul>
     *
     * @param bindingId identificador do binding configurado no sistema
     * @return {@link PollResult} contendo:
     *         <ul>
     *           <li>resumo da leitura</li>
     *           <li>lista de mensagens padronizadas</li>
     *         </ul>
     */
    @PostMapping("/{bindingId}/poll")
    public PollResult poll(@PathVariable String bindingId) {
        return pollingService.poll(bindingId);
    }

    /**
     * Marca uma mensagem como processada, movendo-a para a pasta "Processed".
     *
     * <p>Esta operação deve ser chamada por um sistema cliente (ex: CIR)
     * somente após o processamento bem-sucedido da mensagem.</p>
     *
     * <p>Essa abordagem evita perda de mensagens em caso de falha no processamento,
     * garantindo que apenas mensagens efetivamente consumidas sejam removidas
     * da caixa de entrada.</p>
     *
     * <p>Fluxo esperado:</p>
     * <ol>
     *   <li>cliente chama o endpoint de polling</li>
     *   <li>processa a mensagem</li>
     *   <li>chama este endpoint para confirmar o processamento</li>
     * </ol>
     *
     * @param bindingId identificador do binding
     * @param messageNumber número da mensagem na pasta (identificador interno IMAP)
     * @return {@link OperationResult} com status da operação
     */
    @PostMapping("/{bindingId}/messages/processed")
    public OperationResult markAsProcessed(@PathVariable String bindingId,
                                           @RequestBody MarkAsProcessedRequest request) {

        pollingService.markAsProcessed(bindingId, request.getMessageId());

        return new OperationResult(
                bindingId,
                "SUCCESS",
                "Mensagem movida para a pasta Processed com sucesso. messageId=" + request.getMessageId()
        );
    }
}