package br.ifes.cir.domain.config;

import java.util.ArrayList;
import java.util.List;

public class CirRouteConfig {

    private List<CirRouteDefinition> routes = new ArrayList<>();

    public List<CirRouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<CirRouteDefinition> routes) {
        this.routes = routes;
    }
}

