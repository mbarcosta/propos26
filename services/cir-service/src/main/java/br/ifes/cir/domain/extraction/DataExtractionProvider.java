package br.ifes.cir.domain.extraction;

public interface DataExtractionProvider {
    boolean supports(String strategy);
    ExtractionResult extract(InputExtractionContract contract, String content);
}
