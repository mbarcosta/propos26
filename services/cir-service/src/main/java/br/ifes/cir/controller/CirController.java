package br.ifes.cir.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ifes.cir.domain.model.CirExecutionResult;
import br.ifes.cir.service.CirService;

/**
 * Controller REST do CIR.
 */
@RestController
@RequestMapping("/api/cir")
public class CirController {

    private final CirService cirService;

    public CirController(CirService cirService) {
        this.cirService = cirService;
    }

    /**
     * Executa o CIR para o binding informado.
     *
     * @param bindingId identificador do binding
     * @return resultado consolidado do CIR
     */
    @PostMapping("/execute")
    public CirExecutionResult execute(@RequestParam String bindingId) {
        return cirService.execute(bindingId);
    }
}