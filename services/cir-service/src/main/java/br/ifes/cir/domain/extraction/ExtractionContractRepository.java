package br.ifes.cir.domain.extraction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ExtractionContractRepository {
    private final Path file;
    private final ObjectMapper mapper;
    public ExtractionContractRepository(@Value("${cir.extractions-file:data/extractions.json}") String file, ObjectMapper mapper) {
        this.file = Path.of(file); this.mapper = mapper;
    }
    public synchronized List<InputExtractionContract> findAll() { return load(); }
    public synchronized Optional<InputExtractionContract> findByEvent(String event) {
        return load().stream().filter(item -> item.getEvent() != null && item.getEvent().equalsIgnoreCase(event)).findFirst();
    }
    public synchronized InputExtractionContract upsert(String event, InputExtractionContract contract) {
        if (event == null || event.isBlank()) throw new IllegalArgumentException("event is required");
        contract.setEvent(event);
        List<InputExtractionContract> all = new ArrayList<>(load());
        all.removeIf(item -> event.equalsIgnoreCase(item.getEvent()));
        all.add(contract); save(all); return contract;
    }
    private List<InputExtractionContract> load() {
        try {
            if (!Files.exists(file)) return List.of();
            return mapper.readValue(file.toFile(), new TypeReference<>() {});
        } catch (Exception e) { throw new IllegalStateException("Could not load CIR extraction contracts", e); }
    }
    private void save(List<InputExtractionContract> contracts) {
        try {
            if (file.toAbsolutePath().getParent() != null) Files.createDirectories(file.toAbsolutePath().getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), contracts);
        } catch (Exception e) { throw new IllegalStateException("Could not save CIR extraction contracts", e); }
    }
}
