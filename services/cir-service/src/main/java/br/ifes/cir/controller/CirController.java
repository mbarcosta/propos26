package br.ifes.cir.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import br.ifes.cir.domain.config.CirRouteDefinition;
import br.ifes.cir.domain.config.CirRouteRepository;
import br.ifes.cir.domain.extraction.ExtractionContractRepository;
import br.ifes.cir.domain.extraction.InputExtractionContract;
import br.ifes.cir.domain.model.CirExecutionResult;
import br.ifes.cir.service.CirService;

/**
 * Controller REST do CIR.
 */
@RestController
@RequestMapping("/api/cir")
public class CirController {

    private final CirService cirService;
    private final CirRouteRepository routeRepository;
    private final ExtractionContractRepository extractionRepository;

    public CirController(CirService cirService, CirRouteRepository routeRepository,
                         ExtractionContractRepository extractionRepository) {
        this.cirService = cirService;
        this.routeRepository = routeRepository;
        this.extractionRepository = extractionRepository;
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

    @GetMapping("/routes")
    public List<CirRouteDefinition> routes() {
        return routeRepository.findAll();
    }

    @PostMapping("/routes")
    public List<CirRouteDefinition> replaceRoutes(@RequestBody List<CirRouteDefinition> routes) {
        routeRepository.replaceAll(routes);
        return routeRepository.findAll();
    }

    @GetMapping("/extractions")
    public List<InputExtractionContract> extractionContracts() {
        return extractionRepository.findAll();
    }

    @PutMapping("/extractions/{event}")
    public InputExtractionContract upsertExtraction(@PathVariable String event,
                                                     @RequestBody InputExtractionContract contract) {
        return extractionRepository.upsert(event, contract);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "cir-service");
    }
}
