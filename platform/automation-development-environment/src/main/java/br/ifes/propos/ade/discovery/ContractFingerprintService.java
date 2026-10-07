package br.ifes.propos.ade.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class ContractFingerprintService {
    private final ObjectMapper mapper;
    public ContractFingerprintService(ObjectMapper mapper) { this.mapper = mapper; }
    public String fingerprint(JsonNode contract) {
        try {
            byte[] canonical = mapper.writeValueAsString(contract).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (Exception e) {
            throw new IllegalStateException("Could not fingerprint capability contract", e);
        }
    }
}
