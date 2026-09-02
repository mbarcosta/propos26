package br.ifes.cir.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import br.ifes.cir.domain.config.CirRouteDefinition;
import br.ifes.cir.domain.config.CirRouteRepository;
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

    public CirController(CirService cirService, CirRouteRepository routeRepository) {
        this.cirService = cirService;
        this.routeRepository = routeRepository;
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

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "cir-service");
    }
}
