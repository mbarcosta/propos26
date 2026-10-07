package br.ifes.ppg.management.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/** Runtime contract endpoint owned and versioned by the domain provider. */
@RestController
public class OpenApiController {
    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> apiDocs() throws IOException {
        return ResponseEntity.ok(new ClassPathResource("openapi-runtime.json").getInputStream().readAllBytes());
    }
}
