package br.ifes.cpf_service.controller;

import java.util.Map;

import br.ifes.cpf_service.service.CpfValidatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CpfController {

    private final CpfValidatorService service;

    public CpfController(CpfValidatorService service) {
        this.service = service;
    }

    @GetMapping("/cpf/{cpf}")
    public Map<String, Object> validateCpf(@PathVariable String cpf) {
        return Map.of(
            "cpf", cpf,
            "valid", service.isValid(cpf)
        );
    }
}