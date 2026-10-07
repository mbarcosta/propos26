package br.ifes.cir.domain.extraction;

import java.util.ArrayList;
import java.util.List;

public class InputExtractionContract {
    private String event;
    private String channel = "EMAIL";
    private String sourcePart = "BODY";
    private String strategy = "STRUCTURED_TEMPLATE";
    private String contractFingerprint;
    private List<ExtractionField> outputs = new ArrayList<>();
    public String getEvent() { return event; }
    public void setEvent(String value) { event = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { channel = value; }
    public String getSourcePart() { return sourcePart; }
    public void setSourcePart(String value) { sourcePart = value; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String value) { strategy = value; }
    public String getContractFingerprint() { return contractFingerprint; }
    public void setContractFingerprint(String value) { contractFingerprint = value; }
    public List<ExtractionField> getOutputs() { return outputs; }
    public void setOutputs(List<ExtractionField> value) { outputs = value == null ? new ArrayList<>() : value; }
}
